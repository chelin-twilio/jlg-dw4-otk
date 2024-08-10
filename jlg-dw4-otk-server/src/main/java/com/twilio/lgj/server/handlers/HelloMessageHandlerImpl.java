package com.twilio.lgj.server.handlers;

import static java.util.Objects.requireNonNullElse;

import com.twilio.core.request.context.RequestContext;
import com.twilio.core.security.contexts.ApiAuthContext;
import com.twilio.lgj.server.api.hello.message.HelloMessageApi;
import com.twilio.lgj.server.definitions.HelloMessage;
import com.twilio.lgj.server.definitions.HelloMessageContainer;
import com.twilio.lgj.server.definitions.HelloMessageList;
import com.twilio.lgj.server.services.ServiceRepository;
import com.twilio.rest.TwilioServiceErrorException;
import com.twilio.rest.TwilioServiceErrorResponse;
import com.twilio.sids.SmsSid;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
public class HelloMessageHandlerImpl implements HelloMessageApi {
    private static final TwilioServiceErrorResponse NO_ACCOUNT_SID_SPECIFIED =
            TwilioServiceErrorResponse.create("No account SID specified", 401);
    private static final Integer DEFAULT_PAGE_SIZE = 50;

    private final ServiceRepository serviceRepository;

    @Inject
    public HelloMessageHandlerImpl(final ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public HelloMessageList listMessages(
            final String from,
            final String to,
            final Integer pageSize,
            final String pageToken,
            final ApiAuthContext apiAuthContext,
            final RequestContext requestContext,
            final HelloMessageApi.ListMessagesResponseHeaders headers) {
        return apiAuthContext
                .getAuthenticatedAccount()
                .map(
                        authenticatedAccount ->
                                serviceRepository.retrieveMessages(
                                        authenticatedAccount.getSid(),
                                        from,
                                        to,
                                        requireNonNullElse(pageSize, DEFAULT_PAGE_SIZE),
                                        pageToken))
                .orElseThrow(() -> new TwilioServiceErrorException(unauthenticated(), "Unauthenticated"));
    }

    @Override
    public HelloMessageContainer getMessage(
            final SmsSid sid,
            final ApiAuthContext apiAuthContext,
            final RequestContext requestContext,
            final ConditionalRequest conditionalRequest,
            final GetMessageResponseHeaders responseHeaders) {
        responseHeaders.setEtag("Hello World");

        return apiAuthContext
                .getAuthenticatedAccount()
                .flatMap(
                        authenticatedAccount ->
                                serviceRepository.retrieveMessage(sid, authenticatedAccount.getSid()))
                .orElseThrow(() -> new TwilioServiceErrorException(unauthenticated(), "Unauthenticated"));
    }

    @Override
    public HelloMessageContainer createMessage(
            final SmsSid sid,
            final String etag,
            final HelloMessage body,
            final ApiAuthContext apiAuthContext,
            final RequestContext requestContext,
            final HelloMessageApi.CreateMessageResponseHeaders responseHeaders) {
        responseHeaders.setEtag("Hello World: " + etag);
        return apiAuthContext
                .getAuthenticatedAccount()
                .map(
                        authenticatedAccount ->
                                this.serviceRepository.createMessage(sid, authenticatedAccount.getSid(), body))
                .orElseThrow(() -> new TwilioServiceErrorException(unauthenticated(), "Unauthenticated"));
    }

    @Override
    public HelloMessageContainer updateMessage(
            final SmsSid sid,
            final String from,
            final String to,
            final String message,
            final ApiAuthContext apiAuthContext,
            final RequestContext requestContext,
            final ConditionalRequest conditionalRequest,
            final UpdateMessageResponseHeaders responseHeaders) {
        return apiAuthContext
                .getAuthenticatedAccount()
                .map(
                        authenticatedAccount ->
                                this.serviceRepository.updateMessage(
                                        sid, authenticatedAccount.getSid(), from, to, message))
                .orElseThrow(() -> new TwilioServiceErrorException(unauthenticated(), "Unauthenticated"));
    }

    @Override
    public Void deleteMessage(
            final SmsSid sid,
            final ApiAuthContext apiAuthContext,
            final RequestContext requestContext,
            final HelloMessageApi.DeleteMessageResponseHeaders responseHeaders) {
        return apiAuthContext
                .getAuthenticatedAccount()
                .map(
                        authenticatedAccount ->
                                this.serviceRepository.deleteMessage(sid, authenticatedAccount.getSid()))
                .orElseThrow(() -> new TwilioServiceErrorException(unauthenticated(), "Unauthenticated"));
    }

    private TwilioServiceErrorResponse unauthenticated() {
        return TwilioServiceErrorResponse.create(
                "Unauthenticated", NO_ACCOUNT_SID_SPECIFIED.getHttpStatusCode());
    }
}
