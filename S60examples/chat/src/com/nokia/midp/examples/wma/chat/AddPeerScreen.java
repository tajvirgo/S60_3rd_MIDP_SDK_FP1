/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.wma.chat;

import javax.microedition.lcdui.TextField;

/**
 * Extends the WMAForm class and provides a screen to add peer
 *
 * @version 1.0
 */
public class AddPeerScreen extends WMAForm {
	private TextField numberField;
	private TextField portField;

	public AddPeerScreen (ScreenHandler screenHandler, WMAMIDlet parent) {
        super(screenHandler);
        numberField = new TextField("Number", "", ScreenHandler.TEXTFIELD_SIZE, TextField.PHONENUMBER);
        portField = new TextField("Port",  parent.port, ScreenHandler.TEXTFIELD_SIZE, TextField.ANY);
        append(numberField);
        // append(portField);
        addCommand(ScreenHandler.CMD_OK);
        addCommand(ScreenHandler.CMD_CANCEL);
	}

	/**
	 * Cleans TextField
	 */
	public void emptyField() {
		numberField.setString("");
	}

	/**
	 * Getter for peer number
	 * @return added peer number
	 */
	public String getPeerNumber() {
		return numberField.getString();
	}

	/**
	 * Getter for peer port
	 * @return added peer port
	 */
	public String getPeerPort() {
		return portField.getString();
	}
}
