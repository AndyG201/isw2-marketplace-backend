package co.edu.unbosque.marketplace.usuario.api;

import static co.edu.unbosque.marketplace.config.TraductorDeErroresPrueba.traducir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.usuario.application.VerificacionCorreoService;

@ExtendWith(MockitoExtension.class)
class VerficacionCorreoControllerTest {

    @Mock private VerificacionCorreoService servicio;
    @InjectMocks private VerficacionCorreoController controller;

    @Test
    void confirmaElCodigo() {
        ResponseEntity<String> resultado = controller.confirmarCodigo("ana@ejemplo.com", "123456");

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals("Correo verificado correctamente.", resultado.getBody());
        verify(servicio).confirmarCodigo("ana@ejemplo.com", "123456");
    }

    @Test
    void confirmarSinCorreoOCodigoResponde400() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.confirmarCodigo(" ", "123456").getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.confirmarCodigo("ana@ejemplo.com", null).getStatusCode());
        verifyNoInteractions(servicio);
    }

    @Test
    void unCodigoInvalidoSeTraduceA400ConMensaje() {
        doThrow(new DatosInvalidosException("El código es incorrecto."))
                .when(servicio).confirmarCodigo("ana@ejemplo.com", "000000");

        ResponseEntity<?> resultado = traducir(() -> controller.confirmarCodigo("ana@ejemplo.com", "000000"));

        assertEquals(HttpStatus.BAD_REQUEST, resultado.getStatusCode());
        assertEquals(Map.of("mensaje", "El código es incorrecto."), resultado.getBody());
    }

    @Test
    void reenviaElCodigo() {
        ResponseEntity<String> resultado = controller.reenviarCodigo("ana@ejemplo.com");

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        verify(servicio).reenviarCodigo("ana@ejemplo.com");
    }

    @Test
    void reenviarSinCorreoResponde400() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.reenviarCodigo(" ").getStatusCode());
        verifyNoInteractions(servicio);
    }

    @Test
    void elLimiteDeReenviosSeTraduceA400() {
        doThrow(new DatosInvalidosException("Has alcanzado el máximo de 3 reenvíos por hora."))
                .when(servicio).reenviarCodigo("ana@ejemplo.com");

        ResponseEntity<?> resultado = traducir(() -> controller.reenviarCodigo("ana@ejemplo.com"));

        assertEquals(HttpStatus.BAD_REQUEST, resultado.getStatusCode());
    }
}
