package io.moquette.broker.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Initializes the socket channel pipeline by consuming a given {@link PipelineInitializer}
 * @author benjamin.krenn@leftshift.one - 3/29/19.
 * @since 0.14.0
 */
public class SocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final PipelineInitializer initializer;

    public SocketChannelInitializer(PipelineInitializer initializer) {
        this.initializer = initializer;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        initializer.accept(ch);
    }
}
