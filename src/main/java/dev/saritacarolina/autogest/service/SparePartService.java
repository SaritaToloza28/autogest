package dev.saritacarolina.autogest.service;

import dev.saritacarolina.autogest.model.dto.SparePartDTO;
import dev.saritacarolina.autogest.model.entity.CarModel;
import dev.saritacarolina.autogest.model.entity.SparePart;
import dev.saritacarolina.autogest.repository.SparePartRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SparePartService {

    private static final String IMAGE_FOLDER = "spare-parts";
    private static final BigDecimal MAX_PRICE = new BigDecimal("9999999999.99");

    private final SparePartRepository repository;
    private final CarModelService carModelService;
    private final ImageStorageService imageStorageService;

    public SparePartService(SparePartRepository repository,
                            CarModelService carModelService,
                            ImageStorageService imageStorageService) {
        this.repository = repository;
        this.carModelService = carModelService;
        this.imageStorageService = imageStorageService;
    }

    public List<SparePart> findAllSpareParts(){
        return this.repository.findAllByOrderByNameAsc();
    }

    public SparePart findSparePartById(Long sparePartId) throws EntityNotFoundException {
        return this.repository.findWithCompatibleModelsById(sparePartId)
                .orElseThrow(() -> new EntityNotFoundException("Repuesto con id " + sparePartId + " no encontrado"));
    }

    public SparePartDTO createSparePart(SparePartDTO sparePartRequest, MultipartFile image) throws EntityNotFoundException, IllegalArgumentException {
        validateRequest(sparePartRequest);

        String eanCode = sparePartRequest.eanCode().trim();

        if (!isValidEAN(eanCode)){
            throw new IllegalArgumentException("Código EAN-13 no válido");
        }

        if (this.repository.existsByEanCode(eanCode)){
            throw new IllegalArgumentException("Ya existe un repuesto con EAN " + eanCode);
        }

        Set<CarModel> compatibleModels = this.carModelService.findCarModelsByIds(sparePartRequest.compatibleModelIds());

        SparePart sparePart = new SparePart();

        setAttributesFromDTO(sparePartRequest, sparePart, eanCode, compatibleModels);

        //La imagen se guarda al final, cuando todas las validaciones pasaron
        if (hasImage(image)){
            sparePart.setImagePath(this.imageStorageService.store(image, IMAGE_FOLDER));
        }

        this.repository.save(sparePart);

        return toDTO(sparePart);
    }

    public SparePartDTO updateSparePart(Long sparePartId, SparePartDTO sparePartRequest, MultipartFile image) throws EntityNotFoundException, IllegalArgumentException {
        validateRequest(sparePartRequest);

        SparePart sparePart = findSparePartById(sparePartId);

        String eanCode = sparePartRequest.eanCode().trim();

        if (!isValidEAN(eanCode)){
            throw new IllegalArgumentException("Código EAN-13 no válido");
        }

        if (this.repository.existsByEanCodeAndIdNot(eanCode, sparePartId)){
            throw new IllegalArgumentException("Ya existe un repuesto con EAN " + eanCode);
        }

        Set<CarModel> compatibleModels = this.carModelService.findCarModelsByIds(sparePartRequest.compatibleModelIds());

        setAttributesFromDTO(sparePartRequest, sparePart, eanCode, compatibleModels);

        //Si llega imagen nueva se reemplaza; si no, se conserva la anterior
        String oldImagePath = null;

        if (hasImage(image)){
            oldImagePath = sparePart.getImagePath();
            sparePart.setImagePath(this.imageStorageService.store(image, IMAGE_FOLDER));
        }

        this.repository.save(sparePart);

        this.imageStorageService.delete(oldImagePath);

        return toDTO(sparePart);
    }

    public void deleteSparePart(Long sparePartId) throws EntityNotFoundException {
        SparePart sparePart = findSparePartById(sparePartId);

        if (this.repository.isUsedInRepairs(sparePartId)){
            throw new IllegalArgumentException("No se puede eliminar: el repuesto fue usado en reparaciones");
        }
        this.repository.delete(sparePart);

        this.imageStorageService.delete(sparePart.getImagePath());
    }

    public void setAttributesFromDTO(SparePartDTO request, SparePart sparePart, String eanCode, Set<CarModel> compatibleModels){
        sparePart.setName(request.name().trim());
        sparePart.setDescription(request.description() == null || request.description().isBlank()
                ? null
                : request.description().trim());
        sparePart.setPrice(request.price());
        sparePart.setEanCode(eanCode);
        sparePart.setStock(request.stock());

        //Se modifica la colección existente en lugar de reemplazarla
        sparePart.getCompatibleModels().clear();
        sparePart.getCompatibleModels().addAll(compatibleModels);
    }

    public SparePartDTO toDTO(SparePart sparePart){
        return new SparePartDTO(
                sparePart.getName(),
                sparePart.getDescription(),
                sparePart.getPrice(),
                sparePart.getEanCode(),
                sparePart.getStock(),
                sparePart.getImagePath(),
                sparePart.getCompatibleModels().stream()
                        .map(CarModel::getId)
                        .collect(Collectors.toSet())
        );
    }

    private boolean hasImage(MultipartFile image){
        return image != null && !image.isEmpty();
    }

    private void validateRequest(SparePartDTO request){
        //nombre obligatorio
        if (request.name() == null || request.name().isBlank()){
            throw new IllegalArgumentException("El nombre del repuesto es obligatorio");
        }

        if (request.name().trim().length() > 100){
            throw new IllegalArgumentException("El nombre del repuesto no puede superar 100 caracteres");
        }

        //descripción opcional
        if (request.description() != null && request.description().trim().length() > 500){
            throw new IllegalArgumentException("La descripción no puede superar 500 caracteres");
        }

        //precio mayor a 0
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }

        //la columna price es numeric(12,2): máximo 2 decimales y 10 dígitos enteros
        if (request.price().stripTrailingZeros().scale() > 2){
            throw new IllegalArgumentException("El precio no puede tener más de 2 decimales");
        }

        if (request.price().compareTo(MAX_PRICE) > 0){
            throw new IllegalArgumentException("El precio no puede superar 9.999.999.999,99");
        }

        //EAN obligatorio
        if (request.eanCode() == null || request.eanCode().isBlank()){
            throw new IllegalArgumentException("El código EAN es obligatorio");
        }

        //stock no negativo
        if (request.stock() == null || request.stock() < 0){
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }

        //al menos un modelo compatible
        if (request.compatibleModelIds() == null || request.compatibleModelIds().isEmpty()){
            throw new IllegalArgumentException("Debe seleccionar al menos un modelo compatible");
        }
    }

    private boolean isValidEAN(String ean){
        //que sea de 13
        if (ean.length() != 13){
            return false;
        }

        int sum = 0;

        //los primeros 12 dígitos: posición par pesa 1, impar pesa 3
        for (int i = 0; i < 12; i++) {
            char currentCharacter = ean.charAt(i);

            if (currentCharacter < '0' || currentCharacter > '9'){
                return false;
            }

            int digit = currentCharacter - '0';

            sum += (i % 2 == 0) ? digit : digit * 3;
        }

        char lastCharacter = ean.charAt(12);

        if (lastCharacter < '0' || lastCharacter > '9'){
            return false;
        }

        //dígito de control
        int checkDigit = (10 - (sum % 10)) % 10;

        return checkDigit == lastCharacter - '0';
    }
}