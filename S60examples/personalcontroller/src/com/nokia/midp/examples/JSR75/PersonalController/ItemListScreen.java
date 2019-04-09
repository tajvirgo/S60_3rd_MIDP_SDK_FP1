/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.PersonalController;

import javax.microedition.lcdui.*;
import javax.microedition.pim.*;
import java.util.Enumeration;
import java.util.Vector;

/**

ItemListScreen shows the list containing given PIMItems. ItemListScreen
provides functionalities to create or delete PIMItems and to select PIMItem
for viewing/changing the PIMItems data.

The field value that is shown in the list is the one that has got the field code
GlobalServices.getRepresentativeField(int listTypeIndicator). In case
of Contact list the field that is shown is Contact.FORMATTED_NAME, in case of
Event list the field is Event.SUMMARY and in case of ToDo list the field is
ToDo.SUMMARY. Since the shown field is considered to be a global constant, its
value is encapsulated into the GlobalServices class.

The first constructor (the one that has a reference to a PIMList as a parameter)
is for normal operating of the ItemListScreen and enables all the functionalities
that the class provides. The second constructor (the one that gets the PIMItems
as an Enumeration) is used with the search functionality (the ItemDataScreen
creates an instance of ItemListScreen and passes an Enumeration containing
search results to the ItemListScreen as a parameter) and allows user to select
PIMItems from the search result to be viewed/modified. However, this second constructor
does not allow PIMItems to be added or deleted, it is used only for viewing the
search results.

All the PIMItems that are added to the List (to the javax.microedition.lcdui.List)
while initializing/updating the ItemListScreen are also added to the itemList (Vector)
since the whole content of PIMItem might be needed later and must be accessed by using
the index the PIMItem has got in the shown list (javax.microedition.lcdui.List).

*/

public class ItemListScreen extends List implements CommandListener{

    private PersonalController midlet;
    private Displayable caller;
    private int listTypeIndicator;
    private PIMList currentPIMList;

    private final Enumeration givenPIMItems;
    private final Vector itemList = new Vector();

    private final Command backCommand = new Command("Back", Command.BACK, 1);
    private final Command selectCommand = new Command("Select", Command.ITEM, 1);
    private final Command addCommand = new Command("Add Item", Command.SCREEN, 2);
    private final Command repeatCommand = new Command("Repeat rules", Command.SCREEN, 2);
    private final Command exportCommand = new Command("Export item", Command.SCREEN, 2);
    private final Command importCommand = new Command("Import item", Command.SCREEN, 2);
    private final Command deleteCommand = new Command("Delete Item", Command.SCREEN, 2);
    private final Command searchCommand = new Command("Search", Command.SCREEN, 2);
    private final Command categoriesCommand = new Command("Categories", Command.SCREEN, 2);

    private final int ITEM_LIST_TYPE_SEARCH_RESULTS = 0;
    private final int ITEM_LIST_TYPE_LIST_CONTENT = 1;
    private final int itemListScreenType;

    private int LIST_UPDATE_INITIAL = 0;
    private int LIST_UPDATE_NEW_ITEM = 1;
    private int LIST_UPDATE_ITEM_DATA_CHANGED = 2;
    private int LIST_UPDATE_ITEM_DELETED = 3;
    private int listUpdateMode = LIST_UPDATE_INITIAL;


    // for showing PIM list items
    public ItemListScreen(PersonalController midlet, Displayable caller,
                          int listTypeIndicator, PIMList currentPIMList) throws PIMException{

        super("Items of the list", List.IMPLICIT);
        this.midlet = midlet;
        this.caller = caller;
        this.listTypeIndicator = listTypeIndicator;
        this.currentPIMList = currentPIMList;
        this.givenPIMItems = currentPIMList.items();
        this.itemListScreenType = ITEM_LIST_TYPE_LIST_CONTENT;

        updateShownItemList(null);
        addCommand(backCommand);
        addCommand(addCommand);
        addCommand(categoriesCommand);
        addCommand(exportCommand);
        addCommand(importCommand);
        if(listTypeIndicator==PIM.EVENT_LIST)         addCommand(repeatCommand);
        setCommandListener(this);

    }

    // for showing search results (limited functionality)
    public ItemListScreen(PersonalController midlet,
            Displayable caller,
            int listTypeIndicator,
            PIMList currentPIMList,
            Enumeration searchResults) throws PIMException{

        super("Search results", List.IMPLICIT);
        this.midlet = midlet;
        this.caller = caller;
        this.listTypeIndicator = listTypeIndicator;
        this.currentPIMList = currentPIMList;
        this.givenPIMItems = searchResults;
        this.itemListScreenType = ITEM_LIST_TYPE_SEARCH_RESULTS;

        updateShownItemList(null);
        addCommand(backCommand);
        setCommandListener(this);
    }

    // Handle user commands.
    public void commandAction(Command command, Displayable displayable){
        if (command == backCommand){
            Display.getDisplay(midlet).setCurrent(caller);

        }else if (command == selectCommand){ // Select PIM item to be viewed/modified.
            try{
                PIMItem currentPIMItem = (PIMItem) itemList.elementAt(getSelectedIndex());
                Displayable screen =
                    new ItemDataScreen(midlet, this, listTypeIndicator, currentPIMList, currentPIMItem);
                Display.getDisplay(midlet).setCurrent(screen);
                // Data of only one item may change (if the user saves the changes).
                listUpdateMode = LIST_UPDATE_ITEM_DATA_CHANGED;
            }catch (Exception e){
                ErrorReporter.reportError(e);
            }

        }else if (command == searchCommand){ // Goto search view.
            try{
                Displayable screen =
                    new ItemDataScreen(midlet, this, listTypeIndicator, currentPIMList);
                Display.getDisplay(midlet).setCurrent(screen);
                // It is possible that during search the user changes content
                // of various items. Therefore it is easiest to update the whole content
                // of the list shown in the screen. Unfortunately the currently selected
                // index of list is lost here.
                listUpdateMode = LIST_UPDATE_INITIAL;
            }catch (Exception e){
                ErrorReporter.reportError(e);
            }

        }else if (command == categoriesCommand){ // Show categories.
            try{
                PIMItem thePIMItem = (PIMItem) itemList.elementAt(getSelectedIndex());
                Displayable screen =
                    new CategoryListScreen(midlet, this, listTypeIndicator, currentPIMList, thePIMItem);
                Display.getDisplay(midlet).setCurrent(screen);
                // Here it is also possible that data of various items will be changed by the user.
                listUpdateMode = LIST_UPDATE_INITIAL;
            }
            catch (Exception e){
                ErrorReporter.reportError(e);
            }

        }else if (command == repeatCommand){ // Goto repeat rules.
            try{
                Event event = (Event) itemList.elementAt(getSelectedIndex());
                Displayable screen = new RepeatRulesScreen(midlet, this, listTypeIndicator, currentPIMList, event);
                Display.getDisplay(midlet).setCurrent(screen);
                listUpdateMode = LIST_UPDATE_INITIAL;
            }
            catch (Exception e){
                ErrorReporter.reportError(e);
            }

        }else if (command == exportCommand){ // Export PIM Item.
            try{
                if (itemList.isEmpty())
                {
                    // shows splash screen
                    String text = "Nothing to export";
                    Alert splashScreen = new Alert(null, text, null, null);
                    Display.getDisplay(this.midlet).setCurrent(splashScreen);
                }
                else
                {
                  PIMItem thePIMItem = (PIMItem) itemList.elementAt(
                      getSelectedIndex());
                  Displayable screen = new ExportItem(midlet, this,
                      listTypeIndicator, currentPIMList, thePIMItem);
                  Display.getDisplay(midlet).setCurrent(screen);
                  listUpdateMode = LIST_UPDATE_INITIAL;
                }
              }
            catch (Exception e){
                ErrorReporter.reportError(e);
            }

        }else if (command == importCommand){ // ImportPIM Item.
            try{
                Displayable screen = new ImportItem(midlet, this, listTypeIndicator, currentPIMList);
                Display.getDisplay(midlet).setCurrent(screen);
                listUpdateMode = LIST_UPDATE_INITIAL;
            }
            catch (Exception e){
                ErrorReporter.reportError(e);
            }

        }else if (command == deleteCommand){ // Delete PIM Item.
            try{
                deletePIMItem();
            }catch (Exception e){
                ErrorReporter.reportError(e);
            }
        } else if (command == addCommand){ // Create new PIM item.
            try{
                createNewPIMItem();
            }catch (Exception e){
                ErrorReporter.reportError(e);
            }
        }
    }


    // Decides which amount of updating of shown list view is needed
    // and puts the updatings into action.
    // If it is effortlessly possible to minimize the work amount
    // and to memorize the cursor position in the list, it is done.
    public void updateShownItemList(PIMItem thePIMItem)throws PIMException{
        if(listUpdateMode == LIST_UPDATE_INITIAL){
            // The whole content of the PIMList must be added to screen.
            deleteAll();
            itemList.removeAllElements();
            getPIMListItems();
        }else if(listUpdateMode == LIST_UPDATE_NEW_ITEM){
            // New PIMItem must be added to the end of the list.
            addItemToTheList(thePIMItem);
        }else if(listUpdateMode == LIST_UPDATE_ITEM_DATA_CHANGED){
            // Data of selected PIMItem has changed.
            replaceSelectedListItem(thePIMItem);
        }else if(listUpdateMode == LIST_UPDATE_ITEM_DELETED){
            // Selected PIMItem has been deleted.
            deleteSelectedListItem();
        }
        updateCommands();
    }


    // New PIMItem is added to the shown list (to the javax.microedition.lcdui.List).
    private void addItemToTheList(PIMItem thePIMItem)throws PIMException{
        append(GlobalServices.getShownFieldData(listTypeIndicator, currentPIMList, thePIMItem), null);
        itemList.addElement(thePIMItem);
        setSelectedIndex(size()-1, true);
    }


    // PIMItem is replaced in the shown list (in the javax.microedition.lcdui.List).
    private void replaceSelectedListItem(PIMItem compensatoryPIMItem){
        int index = getSelectedIndex();
        set(index,
            GlobalServices.getShownFieldData(listTypeIndicator,
            currentPIMList,
            compensatoryPIMItem),
            null);
        itemList.removeElementAt(index);
        itemList.insertElementAt(compensatoryPIMItem, index);
    }


    // PIMItem is deleted in the shown list (in the javax.microedition.lcdui.List).
    private void deleteSelectedListItem(){
        int index = getSelectedIndex();
        setSelectedIndex(Math.max(index-1, 0), true);
        delete(index);
        itemList.removeElementAt(index);
    }


    // Adds all given PIMItems to the shown list (javax.microedition.lcdui.List)
    // and to the itemList Vector.
    private void getPIMListItems() throws PIMException{
        for (; givenPIMItems.hasMoreElements();){
            PIMItem currentPIMItem = (PIMItem) givenPIMItems.nextElement();
            append(GlobalServices.getShownFieldData(listTypeIndicator,
                            currentPIMList,
                            currentPIMItem),
                            null);
            itemList.addElement(currentPIMItem);
        }
    }


    // Updates the available commands according to
    // itemListScreenType and number of PIMItems.
    private void updateCommands(){
        removeCommand(selectCommand);
        removeCommand(deleteCommand);
        removeCommand(searchCommand);
        if (size() > 0){
            if(itemListScreenType == ITEM_LIST_TYPE_LIST_CONTENT){
                addCommand(searchCommand);
            }
            setSelectCommand(selectCommand);
            addCommand(deleteCommand);
        }
    }


    // Creates a new PIMItem (but does not make commit-operation).
    private void createNewPIMItem() throws PIMException{
        PIMItem newPIMItem = GlobalServices.createNewPIMItem(listTypeIndicator, currentPIMList);
        listUpdateMode = LIST_UPDATE_NEW_ITEM;
        Displayable itemDataScreen =
            new ItemDataScreen(midlet, this, listTypeIndicator, currentPIMList, newPIMItem);
        Display.getDisplay(midlet).setCurrent(itemDataScreen);
    }


    // Deletes selected PIMItem.
    private void deletePIMItem() throws PIMException{
        GlobalServices.deletePIMItem(listTypeIndicator,
                    currentPIMList,
                    (PIMItem) itemList.elementAt(getSelectedIndex()));
        listUpdateMode = LIST_UPDATE_ITEM_DELETED;
        updateShownItemList(null);
    }
}

