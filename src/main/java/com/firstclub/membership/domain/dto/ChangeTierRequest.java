package com.firstclub.membership.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeTierRequest(@NotBlank String tierCode) {
}
