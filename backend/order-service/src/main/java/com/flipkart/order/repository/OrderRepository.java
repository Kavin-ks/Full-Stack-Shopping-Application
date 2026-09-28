package com.flipkart.order.repository;

import com.flipkart.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
