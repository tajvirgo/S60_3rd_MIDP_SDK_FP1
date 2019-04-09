/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.FileHandler;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.List;
import java.util.Enumeration;
import java.util.Vector;
import java.lang.String;
import javax.microedition.io.file.*;
import javax.microedition.io.*;


/**
FileHandlerViewController is the main controller class to
direct all operations. All data are printed in TextBox controll where
user can make selection in order to target operations to spefific file / folder.
*/
public class FileHandlerViewController extends List implements CommandListener
{
    // Default classes contructor
    public FileHandlerViewController( FileHandlerMIDlet aMidlet )
    {
        // Inheritance from ListBox must be constructed accordingly.
        super( LIST_TITLE,List.IMPLICIT );
        this.iMidlet = aMidlet;
        this.iModel = new FileHandlerModel( this );

        this.setFitPolicy(Choice.TEXT_WRAP_ON);

        this.iDialogs = new FileHandlerDialogs( iMidlet,this );
        // Add command to user
        addCommand( exitCommand );
        addCommand( startCommand );
        setCommandListener( this );
        // Set Flag for UI idle.
        UI_STATE = IDLE_STATE;

        // Check enviroment.
        PrintFileConnectionVersion();
    }

    void setVolumeState()
    {
      // time for some error recovery.
      // Get roots from FileSystemRegistry interface's method.
      Enumeration rootEnum = FileSystemRegistry.listRoots();

      // Create view of available menu
      createMenuView(rootEnum);

      //rootEnum.nextElement().toString()
      setUsedRoot(null, 0);

      deleteAll();

      append("Select Volume", null);

      UI_STATE = VOLUME_SELECTION_STATE;


    }

    private void PrintFileConnectionVersion()
    {
        // Check that the File Connection Optional Package is there
        String v = System.getProperty( "microedition.io.file.FileConnection.version" );
        String systemSeparator = System.getProperty( "file.separator" );
        if (  v != null  )
        {
            append( "File Connection v" + v + " ", null);
            append( "found.", null);
            append( "File separator is", null);
            append( "'" + systemSeparator + "'.",null );
            append( LIST_START,null );
        }
        else
        {
            // JSR75 was not found os system. Remove Start possibility.
            removeCommand( startCommand );
            append( FILE_FAIL,null );
        }
    }

    //Indicates that a command event has occurred on Displayable
    public void commandAction( Command c, Displayable d )
    {
        // If EXIT is called..
        if ( c == exitCommand )
        {
            iMidlet.quitApp();
        }
        else if (  c == startCommand  )
        {
            // Create Thread for FileListener and start it!
            // This is done in here because Wellcome screen had to be shown first.
            // Adding this triggers access granting prompter.
            FileHandlerModelFileSystemListener changer = new FileHandlerModelFileSystemListener( this );
            changer.start();

            // List all available root.
            upDateVolumes();
            // Remove StartCommand. No need any more
            removeCommand( startCommand );

            //Clear ListBox's items (clears the Screen).
            this.deleteAll();

            // Info

            this.append( LIST_SELECT,null );
        }
        // This is true when <select> is pressed on ListBox
        else if ( c == List.SELECT_COMMAND )
        {
            // Determinate action according to UI_State
            // 1 = Volume select, 2 = Actions.
            switch ( UI_STATE )
            {
                case ACTION_COMMANDS_STATE: // Action mode, commands are online
                {
                    // Check if selected item is
                    // Not info and Not Folder and is ends with ".TXT" litteral.
                    if (  ( isInfoElement( this.getString( getSelectedIndex() ) )==false ) &&
                       (  this.getString( getSelectedIndex() ).endsWith( "/" )==false ) &&
                       (  this.getString( getSelectedIndex() ).endsWith( ".txt" ) ) )
                    {
                        // Start the OpenFile Thread, Selected item from listbox was
                        // .txt file (not info or folder)
                        FileHandlerThread_openfile OpenFileThread = new FileHandlerThread_openfile( this,fconn,this.getString( getSelectedIndex() ) );
                        OpenFileThread.start();
                    }
                    else
                    {
                        // Selected item must be folder. Info item is check in called
                        // function.
                        changeDir();
                    }
                    break;
                }
            }
        }
        // Handle other than select event on keyboard.
        else if ( c != List.SELECT_COMMAND )
        {
            switch ( UI_STATE )
            {
                case VOLUME_SELECTION_STATE:  // Volume selection State.
                {

                    // Set the Root to be used.
                    setUsedRoot( c.getLabel(),0 );

                    // Store initial root
                    initialRoot =  getUsedRoot();

                    // Open Root connection acording to USED_ROOT member.
                    OpenFileConn();

                    if (this.fconn != null)
                    {
                        // Clear the listbox
                        this.deleteAll();

                        // Info message
                        this.append("Selected Volume is ", null);
                        this.append("'"+c.getLabel()+"'.",null );
                        // List all command from model.
                        iModel.ListActionCommands();

                        // Shift state to Action from Volume
                        UI_STATE=ACTION_COMMANDS_STATE;
                    }
                    break;
                }
                case ACTION_COMMANDS_STATE: // action commands
                {

                    // "Back" Action was selected on Commands.
                    if ( ( String )iModel.GetCommandVector().elementAt( 0 ) == c.getLabel() )
                    {
                        // Go back in roots. Params in "ListDirs after"
                        upOneLevel( true );
                    }

                    // "List Directory" Action was selected on Commands.
                    if ( ( String )iModel.GetCommandVector().elementAt( 1 ) == c.getLabel() )
                    {
                        // Initialize thread and launch it.
                        FileHandlerThread_List listThread = new FileHandlerThread_List( this,fconn );
                        listThread.start();
                    }

                    // "Open & Edit file" Action was selected on Commands.
                    if ( ( String )iModel.GetCommandVector().elementAt( 2 ) == c.getLabel() )
                    {
                        // Check wheater the selected item on listbox is text file.
                        if ( ( isInfoElement( this.getString( getSelectedIndex() ) )==false ) &&
                             ( this.getString( getSelectedIndex() ).endsWith( "/" )==false ) &&
                             ( this.getString( getSelectedIndex() ).endsWith( ".txt" ) ) )
                        {
                            // Initialize thread and launch it.
                            FileHandlerThread_openfile OpenFileThread = new FileHandlerThread_openfile( this,fconn,this.getString( getSelectedIndex() ) );
                            OpenFileThread.start();
                        }
                        else
                        {
                            // Info message to listbox
                            append( NOT_TEXT_FILE,null );
                        }
                    }

                    // "Change Folder" Action was selected on Commands.
                    if ( ( String )iModel.GetCommandVector().elementAt( 3 ) == c.getLabel() )
                    {
                        changeDir();
                    }

                    // "Create Folder" Action was selected on Commands.
                    if ( ( String )iModel.GetCommandVector().elementAt( 4 ) == c.getLabel() )
                    {
                        // Show the Dialog for MakeDir, zero is functionality code (makedir)
                        // 50 param in MaxLength on the TextBox.
                        iDialogs.shotTextBox( MKDIR_DTITLE,DIR_NAME+DIR_SEPARATOR,50,0 );
                    }

                    // "Rename" Action was selected on Commands.
                    if ( ( String )iModel.GetCommandVector().elementAt( 5 ) == c.getLabel() )
                    {
                        // Check if selected item is Info item.
                        if ( isInfoElement( this.getString( getSelectedIndex() ) )==false )
                        {
                            // Show the Dialog for MakeDir, 1 is functionality code (rename)
                            // 50 param in MaxLength on the TextBox.
                            iDialogs.shotTextBox( REN_DTITLE,this.getString( getSelectedIndex() ),50,1 );
                        }
                        else
                        {
                            // Info message to listbox
                            append( NOTHING_TO_DO,null );
                        }
                    }

                    // "Permissions" Action was selected on Commands.
                    if ( ( String )iModel.GetCommandVector().elementAt( 6 ) == c.getLabel() )
                    {
                        // Check if selected item is Info item.
                        if ( isInfoElement( this.getString( getSelectedIndex() ) )==false )
                        {
                            // Initialize thread and launch it.
                            FileHandlerThread_permissions permissionsThread = new FileHandlerThread_permissions( this,fconn,this.getString( getSelectedIndex() ) );
                            permissionsThread.start();
                        }
                        else
                        {
                            // Info message to listbox
                            append( NOTHING_TO_DO,null );
                        }
                    }

                    // "Size" Action was selected on Commands.
                    if ( ( String )iModel.GetCommandVector().elementAt( 7 ) == c.getLabel() )
                    {
                        // Check if selected item is Info item.
                        if ( isInfoElement( this.getString( getSelectedIndex() ) )==false )
                        {
                            // Initialize thread and launch it.
                            FileHandlerThread_fileSize fileSizeThread = new FileHandlerThread_fileSize( this,fconn,this.getString( getSelectedIndex() ) );
                            fileSizeThread.start();
                        }
                        else
                        {
                            // Info message to listbox
                            append( NOTHING_TO_DO,null );
                        }
                    }

                    // "URL" Action was selected on Commands.
                    if ( ( String )iModel.GetCommandVector().elementAt( 8 ) == c.getLabel() )
                    {
                        // Check if selected item is Info item.
                        // There is one controol boolean print/update member root variable.
                        if ( isInfoElement( this.getString( getSelectedIndex() ) )==false )
                        {
                            // Initialize thread and launch it.
                            // The last boolena param is wheater the result is printed or
                            // is USED_ROOT setted according to listbox's selection
                            FileHandlerThread_GetURL getURLThread = new FileHandlerThread_GetURL( this,fconn,this.getString( getSelectedIndex() ),true );
                            getURLThread.start();
                        }
                        else
                        {
                            // Info message to listbox
                            append( NOTHING_TO_DO,null );
                        }
                    }

                    // "Delete" Action was selected on Commands.
                    if ( ( String )iModel.GetCommandVector().elementAt( 9 ) == c.getLabel() )
                    {
                        // Check if selected item is Info item.
                        if ( isInfoElement( this.getString( getSelectedIndex() ) )==false )
                        {
                            // Initialize thread and launch it.
                            FileHandlerThread_delete deleteThread = new FileHandlerThread_delete( this,fconn,this.getString( getSelectedIndex() ) );
                            deleteThread.start();
                        }
                        else
                        {
                            // Info message to listbox
                            append( NOTHING_TO_DO,null );
                        }
                    }

                    // "Used Mem" Action was selected on Commands.
                    if (  ( String )iModel.GetCommandVector().elementAt( 10 ) == c.getLabel() )
                    {
                        // Initialize thread and launch it.
                        FileHandlerThread_usedSize usedSizeThread = new FileHandlerThread_usedSize( this,fconn );
                        usedSizeThread.start();
                    }

                    // "Free size" Action was selected on Commands.
                    if ( ( String )iModel.GetCommandVector().elementAt( 11 ) == c.getLabel() )
                    {
                        // Initialize thread and launch it.
                        FileHandlerThread_AvailSize availSizeThread = new FileHandlerThread_AvailSize( this,fconn );
                        availSizeThread.start();
                    }

                    // "Select volume" Action was selected on Commands.
                    if (  ( String )iModel.GetCommandVector().elementAt( 12 ) == c.getLabel() )
                    {
                        // Clear listbox
                        this.deleteAll();
                        upDateVolumes();
                    }
                break;
                } // End of Case 2.
            }// End of Switch
        } // End of Else If (Handle other than select event on keyboard.)
    } // EOClass


    // Create menu view according to vector member. This is contructed in Model.
    public void createMenuView( Enumeration menuItems )
    {
        int index;
        // Delete All Command from Displayable surface.
        for ( index=0;index<commandVector.size();index++ )
        {
            // Remove all command from displayable.
            // Command are from Vector.
            removeCommand( ( Command )commandVector.elementAt( index ) );
        }
        // Flush the local CommandVector
        commandVector.removeAllElements();

        if ( menuItems == null )
        {}
        else
        {
            // Reset
            index=0;
            // Add New commands to vector
            while (  menuItems.hasMoreElements() )
            {
                // Add Commands from Model or from Volume List to displayable.
                commandVector.addElement( new Command( ( String )menuItems.nextElement(), Command.SCREEN, 1 ) );
                // Add commands also to vector.
                addCommand( ( Command )commandVector.elementAt( index ) );
                index++;
            }
        }
    }

    // Alert the user when a security exception occurs
    public void securityAlert()
    {
        this.deleteAll();
        this.append("Select Volume",null);
        UI_STATE = VOLUME_SELECTION_STATE;

        // shows splash screen
        String text = "Access Not Allowed";

        Alert splashScreen = new Alert(null, text, null, null);
        Display.getDisplay(this.iMidlet).setCurrent(splashScreen);
    }

    // Generic print result function for all data/selections.
    public void printResult( boolean aClear, Enumeration printResult )
    {
        if( aClear==true )
        {
            // Clear the listbox first
            this.deleteAll();
        }
        while (  printResult.hasMoreElements()  )
        {
            // Add items to listbox from printResult enumeration parameter.
            this.append( ( String )printResult.nextElement(),null );
        }
    }


    public void OpenFileConn()
    {
        try
        {
            // Close previous connection first
            closeFileConn();
            // Open FileConnection accrording to member
            this.fconn = ( FileConnection )Connector.open( getUsedRoot() );
        }
        catch(java.lang.Exception se)
        {
              // Get roots from FileSystemRegistry interface's method.

              Enumeration rootEnum = FileSystemRegistry.listRoots();

              // Create view of available menu
              createMenuView(rootEnum);

              setUsedRoot(null, 0);

              deleteAll();
              append(se.getMessage()+ "\n",null);
              append("Select Volume", null);

              UI_STATE = VOLUME_SELECTION_STATE;
        }
    }


    // Generic connection closing method.
    public void closeFileConn()
    {
        try
        {
            if ( ! ( this.fconn==null ) )
            {
                this.fconn.close();
            }
        }
        // Catch all exceptions and forward them on.
        catch ( Exception e )
        {
            FileHandlerErrorHandler.handleError( e,this );

        }
    }


    public void upDateVolumes()
    {
        // Create null Enumeration is order to reset CommandVector
        Enumeration Enum=null;

        // Create Commands
        createMenuView( Enum );

        // Create Thread for ListRoots and start it!
        FileHandlerThread_ListRoots listRootsThread = new FileHandlerThread_ListRoots( this );
        listRootsThread.start();

        // Set Action to Volme selection
        UI_STATE  = VOLUME_SELECTION_STATE;// Select volume state

        // Info message.
        this.append( LIST_SELECT,null );
    }

    // Changes the current connection pointing directory
    private void changeDir()
    {
        // Chck if listbox's item ends with / (folder)
        // JSR75 API also has isDirectory() method to do this in thread.
        if ( ( this.getString( getSelectedIndex() ).endsWith( "/" )==true ) )
        {
            // Set the member USED_ROOT with append param (1).
            setUsedRoot( this.getString( getSelectedIndex() ),1 );

            // Open Root connection
            OpenFileConn();

            if (this.fconn == null)
              return;
            // List dirs
            FileHandlerThread_List listThread = new FileHandlerThread_List( this,fconn );
            listThread.start();
        }
    }

    // Go back one level
    private void upOneLevel( boolean aListDirectory )
    {
        try
        {
            // pass parent indication ( .. ) to GetURL method with flag that controll's the thread
            // so that no result is printed on screen (false)
            FileHandlerThread_GetURL getURLThread = new FileHandlerThread_GetURL( this,fconn,"..",false );
            getURLThread.start();

            // Is list directory applied after?
            if ( aListDirectory )
            {

                // If we are going to display the directory contents wait
                // for get thread to complete so that we display the correct
                // contents.
                while (getURLThread.isAlive()) Thread.sleep(500);

                // If an error occured during the execution of the thtread the
                // UI_STATE is changed.
                if (UI_STATE == VOLUME_SELECTION_STATE)
                   return;

                // Get the refreshed list
                FileHandlerThread_List listThread = new FileHandlerThread_List( this,fconn );
                listThread.start();
            }
        }
            // Catch all exceptions and forward them on.
        catch ( Exception e )
        {
            FileHandlerErrorHandler.handleError( e,this );
        }
    }

    // Generic getter for Private member (connection uses!)
    public String getUsedRoot()
    {
        return USED_ROOT;
    }

    // Generic setter for Private member USED_ROOT
    public void setUsedRoot( String aRoot,int controll )
    {
        switch ( controll )
        {
            case 0:// volume select
            {
                USED_ROOT = "file:///" + aRoot;
                break;
            }
            case 1:// append dir to root
            {
                USED_ROOT = USED_ROOT + aRoot;
                break;
            }
            case 2: // whole
            {
                USED_ROOT = aRoot;
            }
        }
    }

    // This method checks if given para ends with information mark, dot.
    private boolean isInfoElement( String aString )
    {
        // the purpose of this fuction is to check wheater given string ends with dot.
        // If so, Listbox's element at hand is charaterized as information, therefore
        // No thread is to rise.
        if ( aString.endsWith( "." )==true )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    // This is called when TextBox dialog end wih DONE.
    public void AfterDialog( String aText, int aFunction )
    {
        // Get Current root
        String aCurrentRoot = getUsedRoot();

        // This is called after each dialog based functionality.
        switch ( aFunction )
        {
            case 0:    // MakeDir command
            {
                // Set the New folder connection
                setUsedRoot( aText,1 );
                // open connection
                OpenFileConn();
                if (this.fconn == null)
                      break;

                // Create new Filder according to user's input.
                FileHandlerThread_makeDir makeDirThread = new FileHandlerThread_makeDir( this,fconn,aText,aCurrentRoot );
                makeDirThread.start();
                break;
            }
            case 1:  // Rename command
            {
                // Set the Connection to current file/folder ( selected item )
                setUsedRoot( this.getString( getSelectedIndex() ),1 );

                // open connection
                OpenFileConn();

                if (this.fconn == null)
                      break;

                // Call the Rename thread
                FileHandlerThread_rename renameThread = new FileHandlerThread_rename( this,fconn,aText,this.getString( getSelectedIndex() ),aCurrentRoot );
                renameThread.start();
                break;
            }
            case 2: // Call Write
            {
                // Initialize the WriteFile thread and launch it.
                FileHandlerThread_writefile WriteFileThread = new FileHandlerThread_writefile( this,fconn,this.getString( getSelectedIndex() ),aText );
                WriteFileThread.start();
            }
        }
    }

    // When user opens Text file, all content are set to TextBox.
    // Open functionality is done in Thread which calls this method in order to fill TextBox.
    public void triggerOpenDialog( String aText )
    {
        // para zero ( 0 ) means that TextBox constructs as aText's lengt amount.
        // Param 2 meas OPEN FILE.
        iDialogs.shotTextBox( "Edit file '" + this.getString( getSelectedIndex() )+ "'.",aText,0,2 );
    }


    // Classe's members are here.
    private static final String LIST_TITLE = "FileHandler Example.";
    private static final String FORM_EXIT  = "Exit";
    private static final String FORM_START  = "Start";
    private static final String LIST_START = "Select start from option menu to begin.";
    private static final String LIST_SELECT = "Select volume from menu.";
    private static final String FILE_FAIL = "File Connection ( JSR75 ) was not found on system. Close application.";
    private static final String DIR_NAME = "New Folder";
    private static final String DIR_SEPARATOR = "/";
    private static final String MKDIR_DTITLE  ="Enter the Folder name to create.";
    private static final String REN_DTITLE = "Enter new Name.";
    private static final String NOTHING_TO_DO = "Nothing to do.";
    private static final String NOT_TEXT_FILE = "Only text files are supported.";

    // This member is used to store current connector.open location.
    // This will be updated and user in a OpenConnection method.
    private String USED_ROOT = null;

    // This int determinates the ListBox behaviour
    // 1 = Volume Selection
    // 2 = Action mode (file operations)
    private int UI_STATE;

    private String initialRoot;

    private final Command exitCommand = new Command( FORM_EXIT, Command.EXIT, 1 );
    private final Command startCommand = new Command( FORM_START, Command.SCREEN, 1 );

    // Vector to inhold all Command available to user.
    private final Vector commandVector = new Vector();

    // References to other classes.
    private final FileHandlerMIDlet iMidlet;
    private final FileHandlerModel iModel;
    private final FileHandlerDialogs iDialogs;

    private final int IDLE_STATE = 0;
    private final int VOLUME_SELECTION_STATE = 1;
    private final int ACTION_COMMANDS_STATE = 2;

    // FileSystemListener
    private FileHandlerModelFileSystemListener iListener;

    // This is the actual connector member.
    private FileConnection fconn;

}// End of file
