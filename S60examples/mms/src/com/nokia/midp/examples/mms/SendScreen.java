/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.mms;

import java.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import javax.wireless.messaging.*;

/**
 * SendScreen implements MMS message sending dialog.
 */
class SendScreen
extends Form
implements CommandListener
{
	private final MMSMIDlet midlet;

	private final Command commandSend = new Command("Send", Command.ITEM, 1);
	private final Command commandBack = new Command("Back", Command.BACK, 0);
        private final Command commandExit = new Command("Exit", Command.EXIT, 0);

        private TextField messageText;
	private TextField recipientPhone;
	private byte[] messageImage;

        private final int text_field_len = 256;
        private final String msg_text = "Text: ";
        private final String recipient_to = "To: ";
        private final String mms_schema = "mms://";
        private final String byte_type = "UTF-8";
        private final String image_part_4 = "image/jpeg";
        private final String image_part_5 = "id0";
        private final String image_part_6 = "snapshot image";
        private final String text_part_4 = "text/plain";
        private final String text_part_5 = "id1";
        private final String text_part_6 = "message text";
        private final String text_part_7 = byte_type;
        private String imagefile = "/com/nokia/midp/examples/mms/res/phone.JPG";


        /**
         * Constructs a new SendScreen object with the given MMSMIDlet
         * @param midlet the MMSMIDlet object
         */
        SendScreen(MMSMIDlet midlet)
	{
		super("MMS Message");
		this.midlet = midlet;
		addCommand(commandSend);
                // If we are not using the camera there is no camera screen
                // to go back to.
                if (this.midlet.usingCamera == true)
                {
                  addCommand(commandBack);
                }
                else
                {
                  addCommand(commandExit);
                }
		setCommandListener(this);
	}

        /**
         * Allows user to enter the recipient's phone number and the text and
         * displays the UI.
         * @param inputImage the binary image
         */
        public void initializeComposeCanvas(byte[] inputImage)
	{
		messageImage = inputImage;
		Image tempImage = Image.createImage(inputImage, 0, inputImage.length);
		ImageItem imageItem = new ImageItem(null, tempImage, Item.LAYOUT_CENTER, null);

		messageText = new TextField(msg_text, null, text_field_len, TextField.ANY);
		recipientPhone = new TextField(recipient_to, null, text_field_len, TextField.PHONENUMBER);

		deleteAll();
		append(imageItem);
		append(messageText);
		append(recipientPhone);

		midlet.showSendScreen();
	}

        /**
         * Initializes the default picture.
         */
        public void initializeDefaultCanvas()
        {
          Image testImage= createImage(imagefile);

          ImageItem imageItem = new ImageItem(null, testImage, Item.LAYOUT_CENTER, null);

          messageText = new TextField(msg_text, null, text_field_len, TextField.ANY);
          recipientPhone = new TextField(recipient_to, null, text_field_len, TextField.PHONENUMBER);

          deleteAll();
          append(imageItem);
          append(messageText);
          append(recipientPhone);

          midlet.showSendScreen();
	}

        /**
         * Invokes certain methods as responding to the back and send events that
         * have occurred.
         * @param c a Command object identifying the command
         * @param d  the Displayable on which this event has occurred
         */
	public void commandAction(Command c, Displayable d)
	{
		if (c==commandBack)
		{
			midlet.showCameraScreen();
		}

		if (c==commandSend)
		{
			sendMMS();
		}

                if (c==commandExit)
                {
                        midlet.exitApplication();
                }
	}


	private void sendMMS()
	{
               /*
	       String recipientAddress = mms_schema + recipientPhone.getString();
               if(midlet.getApplicationID()!=null && midlet.getApplicationID().length()>0){
                   recipientAddress =recipientAddress + ":" + midlet.getApplicationID();
               }*/
               String recipientAddress = mms_schema + recipientPhone.getString() + ":" + midlet.getApplicationID();
		try
		{
			byte[] textBytes = messageText.getString().getBytes(byte_type);
			byte[] imageBytes = messageImage;

			MessagePart imagePart= new MessagePart(imageBytes,
					0, imageBytes.length,
					image_part_4, image_part_5,
					image_part_6, null);

			MessagePart textPart= new MessagePart(textBytes,
					0, textBytes.length,
					text_part_4, text_part_5,
					text_part_6, text_part_7);

			midlet.showInfo("Sending message...");
			midlet.sendMessage(recipientAddress, imagePart, textPart);
		}
		catch (UnsupportedEncodingException uee)
		{
			midlet.showError("Encoding not supported. " + uee.getMessage());
		}
		catch (SizeExceededException see)
		{
			midlet.showError("Message part is too large. " + see.getMessage());
		}catch(Exception e){
                        midlet.showError("Other Exception. " + e.getMessage() );
                }

	}

        private Image createImage(String filename) {
          InputStream iStrm = null;
          try {

            iStrm = getClass().getResourceAsStream(filename);
          }
          catch (Exception ex) {
            return null;
          }
          Image im = null;
          byte imageData[] = null;

          try {

            ByteArrayOutputStream bStrm = new ByteArrayOutputStream();

            try {
              int ch;
              while ( (ch = iStrm.read()) != -1)
                bStrm.write(ch);

              imageData = bStrm.toByteArray();
              bStrm.close();
            }
            catch (Exception fe2) {
              return null;
            }
            messageImage = imageData;

            // Create the image from the byte array
            im = Image.createImage(imageData, 0, imageData.length);
          }
          finally {
            // Clean up
            try {
              if (iStrm != null)
                iStrm.close();
            }
            catch (Exception fe3) {
              //no action
            }

          }

          return im;
        }
}
