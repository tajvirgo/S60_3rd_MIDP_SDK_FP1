/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.FileHandler;

import java.io.*;
import java.lang.Long;
import java.lang.String;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.file.*;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.*;

/**
This class is for user command handling. All commands
targeted on specific volume are added to a vector.
With this the Action menu is contructed.
*/
public class FileHandlerModel
{
    // Default classes contructor
    public FileHandlerModel( FileHandlerViewController amidlet )
    {
        this.iMidlet = amidlet;
        CommandVector = new Vector();
        CommandVector.addElement( COMMAND_UP_ONE );
        CommandVector.addElement( COMMAND_LIST );
        CommandVector.addElement( COMMAND_OPEN );
        CommandVector.addElement( COMMAND_CHANGE_DIR );
        CommandVector.addElement( COMMAND_MAKEDIR );
        CommandVector.addElement( COMMAND_RENAME );
        CommandVector.addElement( COMMAND_PERMISSIONS );
        CommandVector.addElement( COMMAND_DIRSIZE );
        CommandVector.addElement( COMMAND_GETURL );
        CommandVector.addElement( COMMAND_DELETE );
        CommandVector.addElement( COMMAND_USEDMEM );
        CommandVector.addElement( COMMAND_SIZE );
        CommandVector.addElement( COMMAND_MENU );
    }
    public Vector GetCommandVector()
    {
        // Returns created vector with command strings in.
        return CommandVector;
    }
    public void ListActionCommands()
    {
        // Creates the menu structure with command vector's strings.
        iMidlet.createMenuView( CommandVector.elements() );
    }

    // Classe's members are here.
    // Construct commands with litteral.
    private static final String COMMAND_UP_ONE = "Back";
    private static final String COMMAND_LIST="List Directory";
    private static final String COMMAND_OPEN = "Open & Edit File";
    private static final String COMMAND_CHANGE_DIR = "Change Folder";
    private static final String COMMAND_MAKEDIR = "Create Folder";
    private static final String COMMAND_RENAME = "Rename";
    private static final String COMMAND_PERMISSIONS = "Permissions";
    private static final String COMMAND_DIRSIZE = "Size";
    private static final String COMMAND_GETURL = "URL";
    private static final String COMMAND_DELETE = "Delete";
    private static final String COMMAND_USEDMEM = "Used Mem";
    private static final String COMMAND_SIZE = "Free size";
    private static final String COMMAND_MENU = "Select volume";

    // All command are stored in a Vector object
    private Vector CommandVector;

    // Reference to Controller
    private final FileHandlerViewController iMidlet;
}


/*****************************************************************************/


/**
FileHandlerThread_ListRoots Class is a thread that List's All
Roots mounted on file system.
*/
class FileHandlerThread_ListRoots extends Thread
{
    // Default classes contructor
    public FileHandlerThread_ListRoots( FileHandlerViewController aMidlet )
    {
        this.iMidlet = aMidlet;
    }
    // Threads starting point is Run().
    public void run()
    {
        try
        {
            // Get roots from FileSystemRegistry interface's method.
            Enumeration rootEnum = FileSystemRegistry.listRoots();

            // Create view of available menu
            iMidlet.createMenuView( rootEnum );
        }

        // Catch all exceptions and forward them on.
        catch ( Exception e )
        {
            FileHandlerErrorHandler.handleError( e,iMidlet );
        }
    }
    // Classe's members are here.
    private final FileHandlerViewController iMidlet;
}



/**
FileHandlerThread_List Class is a thread that List's
folder/ Root's content.
*/
class FileHandlerThread_List extends Thread
{
    // Default classes contructor
    public FileHandlerThread_List( FileHandlerViewController aMidlet,
                                   FileConnection aFconn )
    {
        this.iMidlet = aMidlet;
        this.iFconn = aFconn;
    }
    // Threads starting point is Run().
    public void run()
    {
        Enumeration listEnum=null;
        Vector folder = new Vector();
        try
        {
            // Get the contents
            if (iFconn.canRead())
            {
              listEnum = iFconn.list();
              folder.addElement("Directory changed to");
              folder.addElement("'" + iMidlet.getUsedRoot() + "'.");

              if (listEnum.hasMoreElements() == false) {
                // Folder is empty.
                folder.addElement("<Folder is empty>.");

                // Print Result. "true" means ClearScreen before.
                iMidlet.printResult(true, folder.elements());
              }
              else {
                // loop every list contents to vector.
                for (int i = 0; listEnum.hasMoreElements(); ) {
                  folder.addElement(listEnum.nextElement());
                }

                // Print Result. "true" means ClearScreen before.
                iMidlet.printResult(true, folder.elements());
              }
            }
            else
            {
              // time for some error recovery.
              // Get roots from FileSystemRegistry interface's method.
              iMidlet.setVolumeState();
            }
        }
        catch ( Exception e )
        {
            FileHandlerErrorHandler.handleError( e,iMidlet );
        }
    }

    // Classe's members are here.
    private final FileHandlerViewController iMidlet;
    private final FileConnection iFconn;

}

/**
FileHandlerThread_AvailSize Class is a thread that
determines the free memory that is available
on the file system the file or directory resides on
*/
class FileHandlerThread_AvailSize extends Thread
{
    // Default classes contructor
    public FileHandlerThread_AvailSize( FileHandlerViewController aMidlet,
                                        FileConnection aFconn )
    {
        this.iMidlet = aMidlet;
        this.iFconn = aFconn;
    }

    // Threads starting point is Run().
    public void run()
    {
        Vector printVector = new Vector();
        try
        {
            // Add the result to Vector in by casting it to String.
            printVector.addElement(  Long.toString(  iFconn.availableSize() )  +" bytes."  );

            // Print Result. "false" means not to ClearScreen before.
            iMidlet.printResult( false,printVector.elements() );
        }
        // Catch all exceptions and forward them on.
        catch ( Exception e )
        {
            FileHandlerErrorHandler.handleError( e,iMidlet );
        }

    }
    // Classe's members are here.
    private final FileHandlerViewController iMidlet;
    private final FileConnection iFconn;
}


/**
FileHandlerThread_usedSize Class is a thread that
determines the used memory of a file
system the connection's target resides on.
*/
class FileHandlerThread_usedSize extends Thread
{
    // Default classes contructor
    public FileHandlerThread_usedSize( FileHandlerViewController aMidlet,
                                       FileConnection aFconn )
    {
        this.iMidlet = aMidlet;
        this.iFconn = aFconn;
    }

    // Threads starting point is Run().
    public void run()
    {
        Vector printVector = new Vector();
        try
        {
            // Add the result to Vector in by casting it to String.
            printVector.addElement(  Long.toString(  iFconn.usedSize() )  +" bytes."  );

            // Print Result. "false" means not to ClearScreen before.
            iMidlet.printResult( false,printVector.elements() );
        }

        // Catch all exceptions and forward them on.
        catch ( Exception e )
        {
            FileHandlerErrorHandler.handleError( e,iMidlet );
        }

    }

    // Classe's members are here.
    private final FileHandlerViewController iMidlet;
    private final FileConnection iFconn;
}


/**
FileHandlerThread_fileSize Class is a thread that
determines the size of a file on the file system.
*/
class FileHandlerThread_fileSize extends Thread
{
    // Default classes contructor
    public FileHandlerThread_fileSize( FileHandlerViewController aMidlet,
                                       FileConnection aFconn,
                                       String aFileName )
    {
        this.iMidlet = aMidlet;
        this.iFconn = aFconn;
        this.iFileName = aFileName;
    }

    // Threads starting point is Run().
    public void run()
    {
        Vector printVector = new Vector();
        try
        {
            // Reset the Current's fileconnectin to iFileName.
            // The Controller's private member USED_ROOT is NOT
            // updated!
            iFconn.setFileConnection( iFileName );

            // is it file is a folder? Add propper info to vector
            if ( iFileName.endsWith( "/" )==true )
            {
                //DirectorySize's flag is subdirs also.
                printVector.addElement( "'" + iFileName +"' is " + Long.toString( iFconn.directorySize( true ) ) +" bytes."   );
            }
            else
            {
                // FileSize is put to Vector
                printVector.addElement( "'" + iFileName +"' is " + Long.toString( iFconn.fileSize() ) + " bytes."   );
            }

            // Print Result. "false" means not to ClearScreen before.
            iMidlet.printResult( false,printVector.elements() );

            // Reset the Connection to root ( USED_ROOT points to current folder )
            iMidlet.OpenFileConn();
        }
        // Catch all exceptions and forward them on.
        catch ( Exception e )
        {
            FileHandlerErrorHandler.handleError( e,iMidlet );
        }
    }
    // Classe's members are here.
    private final FileHandlerViewController iMidlet;
    private final FileConnection iFconn;
    private final String iFileName;
}


/**
FileHandlerThread_permissions Class is a thread that
determines the read/Write permission of a file on the file system.
*/
class FileHandlerThread_permissions extends Thread
{
    // Default classes contructor
    public FileHandlerThread_permissions ( FileHandlerViewController aMidlet,
                                           FileConnection aFconn,
                                           String aFileName )
    {
        this.iMidlet = aMidlet;
        this.iFconn = aFconn;
        this.iFileName = aFileName;
    }
    // Threads starting point is Run().
    public void run()
    {
        // Ini variables.
        String canRead = null;
        String CanWrite = null;
        String type = null;

        // Set the type string
        if ( iFileName.endsWith( "/" )==true )
        {
            type  = DIR_TYPE;
        }
        else
        {
            type = FILE_TYPE;
        }

        try
        {
            // ReSet the Current's fileconnectin to iFileName.
            // The Controller's private member USED_ROOT is NOT
            // updated!
            iFconn.setFileConnection( iFileName );

            // Get Write Permissions
            if ( iFconn.canWrite() == true )
            {
                CanWrite =  type +  " '" + iFileName + "' is writable.";
            }
            else
            {
                CanWrite = type +  " '" + iFileName + "' is not writable.";
            }

            // Get Read Permissions
            if ( iFconn.canRead()==true )
            {
                canRead = type +  " '" + iFileName + "' is readable.";
            }
            else
            {
                canRead = type +  " '" + iFileName + "' is not readable.";
            }

            // Create Print Vector and add data into it.
            Vector printVector = new Vector();
            printVector.addElement(  canRead  );
            printVector.addElement(  CanWrite  );

            // Print Result. "false" means not to ClearScreen before.
            iMidlet.printResult( false,printVector.elements() );

            // Reset the Connection to root ( USED_ROOT points to current folder )
            iMidlet.OpenFileConn();

        }
        // Catch all exceptions and forward them on.
        catch ( Exception e )
        {
            FileHandlerErrorHandler.handleError( e,iMidlet );
        }
    }

    // Classe's members are here.
    private final FileHandlerViewController iMidlet;
    private final FileConnection iFconn;
    private final String iFileName;
    private static final String DIR_TYPE = "Directory";
    private static final String FILE_TYPE = "File";
}


/**
FileHandlerThread_permissions Class is a thread that
returns the full file URL including the scheme, host,
and path from where the file or directory specified.
*/
class FileHandlerThread_GetURL extends Thread
{
    // Default classes contructor
    public FileHandlerThread_GetURL ( FileHandlerViewController aMidlet,
                                      FileConnection aFconn,
                                      String aFileName,
                                      boolean aPrintResult )
    {
        this.iMidlet = aMidlet;
        this.iFconn = aFconn;
        this.iFileName = aFileName;
        // If the URL is wanted to print / update.
        this.iPrintResult = aPrintResult;
    }
    // Threads starting point is Run().
    public void run()
    {
        try
        {
            // Reset the Current's fileconnectin to iFileName.
            // The Controller's private member USED_ROOT is NOT
            // updated!

            iFconn.setFileConnection(iFileName);

            // Get The Url
            String fileURL = iFconn.getURL();

            if ( iPrintResult==true )
            {
                Vector printVector = new Vector();

                // Dot must be in place in order to "mark" it as information entry
                printVector.addElement( fileURL+"." );

                // Print Result. "false" means not to ClearScreen before.
                iMidlet.printResult( false,printVector.elements() );

                // Reset the Connection to root ( USED_ROOT points to current folder )
                iMidlet.OpenFileConn();

            }
            else
            {
                // Set the contoller's member USED_ROOT to fileURL's value nativly
                // ( 2nd param 2 does that )
                iMidlet.setUsedRoot( fileURL,2 );
            }
        }
        // Catch all exceptions and forward them on.
        catch (SecurityException se)
        {
            // time for some error recovery.
            // Get roots from FileSystemRegistry interface's method.
            Enumeration rootEnum = FileSystemRegistry.listRoots();

            // Create view of available menu
            iMidlet.createMenuView( rootEnum );
            //rootEnum.nextElement().toString()
            iMidlet.setUsedRoot( null, 0);

            iMidlet.securityAlert();
        }
        // Catch all exceptions and forward them on.
        catch ( Exception e )
        {
            FileHandlerErrorHandler.handleError( e,iMidlet );
        }
    }
    // Classe's members are here.
    private final FileHandlerViewController iMidlet;
    private final FileConnection iFconn;
    private final String iFileName;
    private final boolean iPrintResult;
}

/**
FileHandlerThread_makeDir Class is a thread that
Creates a directory corresponding to the
directory string.
*/
class FileHandlerThread_makeDir extends Thread
{
    // Default classes contructor
    public FileHandlerThread_makeDir( FileHandlerViewController aMidlet,
                                      FileConnection aFconn,
                                      String aDirName,
                                      String aPreviusRoot )
    {
        this.iMidlet = aMidlet;
        this.iFconn = aFconn;
        this.iDirName = aDirName;
        this.iPreviusRoot = aPreviusRoot;
    }

    // Threads starting point is Run().
    public void run()
    {
        Vector printVector = new Vector();
        try
        {

          // If the dir does not exists, create it.
          if ( !iFconn.exists() )
          {
            // Create!
            iFconn.mkdir();
            printVector.addElement("Directory ");
            printVector.addElement("'" + iDirName + "'");
            printVector.addElement("was created.");
          }
          else
          {
            // Already there
            printVector.addElement("Directory ");
            printVector.addElement("'" + iDirName + "'");
            printVector.addElement("was not created.");
          }

          // Print Result. "true" means ClearScreen before.
          iMidlet.printResult(true, printVector.elements());

          // Set the contoller's member USED_ROOT to iPreviusRoot's value nativly
          // ( 2nd param 2 does that )
          iMidlet.setUsedRoot(iPreviusRoot, 2);

          // Reset the Connection to root ( USED_ROOT points to current folder )
          iMidlet.OpenFileConn();
        }
        catch (ConnectionClosedException cce)
        {
          // no action
        }
         // Catch all exceptions and forward them on.
        catch ( Exception e )
        {
            FileHandlerErrorHandler.handleError( e,iMidlet );
        }
    }
    // Classe's members are here.
    private final FileHandlerViewController iMidlet;
    private final FileConnection iFconn;
    private final String iDirName;
    private final String iPreviusRoot;
}

/**
FileHandlerThread_delete Class is a thread that
Deletes the file or directory specified.
*/
class FileHandlerThread_delete extends Thread
{
    // Default classes contructor
    public FileHandlerThread_delete( FileHandlerViewController aMidlet,
                                     FileConnection aFconn,
                                     String aName )
    {
        this.iMidlet = aMidlet;
        this.iFconn = aFconn;
        this.iName = aName;
    }

    // Threads starting point is Run().
    public void run()
    {
        Vector printVector = new Vector();
        String type = null;

        // Set the type string
        if ( iName.endsWith( "/" )==true )
        {
            type  = DIR_TYPE;
        }
        else
        {
            type = FILE_TYPE;
        }

        try
        {
            // ReSet the Current's fileconnectin to iFileName.
            // The Controller's private member USED_ROOT is NOT
            // updated!
            iFconn.setFileConnection( iName );

            // Delete!
            iFconn.delete();
            printVector.addElement( type + " '"+ iName + "'");
            printVector.addElement( "deleted." );

            // Print Result. "true" means ClearScreen before.
            iMidlet.printResult( true,printVector.elements() );

            // Reset the Connection to root
            // ( USED_ROOT points to current folder )
            iMidlet.OpenFileConn();
        }

        // Catch IO Exception and correct
        catch ( IOException IOe )
        {
            if ( type.equals( FILE_TYPE ) )
            {
                printVector.addElement( type + " '"+ iName + "'");
                printVector.addElement( "not deleted: Unaccessible." );
            }
            else
            {
                printVector.addElement( type + " '"+ iName + "'");
                printVector.addElement( "not deleted: Not empty or ReadOnly." );
            }

            // Print Result. "false" means not to ClearScreen before.
            iMidlet.printResult( false,printVector.elements() );

            // Reset the Connection to root
            // ( USED_ROOT points to current folder )
            iMidlet.OpenFileConn();

        }
        // Catch all other exceptions and forward them on.
        catch ( Exception e )
        {
            FileHandlerErrorHandler.handleError( e,iMidlet );
        }
    }

    // Classe's members are here.
    private final FileHandlerViewController iMidlet;
    private final FileConnection iFconn;
    private static final String DIR_TYPE = "Directory";
    private static final String FILE_TYPE = "File";
    private final String iName;
}



/**
FileHandlerThread_rename Class is a thread that
Reanmes the file or directory specified.
*/
class FileHandlerThread_rename extends Thread
{
    // Default classes contructor
    public FileHandlerThread_rename ( FileHandlerViewController aMidlet,
                                      FileConnection aFconn,
                                      String aNewName,
                                      String aOldName,
                                      String aPrevousRoot )
    {
        this.iMidlet = aMidlet;
        this.iFconn = aFconn;
        this.iNewName = aNewName;
        this.iOldName = aOldName;
        this.iPrevousRoot = aPrevousRoot;
    }

    // Threads starting point is Run().
    public void run()
    {
        Vector printVector = new Vector();
        String type = null;

        // Set the type string
        if ( iNewName.endsWith( "/" )==true )
        {
            type  = DIR_TYPE;
        }
        else
        {
            type = FILE_TYPE;
        }

        try
        {
            // ReSet the Current's fileconnectin to iNewName.
            // The Controller's private member USED_ROOT is NOT
            // updated!
            iFconn.rename( iNewName );

            printVector.addElement( type + " '" + iOldName + "' renamed as '"+ iNewName +  "' ." );

            // Print Result. "true" means ClearScreen before.
            iMidlet.printResult( true,printVector.elements() );

            // Set the contoller's member USED_ROOT to iPreviusRoot's value nativly
            // ( 2nd param 2 does that )
            iMidlet.setUsedRoot( iPrevousRoot,2 );

            // Reset the Connection to root
            // ( USED_ROOT points to current folder )
            iMidlet.OpenFileConn();
        }

        // Catch IO Exception and correct
        catch ( IOException IOe )
        {
            if ( type.equals( FILE_TYPE ) )
            {
                printVector.addElement( type + " '"+ iOldName +  "' not renamed: Unaccessible." );
            }
            else
            {
                printVector.addElement( type + " '"+ iOldName +  "' not renamed." );
            }

            // Print Result. "false" means not to ClearScreen before.
            iMidlet.printResult( false,printVector.elements() );

            // Reset the Connection to root
            // ( USED_ROOT points to current folder )
            iMidlet.OpenFileConn();
        }
        // Catch all other exceptions and forward them on.
        catch ( Exception e )
        {
            FileHandlerErrorHandler.handleError( e,iMidlet );
        }
    }
    // Classe's members are here.
    private final FileHandlerViewController iMidlet;
    private final FileConnection iFconn;
    private static final String DIR_TYPE = "Directory";
    private static final String FILE_TYPE = "File";
    private final String iNewName;
    private final String iOldName;
    private final String iPrevousRoot;
}

/**
FileHandlerThread_openfile Class is a thread that
Opens the file specified and show it on TextBox controll to edit.
*/
class FileHandlerThread_openfile extends Thread
{
    // Default classes contructor
    public FileHandlerThread_openfile ( FileHandlerViewController aMidlet,
                                        FileConnection aFconn,
                                        String aFilename )
    {
        this.iMidlet = aMidlet;
        this.iFconn = aFconn;
        this.iFilename = aFilename;
    }

    // Threads starting point is Run().
    public void run()
    {
        try
        {
            // ReSet the Current's fileconnectin to iFileName.
            // The Controller's private member USED_ROOT is NOT
            // updated!
            iFconn.setFileConnection( iFilename );

            // Check that requested file is not larger than 4k bytes.
            // Just to be safe.
            if ( iFconn.fileSize() > 4096 )
            {
                Vector printVector = new Vector();
                printVector.addElement( "File '"+ iFilename + "' is larger than 4k." );

                // Print Result. "false" means not to ClearScreen before.
                iMidlet.printResult( false,printVector.elements() );
            }
            else
            {
                StringBuffer buf = new StringBuffer();
                InputStreamReader in = new InputStreamReader( iFconn.openInputStream() );
                int c = 0;
                // Read the Stream char by char into StringBuffer
                while ( ( c = in.read() ) != -1 )
                {
                    buf.append( ( char )c );
                }
                in.close();

                // Finally trigger the Dialog for Edit
                iMidlet.triggerOpenDialog( buf.toString() );
            }

            // Reset the Connection to root
            // ( USED_ROOT points to current folder )
            iMidlet.OpenFileConn();
        }
        catch ( Exception e )
        {
            FileHandlerErrorHandler.handleError( e,iMidlet );
        }
    }
    // Class's members are here.
    private final FileHandlerViewController iMidlet;
    private final FileConnection iFconn;
    private final String iFilename;
}


/**
FileHandlerThread_writefile Class is a thread that
Writes the file specified.
*/
class FileHandlerThread_writefile extends Thread
{
    // Default classes contructor
    public FileHandlerThread_writefile ( FileHandlerViewController aMidlet,
                                         FileConnection aFconn,
                                         String aFileName,
                                         String aText )
    {
        this.iMidlet = aMidlet;
        this.iFconn = aFconn;
        this.iFileName = aFileName;
        this.iText = aText;
    }
    // Threads starting point is Run().
    public void run()
    {
        Vector printVector = new Vector();
        try
        {
            // ReSet the Current's fileconnectin to iFileName.
            // The Controller's private member USED_ROOT is NOT
            // updated!
            iFconn.setFileConnection( iFileName );

            // Delete the "old" file
            iFconn.delete();

            // Create it again from empty.
            iFconn.create();

            // Stream the payload in.
            OutputStreamWriter out = new OutputStreamWriter( iFconn.openOutputStream() );
            out.write( iText,0,iText.length() );
            out.close();

            // Print info
            printVector.addElement( "File '" + iFileName + "' edited." );
            iMidlet.printResult( true,printVector.elements() );

            // Reset the Connection to root
            // ( USED_ROOT points to current folder )
            iMidlet.OpenFileConn();
        }
        catch ( Exception e )
        {
            FileHandlerErrorHandler.handleError( e,iMidlet );
        }
    }
    // Classe's members are here.
    private final FileHandlerViewController iMidlet;
    private final FileConnection iFconn;
    private final String iFileName;
    private final String iText;
}
// End of file
