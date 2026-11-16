package dev.saritacarolina.autogest.repository;

import dev.saritacarolina.autogest.model.constant.RepairStatus;
import dev.saritacarolina.autogest.model.entity.Repair;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepairRepository extends JpaRepository<Repair, Long> {

    //Listado del operador: trae vehículo, modelo y mecánico en la misma consulta
    @EntityGraph(attributePaths = {"vehicle", "vehicle.carModel", "mechanic"})
    List<Repair> findAllByOrderByRepairDateDesc();

    //Reparaciones asignadas a un mecánico
    @EntityGraph(attributePaths = {"vehicle", "vehicle.carModel", "mechanic"})
    List<Repair> findByMechanicUsernameOrderByRepairDateDesc(String username);

    @EntityGraph(attributePaths = {"vehicle", "vehicle.carModel", "mechanic"})
    Optional<Repair> findWithDetailsById(Long id);

    // Obtiene el historial de reparaciones buscando por placa ordenando en DESC.
    @EntityGraph(attributePaths = {"vehicle", "vehicle.carModel"})
    @Query("SELECT r FROM Repair r WHERE REPLACE(r.vehicle.licensePlate, '-', '') = :plate ORDER BY r.repairDate DESC")
    List<Repair> findByLicensePlateWithoutDash(@Param("plate") String plate);

    //Buscador del dashboard
    @EntityGraph(attributePaths = {"vehicle", "vehicle.carModel", "mechanic"})
    @Query("SELECT r FROM Repair r " +
            "WHERE (:plate <> '' AND REPLACE(r.vehicle.licensePlate, '-', '') LIKE CONCAT('%', :plate, '%')) " +
            "OR LOWER(r.vehicle.ownerName) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "ORDER BY r.repairDate DESC")
    List<Repair> searchByText(@Param("plate") String plate, @Param("text") String text);

    //Evita borrar un vehículo que tiene reparaciones
    boolean existsByVehicleId(Long vehicleId);

    //Recepción: avisa si el vehículo ya tiene una orden en alguno de estos estados
    boolean existsByVehicleIdAndStatusIn(Long vehicleId, Collection<RepairStatus> statuses);

    //Evita borrar o cambiar de rol a un mecánico que tiene reparaciones
    boolean existsByMechanicId(Long mechanicId);
}
