/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.PersonalController;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;
import javax.microedition.pim.*;

/**

GlobalServices gathers together services that are used by many classes in the similar way.


*/

public final class GlobalServices{

    public static final int SHOWN_FIELD_NOT_SUPPORTED = -1;
    private static final String SHOWN_FIELD_NOT_SUPPORTED_TEXT = "-illegal field-";

    // Get the code of the field that is shown when PIMItems of
    // the PIMList are listed in some list view.
    public static int getRepresentativeField(int listTypeIndicator){
        int representativeField = 0;
        if(listTypeIndicator == PIM.CONTACT_LIST){
            representativeField = Contact.FORMATTED_NAME;

        }else if(listTypeIndicator == PIM.EVENT_LIST){
            representativeField = Event.SUMMARY;

        }else if(listTypeIndicator == PIM.TODO_LIST){
            representativeField = ToDo.SUMMARY;
        }
        return representativeField;
    }


    // Creates new PIMItem but does not commit the item.
    public static PIMItem createNewPIMItem(int listTypeIndicator, PIMList thePIMList){
        PIMItem thePIMItem = null;
        if(listTypeIndicator == PIM.CONTACT_LIST){
            thePIMItem = ((ContactList) thePIMList).createContact();
        }else if(listTypeIndicator == PIM.TODO_LIST){
            thePIMItem = ((ToDoList) thePIMList).createToDo();
        }else if(listTypeIndicator == PIM.EVENT_LIST){
            thePIMItem = ((EventList) thePIMList).createEvent();
        }
        return thePIMItem;
    }


    // Deletes given PIMItem.
    public static void deletePIMItem(int listTypeIndicator,
                     PIMList currentPIMList,
                     PIMItem thePIMItem) throws PIMException{
        if(listTypeIndicator == PIM.CONTACT_LIST){
            ((ContactList) currentPIMList).removeContact((Contact) thePIMItem);
        }else if(listTypeIndicator == PIM.TODO_LIST){
            ((ToDoList) currentPIMList).removeToDo((ToDo) thePIMItem);
        }else if(listTypeIndicator == PIM.EVENT_LIST){
            ((EventList) currentPIMList).removeEvent((Event) thePIMItem);
        }
    }


    // Get the field value that is shown in the listings of PIMItems.
    public static String getShownFieldData(int listTypeIndicator,
                    PIMList currentPIMList,
                    PIMItem thePIMItem){

        String shownFieldData = "";

        int shownFieldOfPIMItem = getRepresentativeField(listTypeIndicator);
        if(!currentPIMList.isSupportedField(shownFieldOfPIMItem)){
            shownFieldData = SHOWN_FIELD_NOT_SUPPORTED_TEXT;
        }else if(thePIMItem.countValues(shownFieldOfPIMItem) > 0){
            shownFieldData = thePIMItem.getString(shownFieldOfPIMItem, 0);
            if(shownFieldData != null){
                shownFieldData = shownFieldData.trim();
            }
        }
        return shownFieldData;
    }


    // Shows modal dialog.
    // This is used for confirming delete operations etc.
    public static void showModalDialog(MIDlet midlet,
                       CommandListener cl,
                       String title,
                       String text,
                       Command[] commands){
        Alert alert = new Alert(title, text, null, AlertType.CONFIRMATION);
        alert.setTimeout(Alert.FOREVER);
        for(int i = 0; i < commands.length; i++){
            alert.addCommand(commands[i]);
        }
        alert.setCommandListener(cl);
        Display.getDisplay(midlet).setCurrent(alert);
    }

}
