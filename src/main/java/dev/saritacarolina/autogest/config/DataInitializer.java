package dev.saritacarolina.autogest.config;

import dev.saritacarolina.autogest.model.constant.RoleName;
import dev.saritacarolina.autogest.model.entity.AppUser;
import dev.saritacarolina.autogest.model.entity.Role;
import dev.saritacarolina.autogest.repository.AppUserRepository;
import dev.saritacarolina.autogest.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Usuario semilla: apenas arranca la app, revisa si el admin ya existe.
// Si no está, lo crea con su clave encriptada (por eso no puede ir en data.sql).
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(RoleRepository roleRepository,
                           AppUserRepository appUserRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws EntityNotFoundException {
        if (appUserRepository.existsByUsername(adminUsername)) {
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new EntityNotFoundException("Rol ADMIN no encontrado para el usuario por defecto"));

        AppUser admin = new AppUser();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setFullName("Administrador");
        admin.setActive(true);
        admin.setRole(adminRole);

        appUserRepository.save(admin);
    }
}
