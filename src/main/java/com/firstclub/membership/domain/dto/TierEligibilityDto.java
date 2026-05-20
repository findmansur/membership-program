package com.firstclub.membership.domain.dto;

import java.util.List;

public record TierEligibilityDto(
        String tierCode,
        String tierName,
        int rank,
        boolean eligible,
        List<String> notes
) {
}
