package dev.saritacarolina.autogest.repository;

import dev.saritacarolina.autogest.model.entity.SparePart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SparePartRepository extends JpaRepository<SparePart, Long> {

    @EntityGraph(attributePaths = "compatibleModels")
    List<SparePart> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = "compatibleModels")
    Optional<SparePart> findWithCompatibleModelsById(Long id);

    boolean existsByEanCode(String eanCode);

    boolean existsByEanCodeAndIdNot(String eanCode, Long id);

    boolean existsByCompatibleModelsId(Long carModelId);

    //Le pregunta a la base de datos si ese repuesto ya fue gastado en alguna reparación.
    //JPQL: usa la entidad Repair y su mapa usedParts (KEY(p) = el repuesto), así Hibernate la valida al arrancar.
    @Query("SELECT COUNT(r) > 0 FROM Repair r JOIN r.usedParts p WHERE KEY(p).id = :sparePartId")
    boolean isUsedInRepairs(@Param("sparePartId") Long sparePartId);
}