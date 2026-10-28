package dev.saritacarolina.autogest.service;

import dev.saritacarolina.autogest.model.constant.PlateColor;
import dev.saritacarolina.autogest.model.dto.VehicleDTO;
import dev.saritacarolina.autogest.model.entity.CarModel;
import dev.saritacarolina.autogest.model.entity.Vehicle;
import dev.saritacarolina.autogest.repository.RepairRepository;
import dev.saritacarolina.autogest.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository repository;
    private final CarModelService carModelService;
    private final RepairRepository repairRepository;

    public VehicleService(VehicleRepository repository,
                          CarModelService carModelService,
                          RepairRepository repairRepository) {
        this.repository = repository;
        this.carModelService = carModelService;
        this.repairRepository = repairRepository;
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

        String ownerPhone = normalizePhone(vehicleRequest.ownerPhone());

        CarModel carModel = this.carModelService.findCarModelById(vehicleRequest.carModelId());

        Vehicle vehicle = new Vehicle();

        setAttributesFromDTO(vehicleRequest, vehicle, licensePlate, ownerPhone, carModel);

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

        String ownerPhone = normalizePhone(vehicleRequest.ownerPhone());

        CarModel carModel = this.carModelService.findCarModelById(vehicleRequest.carModelId());

        setAttributesFromDTO(vehicleRequest, vehicle, licensePlate, ownerPhone, carModel);

        this.repository.save(vehicle);

        return toDTO(vehicle);
    }

    public void deleteVehicle(Long vehicleId) throws EntityNotFoundException, IllegalArgumentException {
        Vehicle vehicle = findVehicleById(vehicleId);

        //no se puede borrar si tiene reparaciones registradas
        if (this.repairRepository.existsByVehicleId(vehicleId)){
            throw new IllegalArgumentException("No se puede eliminar: el vehículo tiene reparaciones registradas");
        }

        this.repository.delete(vehicle);
    }

    public void setAttributesFromDTO(VehicleDTO request, Vehicle vehicle, String licensePlate, String ownerPhone, CarModel carModel){
        vehicle.setLicensePlate(licensePlate);
        vehicle.setPlateColor(request.plateColor());
        vehicle.setOwnerName(request.ownerName().trim());
        vehicle.setOwnerPhone(ownerPhone);
        vehicle.setCarModel(carModel);
    }

    public VehicleDTO toDTO(Vehicle vehicle){
        return new VehicleDTO(
                vehicle.getLicensePlate(),
                vehicle.getPlateColor(),
                vehicle.getOwnerName(),
                vehicle.getOwnerPhone(),
                vehicle.getCarModel().getId()
        );
    }

    //"300 123 4567", "300-123-4567" o "+57 3001234567" -> "3001234567"
    private String normalizePhone(String rawPhone){
        String ownerPhone = rawPhone.replaceAll("[^0-9]", "");

        //quita el indicativo de Colombia si viene
        if (ownerPhone.length() == 12 && ownerPhone.startsWith("57")){
            ownerPhone = ownerPhone.substring(2);
        }

        //celular colombiano: 10 dígitos que empiezan por 3
        if (!ownerPhone.matches("3\\d{9}")){
            throw new IllegalArgumentException("El celular debe tener 10 dígitos y empezar por 3 (ej. 3001234567)");
        }

        return ownerPhone;
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

        if (request.ownerName().trim().length() > 100){
            throw new IllegalArgumentException("El nombre del propietario no puede superar 100 caracteres");
        }

        //celular obligatorio
        if (request.ownerPhone() == null || request.ownerPhone().isBlank()){
            throw new IllegalArgumentException("El celular del propietario es obligatorio");
        }

        //modelo obligatorio
        if (request.carModelId() == null){
            throw new IllegalArgumentException("Debe seleccionar un modelo");
        }
    }
}
