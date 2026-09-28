package com.flipkart.cart.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "cart_items")
@CompoundIndex(name = "user_product", def = "{'userId': 1, 'productId': 1}", unique = true)
public class CartItem {

    @Id
    private String id;
    private String userId;
    private String productId;
    private int quantity;
    private boolean savedForLater;
    private Instant addedAt = Instant.now();

    public CartItem() {}

    public CartItem(String userId, String productId, int quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public boolean isSavedForLater() { return savedForLater; }
    public void setSavedForLater(boolean savedForLater) { this.savedForLater = savedForLater; }
    public Instant getAddedAt() { return addedAt; }
    public void setAddedAt(Instant addedAt) { this.addedAt = addedAt; }
}
