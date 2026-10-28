package dev.saritacarolina.autogest.model.dto;

import dev.saritacarolina.autogest.model.constant.ServiceType;

import java.math.BigDecimal;

//Datos que registra el operador. El estado y los repuestos se cambian con acciones aparte.
public record RepairDTO(
        String reason,
        ServiceType serviceType,
        BigDecimal latitude,
        BigDecimal longitude,
        Long vehicleId,
        Long mechanicId
) {
}
