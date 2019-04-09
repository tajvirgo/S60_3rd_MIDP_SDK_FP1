/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.PersonalController;

import javax.microedition.lcdui.*;
import javax.microedition.pim.*;

/**

PIMListGroupScreen shows all PIM lists of selected PIM list type.
If some of the PIM lists will be selected the content of the selected
PIM list will then be shown (by ItemListScreen).

InitialScreen creates instance of PIMListGroupScreen
only if there is more than one list of the selected list type.
Otherwise, InitialScreen uses ItemListScreen directly (since there
is just one choice then and PIMListGroupScreen is not needed).

*/


public class PIMListGroupScreen extends List implements CommandListener{

    private static final String TITLE_TEXT = "List selection";
    private static final String REPEAT_TITLE_TEXT = "Event List selection";

    private final Command selectCommand = new Command("Select", Command.OK, 1);
    private final Command backCommand = new Command("Back", Command.BACK, 1);

    private final PersonalController midlet;
    private final Displayable caller;
    private final int listTypeIndicator;

    public PIMListGroupScreen(PersonalController midlet, Displayable caller, int listTypeIndicator){
        super(TITLE_TEXT, List.IMPLICIT);
        this.midlet = midlet;
        this.caller = caller;
        this.listTypeIndicator = listTypeIndicator;

        // Get all names of PIMLists of this type.
        String[] PIMListNames = PIM.getInstance().listPIMLists(listTypeIndicator);
        for (int i = 0; i < PIMListNames.length; i++){
            append(PIMListNames[i], null);
        }

        setSelectCommand(selectCommand);
        addCommand(backCommand);
        setCommandListener(this);
    }

    // Handles user commands.
    public void commandAction(Command command, Displayable d){
        if (command == backCommand) {
            Display.getDisplay(midlet).setCurrent(caller);
        } else if (command == selectCommand) {
            // Show PIMItems of selected PIMList.
            SelectedPIMListViewThread p =
                new SelectedPIMListViewThread(midlet, this, listTypeIndicator, getString(getSelectedIndex()));
            p.start();
        }
    }
}


/**
Opens selected PIM list and creates an instance of ItemListScreen.
*/
class SelectedPIMListViewThread extends Thread{
    PersonalController midlet;
    Displayable caller;
    int listTypeIndicator;
    String listName;

    SelectedPIMListViewThread(PersonalController midlet, Displayable caller, int listTypeIndicator, String listName){
        this.midlet = midlet;
        this.caller = caller;
        this.listTypeIndicator = listTypeIndicator;
        this.listName = listName;
    }


    // Open the PIM list and create instance of ItemListScreen (for showing the content of
    // the PIM list).
    public void run(){
        try {
            PIMList currentPIMList =
            PIM.getInstance().openPIMList(listTypeIndicator, PIM.READ_WRITE, listName);
            Displayable screen = new ItemListScreen(midlet, caller, listTypeIndicator, currentPIMList);
            Display.getDisplay(midlet).setCurrent(screen);
        } catch (Exception e) {
            ErrorReporter.reportError(e);
        }
    }
}
