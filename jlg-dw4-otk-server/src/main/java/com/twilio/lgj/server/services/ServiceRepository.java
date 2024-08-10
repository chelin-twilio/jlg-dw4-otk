package com.twilio.lgj.server.services;

import com.twilio.lgj.server.definitions.HelloMessage;
import com.twilio.lgj.server.definitions.HelloMessageContainer;
import com.twilio.lgj.server.definitions.HelloMessageList;
import com.twilio.sids.AccountSid;
import com.twilio.sids.SmsSid;
import java.util.Optional;

/** The {@link ServiceRepository} is an interface to the main business logic of the service. */
public interface ServiceRepository {
    HelloMessageList retrieveAllMessages(int pageSize, String pageToken);

    HelloMessageList retrieveMessages(
            AccountSid accountSid, String from, String to, int pageSize, String pageToken);

    Optional<HelloMessageContainer> retrieveMessage(SmsSid sid, AccountSid accountSid);

    HelloMessageContainer createMessage(SmsSid sid, AccountSid accountSid, HelloMessage helloMessage);

    HelloMessageContainer updateMessage(
            SmsSid sid, AccountSid accountSid, String from, String to, String message);

    Void deleteMessage(SmsSid sid, AccountSid accountSid);
}
