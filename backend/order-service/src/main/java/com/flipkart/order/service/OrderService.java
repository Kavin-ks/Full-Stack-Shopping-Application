package com.flipkart.order.service;

import com.flipkart.order.dto.Dtos.*;
import com.flipkart.order.exception.ApiException;
import com.flipkart.order.model.Order;
import com.flipkart.order.model.Order.DeliveryAddress;
import com.flipkart.order.model.Order.OrderItem;
import com.flipkart.order.model.Order.StatusEvent;
import com.flipkart.order.model.OrderStatus;
import com.flipkart.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final int FREE_DELIVERY_THRESHOLD = 500;
    private static final int DELIVERY_CHARGE = 40;

    private final OrderRepository orders;
    private final ServiceClients clients;
    private final SecureRandom random = new SecureRandom();

    public OrderService(OrderRepository orders, ServiceClients clients) {
        this.orders = orders;
        this.clients = clients;
    }

    /**
     * Checkout saga: resolve items with live prices → reserve stock (all-or-nothing) → save order → clear cart.
     * If saving fails after stock was reserved, the reservation is released again.
     */
    public Order place(String userId, PlaceOrderRequest req) {
        boolean fromCart = req.items() == null || req.items().isEmpty();
        List<OrderItem> items = fromCart ? itemsFromCart(userId) : itemsFromBuyNow(req.items());

        List<StockLine> stock = items.stream().map(i -> new StockLine(i.productId(), i.quantity())).toList();
        clients.reserveStock(stock);

        Order order;
        try {
            order = orders.save(build(userId, req, items));
        } catch (RuntimeException e) {
            clients.releaseStock(stock);
            throw e;
        }

        if (fromCart) {
            try {
                clients.clearCart(userId);
            } catch (ApiException e) {
                log.warn("Order {} placed but cart could not be cleared: {}", order.getOrderNumber(), e.getMessage());
            }
        }
        return order;
    }

    public List<Order> list(String userId) {
        return orders.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order get(String userId, String orderId) {
        Order o = find(orderId);
        if (!o.getUserId().equals(userId)) throw ApiException.notFound("Order not found");
        return o;
    }

    public Order cancel(String userId, String orderId, String reason) {
        Order o = get(userId, orderId);
        if (!o.getStatus().isCancellable()) {
            throw ApiException.badRequest("This order can no longer be cancelled");
        }
        clients.releaseStock(o.getItems().stream().map(i -> new StockLine(i.productId(), i.quantity())).toList());
        o.setStatus(OrderStatus.CANCELLED);
        o.setCancelReason(reason == null || reason.isBlank() ? "Cancelled by customer" : reason);
        o.getStatusHistory().add(new StatusEvent(OrderStatus.CANCELLED, Instant.now(), o.getCancelReason()));
        if ("PAID".equals(o.getPaymentStatus())) o.setPaymentStatus("REFUNDED");
        return orders.save(o);
    }

    // ------------------------------------------------------------------ seller hub

    public OrderPage all(int page, int size) {
        Page<Order> p = orders.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
        return new OrderPage(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber());
    }

    public Order updateStatus(String orderId, OrderStatus next) {
        Order o = find(orderId);
        OrderStatus current = o.getStatus();
        if (current == OrderStatus.CANCELLED || current == OrderStatus.DELIVERED) {
            throw ApiException.badRequest("Order is already " + current.name().toLowerCase());
        }
        if (next == OrderStatus.CANCELLED) {
            return cancel(o.getUserId(), orderId, "Cancelled by seller");
        }
        if (next.ordinal() <= current.ordinal()) {
            throw ApiException.badRequest("Order status can only move forward");
        }
        o.setStatus(next);
        o.getStatusHistory().add(new StatusEvent(next, Instant.now(), describe(next)));
        if (next == OrderStatus.DELIVERED) {
            o.setPaymentStatus("PAID");
            o.setExpectedDelivery(Instant.now());
        }
        return orders.save(o);
    }

    public OrderStats stats() {
        List<Order> all = orders.findAll();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (OrderStatus s : OrderStatus.values()) byStatus.put(s.name(), 0L);
        all.forEach(o -> byStatus.merge(o.getStatus().name(), 1L, Long::sum));
        long revenue = all.stream().filter(o -> o.getStatus() != OrderStatus.CANCELLED).mapToLong(Order::getTotalAmount).sum();
        long active = all.stream().filter(o -> o.getStatus().isCancellable() || o.getStatus() == OrderStatus.OUT_FOR_DELIVERY).count();
        return new OrderStats(all.size(), active, revenue, byStatus);
    }

    // ------------------------------------------------------------------ helpers

    private List<OrderItem> itemsFromCart(String userId) {
        CartResponse cart = clients.cart(userId);
        if (cart.items() == null || cart.items().isEmpty()) throw ApiException.badRequest("Your cart is empty");
        Optional<CartLine> unavailable = cart.items().stream().filter(l -> !l.inStock()).findFirst();
        if (unavailable.isPresent()) {
            throw ApiException.conflict(unavailable.get().product().title()
                    + " is out of stock. Please remove it or save it for later");
        }
        return cart.items().stream().map(l -> toItem(l.product(), l.quantity())).toList();
    }

    private List<OrderItem> itemsFromBuyNow(List<LineInput> lines) {
        Map<String, Integer> qty = lines.stream()
                .collect(Collectors.toMap(LineInput::productId, LineInput::quantity, Integer::sum, LinkedHashMap::new));
        Map<String, ProductInfo> byId = clients.products(new ArrayList<>(qty.keySet())).stream()
                .collect(Collectors.toMap(ProductInfo::id, p -> p));
        List<OrderItem> items = new ArrayList<>();
        qty.forEach((id, q) -> {
            ProductInfo p = byId.get(id);
            if (p == null) throw ApiException.notFound("Product not found: " + id);
            items.add(toItem(p, q));
        });
        return items;
    }

    private OrderItem toItem(ProductInfo p, int quantity) {
        // Prices always come from product-service, never from the browser.
        return new OrderItem(p.id(), p.title(), p.brand(), p.thumbnail(), p.mrp(), p.price(), quantity);
    }

    private Order build(String userId, PlaceOrderRequest req, List<OrderItem> items) {
        int mrp = items.stream().mapToInt(i -> i.mrp() * i.quantity()).sum();
        int price = items.stream().mapToInt(i -> i.price() * i.quantity()).sum();
        int delivery = price >= FREE_DELIVERY_THRESHOLD ? 0 : DELIVERY_CHARGE;

        AddressInput a = req.address();
        Order o = new Order();
        o.setOrderNumber(newOrderNumber());
        o.setUserId(userId);
        o.setItems(items);
        o.setAddress(new DeliveryAddress(a.name(), a.mobile(), a.pincode(), a.locality(), a.address(), a.city(),
                a.state(), a.landmark(), a.alternatePhone(), a.type()));
        o.setPaymentMethod(req.paymentMethod());
        o.setPaymentStatus("COD".equals(req.paymentMethod()) ? "PENDING" : "PAID");
        o.setTotalMrp(mrp);
        o.setTotalDiscount(mrp - price);
        o.setDeliveryCharge(delivery);
        o.setTotalAmount(price + delivery);
        o.setCreatedAt(Instant.now());
        o.setExpectedDelivery(Instant.now().plus(Duration.ofDays(3 + random.nextInt(4))));
        o.getStatusHistory().add(new StatusEvent(OrderStatus.PLACED, o.getCreatedAt(), describe(OrderStatus.PLACED)));
        return o;
    }

    /** Flipkart-style order id: "OD" followed by 18 digits. */
    private String newOrderNumber() {
        StringBuilder sb = new StringBuilder("OD").append(System.currentTimeMillis());
        while (sb.length() < 20) sb.append(random.nextInt(10));
        return sb.toString();
    }

    private static String describe(OrderStatus s) {
        return switch (s) {
            case PLACED -> "Your order has been placed";
            case SHIPPED -> "Your item has been shipped";
            case OUT_FOR_DELIVERY -> "Your item is out for delivery";
            case DELIVERED -> "Your item has been delivered";
            case CANCELLED -> "Your order has been cancelled";
        };
    }

    private Order find(String orderId) {
        return orders.findById(orderId).orElseThrow(() -> ApiException.notFound("Order not found"));
    }
}
