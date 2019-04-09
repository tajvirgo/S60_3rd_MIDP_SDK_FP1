/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.wma.chat;

import javax.microedition.lcdui.*;

/**
 *  Extends the Form class to provide the common elements for all the screens.
 *
 * @version 1.0
 */
public class WMAForm extends Form {

    /**
     *  Sets up the common elements of a Form for the all the screens.
     *  Sets the Form title, adds an exit Command and sets the CommandListener.
     *
     *  @param screenHandler the current ScreenHandler
     */
    public WMAForm(ScreenHandler screenHandler) {
        super("WMA - SMS(Chat)");
        addCommand(ScreenHandler.CMD_EXIT);
        setCommandListener(screenHandler);
    }
}
