package dev.saritacarolina.autogest.service;

import dev.saritacarolina.autogest.model.constant.PlateColor;
import dev.saritacarolina.autogest.model.dto.VehicleDTO;
import dev.saritacarolina.autogest.model.entity.CarModel;
import dev.saritacarolina.autogest.model.entity.Vehicle;
import dev.saritacarolina.autogest.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository repository;
    private final CarModelService carModelService;

    public VehicleService(VehicleRepository repository, CarModelService carModelService) {
        this.repository = repository;
        this.carModelService = carModelService;
    }

    public List<Vehicle> findAllVehicles(){
        return this.repository.findAllByOrderByLicensePlateAsc();
    }

    public Vehicle findVehicleById(Long vehicleId) throws EntityNotFoundException {
        return this.repository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehículo con id " + vehicleId + " no encontrado"));
    }

    public VehicleDTO createVehicle(VehicleDTO vehicleRequest) throws EntityNotFoundException, IllegalArgumentException {
        validateRequest(vehicleRequest);

        String licensePlate = normalizePlate(vehicleRequest.licensePlate(), vehicleRequest.plateColor());

        if (this.repository.existsByLicensePlate(licensePlate)){
            throw new IllegalArgumentException("Ya existe un vehículo con placa " + licensePlate);
        }

        CarModel carModel = this.carModelService.findCarModelById(vehicleRequest.carModelId());

        Vehicle vehicle = new Vehicle();

        setAttributesFromDTO(vehicleRequest, vehicle, licensePlate, carModel);

        this.repository.save(vehicle);

        return toDTO(vehicle);
    }

    public VehicleDTO updateVehicle(Long vehicleId, VehicleDTO vehicleRequest) throws EntityNotFoundException, IllegalArgumentException {
        validateRequest(vehicleRequest);

        Vehicle vehicle = findVehicleById(vehicleId);

        String licensePlate = normalizePlate(vehicleRequest.licensePlate(), vehicleRequest.plateColor());

        if (this.repository.existsByLicensePlateAndIdNot(licensePlate, vehicleId)){
            throw new IllegalArgumentException("Ya existe un vehículo con placa " + licensePlate);
        }

        CarModel carModel = this.carModelService.findCarModelById(vehicleRequest.carModelId());

        setAttributesFromDTO(vehicleRequest, vehicle, licensePlate, carModel);

        this.repository.save(vehicle);

        return toDTO(vehicle);
    }

    public void deleteVehicle(Long vehicleId) throws EntityNotFoundException {
        Vehicle vehicle = findVehicleById(vehicleId);

        //TODO: bloquear si tiene reparaciones (RepairRepository.existsByVehicleId)
        this.repository.delete(vehicle);
    }

    public void setAttributesFromDTO(VehicleDTO request, Vehicle vehicle, String licensePlate, CarModel carModel){
        vehicle.setLicensePlate(licensePlate);
        vehicle.setPlateColor(request.plateColor());
        vehicle.setOwnerName(request.ownerName().trim());
        vehicle.setCarModel(carModel);
    }

    public VehicleDTO toDTO(Vehicle vehicle){
        return new VehicleDTO(
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getPlateColor(),
                vehicle.getOwnerName(),
                vehicle.getCarModel().getId()
        );
    }

    private String normalizePlate(String rawPlate, PlateColor plateColor){
        String licensePlate = plateColor.normalize(rawPlate);

        //formato según el color
        if (!plateColor.isValid(licensePlate)){
            throw new IllegalArgumentException("La placa " + licensePlate + " no corresponde a " +
                    plateColor.getLabel() + " (ej. " + plateColor.getExample() + ")");
        }

        return licensePlate;
    }

    private void validateRequest(VehicleDTO request){
        //placa obligatoria
        if (request.licensePlate() == null || request.licensePlate().isBlank()){
            throw new IllegalArgumentException("La placa es obligatoria");
        }

        //color obligatorio
        if (request.plateColor() == null){
            throw new IllegalArgumentException("El color de la placa es obligatorio");
        }

        //propietario obligatorio
        if (request.ownerName() == null || request.ownerName().isBlank()){
            throw new IllegalArgumentException("El nombre del propietario es obligatorio");
        }

        //modelo obligatorio
        if (request.carModelId() == null){
            throw new IllegalArgumentException("Debe seleccionar un modelo");
        }
    }
}
