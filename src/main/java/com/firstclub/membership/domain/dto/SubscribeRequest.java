package com.firstclub.membership.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubscribeRequest(
        @NotNull Long userId,
        @NotBlank String planCode,
        @NotBlank String tierCode,
        boolean autoRenew
) {
}
