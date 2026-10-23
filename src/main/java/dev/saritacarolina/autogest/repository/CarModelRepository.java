package dev.saritacarolina.autogest.repository;


import dev.saritacarolina.autogest.model.entity.CarModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarModelRepository extends JpaRepository<CarModel, Long> {
    //Listado ordenado para tablas y <select>
    List<CarModel> findAllByOrderByBrandAscNameAsc();

    //Evita duplicados al crear
    boolean existsByBrandIgnoreCaseAndNameIgnoreCase(String brand, String name);

    //Evita duplicados al editar, excluyendo el propio registro
    boolean existsByBrandIgnoreCaseAndNameIgnoreCaseAndIdNot(String brand, String name, Long id);
}
