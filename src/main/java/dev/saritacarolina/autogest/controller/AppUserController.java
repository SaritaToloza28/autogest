package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.model.constant.Role;
import dev.saritacarolina.autogest.model.dto.AppUserDTO;
import dev.saritacarolina.autogest.service.AppUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    // ---------- Gestión de usuarios (ADMIN) ----------

    @GetMapping("/users")
    public String list(Model model) {
        model.addAttribute("appUser", emptyAppUser());
        model.addAttribute("appUsers", appUserService.findAll());
        model.addAttribute("roles", Role.values());
        return "users/list";
    }

    @GetMapping("/users/form")
    public String emptyForm(Model model) {
        model.addAttribute("appUser", emptyAppUser());
        model.addAttribute("roles", Role.values());
        return "users/list :: user-form";
    }

    @GetMapping("/users/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("appUser", appUserService.findById(id));
        model.addAttribute("roles", Role.values());
        return "users/list :: user-form";
    }

    @PostMapping("/users")
    public String create(@ModelAttribute AppUserDTO dto, Model model) {
        return saveAndRespond(dto, "Usuario creado correctamente", model);
    }

    @PutMapping("/users/{id}")
    public String update(@PathVariable Long id, @ModelAttribute AppUserDTO dto, Model model) {
        AppUserDTO withId = new AppUserDTO(id, dto.username(), dto.fullName(), dto.password(), dto.role());
        return saveAndRespond(withId, "Usuario actualizado correctamente", model);
    }

    @DeleteMapping("/users/{id}")
    public String delete(@PathVariable Long id, Principal principal, Model model) {
        try {
            appUserService.delete(id, principal.getName());
            model.addAttribute("appUsers", appUserService.findAll());
            addToast(model, "success", "Usuario eliminado correctamente");
        } catch (IllegalArgumentException e) {
            addToast(model, "error", e.getMessage());
        }
        return "users/response";
    }

    // ---------- Cambiar mi contraseña (todos los roles) ----------

    @GetMapping("/account/password")
    public String changePasswordPage() {
        return "account/change-password";
    }

    @PostMapping("/account/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 Model model) {
        try {
            appUserService.changePassword(principal.getName(), currentPassword, newPassword, confirmPassword);
            model.addAttribute("clearForm", true);
            addToast(model, "success", "Contraseña actualizada correctamente");
        } catch (IllegalArgumentException e) {
            addToast(model, "error", e.getMessage());
        }
        return "account/response";
    }

    // ---------- Auxiliares ----------

    private String saveAndRespond(AppUserDTO dto, String successMessage, Model model) {
        model.addAttribute("roles", Role.values());
        try {
            appUserService.save(dto);
            model.addAttribute("appUser", emptyAppUser());
            model.addAttribute("appUsers", appUserService.findAll());
            addToast(model, "success", successMessage);
        } catch (IllegalArgumentException e) {
            // Se devuelve el formulario con lo que escribió el usuario, sin la contraseña
            model.addAttribute("appUser",
                    new AppUserDTO(dto.id(), dto.username(), dto.fullName(), null, dto.role()));
            addToast(model, "error", e.getMessage());
        }
        return "users/response";
    }

    private AppUserDTO emptyAppUser() {
        return new AppUserDTO(null, null, null, null, null);
    }

    private void addToast(Model model, String type, String message) {
        model.addAttribute("toastType", type);
        model.addAttribute("toastMessage", message);
    }
}