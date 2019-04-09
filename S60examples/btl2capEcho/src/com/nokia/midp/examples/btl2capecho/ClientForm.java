/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.btl2capecho;

import java.util.Vector;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import com.nokia.midp.examples.btl2capecho.client.ClientConnectionHandler;
import com.nokia.midp.examples.btl2capecho.client.ClientConnectionHandlerListener;

/**
 * This class provides a midlet form for the client functionality of
 * the example.
 */
public class ClientForm
    extends Form
    implements CommandListener, ClientConnectionHandlerListener
{
    private final MIDletApplication midlet;
    private final StringItem numConnectionsField;
    private final TextField sendDataField;
    private final StringItem receivedDataField;
    private final StringItem statusField;
    private final Command sendCommand;
    private final Command quitCommand;
    private final Command logCommand;
    private final Vector handlers = new Vector();

    private StringItem btAddressField = null;
    private volatile int numReceivedMessages = 0;
    private volatile int numSentMessages = 0;
    private int sendMessageId = 0;


    public ClientForm(MIDletApplication midlet)
    {
        super("Client");
        this.midlet = midlet;

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
        statusField = new StringItem("Status", "listening");
        append(statusField);
        sendDataField = new TextField("Send data",
                                      "Client says: 'Hello, world.'",
                                      64,
                                      TextField.ANY);
        append(sendDataField);
        receivedDataField = new StringItem("Last received data", null);
        append(receivedDataField);

        sendCommand = new Command("Send", Command.SCREEN, 1);
        quitCommand = new Command("Exit", Command.EXIT, 1);
        logCommand = new Command("View log", Command.SCREEN, 2);
        addCommand(quitCommand);
        addCommand(logCommand);
        setCommandListener(this);
    }


    void closeAll()
    {
        for (int i=0; i < handlers.size(); i++)
        {
            ClientConnectionHandler handler =
                (ClientConnectionHandler) handlers.elementAt(i);
            handler.close();
        }
    }


    public void commandAction(Command cmd, Displayable disp)
    {
        if (cmd == logCommand)
        {
            midlet.clientFormViewLog(this);
        }
        if (cmd == sendCommand)
        {
            String sendData = sendDataField.getString();
            try
            {
                sendMessageToAllClients(++sendMessageId, sendData);
            }
            catch (IllegalArgumentException e)
            {
                // Message length longer than
                // ServerConnectionHandler.MAX_MESSAGE_LENGTH

                String errorMessage =
                           "IllegalArgumentException while trying " +
                           "to send a message: " + e.getMessage();

                handleError(null, errorMessage);
            }
        }
        else if (cmd == quitCommand)
        {
            // the MIDlet aborts the ConnectionService, etc.
            midlet.clientFormExitRequest();
        }
    }


    public void removeHandler(ClientConnectionHandler handler)
    {
        // Note: we assume the caller has aborted/closed/etc.
        // the handler if that needed to be done. This method
        // simply removes it from the list of handlers maintained
        // by the ConnectionService.
        handlers.removeElement(handler);
    }


    public void sendMessageToAllClients(int sendMessageId, String sendData)
        throws IllegalArgumentException
    {
        Integer id = new Integer(sendMessageId);

        for (int i=0; i < handlers.size(); i++)
        {
            ClientConnectionHandler handler =
                (ClientConnectionHandler) handlers.elementAt(i);

            // throws IllegalArgumentException if message length
            // > ServerConnectionHandler.MAX_MESSAGE_LENGTH
            handler.queueMessageForSending(
                        id,
                        sendData.getBytes());
        }
    }


    // interface L2CAPConnectionListener

    public void handleAcceptAndOpen(ClientConnectionHandler handler)
    {
        handlers.addElement(handler);
        // start the reader and writer, it also causes underlying
        // InputStream and OutputStream to be opened.
        handler.start();

        statusField.setText("'Accept and open' for new connection");
    }


    public void handleStreamsOpen(ClientConnectionHandler handler)
    {

         // first connection
         if (handlers.size() == 1)
         {
             addCommand(sendCommand);
         }

         String str = Integer.toString(handlers.size());
         numConnectionsField.setText(str);
         statusField.setText("I/O streams opened on connection");
    }


    public void handleStreamsOpenError(ClientConnectionHandler handler,
                                       String errorMessage)
    {
        handlers.removeElement(handler);

        String str = Integer.toString(handlers.size());
        numConnectionsField.setText(str);
        statusField.setText("Error opening I/O streams: " +
                            errorMessage);
    }



    public void handleReceivedMessage(ClientConnectionHandler handler,
                                      byte[] messageBytes)
    {
        numReceivedMessages++;

        String msg = new String(messageBytes);
        receivedDataField.setText(msg);

        statusField.setText(
            "# messages read: " + numReceivedMessages + " " +
            "sent: " + numSentMessages);
    }


    public void handleQueuedMessageWasSent(
                    ClientConnectionHandler handler,
                    Integer id)
    {
        numSentMessages++;

        statusField.setText("# messages read: " +
                    numReceivedMessages + " " +
                    "sent: " + numSentMessages);
    }


    public void handleClose(ClientConnectionHandler handler)
    {
        removeHandler(handler);
        if (handlers.size() == 0)
        {
            removeCommand(sendCommand);
        }

        String str = Integer.toString(handlers.size());
        numConnectionsField.setText(str);
        statusField.setText("Connection closed");
    }


    public void handleErrorClose(ClientConnectionHandler handler,
                                 String errorMessage)
    {
        removeHandler(handler);
        if (handlers.size() == 0)
        {
            removeCommand(sendCommand);
        }

        statusField.setText("Error: (close)\"" + errorMessage + "\"");
    }


    public void handleError(ClientConnectionHandler hander,
                            String errorMessage)
    {
        statusField.setText("Error: \"" + errorMessage + "\"");
    }
}
