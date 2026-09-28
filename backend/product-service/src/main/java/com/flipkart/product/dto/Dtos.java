package com.flipkart.product.dto;

import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Map;

public final class Dtos {
    private Dtos() {}

    public record PageResponse<T>(List<T> content, long totalElements, int totalPages, int page, int size) {}

    public record Facet(String name, long count) {}

    public record Facets(List<Facet> brands, List<Facet> subCategories, int minPrice, int maxPrice) {}

    public record Category(String slug, String name, long count, String image, List<String> subCategories) {}

    public record Suggestion(String type, String id, String title, String category, String thumbnail) {}

    public record StockLine(@NotBlank String productId, @Min(1) int quantity) {}

    public record StockRequest(@NotEmpty List<StockLine> items) {}

    public record StockUpdate(@Min(0) int stock) {}

    public record BatchRequest(@NotNull List<String> ids) {}

    public record ReviewRequest(@Min(1) @Max(5) int rating, @Size(max = 80) String title,
                                @NotBlank @Size(max = 1000) String comment) {}

    public record ProductRequest(
            @NotBlank String title,
            @NotBlank String brand,
            @NotBlank String description,
            @NotBlank String category,
            @NotBlank String categoryName,
            @NotBlank String subCategory,
            @Min(1) int mrp,
            @Min(1) int price,
            @Min(0) int stock,
            @NotBlank String thumbnail,
            List<String> images,
            List<String> highlights,
            Map<String, String> specifications,
            String warranty,
            String returnPolicy) {}
}
