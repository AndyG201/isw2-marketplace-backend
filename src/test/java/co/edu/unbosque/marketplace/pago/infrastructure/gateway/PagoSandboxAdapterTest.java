package co.edu.unbosque.marketplace.pago.infrastructure.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.FallaInternaException;
import co.edu.unbosque.marketplace.compartido.domain.ServicioNoDisponibleException;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoReembolsoSandbox;

/** Validaciones y manejo de errores de la pasarela de pruebas. El cliente HTTP se reemplaza por un mock: no hay red. */
class PagoSandboxAdapterTest {

    private static final BigDecimal MONTO = new BigDecimal("100.00");

    private final HttpClient cliente = mock(HttpClient.class);

    private PagoSandboxAdapter adaptador(String clave, String moneda, String metodo) {
        PagoSandboxAdapter adaptador = new PagoSandboxAdapter(new ObjectMapper(), clave, moneda, metodo);
        ReflectionTestUtils.setField(adaptador, "httpClient", cliente);
        return adaptador;
    }

    private PagoSandboxAdapter adaptador() {
        return adaptador("sk_test_abc", "cop", "pm_card_visa");
    }

    @SuppressWarnings("unchecked")
    private void respuesta(int estado, String cuerpo) throws Exception {
        HttpResponse<String> respuesta = mock(HttpResponse.class);
        when(respuesta.statusCode()).thenReturn(estado);
        when(respuesta.body()).thenReturn(cuerpo);
        when(cliente.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(respuesta);
    }

    @AfterEach
    void limpiarInterrupcion() {
        Thread.interrupted();
    }

    @Test
    void procesarExigeUnaClaveDePrueba() {
        assertThrows(FallaInternaException.class, () -> adaptador("", "cop", "pm").procesar(MONTO, "k"));
        assertThrows(FallaInternaException.class, () -> adaptador("sk_live_x", "cop", "pm").procesar(MONTO, "k"));
    }

    @Test
    void procesarExigeUnaMonedaValida() {
        assertThrows(FallaInternaException.class, () -> adaptador("sk_test_a", "pesos", "pm").procesar(MONTO, "k"));
    }

    @Test
    void procesarExigeMetodoDePagoClaveYMontoPositivo() {
        assertThrows(DatosInvalidosException.class, () -> adaptador().procesar(MONTO, " "));
        assertThrows(DatosInvalidosException.class, () -> adaptador().procesar(MONTO, null));
        assertThrows(DatosInvalidosException.class, () -> adaptador("sk_test_a", "cop", " ").procesar(MONTO, "k"));
        assertThrows(DatosInvalidosException.class, () -> adaptador().procesar(BigDecimal.ZERO, "k"));
        assertThrows(DatosInvalidosException.class, () -> adaptador().procesar(null, "k"));
    }

    @Test
    void procesarRechazaUnMontoConMasDecimalesQueLaMoneda() {
        assertThrows(DatosInvalidosException.class, () -> adaptador().procesar(new BigDecimal("10.005"), "k"));
    }

    @Test
    void procesarAprobadoDevuelveLaReferencia() throws Exception {
        respuesta(200, "{\"id\":\"pi_123\",\"status\":\"succeeded\"}");

        ResultadoPagoSandbox resultado = adaptador().procesar(MONTO, "k");

        assertTrue(resultado.aprobado());
        assertEquals("pi_123", resultado.referencia());
    }

    @Test
    void procesarNoAprobadoPorEstadoDistintoDeSucceeded() throws Exception {
        respuesta(200, "{\"id\":\"pi_123\",\"status\":\"requires_action\"}");

        assertFalse(adaptador().procesar(MONTO, "k").aprobado());
    }

    @Test
    void procesarConPagoRechazado402NoEsUnErrorDelServicio() throws Exception {
        respuesta(402, "{\"error\":{\"message\":\"Tarjeta rechazada\",\"payment_intent\":{\"id\":\"pi_9\"}}}");

        ResultadoPagoSandbox resultado = adaptador().procesar(MONTO, "k");

        assertFalse(resultado.aprobado());
        assertEquals("pi_9", resultado.referencia());
        assertEquals("Tarjeta rechazada", resultado.mensaje());
    }

    @Test
    void procesarConErrorDelProveedorLanzaServicioNoDisponible() throws Exception {
        respuesta(500, "{\"error\":{\"message\":\"Fallo interno\"}}");

        assertThrows(ServicioNoDisponibleException.class, () -> adaptador().procesar(MONTO, "k"));
    }

    @Test
    void procesarSinConexionLanzaServicioNoDisponible() throws Exception {
        when(cliente.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException("sin red"));

        assertThrows(ServicioNoDisponibleException.class, () -> adaptador().procesar(MONTO, "k"));
    }

    @Test
    void procesarInterrumpidoLanzaServicioNoDisponibleYConservaLaInterrupcion() throws Exception {
        when(cliente.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new InterruptedException());

        assertThrows(ServicioNoDisponibleException.class, () -> adaptador().procesar(MONTO, "k"));
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void reembolsarValidaLosDatos() {
        assertThrows(FallaInternaException.class, () -> adaptador("", "cop", "pm").reembolsar("pi_1", MONTO, "k"));
        assertThrows(DatosInvalidosException.class, () -> adaptador().reembolsar("xx", MONTO, "k"));
        assertThrows(DatosInvalidosException.class, () -> adaptador().reembolsar(null, MONTO, "k"));
        assertThrows(DatosInvalidosException.class, () -> adaptador().reembolsar("pi_1", BigDecimal.ZERO, "k"));
        assertThrows(DatosInvalidosException.class, () -> adaptador().reembolsar("pi_1", null, "k"));
        assertThrows(DatosInvalidosException.class, () -> adaptador().reembolsar("pi_1", MONTO, " "));
    }

    @Test
    void reembolsarConfirmadoDevuelveLaReferencia() throws Exception {
        respuesta(200, "{\"id\":\"re_1\",\"status\":\"succeeded\"}");

        ResultadoReembolsoSandbox resultado = adaptador().reembolsar("pi_1", MONTO, "k");

        assertTrue(resultado.confirmado());
        assertEquals("re_1", resultado.referenciaReembolso());
    }

    @Test
    void reembolsarConErrorDelProveedorSinConexionOInterrumpidoLanzaServicioNoDisponible() throws Exception {
        respuesta(500, "{\"error\":{\"message\":\"Fallo\"}}");
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador().reembolsar("pi_1", MONTO, "k"));

        when(cliente.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException("sin red"));
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador().reembolsar("pi_1", MONTO, "k"));

        when(cliente.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new InterruptedException());
        assertThrows(ServicioNoDisponibleException.class, () -> adaptador().reembolsar("pi_1", MONTO, "k"));
    }

    @Test
    void consultarReembolsoExigeUnaReferenciaValida() {
        assertThrows(DatosInvalidosException.class, () -> adaptador().consultarReembolso("xx"));
        assertThrows(DatosInvalidosException.class, () -> adaptador().consultarReembolso(null));
    }

    @Test
    void consultarReembolsoDevuelveElEstado() throws Exception {
        respuesta(200, "{\"id\":\"re_1\",\"status\":\"pending\"}");

        assertTrue(adaptador().consultarReembolso("re_1").pendiente());
    }
}
