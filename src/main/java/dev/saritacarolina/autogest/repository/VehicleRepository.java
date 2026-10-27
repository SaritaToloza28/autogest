package dev.saritacarolina.autogest.repository;

import dev.saritacarolina.autogest.model.entity.Vehicle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    //TODO: revisar
    @EntityGraph(attributePaths = "carModel")
    List<Vehicle> findAllByOrderByLicensePlateAsc();

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlate(String licensePlate);

    //TODO: revisar
    boolean existsByLicensePlateAndIdNot(String licensePlate, Long id);

    boolean existsByCarModelId(Long carModelId);
}