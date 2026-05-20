package com.firstclub.membership.domain.dto;

import com.firstclub.membership.domain.enums.BillingCycle;

import java.math.BigDecimal;

public record PlanDto(
        Long id,
        String code,
        String name,
        BillingCycle billingCycle,
        int durationDays,
        BigDecimal price
) {
}
