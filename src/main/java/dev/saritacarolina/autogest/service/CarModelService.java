package dev.saritacarolina.autogest.service;

import dev.saritacarolina.autogest.model.dto.CarModelDTO;
import dev.saritacarolina.autogest.model.entity.CarModel;
import dev.saritacarolina.autogest.repository.CarModelRepository;
import dev.saritacarolina.autogest.repository.SparePartRepository;
import dev.saritacarolina.autogest.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CarModelService {

    private final CarModelRepository repository;
    private final VehicleRepository vehicleRepository;
    private final SparePartRepository sparePartRepository;

    public CarModelService(CarModelRepository repository,
                           VehicleRepository vehicleRepository,
                           SparePartRepository sparePartRepository) {
        this.repository = repository;
        this.vehicleRepository = vehicleRepository;
        this.sparePartRepository = sparePartRepository;
    }

    public List<CarModel> findAllCarModels(){
        return this.repository.findAllByOrderByBrandAscNameAsc();
    }

    //Búsqueda mientras se escribe: "maz", "spark" o "mazda 3"
    public List<CarModel> searchCarModels(String text){
        //sin texto, se muestran todos los modelos
        if (text == null || text.isBlank()){
            return this.repository.findAllByOrderByBrandAscNameAsc();
        }

        return this.repository.searchByText(text.trim());
    }

    public CarModel findCarModelById(Long carModelId) throws EntityNotFoundException {
        return this.repository.findById(carModelId)
                .orElseThrow(() -> new EntityNotFoundException("Modelo con id " + carModelId + " no encontrado"));
    }

    //Texto del buscador de modelo al editar
    public String findDisplayName(Long carModelId){
        if (carModelId == null){
            return "";
        }

        return this.repository.findById(carModelId)
                .map(CarModel::getDisplayName)
                .orElse("");
    }

    public CarModelDTO createCarModel(CarModelDTO carModelRequest) throws IllegalArgumentException {
        validateRequest(carModelRequest);

        if (this.repository.existsByBrandIgnoreCaseAndNameIgnoreCase(
                carModelRequest.brand().trim(), carModelRequest.name().trim())){
            throw new IllegalArgumentException("El modelo " + carModelRequest.brand() + " " + carModelRequest.name() + " ya existe");
        }

        CarModel carModel = new CarModel();

        setAttributesFromDTO(carModelRequest, carModel);

        this.repository.save(carModel);

        return toDTO(carModel);
    }

    public CarModelDTO updateCarModel(Long carModelId, CarModelDTO carModelRequest) throws EntityNotFoundException, IllegalArgumentException {
        validateRequest(carModelRequest);

        CarModel carModel = findCarModelById(carModelId);

        if (this.repository.existsByBrandIgnoreCaseAndNameIgnoreCaseAndIdNot(
                carModelRequest.brand().trim(), carModelRequest.name().trim(), carModelId)){
            throw new IllegalArgumentException("El modelo " + carModelRequest.brand() + " " + carModelRequest.name() + " ya existe");
        }

        setAttributesFromDTO(carModelRequest, carModel);

        this.repository.save(carModel);

        return toDTO(carModel);
    }

    public void deleteCarModel(Long carModelId) throws EntityNotFoundException, IllegalArgumentException {
        CarModel carModel = findCarModelById(carModelId);

        //no se puede borrar si un vehículo o un repuesto lo está usando
        if (this.vehicleRepository.existsByCarModelId(carModelId)){
            throw new IllegalArgumentException("No se puede eliminar: hay vehículos registrados con este modelo");
        }

        if (this.sparePartRepository.existsByCompatibleModelsId(carModelId)){
            throw new IllegalArgumentException("No se puede eliminar: hay repuestos compatibles con este modelo");
        }

        this.repository.delete(carModel);
    }

    public void setAttributesFromDTO(CarModelDTO request, CarModel carModel){
        carModel.setName(request.name().trim());
        carModel.setBrand(request.brand().trim());
    }

    public CarModelDTO toDTO(CarModel carModel){
        return new CarModelDTO(
                carModel.getName(),
                carModel.getBrand()
        );
    }

    private void validateRequest(CarModelDTO request){
        if (request.name() == null || request.name().isBlank()){
            throw new IllegalArgumentException("El nombre del modelo es obligatorio");
        }

        if (request.name().trim().length() > 60){
            throw new IllegalArgumentException("El nombre del modelo no puede superar 60 caracteres");
        }

        if (request.brand() == null || request.brand().isBlank()){
            throw new IllegalArgumentException("La marca del modelo es obligatoria");
        }

        if (request.brand().trim().length() > 60){
            throw new IllegalArgumentException("La marca del modelo no puede superar 60 caracteres");
        }
    }

    //Verifica que todos los modelos de carros seleccionados si existan por medio de su id
    public Set<CarModel> findCarModelsByIds(Set<Long> carModelIds) throws EntityNotFoundException {
        List<CarModel> carModels = this.repository.findAllById(carModelIds);

        //si llegan menos de los pedidos, alguno no existe
        if (carModels.size() != carModelIds.size()){
            throw new EntityNotFoundException("Uno o más modelos seleccionados no existen");
        }

        return new HashSet<>(carModels);
    }


}