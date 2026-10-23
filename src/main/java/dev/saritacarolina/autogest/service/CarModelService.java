package dev.saritacarolina.autogest.service;

import dev.saritacarolina.autogest.model.dto.CarModelDTO;
import dev.saritacarolina.autogest.model.entity.CarModel;
import dev.saritacarolina.autogest.repository.CarModelRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CarModelService {

    private final CarModelRepository repository;

    public CarModelService(CarModelRepository repository) {
        this.repository = repository;
    }

    public List<CarModel> findAllCarModels(){
        return this.repository.findAllByOrderByBrandAscNameAsc();
    }

    public CarModel findCarModelById(Long carModelId) throws EntityNotFoundException {
        return this.repository.findById(carModelId)
                .orElseThrow(() -> new EntityNotFoundException("Modelo con id " + carModelId + " no encontrado"));
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

    public void deleteCarModel(Long carModelId) throws EntityNotFoundException {
        CarModel carModel = findCarModelById(carModelId);

        this.repository.delete(carModel);
    }

    public void setAttributesFromDTO(CarModelDTO request, CarModel carModel){
        carModel.setName(request.name().trim());
        carModel.setBrand(request.brand().trim());
    }

    public CarModelDTO toDTO(CarModel carModel){
        return new CarModelDTO(
                carModel.getId(),
                carModel.getName(),
                carModel.getBrand()
        );
    }

    private void validateRequest(CarModelDTO request){
        //nombre obligatorio
        if (request.name() == null || request.name().isBlank()){
            throw new IllegalArgumentException("El nombre del modelo es obligatorio");
        }

        //marca obligatoria
        if (request.brand() == null || request.brand().isBlank()){
            throw new IllegalArgumentException("La marca del modelo es obligatoria");
        }
    }


    //Verifica que todos los modelos compatibles seleccionados si existan por medio de su id
    public Set<CarModel> findCarModelsByIds(Set<Long> carModelIds) throws EntityNotFoundException {
        List<CarModel> carModels = this.repository.findAllById(carModelIds);

        //si llegan menos de los pedidos, alguno no existe
        if (carModels.size() != carModelIds.size()){
            throw new EntityNotFoundException("Uno o más modelos seleccionados no existen");
        }

        return new HashSet<>(carModels);
    }


}