package com.firstclub.membership.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotNull Long userId,
        @NotNull @DecimalMin("0.0") BigDecimal amount
) {
}
