package com.firstclub.membership.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserCohortRequest(
        @NotBlank String cohort
) {
}
