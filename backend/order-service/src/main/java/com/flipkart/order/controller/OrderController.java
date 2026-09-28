package com.flipkart.order.controller;

import com.flipkart.order.dto.Dtos.*;
import com.flipkart.order.exception.ApiException;
import com.flipkart.order.model.Order;
import com.flipkart.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** The gateway injects the verified caller as X-User-Id / X-User-Role. */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order place(@RequestHeader("X-User-Id") String userId, @Valid @RequestBody PlaceOrderRequest req) {
        return orders.place(userId, req);
    }

    @GetMapping
    public List<Order> list(@RequestHeader("X-User-Id") String userId) {
        return orders.list(userId);
    }

    @GetMapping("/{id}")
    public Order get(@RequestHeader("X-User-Id") String userId, @PathVariable String id) {
        return orders.get(userId, id);
    }

    @PostMapping("/{id}/cancel")
    public Order cancel(@RequestHeader("X-User-Id") String userId, @PathVariable String id,
                        @RequestBody(required = false) CancelRequest req) {
        return orders.cancel(userId, id, req == null ? null : req.reason());
    }

    // ---------------------------------------------------------------- seller hub (ADMIN only)

    @GetMapping("/admin/all")
    public OrderPage all(@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
                           @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        requireAdmin(role);
        return orders.all(page, size);
    }

    @GetMapping("/admin/stats")
    public OrderStats stats(@RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        requireAdmin(role);
        return orders.stats();
    }

    @PatchMapping("/admin/{id}/status")
    public Order updateStatus(@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
                              @PathVariable String id, @Valid @RequestBody StatusUpdate req) {
        requireAdmin(role);
        return orders.updateStatus(id, req.status());
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) throw ApiException.forbidden("Seller access required");
    }
}
