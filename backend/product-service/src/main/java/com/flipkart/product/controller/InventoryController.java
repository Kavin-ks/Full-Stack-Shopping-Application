package com.flipkart.product.controller;

import com.flipkart.product.dto.Dtos.StockRequest;
import com.flipkart.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Service-to-service inventory API. Not routed by the API gateway, so browsers cannot call it;
 * only order-service uses it to reserve stock on checkout and release it on cancellation.
 */
@RestController
@RequestMapping("/internal/stock")
public class InventoryController {

    private final ProductService service;

    public InventoryController(ProductService service) {
        this.service = service;
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserve(@Valid @RequestBody StockRequest req) {
        service.reserve(req.items());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> release(@Valid @RequestBody StockRequest req) {
        service.release(req.items());
        return ResponseEntity.ok().build();
    }
}
