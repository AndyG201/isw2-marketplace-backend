package co.edu.unbosque.marketplace.disponibilidad.application.port;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;
import co.edu.unbosque.marketplace.disponibilidad.domain.PeriodoDisponibilidad;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Rol;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class PeriodoDisponibilidadRepositoryTest {

    @Autowired
    private PeriodoDisponibilidadRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID idAnfitrion;
    private UUID idAlojamiento;
    private UUID idOtroAlojamiento;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @BeforeEach
    void setUp() {

        fechaInicio = LocalDate.of(2026, 11, 20);
        fechaFin = LocalDate.of(2026, 11, 30);

        // 1. Crear y persistir el rol.
        Rol rol = new Rol(
                UUID.randomUUID(),
                "ANFITRION",
                "Rol de anfitrión para pruebas de disponibilidad"
        );

        entityManager.persist(rol);

        // 2. Crear y persistir el usuario anfitrión.
        Usuario anfitrion = crearAnfitrion(rol);

        entityManager.persist(anfitrion);

        idAnfitrion = anfitrion.getIdUsuario();

        // 3. Crear los alojamientos asociados al anfitrión existente.
        idAlojamiento = UUID.randomUUID();
        idOtroAlojamiento = UUID.randomUUID();

        Alojamiento alojamiento = crearAlojamiento(
                idAlojamiento,
                "Calle 100 #15-20",
                "Apartamento"
        );

        Alojamiento otroAlojamiento = crearAlojamiento(
                idOtroAlojamiento,
                "Carrera 7 #20-30",
                "Casa"
        );

        entityManager.persist(alojamiento);
        entityManager.persist(otroAlojamiento);

        entityManager.flush();
        entityManager.clear();
    }

    private Usuario crearAnfitrion(Rol rol) {

        Usuario usuario = new Usuario();

        usuario.setIdUsuario(UUID.randomUUID());
        usuario.setRol(rol);
        usuario.setNombre("Anfitrion");
        usuario.setApellido("Pruebas");
        usuario.setCorreo(
                "anfitrion." + UUID.randomUUID() + "@test.com"
        );

        usuario.setTipoDocumento("CEDULA");
        usuario.setDocumentoCifrado(new byte[16]);
        usuario.setDocumentoNonce(new byte[12]);

        byte[] hash = new byte[32];
        hash[0] = 1;
        usuario.setDocumentoHash(hash);

        usuario.setTelefono(
                "3" + String.format(
                        "%09d",
                        Math.abs(
                                UUID.randomUUID().getMostSignificantBits()
                                        % 1_000_000_000L
                        )
                )
        );

        usuario.setContrasenaHash("hash-de-prueba");
        usuario.setCorreoVerificado(true);
        usuario.setEstado(EstadoUsuario.ACTIVA);
        usuario.setRestringido(false);
        usuario.setIntentosFallidos(0);

        OffsetDateTime ahora = OffsetDateTime.now();

        usuario.setFechaRegistro(ahora);
        usuario.setFechaActualizacion(ahora);

        return usuario;
    }

    private Alojamiento crearAlojamiento(
            UUID id,
            String ubicacion,
            String tipo) {

        return new Alojamiento(
                id,
                idAnfitrion,
                ubicacion,
                "Bogota",
                tipo,
                4,
                OffsetDateTime.now()
        );
    }

    private PeriodoDisponibilidad crearPeriodo(
            UUID idAlojamiento,
            LocalDate inicio,
            LocalDate fin,
            boolean habilitado) {

        PeriodoDisponibilidad periodo =
                new PeriodoDisponibilidad(
                        UUID.randomUUID(),
                        idAlojamiento,
                        inicio,
                        fin,
                        habilitado,
                        OffsetDateTime.now()
                );

        return repository.saveAndFlush(periodo);
    }

    @Test
    void buscarPorAlojamientoOrdenadoPorFechaInicio() {

        crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15),
                true
        );

        crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 25),
                true
        );

        crearPeriodo(
                idOtroAlojamiento,
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 18),
                true
        );

        List<PeriodoDisponibilidad> resultado =
                repository.findByIdAlojamientoOrderByFechaInicioAsc(
                        idAlojamiento
                );

        assertEquals(2, resultado.size());

        assertTrue(
                resultado.get(0).getFechaInicio()
                        .isBefore(resultado.get(1).getFechaInicio())
        );
    }

    @Test
    void buscarPorAlojamientoSinResultados() {

        List<PeriodoDisponibilidad> resultado =
                repository.findByIdAlojamientoOrderByFechaInicioAsc(
                        idAlojamiento
                );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorIdPeriodoYIdAlojamiento() {

        PeriodoDisponibilidad periodo = crearPeriodo(
                idAlojamiento,
                fechaInicio,
                fechaFin,
                true
        );

        Optional<PeriodoDisponibilidad> resultado =
                repository.findByIdPeriodoAndIdAlojamiento(
                        periodo.getIdPeriodo(),
                        idAlojamiento
                );

        assertTrue(resultado.isPresent());

        assertEquals(
                periodo.getIdPeriodo(),
                resultado.get().getIdPeriodo()
        );
    }

    @Test
    void buscarPorIdPeriodoYAlojamientoIncorrecto() {

        PeriodoDisponibilidad periodo = crearPeriodo(
                idAlojamiento,
                fechaInicio,
                fechaFin,
                true
        );

        Optional<PeriodoDisponibilidad> resultado =
                repository.findByIdPeriodoAndIdAlojamiento(
                        periodo.getIdPeriodo(),
                        idOtroAlojamiento
                );

        assertFalse(resultado.isPresent());
    }

    @Test
    void existeSolapamientoHabilitado() {

        crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 30),
                true
        );

        boolean resultado =
                repository.existeSolapamientoHabilitado(
                        idAlojamiento,
                        LocalDate.of(2026, 11, 25),
                        LocalDate.of(2026, 12, 5)
                );

        assertTrue(resultado);
    }

    @Test
    void noExisteSolapamientoHabilitado() {

        crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 30),
                true
        );

        boolean resultado =
                repository.existeSolapamientoHabilitado(
                        idAlojamiento,
                        LocalDate.of(2026, 12, 1),
                        LocalDate.of(2026, 12, 10)
                );

        assertFalse(resultado);
    }

    @Test
    void noExisteSolapamientoConPeriodoDeshabilitado() {

        crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 30),
                false
        );

        boolean resultado =
                repository.existeSolapamientoHabilitado(
                        idAlojamiento,
                        LocalDate.of(2026, 11, 25),
                        LocalDate.of(2026, 12, 5)
                );

        assertFalse(resultado);
    }

    @Test
    void noExisteSolapamientoEnOtroAlojamiento() {

        crearPeriodo(
                idOtroAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 30),
                true
        );

        boolean resultado =
                repository.existeSolapamientoHabilitado(
                        idAlojamiento,
                        LocalDate.of(2026, 11, 25),
                        LocalDate.of(2026, 12, 5)
                );

        assertFalse(resultado);
    }

    @Test
    void existeSolapamientoExcluyendoPeriodo() {

        crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 30),
                true
        );

        boolean resultado =
                repository.existeSolapamientoExcluyendoPeriodo(
                        idAlojamiento,
                        UUID.randomUUID(),
                        LocalDate.of(2026, 11, 25),
                        LocalDate.of(2026, 12, 5)
                );

        assertTrue(resultado);
    }

    @Test
    void noExisteSolapamientoAlExcluirElMismoPeriodo() {

        PeriodoDisponibilidad periodo = crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 30),
                true
        );

        boolean resultado =
                repository.existeSolapamientoExcluyendoPeriodo(
                        idAlojamiento,
                        periodo.getIdPeriodo(),
                        LocalDate.of(2026, 11, 25),
                        LocalDate.of(2026, 12, 5)
                );

        assertFalse(resultado);
    }

    @Test
    void noExisteSolapamientoExcluyendoPeriodoDeshabilitado() {

        crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 30),
                false
        );

        boolean resultado =
                repository.existeSolapamientoExcluyendoPeriodo(
                        idAlojamiento,
                        UUID.randomUUID(),
                        LocalDate.of(2026, 11, 25),
                        LocalDate.of(2026, 12, 5)
                );

        assertFalse(resultado);
    }
}
