package dev.saritacarolina.autogest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Hace que el navegador pueda ver las imágenes guardadas en disco.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    //Hace que los archivos guardados en la carpeta uploads se puedan ver desde el navegador.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}