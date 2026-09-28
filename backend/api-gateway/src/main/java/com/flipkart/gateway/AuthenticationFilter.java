package com.flipkart.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Central authentication for every microservice.
 * <ol>
 *   <li>Drops any X-User-* headers sent by the browser so identities can't be spoofed.</li>
 *   <li>Verifies the Bearer JWT (issued by user-service) and forwards the caller as X-User-Id / X-User-Name / X-User-Role.</li>
 *   <li>Rejects protected routes without a valid token (401) and seller routes for non-admins (403).</li>
 * </ol>
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Pattern PRODUCT_REVIEW = Pattern.compile("^/api/products/[^/]+/reviews$");

    private final SecretKey key;

    public AuthenticationFilter(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        Claims claims = null;
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            try {
                claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7)).getPayload();
            } catch (JwtException | IllegalArgumentException e) {
                return reject(exchange, HttpStatus.UNAUTHORIZED, "Your session has expired. Please login again");
            }
        }

        Access access = accessFor(path, method);
        if (access != Access.PUBLIC && claims == null) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "Please login to continue");
        }
        if (access == Access.ADMIN && !"ADMIN".equals(claims.get("role", String.class))) {
            return reject(exchange, HttpStatus.FORBIDDEN, "Seller access required");
        }

        final Claims user = claims;
        ServerHttpRequest forwarded = request.mutate().headers(h -> {
            h.remove("X-User-Id");
            h.remove("X-User-Name");
            h.remove("X-User-Role");
            if (user != null) {
                h.set("X-User-Id", user.getSubject());
                h.set("X-User-Name", String.valueOf(user.get("name", String.class)));
                h.set("X-User-Role", String.valueOf(user.get("role", String.class)));
            }
        }).build();
        return chain.filter(exchange.mutate().request(forwarded).build());
    }

    private enum Access { PUBLIC, USER, ADMIN }

    private Access accessFor(String path, HttpMethod method) {
        if (path.startsWith("/api/orders/admin")) return Access.ADMIN;
        if (path.startsWith("/api/cart") || path.startsWith("/api/orders") || path.startsWith("/api/users")) return Access.USER;
        if (path.startsWith("/api/products")) {
            if (HttpMethod.GET.equals(method) || HttpMethod.OPTIONS.equals(method)) return Access.PUBLIC;
            if (path.equals("/api/products/batch")) return Access.PUBLIC;
            if (PRODUCT_REVIEW.matcher(path).matches()) return Access.USER;
            return Access.ADMIN;                          // create / update / delete / stock
        }
        return Access.PUBLIC;                             // /api/auth/**
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":" + status.value() + ",\"error\":\"" + status.getReasonPhrase()
                + "\",\"message\":\"" + message + "\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
