/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.mms;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.wireless.messaging.*;


/**
 * MMSMIDlet implements a messaging application that exchanges MMS messages
 * using the API as defined in JSR-205.
 */
public class MMSMIDlet
extends MIDlet
implements MessageListener, Runnable
{
        private CameraScreen cameraScreen = null;
        private ReceiveScreen receiveScreen;
        private SendScreen sendScreen;
        private InfoScreen infoScreen;

        private Displayable resumeDisplay = null;

        private String applicationID;
        private MessageConnection messageConnection;

        private boolean closing;
        // Use the following flag for cases when camera capture is not
        // supported (either by model or SDK).  Default is false.
        // When set to true the midlet will use the camera to capture'
        // the content to be sent.
        boolean usingCamera = false;
        private Message nextMessage = null;

        private final String application_id_name = "Application-ID";
        private final String mms_con_schema = "mms://:";
        // List of registered push connections.
        String connections[];



  /**
   * Constructs a new MMSMIDlet object.
   */
  public MMSMIDlet()
  {
    applicationID = getAppProperty(application_id_name);
  }
        /**
         * Invokes the new application connection and creates CameraScreen,
         * Infoscreen and SendScreen.  Displays the CameraScreen as the first
         * screen.
         */
        public void startApp()
        {
            // Check to see if this session was started due to
            // inbound connection notification.
            connections = PushRegistry.listConnections(true);
            if(connections!=null && connections.length>0){
                  for (int i = 0; i < connections.length; i++) {
                    Message m = this.readMsg(connections[i]);
                    if (m != null) {
                      if(resumeDisplay == null){
                        if(sendScreen == null){
                          sendScreen = new SendScreen(this);
                        }
                        sendScreen.initializeDefaultCanvas();
                        resumeDisplay = sendScreen;

                      }
                      this.showReceiveScreen(m);
                    }
                  }

            }else{
                if (resumeDisplay == null) {
                    startConnection();
                    infoScreen = new InfoScreen();
                    sendScreen = new SendScreen(this);
                    if (usingCamera == true) {
                        cameraScreen = new CameraScreen(this);
                        Display.getDisplay(this).setCurrent(cameraScreen);
                        resumeDisplay = cameraScreen;
                        cameraScreen.start();
                    } else {
                        // method needed by lots of classes, shared by putting it here
                        sendScreen.initializeDefaultCanvas();
                        showSendScreen();
                    }
                } else {
                    Display.getDisplay(this).setCurrent(resumeDisplay);
                }
            }
        }

        /**
         * Stops the CameraScreen if it is the current screen displayed.
         */
        public void pauseApp()
        {
                if (Display.getDisplay(this).getCurrent() == cameraScreen)
                {
                        cameraScreen.stop();
                }
                try{
                  messageConnection.close();
                }catch(IOException io){
                  showError(io.getMessage());
                }

        }

        /**
         * Stops the current running CameraScreen.
         * @param unconditional the given boolean variable
         */
        public void destroyApp(boolean unconditional)
        {
                if (cameraScreen!=null && Display.getDisplay(this).getCurrent() == cameraScreen)
                {
                        cameraScreen.stop();
                }
                try{
                  if(messageConnection!=null){
                    messageConnection.setMessageListener(null);
                    messageConnection.close();
                  }
                }catch(IOException ie){
                  showError("IOException when close the message connection."+ ie.getMessage());
                }
        }

        /**
         * Releases the resource,destroys the application and notify it is
         * destroyed.
         */
        public void exitApplication()
        {
                if(this.messageConnection!=null){
                    closeConnection(this.messageConnection);
                }
                destroyApp(false);
                notifyDestroyed();
        }

        /**
         * Initiates the connection with certain URL.
         */
        public MessageConnection startConnection()
        {
                if (messageConnection == null)
                {
                        try
                        {
                                messageConnection = (MessageConnection) Connector.open(this.getConnectionAdd());
                                messageConnection.setMessageListener(this);
                                return messageConnection;
                        }
                        catch (IOException ioe)
                        {
                                ioe.printStackTrace();
                                Alert ioeAlert = new Alert("IOException: " + ioe.getMessage() );
                                return null;
                        }
                }
                return null;

        }

        private Message receiveMsg(MessageConnection conn){
            // Callback for inbound message.
                  try
                  {
                          Message incomingMessage = messageConnection.receive();
                          return incomingMessage;
                  }
                  catch (IOException e)
                  {
                          showError("Exception while receiving message: " + e.getMessage());
                          return null;
                  }
        }

        /**
         * Receives the messages and set the ReceiveScreen as the current screen.
         * @param conn the given MessageConnection object
         */
        public void notifyIncomingMessage(MessageConnection conn)
        {
                Message incomingMessage = receiveMsg(conn);
                this.showReceiveScreen(incomingMessage);
        }


        /**
         * Shuts down the message connection.
         */
        public void closeConnection(MessageConnection mc)
        {
                closing = true;

                if (mc != null)
                {
                        try
                        {
                                mc.close();
                        }
                        catch (IOException e)
                        {
                                // Ignore errors on shutdown
                        }
                }
        }

        /**
         * Sends the next message.
         */
        public void run()
        {
                if (nextMessage != null)
                {
                        try
                        {
                                messageConnection.send(nextMessage);
                        }
                        catch (IOException e)
                        {
                                showError("IO Exception while sending message: " + e.getMessage());
                        }
                        catch (Exception ge)
                        {
                                //showError("Exception while sending message: " + ge.getMessage());
                                showInfo("No permission to send.");
                        }
                }
                nextMessage = null;
        }

        /**
         * Gets the multipart message and send it in a seperate thread.
         * @param recipientAddress the recipient's address
         * @param imagePart the image captured
         * @param textPart the text that the user typed
         */
        void sendMessage(String recipientAddress, MessagePart imagePart, MessagePart textPart)
        {
                try
                {
                        showInfo("Sending message...");
                        MultipartMessage mmsMessage = (MultipartMessage) messageConnection.newMessage(MessageConnection.MULTIPART_MESSAGE);
                        mmsMessage.setAddress(recipientAddress);
                        mmsMessage.addMessagePart(imagePart);
                        mmsMessage.addMessagePart(textPart);

                        nextMessage= mmsMessage;
                        // send the message in another thread
                        new Thread(this).start();

                }
                catch (Exception e)
                {
                        showError(e.getMessage());
                }
        }


        private Message readMsg(String connUrl){
            try {
                messageConnection = (MessageConnection) Connector.open(
                connUrl);
                Message m = receiveMsg(messageConnection);
                return m;
            } catch (IOException ioe) {
                showError(ioe.getMessage());
                return null;
            }
        }


        /**
         * Obtains the application ID.
         * @return the application ID set
         */
        String getApplicationID()
        {
             applicationID = getAppProperty(application_id_name);
             return applicationID;
        }

        private String getConnectionAdd(){
            String connectionAddr = mms_con_schema + this.getApplicationID();
            return connectionAddr;
        }

        /**
         * Gets ready to send the image from the SendScreen.  Sets the current
         * screen as send screen.
         * @param imageData the binary format of the image
         */
        void imageCaptured(byte[] imageData)
        {
                cameraScreen.stop();
                resumeDisplay = sendScreen;
                Display.getDisplay(this).setCurrent(sendScreen);
                sendScreen.initializeComposeCanvas(imageData);
        }

        /**
         * Displays the CameraScreen.
         */
        void showCameraScreen()
        {
                resumeDisplay = cameraScreen;
                Display.getDisplay(this).setCurrent(cameraScreen);
                cameraScreen.start();
        }

        /**
         * Displays the ReceiveScreen.
         */
        void showReceiveScreen(Message incomingMessage)
        {
            //resumeDisplay = receiveScreen;
            ReceiveScreen receiveScreen = new ReceiveScreen(this,
            incomingMessage);
            Display.getDisplay(this).setCurrent(receiveScreen);
        }

        /**
         * Displays the SendScreen.
         */
        void showSendScreen()
        {
                resumeDisplay = sendScreen;
                Display.getDisplay(this).setCurrent(sendScreen);
        }

        /**
         * Displays the resumeDisplay object.
         */
        void resumeDisplay()
        {
                Display.getDisplay(this).setCurrent(resumeDisplay);
        }

        /**
         * Displays the info screen with the given messages.
         * @param messageString the info messages to be displayed on the screen
         */
        void showInfo(String messageString)
        {
                infoScreen.showInfo(messageString, Display.getDisplay(this) );
        }

        /**
         * Displays the InfoScreen with the given messages.
         * @param messageString the error messages to be displayed on the screen
         */
        void showError(String messageString)
        {
                infoScreen.showError(messageString, Display.getDisplay(this) );
        }
    }

