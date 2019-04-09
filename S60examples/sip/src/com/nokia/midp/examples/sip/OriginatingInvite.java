/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sip;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.sip.*;

/**
 * OriginatingInvite: Implements a SIP messaging session initiator via JSR-180.
 * It establishes SIP session by sending "INVITE - ACK", and stops session by
 * sending "BYE". It uses shared mode when connecting to the sip server.
 * This example assumes that user already created the profile on the SDK's or
 * phone's sip setting, and uses sip profile to register with sip server.  The
 * sip public name for the SDK running originatingInvite is "sip:sip1@sipServer",
 * and the sip public name for the SDK running terminatingInvite is
 * "sip:sip2@sipServer".
 *
 */
public class OriginatingInvite extends MIDlet implements CommandListener, SipClientConnectionListener, SipServerConnectionListener {

        private Display display;
        private long startTime;
        private Form form;
        private TextField address;
        private Command startCmd;
        private Command restartCmd;
        private Command byeCmd;
        private Command exitCmd;
        private SipDialog dialog;
        private StringItem str;

        //States
        private final short S_OFFLINE = 0;
        private final short S_CALLING = 1;
        private final short S_RINGING = 2;
        private final short S_ONLINE = 3;

        //Protocol constants - headers:
        private final String FROM_HEADER = "From";
        private final String TO_HEADER="To";
        private final String CONTACT_HEADER = "Contact";
        private final String CONTENT_LENGTH_HEADER = "Content-Length";
        private final String CONTENT_TYPE_HEADER = "Content-Type";
        private final String ACCEPT_CONTACT_HEADER = "Accept-Contact";
        private final String CALL_ID_HEADER = "Call-ID";

        //Protocol constants - some header values
        private final String SDP_MIME_TYPE = "application/sdp";
        private final String ACCEPT_CONTACT = "*;type=\"" + SDP_MIME_TYPE + "\"";
        private final String DESTINATION_SIP_URI = "sip:sip2@host:5060";

        //Protocol constants - status
        private final int OK_STATUS = 200;
        private final int RING_STATUS = 180;
        private final int UNSUCCESS_STATUS = 400;
        private final int METHOD_NOT_ALLOWED_STATUS = 405;

        //Protocol constants - methods
        private final String INVITE_METHOD = "INVITE";
        private final String PRACK_METHOD = "PRACK";
        private final String BYE_METHOD = "BYE";

        //Timeouts values
        private final int TIME_OUT = 30000;
        private final int RECEIVE_TIMEOUT = 15000;

        //UI labels and messages
        private final String INVITE_LABEL = "INVITE";
        private final String BYE_COMMAND_LABEL = "Hang-up";
        private final String FORM_LABEL = "Session example";
        private final String RESTART_CMD_LABEL = "Restart";
        private final String START_CMD_LABEL = "Call...";
        private final String EXIT_CMD_LABEL = "Exit";
        private final String SCC_RES = "Response: ";
        private final String RING = "RINGING...\n";
        private final String DIALOG_LABEL = "Early-Dialog state: ";
        private final String DIALOG_STATE = "Dialog state: ";
        private final String SESSION_ESTABLISHED = "Session established: ";
        private final String SESSION_FAIL = "Session failed: ";
        private final String NO_ANSWER = "No answer: ";
        private final String HANG_UP = "user hang-up: ";
        private final String SESSION_CLOSE_NOTIFY = "Session closed successfully...";
        private final String ERROR = "Error: ";
        private final String EXCEPTION = "Exception: ";
        private final String NO_DIALOG_INFO = "No dialog information!";
        private final String SESSION_CANCEL = "Session canceled: ";
        private final String ERROR_CANCEL = "Error canceling the call...";
        private final String OTHER_HANG_UP = "Other side hang-up!";
        private final String CLOSE_NOTIFIER = "Closing notifier...";

        private short state = S_OFFLINE;
        private SipConnectionNotifier scn;
        private SipServerConnection ssc = null;
        private SipClientConnection scc = null;
        private String contact = null;

        // using static SDP content as an example
        private String sdp = "v=0\no=sippy 2890844730 2890844732 IN IP4 host.example.com\ns=example code\nc=IN IP4 host.example.com\nt=0 0\nm=message 54344 SIP/TCP\na=user:sippy";
        /**
         * Creates a new originatingInvite object.
         */
        public OriginatingInvite() {
                // Initialize MIDlet display
                display=Display.getDisplay(this);
                // create a Form for progess info printings
                form = new Form(FORM_LABEL);
                address = new TextField(INVITE_LABEL + ": ", DESTINATION_SIP_URI, 40, TextField.LAYOUT_LEFT);
                form.append(address);
                byeCmd = new Command(BYE_COMMAND_LABEL, Command.CANCEL, 1);
                restartCmd = new Command(RESTART_CMD_LABEL, Command.OK, 1);
                startCmd = new Command(START_CMD_LABEL, Command.OK, 1);
                form.addCommand(startCmd);
                exitCmd = new Command(EXIT_CMD_LABEL, Command.EXIT, 1);
                form.addCommand(exitCmd);
                form.setCommandListener(this);
        }

        /**
         * Creates a commandAction object with given command and displayable.
         * @param c Command the command used for the commandAction
         * @param d Displayable the displayable used when excuting the command
         */
        public void commandAction(Command c, Displayable d) {
                if(c == startCmd) {
                        form.deleteAll();
                        form.removeCommand(startCmd);
                        form.addCommand(byeCmd);

                        state = S_CALLING;
                        Thread t = new Thread() {
                                public void run() {
                                        startSession();
                                }
                        };
                        t.start();
                        return;
                }
                else if(c == exitCmd) {
                        destroyApp(true);
                        return;
                }
                else if(c == byeCmd) {
                        if(state == S_RINGING) {
                                sendCANCEL();
                        } else {
                                sendBYE();
                        }
                        form.removeCommand(byeCmd);
                        form.addCommand(restartCmd);
                        return;
                }
                else if(c == restartCmd) {
                        stopListener();
                        form.removeCommand(restartCmd);
                        form.addCommand(startCmd);
                        form.deleteAll();
                        form.append(address);
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

        private void startSession() {
                try {
                        state = S_CALLING;
                        // start a listener for incoming requests
                        startListener();
                        form.append("Waiting for the connection.");
                        //  SIP connection with remote user
                        scc = (SipClientConnection)
                        Connector.open(address.getString());
                        scc.setListener(this);
                        // initialize INVITE request
                        scc.initRequest(INVITE_METHOD, scn);
                        scc.setHeader(ACCEPT_CONTACT_HEADER, ACCEPT_CONTACT);
                        scc.setHeader(CONTACT_HEADER,contact);
                        scc.setHeader(CONTENT_LENGTH_HEADER, ""+sdp.length());
                        scc.setHeader(CONTENT_TYPE_HEADER, SDP_MIME_TYPE);
                        OutputStream os = scc.openContentOutputStream();
                        os.write(sdp.getBytes());
                        os.close(); // close and send
                        str = new StringItem(INVITE_LABEL + "...", scc.getHeader(TO_HEADER));
                        form.append(str);
                } catch(Exception ex) {
                        ex.printStackTrace();
                        form.append(EXCEPTION + ex.getMessage());
                }
        }

        /**
         * Processes incoming responses sent from the other party based on its
         * contents.
         * @param SipClientConnection the object represents a SIP client
         * transaction.
         */
        public void notifyResponse(SipClientConnection scc) {
                int statusCode = 0;
                boolean received = false;
                try {
                        scc.receive(0); // fetch response
                        statusCode = scc.getStatusCode();
                        str = new StringItem(SCC_RES, statusCode+" "+scc.getReasonPhrase());
                        form.append(str);
                        if(statusCode < OK_STATUS) {
                                dialog = scc.getDialog();
                                form.append(DIALOG_LABEL+dialog.getState()+"\n");
                                if(statusCode == RING_STATUS){
                                  state = S_RINGING;
                                  form.append(RING);
                                }
                        }
                        if(statusCode == OK_STATUS) {
                                String contentType = scc.getHeader(CONTENT_TYPE_HEADER);
                                String contentLength = scc.getHeader(CONTENT_LENGTH_HEADER);
                                int length = Integer.parseInt(contentLength);
                                if(contentType.equals(SDP_MIME_TYPE)) {
                                        form.append("sdp content received.");
                                }
                                form.append(DIALOG_STATE+dialog.getState());
                                scc.initAck(); // initialize and send ACK
                                scc.setHeader(ACCEPT_CONTACT_HEADER, ACCEPT_CONTACT);
                                scc.send();
                                dialog = scc.getDialog(); // save dialog info
                                str = new StringItem(SESSION_ESTABLISHED, scc.getHeader(CALL_ID_HEADER));
                                form.append(str);
                                state = S_OFFLINE;
                        }else if(statusCode >= UNSUCCESS_STATUS){
                                str = new StringItem(SESSION_FAIL,
                                scc.getHeader(CALL_ID_HEADER));
                                form.append(str);
                                form.removeCommand(byeCmd);
                                form.addCommand(restartCmd);
                                scc.close();
                                state = S_OFFLINE;

                        }
                } catch(IOException ioe) {
                        // handle e.g. transaction timeout here
                        str = new StringItem(NO_ANSWER, ioe.getMessage());
                        form.append(str);
                        form.removeCommand(byeCmd);
                        form.addCommand(restartCmd);
                }catch(Exception e){
                }
        }

        // Ends session with "BYE".
        private void sendBYE() {
                if(dialog != null) {
                        try {
                                SipClientConnection sc = dialog.getNewClientConnection(BYE_METHOD);
                                sc.setHeader(ACCEPT_CONTACT_HEADER, ACCEPT_CONTACT);
                                sc.send();
                                str = new StringItem(HANG_UP, BYE_METHOD + " sent...");
                                form.append(str);
                                boolean gotit = sc.receive(RECEIVE_TIMEOUT);
                                if(gotit) {
                                        if(sc.getStatusCode() == OK_STATUS) {
                                                form.append(SESSION_CLOSE_NOTIFY);
                                                form.append(DIALOG_STATE + dialog.getState());
                                        }
                                        else
                                                form.append(ERROR + sc.getReasonPhrase());
                                }
                                sc.close();
                                state = S_OFFLINE;
                        } catch(IOException iox) {
                                form.append(EXCEPTION + iox.getMessage());
                        }
                } else {
                        form.append(NO_DIALOG_INFO);
                }
        }

        private void sendCANCEL() {
                if(scc != null) {
                        try {
                                SipClientConnection cancel = scc.initCancel();
                                cancel.send();
                                if(cancel.receive(TIME_OUT)) {
                                        str = new StringItem(SESSION_CANCEL, cancel.getReasonPhrase());
                                        form.append(str);
                                        state = S_OFFLINE;
                                } else {
                                        form.append(ERROR_CANCEL);
                                }
                        } catch(Exception ex) {
                                ex.printStackTrace();
                                form.append(EXCEPTION + ex.getMessage());
                        }
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

        /**
         * Terminates the application.
         */
        public void shutdown() {
                destroyApp(false);
        }



        /**
         * Processes the requests based on contents.
         * @param scn SipConnectionNotifier the SIP server connection notifier
         * object
         */
        public void notifyRequest(SipConnectionNotifier sn) {
                try {
                        ssc = scn.acceptAndOpen(); // blocking
                        if(ssc.getMethod().equals(BYE_METHOD)) {
                                // respond 200 OK to BYE
                                ssc.initResponse(OK_STATUS);
                                ssc.send();
                                str = new StringItem(OTHER_HANG_UP, "");
                                form.append(str);
                                form.append(CLOSE_NOTIFIER);
                                form.removeCommand(byeCmd);
                                form.addCommand(restartCmd);
                                scn.close();
                                state = S_OFFLINE;
                        } else {
                                if(ssc.getMethod().equals(PRACK_METHOD)) {
                                        ssc.initResponse(OK_STATUS);
                                        ssc.send();
                                } else {
                                        // 405 Method Not Allowed
                                        ssc.initResponse(METHOD_NOT_ALLOWED_STATUS);
                                        ssc.send();
                                }
                        }
                } catch(IOException ex) {
                        form.append(EXCEPTION + ex.getMessage());
                }
        }


        private void startListener() {
                try {
                        if(scn != null)
                                scn.close();
                        scn = (SipConnectionNotifier) Connector.open("sip:*;type=\"" + SDP_MIME_TYPE + '"');
                        contact = new String("sip:sip1@" +
                                             scn.getLocalAddress() +
                                             ":" + scn.getLocalPort());
                        scn.setListener(this);
                } catch(IOException ex) {
                        form.append(EXCEPTION + ex.getMessage());
                }
        }

        private void stopListener() {
                try {
                        if(scn != null)
                                scn.close();
                        scn = null;
                } catch(IOException ex) {
                        form.append(EXCEPTION + ex.getMessage());
                }
        }
}







