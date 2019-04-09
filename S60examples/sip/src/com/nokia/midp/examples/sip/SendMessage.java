/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sip;


import java.util.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import javax.microedition.sip.*;

/**
 * SendMessage: Implements simple message sending using SIP via JSR-180.
 * It sends SIP messsage request with text content to other end.  It uses
 * shared mode when connecting to the sip server.
 * This example assumes that user already created the profile on the SDK's or
 * phone's sip setting, and uses sip profile to register with sip server.  The
 * sip public name for the SDK running SendMessge midlet is "sip:sip1@sipServer",
 * and the sip public name for the SDK running ReceivingMessage midlet is
 * "sip:sip2@sipServer".

 *
 */
public class SendMessage extends MIDlet implements CommandListener, SipClientConnectionListener {

        private Display display;
        private long startTime;
        private Form form;
        private TextField address;
        private TextField subject;
        private TextField message;
        private Command sendCmd;
        private Command exitCmd;

        //Protocol constants - headers
        private final String ACCEPT_CONTACT_HEADER = "Accept-Contact";
        private final String CONTENT_TYPE_HEADER = "Content-Type";
        private final String CONTENT_LENGTH_HEADER = "Content-Length";

        //Protocol constants - some header values
        private final String ACCEPT_CONTACT = "*;type=\"application/test\"";
        private final String DESTINATION_SIP_URI = "sip:sip2@receiveHost:5060";

        //UI labels
        private final String ADDR_LABEL = "Address";
        private final String EXCEPTION = "Exception: ";
        private final String SUBJECT_LABEL = "Subject";
        private final String MSGBODY_LABEL = "Message Text";
        private final String SEND_LABEL = "Send";
        private final String EXIT_LABEL = "Exit";


        /**
         * Creates new SendMessage object with UI for user to enter connection
         * information and the message.
         */
        public SendMessage() {
                display=Display.getDisplay(this);
                form = new Form("Message example");
                address = new TextField(ADDR_LABEL, DESTINATION_SIP_URI, 30, TextField.LAYOUT_LEFT);
                subject = new TextField(SUBJECT_LABEL, "Hi", 30, TextField.LAYOUT_LEFT);
                message = new TextField(MSGBODY_LABEL, "Hello world.", 30, TextField.LAYOUT_LEFT);
                form.append(address);
                form.append(subject);
                form.append(message);
                sendCmd = new Command(SEND_LABEL, Command.ITEM, 1);
                form.addCommand(sendCmd);
                exitCmd = new Command(EXIT_LABEL, Command.EXIT, 1);
                form.addCommand(exitCmd);
                form.setCommandListener(this);
        }
        /**
         * Creates a commandAction object with given command and diaplayable.
         * @param c Command the command used for the commandAction
         * @param d Displayable the displayable used when excuting the command
         */
        public void commandAction(Command c, Displayable d) {
                if(c == sendCmd) {
                        Thread t = new Thread() {
                                public void run() {
                                        sendMessage();
                                }
                        };
                        t.start();
                }
                if(c == exitCmd) {
                        destroyApp(true);
                }
        }
        /**
         * Signals the MIDlet that it has entered the Active state.  It also
         * invokes the application and makes the form visible.
         */
        public void startApp() {
                display.setCurrent(form);
        }
        /**
         * Open the connection and send message over to the receiver.
         */
        private void sendMessage() {
                SipClientConnection sc = null;
                try {
                        form.append("Waiting for the connection.");
                        sc = (SipClientConnection) Connector.open(address.getString());
                        sc.setListener(this);
                        String text = message.getString();
                        sc.initRequest("MESSAGE", null);
                        sc.setHeader(SUBJECT_LABEL, subject.getString());
                        sc.setHeader(CONTENT_TYPE_HEADER , "text/plain");
                        sc.setHeader(CONTENT_LENGTH_HEADER, ""+text.length());
                        sc.setHeader(ACCEPT_CONTACT_HEADER, ACCEPT_CONTACT);
                        OutputStream os = sc.openContentOutputStream();
                        os.write(text.getBytes());
                        os.close(); // close and send out
                        form.append("Message has been sent.");
                        startTime = System.currentTimeMillis();

                } catch(Exception ex) {
                        ex.printStackTrace();
                        form.append("send " + EXCEPTION + ex.getMessage());
                }
        }

        /**
         * Handles incoming responses sent from the other party.
         * @param SipClientConnection the object represents a SIP client
         * transaction.
         */
        public void notifyResponse(SipClientConnection scc) {
                try {
                        scc.receive(1);
                        form.append("notifyResponse: "+scc.getStatusCode()+" "+scc.getReasonPhrase());
                        scc.close();
                } catch(Exception ex) {
                        form.append("notifyResponse exception "+ex.getMessage());
                }
        }

        /**
         * Signals the MIDlet to enter the Paused state.
         */
        public void pauseApp() {
        }
        /**
         * Signals the MIDlet to terminate and notifies the destroyed state.
         * @param b boolean If true when this method is called, the MIDlet
         * must cleanup and release all resources. If false the MIDlet may
         * throw MIDletStateChangeException to indicate it does not want to be
         * destroyed at this time.
         */
        public void destroyApp(boolean b) {
                notifyDestroyed();
        }

}







