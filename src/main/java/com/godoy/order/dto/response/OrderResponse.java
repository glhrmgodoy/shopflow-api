package com.godoy.order.dto.response;

import com.godoy.order.dto.request.OrderItemRequest;
import com.godoy.order.model.enums.OrderStatus;
import com.godoy.order.model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerName,
        PaymentMethod paymentMethod,
        BigDecimal totalAmount,
        OrderStatus status,
        String notes,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
}
