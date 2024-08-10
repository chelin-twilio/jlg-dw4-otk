package com.twilio.lgj.server.services;

import static java.util.Objects.requireNonNullElse;

import com.twilio.core.util.UriBuilder;
import com.twilio.lgj.server.definitions.HelloMessage;
import com.twilio.lgj.server.definitions.HelloMessageContainer;
import com.twilio.lgj.server.definitions.HelloMessageList;
import com.twilio.lgj.server.definitions.ListMeta;
import com.twilio.sids.AccountSid;
import com.twilio.sids.SmsSid;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This is the implementation class for the {@link ServiceRepository} */
@Named
public class ServiceRepositoryImpl implements ServiceRepository {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRepositoryImpl.class);

    public static final AccountSid DUMMY_ACCOUNT_SID =
            AccountSid.parse("ACdeadbeefdeadbeefdeadbeefdeadbeef");
    private static final SmsSid DUMMY_MESSAGE_SID =
            SmsSid.parse("SM12345678901234567890123456789012");

    private static final HelloMessageContainer DUMMY_MESSAGE =
            new HelloMessageContainer(
                    DUMMY_MESSAGE_SID,
                    DUMMY_ACCOUNT_SID,
                    new HelloMessage("Bob", "Jeff", "Hi Jeff"),
                    OffsetDateTime.now(),
                    OffsetDateTime.now());

    @Inject
    public ServiceRepositoryImpl() {}

    @Override
    public HelloMessageList retrieveAllMessages(final int pageSize, final String pageToken) {
        LOG.info("Retrieving ALL messages");

        final var uriBuilder = UriBuilder.from("/v1/service/messages");
        uriBuilder.addQueryParam("PageSize", pageSize);
        if (pageToken != null) {
            uriBuilder.addQueryParam("PageToken", pageToken);
        }

        final var messageList =
                new HelloMessageList(
                        List.of(DUMMY_MESSAGE),
                        new ListMeta("hello_messages", pageSize, uriBuilder.build(), null, null));

        return messageList;
    }

    @Override
    public HelloMessageList retrieveMessages(
            final AccountSid accountSid,
            final String from,
            final String to,
            final int pageSize,
            final String pageToken) {
        LOG.info("Retrieving messages for account {}, from: {}, to: {}", accountSid, from, to);

        final UriBuilder uriBuilder = UriBuilder.from("/v1/service/messages");
        if (from != null) {
            uriBuilder.addQueryParam("From", from);
        }
        if (to != null) {
            uriBuilder.addQueryParam("To", to);
        }
        uriBuilder.addQueryParam("PageSize", pageSize);
        if (pageToken != null) {
            uriBuilder.addQueryParam("PageToken", pageToken);
        }

        // In a real service, you'd fetch rows from a DB, passing the filters provided.
        final var messages =
                DUMMY_ACCOUNT_SID.equals(accountSid)
                        ? List.of(DUMMY_MESSAGE)
                        : List.<HelloMessageContainer>of();
        return new HelloMessageList(
                messages, new ListMeta("hello_messages", pageSize, uriBuilder.build(), null, null));
    }

    @Override
    public Optional<HelloMessageContainer> retrieveMessage(
            final SmsSid sid, final AccountSid accountSid) {
        LOG.info("Returning message with SID {}", sid.getValue());

        // In a real serviec, you'd attempt to fetch a row from the DB.
        if (DUMMY_MESSAGE_SID.equals(sid) && DUMMY_ACCOUNT_SID.equals(accountSid)) {
            return Optional.of(DUMMY_MESSAGE);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public HelloMessageContainer createMessage(
            final SmsSid sid, final AccountSid accountSid, final HelloMessage helloMessage) {
        LOG.info(
                "Posting the following message for account {}: {}", accountSid.getValue(), helloMessage);

        // In a real service, you'd insert a row in the DB.
        final var now = OffsetDateTime.now();
        return new HelloMessageContainer(sid, accountSid, helloMessage, now, now);
    }

    @Override
    public HelloMessageContainer updateMessage(
            final SmsSid sid,
            final AccountSid accountSid,
            final String from,
            final String to,
            final String message) {
        LOG.info("Updating message {} for account {}", sid.getValue(), accountSid.getValue());

        // In a real service, you'd update a row in the DB.
        if (DUMMY_MESSAGE_SID.equals(sid) && DUMMY_ACCOUNT_SID.equals(accountSid)) {
            return new HelloMessageContainer(
                    sid,
                    accountSid,
                    new HelloMessage(
                            requireNonNullElse(from, DUMMY_MESSAGE.getHelloMessage().getFrom()),
                            requireNonNullElse(to, DUMMY_MESSAGE.getHelloMessage().getTo()),
                            requireNonNullElse(message, DUMMY_MESSAGE.getHelloMessage().getMessage())),
                    DUMMY_MESSAGE.getDateCreated(),
                    OffsetDateTime.now());
        } else {
            return null;
        }
    }

    @Override
    public Void deleteMessage(final SmsSid sid, final AccountSid accountSid) {
        if (DUMMY_MESSAGE_SID.equals(sid) && DUMMY_ACCOUNT_SID.equals(accountSid)) {
            // In a real service, you'd attempt to delete a row from the DB.
            LOG.info("Deleting message {} for account {}", sid.getValue(), accountSid.getValue());
        }
        return null;
    }
}
