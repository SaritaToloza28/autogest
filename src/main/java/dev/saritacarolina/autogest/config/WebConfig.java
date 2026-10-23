package dev.saritacarolina.autogest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Permite acceder públicamente desde el navegador a los archivos físicos guardados en la carpeta local "uploads/".
@Configuration
public class WebConfig implements WebMvcConfigurer {

    //Sirve los archivos de la carpeta uploads/ en la URL /uploads/**
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}