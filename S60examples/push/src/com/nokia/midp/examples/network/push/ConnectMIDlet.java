/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.network.push;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;

/**
 *  Supplies a MIDlet with a mechanism to create a connection to
 *  a desired address.
 */
public class ConnectMIDlet extends MIDlet implements CommandListener, Runnable {
    /** String constant for the socket address */
    private static final String SOCKET_ADDRESS = "socket://localhost:";
    /** TextField size constant */
    private static final int TEXTFIELD_SIZE = 256;
    /** Command priority constant */
    private static final int COMMAND_PRIORITY = 1;
    /** Command for exit. */
    public static final Command CMD_EXIT = new Command("Exit", Command.EXIT, COMMAND_PRIORITY);
    /** Command to connect to a desired address */
    private static final Command CMD_CONNECT = new Command("Connect", Command.OK, COMMAND_PRIORITY);

    /** UI display. */
    private Display display;
    /** Form to display information to the user */
    private Form midletForm;
    /** TextField to enter url to connect to */
    private TextField textField;
    /** instance of SocketConnection */
    private SocketConnection socketConnection;

    /**
     *  Sets up the midlet and the items required for user interaction.
     */
    public void startApp() {
        if (display == null) {
            display = Display.getDisplay(this);
            midletForm = new Form("Push Example");
            textField = new TextField("Connect to:", SOCKET_ADDRESS, TEXTFIELD_SIZE, TextField.ANY);
            midletForm.append(textField);
            midletForm.addCommand(CMD_EXIT);
            midletForm.addCommand(CMD_CONNECT);
            midletForm.setCommandListener(this);
        }
        display.setCurrent(midletForm);
    }

    /**
     *  Must be defined but no implementation required as the midlet only responds
     *  to user interaction.
     */
    public void pauseApp() {
    }

    /**
     *  Must be defined but no implementation required as all the resources
     *  are released when the thread terminates.
     */
    public void destroyApp(boolean unconditional) {
    }

    /**
     *  Handles the commands on the UI.
     *
     *  @param cmd the command object identifying the command
     *  @param source the displayable on which the command has occurred
     */
    public void commandAction(Command cmd, Displayable source) {
        if (cmd == CMD_EXIT) {
            destroyApp(true);
            notifyDestroyed();
        } else if (cmd == CMD_CONNECT) {
            Thread t = new Thread(this);
            t.start();
        } else {
        }
    }

    /**
     *  Defines the run() method which all Runnable objects must do.
     *  Creates a SocketConnection to the address appearing in the
     *  TextField on the screen.
     */
    public void run() {
        try {
            String url = textField.getString();
            if (url.startsWith("socket")) {
                Connection c = Connector.open(url);
                if (c instanceof SocketConnection) {
                    // NB: URL must contain a hostname in order to create a client connection.
                    //     Omitting the hostname will create a server connection, which will
                    //     not work for this example.
                    socketConnection = (SocketConnection)c;
                    socketConnection.setSocketOption(SocketConnection.LINGER, 5);
                } else if (c instanceof ServerSocketConnection) {
                    // User omitted the hostname in the entered URL
                    displayAlert("incorrect URL: client socket URL must contain a hostname (e.g. \"localhost\")");
                    c.close();
                } else if (c != null) {
                    // User entered an invalid socket URL
                    displayAlert("incorrect URL: only sockets with user-defined ports are supported in this example");
                    c.close();
                }
            } else {
                displayAlert("incorrect URL, possibly network type not supported");
            }
        } catch (IllegalArgumentException iae) {
            //iae.printStackTrace();
            displayAlert("incorrect url, check and try again");
        } catch (ConnectionNotFoundException cnfe) {
            //cnfe.printStackTrace();
            displayAlert("no such address registered, check and try again");
        } catch (Exception e) {
            e.printStackTrace();
            displayAlert(e.getMessage());
        }
        finally {
            if (socketConnection != null) {
                try {
                    socketConnection.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /** Displays an alert with the desired text to the user. */
    private void displayAlert(String alertText) {
        Alert alert = new Alert("Info", alertText, null, AlertType.INFO);
        display.setCurrent(alert, midletForm);
    }
}
