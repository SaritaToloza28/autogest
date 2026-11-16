package dev.saritacarolina.autogest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


//Solo muestra las vistas
@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "public/index";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
}