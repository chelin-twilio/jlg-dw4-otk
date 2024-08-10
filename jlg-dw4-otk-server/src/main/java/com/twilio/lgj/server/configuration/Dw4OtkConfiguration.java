package com.twilio.lgj.server.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.twilio.concurrency.limit.core.ConcurrencyLimitConfiguration;
import com.twilio.concurrency.limit.core.bundle.configuration.ConcurrencyLimitConfigurationStrategy;
import com.twilio.core.bundles.configuration.BaseAppConfiguration;
import com.twilio.core.bundles.configuration.SettableHostConfiguration;
import com.twilio.core.bundles.jakarta.auth.configuration.AuthConfiguration;
import com.twilio.core.bundles.jakarta.auth.configuration.AuthConfigurationStrategy;
import com.twilio.core.bundles.jakarta.exceptions.ExceptionsConfiguration;
import com.twilio.core.bundles.jakarta.exceptions.ExceptionsConfigurationStrategy;
import com.twilio.core.bundles.jakarta.mapper.ObjectMapperConfiguration;
import com.twilio.core.bundles.jakarta.mapper.ObjectMapperConfigurationStrategy;
import com.twilio.core.bundles.metrics.MetricsConfigurationStrategy;
import com.twilio.core.bundles.metrics.datadog.DataDogMetricsConfiguration;
import com.twilio.core.bundles.metrics.datadog.DataDogMetricsConfigurationStrategy;
import com.twilio.coreutil.configuration.client.ClientConfigurations;
import com.twilio.platform.config.HostConfiguration;
import com.twilio.ratelimit.bundle.config.RateLimitConfiguration;
import com.twilio.ratelimit.bundle.config.RateLimitConfigurationStrategy;
import com.twilio.rollbar.core.RollbarConfiguration;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.server.AbstractServerFactory;

/**
 * The {@link Dw4OtkConfiguration} is the "POJO" for the yaml file in
 * conf/default-service.yaml
 */
public class Dw4OtkConfiguration extends Configuration
        implements MetricsConfigurationStrategy,
                DataDogMetricsConfigurationStrategy,
                ExceptionsConfigurationStrategy,
                ObjectMapperConfigurationStrategy,
                AuthConfigurationStrategy,
                RateLimitConfigurationStrategy,
                ConcurrencyLimitConfigurationStrategy {

    private RateLimitConfiguration rateLimitConfiguration;

    private RollbarConfiguration rollbarConfiguration;

    private DataDogMetricsConfiguration dataDogMetricsConfiguration;

    @JsonDeserialize(as = SettableHostConfiguration.class)
    private HostConfiguration hostConfiguration;

    private boolean localTest;

    private BaseAppConfiguration baseAppConfiguration;

    private ExceptionsConfiguration exceptionMapperConfiguration;

    private ObjectMapperConfiguration objectMapperConfiguration = new ObjectMapperConfiguration();

    private AuthConfiguration authConfiguration;

    private ClientConfigurations clients;

    private ConcurrencyLimitConfiguration concurrencyLimit;

    @Override
    @JsonIgnore
    public boolean isRegisterDefaultExceptionMappers() {
        return ((AbstractServerFactory) getServerFactory()).getRegisterDefaultExceptionMappers();
    }

    @Override
    public RateLimitConfiguration getRateLimitConfiguration() {
        return rateLimitConfiguration;
    }

    public RollbarConfiguration getRollbarConfiguration() {
        return rollbarConfiguration;
    }

    @Override
    public DataDogMetricsConfiguration getDataDogMetricsConfiguration() {
        return dataDogMetricsConfiguration;
    }

    @Override
    public HostConfiguration getHostConfiguration() {
        return hostConfiguration;
    }

    @Override
    public BaseAppConfiguration getBaseAppConfiguration() {
        return baseAppConfiguration;
    }

    @Override
    public ExceptionsConfiguration getExceptionMapperConfiguration() {
        return exceptionMapperConfiguration;
    }

    @Override
    public ObjectMapperConfiguration getObjectMapperConfiguration() {
        return objectMapperConfiguration;
    }

    @Override
    public AuthConfiguration getAuthConfiguration() {
        return authConfiguration;
    }

    public ClientConfigurations getClients() {
        return clients;
    }

    public ConcurrencyLimitConfiguration getConcurrencyLimit() {
        return concurrencyLimit;
    }

    @Override
    public ConcurrencyLimitConfiguration getConcurrencyLimitConfiguration() {
        return concurrencyLimit;
    }

    public Dw4OtkConfiguration setAuthConfiguration(
            final AuthConfiguration authConfiguration) {
        this.authConfiguration = authConfiguration;
        return this;
    }

    public Dw4OtkConfiguration setConcurrencyLimit(
            final ConcurrencyLimitConfiguration concurrencyLimit) {
        this.concurrencyLimit = concurrencyLimit;
        return this;
    }

    public Dw4OtkConfiguration setDataDogMetricsConfiguration(
            final DataDogMetricsConfiguration dataDogMetricsConfiguration) {
        this.dataDogMetricsConfiguration = dataDogMetricsConfiguration;
        return this;
    }

    public Dw4OtkConfiguration setExceptionMapperConfiguration(
            final ExceptionsConfiguration exceptionMapperConfiguration) {
        this.exceptionMapperConfiguration = exceptionMapperConfiguration;
        return this;
    }

    public Dw4OtkConfiguration setLocalTest(final boolean localTest) {
        this.localTest = localTest;
        return this;
    }

    public Dw4OtkConfiguration setRateLimitConfiguration(
            final RateLimitConfiguration rateLimitConfiguration) {
        this.rateLimitConfiguration = rateLimitConfiguration;
        return this;
    }

    public Dw4OtkConfiguration setRollbarConfiguration(
            final RollbarConfiguration rollbarConfiguration) {
        this.rollbarConfiguration = rollbarConfiguration;
        return this;
    }

    @Override
    public String toString() {
        return "Dw4OtkConfiguration{"
                + "rateLimitConfiguration="
                + rateLimitConfiguration
                + ", rollbarConfiguration="
                + rollbarConfiguration
                + ", dataDogMetricsConfiguration="
                + dataDogMetricsConfiguration
                + ", hostConfiguration="
                + hostConfiguration
                + ", localTest="
                + localTest
                + ", baseAppConfiguration="
                + baseAppConfiguration
                + ", exceptionMapperConfiguration="
                + exceptionMapperConfiguration
                + ", objectMapperConfiguration="
                + objectMapperConfiguration
                + ", authConfiguration="
                + authConfiguration
                + ", clients="
                + clients
                + ", concurrencyLimit="
                + concurrencyLimit
                + '}';
    }
}
