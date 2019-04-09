/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sip;

import java.util.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import javax.microedition.sip.*;

/**
 * ReceiveMessage: Implements simple message receiving using SIP via JSR-180.
 * It listens and receives SIP message requests in specified port, and responds
 * accordingly.  It uses shared mode when connecting to the sip server.
 * This example assumes that user already created the profile on the SDK's or
 * phone's sip setting, and uses sip profile to register with sip server.  The
 * sip public name for the SDK running SendMessge midlet is "sip:sip1@sipServer",
 * and the sip public name for the SDK running ReceivingMessage midlet is
 * "sip:sip2@sipServer".


 */
public class ReceiveMessage extends MIDlet implements CommandListener, SipServerConnectionListener  {

        private Display display;
        private long startTime;
        private Form form;
        private TextField receivePort;
        private Command receiveCmd;
        private Command exitCmd;
        SipConnectionNotifier scn = null;
        SipServerConnection ssc = null;

        //Protocol constants - headers
        private final String CONTENT_TYPE_HEADER = "Content-Type";
        private final String CONTENT_LENGTH_HEADER = "Content-Length";

        //Protocol constants - status
        private final int OK_STATUS = 200;

        //UI labels and messages
        private final String FORM_RECEIVE = "Receive Message";
        private final String START_CMD = "Start";
        private final String EXIT_CMD = "Exit";
        private final String FORM_LISTEN_MSG = "Listening... in port: ";
        private final String EXCEPTION = "Exception: ";

        /**
         * Creates a new ReceiveMessage object to handle the incoming messages.
         * It also provides UI for the user to enter the communication
         * information.
         */
        public ReceiveMessage() {
                display=Display.getDisplay(this);
                form = new Form(FORM_RECEIVE);
                form.append("Click start to listen on the port.");
                receiveCmd = new Command(START_CMD, Command.ITEM, 1);
                exitCmd = new Command(EXIT_CMD, Command.EXIT, 1);
                form.addCommand(receiveCmd);
                form.addCommand(exitCmd);
                form.setCommandListener(this);
        }

        /**
         * Creates a commandAction object with given command and diaplayable.
         * @param c Command the command used for the commandAction
         * @param d Displayable the displayable used when excuting the command
         */
        public void commandAction(Command c, Displayable d) {
                if(c == receiveCmd) {
                        Thread t = new Thread() {
                                public void run() {
                                        receiveMessage();
                                }
                        };
                        t.start();
                }else if(c == exitCmd) {
                        if(scn != null) {
                                try {
                                        scn.close();
                                }
                                catch(IOException iox) {
                                  form.append(EXCEPTION + iox.getMessage());
                                }
                        }
                        destroyApp(true);
                }else{}
        }

        /**
         * Signals the MIDlet that it has entered the Active state.  It also
         * invokes the application and makes the form visible.
         */
        public void startApp() {
                display.setCurrent(form);
        }
        /**
         * Listen to the messages and displays the port information on the form.
         */
        public void receiveMessage() {
                try {
                        if(scn != null)
                                scn.close();
                        form.append("Waiting for the connection.");
                        scn = (SipConnectionNotifier)Connector.open("sip:*;type=\"application/test\"");
                        scn.setListener(this);
                        form.append(FORM_LISTEN_MSG + scn.getLocalPort());
                } catch(Exception ex) {
                        ex.printStackTrace();
                        form.append("receive message "+ EXCEPTION + ex.getMessage());
                }
        }
        /**
         * Processes the requests based on contents.
         * Responds with 200 OK after getting the messages.
         * @param scn SipConnectionNotifier the SIP server connection notifier
         * object
         */
        public void notifyRequest(SipConnectionNotifier scn) {
                try {
                        ssc = scn.acceptAndOpen();
                        if(ssc.getMethod().equals("MESSAGE")) {
                                String contentType = ssc.getHeader(CONTENT_TYPE_HEADER );
                                String contentLength = ssc.getHeader(CONTENT_LENGTH_HEADER);
                                int length = Integer.parseInt(contentLength);
                                if((contentType != null) && contentType.equals("text/plain")) {
                                        InputStream is = ssc.openContentInputStream();
                                        int i=0;
                                        byte testBuffer[] = new byte[length];
                                        i = is.read(testBuffer);

                                        String tmp = new String(testBuffer, 0, i);

                                        StringItem st = new StringItem("Subject:", ssc.getHeader("Subject"));
                                        form.append(st);
                                        st = new StringItem("Message:", tmp);
                                        form.append(st);
                                }
                                ssc.initResponse(OK_STATUS);
                                ssc.send();
                        }

                } catch(IOException ex) {
                        form.append("notifyRequest IOException: "+ex.getMessage());
                }catch(Exception e){
                  form.append("notifyRequest other Exception: "+e.getMessage());
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







