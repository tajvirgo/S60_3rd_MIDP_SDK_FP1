/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.FileHandler;

import java.lang.Exception;

/**
FileHandlerErrorHandler class takes care of the exceptions
others fall into.
*/

public final class FileHandlerErrorHandler
{
    // Default classes contructor
    public FileHandlerErrorHandler(FileHandlerViewController aMidlet)
    {
        this.iMidlet = aMidlet;
    }

    // Static method to print the exception to user
    public static void handleError( Exception e,
                                    FileHandlerViewController aMidlet )
    {
        e.printStackTrace();
        FileHandlerErrorHandler instance = new FileHandlerErrorHandler( aMidlet );
        instance.PrintError( e );
    }

    // Print is done actually here.
    private void PrintError( Exception e )
    {
        // Clear controller's listBox's view.
        iMidlet.deleteAll();
        iMidlet.append( "An error has occured: " +e.toString(),null );
        // Restore Conenctionpoint to USED_ROOT.
        try {
          iMidlet.OpenFileConn();
        }
        catch(Exception e2)
        {
          // silent here (exception condition already reported
        }
    }
    // Classe's members are here.
    private final FileHandlerViewController iMidlet;
}// End of file
