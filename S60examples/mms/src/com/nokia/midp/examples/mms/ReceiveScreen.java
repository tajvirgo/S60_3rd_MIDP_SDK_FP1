/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.mms;

import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import javax.wireless.messaging.*;

import java.io.IOException;

/**
 * ReceiveScreen implements MMS message receiving dialog.
 */
class ReceiveScreen
extends Form
implements CommandListener
{

	private String[] connections;
	private final MMSMIDlet midlet;
	private final Command commandClose = new Command("Close", Command.ITEM, 1);

        private final String image_mine_type = "image/jpeg";
        private final String text_mine_type = "text/plain";
        private final String receive_mms_title = "Received MMS Message";
        /**
         * Constructs a new ReceiveScreen object with the given midlet and
         * message, and create a new form on the screen.
         * @param midlet the MMSMIDlet object
         * @param mmsMessage the message to be displayed on the form
         */
        ReceiveScreen(MMSMIDlet midlet, Message mmsMessage)
	{
		super(null);
		this.midlet = midlet;
		if (mmsMessage != null)
		{
			createForm(mmsMessage);
		}
	}

        /**
         * Creates the form with title and multipart messages.
         * @param mmsMessage the Message object to be displayed
         */
        public void createForm(Message mmsMessage)
	{
		deleteAll();

		setTitle(receive_mms_title);

		if (mmsMessage instanceof MultipartMessage)
		{
			MultipartMessage  multipartMessage= (MultipartMessage) mmsMessage;
                        String display = null;
                        if(multipartMessage.getTimestamp()!=null){
                          display = "From: " + mmsMessage.getAddress() + "\n" +
                              "Sent: " +
                              multipartMessage.getTimestamp().toString();
                        }else{
                          display = "From: " + mmsMessage.getAddress();
                        }
                        StringItem messageHeader = new StringItem(null,display);
			messageHeader.setLayout(Item.LAYOUT_NEWLINE_AFTER);
			append(messageHeader);

			MessagePart[] messageParts = multipartMessage.getMessageParts();

			for (int i = 0; i < messageParts.length; i++)
			{
				MessagePart messagePart = messageParts[i];
				String mimeType = messagePart.getMIMEType();
				String contentLocation = messagePart.getContentLocation();

				byte[] part = messagePart.getContent();

				if (mimeType.equals(image_mine_type))
				{
					Image imgage = Image.createImage(part, 0, part.length);
					ImageItem imageItem = new ImageItem(null, imgage, Item.LAYOUT_CENTER + Item.LAYOUT_NEWLINE_AFTER, contentLocation);
					append(imageItem);
				}
				if (mimeType.equals(text_mine_type))
				{
					StringItem stringItem = new StringItem(null, new String(part) );
					stringItem.setLayout(Item.LAYOUT_CENTER);
					append(stringItem);
				}
			}
		}

		addCommand(commandClose);
		setCommandListener(this);
		Display.getDisplay(midlet).setCurrent(this);
	}

        /**
         * Invokes certain methods as responding to the close events that
         * have occurred.
         * @param c a Command object identifying the command
         * @param d  the Displayable on which this event has occurred
         */
	public void commandAction(Command c, Displayable d)
	{
		if (c==commandClose)
		{
			midlet.resumeDisplay();
		}
	}

}
