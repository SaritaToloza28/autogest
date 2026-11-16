package dev.saritacarolina.autogest.model.dto;

import dev.saritacarolina.autogest.model.constant.Role;

public record AppUserDTO(
        String username,
        String fullName,
        String password,
        Role role
) {
}