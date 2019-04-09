/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.PersonalController;

import javax.microedition.lcdui.*;
import javax.microedition.pim.*;
import javax.microedition.io.Connector;
import javax.microedition.io.file.*;

import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import com.nokia.midp.examples.JSR75.PersonalController.ItemListScreen;

/**
    ImportItem handles importing PIM items such as contacts.
*/

public class ImportItem extends Form implements CommandListener{

    private static final String TITLE_TEXT = "Import item";

    private final Command backCommand = new Command("Back", Command.BACK, 1);
    private final Command importCommand = new Command("Import", Command.OK, 1);

    private final PersonalController midlet;
    private final Displayable caller;
    private final int listTypeIndicator;
    private PIMList currentPIMList;

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

    public ImportItem(PersonalController midlet, Displayable caller, int listTypeIndicator, PIMList currentPIMList) throws PIMException{

        super(TITLE_TEXT);
        this.midlet = midlet;
        this.caller = caller;
        this.listTypeIndicator = listTypeIndicator;
        this.currentPIMList=currentPIMList;

        addCommand(backCommand);
        addCommand(importCommand);
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
           try{
             Displayable itemListScreen =
                 new ItemListScreen(midlet, caller, listTypeIndicator,
                                    currentPIMList);
             Display.getDisplay(midlet).setCurrent(itemListScreen);
           }catch(PIMException pe){
             ErrorReporter.reportError(pe);
           }
        }else if (command == importCommand) {
            try{
                String root=groupRoots.getString(groupRoots.getSelectedIndex());
                String file=fileName.getString();
                String url = schemePart + root + file;
                String encoding=groupEncoding.getString(groupEncoding.getSelectedIndex());
                ImportItemThread importItemThread = new ImportItemThread(midlet, this, listTypeIndicator, currentPIMList, url, encoding);
                importItemThread.start();
            } catch (Exception e) {
                ErrorReporter.reportError(e);
            }
        }
    }
}

/**
    A thread to avoid potential deadlock for operations that may block, such as networking.
*/
class ImportItemThread extends Thread{
    PersonalController midlet;
    Displayable creator;
    int listTypeIndicator;
    PIMList currentPIMList;
    String url;
    String encoding;
    PIMItem usedPIMItem;

    ImportItemThread(PersonalController midlet, Displayable creator, int listTypeIndicator, PIMList currentPIMList, String url, String encoding){

        this.midlet = midlet;
        this.creator = creator;
        this.listTypeIndicator = listTypeIndicator;
        this.currentPIMList=currentPIMList;
        this.url = url;
        this.encoding=encoding;
    }

    public void run(){
        try{
            FileConnection fconn = (FileConnection)Connector.open(url);
            // If no exception is thrown, then the URI is valid, but the file may or may not exist.
            if (!fconn.exists()) {
                //File doesn't exist
            } else {
                InputStream is = fconn.openInputStream();
                try {
                    PIMItem importedPIMItem = null;
                    PIMItem[] items = PIM.getInstance().fromSerialFormat(is, encoding);
                    if(listTypeIndicator == PIM.CONTACT_LIST){
                     usedPIMItem = ((ContactList) currentPIMList).createContact();
                      usedPIMItem= (Contact)(items[0]); // assume there´s only one Contact
                       importedPIMItem = ((ContactList) currentPIMList).importContact((Contact)usedPIMItem);
                    }else if(listTypeIndicator == PIM.TODO_LIST){
                      usedPIMItem = ((ToDoList) currentPIMList).createToDo();
                        usedPIMItem= (ToDo)(items[0]); // assume there´s only one ToDo
                        importedPIMItem = ((ToDoList) currentPIMList).importToDo((ToDo)usedPIMItem);
                    }else if(listTypeIndicator == PIM.EVENT_LIST){
                     usedPIMItem = ((EventList) currentPIMList).createEvent();
                      usedPIMItem= (Event)(items[0]); // assume there´s only one ToDo
                      importedPIMItem = ((EventList) currentPIMList).importEvent((Event)usedPIMItem);
                    }

                  try{
                    importedPIMItem.commit();
                    // shows splash screen
                    showAlert("Item Imported");

                  } catch (PIMException pe) {
                        ErrorReporter.reportError(pe);
                        showAlert("Import failed. "+pe.getMessage());
                  } catch (Exception e) {
                        ErrorReporter.reportError(e);
                        showAlert("Import failed. "+e.getMessage());
                  }
                    fconn.close();
                } catch (IOException ioe){
                        ErrorReporter.reportError(ioe);
                        showAlert("Import failed. "+ioe.getMessage());
                } catch (Exception e){
                        ErrorReporter.reportError(e);
                        showAlert("Import failed. "+e.getMessage());
                }
            }
            Display.getDisplay(midlet).setCurrent(creator);
          } catch (SecurityException se){
              // shows splash screen
              showAlert("Access Not Allowed");
          } catch (Exception e){
            ErrorReporter.reportError(e);
            showAlert("Import failed. "+e.getMessage());
        }
      }

      private void showAlert(String content){
        Alert splashScreen = new Alert(null, content, null, null);
        Display.getDisplay(this.midlet).setCurrent(splashScreen);
      }

}
