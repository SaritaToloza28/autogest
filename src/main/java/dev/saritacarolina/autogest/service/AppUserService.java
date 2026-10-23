package dev.saritacarolina.autogest.service;

import dev.saritacarolina.autogest.model.dto.AppUserDTO;
import dev.saritacarolina.autogest.model.entity.AppUser;
import dev.saritacarolina.autogest.repository.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserService {

    private static final int PASSWORD_MIN = 8;
    private static final int PASSWORD_MAX = 72;

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUser> findAllAppUsers(){
        return this.repository.findAllByOrderByFullNameAsc();
    }

    public AppUser findAppUserById(Long appUserId) throws EntityNotFoundException {
        return this.repository.findById(appUserId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario con id " + appUserId + " no encontrado"));
    }

    public AppUserDTO createAppUser(AppUserDTO appUserRequest) throws IllegalArgumentException {
        validateRequest(appUserRequest);

        String username = appUserRequest.username().trim();

        if (this.repository.existsByUsernameIgnoreCase(username)){
            throw new IllegalArgumentException("El usuario '" + username + "' ya existe");
        }

        //al crear, la contraseña es obligatoria
        validatePassword(appUserRequest.password());

        AppUser appUser = new AppUser();

        setAttributesFromDTO(appUserRequest, appUser, username);
        appUser.setPassword(this.passwordEncoder.encode(appUserRequest.password()));

        this.repository.save(appUser);

        return toDTO(appUser);
    }

    public AppUserDTO updateAppUser(Long appUserId, AppUserDTO appUserRequest, String currentUsername) throws EntityNotFoundException, IllegalArgumentException {
        validateRequest(appUserRequest);

        AppUser appUser = findAppUserById(appUserId);

        String username = appUserRequest.username().trim();

        if (this.repository.existsByUsernameIgnoreCaseAndIdNot(username, appUserId)){
            throw new IllegalArgumentException("El usuario '" + username + "' ya existe");
        }

        boolean isCurrentUser = appUser.getUsername().equals(currentUsername);

        //el usuario con sesión iniciada no puede cambiarse su username: la sesión seguiría con el nombre viejo
        if (isCurrentUser && !appUser.getUsername().equals(username)){
            throw new IllegalArgumentException("No puedes cambiar tu propio nombre de usuario");
        }

        //ni su rol: así siempre queda al menos un administrador (el que hace el cambio)
        if (isCurrentUser && appUser.getRole() != appUserRequest.role()){
            throw new IllegalArgumentException("No puedes cambiar tu propio rol");
        }

        setAttributesFromDTO(appUserRequest, appUser, username);

        //al editar, contraseña vacía = se conserva la actual
        if (hasText(appUserRequest.password())){
            validatePassword(appUserRequest.password());
            appUser.setPassword(this.passwordEncoder.encode(appUserRequest.password()));
        }

        this.repository.save(appUser);

        return toDTO(appUser);
    }

    public void deleteAppUser(Long appUserId, String currentUsername) throws EntityNotFoundException, IllegalArgumentException {
        AppUser appUser = findAppUserById(appUserId);

        if (appUser.getUsername().equals(currentUsername)){
            throw new IllegalArgumentException("No puedes eliminar tu propio usuario");
        }

        this.repository.delete(appUser);
    }

    public void changePassword(String username, String currentPassword,
                               String newPassword, String confirmPassword) throws EntityNotFoundException, IllegalArgumentException {
        AppUser appUser = this.repository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario " + username + " no encontrado"));

        if (!hasText(currentPassword) || !this.passwordEncoder.matches(currentPassword, appUser.getPassword())){
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        validatePassword(newPassword);

        if (!newPassword.equals(confirmPassword)){
            throw new IllegalArgumentException("La confirmación no coincide con la nueva contraseña");
        }

        if (this.passwordEncoder.matches(newPassword, appUser.getPassword())){
            throw new IllegalArgumentException("La nueva contraseña debe ser distinta a la actual");
        }

        appUser.setPassword(this.passwordEncoder.encode(newPassword));

        this.repository.save(appUser);
    }

    //La contraseña se asigna aparte porque hay que encriptarla
    public void setAttributesFromDTO(AppUserDTO request, AppUser appUser, String username){
        appUser.setUsername(username);
        appUser.setFullName(request.fullName().trim());
        appUser.setRole(request.role());
    }

    //La contraseña nunca sale del servicio
    public AppUserDTO toDTO(AppUser appUser){
        return new AppUserDTO(
                appUser.getUsername(),
                appUser.getFullName(),
                null,
                appUser.getRole()
        );
    }

    private void validateRequest(AppUserDTO request){
        //usuario obligatorio
        if (!hasText(request.username())){
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        if (request.username().trim().length() > 50){
            throw new IllegalArgumentException("El usuario no puede superar 50 caracteres");
        }

        //nombre obligatorio
        if (!hasText(request.fullName())){
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (request.fullName().trim().length() > 100){
            throw new IllegalArgumentException("El nombre no puede superar 100 caracteres");
        }

        //rol obligatorio
        if (request.role() == null){
            throw new IllegalArgumentException("El rol es obligatorio");
        }
    }

    private void validatePassword(String password){
        if (!hasText(password) || password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX){
            throw new IllegalArgumentException(
                    "La contraseña debe tener entre " + PASSWORD_MIN + " y " + PASSWORD_MAX + " caracteres");
        }
    }

    private boolean hasText(String value){
        return value != null && !value.isBlank();
    }
}
