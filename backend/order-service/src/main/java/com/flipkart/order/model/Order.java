package com.flipkart.order.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
public class Order {

    @Id
    private String id;
    @Indexed(unique = true)
    private String orderNumber;
    @Indexed
    private String userId;
    private List<OrderItem> items = new ArrayList<>();
    private DeliveryAddress address;
    /** UPI, CARD, NETBANKING or COD */
    private String paymentMethod;
    /** PAID, PENDING (cash on delivery) or REFUNDED */
    private String paymentStatus;
    private OrderStatus status = OrderStatus.PLACED;
    private List<StatusEvent> statusHistory = new ArrayList<>();
    private int totalMrp;
    private int totalDiscount;
    private int deliveryCharge;
    private int totalAmount;
    private Instant createdAt = Instant.now();
    private Instant expectedDelivery;
    private String cancelReason;

    public record OrderItem(String productId, String title, String brand, String thumbnail,
                            int mrp, int price, int quantity) {}

    public record StatusEvent(OrderStatus status, Instant at, String note) {}

    public record DeliveryAddress(String name, String mobile, String pincode, String locality, String address,
                                  String city, String state, String landmark, String alternatePhone, String type) {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public DeliveryAddress getAddress() { return address; }
    public void setAddress(DeliveryAddress address) { this.address = address; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public List<StatusEvent> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<StatusEvent> statusHistory) { this.statusHistory = statusHistory; }
    public int getTotalMrp() { return totalMrp; }
    public void setTotalMrp(int totalMrp) { this.totalMrp = totalMrp; }
    public int getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(int totalDiscount) { this.totalDiscount = totalDiscount; }
    public int getDeliveryCharge() { return deliveryCharge; }
    public void setDeliveryCharge(int deliveryCharge) { this.deliveryCharge = deliveryCharge; }
    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getExpectedDelivery() { return expectedDelivery; }
    public void setExpectedDelivery(Instant expectedDelivery) { this.expectedDelivery = expectedDelivery; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
}
