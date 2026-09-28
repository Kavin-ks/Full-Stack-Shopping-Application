package com.flipkart.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Vue.js → API Gateway → { user | product | cart | order } service.
 * Only /api/** paths are routed; the services' /internal/** endpoints are unreachable from the browser.
 */
@Configuration
public class RoutesConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder,
                               @Value("${services.user-url}") String userUrl,
                               @Value("${services.product-url}") String productUrl,
                               @Value("${services.cart-url}") String cartUrl,
                               @Value("${services.order-url}") String orderUrl) {
        return builder.routes()
                .route("user-service", r -> r.path("/api/auth/**", "/api/users/**").uri(userUrl))
                .route("product-service", r -> r.path("/api/products", "/api/products/**").uri(productUrl))
                .route("cart-service", r -> r.path("/api/cart", "/api/cart/**").uri(cartUrl))
                .route("order-service", r -> r.path("/api/orders", "/api/orders/**").uri(orderUrl))
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter(@Value("${app.cors.allowed-origins}") List<String> origins) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(origins);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cors);
        return new CorsWebFilter(source);
    }
}
