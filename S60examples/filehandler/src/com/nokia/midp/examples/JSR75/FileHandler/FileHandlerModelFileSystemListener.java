/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.FileHandler;

import javax.microedition.io.file.FileSystemListener;
import javax.microedition.io.file.FileSystemRegistry;


/**
FileHandlerModelFileSystemListener is a thread class that
is used for receiving status
notification when adding or removing a
file system root. This can be achieved
by inserting or removing a card from a
device or by mounting or unmounting file
systems to a device.
*/

class FileHandlerModelFileSystemListener extends Thread implements FileSystemListener
{
    // Default classes contructor
    public FileHandlerModelFileSystemListener( FileHandlerViewController aMidlet )
    {
        this.iMidlet = aMidlet;
    }

    // Threads starting point is Run().
    public void run()
    {
        // Add this class to listen to volume changes.
        FileSystemRegistry.addFileSystemListener( this );
    }

    //  This method is invoked when a root on the
    // device has changed state.
    public void rootChanged(int state, String rootName)
    {
        if ( state == 1 )
        {
            // Clear screen and print directly to.
            iMidlet.deleteAll();
            iMidlet.append("Root structure Changed. '" + rootName +"' has been removed.\n",null);
        }
        else
        {
            // Clear screen and print directly to.
            iMidlet.deleteAll();
            iMidlet.append("Root structure Changed. '" + rootName +"' has been added.\n",null);
            }

        // Call mainäs method to update mounted volumelist.
        iMidlet.upDateVolumes();
    }
    // Classe's members are here.
    private final FileHandlerViewController iMidlet;
}// End of file
