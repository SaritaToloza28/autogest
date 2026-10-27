package dev.saritacarolina.autogest.config;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


// Configura toda la seguridad de la aplicación: define los accesos por rol, encripta contraseñas y maneja el inicio/cierre de sesión.
@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Público
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        .requestMatchers("/", "/login", "/repair-status/**", "/error").permitAll()

                        // ADMIN
                        .requestMatchers("/users/**", "/car-models/**").hasRole("ADMIN")

                        // Repuestos: el mecánico solo consulta compatibles
                        .requestMatchers("/spare-parts/compatible/**").hasAnyRole("MECHANIC", "ADMIN")
                        .requestMatchers("/spare-parts/**").hasRole("ADMIN")

                        // OPERATOR
                        .requestMatchers("/vehicles/**").hasAnyRole("OPERATOR", "ADMIN")

                        // Reparaciones: primero la ruta específica del mecánico
                        .requestMatchers("/repairs/assigned/**").hasAnyRole("MECHANIC", "ADMIN")
                        .requestMatchers("/repairs/**").hasAnyRole("OPERATOR", "ADMIN")

                        // Cambiar mi contraseña y cualquier otra ruta: solo autenticados
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) ->
                                response.sendRedirect(request.getContextPath() + redirectByRole(authentication)))
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                // CSRF activo; solo se excluye la consola H2 (desarrollo)
                .csrf(csrf -> csrf.ignoringRequestMatchers(PathRequest.toH2Console()))
                // Permite que la consola H2 se muestre en su iframe
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    private String redirectByRole(Authentication authentication) {
        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        return switch (authority) {
            case "ROLE_ADMIN" -> "/users";
            case "ROLE_OPERATOR" -> "/vehicles";
            case "ROLE_MECHANIC" -> "/repairs/assigned";
            default -> "/";
        };
    }
}