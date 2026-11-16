package dev.saritacarolina.autogest.model.dto;

import dev.saritacarolina.autogest.model.constant.PlateColor;

public record VehicleDTO(
        String licensePlate,
        PlateColor plateColor,
        String ownerName,
        String ownerPhone,
        Long carModelId
) {

    //Formulario vacío (creación)
    public static VehicleDTO empty(){
        return new VehicleDTO("", null, "", "", null);
    }

    //Solo la placa y el color (búsqueda o registro de un vehículo nuevo en la recepción)
    public static VehicleDTO withPlate(String licensePlate, PlateColor plateColor){
        return new VehicleDTO(licensePlate == null ? "" : licensePlate, plateColor, "", "", null);
    }
}
