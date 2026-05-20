package com.firstclub.membership.domain.dto;

import com.firstclub.membership.domain.entity.MembershipPlan;
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
    public static PlanDto from(MembershipPlan plan) {
        return new PlanDto(
                plan.getId(),
                plan.getCode(),
                plan.getName(),
                plan.getBillingCycle(),
                plan.getBillingCycle().getDurationDays(),
                plan.getBasePrice()
        );
    }
}
