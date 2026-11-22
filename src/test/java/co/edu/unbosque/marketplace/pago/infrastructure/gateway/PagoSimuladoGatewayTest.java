package co.edu.unbosque.marketplace.pago.infrastructure.gateway;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoReembolsoSandbox;

class PagoSimuladoGatewayTest {

    private PagoSimuladoGateway pasarela;

    @BeforeEach
    void preparar() {
        pasarela = new PagoSimuladoGateway();
    }

    @Test
    void apruebaUnPagoConReferenciaConFormatoDeStripe() {
        ResultadoPagoSandbox resultado = pasarela.procesar(new BigDecimal("220000.00"), "clave-1");

        assertTrue(resultado.aprobado());
        assertTrue(resultado.referencia().matches("pi_[A-Za-z0-9]+"), resultado.referencia());
        assertFalse(resultado.mensaje().isBlank());
    }

    @Test
    void laMismaClaveDevuelveElMismoResultadoSinProcesarDeNuevo() {
        ResultadoPagoSandbox primero = pasarela.procesar(new BigDecimal("100.00"), "clave-repetida");
        ResultadoPagoSandbox segundo = pasarela.procesar(new BigDecimal("999.00"), "clave-repetida");

        assertSame(primero, segundo);
    }

    @Test
    void clavesDistintasDanReferenciasDistintas() {
        String a = pasarela.procesar(new BigDecimal("100.00"), "clave-a").referencia();
        String b = pasarela.procesar(new BigDecimal("100.00"), "clave-b").referencia();

        assertNotEquals(a, b);
    }

    @Test
    void rechazaLosMontosConDecimalesDeTarjetaRechazada() {
        ResultadoPagoSandbox resultado = pasarela.procesar(new BigDecimal("100.13"), "clave-rechazo");

        assertFalse(resultado.aprobado());
        assertTrue(resultado.referencia().startsWith("pi_sim"));
        assertTrue(resultado.mensaje().contains("rechazada"));
        // Otros decimales no se rechazan.
        assertTrue(pasarela.procesar(new BigDecimal("100.14"), "clave-otra").aprobado());
        assertTrue(pasarela.procesar(new BigDecimal("113"), "clave-entero").aprobado());
    }

    @Test
    void validaElMontoYLaClaveComoLaPasarelaReal() {
        assertThrows(DatosInvalidosException.class, () -> pasarela.procesar(null, "clave"));
        assertThrows(DatosInvalidosException.class, () -> pasarela.procesar(BigDecimal.ZERO, "clave"));
        assertThrows(DatosInvalidosException.class, () -> pasarela.procesar(new BigDecimal("-5"), "clave"));
        assertThrows(DatosInvalidosException.class, () -> pasarela.procesar(BigDecimal.TEN, null));
        assertThrows(DatosInvalidosException.class, () -> pasarela.procesar(BigDecimal.TEN, "  "));
    }

    @Test
    void reembolsaAlInstanteYDeFormaIdempotente() {
        ResultadoReembolsoSandbox primero = pasarela.reembolsar("pi_abc123", new BigDecimal("50.00"), "refund-1");
        ResultadoReembolsoSandbox segundo = pasarela.reembolsar("pi_abc123", new BigDecimal("50.00"), "refund-1");

        assertTrue(primero.confirmado());
        assertFalse(primero.pendiente());
        assertFalse(primero.fallido());
        assertTrue(primero.referenciaReembolso().matches("re_[A-Za-z0-9]+"));
        assertSame(primero, segundo);
        assertNotEquals(primero.referenciaReembolso(),
                pasarela.reembolsar("pi_abc123", new BigDecimal("50.00"), "refund-2").referenciaReembolso());
    }

    @Test
    void validaLosDatosDelReembolso() {
        BigDecimal monto = BigDecimal.TEN;
        assertThrows(DatosInvalidosException.class, () -> pasarela.reembolsar(null, monto, "r"));
        assertThrows(DatosInvalidosException.class, () -> pasarela.reembolsar("sin-formato", monto, "r"));
        assertThrows(DatosInvalidosException.class, () -> pasarela.reembolsar("pi_abc", null, "r"));
        assertThrows(DatosInvalidosException.class, () -> pasarela.reembolsar("pi_abc", BigDecimal.ZERO, "r"));
        assertThrows(DatosInvalidosException.class, () -> pasarela.reembolsar("pi_abc", monto, null));
        assertThrows(DatosInvalidosException.class, () -> pasarela.reembolsar("pi_abc", monto, " "));
    }

    @Test
    void consultaUnReembolsoConfirmadoYValidaLaReferencia() {
        ResultadoReembolsoSandbox consulta = pasarela.consultarReembolso("re_abc123");

        assertTrue(consulta.confirmado());
        assertEquals("re_abc123", consulta.referenciaReembolso());
        assertThrows(DatosInvalidosException.class, () -> pasarela.consultarReembolso(null));
        assertThrows(DatosInvalidosException.class, () -> pasarela.consultarReembolso("abc"));
    }

    @Test
    void soloUnaDeLasDosPasarelasEstaActivaSegunElPerfil() {
        assertArrayEquals(new String[] {"pago-simulado"}, PagoSimuladoGateway.class.getAnnotation(Profile.class).value());
        assertArrayEquals(new String[] {"!pago-simulado"}, PagoSandboxAdapter.class.getAnnotation(Profile.class).value());
    }
}
