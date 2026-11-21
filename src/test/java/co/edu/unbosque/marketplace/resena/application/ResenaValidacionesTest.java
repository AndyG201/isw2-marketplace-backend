package co.edu.unbosque.marketplace.resena.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.dao.DataIntegrityViolationException;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaResenaCommand;
import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.ExcepcionDominio;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.resena.api.request.CrearResenaRequest;
import co.edu.unbosque.marketplace.resena.domain.Resena;
import co.edu.unbosque.marketplace.resena.infrastructure.persistence.ResenaModeracionRepository;
import co.edu.unbosque.marketplace.resena.infrastructure.persistence.ResenaPublicaQueryRepository;
import co.edu.unbosque.marketplace.resena.infrastructure.persistence.ResenaRepository;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaResenaInfo;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaResenaQuery;

class ResenaValidacionesTest {

    private static final UUID ID_HUESPED = UUID.randomUUID();
    private static final UUID ID_RESERVA = UUID.randomUUID();
    private static final CrearResenaRequest VALIDA = new CrearResenaRequest(5, "Excelente estadía");

    private ResenaRepository resenas;
    private ReservaResenaQuery reservas;
    private ResenaService servicio;

    @BeforeEach
    void preparar() {
        resenas = mock(ResenaRepository.class);
        reservas = mock(ReservaResenaQuery.class);
        servicio = new ResenaService(resenas, reservas);
    }

    @Test
    void crearExigeSesionYDatosValidos() {
        assertError(SesionRequeridaException.class, "Debes iniciar sesión como huésped.",
                () -> servicio.crear(null, ID_RESERVA, VALIDA));
        assertError(DatosInvalidosException.class, "La reserva y la reseña son obligatorias.",
                () -> servicio.crear(ID_HUESPED, null, VALIDA));
        assertError(DatosInvalidosException.class, "La calificación debe estar entre 1 y 5.",
                () -> servicio.crear(ID_HUESPED, ID_RESERVA, new CrearResenaRequest(6, "Bien")));
        assertError(DatosInvalidosException.class,
                "El comentario es obligatorio y no puede superar 5000 caracteres.",
                () -> servicio.crear(ID_HUESPED, ID_RESERVA, new CrearResenaRequest(4, " ")));
    }

    @Test
    void crearSoloPermiteResenarUnaReservaPropiaYCompletada() {
        when(reservas.consultarReservaParaResena(ID_RESERVA)).thenReturn(Optional.empty());
        assertError(RecursoNoEncontradoException.class, "La reserva no existe.",
                () -> servicio.crear(ID_HUESPED, ID_RESERVA, VALIDA));

        reserva(UUID.randomUUID(), "COMPLETADA");
        assertError(AccesoNoPermitidoException.class, "La reserva no pertenece al huésped autenticado.",
                () -> servicio.crear(ID_HUESPED, ID_RESERVA, VALIDA));

        reserva(ID_HUESPED, "CONFIRMADA");
        assertError(ConflictoNegocioException.class, "Solo puedes reseñar una reserva propia que esté completada.",
                () -> servicio.crear(ID_HUESPED, ID_RESERVA, VALIDA));
    }

    @Test
    void crearNoDuplicaLaResenaDeUnaReserva() {
        reserva(ID_HUESPED, "COMPLETADA");
        when(resenas.findByIdReserva(ID_RESERVA)).thenReturn(Optional.of(resena(true)));
        assertError(ConflictoNegocioException.class, "Esta reserva ya tiene una reseña registrada.",
                () -> servicio.crear(ID_HUESPED, ID_RESERVA, VALIDA));

        when(resenas.findByIdReserva(ID_RESERVA)).thenReturn(Optional.empty());
        when(resenas.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicada"));
        assertError(ConflictoNegocioException.class, "Esta reserva ya tiene una reseña registrada.",
                () -> servicio.crear(ID_HUESPED, ID_RESERVA, VALIDA));
    }

    @Test
    void ocultarValidaLaSolicitudYElEstadoDeLaResena() {
        ResenaModeracionRepository moderacion = mock(ResenaModeracionRepository.class);
        ResenaModeracionService servicioModeracion = new ResenaModeracionService(
                moderacion, mock(AuditoriaResenaCommand.class), new ObjectMapper());
        UUID idAdministrador = UUID.randomUUID();
        UUID idResena = UUID.randomUUID();

        assertError(SesionRequeridaException.class, "Debes iniciar sesión como administrador.",
                () -> servicioModeracion.ocultar(null, idResena, "Lenguaje ofensivo"));
        assertError(DatosInvalidosException.class,
                "Debes indicar un motivo de ocultamiento de máximo 2000 caracteres.",
                () -> servicioModeracion.ocultar(idAdministrador, idResena, " "));

        when(moderacion.bloquearPorId(idResena)).thenReturn(Optional.empty());
        assertError(RecursoNoEncontradoException.class, "La reseña no existe o ya no está disponible.",
                () -> servicioModeracion.ocultar(idAdministrador, idResena, "Lenguaje ofensivo"));

        when(moderacion.bloquearPorId(idResena)).thenReturn(Optional.of(resena(false)));
        assertError(ConflictoNegocioException.class, "La reseña ya está oculta y no puede modificarse.",
                () -> servicioModeracion.ocultar(idAdministrador, idResena, "Lenguaje ofensivo"));
    }

    @Test
    void consultarLasResenasPublicasValidaLaPublicacion() {
        ResenaPublicaQueryRepository publicas = mock(ResenaPublicaQueryRepository.class);
        ResenaPublicaQueryService consulta = new ResenaPublicaQueryService(publicas);
        UUID idPublicacion = UUID.randomUUID();

        assertError(DatosInvalidosException.class, "El identificador de publicación es obligatorio.",
                () -> consulta.consultar(null));
        when(publicas.existePublicacion(idPublicacion)).thenReturn(false);
        assertError(RecursoNoEncontradoException.class, "La publicación no existe.",
                () -> consulta.consultar(idPublicacion));
    }

    private void reserva(UUID idHuesped, String estado) {
        when(reservas.consultarReservaParaResena(ID_RESERVA)).thenReturn(Optional.of(
                new ReservaResenaInfo(ID_RESERVA, idHuesped, UUID.randomUUID(), UUID.randomUUID(), estado)));
    }

    private static Resena resena(boolean visible) {
        return new Resena(UUID.randomUUID(), ID_RESERVA, 5, "Excelente", visible,
                OffsetDateTime.now(), null, null, null);
    }

    private static void assertError(Class<? extends ExcepcionDominio> tipo, String mensaje, Executable accion) {
        ExcepcionDominio error = assertThrows(tipo, accion);
        assertEquals(mensaje, error.getMessage());
    }
}
