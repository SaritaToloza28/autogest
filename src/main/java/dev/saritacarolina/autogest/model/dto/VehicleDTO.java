package dev.saritacarolina.autogest.model.dto;

import dev.saritacarolina.autogest.model.constant.PlateColor;

public record VehicleDTO(
        String licensePlate,
        PlateColor plateColor,
        String ownerName,
        Long carModelId
) {
}