/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sip;


import java.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.sip.*;

/**
 * TerminatingInvite: Implements a SIP messaging session listener via JSR-180.
 * It answers to SIP INVITE request by sending "180 Ringing" and "200 OK".  Stops
 * session by sending "BYE".  It uses shared mode when connecting to the sip server.
 * This example assumes that user already created the profile on the SDK's or
 * phone's sip setting, and uses sip profile to register with sip server.  The
 * sip public name for the SDK running originatingInvite is "sip:sip1@sipServer",
 * and the sip public name for the SDK running terminatingInvite is
 * "sip:sip2@sipServer".
 */
public class TerminatingInvite extends MIDlet implements CommandListener, SipServerConnectionListener  {

        private Display display;
        private long startTime;
        private Form form;
        private TextField receivePort;
        private Command startCmd;
        private Command restartCmd;
        private Command byeCmd;
        private Command answerCmd;
        private Command cancelCmd;
        private Command exitCmd;
        private boolean active = false;
        private SipDialog dialog;
        private StringItem str;
        private SipServerConnection ssc = null; // latest request
        private SipServerConnection origSSC = null; // original request

        //States
        private final short S_OFFLINE = 0;
        private final short S_CALLING = 1;
        private final short S_RINGING = 2;
        private final short S_ONLINE = 3;

        //Protocol constants - headers
        private final String CALL_ID_HEADER = "Call-ID";
        private final String ACCEPT_CONTACT_HEADER = "Accept-Contact";

        //Protocol constants - some header values
        private final String SDP_MIME_TYPE = "application/sdp";

        //Protocol constants - status
        private final int OK_STATUS = 200;
        private final int BUSY_STATUS = 486;
        private final int REQUEST_TERMINATED_STATUS = 487;
        private final int RING_STATUS = 180;

         //Protocol constants - methods
        private final String BYE_METHOD = "BYE";

        //Timeouts values
        private final int RECEIVE_TIMEOUT = 10000;

        //UI labels and messages:
        private final String HANG_UP = "user hang-up: ";
        private final String SESSION_CLOSE_NOTIFY = "Session closed successfully...";
        private final String SESSION_CANCEL = "Session canceled: ";
        private final String SIP_PORT_LABEL = "SipConnectionNotifier on port:";
        private final String EXCEPTION = "Exception: ";

        private final String SIP_PORT = "sip:5060";

        private short state = S_OFFLINE;

        // using static SDP (Session Description Protocol) as an example
        private String sdp = "v=0\no=sippy 2890844730 2890844732 IN IP4 host.example.com\ns=example code\nc=IN IP4 host.example.com\nt=0 0\nm=message 54344 SIP/TCP\na=user:sippy";
        SipConnectionNotifier scn = null;

       /**
        * Creates a new TerminatingInvite object with UI for the user to
        * enter connection information.
        */
       public TerminatingInvite() {
                // Initialize MIDlet display
                display=Display.getDisplay(this);
                form = new Form("Session example");
                receivePort = new TextField(SIP_PORT_LABEL, SIP_PORT, 30, TextField.LAYOUT_LEFT);
                form.append("Click online to listen on the port.");
                answerCmd = new Command("Answer", Command.OK, 1);
                byeCmd = new Command("Hang-up", Command.CANCEL, 1);
                restartCmd = new Command("Restart", Command.OK, 1);
                startCmd = new Command("Online", Command.OK, 1);
                form.addCommand(startCmd);
                exitCmd = new Command("Exit", Command.EXIT, 2);
                form.addCommand(exitCmd);
                form.setCommandListener(this);

        }
        /**
         * Creates a commandAction object with given command and diaplayable.
         * @param c Command the command used for the commandAction
         * @param d Displayable the displayable used when excuting the command
         */
        public void commandAction(Command c, Displayable d) {
                if(c == startCmd) {
                        form.deleteAll();
                        form.removeCommand(startCmd);

                        Thread t = new Thread() {
                                public void run() {
                                        startListener();
                                }
                        };
                        t.start();

                        return;
                }
                else if(c == exitCmd) {
                        if(scn != null) {
                                try {
                                        scn.close();
                                }
                                catch(IOException iox) {
                                  form.append(EXCEPTION + iox.getMessage());
                                }
                        }
                        destroyApp(true);
                        return;
                }

                else if(c == byeCmd) {
                        if(state == S_RINGING) {
                                try {
                                        ssc.initResponse(BUSY_STATUS); // Busy here
                                        ssc.send();
                                        str = new StringItem("Session closed: ", "Buzy here!");
                                        form.append(str);
                                } catch(Exception ex) {
                                        ex.printStackTrace();
                                        form.append(EXCEPTION + ex.getMessage());
                                }
                        } else {
                                sendBYE();
                        }
                        form.removeCommand(byeCmd);
                        form.removeCommand(answerCmd);
                        form.addCommand(restartCmd);
                        state = S_OFFLINE;
                }
                else if(c == answerCmd) {
                        form.removeCommand(answerCmd);
                        form.addCommand(byeCmd);
                        try {
                                ssc.initResponse(OK_STATUS);
                                ssc.setHeader("Content-Length", ""+sdp.length());
                                ssc.setHeader("Content-Type", "application/sdp");
                                OutputStream os = ssc.openContentOutputStream();
                                os.write(sdp.getBytes());
                                os.close(); // close and send
                                // save Dialog
                                dialog = ssc.getDialog();
                                form.append("Dialog state: " + dialog.getState()+"\n");

                                // Wait for otherside to ACK
                                form.append("Waiting for ACK...");
                        } catch(Exception ex) {
                                ex.printStackTrace();
                                form.append(EXCEPTION + ex.getMessage());
                        }
                }
                else if(c == restartCmd) {
                        if(scn != null) {
                                try {
                                        scn.close();
                                } catch(Exception ex) {
                                  form.append(EXCEPTION + ex.getMessage());
                                }
                        }
                        form.removeCommand(restartCmd);
                        form.addCommand(startCmd);
                        form.deleteAll();
                        form.append(receivePort);
                        return;
                }
        }
        /**
         * Signals the MIDlet that it has entered the Active state.  It also
         * invokes the application and makes the form visible.
         */
        public void startApp() {
                display.setCurrent(form);
        }

        private void startListener() {

                try {
                        if(scn != null)
                                scn.close();
                        // start a listener for incoming request
                        form.append("Waiting for the connection.");
                        scn = (SipConnectionNotifier) Connector.open("sip:*;type=\"" + SDP_MIME_TYPE + '"');
                        scn.setListener(this);
                        form.append("Listening on port: "+scn.getLocalPort());
                } catch(Exception ex) {
                        ex.printStackTrace();
                        form.append(EXCEPTION + ex.getMessage());
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
                        ssc = scn.acceptAndOpen(); // blocking
                        if(ssc.getMethod().equals("INVITE")) {
                                origSSC = ssc;
                                state = S_CALLING;
                                // handle content sdp
                                String contentType = ssc.getHeader("Content-Type");
                                String contentLength = ssc.getHeader("Content-Length");
                                int length = Integer.parseInt(contentLength);

                                if(contentType.equals("application/sdp")) {
                                        InputStream is = ssc.openContentInputStream();
                                        byte content[] = new byte[length];
                                        is.read(content);
                                        String sc = new String(content);
                                        int m = sc.indexOf("m=");
                                        String media = sc.substring(m, sc.indexOf('\n', m));
                                        str = new StringItem("media is: ", media);
                                        form.append(str);
                                        // initialize and send 180 response
                                        ssc.initResponse(RING_STATUS);
                                        ssc.send();
                                        // save Dialog
                                        dialog = ssc.getDialog();
                                        form.append("Dialog state: " + dialog.getState()+"\n");
                                        form.addCommand(answerCmd);
                                        form.addCommand(byeCmd);
                                        // inform user about the session here...
                                        form.append("RINGING!!!\n");
                                        state = S_RINGING;
                                        return;
                                }

                        }
                        else if(ssc.getMethod().equals("ACK")) {
                                str = new StringItem("Session established: ", ssc.getHeader(CALL_ID_HEADER));
                                state = S_ONLINE;
                                form.append(str);
                                dialog = ssc.getDialog();
                                form.append("Dialog state: " + dialog.getState()+"\n");
                                // Wait for otherside to send BYE
                                form.append("Waiting for BYE...");
                        }
                        else if(ssc.getMethod().equals(BYE_METHOD)) {
                                ssc.initResponse(OK_STATUS);
                                ssc.send();
                                state = S_OFFLINE;
                                str = new StringItem("Session closed: ", ssc.getHeader(CALL_ID_HEADER));
                                form.append(str);
                                form.append("Dialog state: " + dialog.getState());
                                ssc.close();
                                form.removeCommand(byeCmd);
                                form.removeCommand(answerCmd);
                                form.addCommand(restartCmd);
                        }else if(ssc.getMethod().equals("CANCEL")) {
                                ssc.initResponse(OK_STATUS);
                                ssc.send();
                                origSSC.initResponse(REQUEST_TERMINATED_STATUS);
                                origSSC.send();
                                state = S_OFFLINE;
                                str = new StringItem(SESSION_CANCEL, ssc.getHeader(CALL_ID_HEADER));
                                form.append(str);
                                form.removeCommand(byeCmd);
                                form.removeCommand(answerCmd);
                                form.addCommand(restartCmd);
                        }else{}
                      } catch(IOException iox) {
                              iox.printStackTrace();
                              form.append("IO"+EXCEPTION + iox.getMessage());
                      } catch(Exception ex) {
                        ex.printStackTrace();
                        form.append("Other"+EXCEPTION + ex.getMessage());
                      }
        }

        private void sendBYE() {
                if(dialog != null) {
                        try {
                                SipClientConnection sc = dialog.getNewClientConnection(BYE_METHOD);
                                sc.setHeader(ACCEPT_CONTACT_HEADER,
                                             "*;type=\"" + SDP_MIME_TYPE + "\"");
                                sc.send();
                                str = new StringItem(HANG_UP, BYE_METHOD + " sent...");
                                form.append(str);

                                boolean gotit = sc.receive(RECEIVE_TIMEOUT);
                                if(gotit) {
                                        if(sc.getStatusCode() == OK_STATUS) {
                                                form.append(SESSION_CLOSE_NOTIFY);
                                                state = S_OFFLINE;
                                        }
                                        else
                                                form.append("Error: "+sc.getReasonPhrase());
                                }
                                form.append("Dialog state: " + dialog.getState());
                                sc.close();
                        } catch(IOException iox) {
                                form.append("Exception: "+iox.getMessage());
                        }
                } else {
                        form.append("No dialog information!");
                }
        }

        /**
         * Signals the MIDlet to enter the Paused state.
         */
        public void pauseApp() {
                // pause
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







