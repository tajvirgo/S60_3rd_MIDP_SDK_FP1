/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.PersonalController;

import javax.microedition.lcdui.*;
import javax.microedition.pim.*;

/**

InitialScreen is the first screen to appear after the application has started.
InitialScreen allows the selection to be made between different PIM list types
(Contact list, Event list or ToDo list).

When the user has selected the list type InitialScreen creates an instance
of PIMListGroupScreen or ItemListScreen depending on the case:
-if there are more than one list of the selected list type an instance of
PIMListGroupScreen is needed (since the user has to select a list to be opened).
-if there is only one list of the selected list type PIMListGroupScreen is not
needed and an instance of ItemListScreen is created instead (ItemListScreen is used
directly).

*/

public class InitialScreen extends List implements CommandListener{

    private static final String PIM_FAIL = "PIM ( JSR75 ) was not found on system. Close application.";
    private static final String LIST_START = "Select start to begin.";

    private static final String LIST_TITLE = "Personal Controller Example.";
    private static final String FORM_START  = "Start";
    private static final String TITLE_TEXT = "List type selection";
    private static final String CONTACT_TEXT = "Contact";
    private static final String EVENT_TEXT = "Event";
    private static final String TODO_TEXT = "ToDo";

    private final PersonalController midlet;

    private final Command selectCommand = new Command("Select", Command.OK, 1);
    private final Command exitCommand = new Command("Exit", Command.EXIT, 1);
    private final Command startCommand = new Command( FORM_START, Command.SCREEN, 1 );


    public InitialScreen(PersonalController midlet){
      super(LIST_TITLE,List.IMPLICIT);
       this.midlet = midlet;
        addCommand(exitCommand);
        addCommand(startCommand);
        setCommandListener(this);
        // Check PIM presence.
       PrintPIMVersion();
    }


    private void PrintPIMVersion()
    {
        // Check that the File Connection Optional Package is there
        String v = System.getProperty( "microedition.pim.version");
        if (  v != null  )
        {
            append( "PIM v" + v + " found.",null );
            append( LIST_START,null );
        }
        else
        {
            // JSR75 was not found os system. Remove Start possibility.
            removeCommand( startCommand );
            append( PIM_FAIL,null );
        }
    }

    public void StartPIMSelection()
    {
      // Clear the ListBox
      this.deleteAll();

      // Remove Start command
      removeCommand(startCommand);
      // Add Select command
      setSelectCommand(selectCommand);
      // Habit listbox with default actions.
      append(CONTACT_TEXT,null);
      append(EVENT_TEXT,null);
      append(TODO_TEXT,null);
    }


    // Handles user commands.
    public void commandAction(Command c, Displayable d){
        if (c == exitCommand){
            midlet.exitRequested();
        }else if(c == startCommand){
          StartPIMSelection();
        }else if(c == selectCommand){
            handleListTypeSelection();
        }
    }

    // Show PIMListGroupScreen or ItemListScreen (depending on number of lists).
    private void handleListTypeSelection(){
        // Check the selected list type.
        int listTypeIndicator = 0;
        if(getString(getSelectedIndex()).equals(CONTACT_TEXT)){
            // Contact list
            listTypeIndicator = PIM.CONTACT_LIST;
        }else if(getString(getSelectedIndex()).equals(EVENT_TEXT)){
            // Event list
            listTypeIndicator = PIM.EVENT_LIST;
        }else if(getString(getSelectedIndex()).equals(TODO_TEXT)){
            // ToDo list
            listTypeIndicator = PIM.TODO_LIST;
        }

        PIMListViewThread listViewThread = new PIMListViewThread(midlet, this, listTypeIndicator);
        listViewThread.start();
    }
}


/**
   Checks the number of PIM lists of the selected list type.  If there is more that one list then it creates an instance of PIMListGroupScreen.  If there is only one list then it opens the PIM list and creates an instance of ItemListScreen. */
class PIMListViewThread extends Thread {

    int listTypeIndicator;
    PersonalController midlet;
    Displayable creator;

    PIMListViewThread(PersonalController midlet, Displayable creator, int listTypeIndicator){
        this.midlet = midlet;
        this.creator = creator;
        this.listTypeIndicator = listTypeIndicator;
    }

    public void run(){
        try{
            // Get the number of lists of selected type.
            String[] PIMListNames = PIM.getInstance().listPIMLists(listTypeIndicator);

            if(PIMListNames.length > 1){
                // More than one list of this type found. List selection needed.
                Displayable listGroupScreen =
                    new PIMListGroupScreen(midlet, creator, listTypeIndicator);
                Display.getDisplay(midlet).setCurrent(listGroupScreen);
            }else if (PIMListNames.length == 1){
                // Only one list of this type exists. No need to select
                // since there are no choices.
                PIMList selectedPIMList = PIM.getInstance().openPIMList(listTypeIndicator, PIM.READ_WRITE, PIMListNames[0]);
                Displayable itemListScreen =
                    new ItemListScreen(midlet, creator, listTypeIndicator, selectedPIMList);
                Display.getDisplay(midlet).setCurrent(itemListScreen);
            }else{
                // No list of this type found.
            }
        } catch (SecurityException se) {
          Alert alert = new Alert("Error", "No permission", null, AlertType.ERROR );
          alert.setTimeout(Alert.FOREVER);
          alert.setCommandListener(null);
          Display.getDisplay(midlet).setCurrent(alert);
        }catch (Exception e){
          ErrorReporter.reportError(e);
        }
    }
}
