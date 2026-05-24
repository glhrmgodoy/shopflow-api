package com.godoy.order.dto.request;

import com.godoy.order.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OrderRequest(

        @NotNull(message = "ID do cliente é obrigatório")
        UUID customerId,

        @NotNull(message = "Método de pagamento é obrigatório")
        PaymentMethod paymentMethod,

        @NotEmpty(message = "Pedido deve ter pelo menos um item")
        List<OrderItemRequest> items,

        String notes
) {
}
