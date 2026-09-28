package com.flipkart.cart.controller;

import com.flipkart.cart.dto.Dtos.CartResponse;
import com.flipkart.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Service-to-service API used by order-service; not exposed through the API gateway. */
@RestController
@RequestMapping("/internal/cart/{userId}")
public class InternalCartController {

    private final CartService cart;

    public InternalCartController(CartService cart) {
        this.cart = cart;
    }

    @GetMapping
    public CartResponse get(@PathVariable String userId) {
        return cart.getCart(userId);
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(@PathVariable String userId) {
        cart.clearActive(userId);
        return ResponseEntity.noContent().build();
    }
}
