/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.btl2capecho.server;

/**
 * An interface for a service connection handler listener.
 */
public interface ServerConnectionHandlerListener
{
    // The handler's open succeeded. It can now be used for sending
    // and receiving messages.
    public void handleOpen(ServerConnectionHandler handler);


    // The hadler's open failed. It has closed any connections or
    // streams that were open. The handler should not be used anymore,
    // and should be discarded.
    public void handleOpenError(ServerConnectionHandler handler,
                                String errorMessage);


    // The handler got an inbound message.
    public void handleReceivedMessage(
                    ServerConnectionHandler handler,
                    byte[] messageBytes);


    // A message that had been previously queued for sending
    // (identified by id) by the handler, has been sent successfully.
    public void handleQueuedMessageWasSent(
                    ServerConnectionHandler handler,
                    Integer id);


    // The handler has closed its connections and streams.
    // The handler should not be used anymore, and should be discarded.
    // Only handlers which have previously called 'handleOpen' may
    // call 'handleClose', and only just once.
    public void handleClose(ServerConnectionHandler handler);


    // An error related to the handler occurred. The handler
    // has closed the connection, and the handler should no
    // longer be used.
    public void handleErrorClose(ServerConnectionHandler handler,
                                 String errorMessage);

}

