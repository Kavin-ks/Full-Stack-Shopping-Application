package com.flipkart.product.controller;

import com.flipkart.product.dto.Dtos.*;
import com.flipkart.product.exception.ApiException;
import com.flipkart.product.model.Product;
import com.flipkart.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public catalogue API, reached through the API gateway at /api/products.
 * The gateway validates the JWT and forwards the caller as X-User-* headers.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<Product> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> subCategory,
            @RequestParam(required = false) List<String> brand,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Integer minDiscount,
            @RequestParam(defaultValue = "false") boolean assured,
            @RequestParam(defaultValue = "false") boolean inStock,
            @RequestParam(defaultValue = "relevance") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        return service.search(new ProductService.SearchParams(q, category, subCategory, brand, minPrice, maxPrice,
                minRating, minDiscount, assured, inStock, sort, page, size));
    }

    @GetMapping("/facets")
    public Facets facets(@RequestParam(required = false) String q, @RequestParam(required = false) String category) {
        return service.facets(q, category);
    }

    @GetMapping("/categories")
    public List<Category> categories() {
        return service.categories();
    }

    @GetMapping("/suggest")
    public List<Suggestion> suggest(@RequestParam(defaultValue = "") String q) {
        return service.suggest(q);
    }

    @PostMapping("/batch")
    public List<Product> batch(@Valid @RequestBody BatchRequest req) {
        return service.batch(req.ids());
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable String id) {
        return service.get(id);
    }

    @GetMapping("/{id}/similar")
    public List<Product> similar(@PathVariable String id) {
        return service.similar(id);
    }

    @PostMapping("/{id}/reviews")
    public Product review(@PathVariable String id,
                          @RequestHeader("X-User-Id") String userId,
                          @RequestHeader(value = "X-User-Name", defaultValue = "Flipkart Customer") String userName,
                          @Valid @RequestBody ReviewRequest req) {
        return service.addReview(id, userId, userName, req);
    }

    // ---------------------------------------------------------------- seller hub (ADMIN only)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
                          @Valid @RequestBody ProductRequest req) {
        requireAdmin(role);
        return service.create(req);
    }

    @PutMapping("/{id}")
    public Product update(@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
                          @PathVariable String id, @Valid @RequestBody ProductRequest req) {
        requireAdmin(role);
        return service.update(id, req);
    }

    @PatchMapping("/{id}/stock")
    public Product updateStock(@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
                               @PathVariable String id, @Valid @RequestBody StockUpdate req) {
        requireAdmin(role);
        return service.updateStock(id, req.stock());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(value = "X-User-Role", defaultValue = "") String role, @PathVariable String id) {
        requireAdmin(role);
        service.delete(id);
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) throw ApiException.forbidden("Seller access required");
    }
}
