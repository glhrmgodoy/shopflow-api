package com.godoy.order.controller;

import com.godoy.order.dto.request.OrderRequest;
import com.godoy.order.dto.response.OrderResponse;
import com.godoy.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody @Valid OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> findAllByCustomerId(@PathVariable UUID customerId) {
        return ResponseEntity.ok(orderService.findAllByCustomerId(customerId));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.confirm(id));
    }

    @PatchMapping("/{id}/ship")
    public ResponseEntity<OrderResponse> ship(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.ship(id));
    }

    @PatchMapping("/{id}/deliver")
    public ResponseEntity<OrderResponse> deliver(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.deliver(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        orderService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
