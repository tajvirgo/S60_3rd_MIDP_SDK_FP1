/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.mms;

import javax.microedition.lcdui.*;

/**
 * InfoScreen shows informational or error alert dialogs.
 */
class InfoScreen
extends Alert
{
	private final int timeInfo_duration = 3000;
        private final int timeErr_duration = 5000;
        private final String errTitle = "Error";
        InfoScreen()
	{
		super("InfoScreen");
	}
        /**
         * Displays the information messages on the info screen.
         * @param message the given message to be displayed
         * @param display the display object
         */
        public void showInfo(String message, Display display)
	{
		setTitle(null);
		setType(AlertType.INFO);
		setTimeout(timeInfo_duration);
		setString(message);
		display.setCurrent(this);
	}

        /**
         * Displays the error messages on the info screen.
         * @param message the given message to be displayed
         * @param display the display object
         */
	public void showError(String message, Display display)
	{
		setTitle(errTitle);
		setType(AlertType.ERROR);
		setTimeout(timeErr_duration);
		setString(message);
		display.setCurrent(this);
	}

}

