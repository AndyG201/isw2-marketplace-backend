package co.edu.unbosque.marketplace.publicacion.api.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ImagenPublicacionResponseTest {

	@Test
	void testConstructorYGetters() {
		UUID idImagen = UUID.randomUUID();

		ImagenPublicacionResponse response = new ImagenPublicacionResponse(idImagen, "imagenes/publicacion1.jpg", 1,
				true);
		assertEquals(idImagen, response.getIdImagen());
		assertEquals("imagenes/publicacion1.jpg", response.getRutaImagen());
		assertEquals(1, response.getOrden());
		assertTrue(response.isEsPortada());
	}

	@Test
	void testImagenNoEsPortada() {
		ImagenPublicacionResponse response = new ImagenPublicacionResponse(UUID.randomUUID(),
				"imagenes/publicacion2.jpg", 2, false);

		assertFalse(response.isEsPortada());
	}
}