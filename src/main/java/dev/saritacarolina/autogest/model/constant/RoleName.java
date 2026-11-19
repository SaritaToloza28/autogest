package dev.saritacarolina.autogest.model.constant;

import org.springframework.security.core.GrantedAuthority;

//Roles de la aplicación. Implementa GrantedAuthority para que Spring Security lo use directamente.
//La tabla app_role se llena con estos mismos valores desde data.sql.
public enum RoleName implements GrantedAuthority {

    //Lo de los paréntesis es el nombre que se muestra en pantalla.
    //Spring Security usa name() (ROLE_ADMIN, ROLE_OPERATOR, ROLE_MECHANIC), no el label.
    ROLE_ADMIN("Administrador"),
    ROLE_OPERATOR("Operador"),
    ROLE_MECHANIC("Mecánico");

    private final String label;

    RoleName(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String getAuthority() {
        return name();
    }
}
