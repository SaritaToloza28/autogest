package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.model.entity.AppUser;
import dev.saritacarolina.autogest.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

//Pone en todas las vistas el nombre completo del usuario de la sesión ("Carlos Mejía" en vez de "mecanico1").
//Lo usan el saludo (fragments/header :: welcome) y el pie del menú (fragments/sidebar).
@ControllerAdvice
public class CurrentUserAdvice {

    private final AppUserRepository appUserRepository;

    public CurrentUserAdvice(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @ModelAttribute("currentUserName")
    public String currentUserName(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return appUserRepository.findByUsername(authentication.getName())
                .map(AppUser::getFullName)
                .orElse(null);
    }
}
