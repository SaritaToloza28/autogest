package dev.saritacarolina.autogest.repository;

import dev.saritacarolina.autogest.model.constant.RoleName;
import dev.saritacarolina.autogest.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
