package dev.saritacarolina.autogest.repository;

import dev.saritacarolina.autogest.model.entity.Repair;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairRepository extends JpaRepository<Repair, Long> {

    //lo fr @EntityGraph es solo para llaves foráneas
    //Listado del operador: trae vehículo, modelo y mecánico en la misma consulta
    @EntityGraph(attributePaths = {"vehicle", "vehicle.carModel", "mechanic"})
    List<Repair> findAllByOrderByRepairDateDesc();

    //Reparaciones asignadas a un mecánico
    @EntityGraph(attributePaths = {"vehicle", "vehicle.carModel", "mechanic"})
    List<Repair> findByMechanicUsernameOrderByRepairDateDesc(String username);

    @EntityGraph(attributePaths = {"vehicle", "vehicle.carModel", "mechanic"})
    Optional<Repair> findWithDetailsById(Long id);

    // Obtiene el historial de reparaciones buscando por placa (ignorando guiones) ordenado en DESC.
    //JPQL es para consultar las clases no la bd
    //Consulta pública: compara la placa sin guion
    @EntityGraph(attributePaths = {"vehicle", "vehicle.carModel"})
    @Query("SELECT r FROM Repair r WHERE REPLACE(r.vehicle.licensePlate, '-', '') = :plate ORDER BY r.repairDate DESC")
    List<Repair> findByLicensePlateWithoutDash(@Param("plate") String plate);

    //Evita borrar un vehículo que tiene reparaciones
    boolean existsByVehicleId(Long vehicleId);

    //Evita borrar o cambiar de rol a un mecánico que tiene reparaciones
    boolean existsByMechanicId(Long mechanicId);
}
