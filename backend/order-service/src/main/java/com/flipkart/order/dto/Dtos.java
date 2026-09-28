package com.flipkart.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flipkart.order.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Map;

public final class Dtos {
    private Dtos() {}

    public record AddressInput(
            @NotBlank String name,
            @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$", message = "must be a valid 10-digit mobile number") String mobile,
            @NotBlank @Pattern(regexp = "^[1-9]\\d{5}$", message = "must be a valid 6-digit pincode") String pincode,
            @NotBlank String locality,
            @NotBlank String address,
            @NotBlank String city,
            @NotBlank String state,
            String landmark,
            String alternatePhone,
            String type) {}

    public record LineInput(@NotBlank String productId, @Min(1) @Max(10) int quantity) {}

    /**
     * Places an order. When {@code items} is empty the whole cart is checked out;
     * otherwise it is a "Buy Now" order for just those items.
     */
    public record PlaceOrderRequest(
            @NotNull @Valid AddressInput address,
            @NotBlank @Pattern(regexp = "UPI|CARD|NETBANKING|COD") String paymentMethod,
            @Valid List<LineInput> items) {}

    public record CancelRequest(String reason) {}

    public record StatusUpdate(@NotNull OrderStatus status) {}

    public record OrderPage(List<com.flipkart.order.model.Order> content, long totalElements, int totalPages, int page) {}

    public record OrderStats(long totalOrders, long activeOrders, long revenue, Map<String, Long> byStatus) {}

    // ---- shapes returned by other services

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductInfo(String id, String title, String brand, String thumbnail,
                              int mrp, int price, int stock) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CartLine(String productId, int quantity, boolean inStock, ProductInfo product) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CartResponse(List<CartLine> items) {}

    public record StockLine(String productId, int quantity) {}
}
