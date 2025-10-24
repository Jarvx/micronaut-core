package io.micronaut.reproduce

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Reproducer test: inject a parameterized GenericRepository<Foo> while multiple GenericRepository
 * implementations (including a generic implementation) are present in the context.
 * The original issue surfaced during test metadata resolution (Micronaut JUnit5 extension)
 * when trying to pick a concrete candidate for a parameterized injection point.
 */
@MicronautTest
class ParameterizedInjectionMicronautTest {

    @Inject
    lateinit var repo: GenericRepository<Foo>

    @Test
    fun testGenericRepositoryInjection() {
        assertNotNull(repo, "Repository should have been injected")
        // Ensure the concrete typed implementation is chosen for GenericRepository<Foo>
        assertTrue(repo is FooRepository, "Expected FooRepository to be injected for GenericRepository<Foo>)")
    }
}
