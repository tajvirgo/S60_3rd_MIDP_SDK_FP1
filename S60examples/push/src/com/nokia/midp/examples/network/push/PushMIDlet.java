/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.network.push;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import java.util.*;
import java.io.*;

/**
 *  Supplies a MIDlet that can be activated in response to a connection,
 *  timer based alarm or manually by the user. Provides methods to set and
 *  unset a timer based alarm and register and unregister a connection.
 */
public class PushMIDlet extends MIDlet implements CommandListener, Runnable {
    /** constant string representing socket connection */
    private static final String SOCKET_STRING = "socket://:";
    /** textfield size constant */
    private static final int TEXTFIELD_SIZE = 256;
    /** Command priority constant */
    private static final int COMMAND_PRIORITY = 1;
    /** Command for exit */
    private static final Command CMD_EXIT = new Command("Exit", Command.EXIT, COMMAND_PRIORITY);
    /** Command to set the alarm */
    private static final Command CMD_SET = new Command("Set Alarm", Command.OK, COMMAND_PRIORITY);
    /** Command to unset the alarm */
    private static final Command CMD_UNSET = new Command("Unset Alarm", Command.OK, COMMAND_PRIORITY);
    /** Command to switch to the connection edit screen */
    private static final Command CMD_EDITCONN = new Command("Dynamic Connection", Command.OK, COMMAND_PRIORITY);
    /** Command to register a connection */
    private static final Command CMD_REGISTER = new Command("Register", Command.OK, COMMAND_PRIORITY);
    /** Command to unregister a connection */
    private static final Command CMD_UNREGISTER = new Command("Unregister", Command.OK, COMMAND_PRIORITY);
    /** Command to cancel and return to previous screen */
    private static final Command CMD_CANCEL = new Command("Cancel", Command.OK, COMMAND_PRIORITY);
    /** Time until MIDlet is restarted once it's been terminated */
    private static final long MILLISECONDS_TILL_ALARM = 1000 * 30;

    /** UI display */
    private Display display;
    /** Form to display information to the user */
    private Form midletForm;
    /** Form to register or unregister a connection */
    private Form registerForm;
    /** StringItem to identify how midlet was started */
    private StringItem startedBy;
    /** StringItem to display the currently registered push connections */
    private StringItem connectionList;
    /** StringItem to identify if the alarm is set or not */
    private StringItem alarmIndicator;
    /** TextField for entering a url to be registered or unregistered */
    private TextField textField;
    /** flag to indicate if an alarm has been registered */
    private boolean alarmSet = false;
    /** String to identify this class */
    private String classname;
    /** instance of a SocketConnection */
    private SocketConnection socketConnection = null;
    /** instance of ServerSocketConnection */
    private ServerSocketConnection serverConnection = null;
    /** flag indicating if the connection is to be registered or unregistered */
    private boolean register = false;
    /** flag indicating to the thread that a connection is to be registered or unregistered */
    private boolean registerConnNow = false;
    /** flag indicating if the thread is to terminate */
    private boolean isRunning = true;
    /** flag indicating to the thread that the alarm is to be set */
    private boolean setAlarmNow = false;

    /**
     *  Sets up the MIDlet and starts the thread running.
     *  Sets up the display with StringItems and Commands for user interaction
     *  and starts the thread running.
     */
    public void startApp() {
        if (display == null) {
            classname = this.getClass().getName();

            display = Display.getDisplay(this);
            midletForm = new Form("Push Example");
            startedBy = new StringItem("Started by:","");
            startedBy.setLayout(Item.LAYOUT_NEWLINE_BEFORE);
            connectionList = new StringItem("Current Push List", "");
            connectionList.setLayout(Item.LAYOUT_NEWLINE_BEFORE);
            alarmIndicator = new StringItem("Alarm", "not set");
            alarmIndicator.setLayout(Item.LAYOUT_NEWLINE_BEFORE);
            midletForm.append(startedBy);
            midletForm.append(connectionList);
            midletForm.append(alarmIndicator);
            midletForm.addCommand(CMD_EXIT);
            midletForm.addCommand(CMD_SET);
            midletForm.addCommand(CMD_UNSET);
            midletForm.addCommand(CMD_EDITCONN);
            midletForm.setCommandListener(this);

            registerForm = new Form("Push Example");
            textField = new TextField("Connection:",SOCKET_STRING, TEXTFIELD_SIZE, TextField.ANY);
            registerForm.append(textField);
            registerForm.addCommand(CMD_REGISTER);
            registerForm.addCommand(CMD_UNREGISTER);
            registerForm.addCommand(CMD_CANCEL);
            registerForm.addCommand(CMD_EXIT);
            registerForm.setCommandListener(this);
        }
        display.setCurrent(midletForm);

        Thread t = new Thread(this);
        t.start();
    }

    /** No implementation required. */
    public void pauseApp() {
    }

    /**
     *  Destroys the application resources. The alarm is set if required, the resources destroyed
     *  and the thread terminated.
     */
    public void destroyApp(boolean unconditional) {
        if (alarmSet) {
            setAlarmNow = true;
            synchronized (this) {
                notify();
            }
        }
        exit();
        stopThread();
    }

    /**
     *  Handles the commands on the UI.
     *
     *  @param cmd the command object identifying the command
     *  @param source the displayable on which the command has occured
     */
    public void commandAction(Command cmd, Displayable source) {
        if (cmd == CMD_EXIT) {
            destroyApp(true);
            notifyDestroyed();
        } else if (cmd == CMD_SET) {
            alarmSet = true;
            alarmIndicator.setText("set to start 30 seconds after exiting");
        } else if (cmd == CMD_UNSET) {
            alarmSet = false;
            alarmIndicator.setText("not set");
        } else if (cmd == CMD_REGISTER) {
            register = true;
            registerConnNow = true;
            synchronized (this) {
                notify();
            }
        } else if (cmd == CMD_UNREGISTER) {
            register = false;
            registerConnNow = true;
            synchronized (this) {
                notify();
            }
        } else if (cmd == CMD_EDITCONN) {
            textField.setString(SOCKET_STRING);
            display.setCurrent(registerForm);
        } else if (cmd == CMD_CANCEL) {
            display.setCurrent(midletForm);
        } else {
            //functionality to deal with other commands
        }
    }

    /**
     *  Defines the run() method which all objects implementing the Runnable
     *  interface must do. If the MIDlet was started by a connection the
     *  connection is accepted and dealt with. The display is updated with
     *  an indication of how the MIDlet was started followed by a list of the
     *  registered connections and an indication of whether the alarm is set.
     *  A while loop is then entered to wait for connections to be registered
     *  and unregistered or the alarm to be set.
     */
    public void run() {
        try {
            String[] connections = PushRegistry.listConnections(true);
            if (connections != null && connections.length > 0) {
                for (int i = 0; i < connections.length; i++ ) {
                    startedBy.setText("connection: " + connections[i]);
                    serverConnection = (ServerSocketConnection)Connector.open(connections[i]);
                    socketConnection = (SocketConnection)serverConnection.acceptAndOpen();
                    socketConnection.close();
                    serverConnection.close();
                }
            } else {
                startedBy.setText("alarm or manually by user");
            }
            displayConnections();
        } catch (Exception e) {
            e.printStackTrace();
            displayAlert(e.getMessage());
        }
        while (isRunning) {
            synchronized (this) {
                if (!setAlarmNow && !registerConnNow) {
                    try {
                        wait();
                    } catch (Exception e) {
                    }
                }
            }
            synchronized (this) {
                if (setAlarmNow) {
                    setAlarm();
                    setAlarmNow = false;
                }
            }
            if (registerConnNow) {
                regConnection();
                registerConnNow = false;
            }
        }
    }

    /**
     *  Registers or unregisters a connection depending on the boolean flag.
     *  After registering or unregistering the connection the user is returned
     *  to the midlet's main display and updates the list of current connections.
     */
    public void regConnection() {
        try {
            String url = textField.getString();
            if (!url.equals("socket://:") && !url.equals("socket://") && url.startsWith("socket")) {
                if (register) {
                    PushRegistry.registerConnection(url, classname, "*");
                } else {
                    PushRegistry.unregisterConnection(url);
                }
                display.setCurrent(midletForm);
                displayConnections();
            } else {
                displayAlert("incorrect URL: only sockets with user-defined ports are supported in this example");
            }
        } catch (IllegalArgumentException iae) {
            displayAlert("not a valid address");
        } catch (IOException ioe) {
            displayAlert("connection already registered");
        } catch (Exception e) {
            e.printStackTrace();
            displayAlert(e.getMessage());
        }
    }

    /**
     *  Handles the registering of an alarm.
     */
    public synchronized void setAlarm() {
        try {
            Date d = new Date();
            long previousAlarmTime = PushRegistry.registerAlarm(classname,
                                            d.getTime() + MILLISECONDS_TILL_ALARM);
        } catch (Exception e) {
            e.printStackTrace();
            displayAlert(e.getMessage());
        }
    }

    /**
     *  Creates a list of all the registered connections.
     */
    public void displayConnections() {
        String[] allConnections = PushRegistry.listConnections(false);
        StringBuffer strBuff = new StringBuffer("");
        if (allConnections != null && allConnections.length > 0) {
            for (int i = 0; i < allConnections.length; i++ ) {
                strBuff.append(allConnections[i]);
                strBuff.append("\n");
            }
        }
        connectionList.setText(strBuff.toString());
    }

    /**
     *  Displays an alert to the user with the desired text.
     */
    public void displayAlert(String alertText) {
        Alert alert = new Alert("Info", alertText, null, AlertType.INFO);
        display.setCurrent(alert, registerForm);
    }

    /** Terminates the thread. */
    public synchronized void stopThread() {
        isRunning = false;
    }

    /** Releases all resources if they have not already been. */
    public void exit() {
        if(socketConnection != null) {
            try {
                socketConnection.close();
            } catch (Exception e) {
            }
        }
        if (serverConnection != null) {
            try {
                serverConnection.close();
            } catch (Exception e) {
            }
        }
    }
}
