/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.FileHandler;

import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.*;
import java.lang.String;


/**
FileHandlerDialogs class inholds one TextBox controll that is used
with rename, makedir and open file functions. The class has its own
CommandListener so that it can react user's commands by discarding or
accepting changes / inputs.
*/
public class FileHandlerDialogs implements CommandListener
{
    // Default classes contructor
    public FileHandlerDialogs( FileHandlerMIDlet aMidlet,
                               FileHandlerViewController aMain )
    {
        // Initialize members with contructor's params.
        this.iMidlet = aMidlet; // reference to FileHandlerMIDlet
        this.iMain = aMain; // Caller function, FileHandlerViewController
    }

    public void shotTextBox( String aTitle,
                             String aText,
                             int aMaxLength,
                             int aFunction )
    {
        // What is the function (Thread to be initialized)
        this.iFunction = aFunction;

        try
        {
        // Test wheater the given MaxLenght param is zero. If so,
        // construct TextBox with 4k space bound
        // (open file functionality uses this)

            if ( aMaxLength == 0 )
            {
                aMaxLength = 4096; // just to be safe to allocate, in bytes.
            }
            // Contructs TextBox controll acording
            textBox = new TextBox( aTitle, aText, aMaxLength, TextField.ANY );


            // Initialize the commands
            exitCommand = new Command( COMMAND_BACK, Command.BACK, 1 );
            doneCommand = new Command( COMMAND_DONE, Command.SCREEN, 1 );

            // Add the commands to the TextBox
            textBox.addCommand( exitCommand );
            textBox.addCommand( doneCommand );

            // Set the command listener for the textbox to the current midlet
            textBox.setCommandListener( ( CommandListener ) this );

            // Show the textBox by using Display's setCurrent() method.
            Display.getDisplay( iMidlet ).setCurrent( textBox );
        }
        catch ( Exception e )
        {
            // If something went wrong, call static ErrorHandler.
            FileHandlerErrorHandler.handleError( e,iMain );
        }
    }

    // User' commands are controlled here.
    public void commandAction( Command c, Displayable d )
    {
        // If user selected Done command
        if ( c == doneCommand )
        {
            // Check what function was called to show textBox
            switch ( iFunction )
            {
                case 0:    // MakeDir functionality was the TextBox caller
                case 1:    // Rename functionality was the TextBox caller
                {
                    // Check is TextBox's entry lenght zero (no input)
                    if ( textBox.size()>0 )
                    {
                        // Call viewConteller's method to handle the
                        // input in order to trigger the threads.
                        // Pass also the iFunction param
                        iMain.AfterDialog( textBox.getString(),iFunction );
                    }
                    else
                    {
                        // User's input was not correct.
                        iMain.append( INCORRECT_NAME,null );
                    }
                    break;
                }
                case 2:    // OpenFile functionality was the TextBox caller
                {
                    iMain.AfterDialog( textBox.getString(),iFunction );
                    break;
                }
            } // End of Switch
        }
        else // Exit was called.
        {
            //Show cancel info to user.
            iMain.append( STRING_INTERRUPTED,null );
        }

        // Call main view (restore)
        Display.getDisplay( iMidlet ).setCurrent( iMain );
    }


    // Classe's members are here.
    private Command doneCommand;
    private Command exitCommand;

    // Info texts
    private final String STRING_INTERRUPTED = "Command was canceled.";
    private final String INCORRECT_NAME = "Incorrect name or Empty.";
    private final String COMMAND_BACK = "Back";
    private final String COMMAND_DONE = "Done";

    private TextBox textBox;
    private int iFunction;

    // References to contoller and main Midlet.
    private final FileHandlerMIDlet iMidlet;
    private final FileHandlerViewController iMain;

}// End of file
