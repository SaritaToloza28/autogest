package dev.saritacarolina.autogest.model.dto;

import dev.saritacarolina.autogest.model.constant.PlateColor;

public record VehicleDTO(
        Long id,
        String licensePlate,
        PlateColor plateColor,
        String ownerName,
        Long carModelId
) {
}