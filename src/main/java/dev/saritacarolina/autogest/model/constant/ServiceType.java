package dev.saritacarolina.autogest.model.constant;

public enum ServiceType {

    //Lo de los parentesis es para la salida del textp
    SERVICE_INTERNAL("En el taller"),
    SERVICE_EXTERNAL("A domicilio");

    private final String label;

    ServiceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
