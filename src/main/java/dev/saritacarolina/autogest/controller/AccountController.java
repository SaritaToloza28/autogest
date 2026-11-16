package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.config.SecurityConfig;
import dev.saritacarolina.autogest.service.AppUserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

//Acciones del usuario con sesión iniciada, para todos los roles
@Controller
@RequestMapping("/account")
public class AccountController {

    private final AppUserService appUserService;

    public AccountController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    //Lleva a la vista principal del rol
    @GetMapping("/password")
    public String changePasswordPage(Authentication authentication, Model model) {
        model.addAttribute("homeUrl", SecurityConfig.redirectByRole(authentication));
        return "account/change-password";
    }

    // Cambia la clave del usuario. Muestra un aviso de éxito o error.
    @PostMapping("/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 Model model) {
        try {
            appUserService.changePassword(authentication.getName(), currentPassword, newPassword, confirmPassword);

            String homeUrl = request.getContextPath() + SecurityConfig.redirectByRole(authentication);
            response.setHeader("HX-Trigger", "{\"passwordChanged\": \"" + homeUrl + "\"}");
            addToast(model, "success", "Contraseña actualizada correctamente");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            addToast(model, "error", e.getMessage());
        }
        return "account/response";
    }

    // Prepara la notificación visual que le va a salir al usuario.
    private void addToast(Model model, String type, String message) {
        model.addAttribute("toastType", type);
        model.addAttribute("toastMessage", message);
    }
}
