package com.firstclub.membership.domain.dto;

import com.firstclub.membership.benefit.BenefitResult;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutPreviewResponse(
        Long userId,
        String activeTier,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal totalDiscount,
        BigDecimal finalAmount,
        List<BenefitResult> appliedBenefits
) {
}
