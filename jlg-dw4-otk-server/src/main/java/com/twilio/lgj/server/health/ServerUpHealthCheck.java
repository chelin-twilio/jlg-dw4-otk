package com.twilio.lgj.server.health;

import com.twilio.coreutil.service.api.healthcheck.HealthCheck;
import com.twilio.coreutil.service.api.healthcheck.HealthCheckResult;
import com.twilio.coreutil.service.api.healthcheck.Result;

/**
 * The {@link ServerUpHealthCheck} is a default {@link HealthCheck} that simply returns healthy.
 *
 * <p>TODO: Implement a meaningful health check for your service.
 */
public class ServerUpHealthCheck implements HealthCheck {

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public Result execute() {
        return HealthCheckResult.healthy("Server is in UP state");
    }

    @Override
    public String toString() {
        return "ServerUpHealthCheck - implement a meaningful health check for your service";
    }
}
