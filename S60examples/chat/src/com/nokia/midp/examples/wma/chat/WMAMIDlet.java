/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.wma.chat;

import javax.microedition.midlet.*;
import java.util.*;

/**
 *  Handles the setting up of the application.
 *
 * @version 1
 */
public class WMAMIDlet extends MIDlet {
    /** default port */
    private final static String defaultPort = "5000";
    /** string identifying which port the connection will be opened on */
    public String port = null;
	/** flag indicating if this is the first time startApp has been called */
    private boolean firstTime = true;
    /** instance of ScreenHandler */
    public ScreenHandler screenHandler;
    /** instance of MessageHandler */
    public MessageHandler messageHandler;
    /** hashtable containing phone numbers */
    public Hashtable numbersList = new Hashtable();

    /**
     *  Sets up the MIDlet and creates instances of ScreenHandler and MessageHandler.
     *  Gets the Default-Port property from the JAD, calls setNumbers() and creates
     *  instances of MessageHandler and ScreenHandler.
     */
    public synchronized void startApp() {
        if (firstTime) {
        	port = getAppProperty("Default-Port");
        	port =  port == null ? defaultPort: port;
        	setNumbers();
            messageHandler = new MessageHandler(this);
            screenHandler = new ScreenHandler(this);
            firstTime = false;
        }
    }

    /** No implementation required as MIDlet only responds to user interaction.*/
    public void pauseApp() {
    }

    /**
     *  Destroys all resources used. If the MessageHandler has been instantiated
     *  its exit() method is called and the thread terminated otherwise this does
     *  nothing.
     */
    public void destroyApp(boolean unconditional) {
        //if messageHandler has been instantiated call exit()
        if (messageHandler != null) {
            messageHandler.exit();
            messageHandler.stopThread();
        }
    }

    /**
     *  Sets the phone numbers and ports for the devices.
     *  Gets the Peers-x properties from the JAD file. These properties contain
     *  the phone numbers and ports for other devices.
     */
    public void setNumbers() {
        //adds phone numbers to a hashtable
        int i;
        for (i = 0; ; i++){
            String property = "Peers-" + i;
            String attr = getAppProperty(property);
            if(attr != null) {
            	String number = attr.substring(0,attr.indexOf(':'));
                String port = attr.substring(attr.indexOf(':')+1);
                numbersList.put(number,port);
            } else {
                break;
            }
        }
    }
}
