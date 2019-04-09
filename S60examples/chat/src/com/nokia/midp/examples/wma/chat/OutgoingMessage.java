/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.wma.chat;

/**
 *  Holds a String containing the address to send a message to
 *  and holds a String containing the message to send.
 *
 * @version 1.0
 */
public class OutgoingMessage {
    /** String to hold the address */
    private final String address;
    /** String to hold the message */
    private final String message;

    /**
     *  Sets up the object with the an address and message.
     *
     *  @param address the address to send the message to
     *  @param message the message to send
     */
    public  OutgoingMessage(String address, String message) {
        this.address = address;
        this.message = message;
    }

    /**
     *  Returns the address the message is to be sent to.
     *
     *  @return a String containing the address
     */
    public String getAddress() {
        return address;
    }

    /**
     *  Returns the message to be sent.
     *
     *  @return a String containing the message
     */
    public String getMessage() {
        return message;
    }
}