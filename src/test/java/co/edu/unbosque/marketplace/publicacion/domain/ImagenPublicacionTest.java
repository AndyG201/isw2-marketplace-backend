package co.edu.unbosque.marketplace.publicacion.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ImagenPublicacionTest {

    @Test
    void debeCrearImagenPublicacionConConstructorVacio() {
        ImagenPublicacion imagen = new ImagenPublicacion();

        assertNotNull(imagen);
        assertNull(imagen.getIdImagen());
        assertNull(imagen.getIdPublicacion());
        assertNull(imagen.getRutaImagen());
        assertEquals(0, imagen.getOrden());
        assertFalse(imagen.isEsPortada());
    }

    @Test
    void debeCrearImagenPublicacionCorrectamente() {
        UUID idImagen = UUID.randomUUID();
        UUID idPublicacion = UUID.randomUUID();

        ImagenPublicacion imagen = new ImagenPublicacion(
                idImagen,
                idPublicacion,
                "/imagenes/publicacion/fachada.jpg",
                1,
                true
        );

        assertEquals(idImagen, imagen.getIdImagen());
        assertEquals(idPublicacion, imagen.getIdPublicacion());
        assertEquals("/imagenes/publicacion/fachada.jpg", imagen.getRutaImagen());
        assertEquals(1, imagen.getOrden());
        assertTrue(imagen.isEsPortada());
    }

    @Test
    void debeModificarIdImagen() {
        ImagenPublicacion imagen = new ImagenPublicacion();
        UUID idImagen = UUID.randomUUID();

        imagen.setIdImagen(idImagen);

        assertEquals(idImagen, imagen.getIdImagen());
    }

    @Test
    void debeModificarIdPublicacion() {
        ImagenPublicacion imagen = new ImagenPublicacion();
        UUID idPublicacion = UUID.randomUUID();

        imagen.setIdPublicacion(idPublicacion);

        assertEquals(idPublicacion, imagen.getIdPublicacion());
    }

    @Test
    void debeModificarRutaImagen() {
        ImagenPublicacion imagen = new ImagenPublicacion();

        imagen.setRutaImagen("/imagenes/publicacion/habitacion.jpg");

        assertEquals(
                "/imagenes/publicacion/habitacion.jpg",
                imagen.getRutaImagen()
        );
    }

    @Test
    void debeModificarOrden() {
        ImagenPublicacion imagen = new ImagenPublicacion();

        imagen.setOrden(5);

        assertEquals(5, imagen.getOrden());
    }

    @Test
    void debeModificarEstadoPortada() {
        ImagenPublicacion imagen = new ImagenPublicacion();

        imagen.setEsPortada(true);

        assertTrue(imagen.isEsPortada());

        imagen.setEsPortada(false);

        assertFalse(imagen.isEsPortada());
    }

    @Test
    void debePermitirModificarTodosLosAtributos() {
        UUID idImagen = UUID.randomUUID();
        UUID idPublicacion = UUID.randomUUID();

        ImagenPublicacion imagen = new ImagenPublicacion();

        imagen.setIdImagen(idImagen);
        imagen.setIdPublicacion(idPublicacion);
        imagen.setRutaImagen("/imagenes/nueva.jpg");
        imagen.setOrden(3);
        imagen.setEsPortada(true);

        assertAll(
                () -> assertEquals(idImagen, imagen.getIdImagen()),
                () -> assertEquals(idPublicacion, imagen.getIdPublicacion()),
                () -> assertEquals("/imagenes/nueva.jpg", imagen.getRutaImagen()),
                () -> assertEquals(3, imagen.getOrden()),
                () -> assertTrue(imagen.isEsPortada())
        );
    }
}
