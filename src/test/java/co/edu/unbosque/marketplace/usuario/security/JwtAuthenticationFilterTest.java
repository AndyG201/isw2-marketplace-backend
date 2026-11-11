package co.edu.unbosque.marketplace.usuario.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private Usuario usuario;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtFilter;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtAuthenticationFilter(
                jwtUtil,
                userDetailsService
        );

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest crearPeticion(String token) {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/test");

        if (token != null) {
            request.addHeader("Authorization", "Bearer " + token);
        }

        return request;
    }

    private void configurarUsuarioActivo() {

        when(usuario.isCorreoVerificado()).thenReturn(true);
        when(usuario.getEstado()).thenReturn(EstadoUsuario.ACTIVA);
        when(usuario.isRestringido()).thenReturn(false);
        when(usuario.getIntentosFallidos()).thenReturn(0);

        doReturn(List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        )).when(usuario).getAuthorities();
    }
    
    @Test
    void testTokenValido() throws Exception {

        MockHttpServletRequest request =
                crearPeticion("tokenValido");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        configurarUsuarioActivo();

        when(jwtUtil.extractUsername("tokenValido"))
                .thenReturn("usuario");

        when(userDetailsService.loadUserByUsername("usuario"))
                .thenReturn(usuario);

        when(jwtUtil.validateToken("tokenValido", usuario))
                .thenReturn(true);

        jwtFilter.doFilter(request, response, filterChain);

        assertNotNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        assertEquals(
                usuario,
                SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testTokenInvalido() throws Exception {

        MockHttpServletRequest request =
                crearPeticion("tokenInvalido");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        configurarUsuarioActivo();

        when(jwtUtil.extractUsername("tokenInvalido"))
                .thenReturn("usuario");

        when(userDetailsService.loadUserByUsername("usuario"))
                .thenReturn(usuario);

        when(jwtUtil.validateToken("tokenInvalido", usuario))
                .thenReturn(false);

        jwtFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testCuentaConCorreoNoVerificado() throws Exception {
        MockHttpServletRequest request =
                crearPeticion("tokenValido");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(usuario.isCorreoVerificado()).thenReturn(false);

        when(jwtUtil.extractUsername("tokenValido"))
                .thenReturn("usuario");

        when(userDetailsService.loadUserByUsername("usuario"))
                .thenReturn(usuario);

        when(jwtUtil.validateToken("tokenValido", usuario))
                .thenReturn(true);

        jwtFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }

    @Test
    void testCuentaRestringida() throws Exception {
        MockHttpServletRequest request =
                crearPeticion("tokenValido");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(usuario.isCorreoVerificado()).thenReturn(true);
        when(usuario.getEstado()).thenReturn(EstadoUsuario.ACTIVA);
        when(usuario.isRestringido()).thenReturn(true);

        when(jwtUtil.extractUsername("tokenValido"))
                .thenReturn("usuario");

        when(userDetailsService.loadUserByUsername("usuario"))
                .thenReturn(usuario);

        when(jwtUtil.validateToken("tokenValido", usuario))
                .thenReturn(true);

        jwtFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testSinToken() throws Exception {

        MockHttpServletRequest request =
                crearPeticion(null);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testErrorAlExtraerToken() throws Exception {

        MockHttpServletRequest request =
                crearPeticion("tokenError");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        when(jwtUtil.extractUsername("tokenError"))
                .thenThrow(new RuntimeException("Error de token"));

        jwtFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }
}
