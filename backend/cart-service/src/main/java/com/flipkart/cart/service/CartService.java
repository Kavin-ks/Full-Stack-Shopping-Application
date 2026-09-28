package com.flipkart.cart.service;

import com.flipkart.cart.dto.Dtos.*;
import com.flipkart.cart.exception.ApiException;
import com.flipkart.cart.model.CartItem;
import com.flipkart.cart.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CartService {

    /** Flipkart caps quantity per item. */
    public static final int MAX_QUANTITY_PER_ITEM = 10;
    /** Orders at or above this amount get free delivery. */
    public static final int FREE_DELIVERY_THRESHOLD = 500;
    public static final int DELIVERY_CHARGE = 40;

    private final CartRepository repository;
    private final ProductClient products;

    public CartService(CartRepository repository, ProductClient products) {
        this.repository = repository;
        this.products = products;
    }

    /** Builds the cart with live prices and stock from product-service and computes the price details. */
    public CartResponse getCart(String userId) {
        List<CartItem> items = repository.findByUserIdOrderByAddedAtDesc(userId);
        Map<String, ProductInfo> byId = products.findAll(items.stream().map(CartItem::getProductId).toList())
                .stream().collect(Collectors.toMap(ProductInfo::id, Function.identity()));

        List<CartLine> active = new ArrayList<>();
        List<CartLine> saved = new ArrayList<>();
        for (CartItem item : items) {
            ProductInfo p = byId.get(item.getProductId());
            if (p == null) {                       // product was removed from the catalogue
                repository.delete(item);
                continue;
            }
            int max = Math.min(p.stock(), MAX_QUANTITY_PER_ITEM);
            if (p.stock() > 0 && item.getQuantity() > max) {   // stock dropped below what's in the cart
                item.setQuantity(max);
                repository.save(item);
            }
            CartLine line = new CartLine(p.id(), item.getQuantity(), Math.max(max, 1), p.stock() > 0, p);
            (item.isSavedForLater() ? saved : active).add(line);
        }
        return new CartResponse(active, saved, summarize(active));
    }

    public CartResponse addItem(String userId, AddItemRequest req) {
        ProductInfo p = requireProduct(req.productId());
        if (p.stock() <= 0) throw ApiException.conflict(p.title() + " is out of stock");

        CartItem item = repository.findByUserIdAndProductId(userId, req.productId())
                .orElseGet(() -> new CartItem(userId, req.productId(), 0));
        int wanted = item.isSavedForLater() ? req.quantity() : item.getQuantity() + req.quantity();
        item.setQuantity(Math.min(wanted, Math.min(p.stock(), MAX_QUANTITY_PER_ITEM)));
        item.setSavedForLater(false);
        item.setAddedAt(Instant.now());
        repository.save(item);
        return getCart(userId);
    }

    public CartResponse updateQuantity(String userId, String productId, int quantity) {
        CartItem item = requireItem(userId, productId);
        ProductInfo p = requireProduct(productId);
        if (quantity > p.stock()) {
            throw ApiException.conflict("We're sorry! Only " + p.stock() + " unit(s) available for this item");
        }
        item.setQuantity(quantity);
        repository.save(item);
        return getCart(userId);
    }

    public CartResponse remove(String userId, String productId) {
        repository.deleteByUserIdAndProductId(userId, productId);
        return getCart(userId);
    }

    public CartResponse setSavedForLater(String userId, String productId, boolean saved) {
        CartItem item = requireItem(userId, productId);
        item.setSavedForLater(saved);
        item.setAddedAt(Instant.now());
        repository.save(item);
        return getCart(userId);
    }

    public long count(String userId) {
        return repository.countByUserIdAndSavedForLaterFalse(userId);
    }

    /** Called by order-service after a successful checkout. Saved-for-later items stay. */
    public void clearActive(String userId) {
        repository.deleteByUserIdAndSavedForLaterFalse(userId);
    }

    public static PriceSummary summarize(List<CartLine> lines) {
        int count = 0, mrp = 0, price = 0;
        for (CartLine l : lines) {
            if (!l.inStock()) continue;
            count += l.quantity();
            mrp += l.product().mrp() * l.quantity();
            price += l.product().price() * l.quantity();
        }
        int delivery = count == 0 || price >= FREE_DELIVERY_THRESHOLD ? 0 : DELIVERY_CHARGE;
        int discount = mrp - price;
        return new PriceSummary(count, mrp, discount, delivery, price + delivery, discount);
    }

    private CartItem requireItem(String userId, String productId) {
        return repository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> ApiException.notFound("Item is not in your cart"));
    }

    private ProductInfo requireProduct(String productId) {
        return products.findAll(List.of(productId)).stream().findFirst()
                .orElseThrow(() -> ApiException.notFound("Product not found"));
    }
}
