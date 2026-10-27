package dev.saritacarolina.autogest.model.dto;

import dev.saritacarolina.autogest.model.constant.Role;

//TODO: revisar en todos los DTO si es String o la clase
public record AppUserDTO(
        String username,
        String fullName,
        String password,
        Role role
) {
}