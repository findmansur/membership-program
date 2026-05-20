package com.firstclub.membership.domain.dto;

import java.util.List;

public record TierDto(
        Long id,
        String code,
        String name,
        String description,
        int rank,
        List<TierBenefitDto> benefits,
        List<String> eligibilityCriteria
) {
}
