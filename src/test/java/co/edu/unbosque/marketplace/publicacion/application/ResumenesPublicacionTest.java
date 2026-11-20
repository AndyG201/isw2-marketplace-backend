package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;

class ResumenesPublicacionTest {

    private final ImagenPublicacionRepository repositorio =
            mock(ImagenPublicacionRepository.class);

    @Test
    void sinPublicacionesNoConsultaImagenes() {

        assertTrue(ResumenesPublicacion.consultar(repositorio, List.of()).isEmpty());

        verifyNoInteractions(repositorio);
    }

    @Test
    void prefiereLaPortadaYSinoLaPrimeraImagen() {

        UUID conPortada = UUID.randomUUID();
        UUID sinPortada = UUID.randomUUID();

        when(repositorio.findByIdPublicacionInOrderByOrdenAsc(List.of(conPortada, sinPortada)))
                .thenReturn(List.of(
                        imagen(conPortada, "uno.jpg", 1, false),
                        imagen(conPortada, "dos.jpg", 2, true),
                        imagen(sinPortada, "tercera.jpg", 3, false),
                        imagen(sinPortada, "segunda.jpg", 2, false)
                ));

        Map<UUID, String> portadas =
                ResumenesPublicacion.consultar(repositorio, List.of(conPortada, sinPortada));

        assertEquals("dos.jpg", portadas.get(conPortada));
        assertEquals("segunda.jpg", portadas.get(sinPortada));
    }

    private ImagenPublicacion imagen(UUID idPublicacion, String ruta, int orden, boolean portada) {
        return new ImagenPublicacion(UUID.randomUUID(), idPublicacion, ruta, orden, portada);
    }
}
