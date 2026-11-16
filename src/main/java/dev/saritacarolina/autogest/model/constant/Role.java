package dev.saritacarolina.autogest.model.constant;

public enum Role {

    //Lo de los paréntesis es el nombre que se muestra en pantalla.
    //Spring Security usa name() (ADMIN, OPERATOR, MECHANIC), no el label.
    ADMIN("Administrador"),
    OPERATOR("Operador"),
    MECHANIC("Mecánico");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
