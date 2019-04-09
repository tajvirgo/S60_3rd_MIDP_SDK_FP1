/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.btl2capecho;

import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import com.nokia.midp.examples.btl2capecho.server.ServerConnectionHandler;
import com.nokia.midp.examples.btl2capecho.server.ServerConnectionHandlerListener;

/**
 * This class provides a midlet form for the server functionality of
 * the example.
 */

class ServerForm
    extends Form
    implements ServerConnectionHandlerListener, CommandListener
{
    private final MIDletApplication midlet;
    private final StringItem numConnectionsField;
    private final TextField sendDataField;
    private final StringItem receivedDataField;
    private final StringItem statusField;
    private final Command sendCommand;
    private final Command addConnectionCommand;
    private final Command searchCommand;
    private final Command logCommand;
    private final Command quitCommand;
    private final Command clearStatusCommand;
    private final Vector handlers;

    private int maxConnections;
    private StringItem btAddressField = null;
    private volatile int numReceivedMessages = 0;
    private volatile int numSentMessages = 0;
    private int sendMessageId = 0;


    ServerForm(MIDletApplication midlet)
    {
        super("Server");
        this.midlet = midlet;
        handlers = new Vector();

        String value =
            LocalDevice.getProperty(
                            "bluetooth.connected.devices.max");
        try
        {
            maxConnections = Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            maxConnections = 0;
        }


        // 1. add Form items
        try
        {
            String address = LocalDevice.getLocalDevice()
                                        .getBluetoothAddress();
            btAddressField = new StringItem("My address", address);
            append(btAddressField);
        }
        catch (BluetoothStateException e)
        {
            // nothing we can do, don't add field
        }
        numConnectionsField = new StringItem("Connections", "0");
        append(numConnectionsField);
        statusField = new StringItem("Status", "");
        append(statusField);
        sendDataField = new TextField("Send data",
                                      "Server says: 'Hello, world.'",
                                      64,
                                      TextField.ANY);
        append(sendDataField);
        receivedDataField = new StringItem("Last received data",
                                           null);
        append(receivedDataField);


        // 2. create commands
        sendCommand = new Command("Send", Command.SCREEN, 1);
        searchCommand = new Command("Search for more",
                                    Command.SCREEN,
                                    1);
        addConnectionCommand = new Command("Add connection",
                                           Command.SCREEN,
                                           2);
        logCommand = new Command("View log", Command.SCREEN, 3);
        clearStatusCommand = new Command("Clear status", Command.SCREEN, 4);
        quitCommand = new Command("Quit", Command.EXIT, 1);


        // 3. add commands and set command listener
        addCommand(searchCommand);
        addCommand(addConnectionCommand);
        addCommand(logCommand);
        addCommand(clearStatusCommand);
        addCommand(quitCommand);
        // The 'sendCommand' is added later to screen,
        // if at least one connection is open.
        setCommandListener(this);
    }


    public void makeConnections(Vector serviceRecords, int security)
    {
        for (int i=0; i < serviceRecords.size(); i++)
        {
            ServiceRecord serviceRecord =
                (ServiceRecord) serviceRecords.elementAt(i);
            boolean found = false;
            for (int j=0; j < handlers.size(); j++)
            {
                ServerConnectionHandler old =
                    (ServerConnectionHandler) handlers.elementAt(j);
                String oldAddr = old.getServiceRecord().
                                     getHostDevice().
                                     getBluetoothAddress();
                String newAddr =
                       serviceRecord.getHostDevice()
                                    .getBluetoothAddress();
                if (oldAddr.equals(newAddr))
                {
                     found = true;
                     break;
                }
            }
            if (!found)
            {
                ServerConnectionHandler newHandler =
                    new ServerConnectionHandler(
                            this,
                            serviceRecord,
                            security);
                newHandler.start(); // start reader & writer
            }
        }
    }


    private void removeHandler(ServerConnectionHandler handler)
    {
        if (handlers.contains(handler))
        {
            handlers.removeElement(handler);
            String str = Integer.toString(handlers.size());
            numConnectionsField.setText(str);
            if (handlers.size() == 0)
            {
                removeCommand(sendCommand);
                addCommand(searchCommand);
            }
        }
    }


    void closeAll()
    {
        for (int i=0; i < handlers.size(); i++)
        {
            ServerConnectionHandler handler =
                (ServerConnectionHandler) handlers.elementAt(i);
            handler.close();
            removeHandler(handler);
        }
    }


    public void commandAction(Command cmd, Displayable disp)
    {
        if (cmd == addConnectionCommand)
        {
            Vector v = new Vector();
            for (int i=0; i < handlers.size(); i++)
            {
                ServerConnectionHandler handler =
                    (ServerConnectionHandler) handlers.elementAt(i);
                String btAddress = handler.getServiceRecord()
                                       .getHostDevice()
                                       .getBluetoothAddress();
                v.addElement(btAddress);
            }
            midlet.serverFormAddConnection(v);
        }
        else if (cmd == clearStatusCommand)
        {
            statusField.setText("");
        }
        else if (cmd == logCommand)
        {
            midlet.serverFormViewLog();
        }
        else if (cmd == sendCommand)
        {
            String sendData = sendDataField.getString();
            Integer id = new Integer(sendMessageId++);

            for (int i=0; i < handlers.size(); i++)
            {
                ServerConnectionHandler handler =
                    (ServerConnectionHandler) handlers.elementAt(i);
                try
                {
                    handler.queueMessageForSending(
                                id,
                                sendData.getBytes());
                    statusField.setText(
                                    "Queued a send message request");
                }
                catch(IllegalArgumentException e)
                {
                    // Message length longer than
                    // ServerConnectionHandler.MAX_MESSAGE_LENGTH

                    String errorMessage =
                        "IllegalArgumentException while trying " +
                        "to send a message: " + e.getMessage();
                    handleError(handler, errorMessage);
                }
            }
        }
        else if (cmd == searchCommand)
        {
            midlet.serverFormSearchRequest(handlers.size());
        }
        else if (cmd == quitCommand)
        {
            closeAll();
            midlet.serverFormExitRequest();
        }

        // To keep this MIDlet simple, I didn't add any way
        // to drop individual connections. But you might
        // want to do so.
    }


    // ServerConnectionHandlerListener interface methods

    public void handleOpen(ServerConnectionHandler handler)
    {
        handlers.addElement(handler);
        // for the first open connection
        if (handlers.size() == 1)
        {
            removeCommand(searchCommand);

            removeCommand(sendCommand);
            addCommand(sendCommand);
        }

        // Remove the 'Add connection' command
        // when the device already has open the
        // maximum number of connections it can
        // support.
        if (handlers.size() >= maxConnections)
        {
            removeCommand(addConnectionCommand);
        }


        statusField.setText("Connection opened");
        String str = Integer.toString(handlers.size());
        numConnectionsField.setText(str);
    }


    public void handleOpenError(
                    ServerConnectionHandler handler,
                    String errorMessage)
    {
        statusField.setText("Error opening outbound connection: " +
                            errorMessage);
    }


    public void handleReceivedMessage(
                    ServerConnectionHandler handler,
                    byte[] messageBytes)
    {
        numReceivedMessages++;

        String message = new String(messageBytes);
        receivedDataField.setText(message);

        statusField.setText(
            "# messages read: " + numReceivedMessages + " " +
            "sent: " + numSentMessages);

        // Broadcast message to all clients
        for (int i=0; i < handlers.size(); i++)
        {
            ServerConnectionHandler h =
                (ServerConnectionHandler) handlers.elementAt(i);

            Integer id = new Integer(sendMessageId++);
            try
            {
                h.queueMessageForSending(id, messageBytes);
            }
            catch (IllegalArgumentException e)
            {
                String errorMessage =
                    "IllegalArgumentException while trying to " +
                    "send message: " + e.getMessage();
                handleError(handler, errorMessage);
            }
        }
    }


    public void handleQueuedMessageWasSent(
                    ServerConnectionHandler handler,
                    Integer id)
    {
        numSentMessages++;
        statusField.setText("# messages read: " +
                            numReceivedMessages + " " +
                            "sent: " + numSentMessages);
    }


    public void handleClose(ServerConnectionHandler handler)
    {
        removeHandler(handler);
        if (handlers.size() == 0)
        {
            removeCommand(sendCommand);
            addCommand(searchCommand);
        }

        // If the number of currently open connections
        // drops below the maximum number that this
        // device could have open, restore
        // 'Add connection' to the screen commands.
        if (handlers.size() < maxConnections)
        {
            removeCommand(addConnectionCommand);
            addCommand(addConnectionCommand);
        }


        statusField.setText("Connection closed");
    }


    public void handleErrorClose(ServerConnectionHandler handler,
                                 String errorMessage)
    {
        removeHandler(handler);
        if (handlers.size() == 0)
        {
            removeCommand(sendCommand);
            addCommand(searchCommand);
        }

        statusField.setText("Error (close): '" + errorMessage + "'");
    }


    public void handleError(ServerConnectionHandler handler,
                            String errorMessage)
    {
        statusField.setText("Error: '" + errorMessage + "'");
    }
}
