package co.edu.unbosque.marketplace.usuario.security;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private Usuario usuario;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsServiceImpl(
                usuarioRepository
        );
    }

    @Test
    void testLoadUserByUsername() {
        String email = "usuario@correo.com";

        when(usuarioRepository.findByCorreoIgnoreCase(email))
                .thenReturn(Optional.of(usuario));

        UserDetails resultado =
                userDetailsService.loadUserByUsername(email);

        assertNotNull(resultado);
        assertEquals(usuario, resultado);

        verify(usuarioRepository).findByCorreoIgnoreCase(email);
    }

    @Test
    void testLoadUserByUsernameNoEncontrado() {
        String email = "inexistente@correo.com";

        when(usuarioRepository.findByCorreoIgnoreCase(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email)
        );

        verify(usuarioRepository).findByCorreoIgnoreCase(email);
    }

    @Test
    void testLoadUserByUsernameCorreoMayusculas() {
        String email = "USUARIO@CORREO.COM";

        when(usuarioRepository.findByCorreoIgnoreCase(email))
                .thenReturn(Optional.of(usuario));

        UserDetails resultado =
                userDetailsService.loadUserByUsername(email);

        assertNotNull(resultado);
        assertEquals(usuario, resultado);

        verify(usuarioRepository).findByCorreoIgnoreCase(email);
    }
}
