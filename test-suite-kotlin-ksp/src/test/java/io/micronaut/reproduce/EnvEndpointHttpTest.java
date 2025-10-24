package io.micronaut.reproduce;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.HashMap;

/**
 * These tests exercise the environment endpoint over HTTP. They enable the env endpoint
 * and disable sensitivity so the endpoint can be invoked without authentication.
 *
 * The first test asserts the default response contains "packages" and "propertySources".
 * The second test sets endpoints.env.keys[0]=activeEnvironments and asserts only that key
 * appears in the returned payload. This demonstrates the desired behavior (and will fail
 * if the feature is not implemented), reproducing the reported issue.
 */
public class EnvEndpointHttpTest {

    private EmbeddedServer server;
    private HttpClient client;

    @AfterEach
    void cleanup() {
        if (client != null) {
            client.close();
        }
        if (server != null) {
            server.close();
        }
    }

    @Test
    public void defaultEnvEndpointContainsPackagesAndPropertySources() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("endpoints.env.enabled", true);
        // allow anonymous access for tests
        props.put("endpoints.env.sensitive", false);
        // run on random port
        props.put("micronaut.server.port", -1);

        server = ApplicationContext.run(EmbeddedServer.class, props);
        client = HttpClient.create(server.getURL());

        HttpRequest<?> req = HttpRequest.GET("/env");
        HttpResponse<String> rsp = client.toBlocking().exchange(req, String.class);
        Assertions.assertEquals(200, rsp.getStatus().getCode());
        String body = rsp.getBody().orElse("{}");

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(body, new TypeReference<Map<String, Object>>() {});

        Assertions.assertTrue(map.containsKey("packages"), "Expected 'packages' key in env payload");
        Assertions.assertTrue(map.containsKey("propertySources"), "Expected 'propertySources' key in env payload");
        Assertions.assertTrue(map.containsKey("activeEnvironments"), "Expected 'activeEnvironments' key in env payload");
    }

    @Test
    public void restrictedEnvEndpointOnlyActiveEnvironmentsWhenConfigured() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("endpoints.env.enabled", true);
        // allow anonymous access for tests
        props.put("endpoints.env.sensitive", false);
        // configure the keys to only include activeEnvironments
        props.put("endpoints.env.keys[0]", "activeEnvironments");
        props.put("micronaut.server.port", -1);

        server = ApplicationContext.run(EmbeddedServer.class, props);
        client = HttpClient.create(server.getURL());

        HttpRequest<?> req = HttpRequest.GET("/env");
        HttpResponse<String> rsp = client.toBlocking().exchange(req, String.class);
        Assertions.assertEquals(200, rsp.getStatus().getCode());
        String body = rsp.getBody().orElse("{}");

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(body, new TypeReference<Map<String, Object>>() {});

        // Expect only the configured key to be present
        Assertions.assertEquals(1, map.size(), "When endpoints.env.keys is set to only 'activeEnvironments' the env payload should contain only one key");
        Assertions.assertTrue(map.containsKey("activeEnvironments"), "Expected only 'activeEnvironments' key in env payload");
    }
}
