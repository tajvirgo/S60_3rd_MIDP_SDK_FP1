/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.wma.chat;

import java.util.*;
import javax.microedition.io.*;
import javax.wireless.messaging.*;

/**
 * Handles setting up a connection and the sending and receiving of messages.
 * Implements the MessageListener to listen for incoming messages.
 *
 * @version 1.0
 */
public class MessageHandler extends Thread implements MessageListener {
    /** reference to midlet instance */
    private WMAMIDlet parent;
    /** reference to ScreenHandler instance */
    private ScreenHandler screenHandler;
    /** instance of MessageConnection */
    private MessageConnection messageConnection;
    /** instance of TextMessage */
    private TextMessage textMessage;
    /** instance of Message */
    private Message message;
    /** flag to terminate thread */
    private boolean isRunning = true;
    /** String to hold text of message to send */
    private String text = "";
    /** Vector holding the selected peers numbers */
    private Vector numbers = new Vector();
    /** counter for number of messages waiting to be received. By default is instantiated to 0. */
    private int incomingQueue;
    /** Vector containing messages to be sent */
    private Vector outgoingQueue = new Vector();

    /** Sets up the thread and starts it running. */
    public MessageHandler(WMAMIDlet midlet) {
        parent = midlet;
        start();
    }

    /**
     *  Defines the run() method which all threads must do.
     *  If there are no messages to be sent or received the thread waits
     *  to be notified. Once notified it calls the appropriate send or
     *  receive method.
     */
    public void run() {
        if (connect()) {
            while (isRunning) {
                synchronized (this) {
                    if ((outgoingQueue.size() == 0) && (incomingQueue == 0)) {
                        try {
                            wait();
                        } catch (InterruptedException ie) {
                        }
                    }
                }
                // calls the send method
                while (outgoingQueue.size() > 0) {
                    OutgoingMessage out = (OutgoingMessage)outgoingQueue.firstElement();
                    send(out);
                    outgoingQueue.removeElementAt(0);
                }
                //calls the receive method
                while (incomingQueue > 0) {
                    receive();
                    synchronized (this){
                        incomingQueue--;
                    }
                }
            }
        } else  {
            //handles a failed attempt to connect
            parent.destroyApp(true);
            parent.notifyDestroyed();
        }
    }

    /**
     *  Performs the connection and sets the message listener listening.
     */
    public boolean connect() {
        try {
            messageConnection = (MessageConnection)Connector.open("sms://:" + parent.port);
            messageConnection.setMessageListener(this);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *   Adds outgoing messages to a queue and notifies the thread that it
     *   has something to do.
     */
    public void doSend(String text, Vector numbers) {
        for(Enumeration enumeration = numbers.elements(); enumeration.hasMoreElements(); ) {
            String number = (String)enumeration.nextElement();
            String port = (String)parent.numbersList.get(number);
            String address = "sms://" + number + ":" + port;
            OutgoingMessage out = new OutgoingMessage(address, text);
            outgoingQueue.addElement(out);
        }
        synchronized (this) {
            notify();
        }
    }

    /**
     *  Sends a message on the connection.
     *  Creates a new TextMessage, sets the payload and tries to send the message.
     *
     *  @param out the message to be sent
     */
    public void send(OutgoingMessage out) {
        try {
            //creates a new TextMessage
            textMessage = (TextMessage)messageConnection.newMessage(MessageConnection.TEXT_MESSAGE, out.getAddress());
            textMessage.setPayloadText(out.getMessage());
            //attempts to send the new TextMessage
            messageConnection.send(textMessage);
            parent.screenHandler.updateChatScreen(out.getMessage());
        } catch (Exception e) {
            parent.screenHandler.updateChatScreen("Send exception");
        }
    }

    /**
     *  Receives a message from the connection.
     *  Receives a message, tests to see if it is of type TextMessage if it is
     *  then it updates the ChatScreen with the received text.
     */
    public void receive() {
        try {
            message = messageConnection.receive();
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage)message;
                parent.screenHandler.updateChatScreen(textMessage.getPayloadText());
            } else {
                // functionality to deal with BinaryMessage
                // currently no action.
            }
        } catch (Exception e) {
            parent.screenHandler.updateChatScreen("Receive exception");
        }
    }

    /**
     *  Handles the event of a message arriving on the connection.
     *  Increments a counter and notifies the thread that it has something todo.
     *
     *  @param conn the message connection to which the listener is listening
     */
    public synchronized void notifyIncomingMessage(MessageConnection conn) {
        incomingQueue++;
        synchronized (this){
            notify();
        }
    }

    /**
     *  Handles the tidying up of the messageHandler.
     *  Sets listener interface to null to de-register it, closes the
     *  connection.
     */
    public void exit() {
        if (messageConnection != null) {
            try {
                messageConnection.setMessageListener(null);
                messageConnection.close();
            } catch (Exception e) {
                 parent.screenHandler.updateChatScreen("Exit exception");
            }
        }
    }

    /**
     *  Stops the thread. Sets the isRunning flag to false.
     */
    public synchronized void stopThread() {
        isRunning = false;
    }
}
