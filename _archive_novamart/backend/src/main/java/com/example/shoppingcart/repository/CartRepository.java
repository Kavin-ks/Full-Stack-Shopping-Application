package com.example.shoppingcart.repository;

import com.example.shoppingcart.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<CartItem, String> {
    Optional<CartItem> findByProductId(String productId);
}
