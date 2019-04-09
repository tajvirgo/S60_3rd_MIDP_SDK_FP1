/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.PersonalController;

import javax.microedition.lcdui.*;
import javax.microedition.pim.*;
import java.util.Enumeration;


/**

CategoryListScreen shows all the categories that the selected PIM list contains.
CategoryListScreen allows new categories to be added and existing categories to be
deleted or renamed. If category is deleted also the PIMItems that are associated to
the category may be deleted (a modal dialog asks the user to make a choice in this case).

PIMItems that are members of selected category can be viewed (an instance of ItemListScreen
is created in that case).

PIMItems can be added to selected category or removed from it (an instance of CategoryDataScreen
is created in that case).

*/

public class CategoryListScreen extends List implements CommandListener{

    private static final String TITLE_TEXT = "All categories";
    private static final String DELETE_DIALOG_TITLE_TEXT = "Delete category";
    private static final String CATEGORY_ADD_FORM_TITLE_TEXT = "New category";
    private static final String CATEGORY_RENAME_FORM_TITLE_TEXT = "Category renaming";

    private static final String CATEGORY_NAMING_FIELD_LABEL = "Name of category";
    private static final String CATEGORY_RENAMING_FIELD_LABEL = "Rename category";

    private static final String ASSOCIATED_ITEMS_DEL_TEXT = "Delete associated items also?";
    private static final String DEL_CONFIRM_TEXT_1 = "Really delete category";
    private static final String DEL_CONFIRM_TEXT_2 = "? and associated items? Are you sure?";
    private static final String DEL_CONFIRM_TEXT_3 = "? Are you sure?";

    private static final int CATEGORY_NAME_LENGTH = 40;

    private final Command backCommand = new Command("Back", Command.BACK, 1);
    private final Command categoryItemsCommand = new Command("Associated items", Command.ITEM, 1);
    private final Command manageCategoryCommand = new Command("Manage category", Command.ITEM, 1);
    private final Command newCommand = new Command("New Category", Command.SCREEN, 2);
    private final Command deleteCommand = new Command("Delete", Command.SCREEN, 3);
    private final Command renameCommand = new Command("Rename", Command.SCREEN, 3);

    private final Command yesAssociatedItemsCommand = new Command("Yes", Command.OK, 1);
    private final Command noAssociatedItemsCommand = new Command("No", Command.CANCEL, 1);
    private final Command yesDeleteCategoryCommand = new Command("Yes", Command.OK, 1);
    private final Command noDeleteCategoryCommand = new Command("No", Command.CANCEL, 1);

    private final PersonalController midlet;
    private final Displayable caller;
    private final int listTypeIndicator;
    private final PIMList currentPIMList;
    private final PIMItem usedPIMItem;

    // Indicator which tells whether members of category are deleted with the category.
    private boolean deleteAssociatedItems = false;

    public CategoryListScreen(PersonalController midlet,
                              Displayable caller,
                              int listTypeIndicator,
                              PIMList currentPIMList,
                              PIMItem usedPIMItem) throws PIMException{

        super(TITLE_TEXT, List.IMPLICIT);
        this.midlet = midlet;
        this.caller = caller;
        this.listTypeIndicator = listTypeIndicator;
        this.currentPIMList = currentPIMList;
        this.usedPIMItem = usedPIMItem;

        updateShownCatList();

        addCommand(backCommand);
        addCommand(newCommand);
        setCommandListener(this);
    }


    // Updates shown category listing and commands.
    public void updateShownCatList() throws PIMException{
        // All category names are added to screen.
        deleteAll();
        String[] categories = currentPIMList.getCategories();

        for(int i=0;i<categories.length;i++){
            append(categories[i], null);
        }
        if(categories.length>0){
            addCommand(manageCategoryCommand);
            addCommand(categoryItemsCommand);
            addCommand(deleteCommand);
            addCommand(renameCommand);
        }
    }

    // User command handling.
    public void commandAction(Command command, Displayable displayable){
        if (command == backCommand) {
            Display.getDisplay(midlet).setCurrent(caller);

        }else if(command == newCommand) {
            configureCategory(null);

        }else if(command == renameCommand) {
            configureCategory(getString(getSelectedIndex()));

        }else if(command == categoryItemsCommand){
            // Show list containing all PIMItems associated to this category.
            // Contents of PIMItems can be viewed or modified. PIMItems can be deleted.
            try{
                Enumeration categoryItems =
                    currentPIMList.itemsByCategory(getString(getSelectedIndex()));
                Displayable screen =
                    new ItemListScreen(midlet, this, listTypeIndicator, currentPIMList, categoryItems);
                Display.getDisplay(midlet).setCurrent(screen);
            }
            catch (Exception e){
                ErrorReporter.reportError(e);
            }

        }else if(command == manageCategoryCommand){
            // Shows all PIMItems of currentPIMList in the multiselection list.
            // Selected PIMItems in the multiselection list are members of selected category
            // and the others are not.
            // Content of selected category can be changed by selecting/deselecting PIMItems
            // in the multiselection list.
            try{
                Displayable screen =
                    new CategoryDataScreen(midlet,
                                           this,
                                           listTypeIndicator,
                                           currentPIMList,
                                           getString(getSelectedIndex()));
                Display.getDisplay(midlet).setCurrent(screen);
            }
            catch (Exception e){
                ErrorReporter.reportError(e);
            }

        }else if(command == deleteCommand){
            Command[] commands = new Command[2];
            commands[0] = yesAssociatedItemsCommand;
            commands[1] = noAssociatedItemsCommand;
            GlobalServices.showModalDialog(midlet,
                       this,
                       DELETE_DIALOG_TITLE_TEXT,
                       ASSOCIATED_ITEMS_DEL_TEXT,
                       commands);

        // Commands of modal dialog
        }else if(command == yesAssociatedItemsCommand){
            // Members of category (PIMItems) and the category are deleted.
            deleteAssociatedItems = true;
            Command[] commands = new Command[2];
            commands[0] = yesDeleteCategoryCommand;
            commands[1] = noDeleteCategoryCommand;
            GlobalServices.showModalDialog(midlet,
                        this,
                        DELETE_DIALOG_TITLE_TEXT,
                        DEL_CONFIRM_TEXT_1 +  " '" +
                        getString(getSelectedIndex()) + "'" + DEL_CONFIRM_TEXT_2,
                        commands);

        }else if(command == noAssociatedItemsCommand){
            // Category is deleted but its members (PIMItems) are not.
            deleteAssociatedItems = false;
            Command[] commands = new Command[2];
            commands[0] = yesDeleteCategoryCommand;
            commands[1] = noDeleteCategoryCommand;
            GlobalServices.showModalDialog(midlet,
                        this,
                        DELETE_DIALOG_TITLE_TEXT,
                        DEL_CONFIRM_TEXT_1 + " '" +
                        getString(getSelectedIndex()) + "'" + DEL_CONFIRM_TEXT_3,
                        commands);

        }else if(command == yesDeleteCategoryCommand){
            // After final confirmation from the user, delete the category.
            try{
                currentPIMList.deleteCategory(getString(getSelectedIndex()),
                                              deleteAssociatedItems);
                updateShownCatList();
            }catch (Exception e) {
                ErrorReporter.reportError(e);
            }
            Display.getDisplay(midlet).setCurrent(this);

        }else if(command == noDeleteCategoryCommand){
            Display.getDisplay(midlet).setCurrent(this);
        }
    }


    // Add new category or edit an existing category name.
    public void configureCategory(final String currentCategoryName){
        try{
            Form form;
            final TextField[] categoryNameField = new TextField[1];
            if(currentCategoryName == null){
                categoryNameField[0] = new TextField(CATEGORY_NAMING_FIELD_LABEL +
                                                     ":",
                                                     null,
                                                     CATEGORY_NAME_LENGTH,
                                                     TextField.ANY);

                form = new Form(CATEGORY_ADD_FORM_TITLE_TEXT, categoryNameField);
            }else{
                categoryNameField[0] =
                    new TextField(CATEGORY_RENAMING_FIELD_LABEL +
                                  " '" +
                                  currentCategoryName + "' ",
                                  null,
                                  CATEGORY_NAME_LENGTH,
                                  TextField.ANY);

                form = new Form(CATEGORY_RENAME_FORM_TITLE_TEXT, categoryNameField);
            }

            final Command okCommand = new Command("OK", Command.OK, 1);
            final Command cancelCommand = new Command("Cancel", Command.CANCEL, 1);
            form.addCommand(okCommand);
            form.addCommand(cancelCommand);
            form.setCommandListener(new CommandListener(){
                    public void commandAction(Command command, Displayable d) {
                        if (command == okCommand) {
                            try{
                                // User has decided to save the category name.
                                if(categoryNameField[0].getString() != null && categoryNameField[0].size() > 0){
                                    if(currentCategoryName == null){
                                        // New category adding.
                                        currentPIMList.addCategory(categoryNameField[0].getString());
                                    }else{
                                        // Category renaming.
                                        currentPIMList.renameCategory(currentCategoryName,
                                                                      categoryNameField[0].getString());
                                    }
                                }
                                // Update shown category list.
                                CategoryListScreen.this.updateShownCatList();
                            }catch (Exception e) {
                                ErrorReporter.reportError(e);
                            }
                        }
                        // Show the CategoryListScreen again.
                        Display.getDisplay(midlet).setCurrent(CategoryListScreen.this);
                    }
                });
            // Show the form for adding/renaming category.
            Display.getDisplay(midlet).setCurrent(form);
        } catch (Exception e) {
            ErrorReporter.reportError(e);
        }
    }
}
