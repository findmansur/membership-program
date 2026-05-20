package com.firstclub.membership.domain.dto;

public record UserDto(
        Long id,
        String name,
        String email,
        String cohort
) {
}
