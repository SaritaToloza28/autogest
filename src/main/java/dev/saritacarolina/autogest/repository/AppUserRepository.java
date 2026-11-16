package dev.saritacarolina.autogest.repository;

import dev.saritacarolina.autogest.model.constant.Role;
import dev.saritacarolina.autogest.model.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    List<AppUser> findAllByOrderByFullNameAsc();

    //Lista de mecánicos para asignar reparaciones
    List<AppUser> findByRoleOrderByFullNameAsc(Role role);

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    //Evita duplicados al crear, sin distinguir mayúsculas ("Admin" = "admin")
    boolean existsByUsernameIgnoreCase(String username);

    //Evita duplicados al editar, excluyendo el propio registro
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
}