package com.example.shoppingcart.dto;

import com.example.shoppingcart.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private List<CartItem> items;
    private int totalQuantity;
    private double totalAmount;
}
