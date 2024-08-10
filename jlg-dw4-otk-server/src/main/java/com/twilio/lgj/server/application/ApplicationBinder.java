package com.twilio.lgj.server.application;

import com.twilio.lgj.server.api.admin.AdminApi;
import com.twilio.lgj.server.api.hello.message.HelloMessageApi;
import com.twilio.lgj.server.configuration.Dw4OtkConfiguration;
import com.twilio.lgj.server.handlers.AdminHandlerImpl;
import com.twilio.lgj.server.handlers.HelloMessageHandlerImpl;
import com.twilio.lgj.server.services.ServiceRepository;
import com.twilio.lgj.server.services.ServiceRepositoryImpl;
import io.dropwizard.core.Configuration;
import jakarta.inject.Singleton;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ApplicationBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(AdminHandlerImpl.class).to(AdminApi.class).in(Singleton.class);
        bind(HelloMessageHandlerImpl.class).to(HelloMessageApi.class).in(Singleton.class);
        bind(ServiceRepositoryImpl.class).to(ServiceRepository.class).in(Singleton.class);
    }
}