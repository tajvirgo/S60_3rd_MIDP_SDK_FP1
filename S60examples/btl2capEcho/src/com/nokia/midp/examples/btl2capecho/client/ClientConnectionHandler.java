/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.btl2capecho.client;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;

import javax.bluetooth.L2CAPConnection;

/**
 * This class provides a client bluetooth connection handler.
 */
public class ClientConnectionHandler
    implements Runnable
{
    private final static int WAIT_MILLIS = 250;

    private final ConnectionService ConnectionService;
    private final ClientConnectionHandlerListener listener;
    private final Hashtable sendMessages = new Hashtable();

    private L2CAPConnection connection;
    int transmitMTU = 0;

    private volatile boolean aborting;


    public ClientConnectionHandler(
        ConnectionService ConnectionService,
        L2CAPConnection connection,
        ClientConnectionHandlerListener listener)
    {
        this.ConnectionService = ConnectionService;
        this.connection = connection;
        this.listener = listener;
        aborting = false;

        // Our caller uses method 'start' to start the reader
        // and writer. (This allows the ConnectionService a
        // chance to add us to its list of handlers before
        // the reader and writer start running.)
    }


    ClientConnectionHandlerListener getListener()
    {
        return listener;
    }


    public synchronized void start()
    {
        Thread thread = new Thread(this);
        thread.start();
    }


    public void close()
    {
        if (!aborting)
        {
            synchronized(this)
            {
                aborting = true;
            }

            synchronized(sendMessages)
            {
                sendMessages.notify();
            }

            if (connection != null)
            {
                try
                {
                    connection.close();
                    synchronized (this)
                    {
                        connection = null;
                        transmitMTU = 0;
                    }
                }
                catch (IOException e)
                {
                    // there is nothing we can do: ignore it
                }
            }
        }
    }


    public void queueMessageForSending(Integer id, byte[] data)
    {
        if (data.length > transmitMTU)
        {
            throw new IllegalArgumentException(
                          "Message too long: limit is " +
                          transmitMTU + " bytes");
        }

        synchronized(sendMessages)
        {
            sendMessages.put(id, data);
            sendMessages.notify();
        }
    }


    public void run()
    {
        // the reader

        // 1. open the streams, start the writer
        try
        {
            transmitMTU = connection.getTransmitMTU();

            // start the writer
            Writer writer = new Writer(this);
            Thread writeThread = new Thread(writer);
            writeThread.start();

            listener.handleStreamsOpen(this);
        }
        catch(IOException e)
        {
            // open failed: close any connections/streams and
            // inform listener that the open failed

            close(); // also tells listener to delete handler

            listener.handleStreamsOpenError(this, e.getMessage());
            return;
        }


        // 2. wait to receive and read messages
        while (!aborting)
        {
            boolean ready = false;
            try
            {
                ready = connection.ready();
            }
            catch (IOException e)
            {
                close();
                listener.handleClose(this);
            }

            int length = 0;
            try
            {
                // something might be ready for reading
                if (ready)
                {
                    int mtuLength = connection.getReceiveMTU();
                    if (mtuLength > 0)
                    {
                        // Note: In theory, you might need to be
                        // a bit cautious about allocating an
                        // array of arbitrarily large length.
                        byte[] buffer = new byte[mtuLength];

                        length = connection.receive(buffer);
                        byte[] readData = new byte[length];
                        System.arraycopy(buffer,
                                         0,
                                         readData,
                                         0,
                                         length);

                        // handle read
                        listener.handleReceivedMessage(this, readData);
                    }
                }
                else
                {
                    // take a short wait before spinning through
                    // the loop again
                    try
                    {
                        synchronized(this)
                        {
                            wait(WAIT_MILLIS);
                        }
                    }
                    catch(InterruptedException e)
                    {
                        // can't happen in MIDP, just ignore
                    }
                }
            }
            catch(IOException e)
            {
                close();

                if (length == 0)
                {
                    listener.handleClose(this);
                }
                else
                {
                    // we were in the middle of receiving something?
                    listener.handleErrorClose(this, e.getMessage());
                }
            }
        }
    }


    private class Writer
            implements Runnable
    {
        private final ClientConnectionHandler handler;


        Writer(ClientConnectionHandler handler)
        {
            this.handler = handler;
        }


        public void run()
        {
            while (!aborting)
            {
                Enumeration e = sendMessages.keys();
                if (e.hasMoreElements())
                {
                    // send any pending messages
                    Integer id = (Integer) e.nextElement();
                    byte[] sendData = (byte[]) sendMessages.get(id);
                    try
                    {
                        // send message
                        connection.send(sendData);

                        // remove sent message from queue
                        sendMessages.remove(id);

                        // inform listener that it was sent
                        listener.handleQueuedMessageWasSent(
                                     handler,
                                     id);
                    }
                    catch (IOException ex)
                    {
                        close(); // stop the networking thread

                        // inform that we got an error close
                        listener.handleErrorClose(
                                     handler,
                                     ex.getMessage());
                    }
                }

                synchronized(sendMessages)
                {
                    if (sendMessages.isEmpty())
                    {
                        try
                        {
                            sendMessages.wait();
                        }
                        catch (InterruptedException ex)
                        {
                            // can't happen in MIDP: ignore
                        }
                    }
                }
            }
        }
    }
}
