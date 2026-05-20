package com.firstclub.membership.domain.dto;

import com.firstclub.membership.domain.entity.Subscription;
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
    public static SubscriptionDto from(Subscription s) {
        return new SubscriptionDto(
                s.getId(),
                s.getUserId(),
                s.getPlan().getCode(),
                s.getTier().getCode(),
                s.getStatus(),
                s.getStartAt(),
                s.getEndAt(),
                s.isAutoRenew(),
                s.getVersion()
        );
    }
}
