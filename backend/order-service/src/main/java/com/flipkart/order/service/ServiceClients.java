package com.flipkart.order.service;

import com.flipkart.order.dto.Dtos.*;
import com.flipkart.order.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/** HTTP clients for the product and cart services that checkout depends on. */
@Component
public class ServiceClients {

    private final RestClient products;
    private final RestClient carts;

    public ServiceClients(@Value("${services.product-url}") String productUrl,
                          @Value("${services.cart-url}") String cartUrl) {
        this.products = RestClient.builder().baseUrl(productUrl).build();
        this.carts = RestClient.builder().baseUrl(cartUrl).build();
    }

    public List<ProductInfo> products(List<String> ids) {
        return call("Product", () -> products.post().uri("/api/products/batch")
                .body(Map.of("ids", ids)).retrieve()
                .body(new ParameterizedTypeReference<List<ProductInfo>>() {}));
    }

    public CartResponse cart(String userId) {
        return call("Cart", () -> carts.get().uri("/internal/cart/{userId}", userId).retrieve().body(CartResponse.class));
    }

    public void clearCart(String userId) {
        call("Cart", () -> carts.delete().uri("/internal/cart/{userId}", userId).retrieve().toBodilessEntity());
    }

    public void reserveStock(List<StockLine> lines) {
        call("Product", () -> products.post().uri("/internal/stock/reserve")
                .body(Map.of("items", lines)).retrieve().toBodilessEntity());
    }

    public void releaseStock(List<StockLine> lines) {
        call("Product", () -> products.post().uri("/internal/stock/release")
                .body(Map.of("items", lines)).retrieve().toBodilessEntity());
    }

    /** Re-throws downstream errors with their original status and message (e.g. "Only 2 left in stock"). */
    private <T> T call(String service, java.util.function.Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientResponseException e) {
            String message = service + " service error";
            try {
                Map<?, ?> body = e.getResponseBodyAs(Map.class);
                if (body != null && body.get("message") != null) message = body.get("message").toString();
            } catch (Exception ignored) {
                // body was not JSON; keep the generic message
            }
            HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
            throw new ApiException(status == null ? HttpStatus.BAD_GATEWAY : status, message);
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, service + " service is unavailable. Please try again");
        }
    }
}
