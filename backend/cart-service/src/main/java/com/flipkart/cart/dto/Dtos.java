package com.flipkart.cart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class Dtos {
    private Dtos() {}

    /** The subset of product-service's Product that the cart needs. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductInfo(String id, String title, String brand, String category, String subCategory,
                              int mrp, int price, int discount, int stock, String thumbnail,
                              double rating, int ratingCount, boolean assured) {}

    public record AddItemRequest(@NotBlank String productId, @Min(1) @Max(10) int quantity) {}

    public record QuantityRequest(@Min(1) @Max(10) int quantity) {}

    public record CartLine(String productId, int quantity, int maxQuantity, boolean inStock, ProductInfo product) {}

    /** Mirrors Flipkart's "PRICE DETAILS" box. */
    public record PriceSummary(int itemCount, int totalMrp, int totalDiscount, int deliveryCharge,
                               int totalAmount, int savings) {}

    public record CartResponse(List<CartLine> items, List<CartLine> savedForLater, PriceSummary summary) {}
}
