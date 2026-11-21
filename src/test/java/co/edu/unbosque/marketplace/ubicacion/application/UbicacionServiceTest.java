package co.edu.unbosque.marketplace.ubicacion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import co.edu.unbosque.marketplace.ubicacion.application.port.CatalogoUbicacionesExterno;
import co.edu.unbosque.marketplace.ubicacion.application.port.CatalogoUbicacionesExterno.PaisExterno;
import co.edu.unbosque.marketplace.ubicacion.application.port.CiudadRepository;
import co.edu.unbosque.marketplace.ubicacion.application.port.PaisRepository;
import co.edu.unbosque.marketplace.ubicacion.contracts.CiudadUbicacionInfo;
import co.edu.unbosque.marketplace.ubicacion.domain.Ciudad;
import co.edu.unbosque.marketplace.ubicacion.domain.Pais;

class UbicacionServiceTest {

    private final PaisRepository paises = mock(PaisRepository.class);
    private final CiudadRepository ciudades = mock(CiudadRepository.class);
    private final CatalogoUbicacionesExterno cliente = mock(CatalogoUbicacionesExterno.class);
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

    @Test
    void sincronizaLosPaisesDelCatalogoExternoSoloLaPrimeraVez() {
        Pais existente = new Pais(UUID.randomUUID(), "CO", "Colombia");
        when(paises.count()).thenReturn(0L);
        when(cliente.paises()).thenReturn(List.of(
                new PaisExterno("Colombia", "CO"), new PaisExterno("Perú", "PE")));
        when(paises.findByCodigoIso2IgnoreCase("CO")).thenReturn(Optional.of(existente));
        when(paises.findByCodigoIso2IgnoreCase("PE")).thenReturn(Optional.empty());
        when(paises.findAllByOrderByNombreAsc()).thenReturn(List.of(existente));

        assertEquals(1, servicio.listarPaises().size());

        ArgumentCaptor<List<Pais>> guardados = listaCaptor();
        verify(paises).saveAll(guardados.capture());
        assertEquals(1, guardados.getValue().size());
        assertEquals("PE", guardados.getValue().get(0).getCodigoIso2());
        assertEquals("Perú", guardados.getValue().get(0).getNombre());
    }

    @Test
    void sincronizaLasCiudadesSinRepetirLasQueSoloDifierenEnTildes() {
        Pais peru = new Pais(UUID.randomUUID(), "PE", "Perú");
        when(paises.findById(peru.getIdPais())).thenReturn(Optional.of(peru));
        when(ciudades.existsByPaisIdPais(peru.getIdPais())).thenReturn(false);
        when(cliente.ciudades("Perú")).thenReturn(List.of("Cusco", "Lima", "LIMA", "Límá"));

        servicio.listarCiudades(peru.getIdPais());

        ArgumentCaptor<List<Ciudad>> guardadas = listaCaptor();
        verify(ciudades).saveAll(guardadas.capture());
        assertEquals(List.of("Cusco", "Lima"), guardadas.getValue().stream().map(Ciudad::getNombre).toList());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> ArgumentCaptor<List<T>> listaCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(List.class);
    }
}
