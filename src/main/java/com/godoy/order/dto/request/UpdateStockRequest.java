package com.godoy.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateStockRequest(

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 0, message = "Estoque não pode ser negativo")
        Integer stockQuantity
) {
}
