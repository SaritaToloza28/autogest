package dev.saritacarolina.autogest.repository;

import dev.saritacarolina.autogest.model.entity.SparePart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
}