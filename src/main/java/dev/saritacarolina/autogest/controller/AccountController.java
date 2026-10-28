package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.service.AppUserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

//"Mi cuenta": acciones del usuario con sesión iniciada, para todos los roles
@Controller
@RequestMapping("/account")
public class AccountController {

    private final AppUserService appUserService;

    public AccountController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/password")
    public String changePasswordPage() {
        return "account/change-password";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 Model model) {
        try {
            appUserService.changePassword(principal.getName(), currentPassword, newPassword, confirmPassword);
            model.addAttribute("clearForm", true);
            addToast(model, "success", "Contraseña actualizada correctamente");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            addToast(model, "error", e.getMessage());
        }
        return "account/response";
    }

    private void addToast(Model model, String type, String message) {
        model.addAttribute("toastType", type);
        model.addAttribute("toastMessage", message);
    }
}
