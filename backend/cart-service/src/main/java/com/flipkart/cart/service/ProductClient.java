package com.flipkart.cart.service;

import com.flipkart.cart.dto.Dtos.ProductInfo;
import com.flipkart.cart.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/** HTTP client for product-service. */
@Component
public class ProductClient {

    private final RestClient http;

    public ProductClient(@Value("${services.product-url}") String baseUrl) {
        this.http = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<ProductInfo> findAll(List<String> ids) {
        if (ids.isEmpty()) return List.of();
        try {
            return http.post().uri("/api/products/batch")
                    .body(Map.of("ids", ids))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ProductInfo>>() {});
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Product service is unavailable. Please try again");
        }
    }
}
