package co.edu.unbosque.marketplace.usuario.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.usuario.application.port.CorreoPendienteRepository;
import co.edu.unbosque.marketplace.usuario.security.ProteccionDocumento;

@ExtendWith(MockitoExtension.class)
class CorreoOutboxServiceDatosInvalidosTest {

    @Mock private CorreoPendienteRepository repository;
    @Mock private ProteccionDocumento proteccion;
    @InjectMocks private CorreoOutboxService service;

    @Test
    void encolarRechazaCorreoOCodigoInvalidos() {
        assertThrows(DatosInvalidosException.class, () -> service.encolarCodigoVerificacion(null, "123456"));
        assertThrows(DatosInvalidosException.class, () -> service.encolarCodigoVerificacion("  ", "123456"));
        assertThrows(DatosInvalidosException.class, () -> service.encolarCodigoVerificacion("ana@example.com", null));
        assertThrows(DatosInvalidosException.class, () -> service.encolarCodigoVerificacion("ana@example.com", "12ab56"));
        verifyNoInteractions(repository, proteccion);
    }
}
