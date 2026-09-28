package com.example.shoppingcart.controller;

import com.example.shoppingcart.dto.CartResponse;
import com.example.shoppingcart.model.Product;
import com.example.shoppingcart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(@RequestBody Map<String, Object> payload) {
        String productId = (String) payload.get("productId");
        int quantity = payload.containsKey("quantity") ? ((Number) payload.get("quantity")).intValue() : 1;
        return ResponseEntity.ok(cartService.addItem(productId, quantity, null));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartResponse> updateQuantity(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        int quantity = ((Number) payload.get("quantity")).intValue();
        return ResponseEntity.ok(cartService.updateQuantity(id, quantity));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable String id) {
        return ResponseEntity.ok(cartService.removeItem(id));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartResponse> clearCart() {
        return ResponseEntity.ok(cartService.clearCart());
    }
}
