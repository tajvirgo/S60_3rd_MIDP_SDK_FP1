/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sip;


import java.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import javax.microedition.sip.*;

/**
 * Subscribe: Implements SIP service subscription via JSR-180.
 * It sends "SUBSCRIBE". It also receives response and "NOTIFY" from the
 * presence. This example uses shared mode when opening server connection.
 * This example assumes that user already created the profile on the SDK's or
 * phone's sip setting, and uses sip profile to register with sip server.  The
 * sip public name for the SDK running Subscribe midlet is "sip:sip1@sipServer".


 */
//public class Subscribe extends MIDlet implements SipServerConnectionListener, CommandListener {
public class Subscribe extends MIDlet implements CommandListener {

        private Display display;
        private SipDialog dialog;
        private Form form;
        private TextField userAddr;
        private Command sendCmd;
        private Command exitCmd;

        //Protocol constants - status
        private final int OK_STATUS = 200;

        //Timeouts values
        private final int BLOCK_RECEIVE_INDICATOR = 15000;

        //UI labels
        private final String SUB_LABEL = "Subscribe To:";
        private final String EXCEPTION = "Exception: ";

        private final String SUB_ADDR = "sip:sip2@SipServer";

        /**
         * Creates a new Subscribe oject with UI for user to enter information.
         */
        public Subscribe() {
                display=Display.getDisplay(this);
                form = new Form("SUBSCRIBE from server");
                display.setCurrent(form);
                userAddr = new TextField(SUB_LABEL, SUB_ADDR, 40, TextField.LAYOUT_LEFT);
                form.append(userAddr);
                sendCmd = new Command("Subscribe", Command.ITEM, 1);
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
                                        subscribe();
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
        }

        private void subscribe() {
                form.append("Subscribing.....\n");
                try {
                        SipConnectionNotifier scn = null;
                        SipServerConnection ssc = null;
                        scn = (SipConnectionNotifier) Connector.open("sip:*;type=\"application/vnd.company.x-game\"");
                        SipClientConnection scc =
                                (SipClientConnection) Connector.open(userAddr.getString());
                        scc.initRequest("SUBSCRIBE", scn);
                        scc.setHeader("From", userAddr.getString());
                        scc.setHeader("Contact", "sip:sip2@"+"10.20.30.60");
                        scc.setHeader("Expires", "950");
                        scc.setHeader("Event", "presence");
                        scc.addHeader("Accept", "application/xpidf+xml");
                        scc.send();
                        boolean res = scc.receive(BLOCK_RECEIVE_INDICATOR); // blocking receive
                        form.append("Subscribe Response: "+scc.getStatusCode()+" "+
                                                scc.getReasonPhrase()+"\n");
                        scc.close();
                }catch(ConnectionNotFoundException  cnfex){
                        form.append( "subscribe CONNECTION" + EXCEPTION + cnfex.getMessage());
                }catch(SecurityException secex){
                        form.append( "subscribe Security" + EXCEPTION + secex.getMessage());
                }catch(IOException ex) {
                        ex.printStackTrace();
                        form.append("subscribe IO" + EXCEPTION + ex.getMessage());
                }catch(Exception x){
                        x.printStackTrace();
                        form.append("subscribe OTHER" + EXCEPTION + x.getMessage());
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
        /**
         * Terminates the application.
         */
        public void shutdown() {
                destroyApp(false);
        }
}
