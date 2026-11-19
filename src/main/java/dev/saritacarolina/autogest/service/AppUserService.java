package dev.saritacarolina.autogest.service;

import dev.saritacarolina.autogest.model.constant.RoleName;
import dev.saritacarolina.autogest.model.dto.AppUserDTO;
import dev.saritacarolina.autogest.model.entity.AppUser;
import dev.saritacarolina.autogest.model.entity.Role;
import dev.saritacarolina.autogest.repository.AppUserRepository;
import dev.saritacarolina.autogest.repository.RepairRepository;
import dev.saritacarolina.autogest.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//Además del CRUD, es el UserDetailsService: Spring Security lo llama al iniciar sesión
@Service
public class AppUserService implements UserDetailsService {

    private static final int PASSWORD_MIN = 8;
    private static final int PASSWORD_MAX = 72;

    private final AppUserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RepairRepository repairRepository;

    public AppUserService(AppUserRepository repository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          RepairRepository repairRepository) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.repairRepository = repairRepository;
    }

    //Carga el usuario para el login. Si está inactivo, Spring lo rechaza por isEnabled()
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario " + username + " no encontrado"));
    }

    public List<AppUser> findAllAppUsers(){
        return this.repository.findAllByOrderByFullNameAsc();
    }

    //Solo los mecánicos activos se pueden asignar a reparaciones
    public List<AppUser> findAllMechanics(){
        return this.repository.findByRole_NameAndIsActiveTrueOrderByFullNameAsc(RoleName.ROLE_MECHANIC);
    }

    public AppUser findAppUserById(Long appUserId) throws EntityNotFoundException {
        return this.repository.findById(appUserId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario con id " + appUserId + " no encontrado"));
    }

    public Role findRoleByName(RoleName name) throws EntityNotFoundException {
        return this.roleRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Rol " + name + " no encontrado"));
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
        //los usuarios nuevos quedan activos
        appUser.setActive(true);

        this.repository.save(appUser);

        //devuelve un DTO diferente al request que refleja lo que realmente se guardó.
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
        RoleName currentRole = appUser.getRole().getName();

        //el usuario con sesión iniciada no puede cambiarse su username
        if (isCurrentUser && !appUser.getUsername().equals(username)){
            throw new IllegalArgumentException("No puedes cambiar tu propio nombre de usuario");
        }

        //ni su rol: así siempre queda al menos un administrador
        if (isCurrentUser && currentRole != appUserRequest.role()){
            throw new IllegalArgumentException("No puedes cambiar tu propio rol");
        }

        //un mecánico con reparaciones no puede dejar de ser mecánico: sus reparaciones quedarían sin responsable
        if (currentRole == RoleName.ROLE_MECHANIC && appUserRequest.role() != RoleName.ROLE_MECHANIC
                && this.repairRepository.existsByMechanicId(appUserId)){
            throw new IllegalArgumentException("No se puede cambiar el rol: el mecánico tiene reparaciones asignadas");
        }

        setAttributesFromDTO(appUserRequest, appUser, username);

        if (hasText(appUserRequest.password())){
            validatePassword(appUserRequest.password());
            appUser.setPassword(this.passwordEncoder.encode(appUserRequest.password()));
        }

        this.repository.save(appUser);

        return toDTO(appUser);
    }

    //Activa o desactiva la cuenta sin borrarla (un usuario inactivo no puede iniciar sesión)
    public AppUser toggleActive(Long appUserId, String currentUsername) throws EntityNotFoundException, IllegalArgumentException {
        AppUser appUser = findAppUserById(appUserId);

        if (appUser.getUsername().equals(currentUsername)){
            throw new IllegalArgumentException("No puedes desactivar tu propio usuario");
        }

        appUser.setActive(!appUser.isActive());

        return this.repository.save(appUser);
    }

    public void deleteAppUser(Long appUserId, String currentUsername) throws EntityNotFoundException, IllegalArgumentException {
        AppUser appUser = findAppUserById(appUserId);

        if (appUser.getUsername().equals(currentUsername)){
            throw new IllegalArgumentException("No puedes eliminar tu propio usuario");
        }

        //no se puede borrar un mecánico que tiene reparaciones (sí se puede desactivar)
        if (this.repairRepository.existsByMechanicId(appUserId)){
            throw new IllegalArgumentException("No se puede eliminar: el mecánico tiene reparaciones asignadas. Puedes desactivarlo");
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
        appUser.setRole(findRoleByName(request.role()));
    }

    //La contraseña nunca sale del servicio
    public AppUserDTO toDTO(AppUser appUser){
        return new AppUserDTO(
                appUser.getUsername(),
                appUser.getFullName(),
                null,
                appUser.getRole().getName()
        );
    }

    //Valida los datos
    private void validateRequest(AppUserDTO request){
        if (!hasText(request.username())){
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        if (request.username().trim().length() > 50){
            throw new IllegalArgumentException("El usuario no puede superar 50 caracteres");
        }

        if (!hasText(request.fullName())){
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (request.fullName().trim().length() > 100){
            throw new IllegalArgumentException("El nombre no puede superar 100 caracteres");
        }

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
