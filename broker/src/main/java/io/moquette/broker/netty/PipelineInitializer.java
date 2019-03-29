package io.moquette.broker.netty;

import io.netty.channel.socket.SocketChannel;

import java.util.function.Consumer;

/**
 * @author benjamin.krenn@leftshift.one - 3/29/19.
 * @since 0.14.0
 */
@FunctionalInterface
public interface PipelineInitializer extends Consumer<SocketChannel> { }
