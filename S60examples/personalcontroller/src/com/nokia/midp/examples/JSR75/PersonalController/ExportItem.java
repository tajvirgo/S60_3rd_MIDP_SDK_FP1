/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.PersonalController;

import javax.microedition.lcdui.*;

import javax.microedition.pim.*;
import javax.microedition.io.Connector;
import javax.microedition.io.file.*;

import java.io.OutputStream;
import java.io.IOException;
import java.util.Enumeration;

/**
    ExportItem handles exporting items to files
*/

public class ExportItem extends Form implements CommandListener{

    private static final String TITLE_TEXT = "Export item";

    private final Command backCommand = new Command("Back", Command.BACK, 1);
    private final Command selectCommand = new Command("Export", Command.OK, 1);

    private final int listTypeIndicator;
    private PIMList currentPIMList;
    private final Displayable caller;
    private final PersonalController midlet;
    private PIMItem usedPIMItem;

    private ChoiceGroup groupRoots;
    private ChoiceGroup groupEncoding;
    private TextField fileName;

    private final int DATA_SCREEN_TYPE_VIEW = 0;
    private final int DATA_SCREEN_TYPE_SEARCH = 1;
    private static final int STRING_VALUE_SIZE = 150;
    private final int INDEX_OF_FILE_IN_URL = 8;

    public static final String[] encodingArray = { "UTF-8", "UTF-16", "ISO-8859-1", "US-ASCII" };
    //ISO-8859-1, ISO-8859-6 (Arabic), UTF-8, UTF-16, UTF-16BE, UTF-16LE, US-ASCII, ISO-10646-UCS-2 (=UCS-2), Big5 (chinese traditional)
    String schemePart = "file:///";

    public ExportItem(PersonalController midlet, Displayable caller, int listTypeIndicator, PIMList currentPIMList, PIMItem usedPIMItem) throws PIMException, IOException{

        super(TITLE_TEXT);
        this.midlet = midlet;
        this.caller = caller;
        this.listTypeIndicator = listTypeIndicator;
        this.currentPIMList=currentPIMList;
        this.usedPIMItem=usedPIMItem;

        addCommand(backCommand);
        addCommand(selectCommand);
        setCommandListener(this);
        InitializeTheForm();
    }

    public void InitializeTheForm(){

        // Choose encodings
        groupEncoding = new ChoiceGroup("Choose encoding", ChoiceGroup.POPUP, encodingArray, null);
        append(groupEncoding);

        // This root query should be in thread to avoid potential deadlock
        groupRoots = new ChoiceGroup( "Choose root", ChoiceGroup.POPUP );
        Enumeration roots = FileSystemRegistry.listRoots();

        String privateDir = System.getProperty("fileconn.dir.private");

        // Append system property location as first choice if available.
        if (privateDir != null)
        {
          groupRoots.append(privateDir.substring(INDEX_OF_FILE_IN_URL), null);
        }
        else
        {
            // Some implementations may not provide location to private directory
            // in that case use an alternate writable location if available
            // (e.g. graphics location)
            String graphicsDir = System.getProperty("fileconn.dir.graphics");
            if (graphicsDir != null)
            {
              groupRoots.append(graphicsDir.substring(INDEX_OF_FILE_IN_URL), null);
            }
            // otherwise the user can slect any path from the root and file
            // controls on the midlet...
          }


        for(; roots.hasMoreElements();){
            groupRoots.append(roots.nextElement().toString(), null);
        }
        append(groupRoots);

        if(listTypeIndicator == PIM.CONTACT_LIST){
         fileName = new TextField("File name", "vcard.txt", STRING_VALUE_SIZE, TextField.ANY);
        } else {
            fileName = new TextField("File name", "vcalendar.txt", STRING_VALUE_SIZE, TextField.ANY);
        }
        append(fileName);
    }

    // Handle the menu commands (CommandListener).
    public void commandAction(Command command, Displayable displayable){
        if (command == backCommand) {
            Display.getDisplay(midlet).setCurrent(caller);

        }else if (command == selectCommand) {
                String root=groupRoots.getString(groupRoots.getSelectedIndex());
                String file=fileName.getString();
                String url = schemePart + root + file;
                String encoding=groupEncoding.getString(groupEncoding.getSelectedIndex());
                ExportItemThread exportItemThread = new ExportItemThread(midlet, this, listTypeIndicator, currentPIMList, usedPIMItem, url, encoding);
                exportItemThread.start();
        }
    }

}

/**
    A thread to avoid potential deadlock for operations that may block, such as networking.
*/
class ExportItemThread extends Thread{
    PersonalController midlet;
    Displayable creator;
    int listTypeIndicator;
    PIMList currentPIMList;
    PIMItem usedPIMItem;
    String url;
    String encoding;

    ExportItemThread(PersonalController midlet, Displayable creator, int listTypeIndicator, PIMList currentPIMList, PIMItem usedPIMItem, String url, String encoding){

        this.midlet = midlet;
        this.creator = creator;
        this.listTypeIndicator = listTypeIndicator;
        this.currentPIMList=currentPIMList;
        this.usedPIMItem=usedPIMItem;
        this.url = url;
        this.encoding=encoding;
    }

    public void run(){
        try{
            FileConnection fconn = (FileConnection)Connector.open(url);
            // If no exception is thrown, then the URI is valid, but the file may or may not exist.
            if (!fconn.exists()) {
                fconn.create();  // create the file if it doesn't exist
            }
            OutputStream os = fconn.openOutputStream();
            // Contacts are stored in vCard format, while calendar and to-do items are both stored in vCalendar format.
            // Therefore data_formats[0] equals "vCard 2.1" with Contact List
            // and "vCalendar 1.0" with ToDo and Event Lists.
            String[] data_formats = PIM.getInstance().supportedSerialFormats(listTypeIndicator);
            try {
                PIM.getInstance().toSerialFormat(usedPIMItem, os, encoding, data_formats[0]);
                os.close();
            } catch (Exception e){
                ErrorReporter.reportError(e);
            }
            Display.getDisplay(midlet).setCurrent(creator);
          } catch (SecurityException se){
           // shows splash screen
           String text = "Access Not Allowed";

           Alert splashScreen = new Alert(null, text, null, null);
           Display.getDisplay(this.midlet).setCurrent(splashScreen);

         } catch (Exception e){
           ErrorReporter.reportError(e);
         }
         // shows splash screen
         String text = "Item Exported";

         Alert splashScreen = new Alert(null, text, null, null);
         Display.getDisplay(this.midlet).setCurrent(splashScreen);
    }
}
