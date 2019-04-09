/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.FileHandler;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.Display;

/**
FileHandlerMIDlet is where the application on created and
run. Application management software contructs this and calls
startApp() method.
*/
public class FileHandlerMIDlet extends MIDlet
{

   // Default classes contructor
   public FileHandlerMIDlet()
    {
        instance = this;
        displayable = new FileHandlerViewController( this );
    }

    public void startApp()
    {
        // Get current's manager of the display and input
        // devices of the system. Requests that a different
        // Displayable object be made visible on the display.
        // Show FileHandlerViewController class.
        Display.getDisplay( this ).setCurrent( displayable );
    }

    public void pauseApp()
    {
        // Notifies the application management software
        // that the MIDlet does not want to be
        // active and has entered the Paused state
        instance.notifyPaused();
    }

    public void destroyApp( boolean unconditional )
    {}

    public void quitApp()
    {
        // Close the FileConnection
        displayable.closeFileConn();

        // Destory the application
        instance.destroyApp( true );

        // Used by an MIDlet to notify the application
        // management software that it has entered into the Destroyed state
        instance.notifyDestroyed();

        instance = null;
    }

    // Classe's members are here.
    private static FileHandlerMIDlet instance;
    private FileHandlerViewController displayable = null;
}// End of file
