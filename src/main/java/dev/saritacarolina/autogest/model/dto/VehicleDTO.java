package dev.saritacarolina.autogest.model.dto;

import dev.saritacarolina.autogest.model.constant.PlateColor;

//TODO: añadir teléfono
public record VehicleDTO(
        String licensePlate,
        PlateColor plateColor,
        String ownerName,
        Long carModelId
) {
}