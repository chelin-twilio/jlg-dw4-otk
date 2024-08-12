package com.twilio.lgj.containers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.core.request.context.impl.DefaultRequestContext;
import com.twilio.core.request.context.impl.DefaultRequestContext.Builder;
import com.twilio.lgj.client.api.hello.message.HelloMessageClient;
import com.twilio.lgj.client.api.hello.message.HelloMessageClient.ConditionalRequest;
import com.twilio.lgj.client.api.hello.message.HelloMessageClient.CreateMessageResponseHeaders;
import com.twilio.lgj.client.api.hello.message.HelloMessageClient.GetMessageResponseHeaders;
import com.twilio.lgj.client.definitions.HelloMessage.HelloMessageBuilder;
import com.twilio.platform.metrics.noop.NoopTwilioMetricRegistry;
import com.twilio.rest.TwilioHeaders;
import com.twilio.sids.AccountSid;
import com.twilio.sids.SmsSid;
import com.twilio.test.containers.ContainerComposeHelper;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIf(
        value = "com.twilio.test.containers.ContainerComposeHelper#composeFileExists",
        disabledReason = "docker-compose.yml file not found")
@EnabledIfSystemProperty(named = "container.test", matches = "true")
class ContainerTestIT {

    private static final ContainerComposeHelper CONTAINER_HELPER = new ContainerComposeHelper();

    private HelloMessageClient client;
    private int times;

    @BeforeEach
    void setup() throws IOException {
        //    CONTAINER_HELPER.start("containers/compose.properties");
        times = Integer.parseInt(System.getProperty("times", "1000"));

        client =
                new HelloMessageClient.Builder(
                                CONTAINER_HELPER.readClientConfiguration("client-configuration.yaml"))
                        .metricsRegistry(new NoopTwilioMetricRegistry())
                        .baseUrl(URI.create("http://localhost:51353/v1"))
                        //            .baseUrl(URI.create(CONTAINER_HELPER.getBaseURL("v1")))
                        .objectMapper(new ObjectMapper().findAndRegisterModules())
                        .build();
    }

    @AfterEach
    void tearDown() {
        //    CONTAINER_HELPER.stop();
    }

    @Test
    void simpleRequest() {
        final var sid = SmsSid.parse("SM12345678901234567890123456789012");
        final var accountSid = AccountSid.parse("ACdeadbeefdeadbeefdeadbeefdeadbeef");

        // Send a message from Bob to Jeff
        final var helloMessage =
                new HelloMessageBuilder().from("Bob").to("Jeff").message("Hi Jeff").build();

        final var responseHeaders = new CreateMessageResponseHeaders();
        final var hc =
                client
                        .createMessage(
                                helloMessage,
                                sid,
                                "One",
                                responseHeaders,
                                new DefaultRequestContext.Builder().build())
                        .header(TwilioHeaders.AUTH_ACCOUNT_HEADER_KEY, accountSid.getValue())
                        .call();

        // Check that the result is a MessageContainer with the same sid
        assertNotNull(hc);
        assertEquals(sid, hc.getSid());
        assertEquals("Hello World: One", responseHeaders.getEtag().orElse(null));
    }

    @Test
    void repeat() {
        final var sid = SmsSid.parse("SM12345678901234567890123456789012");
        final var accountSid = AccountSid.parse("ACdeadbeefdeadbeefdeadbeefdeadbeef");

        // Send a message from Bob to Jeff
        final var helloMessage =
                new HelloMessageBuilder().from("Bob").to("Jeff").message("Hi Jeff").build();

        final var responseHeaders = new CreateMessageResponseHeaders();
        final var start = Instant.now();
        for (int i = 0; i < times; i++) {
            final var iter = Instant.now();
            final var callBuilder =
                    client
                            .createMessage(
                                    helloMessage,
                                    sid,
                                    "One",
                                    responseHeaders,
                                    new DefaultRequestContext.Builder().build())
                            .header(TwilioHeaders.AUTH_ACCOUNT_HEADER_KEY, accountSid.getValue());

            final var hc = callBuilder.call();
            System.out.printf("NJ %d%n", Duration.between(iter, Instant.now()).toNanos());
            assertNotNull(hc);
            assertEquals(sid, hc.getSid());
            assertEquals("Hello World: One", responseHeaders.getEtag().orElse(null));
        }

        final var getMessageResponseHeaders = new GetMessageResponseHeaders();
        final var conditional = new ConditionalRequest(null, null, null, null, null);

        for (int i = 0; i < times; i++) {
            final var iter = Instant.now();
            final var hc =
                    client
                            .getMessage(sid, getMessageResponseHeaders, conditional, new Builder().build())
                            .header(TwilioHeaders.AUTH_ACCOUNT_HEADER_KEY, accountSid.getValue())
                            .call();
            System.out.printf("NJ %d%n", Duration.between(iter, Instant.now()).toNanos());
            assertNotNull(hc);
            assertEquals(sid, hc.getSid());
            assertEquals("Hello World", getMessageResponseHeaders.getEtag().orElse(null));
        }
        final var duration = java.time.Duration.between(start, Instant.now());
        System.out.println("Took " + duration.toMillis() + "ms");
    }
}
