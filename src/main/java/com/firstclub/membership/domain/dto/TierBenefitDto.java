package com.firstclub.membership.domain.dto;

import com.firstclub.membership.domain.enums.BenefitType;

public record TierBenefitDto(
        BenefitType type,
        String description,
        String summary
) {
}
