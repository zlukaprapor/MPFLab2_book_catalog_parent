package com.bookapp;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class ArchitectureTest {
    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter().importPackages("com.bookapp");
    }

    @Test
    void webShouldNotDependOnPersistence() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..web..")
                .should().dependOnClassesThat().resideInAPackage("..persistence..");

        rule.check(classes);
    }

    @Test
    void coreShouldNotDependOnServlet() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..core..")
                .should().dependOnClassesThat().resideInAPackage("javax.servlet..");

        rule.check(classes);
    }

    @Test
    void coreShouldNotDependOnJdbc() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..core..")
                .should().dependOnClassesThat().resideInAnyPackage("java.sql..", "javax.sql..");

        rule.check(classes);
    }

    @Test
    void controllersShouldOnlyBeInWeb() {
        ArchRule rule = noClasses()
                .that().haveSimpleNameEndingWith("Controller")
                .should().resideOutsideOfPackage("..web..");

        rule.check(classes);
    }

    @Test
    void repositoriesShouldOnlyBeInPersistence() {
        ArchRule rule = noClasses()
                .that().haveSimpleNameEndingWith("Repository")
                .should().resideOutsideOfPackage("..persistence..");

        rule.check(classes);
    }

    @Test
    void layeredArchitectureShouldBeRespected() {
        ArchRule rule = layeredArchitecture()
                .consideringAllDependencies()
                .layer("Web").definedBy("..web..")
                .layer("Core").definedBy("..core..")
                .layer("Persistence").definedBy("..persistence..")

                .whereLayer("Web").mayNotBeAccessedByAnyLayer()
                .whereLayer("Core").mayOnlyBeAccessedByLayers("Web", "Persistence")
                .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Web");

        rule.check(classes);
    }
}