package com.twilio.lgj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.core.client.base.exceptions.ServiceResponseException;
import com.twilio.core.request.context.RequestContext;
import com.twilio.core.request.context.impl.DefaultRequestContext;
import com.twilio.lgj.client.api.hello.message.HelloMessageClient;
import com.twilio.lgj.client.api.hello.message.HelloMessageClient.CreateMessageResponseHeaders;
import com.twilio.lgj.client.definitions.HelloMessage.HelloMessageBuilder;
import com.twilio.lgj.client.definitions.HelloMessageContainer;
import com.twilio.lgj.server.application.Dw4OtkApplication;
import com.twilio.lgj.server.application.Dw4OtkApplicationTest;
import com.twilio.lgj.server.configuration.Dw4OtkConfiguration;
import com.twilio.platform.metrics.TwilioMetricRegistryImpl;
import com.twilio.rest.TwilioHeaders;
import com.twilio.sids.AccountSid;
import com.twilio.sids.SmsSid;
import com.twilio.test.containers.CpsMonitorHelper;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Integration test. Creates a client from the blueprint schema and calls the server, started as a
 * dropwizard local application.
 */
@ExtendWith(DropwizardExtensionsSupport.class)
public class IntegrationTest {
    private static final int APP_PORT = Integer.parseInt(System.getenv("APP_PORT"));

    private static final RequestContext REQUEST_CONTEXT = new DefaultRequestContext.Builder().build();
    public static DropwizardAppExtension<Dw4OtkConfiguration> DROPWIZARD = setupTests();

    public static DropwizardAppExtension<Dw4OtkConfiguration> setupTests() {
        final var instance = new CpsMonitorHelper();
        instance.start("cpsmonitor/local.env");
        // it's important that we get the address of the container host to initialize
        // rateLimitConfiguration.cpsMonitor.hostname configuration correctly
        final var cpsHost = instance.getContainerHost();
        final var cpsPort = instance.getAppPort();

        DROPWIZARD =
                new DropwizardAppExtension<>(
                        new DropwizardTestSupport<>(
                                Dw4OtkApplicationTest.DW40App.class,
                                ResourceHelpers.resourceFilePath("service.yaml"),
                                ConfigOverride.config("localTest", "true"),
                                ConfigOverride.config("rateLimitConfiguration.cpsMonitor.hostname", cpsHost),
                                ConfigOverride.config(
                                        "rateLimitConfiguration.cpsMonitor.port", String.valueOf(cpsPort))) {
                            public io.dropwizard.core.Application<Dw4OtkConfiguration>
                                    newApplication() {
                                return new Dw4OtkApplicationTest.DW40App(
                                        new Dw4OtkApplication());
                            }
                        });
        return DROPWIZARD;
    }

    private HelloMessageClient client;

    @BeforeEach
    void setUp() {
        Dw4OtkConfiguration configuration = DROPWIZARD.getTestSupport().getConfiguration();
        final var helloMessageConfig = configuration.getClients().ensureConfiguration("hello_message");

        client =
                new HelloMessageClient.Builder(helloMessageConfig)
                        .metricsRegistry(new TwilioMetricRegistryImpl(DROPWIZARD.getEnvironment().metrics()))
                        .baseUrl(URI.create("http://localhost:" + APP_PORT + "/v1"))
                        .objectMapper(new ObjectMapper())
                        .build();
    }

    @Test
    void postAMessageAndReadItBack() {
        final var sid = SmsSid.parse("SM12345678901234567890123456789012");
        final var accountSid = AccountSid.parse("ACdeadbeefdeadbeefdeadbeefdeadbeef");

        // Send a message from Bob to Jeff
        final var helloMessage =
                new HelloMessageBuilder().from("Bob").to("Jeff").message("Hi Jeff").build();

        // Check that the result is a MessageContainer with the same sid
        final var responseHeaders = new CreateMessageResponseHeaders();
        final var helloMessageContainer =
                client
                        .createMessage(helloMessage, sid, "One", responseHeaders, REQUEST_CONTEXT)
                        .header(TwilioHeaders.AUTH_ACCOUNT_HEADER_KEY, accountSid.getValue())
                        .call();

        assertNotNull(helloMessageContainer);
        assertEquals(sid, helloMessageContainer.getSid());

        assertEquals("Hello World: One", responseHeaders.getEtag().orElse(null));
        final var headers = new HelloMessageClient.ListMessagesResponseHeaders();
        var listMessage =
                client
                        .listMessages("Bob", null, null, null, headers, REQUEST_CONTEXT)
                        .header(TwilioHeaders.AUTH_ACCOUNT_HEADER_KEY, accountSid.getValue())
                        .call();

        // Retrieve a list again, verify there is a message from Jeff
        assertNotNull(listMessage);
        assertTrue(
                listMessage.getHelloMessages().stream()
                        .anyMatch(m -> m.getHelloMessage().getTo().equals("Jeff")));
    }

    @Test
    void createMessageOverLimit() {
        final var accountSid = AccountSid.parse("ACdeadbeefdeadbeefdeadbeefdeadbeef");

        for (int i = 0; i < 100; i++) {
            // Send a message from Alice to Jeff
            final var sid = new SmsSid();
            final String messageString = "Hi Jeff: " + i;
            final var message =
                    new HelloMessageBuilder().from("Alice").to("Jeff").message(messageString).build();

            final var headers = new CreateMessageResponseHeaders();

            HelloMessageContainer hc;
            try {
                hc =
                        client
                                .createMessage(message, sid, Integer.toString(i), headers, REQUEST_CONTEXT)
                                .header(TwilioHeaders.AUTH_ACCOUNT_HEADER_KEY, accountSid.getValue())
                                .call();
                assertNotNull(hc);
                assertEquals(sid, hc.getSid());
                assertEquals(messageString, hc.getHelloMessage().getMessage());
            } catch (final ServiceResponseException e) {
                assertEquals(429, e.getResponse().getStatusCode());
                assertEquals(0L, (long) headers.getXRateLimitRemaining().orElse(-1));
                return;
            }

            assertEquals("Hello World: " + i, headers.etag);
            assertEquals(messageString, hc.getHelloMessage().getMessage());
        }

        // if we got here it means that the rate limit was not enforced
        fail("Rate limit was not enforced");
    }
}
