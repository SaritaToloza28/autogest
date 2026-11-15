package dev.saritacarolina.autogest.repository;


import dev.saritacarolina.autogest.model.entity.CarModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarModelRepository extends JpaRepository<CarModel, Long> {
    //Listado ordenado para tablas
    List<CarModel> findAllByOrderByBrandAscNameAsc();

    //Buscador: busca en "marca modelo" juntos, así "mazda 3" encuentra Mazda 3 (sin distinguir mayúsculas)
    @Query("SELECT c FROM CarModel c WHERE LOWER(CONCAT(c.brand, ' ', c.name)) LIKE LOWER(CONCAT('%', :text, '%')) ORDER BY c.brand, c.name")
    List<CarModel> searchByText(@Param("text") String text);

    //Evita duplicados al crear
    boolean existsByBrandIgnoreCaseAndNameIgnoreCase(String brand, String name);

    //Evita duplicados al editar, excluyendo el propio registro
    boolean existsByBrandIgnoreCaseAndNameIgnoreCaseAndIdNot(String brand, String name, Long id);
}
