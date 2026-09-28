package com.flipkart.product.repository;

import com.flipkart.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findTop12ByCategoryAndIdNotOrderByRatingDesc(String category, String id);
}
