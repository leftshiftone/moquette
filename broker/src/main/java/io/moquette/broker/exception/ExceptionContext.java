package io.moquette.broker.exception;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author benjamin.krenn@leftshift.one - 3/29/19.
 * @since 0.1.0
 */
public class ExceptionContext {
    private final String clientId;
    private final ChannelContextWrapper context;

    public ExceptionContext(String clientId, ChannelContextWrapper context) {
        this.clientId = clientId;
        this.context = context;
    }

    public String getClientId() {
        return clientId;
    }

    public ChannelContextWrapper getContext() {
        return context;
    }
}

class ChannelContextWrapper {
    private final ChannelHandlerContext delegate;

    public ChannelContextWrapper(ChannelHandlerContext delegate) {
        this.delegate = delegate;
    }

    /**
     * Closes the connection
     */
    public ChannelFuture close() {
        return this.delegate.close();
    }
}
