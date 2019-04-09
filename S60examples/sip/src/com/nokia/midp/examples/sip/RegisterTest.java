/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sip;


import java.util.*;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import javax.microedition.sip.*;

/**
 * RegisterTest: Implements SIP service registration via JSR-180.
 * It sends "REGISTER", and receives response from the SIP registrar.
 */
public class RegisterTest extends MIDlet implements CommandListener {

        private Display display;
        private Form form;
        private TextField registrar;
        private TextField useraddr;
        private TextField contact;
        private Command sendCmd;
        private Command exitCmd;
        private final int OK_STATUS = 200;
        private final String REG_ADD_LABEL = "Registrar address:";
        private final String REG_ADD = "sip:localhost";
        private final String FROM_TO_LABEL = "From-To:";
        private final String FROM_TO = "sip:sippy.tester@localhost";
        private final String CONNECTION = "Connection ";
        private final String EXCEPTION = "Exception: ";

        /**
         * Creates a new RegisterTest object to register.
         */
        public RegisterTest() {
                String ctaddr = "sip:user@host"; // Contact address
                // Open a SipConnectionNotifier in an arbitrary port
                SipConnectionNotifier scn = null;
                form = new Form("Register test");
                try {
                        scn = (SipConnectionNotifier) Connector.open("sip:");
                        if(scn != null) {
                                // resolve Contact address from SipConnectionNotifier ino
                                ctaddr = new String("sip:user@"+scn.getLocalAddress()+":"+scn.getLocalPort());
                        }
                } catch(Exception ex) {
                        ex.printStackTrace();
                        form.append( CONNECTION + EXCEPTION + ex.getMessage());
                }
                display=Display.getDisplay(this);
                registrar = new TextField(REG_ADD_LABEL, REG_ADD, 40, TextField.LAYOUT_LEFT);
                useraddr = new TextField(FROM_TO_LABEL, FROM_TO, 40, TextField.LAYOUT_LEFT);
                contact = new TextField("Contact:", ctaddr, 40, TextField.LAYOUT_LEFT);
                form.append(registrar);
                form.append(useraddr);
                form.append(contact);
                sendCmd = new Command("Register", Command.ITEM, 1);
                form.addCommand(sendCmd);
                exitCmd = new Command("Exit", Command.EXIT, 1);
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
                                        register();
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
         * Registers through the SipClientConnection.
         */
        public void register() {
                try {
                        SipClientConnection scc = null;
                        // Open a SipClientConnection for REGISTER targeting the
                        // registrar address
                        scc = (SipClientConnection) Connector.open(registrar.getString());
                        scc.initRequest("REGISTER", null);
                        // Set necessary headers
                        scc.setHeader("From", useraddr.getString());
                        scc.setHeader("To", useraddr.getString());
                        scc.setHeader("Contact", contact.getString());
                        // Send it out
                        scc.send();

                        // wait for response
                        if(scc.receive(15000)) {
                                form.append("REGISTER Response: "+scc.getStatusCode()+" "+
                                                scc.getReasonPhrase()+"\n");
                                // Display the registered Contact(s)
                                if(scc.getStatusCode() == OK_STATUS) {
                                        String hdrs[] = scc.getHeaders("Contact");
                                        for(int i=0; i <hdrs.length; i++) {
                                                form.append("Contact: "+hdrs[i]+"\n");
                                        }
                                }
                        } else {
                                form.append("No response...");
                        }

                } catch(Exception ex) {
                        ex.printStackTrace();
                        form.append(EXCEPTION + ex.getMessage());
                }
        }
        /**
         * Signals the MIDlet to enter the Paused state.
         */
        public void pauseApp() {
        }

        /**
         * Signals the MIDlet to terminate and enter the Destroyed state.
         * @param b boolean If true when this method is called, the MIDlet
         * must cleanup and release all resources. If false the MIDlet may
         * throw MIDletStateChangeException to indicate it does not want to be
         * destroyed at this time.
         */
        public void destroyApp(boolean b) {
                notifyDestroyed();
        }

}







