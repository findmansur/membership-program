package com.firstclub.membership.domain.dto;

import com.firstclub.membership.domain.enums.SubscriptionStatus;

import java.time.Instant;

public record SubscriptionDto(
        Long id,
        Long userId,
        String planCode,
        String tierCode,
        SubscriptionStatus status,
        Instant startAt,
        Instant endAt,
        boolean autoRenew,
        long version
) {
}
