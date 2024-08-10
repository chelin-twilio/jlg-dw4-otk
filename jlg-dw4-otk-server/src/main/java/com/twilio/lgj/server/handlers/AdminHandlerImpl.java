package com.twilio.lgj.server.handlers;

import static java.util.Objects.requireNonNullElse;

import com.twilio.core.request.context.RequestContext;
import com.twilio.core.security.contexts.RemoteHostContext;
import com.twilio.lgj.server.api.admin.AdminApi;
import com.twilio.lgj.server.definitions.HelloMessageList;
import com.twilio.lgj.server.services.ServiceRepository;
import com.twilio.rest.TwilioServiceErrorException;
import com.twilio.rest.TwilioServiceErrorResponse;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;

@Named
public class AdminHandlerImpl implements AdminApi {
    private static final int DEFAULT_PAGE_SIZE = 50;

    private final ServiceRepository serviceRepository;

    @Inject
    public AdminHandlerImpl(final ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public long getTimeoutMs(final String operation) {
        return 5000L;
    }

    @Override
    public HelloMessageList readMessages(
            final Integer pageSize,
            final String pageToken,
            final RemoteHostContext remoteHostContext,
            final RequestContext requestContext,
            final ReadMessagesResponseHeaders responseHeaders)
            throws TwilioServiceErrorException {

        responseHeaders.setEtag("Hello World");

        return remoteHostContext
                .getAuthenticatedHost()
                .map(
                        authenticatedHost ->
                                this.serviceRepository.retrieveAllMessages(
                                        requireNonNullElse(pageSize, DEFAULT_PAGE_SIZE), pageToken))
                .orElseThrow(() -> new TwilioServiceErrorException(unauthenticated(), "Unauthenticated"));
    }

    private TwilioServiceErrorResponse unauthenticated() {
        return TwilioServiceErrorResponse.create(
                "Unauthenticated", Response.Status.UNAUTHORIZED.getStatusCode());
    }
}
