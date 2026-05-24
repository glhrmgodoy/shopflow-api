package com.godoy.order.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(

        @NotBlank(message = "Nome é obrigatório")
        String name,

        String description,

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
        BigDecimal price,

        @NotNull(message = "Quantidade em estoque é obrigatório")
        @Min(value = 0, message = "Estoque não pode ser negativo")
        Integer stockQuantity,

        @NotNull(message = "ID da categoria é obrigatório")
        UUID categoryId
) {
}
