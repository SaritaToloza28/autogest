package dev.saritacarolina.autogest.repository;

import dev.saritacarolina.autogest.model.entity.Repair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepairRepository extends JpaRepository<Repair, Long> {

    //Evita borrar un vehículo que tiene reparaciones
    boolean existsByVehicleId(Long vehicleId);

    //Evita borrar o cambiar de rol a un mecánico que tiene reparaciones
    boolean existsByMechanicId(Long mechanicId);
}
