package dev.saritacarolina.autogest.model.dto;

import java.time.LocalDateTime;

//Consulta pública (/repair-status): solo lo que el cliente puede ver.
//Nunca lleva datos del propietario, del mecánico ni la ubicación.
public record RepairStatusDTO(
        LocalDateTime repairDate,
        String licensePlate,
        String carModelName,
        String reason,
        String statusLabel
) {
}
