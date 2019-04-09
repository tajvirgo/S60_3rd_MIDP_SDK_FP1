/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.btsppecho.server;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import com.nokia.midp.examples.btsppecho.LogScreen;

/**
 * This class provides a server connection handler for bluetooth connections.
 */
public class ServerConnectionHandler
    implements Runnable
{
    private final static byte ZERO = (byte) '0';
    private final static int LENGTH_MAX_DIGITS = 5;

    // this is an arbitrarily chosen value:
    private final static int MAX_MESSAGE_LENGTH =
                             65536 - LENGTH_MAX_DIGITS;

    private final ServiceRecord serviceRecord;
    private final int requiredSecurity;
    private final ServerConnectionHandlerListener listener;
    private final Hashtable sendMessages = new Hashtable();

    private StreamConnection connection;
    private OutputStream out;
    private InputStream in;
    private volatile boolean aborting;
    private Writer writer;


    public ServerConnectionHandler(
               ServerConnectionHandlerListener listener,
               ServiceRecord serviceRecord,
               int requiredSecurity)
    {
        this.listener = listener;
        this.serviceRecord = serviceRecord;
        this.requiredSecurity = requiredSecurity;
        aborting = false;

        connection = null;
        out = null;
        in = null;
        listener = null;

        // the caller must call method 'start'
        // to start the reader and writer
    }


    public ServiceRecord getServiceRecord()
    {
        return serviceRecord;
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

            if (out != null)
            {
                try
                {
                    out.close();
                    synchronized (this)
                    {
                        out = null;
                    }
                }
                catch(IOException e)
                {
                    // there is nothing we can do: ignore it
                }
            }

            if (in != null)
            {
                try
                {
                    in.close();
                    synchronized (this)
                    {
                        in = null;
                    }
                }
                catch(IOException e)
                {
                    // there is nothing we can do: ignore it
                }
            }

            if (connection != null)
            {
                try
                {
                    connection.close();
                    synchronized (this)
                    {
                        connection = null;
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
        if (data.length > MAX_MESSAGE_LENGTH)
        {
            throw new IllegalArgumentException(
                          "Message too long: limit is " +
                          MAX_MESSAGE_LENGTH + " bytes");
        }

        synchronized(sendMessages)
        {
            sendMessages.put(id, data);
            sendMessages.notify();
        }
    }


    private void sendMessage(byte[] data)
        throws IOException
    {
        byte[] buf = new byte[LENGTH_MAX_DIGITS + data.length];
        writeLength(data.length, buf);
        System.arraycopy(data,
                         0,
                         buf,
                         LENGTH_MAX_DIGITS,
                         data.length);
        out.write(buf);
        out.flush();
    }


    public void run()
    {
        // the reader

        // 1. open the connection and streams, start the writer
        String url = null;
        try
        {
            // 'must be master': false
            url = serviceRecord.getConnectionURL(
                                  requiredSecurity,
                                  false);

            connection = (StreamConnection) Connector.open(url);
            in = connection.openInputStream();
            out = connection.openOutputStream();

            LogScreen.log("Opened connection & streams to: '" +
                      url + "'\n");

            // start the writer
            Writer writer = new Writer(this);
            Thread writeThread = new Thread(writer);
            writeThread.start();

            LogScreen.log("Started a reader & writer for: '" +
                      url + "'\n");

            // open succeeded, inform listener
            listener.handleOpen(this);
        }
        catch(IOException e)
        {
            // open failed, close any connections/streams, and
            // inform listener that the open failed

            LogScreen.log("Failed to open " +
                          "connection or streams for '" +
                           url + "' , Error: " +
                           e.getMessage());

            close();

            listener.handleOpenError(
                         this,
                         "IOException: '" + e.getMessage() + "'");

            return;
        }
        catch (SecurityException e)
        {
            // open failed, close any connections/streams, and
            // inform listener that the open failed

            LogScreen.log("Failed to open " +
                          "connection or streams for '" +
                           url + "' , Error: " +
                           e.getMessage());

            close();

            listener.handleOpenError(
                         this,
                         "SecurityException: '" + e.getMessage() + "'");

            return;
        }

        // 2. wait to receive and read messages
        while (!aborting)
        {
            int length = 0;
            try
            {
                byte[] lengthBuf = new byte[LENGTH_MAX_DIGITS];
                readFully(in, lengthBuf);
                length = readLength(lengthBuf);
                byte[] temp = new byte[length];
                readFully(in, temp);

                listener.handleReceivedMessage(this, temp);
            }
            catch (IOException e)
            {
                close();
                if (length == 0)
                {
                   listener.handleClose(this);
                }
                else
                {
                   // we were in the middle of reading...
                   listener.handleErrorClose(this, e.getMessage());
                }
            }
        }
    }


    private static void readFully(InputStream in, byte[] buffer)
        throws IOException
    {
        int bytesRead = 0;

        while (bytesRead < buffer.length)
        {
            int count = in.read(buffer,
                                bytesRead,
                                buffer.length - bytesRead);

            if (count == -1)
            {
                throw new IOException("Input stream closed");
            }
            bytesRead += count;
        }
    }


    private static int readLength(byte[] buffer)
    {
        int value = 0;

        for (int i = 0; i < LENGTH_MAX_DIGITS; ++i)
        {
            value *= 10;
            value += buffer[i] - ZERO;
        }
        return value;
    }


    private void sendMessage(OutputStream out, byte[] data)
        throws IOException
    {
        if (data.length > MAX_MESSAGE_LENGTH)
        {
            throw new IllegalArgumentException(
                          "Message too long: limit is: " +
                          MAX_MESSAGE_LENGTH + " bytes");
        }
        byte[] buf = new byte[LENGTH_MAX_DIGITS + data.length];
        writeLength(data.length, buf);
        System.arraycopy(data,
                         0,
                         buf,
                         LENGTH_MAX_DIGITS,
                         data.length);
        out.write(buf);
        out.flush();
    }


    private static void writeLength(int value, byte[] buffer)
    {
        for (int i = LENGTH_MAX_DIGITS -1; i >= 0; --i)
        {
            buffer[i] = (byte) (ZERO + value % 10);
            value = value / 10;
        }
    }


    private class Writer
        implements Runnable
    {
        private final ServerConnectionHandler handler;


        Writer(ServerConnectionHandler handler)
        {
            this.handler = handler;
        }


        public void run()
        {
            while (!aborting)
            {
                synchronized(sendMessages)
                {
                    Enumeration e = sendMessages.keys();
                    if (e.hasMoreElements())
                    {
                        // send any pending messages
                        Integer id = (Integer) e.nextElement();
                        byte[] sendData =
                               (byte[]) sendMessages.get(id);
                        try
                        {
                            sendMessage(out, sendData);

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
                            listener.handleErrorClose(handler,
                                                      ex.getMessage());
                        }
                    }

                    if (sendMessages.isEmpty())
                    {
                        try
                        {
                            sendMessages.wait();
                        }
                        catch (InterruptedException ex)
                        {
                            // this can't happen in MIDP: ignore it
                        }
                    }
                }
            }
        }
    }
}
