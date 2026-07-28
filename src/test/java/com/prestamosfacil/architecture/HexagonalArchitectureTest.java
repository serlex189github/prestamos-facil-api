package com.prestamosfacil.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HexagonalArchitectureTest {

    private static JavaClasses todasLasClases;

    @BeforeAll
    static void importarClases() {
        // Se importa una sola vez para toda la clase de test, no en cada
        // método: escanear el classpath en cada @Test es costoso e innecesario.
        todasLasClases = new ClassFileImporter()
            .importPackages("com.prestamosfacil");
    }

    // ---------- DOMAIN: NO DEBE CONOCER NADA POR FUERA DE SÍ MISMO ----------

    @Test
    void elDominioNoDebeDependerDeSpring() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..")
            .check(todasLasClases);
    }

    @Test
    void elDominioNoDebeDependerDeJakartaPersistence() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("jakarta.persistence..")
            .check(todasLasClases);
    }

    @Test
    void elDominioNoDebeDependerDeInfraestructura() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .check(todasLasClases);
    }

    @Test
    void elDominioNoDebeDependerDeLaCapaDeAplicacion() {
        // Regla que faltaba en la versión original: en arquitectura hexagonal
        // la dependencia va de afuera hacia adentro (application -> domain),
        // nunca al revés.
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..application..")
            .check(todasLasClases);
    }

    // ---------- APPLICATION: SOLO DEBE CONOCER PUERTOS, NO ADAPTADORES ----------

    @Test
    void laAplicacionNoDebeDependerDeInfraestructura() {
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .check(todasLasClases);
    }

    @Test
    void laAplicacionNoDebeDependerDeJakartaPersistence() {
        // Los DTOs y casos de uso de application no deben acoplarse a
        // anotaciones de persistencia; eso es responsabilidad exclusiva
        // de los adaptadores en infrastructure.
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("jakarta.persistence..")
            .check(todasLasClases);
    }
}
