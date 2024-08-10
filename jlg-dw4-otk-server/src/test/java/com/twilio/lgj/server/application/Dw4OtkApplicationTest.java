package com.twilio.lgj.server.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.twilio.coreutil.service.dw40.application.DW40ApplicationAdapter;
import com.twilio.coreutil.service.jakarta.api.application.Application;
import com.twilio.json.SidJsonModule;
import com.twilio.lgj.server.configuration.Dw4OtkConfiguration;
import com.twilio.lgj.server.definitions.HelloMessage;
import com.twilio.lgj.server.definitions.HelloMessageList;
import com.twilio.lgj.server.services.ServiceRepositoryImpl;
import com.twilio.rest.TwilioHeaders;
import com.twilio.sids.SmsSid;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
public class Dw4OtkApplicationTest {
    private static final int ADMIN_PORT = Integer.parseInt(System.getenv("ADMIN_PORT"));
    private static final int APP_PORT = Integer.parseInt(System.getenv("APP_PORT"));
    private final ObjectMapper mapper =
            new ObjectMapper().registerModule(new SidJsonModule()).registerModule(new JavaTimeModule());
    private String baseUrl;

    public static DropwizardAppExtension<Dw4OtkConfiguration> dropwizardAppRule =
            new DropwizardAppExtension<>(
                    new DropwizardTestSupport<>(
                            DW40App.class,
                            ResourceHelpers.resourceFilePath("service.yaml"),
                            ConfigOverride.config("localTest", "true")) {
                        public io.dropwizard.core.Application<Dw4OtkConfiguration> newApplication() {
                            return new DW40App(new Dw4OtkApplication());
                        }
                    });

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + APP_PORT;
    }

    @Test
    void validateServerRuns() throws IOException {
        final URL url = new URL("http://localhost:" + ADMIN_PORT);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        final int responseCode = connection.getResponseCode();
        assertThat("No response received.", responseCode, is(200));
    }

    @Test
    void listMessages() throws IOException {
        final var uri = baseUrl + "/v1/Service/Messages/";
        final var connection = createConnection(uri, "GET");
        final var responseCode = connection.getResponseCode();

        assertEquals(200, responseCode);
        String text = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        final var list = mapper.readValue(text, HelloMessageList.class);
        assertNotNull(list);
        assertEquals("Bob", list.getHelloMessages().get(0).getHelloMessage().getFrom());
    }

    @Test
    void createMessage() throws IOException {
        final var uri = baseUrl + "/v1/Service/Messages/" + new SmsSid();
        final var connection = createConnection(uri, "POST");

        final var helloMessage = new HelloMessage("apa", "ko", "Hello little apa");
        connection.setDoOutput(true);
        connection.setRequestProperty("content-type", "application/json");
        try (final var os = connection.getOutputStream()) {
            mapper.writeValue(os, helloMessage);
        }
        assertEquals(201, connection.getResponseCode());
    }

    @Test
    void validateServerAdminEndpoint() throws IOException {
        final URL url = new URL("http://localhost:" + APP_PORT + "/v1/Admin/ReadMessages");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        final int responseCode = connection.getResponseCode();
        assertThat("No response received.", responseCode, is(400));
    }

    private static HttpURLConnection createConnection(final String uri, final String method)
            throws IOException {
        final var url = new URL(uri);
        final var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty(
                TwilioHeaders.AUTH_ACCOUNT_HEADER_KEY, ServiceRepositoryImpl.DUMMY_ACCOUNT_SID.getValue());
        connection.setRequestMethod(method);
        return connection;
    }

    public static class DW40App extends DW40ApplicationAdapter<Dw4OtkConfiguration> {
        public DW40App(final Application<Dw4OtkConfiguration> application) {
            super(application);
        }
    }
}
