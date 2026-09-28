package com.flipkart.product.config;

import com.flipkart.product.model.Product;
import com.flipkart.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;

/**
 * Loads the product catalogue / stock file (resources/data/products.json) into MongoDB
 * the first time the service starts against an empty database.
 */
@Component
public class ProductSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductSeeder.class);

    private final ProductRepository repository;
    private final ObjectMapper mapper;

    public ProductSeeder(ProductRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0) {
            log.info("Product catalogue already present ({} products)", repository.count());
            return;
        }
        try (InputStream in = new ClassPathResource("data/products.json").getInputStream()) {
            List<Product> products = mapper.readValue(in, new TypeReference<>() {});
            // Stagger createdAt so "Newest First" sorting has a stable, meaningful order.
            Instant now = Instant.now();
            for (int i = 0; i < products.size(); i++) {
                products.get(i).setCreatedAt(now.minusSeconds((long) (products.size() - i) * 3600));
            }
            repository.saveAll(products);
            log.info("Seeded {} products from data/products.json", products.size());
        }
    }
}
