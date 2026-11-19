package co.edu.unbosque.marketplace.publicacion.contracts;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PublicacionDisponibilidadQueryTest {

    private PublicacionDisponibilidadQuery query;

    @BeforeEach
    void setUp() {
        query = mock(PublicacionDisponibilidadQuery.class);
    }

    @Test
    void debeRetornarTrueCuandoExistePublicacionActiva() {
        UUID idAlojamiento = UUID.randomUUID();

        when(query.existePublicacionActivaPorAlojamiento(idAlojamiento))
                .thenReturn(true);

        boolean resultado =
                query.existePublicacionActivaPorAlojamiento(idAlojamiento);

        assertTrue(resultado);
        verify(query).existePublicacionActivaPorAlojamiento(idAlojamiento);
    }

    @Test
    void debeRetornarFalseCuandoNoExistePublicacionActiva() {
        UUID idAlojamiento = UUID.randomUUID();

        when(query.existePublicacionActivaPorAlojamiento(idAlojamiento))
                .thenReturn(false);

        boolean resultado =
                query.existePublicacionActivaPorAlojamiento(idAlojamiento);

        assertFalse(resultado);
        verify(query).existePublicacionActivaPorAlojamiento(idAlojamiento);
    }

    @Test
    void debeConsultarPublicacionConIdentificadorEspecifico() {
        UUID idAlojamiento = UUID.randomUUID();

        when(query.existePublicacionActivaPorAlojamiento(idAlojamiento))
                .thenReturn(true);

        query.existePublicacionActivaPorAlojamiento(idAlojamiento);

        verify(query, times(1))
                .existePublicacionActivaPorAlojamiento(idAlojamiento);
    }

    @Test
    void debePermitirConsultarDiferentesAlojamientos() {
        UUID idAlojamiento1 = UUID.randomUUID();
        UUID idAlojamiento2 = UUID.randomUUID();

        when(query.existePublicacionActivaPorAlojamiento(idAlojamiento1))
                .thenReturn(true);

        when(query.existePublicacionActivaPorAlojamiento(idAlojamiento2))
                .thenReturn(false);

        assertTrue(query.existePublicacionActivaPorAlojamiento(idAlojamiento1));
        assertFalse(query.existePublicacionActivaPorAlojamiento(idAlojamiento2));

        verify(query).existePublicacionActivaPorAlojamiento(idAlojamiento1);
        verify(query).existePublicacionActivaPorAlojamiento(idAlojamiento2);
    }
}
