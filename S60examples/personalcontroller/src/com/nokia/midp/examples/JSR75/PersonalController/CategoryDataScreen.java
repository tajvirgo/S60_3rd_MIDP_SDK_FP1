/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.PersonalController;

import javax.microedition.lcdui.*;
import javax.microedition.pim.*;
import java.util.Vector;
import java.util.Enumeration;

/**

CategoryDataScreen shows the PIMItems of the PIMList in multiselection List
(javax.microedition.lcdui.List). All the PIMItems that are members of the
category are selected and the others are nonselected. By selecting and
deselecting the PIMItems in the shown list user can add and remove PIMItems
to/from the category (save button must also be pressed to commit the changes).

The PIMItems that are shown in the list (javax.microedition.lcdui.List)
are also put into the PIMItemVector so that they can be quickly accessed later.

*/

public class CategoryDataScreen extends List implements CommandListener{

    private static final String TITLE_TEXT = "Category items";

    private final Command backCommand = new Command("Back", Command.BACK, 1);
    private final Command saveCommand = new Command("Save changes", Command.OK, 1);

    private final int listTypeIndicator;
    private final PIMList currentPIMList;
    private final Displayable caller;
    private final PersonalController midlet;
    private Vector PIMItemVector = new Vector();
    private final String category;


    public CategoryDataScreen(PersonalController midlet,
                  Displayable caller,
                  int listTypeIndicator,
                  PIMList currentPIMList,
                  String category) throws PIMException{

        super(TITLE_TEXT, List.MULTIPLE);
        this.listTypeIndicator = listTypeIndicator;
        this.currentPIMList = currentPIMList;
        this.midlet = midlet;
        this.caller = caller;
        this.category = category;

        addCommand(backCommand);
        addCommand(saveCommand);
        setCommandListener(this);
        showTheList();
    }


    // Handle the user commands.
    public void commandAction(Command command, Displayable displayable) {
        if (command == backCommand) {
            Display.getDisplay(midlet).setCurrent(caller);
        }else if(command == saveCommand) {
            saveTheChanges();
            Display.getDisplay(midlet).setCurrent(caller);
        }
    }


    // Show the multiselection list on the screen.
    private void showTheList() throws PIMException{
        int index = 0;

        for(Enumeration PIMItems = currentPIMList.items(); PIMItems.hasMoreElements();){
            PIMItem currentPIMItem = (PIMItem) PIMItems.nextElement();

            // Add the field value (of representative field) to
            // the shown list (javax.microedition.lcdui.List).
            append(GlobalServices.getShownFieldData(listTypeIndicator,
                                currentPIMList,
                                currentPIMItem),
                                null);

            // Add the currentPIMItem to the Vector (so that it can be
            // quickly accessed later).
            PIMItemVector.addElement(currentPIMItem);

            // Each item that is member of the category is selected
            // in the shown multiselection list (javax.microedition.lcdui.List).
            String[] associatedCategories = currentPIMItem.getCategories();
            for(int i = 0; i < associatedCategories.length; i++){
                if(category.equals(associatedCategories[i])){
                    setSelectedIndex(index, true);
                    break;
                }
            }
            index++;
        }
    }


    // Save the changes to the PIMList.
    private void saveTheChanges(){
        for(int i = 0; i < size(); i++){
            boolean originallyAssociatedToCategory = false;
            PIMItem currentPIMItem = (PIMItem) PIMItemVector.elementAt(i);
            String[] associatedCategories = currentPIMItem.getCategories();

            for(int j = 0; j < associatedCategories.length; j++){
                if(category.equals(associatedCategories[j])){
                    // current PIMItem was originally associated to the category
                    // (this.category).
                    originallyAssociatedToCategory = true;
                    break;
                }
            }

            // Check if changes need to be saved.
            if(isSelected(i) ^ originallyAssociatedToCategory){
                // There are changes concerning this PIMItem.
                // Must be saved.
                try {
                    if(isSelected(i)){
                        currentPIMItem.addToCategory(category);
                    }else{
                        currentPIMItem.removeFromCategory(category);
                    }
                    currentPIMItem.commit();
                } catch (Exception e) {
                    ErrorReporter.reportError(e);
                }
            }
        }
    }
}

