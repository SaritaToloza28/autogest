package dev.saritacarolina.autogest.model.constant;

public enum RepairStatus {

    PENDING("Pendiente"),
    IN_PROGRESS("En progreso"),
    COMPLETED("Completada"),
    DELIVERED("Entregada"),
    CANCELLED("Cancelada");

    private final String label;

    RepairStatus(String label) {
        this.label = label;
    }

    //Orden fijo: PENDING -> IN_PROGRESS -> COMPLETED -> DELIVERED.
    //Solo se cancela antes de completar; entregada o cancelada ya no cambia.
    public boolean canChangeTo(RepairStatus next){
        return switch (this) {
            case PENDING -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == COMPLETED || next == CANCELLED;
            case COMPLETED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    public String getLabel() {
        return label;
    }
}
