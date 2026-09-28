package com.example.shoppingcart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cart")
public class CartItem {
    @Id
    private String id;
    private String productId;
    private String name;
    private double price;
    private int quantity;
    private double subTotal;
    private String emoji;
}
