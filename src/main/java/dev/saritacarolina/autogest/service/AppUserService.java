package dev.saritacarolina.autogest.service;

import dev.saritacarolina.autogest.model.dto.AppUserDTO;
import dev.saritacarolina.autogest.model.entity.AppUser;
import dev.saritacarolina.autogest.repository.AppUserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppUserService {

    private static final int PASSWORD_MIN = 8;
    private static final int PASSWORD_MAX = 72;

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AppUserDTO> findAll() {
        return appUserRepository.findAll(Sort.by("fullName")).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppUserDTO findById(Long id) {
        return toDTO(getAppUser(id));
    }

    @Transactional
    public AppUserDTO save(AppUserDTO dto) {
        validateRequired(dto);

        boolean isNew = dto.id() == null;
        AppUser appUser = isNew ? new AppUser() : getAppUser(dto.id());

        String username = dto.username().trim();
        validateUniqueUsername(username, dto.id());

        appUser.setUsername(username);
        appUser.setFullName(dto.fullName().trim());
        appUser.setRole(dto.role());

        // Crear: contraseña obligatoria. Editar: vacía = se conserva la actual.
        if (isNew || hasText(dto.password())) {
            validatePassword(dto.password());
            appUser.setPassword(passwordEncoder.encode(dto.password()));
        }

        return toDTO(appUserRepository.save(appUser));
    }

    @Transactional
    public void delete(Long id, String currentUsername) {
        AppUser appUser = getAppUser(id);

        if (appUser.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("No puedes eliminar tu propio usuario");
        }

        appUserRepository.delete(appUser);
    }

    @Transactional
    public void changePassword(String username, String currentPassword,
                               String newPassword, String confirmPassword) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!hasText(currentPassword) || !passwordEncoder.matches(currentPassword, appUser.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        validatePassword(newPassword);

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("La confirmación no coincide con la nueva contraseña");
        }

        if (passwordEncoder.matches(newPassword, appUser.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser distinta a la actual");
        }

        appUser.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(appUser);
    }

    // ---------- Auxiliares ----------

    private AppUser getAppUser(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private AppUserDTO toDTO(AppUser appUser) {
        // La contraseña nunca sale del servicio
        return new AppUserDTO(appUser.getId(), appUser.getUsername(), appUser.getFullName(), null, appUser.getRole());
    }

    private void validateRequired(AppUserDTO dto) {
        if (!hasText(dto.username())) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        if (dto.username().trim().length() > 50) {
            throw new IllegalArgumentException("El usuario no puede superar 50 caracteres");
        }
        if (!hasText(dto.fullName())) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (dto.fullName().trim().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar 100 caracteres");
        }
        if (dto.role() == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
    }

    private void validateUniqueUsername(String username, Long id) {
        appUserRepository.findByUsername(username)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("El usuario '" + username + "' ya existe");
                });
    }

    private void validatePassword(String password) {
        if (!hasText(password) || password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener entre " + PASSWORD_MIN + " y " + PASSWORD_MAX + " caracteres");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}