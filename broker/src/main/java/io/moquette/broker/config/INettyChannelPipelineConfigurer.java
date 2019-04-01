package io.moquette.broker.config;

import io.netty.channel.ChannelPipeline;

@FunctionalInterface
public interface INettyChannelPipelineConfigurer {

    INettyChannelPipelineConfigurer DEFAULT = (pipeline -> {});
    void configure(ChannelPipeline pipeline);
}
