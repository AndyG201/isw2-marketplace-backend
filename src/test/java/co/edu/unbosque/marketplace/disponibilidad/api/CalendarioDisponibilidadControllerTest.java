package co.edu.unbosque.marketplace.disponibilidad.api;


import static co.edu.unbosque.marketplace.config.TraductorDeErroresPrueba.traducir;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.disponibilidad.application.dto.CalendarioDisponibilidadResponse;
import co.edu.unbosque.marketplace.disponibilidad.application.dto.ConsultaDisponibilidadResponse;
import co.edu.unbosque.marketplace.disponibilidad.application.CalendarioDisponibilidadService;

class CalendarioDisponibilidadControllerTest {

    private CalendarioDisponibilidadService calendarioService;
    private CalendarioDisponibilidadController controller;

    private UUID idAlojamiento;
    private UUID idAnfitrion;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @BeforeEach
    void setUp() {
        calendarioService = mock(CalendarioDisponibilidadService.class);
        controller = new CalendarioDisponibilidadController(calendarioService);

        idAlojamiento = UUID.randomUUID();
        idAnfitrion = UUID.randomUUID();

        fechaInicio = LocalDate.of(2026, 12, 10);
        fechaFin = LocalDate.of(2026, 12, 15);
    }

    @Test
    void debeConsultarDisponibilidadPorFechas() {
        ConsultaDisponibilidadResponse respuesta =
                new ConsultaDisponibilidadResponse(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin,
                        true,
                        true,
                        List.of(),
                        List.of()                );

        when(calendarioService.consultarPorFechas(
                idAlojamiento, fechaInicio, fechaFin
        )).thenReturn(respuesta);

        ResponseEntity<?> resultado = traducir(() ->
                controller.consultarPorFechas(
                idAlojamiento, fechaInicio, fechaFin
        ));

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(respuesta, resultado.getBody());

        verify(calendarioService).consultarPorFechas(
                idAlojamiento, fechaInicio, fechaFin
        );
    }

    @Test
    void debeRetornarNotFoundCuandoNoExisteElAlojamiento() {
        when(calendarioService.consultarPorFechas(
                idAlojamiento, fechaInicio, fechaFin
        )).thenThrow(new RecursoNoEncontradoException("Alojamiento no encontrado."));

        ResponseEntity<?> resultado = traducir(() ->
                controller.consultarPorFechas(
                idAlojamiento, fechaInicio, fechaFin
        ));

        assertEquals(HttpStatus.NOT_FOUND, resultado.getStatusCode());
        assertEquals(
                Map.of("mensaje", "Alojamiento no encontrado."),
                resultado.getBody()
        );
    }

    @Test
    void debeRetornarBadRequestCuandoLosParametrosSonInvalidos() {
        when(calendarioService.consultarPorFechas(
                idAlojamiento, fechaInicio, fechaFin
        )).thenThrow(new DatosInvalidosException("Rango de fechas inválido."));

        ResponseEntity<?> resultado = traducir(() ->
                controller.consultarPorFechas(
                idAlojamiento, fechaInicio, fechaFin
        ));

        assertEquals(HttpStatus.BAD_REQUEST, resultado.getStatusCode());
        assertEquals(
                Map.of("mensaje", "Rango de fechas inválido."),
                resultado.getBody()
        );
    }

    @Test
    void debeRetornarUnauthorizedCuandoElAnfitrionEsNulo() {
        ResponseEntity<?> resultado = traducir(() ->
                controller.consultarCalendarioCompleto(
                        null, idAlojamiento
                ));

        assertEquals(HttpStatus.UNAUTHORIZED, resultado.getStatusCode());
        assertEquals(
                Map.of(
                        "mensaje",
                        "Debes iniciar sesión para consultar el calendario completo."
                ),
                resultado.getBody()
        );

        verifyNoInteractions(calendarioService);
    }

    @Test
    void debeConsultarCalendarioCompleto() {
        CalendarioDisponibilidadResponse respuesta =
                new CalendarioDisponibilidadResponse(
                        idAlojamiento,
                        List.of(),
                        List.of()
                );

        when(calendarioService.consultarCalendarioCompleto(
                idAnfitrion, idAlojamiento
        )).thenReturn(respuesta);

        ResponseEntity<?> resultado = traducir(() ->
                controller.consultarCalendarioCompleto(
                        idAnfitrion, idAlojamiento
                ));

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(respuesta, resultado.getBody());

        verify(calendarioService).consultarCalendarioCompleto(
                idAnfitrion, idAlojamiento
        );
    }

    @Test
    void debeRetornarForbiddenCuandoElAnfitrionNoTienePermiso() {
        when(calendarioService.consultarCalendarioCompleto(
                idAnfitrion, idAlojamiento
        )).thenThrow(new AccesoNoPermitidoException("Acceso denegado."));

        ResponseEntity<?> resultado = traducir(() ->
                controller.consultarCalendarioCompleto(
                        idAnfitrion, idAlojamiento
                ));

        assertEquals(HttpStatus.FORBIDDEN, resultado.getStatusCode());
        assertEquals(
                Map.of("mensaje", "Acceso denegado."),
                resultado.getBody()
        );
    }

    @Test
    void debeRetornarNotFoundCuandoNoExisteElCalendario() {
        when(calendarioService.consultarCalendarioCompleto(
                idAnfitrion, idAlojamiento
        )).thenThrow(new RecursoNoEncontradoException("Calendario no encontrado."));

        ResponseEntity<?> resultado = traducir(() ->
                controller.consultarCalendarioCompleto(
                        idAnfitrion, idAlojamiento
                ));

        assertEquals(HttpStatus.NOT_FOUND, resultado.getStatusCode());
        assertEquals(
                Map.of("mensaje", "Calendario no encontrado."),
                resultado.getBody()
        );
    }

    @Test
    void debeRetornarBadRequestCuandoLosDatosDelCalendarioSonInvalidos() {
        when(calendarioService.consultarCalendarioCompleto(
                idAnfitrion, idAlojamiento
        )).thenThrow(new DatosInvalidosException("Datos inválidos."));

        ResponseEntity<?> resultado = traducir(() ->
                controller.consultarCalendarioCompleto(
                        idAnfitrion, idAlojamiento
                ));

        assertEquals(HttpStatus.BAD_REQUEST, resultado.getStatusCode());
        assertEquals(
                Map.of("mensaje", "Datos inválidos."),
                resultado.getBody()
        );
    }
}
