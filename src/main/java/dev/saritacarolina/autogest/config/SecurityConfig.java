package dev.saritacarolina.autogest.config;

import dev.saritacarolina.autogest.model.constant.RoleName;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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

    private static final String ADMIN = RoleName.ROLE_ADMIN.name();
    private static final String OPERATOR = RoleName.ROLE_OPERATOR.name();
    private static final String MECHANIC = RoleName.ROLE_MECHANIC.name();

    // Consola H2 (solo desarrollo): cadena aparte, sin CSRF ni cabeceras para que cargue en su iframe.
    // @Order(1) para que se evalúe antes de la cadena principal.
    @Bean
    @Order(1)
    public SecurityFilterChain h2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(PathRequest.toH2Console())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.disable());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Público
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/", "/login", "/repair-status/**", "/error").permitAll()

                        // ADMIN
                        .requestMatchers("/users/**", "/car-models/**").hasAuthority(ADMIN)

                        // Repuestos: el mecánico ve los compatibles dentro de /repairs/assigned
                        .requestMatchers("/spare-parts/**").hasAuthority(ADMIN)

                        // OPERATOR
                        .requestMatchers("/vehicles/**").hasAnyAuthority(OPERATOR, ADMIN)

                        // Reparaciones: primero la ruta específica del mecánico
                        .requestMatchers("/repairs/assigned/**").hasAuthority(MECHANIC)
                        .requestMatchers("/repairs/**").hasAnyAuthority(OPERATOR, ADMIN)

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
                );

        return http.build();
    }

    // Las contraseñas se guardan como hash BCrypt; Spring lo usa al comparar en el login
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Vista principal de cada rol
    public static String redirectByRole(Authentication authentication) {
        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        return switch (RoleName.valueOf(authority)) {
            case ROLE_ADMIN, ROLE_OPERATOR -> "/repairs";
            case ROLE_MECHANIC -> "/repairs/assigned";
        };
    }
}
