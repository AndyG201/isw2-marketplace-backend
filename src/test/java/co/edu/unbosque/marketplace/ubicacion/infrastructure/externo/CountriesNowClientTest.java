package co.edu.unbosque.marketplace.ubicacion.infrastructure.externo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import co.edu.unbosque.marketplace.ubicacion.application.port.CatalogoUbicacionesExterno.PaisExterno;

class CountriesNowClientTest {

    private HttpServer servidor;
    private CountriesNowClient cliente;
    private final AtomicReference<String> cuerpoRecibido = new AtomicReference<>();

    @BeforeEach
    void iniciar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.start();
        cliente = new CountriesNowClient("http://127.0.0.1:" + servidor.getAddress().getPort());
    }

    @AfterEach
    void detener() {
        servidor.stop(0);
    }

    @Test
    void leeLosPaisesValidosConSuCodigoIso() {
        responder("/countries/iso", """
                {"error":false,"data":[
                  {"name":"Colombia","Iso2":"co"},
                  {"name":" ","Iso2":"PE"},
                  {"name":"Sin código","Iso2":"X1"}
                ]}""");

        assertEquals(List.of(new PaisExterno("Colombia", "CO")), cliente.paises());
    }

    @Test
    void consultaLasCiudadesDelPaisIndicado() {
        responder("/countries/cities", """
                {"error":false,"data":["Bogotá"," ","Medellín"]}""");

        assertEquals(List.of("Bogotá", "Medellín"), cliente.ciudades("Colombia"));
        assertEquals("{\"country\":\"Colombia\"}", cuerpoRecibido.get());
    }

    @Test
    void rechazaUnaRespuestaConError() {
        responder("/countries/iso", """
                {"error":true,"msg":"falló"}""");

        assertThrows(IllegalStateException.class, () -> cliente.paises());
    }

    private void responder(String ruta, String json) {
        servidor.createContext(ruta, intercambio -> escribir(intercambio, json));
    }

    private void escribir(HttpExchange intercambio, String json) throws IOException {
        cuerpoRecibido.set(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        byte[] respuesta = json.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().add("Content-Type", "application/json");
        intercambio.sendResponseHeaders(200, respuesta.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(respuesta);
        }
    }
}
