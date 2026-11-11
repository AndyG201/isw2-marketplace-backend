package co.edu.unbosque.marketplace.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import co.edu.unbosque.marketplace.usuario.security.JwtAuthenticationFilter;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void testCorsConfigurationSource() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        assertNotNull(source);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/autenticacion/login");
        request.setMethod(HttpMethod.GET.name());

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertNotNull(configuration);
        assertEquals(
            List.of("http://localhost:5173", "http://127.0.0.1:5173"),
            configuration.getAllowedOrigins()
        );
        assertEquals(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"),
            configuration.getAllowedMethods()
        );
        assertEquals(List.of("*"), configuration.getAllowedHeaders());
        assertTrue(configuration.getAllowCredentials());
    }

    @Test
    void testAuthenticationProvider() {
        Collection<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );

        UserDetails userDetails = User.withUsername("usuario@correo.com")
            .password("hashGuardado")
            .authorities(authorities)
            .build();

        when(userDetailsService.loadUserByUsername("usuario@correo.com"))
            .thenReturn(userDetails);

        when(passwordEncoder.matches("Clave123", "hashGuardado"))
            .thenReturn(true);

        AuthenticationProvider provider =
            securityConfig.authenticationProvider(passwordEncoder);

        assertTrue(provider instanceof DaoAuthenticationProvider);

        Authentication authentication = provider.authenticate(
            new UsernamePasswordAuthenticationToken(
                "usuario@correo.com",
                "Clave123"
            )
        );

        assertNotNull(authentication);
        assertEquals("usuario@correo.com", authentication.getName());
        assertTrue(authentication.isAuthenticated());

        verify(userDetailsService).loadUserByUsername("usuario@correo.com");
        verify(passwordEncoder).matches("Clave123", "hashGuardado");
    }

    @Test
    void testAuthenticationManager() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager())
            .thenReturn(authenticationManager);

        AuthenticationManager result =
            securityConfig.authenticationManager(authenticationConfiguration);

        assertNotNull(result);
        assertEquals(authenticationManager, result);

        verify(authenticationConfiguration).getAuthenticationManager();
    }
}