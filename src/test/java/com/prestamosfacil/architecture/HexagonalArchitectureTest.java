package com.prestamosfacil.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HexagonalArchitectureTest {

    private final JavaClasses applicationClasses = new ClassFileImporter()
            .importPackages("com.prestamosfacil");

    @Test
    void domainMustNotDependOnSpringJpaOrInfrastructure() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "..infrastructure..")
                .check(applicationClasses);
    }

    @Test
    void applicationMustNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure..")
                .check(applicationClasses);
    }
}

