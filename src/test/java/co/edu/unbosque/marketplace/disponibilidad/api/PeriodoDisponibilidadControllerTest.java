package co.edu.unbosque.marketplace.disponibilidad.api;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;

import co.edu.unbosque.marketplace.disponibilidad.application.dto.PeriodoDisponibilidadRequest;
import co.edu.unbosque.marketplace.disponibilidad.application.dto.PeriodoDisponibilidadResponse;
import co.edu.unbosque.marketplace.disponibilidad.application.PeriodoDisponibilidadService;

class PeriodoDisponibilidadControllerTest {

    private PeriodoDisponibilidadService disponibilidadService;
    private PeriodoDisponibilidadController controller;
    private BindingResult resultadoValidacion;

    private UUID idAnfitrion;
    private UUID idAlojamiento;
    private UUID idPeriodo;

    private PeriodoDisponibilidadRequest solicitud;
    private PeriodoDisponibilidadResponse respuesta;

    @BeforeEach
    void setUp() {
        disponibilidadService = mock(PeriodoDisponibilidadService.class);
        controller = new PeriodoDisponibilidadController(disponibilidadService);
        resultadoValidacion = mock(BindingResult.class);

        idAnfitrion = UUID.randomUUID();
        idAlojamiento = UUID.randomUUID();
        idPeriodo = UUID.randomUUID();

        solicitud = new PeriodoDisponibilidadRequest();
        solicitud.setFechaInicio(java.time.LocalDate.of(2026, 12, 10));
        solicitud.setFechaFin(java.time.LocalDate.of(2026, 12, 15));

        respuesta = new PeriodoDisponibilidadResponse(
                idPeriodo,
                idAlojamiento,
                solicitud.getFechaInicio(),
                solicitud.getFechaFin(),
                true,
                java.time.OffsetDateTime.now()
        );

        when(resultadoValidacion.hasErrors()).thenReturn(false);
    }

    // REGISTRAR PERIODO

    @Test
    void debeRegistrarPeriodoCorrectamente() {
        when(disponibilidadService.registrarPeriodo(
                idAnfitrion, idAlojamiento, solicitud
        )).thenReturn(respuesta);

        ResponseEntity<?> resultado = controller.registrarPeriodo(
                idAnfitrion, idAlojamiento, solicitud, resultadoValidacion
        );

        assertEquals(HttpStatus.CREATED, resultado.getStatusCode());
        assertEquals(respuesta, resultado.getBody());

        verify(disponibilidadService).registrarPeriodo(
                idAnfitrion, idAlojamiento, solicitud
        );
    }

    @Test
    void debeRetornarUnauthorizedAlRegistrarSinAutenticacion() {
        ResponseEntity<?> resultado = controller.registrarPeriodo(
                null, idAlojamiento, solicitud, resultadoValidacion
        );

        assertEquals(HttpStatus.UNAUTHORIZED, resultado.getStatusCode());
        assertEquals(
                Map.of("mensaje", "Debes iniciar sesión para registrar periodos."),
                resultado.getBody()
        );

        verifyNoInteractions(disponibilidadService);
    }

    @Test
    void debeRetornarBadRequestAlRegistrarConErroresDeValidacion() {
        when(resultadoValidacion.hasErrors()).thenReturn(true);

        ResponseEntity<?> resultado = controller.registrarPeriodo(
                idAnfitrion, idAlojamiento, solicitud, resultadoValidacion
        );

        assertEquals(HttpStatus.BAD_REQUEST, resultado.getStatusCode());
        verifyNoInteractions(disponibilidadService);
    }

    @Test
    void debeRetornarForbiddenAlRegistrarSinPermiso() {
        when(disponibilidadService.registrarPeriodo(
                idAnfitrion, idAlojamiento, solicitud
        )).thenThrow(new AccessDeniedException("Acceso denegado."));

        ResponseEntity<?> resultado = controller.registrarPeriodo(
                idAnfitrion, idAlojamiento, solicitud, resultadoValidacion
        );

        assertEquals(HttpStatus.FORBIDDEN, resultado.getStatusCode());
        assertEquals(
                Map.of("mensaje", "Acceso denegado."),
                resultado.getBody()
        );
    }

    @Test
    void debeRetornarNotFoundAlRegistrarEnAlojamientoInexistente() {
        when(disponibilidadService.registrarPeriodo(
                idAnfitrion, idAlojamiento, solicitud
        )).thenThrow(new NoSuchElementException("Alojamiento no encontrado."));

        ResponseEntity<?> resultado = controller.registrarPeriodo(
                idAnfitrion, idAlojamiento, solicitud, resultadoValidacion
        );

        assertEquals(HttpStatus.NOT_FOUND, resultado.getStatusCode());
    }

    @Test
    void debeRetornarBadRequestAlRegistrarConDatosInvalidos() {
        when(disponibilidadService.registrarPeriodo(
                idAnfitrion, idAlojamiento, solicitud
        )).thenThrow(new IllegalArgumentException("Fechas inválidas."));

        ResponseEntity<?> resultado = controller.registrarPeriodo(
                idAnfitrion, idAlojamiento, solicitud, resultadoValidacion
        );

        assertEquals(HttpStatus.BAD_REQUEST, resultado.getStatusCode());
    }

    @Test
    void debeRetornarConflictAlRegistrarUnPeriodoEnConflicto() {
        when(disponibilidadService.registrarPeriodo(
                idAnfitrion, idAlojamiento, solicitud
        )).thenThrow(new IllegalStateException("El periodo se encuentra ocupado."));

        ResponseEntity<?> resultado = controller.registrarPeriodo(
                idAnfitrion, idAlojamiento, solicitud, resultadoValidacion
        );

        assertEquals(HttpStatus.CONFLICT, resultado.getStatusCode());
    }

    // CONSULTAR PERIODOS

    @Test
    void debeConsultarPeriodosCorrectamente() {
        List<PeriodoDisponibilidadResponse> periodos = List.of(respuesta);

        when(disponibilidadService.consultarPeriodos(
                idAnfitrion, idAlojamiento
        )).thenReturn(periodos);

        ResponseEntity<?> resultado = controller.consultarPeriodos(
                idAnfitrion, idAlojamiento
        );

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(periodos, resultado.getBody());
    }

    @Test
    void debeRetornarUnauthorizedAlConsultarSinAutenticacion() {
        ResponseEntity<?> resultado = controller.consultarPeriodos(
                null, idAlojamiento
        );

        assertEquals(HttpStatus.UNAUTHORIZED, resultado.getStatusCode());
        assertEquals(
                Map.of("mensaje", "Debes iniciar sesión para consultar los periodos."),
                resultado.getBody()
        );

        verifyNoInteractions(disponibilidadService);
    }

    @Test
    void debeRetornarForbiddenAlConsultarSinPermiso() {
        when(disponibilidadService.consultarPeriodos(
                idAnfitrion, idAlojamiento
        )).thenThrow(new AccessDeniedException("Acceso denegado."));

        ResponseEntity<?> resultado = controller.consultarPeriodos(
                idAnfitrion, idAlojamiento
        );

        assertEquals(HttpStatus.FORBIDDEN, resultado.getStatusCode());
    }

    @Test
    void debeRetornarNotFoundAlConsultarAlojamientoInexistente() {
        when(disponibilidadService.consultarPeriodos(
                idAnfitrion, idAlojamiento
        )).thenThrow(new NoSuchElementException("Alojamiento no encontrado."));

        ResponseEntity<?> resultado = controller.consultarPeriodos(
                idAnfitrion, idAlojamiento
        );

        assertEquals(HttpStatus.NOT_FOUND, resultado.getStatusCode());
    }

    @Test
    void debeRetornarBadRequestAlConsultarConDatosInvalidos() {
        when(disponibilidadService.consultarPeriodos(
                idAnfitrion, idAlojamiento
        )).thenThrow(new IllegalArgumentException("Identificador inválido."));

        ResponseEntity<?> resultado = controller.consultarPeriodos(
                idAnfitrion, idAlojamiento
        );

        assertEquals(HttpStatus.BAD_REQUEST, resultado.getStatusCode());
    }

    // ACTUALIZAR PERIODO

    @Test
    void debeActualizarPeriodoCorrectamente() {
        when(disponibilidadService.actualizarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo, solicitud
        )).thenReturn(respuesta);

        ResponseEntity<?> resultado = controller.actualizarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo,
                solicitud, resultadoValidacion
        );

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(respuesta, resultado.getBody());
    }

    @Test
    void debeRetornarUnauthorizedAlActualizarSinAutenticacion() {
        ResponseEntity<?> resultado = controller.actualizarPeriodo(
                null, idAlojamiento, idPeriodo,
                solicitud, resultadoValidacion
        );

        assertEquals(HttpStatus.UNAUTHORIZED, resultado.getStatusCode());
        verifyNoInteractions(disponibilidadService);
    }

    @Test
    void debeRetornarBadRequestAlActualizarConErroresDeValidacion() {
        when(resultadoValidacion.hasErrors()).thenReturn(true);

        ResponseEntity<?> resultado = controller.actualizarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo,
                solicitud, resultadoValidacion
        );

        assertEquals(HttpStatus.BAD_REQUEST, resultado.getStatusCode());
        verifyNoInteractions(disponibilidadService);
    }

    @Test
    void debeRetornarForbiddenAlActualizarSinPermiso() {
        when(disponibilidadService.actualizarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo, solicitud
        )).thenThrow(new AccessDeniedException("Acceso denegado."));

        ResponseEntity<?> resultado = controller.actualizarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo,
                solicitud, resultadoValidacion
        );

        assertEquals(HttpStatus.FORBIDDEN, resultado.getStatusCode());
    }

    @Test
    void debeRetornarNotFoundAlActualizarPeriodoInexistente() {
        when(disponibilidadService.actualizarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo, solicitud
        )).thenThrow(new NoSuchElementException("Periodo no encontrado."));

        ResponseEntity<?> resultado = controller.actualizarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo,
                solicitud, resultadoValidacion
        );

        assertEquals(HttpStatus.NOT_FOUND, resultado.getStatusCode());
    }

    @Test
    void debeRetornarBadRequestAlActualizarConDatosInvalidos() {
        when(disponibilidadService.actualizarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo, solicitud
        )).thenThrow(new IllegalArgumentException("Fechas inválidas."));

        ResponseEntity<?> resultado = controller.actualizarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo,
                solicitud, resultadoValidacion
        );

        assertEquals(HttpStatus.BAD_REQUEST, resultado.getStatusCode());
    }

    @Test
    void debeRetornarConflictAlActualizarPeriodoEnConflicto() {
        when(disponibilidadService.actualizarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo, solicitud
        )).thenThrow(new IllegalStateException("El periodo se encuentra ocupado."));

        ResponseEntity<?> resultado = controller.actualizarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo,
                solicitud, resultadoValidacion
        );

        assertEquals(HttpStatus.CONFLICT, resultado.getStatusCode());
    }

    // DESHABILITAR PERIODO

    @Test
    void debeDeshabilitarPeriodoCorrectamente() {
        when(disponibilidadService.deshabilitarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo
        )).thenReturn(respuesta);

        ResponseEntity<?> resultado = controller.deshabilitarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo
        );

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(respuesta, resultado.getBody());
    }

    @Test
    void debeRetornarUnauthorizedAlDeshabilitarSinAutenticacion() {
        ResponseEntity<?> resultado = controller.deshabilitarPeriodo(
                null, idAlojamiento, idPeriodo
        );

        assertEquals(HttpStatus.UNAUTHORIZED, resultado.getStatusCode());
        verifyNoInteractions(disponibilidadService);
    }

    @Test
    void debeRetornarForbiddenAlDeshabilitarSinPermiso() {
        when(disponibilidadService.deshabilitarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo
        )).thenThrow(new AccessDeniedException("Acceso denegado."));

        ResponseEntity<?> resultado = controller.deshabilitarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo
        );

        assertEquals(HttpStatus.FORBIDDEN, resultado.getStatusCode());
    }

    @Test
    void debeRetornarNotFoundAlDeshabilitarPeriodoInexistente() {
        when(disponibilidadService.deshabilitarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo
        )).thenThrow(new NoSuchElementException("Periodo no encontrado."));

        ResponseEntity<?> resultado = controller.deshabilitarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo
        );

        assertEquals(HttpStatus.NOT_FOUND, resultado.getStatusCode());
    }

    @Test
    void debeRetornarBadRequestAlDeshabilitarConDatosInvalidos() {
        when(disponibilidadService.deshabilitarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo
        )).thenThrow(new IllegalArgumentException("Identificador inválido."));

        ResponseEntity<?> resultado = controller.deshabilitarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo
        );

        assertEquals(HttpStatus.BAD_REQUEST, resultado.getStatusCode());
    }

    @Test
    void debeRetornarConflictAlDeshabilitarPeriodoEnConflicto() {
        when(disponibilidadService.deshabilitarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo
        )).thenThrow(new IllegalStateException("No se puede deshabilitar el periodo."));

        ResponseEntity<?> resultado = controller.deshabilitarPeriodo(
                idAnfitrion, idAlojamiento, idPeriodo
        );

        assertEquals(HttpStatus.CONFLICT, resultado.getStatusCode());
    }
}
