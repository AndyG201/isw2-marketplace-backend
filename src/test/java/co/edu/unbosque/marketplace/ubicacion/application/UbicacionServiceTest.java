package co.edu.unbosque.marketplace.ubicacion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.ubicacion.application.port.CiudadRepository;
import co.edu.unbosque.marketplace.ubicacion.application.port.PaisRepository;
import co.edu.unbosque.marketplace.ubicacion.contracts.CiudadUbicacionInfo;
import co.edu.unbosque.marketplace.ubicacion.domain.Ciudad;
import co.edu.unbosque.marketplace.ubicacion.domain.Pais;

class UbicacionServiceTest {

    private final PaisRepository paises = mock(PaisRepository.class);
    private final CiudadRepository ciudades = mock(CiudadRepository.class);
    private final CountriesNowClient cliente = mock(CountriesNowClient.class);
    private final UbicacionService servicio = new UbicacionService(paises, ciudades, cliente);

    @Test
    void devuelveLaCiudadConSuPais() {
        UUID id = UUID.randomUUID();
        Pais colombia = new Pais(UUID.randomUUID(), "CO", "Colombia");
        when(ciudades.findById(id)).thenReturn(Optional.of(new Ciudad(id, colombia, "Medellín")));

        assertEquals(Optional.of(new CiudadUbicacionInfo("Medellín", "Colombia", "CO")),
                servicio.consultarCiudad(id));
    }

    @Test
    void sinCiudadNoConsulta() {
        assertTrue(servicio.consultarCiudad(null).isEmpty());
        verifyNoInteractions(ciudades);

        UUID id = UUID.randomUUID();
        when(ciudades.findById(id)).thenReturn(Optional.empty());
        assertTrue(servicio.consultarCiudad(id).isEmpty());
    }
}
