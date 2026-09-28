package com.flipkart.cart.controller;

import com.flipkart.cart.dto.Dtos.*;
import com.flipkart.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Logged-in user's cart. The gateway injects the verified user id as X-User-Id. */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cart;

    public CartController(CartService cart) {
        this.cart = cart;
    }

    @GetMapping
    public CartResponse get(@RequestHeader("X-User-Id") String userId) {
        return cart.getCart(userId);
    }

    @GetMapping("/count")
    public Map<String, Long> count(@RequestHeader("X-User-Id") String userId) {
        return Map.of("count", cart.count(userId));
    }

    @PostMapping("/items")
    public CartResponse add(@RequestHeader("X-User-Id") String userId, @Valid @RequestBody AddItemRequest req) {
        return cart.addItem(userId, req);
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateQuantity(@RequestHeader("X-User-Id") String userId, @PathVariable String productId,
                                       @Valid @RequestBody QuantityRequest req) {
        return cart.updateQuantity(userId, productId, req.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse remove(@RequestHeader("X-User-Id") String userId, @PathVariable String productId) {
        return cart.remove(userId, productId);
    }

    @PostMapping("/items/{productId}/save-for-later")
    public CartResponse saveForLater(@RequestHeader("X-User-Id") String userId, @PathVariable String productId) {
        return cart.setSavedForLater(userId, productId, true);
    }

    @PostMapping("/items/{productId}/move-to-cart")
    public CartResponse moveToCart(@RequestHeader("X-User-Id") String userId, @PathVariable String productId) {
        return cart.setSavedForLater(userId, productId, false);
    }
}
