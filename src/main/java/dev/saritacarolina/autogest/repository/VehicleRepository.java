package dev.saritacarolina.autogest.repository;

import dev.saritacarolina.autogest.model.entity.Vehicle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // Trae todos los vehículos ordenados por placa e incluye la información de su modelo en una sola consulta SQL, así la relacion se LAZY
    @EntityGraph(attributePaths = "carModel")
    List<Vehicle> findAllByOrderByLicensePlateAsc();

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlate(String licensePlate);

    // Valida que la placa no pertenezca a un vehículo distinto al actual.
    boolean existsByLicensePlateAndIdNot(String licensePlate, Long id);

    boolean existsByCarModelId(Long carModelId);
}