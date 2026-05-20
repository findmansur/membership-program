package com.firstclub.membership.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutPreviewRequest(
        @NotNull Long userId,
        @NotEmpty @Valid List<Item> items,
        @NotNull @DecimalMin("0.0") BigDecimal deliveryFee
) {
    public record Item(
            @NotNull String sku,
            @NotNull String category,
            @NotNull @DecimalMin("0.0") BigDecimal price,
            int quantity
    ) {
    }
}
