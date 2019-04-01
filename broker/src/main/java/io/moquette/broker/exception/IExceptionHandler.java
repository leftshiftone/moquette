package io.moquette.broker.exception;

import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;

/**
 * Allows clients to handle uncaught exceptions from the netty pipeline
 *
 * @author benjamin.krenn@leftshift.one - 3/29/19.
 * @since 0.14.0
 */
@FunctionalInterface
public interface IExceptionHandler {
    IExceptionHandler DEFAULT = ((throwable, context) -> context.getContext().close().addListener(CLOSE_ON_FAILURE));
    void handle(Throwable throwable, ExceptionContext context);
}
