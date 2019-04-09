/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.btsppecho.client;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import com.nokia.midp.examples.btsppecho.ClientForm;
import com.nokia.midp.examples.btsppecho.LogScreen;

/**
 * This class provides a connection service for bluetooth connections.
 */
public class ConnectionService
    implements Runnable
{
    private final ClientForm listener;
    private final String url;

    private StreamConnectionNotifier connectionNotifier = null;
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
            synchronized(this)
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
                (StreamConnectionNotifier) Connector.open(url);

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
            // ConnectionNotFoundException is an IOException
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
                StreamConnection connection =
                    (StreamConnection)
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

                // One could consider exiting the
                // ConnectionService when the Client
                // reaches the maximum number of allowed
                // open connections. In that case (i.e.
                // when the maximum number of possible
                // connections is already open), the
                // ConnectionService will not be able
                // to accept any new connections and one
                // might possibly want to consider whether
                // or not the ConnectionService thread
                // could then be terminated.
                //
                // However, existing connections can also
                // be disconnected (e.g. the Server is
                // terminated or closes some/all of its
                // existing connections). In that case,
                // one may want to keep the
                // ConnectionService alive and running:
                // in order to accept later new connections
                // without the need to restart the
                // ConnectionService or MIDlet.
                //
                // (This MIDlet uses the latter approach.)
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
            catch (SecurityException e)
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
