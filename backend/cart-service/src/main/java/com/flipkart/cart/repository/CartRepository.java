package com.flipkart.cart.repository;

import com.flipkart.cart.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends MongoRepository<CartItem, String> {
    List<CartItem> findByUserIdOrderByAddedAtDesc(String userId);
    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);
    long countByUserIdAndSavedForLaterFalse(String userId);
    void deleteByUserIdAndProductId(String userId, String productId);
    void deleteByUserIdAndSavedForLaterFalse(String userId);
}
