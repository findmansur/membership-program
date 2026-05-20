package com.firstclub.membership.benefit;

import com.firstclub.membership.domain.enums.BenefitType;

import java.math.BigDecimal;

/**
 * Outcome produced by a benefit. Benefits are deliberately not allowed to
 * mutate the cart — they only describe their effect on the totals
 * ({@code discount}, {@code deliveryWaived}) and surface a human-readable
 * {@code description} for display in checkout.
 */
public record BenefitResult(
        BenefitType type,
        BigDecimal discount,
        boolean deliveryWaived,
        String description
) {
    public static BenefitResult none(BenefitType type, String description) {
        return new BenefitResult(type, BigDecimal.ZERO, false, description);
    }

    public static BenefitResult discount(BenefitType type, BigDecimal amount, String description) {
        return new BenefitResult(type, amount, false, description);
    }

    public static BenefitResult freeDelivery(BenefitType type, String description) {
        return new BenefitResult(type, BigDecimal.ZERO, true, description);
    }

    public static BenefitResult informational(BenefitType type, String description) {
        return new BenefitResult(type, BigDecimal.ZERO, false, description);
    }
}
