package com.godoy.order.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String email,
        String cpf,
        String phone,
        String address,
        LocalDateTime createdAt
) {
}
