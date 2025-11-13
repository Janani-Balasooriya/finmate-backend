package com.finmate.finmate_backend.families.dto;

public record MemberDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String role
) {}