package dev.saritacarolina.autogest.model.dto;

import dev.saritacarolina.autogest.model.constant.RoleName;

//El formulario envía el nombre del rol (ROLE_ADMIN...); el servicio busca la entidad Role
public record AppUserDTO(
        String username,
        String fullName,
        String password,
        RoleName role
) {
}
