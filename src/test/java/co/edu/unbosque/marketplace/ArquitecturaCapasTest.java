package co.edu.unbosque.marketplace;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaCall;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.properties.HasOwner;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * Vigila las capas dentro de cada módulo (api, application, domain, infrastructure).
 * ArquitecturaModularTest (Spring Modulith) ya revisa las fronteras entre módulos;
 * estas reglas revisan que las dependencias dentro de un módulo apunten hacia el dominio.
 */
class ArquitecturaCapasTest {

    private static final String BASE = "co.edu.unbosque.marketplace";

    private static final String INFRAESTRUCTURA = "..infrastructure..";

    private static JavaClasses clases;

    @BeforeAll
    static void importarClases() {
        clases = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);
    }

    @Test
    void elDominioNoDependeDeLasDemasCapas() {
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..api..", "..application..", INFRAESTRUCTURA)
                .because("el dominio es el centro: no conoce la presentación, la aplicación ni la infraestructura")
                .check(clases);
    }

    @Test
    void laAplicacionNoDependeDeLaPresentacion() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..api..")
                .because("la capa de aplicación no debe conocer los controladores ni sus clases")
                .check(clases);
    }

    @Test
    void laAplicacionNoDependeDeLaInfraestructura() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(INFRAESTRUCTURA)
                .because("la aplicación usa puertos (interfaces); los adaptadores viven en infraestructura")
                .check(clases);
    }

    @Test
    void laInfraestructuraNoDependeDeLaPresentacion() {
        noClasses().that().resideInAnyPackage(INFRAESTRUCTURA)
                .should().dependOnClassesThat().resideInAPackage("..api..")
                .because("la infraestructura no debe devolver ni conocer las clases de los controladores")
                .check(clases);
    }

    @Test
    void laPresentacionNoDependeDeLaInfraestructura() {
        noClasses().that().resideInAPackage("..api..")
                .should().dependOnClassesThat().resideInAnyPackage(INFRAESTRUCTURA)
                .because("los controladores hablan con la aplicación, no con repositorios ni adaptadores")
                .check(clases);
    }

    /**
     * La capa de aplicación y la de presentación lanzan excepciones de dominio (compartido.domain);
     * ErroresApiHandler las traduce a HTTP.
     */
    @Test
    void laAplicacionYLaPresentacionLanzanExcepcionesDeDominio() {
        noClasses().that().resideInAnyPackage("..application..", "..api..")
                .should().callConstructorWhere(JavaCall.Predicates.target(
                        HasOwner.Predicates.With.owner(assignableTo(IllegalArgumentException.class)
                                .or(assignableTo(IllegalStateException.class)))))
                .because("se lanza una excepción de dominio (compartido.domain) para que el HTTP lo decida ErroresApiHandler")
                .check(clases);
    }

    /**
     * Los repositorios de la aplicación son interfaces puras (application/port); la parte de Spring Data
     * (JpaRepository, @Query, @Lock) vive en infrastructure/persistence. Se permiten los tipos de valor
     * org.springframework.data.domain (Sort, Pageable).
     */
    @Test
    void laAplicacionNoDependeDeSpringDataJpa() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework.data.jpa..", "org.springframework.data.repository..")
                .because("los repositorios son puertos puros; Spring Data se implementa en infraestructura")
                .check(clases);
    }

    @Test
    void laAplicacionNoDependeDeRedisNiDelAdministradorDeEntidades() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework.data.redis..", "jakarta.persistence..")
                .because("Redis y JPA se usan detrás de un puerto implementado en infraestructura")
                .check(clases);
    }

    @Test
    void laAplicacionNoUsaClientesHttp() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("java.net.http..")
                .because("las integraciones externas (Stripe, etc.) son adaptadores de infraestructura detrás de un contrato")
                .check(clases);
    }

    @Test
    void laAplicacionNoUsaJdbcDirectamente() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.jdbc..", "java.sql..")
                .because("el SQL va en repositorios de infraestructura detrás de un puerto")
                .check(clases);
    }
}
