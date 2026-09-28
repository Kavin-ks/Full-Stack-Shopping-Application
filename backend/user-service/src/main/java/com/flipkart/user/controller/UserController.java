package com.flipkart.user.controller;

import com.flipkart.user.dto.Dtos.*;
import com.flipkart.user.model.Address;
import com.flipkart.user.service.AuthService;
import com.flipkart.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** All routes require login; the gateway injects the verified user id as X-User-Id. */
@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public UserView me(@RequestHeader("X-User-Id") String userId) {
        return AuthService.toView(users.get(userId));
    }

    @PutMapping
    public UserView update(@RequestHeader("X-User-Id") String userId, @Valid @RequestBody ProfileUpdate req) {
        return users.updateProfile(userId, req);
    }

    @GetMapping("/addresses")
    public List<Address> addresses(@RequestHeader("X-User-Id") String userId) {
        return users.addresses(userId);
    }

    @PostMapping("/addresses")
    public List<Address> addAddress(@RequestHeader("X-User-Id") String userId, @Valid @RequestBody AddressRequest req) {
        return users.addAddress(userId, req);
    }

    @PutMapping("/addresses/{id}")
    public List<Address> updateAddress(@RequestHeader("X-User-Id") String userId, @PathVariable String id,
                                       @Valid @RequestBody AddressRequest req) {
        return users.updateAddress(userId, id, req);
    }

    @DeleteMapping("/addresses/{id}")
    public List<Address> deleteAddress(@RequestHeader("X-User-Id") String userId, @PathVariable String id) {
        return users.deleteAddress(userId, id);
    }

    @GetMapping("/wishlist")
    public List<String> wishlist(@RequestHeader("X-User-Id") String userId) {
        return users.wishlist(userId);
    }

    @PostMapping("/wishlist/{productId}")
    public List<String> addToWishlist(@RequestHeader("X-User-Id") String userId, @PathVariable String productId) {
        return users.addToWishlist(userId, productId);
    }

    @DeleteMapping("/wishlist/{productId}")
    public List<String> removeFromWishlist(@RequestHeader("X-User-Id") String userId, @PathVariable String productId) {
        return users.removeFromWishlist(userId, productId);
    }
}
