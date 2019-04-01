package io.moquette.broker;

import io.moquette.broker.config.*;
import io.moquette.broker.exception.IExceptionHandler;
import io.moquette.broker.security.IAuthenticator;
import io.moquette.broker.security.IAuthorizationPolicy;
import io.moquette.interception.InterceptHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author benjamin.krenn@leftshift.one - 3/31/19.
 * @since 0.14.0
 */
public class DefaultMoquetteServerBuilder implements MoquetteServer.Builder {

    private final List<InterceptHandler> interceptors = new ArrayList<>();
    private IConfig configuration;
    private INettyChannelPipelineConfigurer pipelineConfigurer = INettyChannelPipelineConfigurer.DEFAULT;
    private IExceptionHandler exceptionHandler = IExceptionHandler.DEFAULT;
    private ISslContextCreator sslCtxCreator;
    private IAuthenticator authenticator;
    private IAuthorizationPolicy authorizationPolicy;

    private static File defaultConfigFile() {
        String configPath = System.getProperty("moquette.path", null);
        return new File(configPath, IConfig.DEFAULT_CONFIG);
    }

    @Override
    public MoquetteServer.Builder withDefaultConfigurationFile() {
        return this.withConfiguration(defaultConfigFile());
    }

    @Override
    public MoquetteServer.Builder withConfiguration(File config) {
        Objects.requireNonNull(config, "config can not be null");
        IResourceLoader filesystemLoader = new FileResourceLoader(config);
        this.configuration = new ResourceLoaderConfig(filesystemLoader);
        return this;
    }

    @Override
    public MoquetteServer.Builder withConfiguration(Properties config) {
        Objects.requireNonNull(config, "config can not be null");
        this.configuration = new MemoryConfig(config);
        return this;
    }

    @Override
    public MoquetteServer.Builder withConfiguration(IConfig config) {
        this.configuration = config;
        return this;
    }

    @Override
    public MoquetteServer.Builder attachInterceptHandler(InterceptHandler interceptHandler) {
        this.interceptors.add(interceptHandler);
        return this;
    }

    @Override
    public MoquetteServer.Builder withExceptionHandler(IExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    @Override
    public MoquetteServer.Builder withPipelineConfigurer(INettyChannelPipelineConfigurer configurer) {
        this.pipelineConfigurer = configurer;
        return this;
    }

    @Override
    public MoquetteServer.Builder withSslContextCreator(ISslContextCreator sslCtxCreator) {
        this.sslCtxCreator = sslCtxCreator;
        return this;
    }

    @Override
    public MoquetteServer.Builder withAuthenticator(IAuthenticator authenticator) {
        this.authenticator = authenticator;
        return this;
    }

    @Override
    public MoquetteServer.Builder withAuthorizationPolicy(IAuthorizationPolicy authorizationPolicy) {
        this.authorizationPolicy = authorizationPolicy;
        return this;
    }

    @Override
    public MoquetteServer build() {
        return new Server(this.configuration,
                            this.pipelineConfigurer,
                            this.exceptionHandler,
                            this.sslCtxCreator,
                            this.authenticator,
                            this.authorizationPolicy);
    }
}
