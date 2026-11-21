package co.edu.unbosque.marketplace.usuario.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.usuario.application.port.UsuarioRepository;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentasSinVerificarServiceTest {

    private static final OffsetDateTime AHORA =
            OffsetDateTime.of(2026, 11, 18, 15, 0, 0, 0, ZoneOffset.ofHours(-5));

    @Mock private UsuarioRepository usuarioRepository;

    @Test
    void eliminaLasCuentasPendientesRegistradasHaceMasDe24Horas() {
        CuentasSinVerificarService servicio = new CuentasSinVerificarService(usuarioRepository, 24);
        when(usuarioRepository.eliminarSinVerificarRegistradosAntesDe(
                EstadoUsuario.PENDIENTE_VERIFICACION, AHORA.minusHours(24))).thenReturn(3);

        assertEquals(3, servicio.eliminarVencidas(AHORA));
        verify(usuarioRepository).eliminarSinVerificarRegistradosAntesDe(
                EstadoUsuario.PENDIENTE_VERIFICACION, AHORA.minusHours(24));
    }

    @Test
    void usaLasHorasLimiteConfiguradas() {
        CuentasSinVerificarService servicio = new CuentasSinVerificarService(usuarioRepository, 48);

        assertEquals(0, servicio.eliminarVencidas(AHORA));
        verify(usuarioRepository).eliminarSinVerificarRegistradosAntesDe(
                EstadoUsuario.PENDIENTE_VERIFICACION, AHORA.minusHours(48));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void rechazaHorasLimiteQueNoSeanPositivas(long horas) {
        assertThrows(DatosInvalidosException.class,
                () -> new CuentasSinVerificarService(usuarioRepository, horas));
        verifyNoInteractions(usuarioRepository);
    }
}
