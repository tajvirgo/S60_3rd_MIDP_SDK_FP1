/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.btsppecho.client;

/**
 * Interface for a client bluetooth connection handler.
 */
public interface ClientConnectionHandlerListener
{
    // The handler's accept and open of a new connection
    // has occurred, but the I/O streams are not yet open.
    // The I/O streams must be open to send or receive
    // messages.
    public void handleAcceptAndOpen(ClientConnectionHandler handler);


    // The handler's I/O streams are now open and the
    // handler can now be used to send and receive messages.
    public void handleStreamsOpen(ClientConnectionHandler handler);


    // Opening of the handler's I/O streams failed. The handler has
    // closed any connections or streams that were open.
    // The handler should not be used anymore,
    // and should be discarded.
    public void handleStreamsOpenError(ClientConnectionHandler handler,
                                       String errorMessage);


    // The handler got an inbound message.
    public void handleReceivedMessage(
                    ClientConnectionHandler handler,
                    byte[] messageBytes);


    // A message that had been previously queued for sending
    // (identified by id) by the handler, has been sent successfully.
    public void handleQueuedMessageWasSent(
                    ClientConnectionHandler handler,
                    Integer id);


    // The handler has closed its connections and streams.
    // The handler should not be used anymore, and should be discarded.
    // Only handlers which have previously called 'handleOpen' may
    // call 'handleClose', and only just once.
    public void handleClose(ClientConnectionHandler handler);


    // An error related to the handler occurred. The handler
    // has closed the connection, and the handler should no
    // longer be used.
    public void handleErrorClose(ClientConnectionHandler handler,
                                 String errorMessage);

}
