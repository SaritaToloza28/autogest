package dev.saritacarolina.autogest.model.dto;

import dev.saritacarolina.autogest.model.constant.PlateColor;
import dev.saritacarolina.autogest.model.constant.RepairStatus;

import java.time.LocalDateTime;

//Consulta pública (/repair-status): solo lo que el cliente puede ver.
//Nunca lleva datos del propietario, del mecánico ni la ubicación.
//plateColor pinta la placa y status da el color del estado (status.label es el texto).
public record RepairStatusDTO(
        LocalDateTime repairDate,
        String licensePlate,
        PlateColor plateColor,
        String carModelName,
        String reason,
        RepairStatus status
) {
}
