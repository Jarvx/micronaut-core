package io.micronaut.reproduce

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Reproduces the expectation that an object parameter annotated with @QueryValue
 * is populated from individual query parameters, e.g. ?page=1&size=10 -> object.page=1, object.size=10
 */
class QueryValueObjectBindingTest {

    lateinit var server: EmbeddedServer
    lateinit var client: HttpClient

    @BeforeEach
    fun setup() {
        // restrict bean loading to this spec using spec.name so test is isolated
        server = ApplicationContext.run(mapOf("spec.name" to "QueryValueObjectBindingTest")).getBean(EmbeddedServer::class.java)
        server.start()
        client = server.applicationContext.createBean(HttpClient::class.java, server.url)
    }

    @AfterEach
    fun cleanup() {
        client.close()
        server.close()
    }

    @Test
    fun `object annotated with QueryValue should have its fields bound from query params`() {
        val request = HttpRequest.GET<Any>("/pagination?page=1&size=10")
        val body = client.toBlocking().retrieve(request)

        // Expect the returned JSON to contain both fields populated
        // This test will fail if Micronaut does not support binding an object parameter annotated with @QueryValue
        assertTrue(body.contains("\"page\""), "response should contain 'page' field: $body")
        assertTrue(body.contains("\"size\""), "response should contain 'size' field: $body")
        assertTrue(body.contains("1"), "response should contain page value 1: $body")
        assertTrue(body.contains("10"), "response should contain size value 10: $body")
    }
}

@Requires(property = "spec.name", value = "QueryValueObjectBindingTest")
@Controller("/pagination")
class PaginationController {
    // The behavior under test: annotating an object with @QueryValue
    // so that its properties are bound from query params.
    @Get
    fun list(@QueryValue pagination: JsPaginationRequest): JsPaginationRequest = pagination
}

@Requires(property = "spec.name", value = "QueryValueObjectBindingTest")
data class JsPaginationRequest(
    var page: Int? = null,
    var size: Int? = null
)
