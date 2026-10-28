package dev.saritacarolina.autogest.service;

import dev.saritacarolina.autogest.model.constant.RepairStatus;
import dev.saritacarolina.autogest.model.constant.Role;
import dev.saritacarolina.autogest.model.constant.ServiceType;
import dev.saritacarolina.autogest.model.dto.RepairDTO;
import dev.saritacarolina.autogest.model.entity.AppUser;
import dev.saritacarolina.autogest.model.entity.Repair;
import dev.saritacarolina.autogest.model.entity.SparePart;
import dev.saritacarolina.autogest.model.entity.Vehicle;
import dev.saritacarolina.autogest.repository.RepairRepository;
import dev.saritacarolina.autogest.repository.SparePartRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class RepairService {

    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");

    private final RepairRepository repository;
    private final VehicleService vehicleService;
    private final AppUserService appUserService;
    //Repositorio (no servicio) porque solo se busca el repuesto y se actualiza su stock
    private final SparePartRepository sparePartRepository;

    public RepairService(RepairRepository repository,
                         VehicleService vehicleService,
                         AppUserService appUserService,
                         SparePartRepository sparePartRepository) {
        this.repository = repository;
        this.vehicleService = vehicleService;
        this.appUserService = appUserService;
        this.sparePartRepository = sparePartRepository;
    }

    public List<Repair> findAllRepairs(){
        return this.repository.findAllByOrderByRepairDateDesc();
    }

    //Reparaciones del mecánico con sesión iniciada; el admin las ve todas
    public List<Repair> findAssignedRepairs(String username, boolean isAdmin){
        if (isAdmin){
            return findAllRepairs();
        }

        return this.repository.findByMechanicUsernameOrderByRepairDateDesc(username);
    }

    public Repair findRepairById(Long repairId) throws EntityNotFoundException {
        return this.repository.findWithDetailsById(repairId)
                .orElseThrow(() -> new EntityNotFoundException("Reparación con id " + repairId + " no encontrada"));
    }

    //Consulta pública: "abc 123", "ABC123" o "abc-123" buscan la misma placa
    public List<Repair> findRepairsByLicensePlate(String rawPlate) throws IllegalArgumentException {
        String plate = rawPlate == null ? "" : rawPlate.toUpperCase().replaceAll("[^A-Z0-9]", "");

        if (plate.isEmpty()){
            throw new IllegalArgumentException("Escribe la placa del vehículo");
        }

        return this.repository.findByLicensePlateWithoutDash(plate);
    }

    // ---------- Registro (operador) ----------

    public RepairDTO createRepair(RepairDTO repairRequest) throws EntityNotFoundException, IllegalArgumentException {
        validateRequest(repairRequest);

        Vehicle vehicle = this.vehicleService.findVehicleById(repairRequest.vehicleId());

        AppUser mechanic = findMechanic(repairRequest.mechanicId());

        Repair repair = new Repair();

        setAttributesFromDTO(repairRequest, repair, vehicle, mechanic);

        //la fecha y el estado PENDING los asigna @PrePersist
        this.repository.save(repair);

        return toDTO(repair);
    }

    public RepairDTO updateRepair(Long repairId, RepairDTO repairRequest) throws EntityNotFoundException, IllegalArgumentException {
        validateRequest(repairRequest);

        Repair repair = findRepairById(repairId);

        //solo se edita antes de que el mecánico empiece
        if (repair.getStatus() != RepairStatus.PENDING){
            throw new IllegalArgumentException("Solo se puede editar una reparación pendiente");
        }

        Vehicle vehicle = this.vehicleService.findVehicleById(repairRequest.vehicleId());

        AppUser mechanic = findMechanic(repairRequest.mechanicId());

        setAttributesFromDTO(repairRequest, repair, vehicle, mechanic);

        this.repository.save(repair);

        return toDTO(repair);
    }

    public void deleteRepair(Long repairId) throws EntityNotFoundException, IllegalArgumentException {
        Repair repair = findRepairById(repairId);

        //una pendiente todavía no tiene repuestos; si ya empezó, se cancela en vez de borrarla
        if (repair.getStatus() != RepairStatus.PENDING){
            throw new IllegalArgumentException("Solo se puede eliminar una reparación pendiente; si ya empezó, cancélala");
        }

        this.repository.delete(repair);
    }

    // ---------- Cambios de estado ----------

    //Mecánico: PENDING -> IN_PROGRESS
    public void startRepair(Long repairId, String username, boolean isAdmin) throws EntityNotFoundException, IllegalArgumentException {
        Repair repair = findAssignedRepair(repairId, username, isAdmin);

        changeStatus(repair, RepairStatus.IN_PROGRESS);
    }

    //Mecánico: IN_PROGRESS -> COMPLETED
    public void completeRepair(Long repairId, String username, boolean isAdmin) throws EntityNotFoundException, IllegalArgumentException {
        Repair repair = findAssignedRepair(repairId, username, isAdmin);

        changeStatus(repair, RepairStatus.COMPLETED);
    }

    //Operador: COMPLETED -> DELIVERED (el vehículo se entrega al cliente)
    public void deliverRepair(Long repairId) throws EntityNotFoundException, IllegalArgumentException {
        Repair repair = findRepairById(repairId);

        changeStatus(repair, RepairStatus.DELIVERED);
    }

    //Operador: PENDING o IN_PROGRESS -> CANCELLED. Los repuestos usados vuelven al stock.
    //@Transactional: se actualizan varios repuestos y la reparación, todo o nada
    @Transactional
    public void cancelRepair(Long repairId) throws EntityNotFoundException, IllegalArgumentException {
        Repair repair = findRepairById(repairId);

        validateStatusChange(repair, RepairStatus.CANCELLED);

        for (Map.Entry<SparePart, Integer> usedPart : repair.getUsedParts().entrySet()){
            SparePart sparePart = usedPart.getKey();
            sparePart.setStock(sparePart.getStock() + usedPart.getValue());
            this.sparePartRepository.save(sparePart);
        }

        repair.getUsedParts().clear();
        repair.setStatus(RepairStatus.CANCELLED);

        this.repository.save(repair);
    }

    // ---------- Repuestos usados (mecánico) ----------

    //Descuenta el stock; si el repuesto ya estaba en la reparación, suma la cantidad
    @Transactional
    public void addSparePart(Long repairId, Long sparePartId, Integer quantity, String username, boolean isAdmin) throws EntityNotFoundException, IllegalArgumentException {
        Repair repair = findAssignedRepair(repairId, username, isAdmin);

        validateInProgress(repair);

        if (sparePartId == null){
            throw new IllegalArgumentException("Debe seleccionar un repuesto");
        }

        if (quantity == null || quantity <= 0){
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        SparePart sparePart = this.sparePartRepository.findWithCompatibleModelsById(sparePartId)
                .orElseThrow(() -> new EntityNotFoundException("Repuesto con id " + sparePartId + " no encontrado"));

        //solo repuestos compatibles con el modelo del vehículo
        Long carModelId = repair.getVehicle().getCarModel().getId();

        boolean isCompatible = sparePart.getCompatibleModels().stream()
                .anyMatch(carModel -> carModel.getId().equals(carModelId));

        if (!isCompatible){
            throw new IllegalArgumentException("El repuesto " + sparePart.getName() + " no es compatible con este vehículo");
        }

        if (sparePart.getStock() < quantity){
            throw new IllegalArgumentException("Stock insuficiente: quedan " + sparePart.getStock() + " unidades de " + sparePart.getName());
        }

        sparePart.setStock(sparePart.getStock() - quantity);
        this.sparePartRepository.save(sparePart);

        repair.getUsedParts().merge(sparePart, quantity, Integer::sum);
        this.repository.save(repair);
    }

    //Quita el repuesto de la reparación y devuelve la cantidad al stock
    @Transactional
    public void removeSparePart(Long repairId, Long sparePartId, String username, boolean isAdmin) throws EntityNotFoundException, IllegalArgumentException {
        Repair repair = findAssignedRepair(repairId, username, isAdmin);

        validateInProgress(repair);

        SparePart sparePart = this.sparePartRepository.findById(sparePartId)
                .orElseThrow(() -> new EntityNotFoundException("Repuesto con id " + sparePartId + " no encontrado"));

        Integer quantity = repair.getUsedParts().remove(sparePart);

        if (quantity == null){
            throw new IllegalArgumentException("El repuesto " + sparePart.getName() + " no está en esta reparación");
        }

        sparePart.setStock(sparePart.getStock() + quantity);
        this.sparePartRepository.save(sparePart);

        this.repository.save(repair);
    }

    // ---------- Auxiliares ----------

    public void setAttributesFromDTO(RepairDTO request, Repair repair, Vehicle vehicle, AppUser mechanic){
        repair.setReason(request.reason().trim());
        repair.setServiceType(request.serviceType());

        //la ubicación solo aplica a domicilio
        if (request.serviceType() == ServiceType.SERVICE_EXTERNAL){
            repair.setLatitude(request.latitude());
            repair.setLongitude(request.longitude());
        } else {
            repair.setLatitude(null);
            repair.setLongitude(null);
        }

        repair.setVehicle(vehicle);
        repair.setMechanic(mechanic);
    }

    public RepairDTO toDTO(Repair repair){
        return new RepairDTO(
                repair.getReason(),
                repair.getServiceType(),
                repair.getLatitude(),
                repair.getLongitude(),
                repair.getVehicle().getId(),
                repair.getMechanic().getId()
        );
    }

    //El usuario asignado debe tener rol de mecánico
    private AppUser findMechanic(Long mechanicId) throws EntityNotFoundException, IllegalArgumentException {
        AppUser mechanic = this.appUserService.findAppUserById(mechanicId);

        if (mechanic.getRole() != Role.MECHANIC){
            throw new IllegalArgumentException("El usuario " + mechanic.getUsername() + " no es mecánico");
        }

        return mechanic;
    }

    //El mecánico solo puede trabajar sus propias reparaciones; el admin, cualquiera
    private Repair findAssignedRepair(Long repairId, String username, boolean isAdmin) throws EntityNotFoundException, IllegalArgumentException {
        Repair repair = findRepairById(repairId);

        if (!isAdmin && !repair.getMechanic().getUsername().equals(username)){
            throw new IllegalArgumentException("Esta reparación no está asignada a ti");
        }

        return repair;
    }

    private void changeStatus(Repair repair, RepairStatus newStatus){
        validateStatusChange(repair, newStatus);

        repair.setStatus(newStatus);

        this.repository.save(repair);
    }

    private void validateStatusChange(Repair repair, RepairStatus newStatus){
        if (!repair.getStatus().canChangeTo(newStatus)){
            throw new IllegalArgumentException("No se puede pasar de " + repair.getStatus().getLabel()
                    + " a " + newStatus.getLabel());
        }
    }

    //Los repuestos solo se registran mientras se trabaja
    private void validateInProgress(Repair repair){
        if (repair.getStatus() != RepairStatus.IN_PROGRESS){
            throw new IllegalArgumentException("Solo se pueden registrar repuestos en una reparación en progreso");
        }
    }

    private void validateRequest(RepairDTO request){
        //motivo obligatorio
        if (request.reason() == null || request.reason().isBlank()){
            throw new IllegalArgumentException("El motivo de la reparación es obligatorio");
        }

        if (request.reason().trim().length() > 500){
            throw new IllegalArgumentException("El motivo no puede superar 500 caracteres");
        }

        //tipo de servicio obligatorio
        if (request.serviceType() == null){
            throw new IllegalArgumentException("El tipo de servicio es obligatorio");
        }

        //vehículo y mecánico obligatorios
        if (request.vehicleId() == null){
            throw new IllegalArgumentException("Debe seleccionar un vehículo");
        }

        if (request.mechanicId() == null){
            throw new IllegalArgumentException("Debe seleccionar un mecánico");
        }

        //a domicilio: latitud y longitud obligatorias y dentro del rango válido
        if (request.serviceType() == ServiceType.SERVICE_EXTERNAL){
            if (request.latitude() == null || request.longitude() == null){
                throw new IllegalArgumentException("Para el servicio a domicilio la latitud y la longitud son obligatorias");
            }

            if (request.latitude().abs().compareTo(MAX_LATITUDE) > 0){
                throw new IllegalArgumentException("La latitud debe estar entre -90 y 90");
            }

            if (request.longitude().abs().compareTo(MAX_LONGITUDE) > 0){
                throw new IllegalArgumentException("La longitud debe estar entre -180 y 180");
            }

            //las columnas guardan máximo 6 decimales
            if (request.latitude().stripTrailingZeros().scale() > 6 || request.longitude().stripTrailingZeros().scale() > 6){
                throw new IllegalArgumentException("La latitud y la longitud pueden tener máximo 6 decimales");
            }
        }
    }
}
