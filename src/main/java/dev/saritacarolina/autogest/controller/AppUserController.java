package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.model.constant.Role;
import dev.saritacarolina.autogest.model.dto.AppUserDTO;
import dev.saritacarolina.autogest.service.AppUserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

//CRUD usuarios
@Controller
@RequestMapping("/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("appUser", emptyAppUser());
        model.addAttribute("appUsers", appUserService.findAllAppUsers());
        model.addAttribute("roles", Role.values());
        return "users/list";
    }

    @GetMapping("/form")
    public String emptyForm(Model model) {
        model.addAttribute("appUser", emptyAppUser());
        model.addAttribute("roles", Role.values());
        return "users/list :: user-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("roles", Role.values());
        try {
            model.addAttribute("appUser", appUserService.toDTO(appUserService.findAppUserById(id)));
            //el id va aparte porque el DTO no lo tiene
            model.addAttribute("appUserId", id);
            return "users/list :: user-form";
        } catch (EntityNotFoundException e) {
            model.addAttribute("appUser", emptyAppUser());
            addToast(model, "error", e.getMessage());
            return "users/response";
        }
    }

    @PostMapping
    public String create(@ModelAttribute AppUserDTO dto, Model model) {
        model.addAttribute("roles", Role.values());
        try {
            appUserService.createAppUser(dto);
            model.addAttribute("appUser", emptyAppUser());
            model.addAttribute("appUsers", appUserService.findAllAppUsers());
            addToast(model, "success", "Usuario creado correctamente");
        } catch (IllegalArgumentException e) {
            // Se devuelve el formulario con lo que escribió el usuario, sin la contraseña
            model.addAttribute("appUser", withoutPassword(dto));
            addToast(model, "error", e.getMessage());
        }
        return "users/response";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute AppUserDTO dto, Principal principal, Model model) {
        model.addAttribute("roles", Role.values());
        try {
            appUserService.updateAppUser(id, dto, principal.getName());
            model.addAttribute("appUser", emptyAppUser());
            model.addAttribute("appUsers", appUserService.findAllAppUsers());
            addToast(model, "success", "Usuario actualizado correctamente");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            model.addAttribute("appUser", withoutPassword(dto));
            model.addAttribute("appUserId", id);
            addToast(model, "error", e.getMessage());
        }
        return "users/response";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, Principal principal, Model model) {
        try {
            appUserService.deleteAppUser(id, principal.getName());
            model.addAttribute("appUsers", appUserService.findAllAppUsers());
            addToast(model, "success", "Usuario eliminado correctamente");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            addToast(model, "error", e.getMessage());
        }
        return "users/response";
    }

    // ---------- Auxiliares ----------

    //Devuelve un AppUserDTO vacío para que el formulario de usuarios no falle
    private AppUserDTO emptyAppUser() {
        return new AppUserDTO(null, null, null, null);
    }

    // Copia los datos que se ingresaron, pero borra la contraseña por seguridad
    private AppUserDTO withoutPassword(AppUserDTO dto) {
        return new AppUserDTO(dto.username(), dto.fullName(), null, dto.role());
    }

    // Prepara el mensaje de alerta
    private void addToast(Model model, String type, String message) {
        model.addAttribute("toastType", type);
        model.addAttribute("toastMessage", message);
    }
}
