package co.edu.unbosque.marketplace.usuario.infrastructure.persistence;

import java.util.UUID;

import co.edu.unbosque.marketplace.usuario.domain.Rol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RolRepositoryTest {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID idRol;

    @BeforeEach
    void configurar() {
        idRol = UUID.randomUUID();

        Rol rol = new Rol(
            idRol,
            "HUESPED",
            "Usuario que puede realizar reservas."
        );

        entityManager.persistAndFlush(rol);
        entityManager.clear();
    }

    @ParameterizedTest
    @ValueSource(strings = {"HUESPED", "huesped", "HuEsPeD"})
    void encuentraRolSinDistinguirMayusculas(String nombre) {
        var resultado = rolRepository.findByNombreIgnoreCase(nombre);

        assertTrue(resultado.isPresent());

        Rol rol = resultado.orElseThrow();

        assertAll(
            () -> assertEquals(idRol, rol.getIdRol()),
            () -> assertEquals("HUESPED", rol.getNombre()),
            () -> assertEquals(
                "Usuario que puede realizar reservas.",
                rol.getDescripcion()
            )
        );
    }

    @Test
    void buscarRolInexistenteRetornaOptionalVacio() {
        var resultado =
                rolRepository.findByNombreIgnoreCase("ROL_INEXISTENTE");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarNombreVacioNoEncuentraRol() {
        assertTrue(
            rolRepository.findByNombreIgnoreCase("").isEmpty()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"HUESPED", "huesped", "HuEsPeD"})
    void existeRolSinDistinguirMayusculas(String nombre) {
        assertTrue(rolRepository.existsByNombreIgnoreCase(nombre));
    }

    @Test
    void rolInexistenteRetornaFalse() {
        assertFalse(
            rolRepository.existsByNombreIgnoreCase("ROL_INEXISTENTE")
        );
    }

    @Test
    void nombreVacioRetornaFalse() {
        assertFalse(rolRepository.existsByNombreIgnoreCase(""));
    }
}