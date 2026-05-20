package com.firstclub.membership.domain.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderDto(
        Long id,
        Long userId,
        BigDecimal amount,
        Instant createdAt
) {
}
