
package co.edu.unbosque.marketplace.usuario.security;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            UserDetailsService userDetailsService) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider)
            throws Exception {

        http
            /*
             * API REST sin estado.
             *
             * La autenticación utiliza exclusivamente JWT enviado
             * mediante el encabezado Authorization: Bearer.
             *
             * JwtAuthenticationFilter no obtiene credenciales
             * de cookies ni utiliza sesiones de navegador.
             *
             * Por tanto, no existe autenticación automática
             * basada en cookies que requiera protección CSRF.
             */
            .csrf(csrf -> csrf.disable())

            .cors(cors -> {})

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    HttpMethod.OPTIONS,
                    "/**"
                ).permitAll()

                // Autenticación y verificación de correo.

                .requestMatchers(
                    "/autenticacion/**",
                    "/verificacion-correo/**"
                ).permitAll()

                // Documentación Swagger.

                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // Recuperación de contraseña.

                .requestMatchers(
                    "/recuperacion-contrasena/**"
                ).permitAll()

                // Administración de usuarios.

                .requestMatchers(
                    "/administracion/usuarios/**"
                ).hasAuthority("ROLE_ADMINISTRADOR")

                // Creación de alojamientos.

                .requestMatchers(
                    HttpMethod.POST,
                    "/alojamientos"
                ).hasAuthority("ROLE_ANFITRION")

                // Consultas de servicios y políticas.

                .requestMatchers(
                    HttpMethod.GET,
                    "/servicios",
                    "/politicas-cancelacion/activas"
                ).hasAuthority("ROLE_ANFITRION")

                // Publicaciones propias del anfitrión.

                .requestMatchers(
                    HttpMethod.GET,
                    "/publicaciones/mis-publicaciones",
                    "/publicaciones/mis-publicaciones/**"
                ).hasAuthority("ROLE_ANFITRION")

                // Edición de publicaciones.

                .requestMatchers(
                    HttpMethod.PUT,
                    "/publicaciones/**"
                ).hasAnyAuthority(
                    "ROLE_ANFITRION",
                    "ROLE_ADMINISTRADOR"
                )

                // Enviar publicación a revisión.

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/publicaciones/*/enviar-revision"
                ).hasAuthority("ROLE_ANFITRION")

                // Consulta administrativa de publicaciones.

                .requestMatchers(
                    HttpMethod.GET,
                    "/administracion/publicaciones",
                    "/administracion/publicaciones/**"
                ).hasAuthority("ROLE_ADMINISTRADOR")

                // Moderación administrativa.

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/administracion/publicaciones/**"
                ).hasAuthority("ROLE_ADMINISTRADOR")

                // Pausar y reactivar publicaciones.

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/publicaciones/*/pausar",
                    "/publicaciones/*/reactivar"
                ).hasAuthority("ROLE_ANFITRION")

                // Catálogo público.

                .requestMatchers(
                    HttpMethod.GET,
                    "/publicaciones/catalogo"
                ).permitAll()
             // Consulta de política de cancelación de la publicación.
                .requestMatchers(
                        HttpMethod.GET,
                        "/publicaciones/*/politica-cancelacion"
                ).hasAuthority("ROLE_ANFITRION")

                // Selección de política de cancelación.

                .requestMatchers(
                        HttpMethod.PATCH,
                        "/publicaciones/*/politica-cancelacion"
                ).hasAuthority("ROLE_ANFITRION")

                // Todo lo demás requiere autenticación.

                .anyRequest().authenticated()
            )

            // Manejo de errores de autenticación y autorización.

            .exceptionHandling(exception -> exception

                .authenticationEntryPoint(
                    (request, response, authException) -> {

                        response.setStatus(
                                HttpServletResponse.SC_UNAUTHORIZED
                        );

                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");

                        response.getWriter().write(
                            "{\"mensaje\":\"Debes iniciar sesión para acceder a este recurso.\"}"
                        );
                    }
                )

                .accessDeniedHandler(
                    (request, response, accessDeniedException) -> {

                        response.setStatus(
                                HttpServletResponse.SC_FORBIDDEN
                        );

                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");

                        response.getWriter().write(
                            "{\"mensaje\":\"No tienes permisos para realizar esta operación.\"}"
                        );
                    }
                )
            )

            // API sin estado y sin sesiones HTTP.

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authenticationProvider(authenticationProvider)

            // Incorporar filtro JWT antes del filtro de usuario y contraseña.

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173"
        ));

        configuration.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config)
            throws Exception {

        return config.getAuthenticationManager();
    }
}
