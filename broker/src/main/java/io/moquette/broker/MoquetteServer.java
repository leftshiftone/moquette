package io.moquette.broker;

import io.moquette.broker.config.*;
import io.moquette.broker.exception.IExceptionHandler;
import io.moquette.broker.security.IAuthenticator;
import io.moquette.broker.security.IAuthorizationPolicy;
import io.moquette.interception.InterceptHandler;
import io.netty.handler.codec.mqtt.MqttPublishMessage;

import java.io.File;
import java.util.Collection;
import java.util.Properties;

/**
 * @author benjamin.krenn@leftshift.one - 3/31/19.
 * @since 0.14.0
 */
public interface MoquetteServer {
    void start();

    void stop();

    int getPort();

    int getSslPort();

    Collection<ClientDescriptor> listConnectedClients();

    /**
     * Use the broker to publish a message. It's intended for embedding applications. It can be used
     * only after the integration is correctly started with startServer.
     *
     * @param msg      the message to forward.
     * @param clientId the id of the sending integration.
     * @throws IllegalStateException if the integration is not yet started
     */
    void internalPublish(MqttPublishMessage msg, final String clientId);

    /**
     * @return a default Moquette server with the configuration loaded from moquette.conf residing in the given
     * moquette.path system property.
     */
    static MoquetteServer defaultConfiguration() {
        return new DefaultMoquetteServerBuilder().withDefaultConfigurationFile().build();
    }

    static MoquetteServer.Builder builder() {
        return new DefaultMoquetteServerBuilder();
    }


    interface Builder {
        Builder withDefaultConfigurationFile();

        Builder withConfiguration(File config);

        Builder withConfiguration(Properties config);

        Builder withConfiguration(IConfig config);

        Builder attachInterceptHandler(InterceptHandler interceptHandler);

        Builder withExceptionHandler(IExceptionHandler exceptionHandler);

        Builder withPipelineConfigurer(INettyChannelPipelineConfigurer configurer);

        Builder withSslContextCreator(ISslContextCreator sslCtxCreator);
        Builder withAuthenticator(IAuthenticator authenticator);
        Builder withAuthorizationPolicy (IAuthorizationPolicy authorizatorPolicy);

        MoquetteServer build();
    }
}
