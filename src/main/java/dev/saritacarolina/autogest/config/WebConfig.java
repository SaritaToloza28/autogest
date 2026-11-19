package dev.saritacarolina.autogest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import java.util.Locale;

// Hace que el navegador pueda ver las imágenes guardadas en disco.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    //Fechas siempre en español ("lunes 17 noviembre"), sin importar el idioma del navegador
    @Bean
    public LocaleResolver localeResolver() {
        return new FixedLocaleResolver(Locale.forLanguageTag("es-CO"));
    }

    //Hace que los archivos guardados en la carpeta uploads se puedan ver desde el navegador.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}