/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.wma.chat;

import java.util.*;
import javax.microedition.lcdui.*;

/**
 * Extends the WMAForm class and provides a field in which to add text to be sent.
 *
 * @version 1.0
 */
public class WriteScreen extends WMAForm{
    /** String constant for TextField label */
    private static final String TO_STRING = "To: ";
    /** TextField for the text to be sent */
    private TextField sendTextField;

   /**
    *   Sets up the screen with a textfield in which to add text to send and adds
    *   the Send and Cancel commands.
    *
    *   @param screenHandler the current ScreenHandler
    */
    public WriteScreen (ScreenHandler screenHandler) {
        super(screenHandler);
        sendTextField = new TextField("", "", ScreenHandler.TEXTFIELD_SIZE, TextField.ANY);
        append(sendTextField);
        addCommand(ScreenHandler.CMD_SEND);
        addCommand(ScreenHandler.CMD_CANCEL);
    }

    /**
     *  Updates the textfield with newly selected peers.
     *  Adds the peer names to the label of the TextField.
     *
     *  @param peerNumbers the currently selected peer numbers
     */
    public void updateScreen(Vector peerNumbers) {
        String names = TO_STRING;
        for ( Enumeration enumeration = peerNumbers.elements(); enumeration.hasMoreElements(); ) {
            String number = (String)enumeration.nextElement();
            if (names.equals(TO_STRING)) {
                names += number;
            } else {
                names += "," + number;
            }
        }
        sendTextField.setLabel(names);
        sendTextField.setString("");
    }

    /**
     *  Returns the message to be sent.
     *
     *  @return a String containing the message to be sent
     */
    public String getMessageText() {
        return sendTextField.getString();
    }
}