/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.btl2capecho.client;


import java.io.IOException;
import javax.microedition.io.Connector;
import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.L2CAPConnectionNotifier;

import com.nokia.midp.examples.btl2capecho.ClientForm;
import com.nokia.midp.examples.btl2capecho.LogScreen;

/**
 * This class provides a connection service for bluetooth connections.
 */
public class ConnectionService
    implements Runnable
{
    private final ClientForm listener;
    private final String url;

    private L2CAPConnectionNotifier connectionNotifier = null;
    private volatile boolean aborting;


    public ConnectionService(String url,
                             ClientForm listener)
    {
        this.url = url;
        this.listener = listener;

        LogScreen.log("ConnectionService: waiting to " +
                      "accept connections on '" +
                      url + "'\n");

        // start waiting for a connection
        Thread thread = new Thread(this);
        thread.start();
    }


    public String getClientURL()
    {
        return url;
    }


    public void close()
    {
        if (!aborting)
        {
            synchronized (this)
            {
                aborting = true;
            }

            // Ideally, one might want to give the run method's
            // loop a chance to abort before calling the
            // subsequent close, but the run loop is anyways
            // likely to be sitting on the acceptAndOpen
            // (i.e. blocked).

            try
            {
                connectionNotifier.close();
            }
            catch (IOException e)
            {
                // There is nothing very useful that
                // we can do for this case.
            }
        }
    }


    public void run()
    {
        aborting = false;

        try
        {
            connectionNotifier =
                (L2CAPConnectionNotifier) Connector.open(url);

            // It might useful in some cases to add a service to the
            // 'Public Browse Group'. For example by doing something
            // approximately as follows:
            // -----------------------------------------------------
            // Retrieve the service record template
            // LocalDevice ld = LocalDevice.getLocalDevice();
            // ServiceRecord rec = ld.getRecord(connectionNotifier);
            // DataElement element =
            //             new DataElement(DataElement.DATSEQ);
            //
            // The service class for PublicBrowseGroup (0x1002)
            // is defined in the Bluetooth Assigned Numbers document.
            // element.addElement(new DataElement(DataElement.UUID,
            //                                    new UUID(0x1002)));
            //
            // Add to the public browse group:
            // rec.setAttributeValue(0x0005, element);
            // -----------------------------------------------------

        }
        catch (IOException e)
        {
            String errorMessage =
                   "Error while starting ConnectionService: " +
                   e.getMessage();

            listener.handleError(null, errorMessage);

            aborting = true;
        }
        catch (SecurityException e)
        {
            String errorMessage =
                "SecurityException while starting ConnectionService: " +
            e.getMessage();

            listener.handleError(null, errorMessage);

            aborting = true;
        }

        while (!aborting)
        {
            try
            {
                // 1. wait to accept & open a new connection
                L2CAPConnection connection =
                    (L2CAPConnection)
                    connectionNotifier.acceptAndOpen();

                LogScreen.log("ConnectionService: new connection\n");

                // 2. create a handler to take care of
                // the new connection and inform
                // the listener
                if (!aborting)
                {
                    ClientConnectionHandler handler =
                        new ClientConnectionHandler(this,
                                                    connection,
                                                    listener);
                    listener.handleAcceptAndOpen(handler);
                }
            }
            catch (IOException e)
            {
                if (!aborting)
                {
                    String errorMessage =
                               "IOException occurred during " +
                               "accept and open: " +
                               e.getMessage();

                    listener.handleError(null, errorMessage);
                }
            }
        }
    }
}
