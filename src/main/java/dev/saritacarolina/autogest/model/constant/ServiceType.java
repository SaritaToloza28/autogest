package dev.saritacarolina.autogest.model.constant;

public enum ServiceType {

    //Deben iniciar de la misma forma
    SERVICE_INTERNAL("En el taller"),
    SERVICE_EXTERNAL("A domicilio");

    //TODO: averiguar bien lo de rich enum
    private final String label;

    ServiceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
