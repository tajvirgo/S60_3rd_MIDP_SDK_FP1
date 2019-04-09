/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.wma.chat;

import javax.microedition.lcdui.*;

/**
 * Extends the WMAForm class and provides a field to enter a chosen chat name.
 *
 * @version 1.0
 */
public class ChatNameScreen extends WMAForm {
    /** TextField to enter chat name */
    private TextField chatNameTextField;

    /**
     *  Sets up the screen with a textfield and an additional ok command.
     *
     *  @param screenHandler the current ScreenHandler
     */
    public ChatNameScreen(ScreenHandler screenHandler) {
        super(screenHandler);
        chatNameTextField = new TextField("Enter chat name:", "", ScreenHandler.TEXTFIELD_SIZE, TextField.ANY);
        append(chatNameTextField);
        addCommand(ScreenHandler.CMD_OK);
    }

    /**
     *  Returns the chosen chat name.
     *
     *  @return a String containing the chosen chat name
     */
    public String getChatName() {
        return chatNameTextField.getString();
    }
}