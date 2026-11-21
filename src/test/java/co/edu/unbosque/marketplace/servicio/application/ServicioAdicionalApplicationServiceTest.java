package co.edu.unbosque.marketplace.servicio.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.servicio.application.port.ServicioAdicionalRepository;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalData;

@ExtendWith(MockitoExtension.class)
class ServicioAdicionalApplicationServiceTest {

    @Mock
    private ServicioAdicionalRepository repository;

    @InjectMocks
    private ServicioAdicionalApplicationService service;

    private final UUID idPublicacion = UUID.randomUUID();

    private void rechaza(UUID id, List<ServicioAdicionalData> servicios) {
        assertThrows(DatosInvalidosException.class, () -> service.reemplazarActivosPorPublicacion(id, servicios));
        verify(repository, never()).saveAll(any());
    }

    @Test
    void rechazaPublicacionOListaNulas() {
        rechaza(null, List.of());
        rechaza(idPublicacion, null);
    }

    @Test
    void rechazaNombresInvalidos() {
        rechaza(idPublicacion, Arrays.asList((ServicioAdicionalData) null));
        rechaza(idPublicacion, List.of(new ServicioAdicionalData(null, null, BigDecimal.ONE)));
        rechaza(idPublicacion, List.of(new ServicioAdicionalData("   ", null, BigDecimal.ONE)));
        rechaza(idPublicacion, List.of(new ServicioAdicionalData("x".repeat(101), null, BigDecimal.ONE)));
    }

    @Test
    void rechazaCostosInvalidos() {
        rechaza(idPublicacion, List.of(new ServicioAdicionalData("Desayuno", null, null)));
        rechaza(idPublicacion, List.of(new ServicioAdicionalData("Desayuno", null, new BigDecimal("-1"))));
    }

    @Test
    void rechazaNombresDuplicadosSinImportarMayusculas() {
        rechaza(idPublicacion, List.of(
                new ServicioAdicionalData("Desayuno", null, BigDecimal.ONE),
                new ServicioAdicionalData(" desayuno ", null, BigDecimal.TEN)));
    }
}
