/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.wma.chat;

import javax.microedition.lcdui.*;

/**
 * Extends the WMAForm class and provides a screen to display messages that
 * are sent and received.
 *
 * @version 1.0
 */
public class ChatScreen extends WMAForm{
   /** StringItem to display the text */
   private StringItem chatText;
   /** StringBuffer to append the newly received text to */
   private StringBuffer strBuffer;

   /**
    *   Sets up the screen with a StringItem and commands for getting
    *   to the write and change peers screens.
    *
    *   @param screenHandler the current ScreenHandler
    */
   public ChatScreen (ScreenHandler screenHandler) {
        super(screenHandler);
        strBuffer = new StringBuffer();
        chatText = new StringItem("", "");
        chatText.setLayout(Item.LAYOUT_BOTTOM);
        append(chatText);
        addCommand(ScreenHandler.CMD_WRITE);
        addCommand(ScreenHandler.CMD_PEERS);
    }

    /**
     *  Updates the ChatScreen with the sent or received message.
     *
     *  @param message the sent or received messsage
     */
    public void updateScreen(String message) {
        strBuffer.insert(0, "\n"+message);
        chatText.setText(strBuffer.toString());
        chatText.setLayout(Item.LAYOUT_BOTTOM);
    }
}
