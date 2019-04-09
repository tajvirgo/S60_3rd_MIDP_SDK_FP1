/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.rms.shopping;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.io.*;
import java.util.*;

/**
 * Shows a List screen, displaying the items in the shopping list;
 * also handles loading and saving the data.
 *
 * Editing an item is handled by the {@link EditItemScreen} class.
 *
 * The implementation is discussed in the package documentation for
 * {@link com.nokia.midp.examples.rms.shopping} .
 */
public class ShoppingListMIDlet extends MIDlet implements CommandListener {
    /** Priority of commands relative to others of the same type. */
    private static final int COMMAND_PRIORITY = 1;
    /** Command to exit the application. */
    private static final Command CMD_EXIT = new Command("Exit", Command.EXIT, COMMAND_PRIORITY);
    /** Command to add an item. */
    private static final Command CMD_ADD = new Command("Add", Command.SCREEN, COMMAND_PRIORITY);
    /** Command to delete an item. */
    private static final Command CMD_DELETE = new Command("Delete", Command.ITEM, COMMAND_PRIORITY);
    /** Command to edit an item. */
    private static final Command CMD_EDIT = new Command("Edit", Command.ITEM, COMMAND_PRIORITY);
    /** Command to generate a lot of records. */
    private static final Command CMD_GENERATE_100 = new Command("Generate 100 items", Command.SCREEN, COMMAND_PRIORITY);
    /** Command to generate a lot of records. */
    private static final Command CMD_GENERATE_1000 = new Command("Generate 1000 items", Command.SCREEN, COMMAND_PRIORITY);

    /** Used on the EditItemScreen to lose changes and return to the main list. */
    private static final Command CMD_CANCEL = new Command("Cancel", Command.CANCEL, COMMAND_PRIORITY);
    /** Used on the EditItemScreen to save changes and return to the main list. */
    private static final Command CMD_SAVE = new Command("OK", Command.OK, COMMAND_PRIORITY);

    /** Name of the record store. */
    private static final String RECORD_STORE_NAME = "shopping";

    /** Screen for editing a single item. */
    private EditItemScreen editItemScreen;
    /** Reason for showing the item editing screen - can be to modify an existing item, or to create a new item. */
    private boolean editingNewItem;

    /** List.getSelectedIndex() returns this if nothing is selected. */
    private static final int LIST_NONE_SELECTED = -1;

    /* Indices into the displayable List match those in the shoppingItems Vector. */
    /** Vector holding details about each item. */
    private Vector shoppingItems;
    /** List showing all items. */
    private javax.microedition.lcdui.List list;

    /** Caches the result of Display.getDisplay(). */
    private Display display;


    /**
     * Vector holding a record id for each record in the database that has been deleted.
     */
    private Vector deletedRecords;

    /**
     * Loads all data, and shows the List screen.
     */
    public void startApp() {
        if (display == null ) {
            // first time startApp() has been called
            display = Display.getDisplay(this);

            shoppingItems = new Vector();
            deletedRecords = new Vector();

            list = new javax.microedition.lcdui.List("RMS-Shopping List", javax.microedition.lcdui.List.IMPLICIT);
            list.setCommandListener(this);
            list.addCommand(CMD_EXIT);
            list.addCommand(CMD_ADD);
            list.addCommand(CMD_GENERATE_100);
            list.addCommand(CMD_GENERATE_1000);

            loadData();

            if (list.size() > 0) {
                list.addCommand(CMD_EDIT);
                list.addCommand(CMD_DELETE);
            }

            editItemScreen = new EditItemScreen(this);
            editItemScreen.addCommand(CMD_CANCEL);
            editItemScreen.addCommand(CMD_SAVE);
            editItemScreen.setCommandListener(this);
        }
        display.setCurrent(list);
    }

    /**
     * Pauses the midlet - this midlet only runs in response to user input,
     * so this does nothing.
     */
    public void pauseApp() {
    }

    /**
     * Saves and exits.
     *
     * If an item is being edited, then we do not save the changes made in the
     * editing screen.
     *
     *@param unconditional is ignored
     */
    public void destroyApp(boolean unconditional) {
        saveData();
    }

    /**
     * Initialises the shopping list, loading from RMS.
     */
    private void loadData()
    {
        try {
            RecordStore recordStore = RecordStore.openRecordStore(RECORD_STORE_NAME, true);
            RecordEnumeration enumer = recordStore.enumerateRecords(null, null, false);
            shoppingItems.ensureCapacity(enumer.numRecords()); //an optimisation, not strictly needed
            while (enumer.hasNextElement()) {
                int id = enumer.nextRecordId();
                byte record[] = recordStore.getRecord(id);
                try {
                    shoppingItems.addElement(new ShoppingItem(id, record));
                } catch (IOException e) {
                    //this shouldn't happen, probably a record in the database is corrupt
                    //the application continues to load all items, marking corrupt ones for deletion by saveData()
                    deletedRecords.addElement(new Integer(id));
                }
            }
            recordStore.closeRecordStore();
        } catch (RecordStoreException e) {
            //this is only expected to occur if the record store does not exist, in which case
            //the application's normal flow of exection will start with a blank list, and create the
            //store in saveData().
        }

        //create the List screen based on the objects loaded
         for (Enumeration e = shoppingItems.elements() ; e.hasMoreElements() ;) {
            list.append(e.nextElement().toString(), null);
         }
    }


    /**
     * Saves the list to RMS.
     * This is intended to be called as part of exiting the application.  It is not valid to call
     * this method twice.
     */
    private void saveData()
    {
        RecordStore recordStore = null;
        try {
            recordStore = RecordStore.openRecordStore(RECORD_STORE_NAME, true);

            //delete records that have been deleted in the app
            for (Enumeration e = deletedRecords.elements() ; e.hasMoreElements() ;) {
                int id = ((Integer) (e.nextElement())).intValue();
                recordStore.deleteRecord(id);
            }
            //to allow saveData to be called twice, this must reset deletedRecords

            //save records that have been modified
            for (Enumeration e = shoppingItems.elements(); e.hasMoreElements(); ) {
                ShoppingItem item = (ShoppingItem) (e.nextElement());
                if (item.isModified()) {
                    byte record[] = item.toByteArray();
                    if (item.getRecordId() != ShoppingItem.NOT_IN_DATABASE) {
                        recordStore.setRecord(item.getRecordId(), record, 0, record.length);
                        //to allow saveData to be called twice, this should clear item.modified
                    } else {
                        recordStore.addRecord(record, 0, record.length);
                        //to allow saveData to be called twice, this must set item.recordId and clear item.modified
                    }
                }
            }
        } catch (RecordStoreFullException e) {
            //a non-demo application should handle this condition, probably warning the user and trying to
            //let them choose what will be lost
            //
            //this demo just ensures that the recordStore is closed, and continues to exit
        } catch (Exception e) {
            //this shouldn't happen
            //the only expected exception is already handled.
        } finally {
            try {
                recordStore.closeRecordStore();
            } catch (Exception e) {
                //an exception here is completely unexpected
            }
        }
    }

    /**
     * Responds to a menu item on either the List or Edit screen.
     *
     *@param cmd the menu item or implicit SELECT_COMMAND of LCDUI.List
     *@param source the Displayable object where the event originated
     */
    public void commandAction(Command cmd, Displayable source) {
        if (source == editItemScreen) {
            editScreenCommandAction(cmd);
            return;
        }
        if (cmd == CMD_EXIT) {
            destroyApp(true);
            notifyDestroyed();
        } else if (cmd == CMD_ADD) {
            addNewItem();
        } else if (cmd == CMD_DELETE) {
            deleteSelectedItem();
        } else if (cmd == CMD_EDIT || cmd == javax.microedition.lcdui.List.SELECT_COMMAND) {
            editSelectedItem();
        } else if (cmd == CMD_GENERATE_100) {
            generateItems(100);
        } else if (cmd == CMD_GENERATE_1000) {
            generateItems(1000);
        } else {
            //a non-demo application should handle this unexpected condition
        }
    }

    /**
     * Starts the editing screen to add a new item.
     * Called from commandAction().
     */
    public void addNewItem() {
        editingNewItem = true;
        editItemScreen.editItem(new ShoppingItem());
        display.setCurrent(editItemScreen);

        //if the list was empty, add some menu items
        //if they were already present, this is a no-op
        list.addCommand(CMD_EDIT);
        list.addCommand(CMD_DELETE);
    }

    /**
     * Starts the editing screen to edit the currently selected item.
     * Called from commandAction().
     * If no item is selected, does nothing.
     */
    public void editSelectedItem() {
        int selected = list.getSelectedIndex();
        if (selected == LIST_NONE_SELECTED) {
            return;
        }
        editingNewItem = false;
        editItemScreen.editItem((ShoppingItem) (shoppingItems.elementAt(selected)));
        display.setCurrent(editItemScreen);
    }

    /**
     * Deletes the currently selected item.
     * Called from commandAction().
     * If no item is selected, does nothing.
     */
    public void deleteSelectedItem() {
        int selected = list.getSelectedIndex();
        if (selected == LIST_NONE_SELECTED) {
            return;
        }
        int recordId = ((ShoppingItem)(shoppingItems.elementAt(selected))).getRecordId();
        if (recordId != ShoppingItem.NOT_IN_DATABASE) {
            deletedRecords.addElement(new Integer(recordId));
        }
        list.delete(selected);
        shoppingItems.removeElementAt(selected);

        //if the list is empty, remove some menu items
        if (list.size() == 0) {
            list.removeCommand(CMD_EDIT);
            list.removeCommand(CMD_DELETE);
        }
    }

    /**
     * Generates a number of items and adds them to the list without showing the editing screen.
     * Called from commandAction().
     *
     *@param number the number of new items generated
     */
    public void generateItems(int number) {
        int limit = shoppingItems.size() + number;
        for (int i=shoppingItems.size(); i<limit; i++) {
            ShoppingItem item = new ShoppingItem();
            item.setDetails("Test "+i, i);
            list.append(item.toString(), null);
            shoppingItems.addElement(item);
        }

        //if the list was empty, add some menu items
        //if they were already present, this is a no-op
        list.addCommand(CMD_EDIT);
        list.addCommand(CMD_DELETE);
    }

    /**
     * Handles command actions on the edit screen (save or cancel).
     * Both commands return to the List screen, but CMD_SAVE saves the details from the editing screen first.
     */
    public void editScreenCommandAction(Command cmd) {
        if (cmd == CMD_SAVE) {
            ShoppingItem item = editItemScreen.saveChanges();
            if (editingNewItem) {
                list.append(item.toString(), null);
                shoppingItems.addElement(item);
            } else {
                //replace the old description in the list with a new one
                //the object in the Vector was changed by saveChanges().
                list.set(list.getSelectedIndex(), item.toString(), null);
            }
        }
        display.setCurrent(list);
    }
}
