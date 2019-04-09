/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.PersonalController;

import javax.microedition.lcdui.*;
import javax.microedition.pim.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Date;

/**

ItemDataScreen reads the data of given PIMItem and puts the data into the Form
so that user can view and change the data. ItemDataScreen can also save the
data from Form fields (the data that user has possibly changed by editing the
values of the Form fields) to the PIMItem and commit the changes so that
the modified data is committed into the PIMList.

ItemDataScreen contains search functionalities ("search by PIMItem" and "search by
string" functionalities. [see the PIM API]). If the class instance is created
by the second constructor (the one that contains no reference to PIMItem) the
class instance creates temporary PIMItem that is used in the search functionality
but that is never saved/committed.

*/

public class ItemDataScreen extends Form implements ItemCommandListener, CommandListener{

    private static final String TITLE_TEXT = "View/modify data";
    private static final String TITLE_TEXT_SEARCH = "Search by item/string";
    private static final String SEARCH_BY_STRING_TEXT = "Search by string only (fill to exclude other fields):";
    private static final String BOOLEAN_TRUE_STRING = "Yes";
    private static final String BOOLEAN_FALSE_STRING = "No";
    private static final int STRING_VALUE_SIZE = 150;
    private static final int STRING_ARRAY_ELEMENT_SIZE = STRING_VALUE_SIZE;
    private static final int INT_VALUE_SIZE = 50;

    private final Command backCommand = new Command("Back", Command.BACK, 1);
    private final Command searchCommand = new Command("Search now", Command.OK, 1);
    private final Command clearCommand = new Command("Clear", Command.OK, 2);
    private final Command editArrayCommand = new Command("Edit", Command.OK, 2);
    private final Command editBooleanCommand = new Command("Edit", Command.OK, 2);
    private final Command saveCommand = new Command("Save", Command.OK, 2);
    private final Command setAttributesCommand = new Command("Field attributes", Command.OK, 2);

    private final int listTypeIndicator;
    private final PIMList currentPIMList;
    private final Displayable caller;
    private final PersonalController midlet;
    private PIMItem usedPIMItem;

    private ChoiceGroup groupClass;

    private final int DATA_SCREEN_TYPE_VIEW = 0;
    private final int DATA_SCREEN_TYPE_SEARCH = 1;
    private final int DataScreenType;

    private final String[] priorityArray={ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
    private final int[] priorityIntArray={ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    private Item searchStringItem = null; // For "Search by String" functionality.

    private final Hashtable formFields = new Hashtable(); // Stores fields of form.
    private final Hashtable stringArrays = new Hashtable(); // Stores contents of string array fields.
    private final Hashtable PIMItemAttributes = new Hashtable(); // Stores field attribute information.

    // For viewing/modifying data of existing item.
    public ItemDataScreen(PersonalController midlet,
                          Displayable caller,
                          int listTypeIndicator,
                          PIMList currentPIMList,
                          PIMItem usedPIMItem) throws PIMException{
        super(TITLE_TEXT);
        this.listTypeIndicator = listTypeIndicator;
        this.currentPIMList = currentPIMList;
        this.midlet = midlet;
        this.caller = caller;
        this.usedPIMItem = usedPIMItem;
        this.DataScreenType = DATA_SCREEN_TYPE_VIEW;

        addCommand(backCommand);
        addCommand(saveCommand);
        setCommandListener(this);
        initializeTheForm();
    }


    // For search functionality.
    public ItemDataScreen(PersonalController midlet,
                          Displayable caller,
                          int listTypeIndicator,
                          PIMList currentPIMList) throws PIMException{
        super(TITLE_TEXT_SEARCH);
        this.listTypeIndicator = listTypeIndicator;
        this.currentPIMList = currentPIMList;
        this.midlet = midlet;
        this.caller = caller;
        this.usedPIMItem = GlobalServices.createNewPIMItem(listTypeIndicator, currentPIMList);
        this.DataScreenType = DATA_SCREEN_TYPE_SEARCH;

        addCommand(backCommand);
        addCommand(searchCommand);
        addCommand(clearCommand);
        setCommandListener(this);
        searchStringItem = new TextField(SEARCH_BY_STRING_TEXT, null, STRING_VALUE_SIZE, TextField.ANY);
        initializeTheForm();
    }


    // Handle the menu commands (CommandListener).
    public void commandAction(Command command, Displayable displayable){
        if (command == backCommand) {
            Display.getDisplay(midlet).setCurrent(caller);

        }else if (command == clearCommand) {
            try{
                this.usedPIMItem = GlobalServices.createNewPIMItem(listTypeIndicator, currentPIMList);
                initializeTheForm();
            } catch (Exception e) {
                ErrorReporter.reportError(e);
            }

        }else if (command == saveCommand) {
            try {
              getFormFieldValues();
            }
            catch (Exception e)
            {
              return;  // user alerted already.
            }
            int representativeField=0;
            String alertText="";
            // Check if there is a field value to show in the ItemDataScreen.
            // If not, this item must not save. In this case, alert user
            if (listTypeIndicator== PIM.EVENT_LIST){
                representativeField = usedPIMItem.countValues(Event.SUMMARY);
                alertText="Summary -field is empty. This contact item is not saved";
            }else if (listTypeIndicator== PIM.TODO_LIST){
                representativeField = usedPIMItem.countValues(ToDo.SUMMARY);
                alertText="Summary -field is empty. This contact item is not saved";
            } else {
              representativeField = 1; // continue on with commit...
            }

            if(representativeField == 0){
                Alert alert = new Alert("Error when saving item", alertText, null, AlertType.ERROR );
                alert.setTimeout(Alert.FOREVER);
               alert.setCommandListener(this);
                Display.getDisplay(midlet).setCurrent(alert);
           } else {
                try {
                    usedPIMItem.commit();
                    initializeTheForm();
                    ((ItemListScreen) caller).updateShownItemList(usedPIMItem);
                } catch (Exception e) {
                    ErrorReporter.reportError(e);
                }
            }

        }else if (command == Alert.DISMISS_COMMAND) {
                Display.getDisplay(midlet).setCurrent(this);
        }else if (command == searchCommand) {
            Enumeration foundItems = null;
            try{
                if(searchStringItem!=null && ((TextField)searchStringItem).getString()!=null &&
                   !((TextField)searchStringItem).getString().equals("")){
                    // Search by string.
                    foundItems = currentPIMList.items(((TextField)searchStringItem).getString());
                }else{
                    // Search by matching PIMitem.
                    try {
                      getFormFieldValues();
                    }
                    catch (Exception e)
                    {
                      return;  // user alerted already.
                    }

                    foundItems = currentPIMList.items(usedPIMItem);
                }
                // View the search result
                ItemListScreen itemListScreen =
                    new ItemListScreen(this.midlet, this, this.listTypeIndicator, this.currentPIMList, foundItems);
                Display.getDisplay(midlet).setCurrent(itemListScreen);
            }catch (Exception e){
                ErrorReporter.reportError(e);
            }
        }
    }

    // Handle the Form Item commands (ItemCommandListener).
    public void commandAction(Command command, final Item formField) {

        final int field = ((Integer) formFields.get(formField)).intValue();

        // If this is an edit array command and the field is a string array then
        // handle edit of the strings
        if ((command == editArrayCommand) && (currentPIMList.getFieldDataType(field)==PIMItem.STRING_ARRAY))
        {

            // Create array of TextFields containing as many TextFields
            // as there are supported array elements in the field value.
            // (Field value is an STRING_ARRAY which may contain unsupported
            // fields. It is no use to show these unsupported fields since
            // their value cannot be saved.)

            final TextField[] textFields = new TextField[currentPIMList.getSupportedArrayElements(field).length];

            // Get the corresponding field value from the hashtable.
            // (Note that this value array contains all possible array
            // elements and may thus also contain unsupported fields
            // having null-value. These unsupported fields are not
            // shown.)
            final String[] valueArray = (String[]) stringArrays.get(new Integer(field));

            for (int i = 0, k = 0; i < valueArray.length; i++) {
                if(currentPIMList.isSupportedArrayElement(field, i)){
                    textFields[k] = new TextField(currentPIMList.getArrayElementLabel(field, i),
                                                  valueArray[i],
                                                  STRING_ARRAY_ELEMENT_SIZE,
                                                  TextField.ANY);
                    k++;
                }
            }

            Form form = new Form(currentPIMList.getFieldLabel(field) + " values", textFields);
            final Command okCommand = new Command("OK", Command.OK, 1);
            final Command cancelCommand = new Command("Cancel", Command.CANCEL, 1);
            form.addCommand(okCommand);
            form.addCommand(cancelCommand);
            form.setCommandListener(new CommandListener() {
                    public void commandAction(Command command, Displayable d) {
                        if (command == okCommand) {
                            for (int i = 0, k = 0; i < valueArray.length; i++) {
                                // Get the data from TextFields. Note that
                                // textFields contains only supported array
                                // elements while valueArray contains them all.
                                if(currentPIMList.isSupportedArrayElement(field, i)){
                                    valueArray[i] = textFields[k].getString();
                                    k++;
                                }
                            }
                        }
                        // Return from the STRING_ARRAY editing screen.
                        Display.getDisplay(midlet).setCurrent(ItemDataScreen.this);
                    }
                });
            // Show the STRING_ARRAY editing screen.
            Display.getDisplay(midlet).setCurrent(form);

        } else if (command == editBooleanCommand) {
            // formField contains either null-value or BOOLEAN_TRUE_STRING
            // or BOOLEAN_FALSE_STRING. User can switch between these values.
            String booleanValue = ((StringItem) formField).getText();
            if(booleanValue == null){
                ((StringItem) formField).setText(BOOLEAN_TRUE_STRING);
            }else if (booleanValue.equals(BOOLEAN_TRUE_STRING)){
                ((StringItem) formField).setText(BOOLEAN_FALSE_STRING);
            }else{
                ((StringItem) formField).setText(null);
            }

        } else if(command == setAttributesCommand){
            // Set attributes for the selected field (index 0..only first value
            // is used).
            Displayable screen =
                new AttributeHandlingScreen(this.midlet,
                            this,
                            currentPIMList,
                            PIMItemAttributes,
                            ((Integer) formFields.get(formField)).intValue());
            Display.getDisplay(midlet).setCurrent(screen);
        }
    }

    // Get the values from the form fields (e.g. get the values the user has written).
    private void getFormFieldValues() throws Exception {
        for(Enumeration e = formFields.keys(); e.hasMoreElements();){
            Object formField = (Item) e.nextElement();
            int field = ((Integer) formFields.get(formField)).intValue();

            // Check that the field is not one of the fields automatically set/updated by PIMList.
            // There is sence to show the values of those fields but no sence to save them
            // (despites they cannot be saved, Exception is thrown if tried).
            if((listTypeIndicator == PIM.CONTACT_LIST && (field == Contact.UID || field == Contact.REVISION)) ||
               (listTypeIndicator == PIM.TODO_LIST && (field == ToDo.UID || field == ToDo.REVISION)) ||
               (listTypeIndicator == PIM.EVENT_LIST && (field == Event.UID || field == Event.REVISION))){
                continue;
            }

            switch(currentPIMList.getFieldDataType(field)){
                case PIMItem.BOOLEAN:{
                    String s = ((StringItem) formField).getText();
                    if(s != null && !s.equals("")){
                        if(usedPIMItem.countValues(field) > 0){
                            // Set the value since there are already values set.
                            usedPIMItem.setBoolean(field,
                                0,
                                getAttributes(field),
                                ((StringItem) formField).getText().equals(BOOLEAN_TRUE_STRING));
                        } else {
                            // No previous values. Add new value.
                            usedPIMItem.addBoolean(field,
                                getAttributes(field),
                                ((StringItem) formField).getText().equals(BOOLEAN_TRUE_STRING));
                        }
                    }else if(usedPIMItem.countValues(field) > 0){
                        // Set null-value, since the user has not set any value.
                        usedPIMItem.removeValue(field, 0);
                    }
                    break;
                }
                case PIMItem.DATE:{
                    Date d = ((DateField) formField).getDate();
                    if(d != null){
                        if(usedPIMItem.countValues(field) > 0){
                            usedPIMItem.setDate(field,
                                0,
                                getAttributes(field),
                                ((DateField)formField).getDate().getTime());
                        } else {
                            usedPIMItem.addDate(field,
                                getAttributes(field),
                                ((DateField)formField).getDate().getTime());
                        }
                    } else if(usedPIMItem.countValues(field) > 0){
                        usedPIMItem.removeValue(field, 0);
                    }
                    break;
                }
                case PIMItem.INT:{
                    String s;
                    // This way it is easy to handle different specific fields
                    if ((listTypeIndicator== PIM.CONTACT_LIST && field==Contact.CLASS)
                        || (listTypeIndicator== PIM.EVENT_LIST && field==Event.CLASS)
                        || (listTypeIndicator== PIM.TODO_LIST && field==ToDo.CLASS)){
                        // Choose class, not implemented
                    } else if (listTypeIndicator== PIM.TODO_LIST && field==ToDo.PRIORITY){
                        s=((ChoiceGroup) formField).getString(((ChoiceGroup) formField).getSelectedIndex()) ;
                        if(s != null && !s.equals("")){
                            try{
                                if(usedPIMItem.countValues(field) > 0){
                                    usedPIMItem.setInt(field, 0, getAttributes(field), priorityIntArray[((ChoiceGroup) formField).getSelectedIndex()]);
                                } else {
                                    usedPIMItem.addInt(field, getAttributes(field), priorityIntArray[((ChoiceGroup) formField).getSelectedIndex()]);
                                }
                            }catch(Exception exception){
                                ErrorReporter.reportError(exception);
                            }
                        } else if(usedPIMItem.countValues(field) > 0){
                            usedPIMItem.removeValue(field, 0);
                        }
                    } else{
                        s = ((TextField) formField).getString();
                        if(s != null && !s.equals("")){
                            try{
                                if(usedPIMItem.countValues(field) > 0){
                                    usedPIMItem.setInt(field, 0, getAttributes(field), Integer.parseInt(((TextField) formField).getString()));
                                } else {
                                    usedPIMItem.addInt(field, getAttributes(field), Integer.parseInt(((TextField) formField).getString()));
                                }
                            }catch(Exception exception){
                                ErrorReporter.reportError(exception);
                            }
                        } else if(usedPIMItem.countValues(field) > 0){
                            usedPIMItem.removeValue(field, 0);
                        }
                    }

                    break;
                }
                case PIMItem.STRING:{
                    String s = ((TextField) formField).getString();
                    if(s != null && !s.equals("")){
                        if(usedPIMItem.countValues(field) > 0){
                            usedPIMItem.setString(field, 0, getAttributes(field), s);
                        }else{
                            usedPIMItem.addString(field, getAttributes(field), s);
                        }
                    } else if(usedPIMItem.countValues(field) > 0){
                        usedPIMItem.removeValue(field, 0);
                    }
                    break;
                }
            }
        }


        // Handle the string array fields.
        for(Enumeration e = stringArrays.keys(); e.hasMoreElements();){
            Integer field = (Integer)e.nextElement();
            String[] stringArray = (String[]) stringArrays.get(field);

            // Check that there is at least one non-null string in string array
            // before saving the value.
            boolean stringFound = false;
            for(int i=0;i<stringArray.length;i++){
                if (stringArray[i] != null){
                    stringFound = true;
                    break;
                }
            }
            try
            {
              if (stringFound) {
                if (usedPIMItem.countValues(field.intValue()) > 0) {
                  usedPIMItem.setStringArray(field.intValue(), 0,
                                             PIMItem.ATTR_NONE, stringArray);
                }
                else {
                  usedPIMItem.addStringArray(field.intValue(),
                                             PIMItem.ATTR_NONE, stringArray);
                }
              }
            }catch (Exception ex)
            {
              Alert alert = new Alert("Error when saving",
                                      "strings",
                                      null,
                                      AlertType.ERROR );
              alert.setTimeout(Alert.FOREVER);
              alert.setCommandListener(this);
              Display.getDisplay(midlet).setCurrent(alert);
              // rethrow the exception so that we don't try to commit without
              // the required information
              throw(ex);
            }
        }
    }

    // Initializes the form fields.
    // Gets the PIMItem's field values and puts them to corresponding form
    // fields so that user can view and change the values.
    private void initializeTheForm() throws PIMException {
        deleteAll();
        formFields.clear();
        stringArrays.clear();

        // In case of Searching add an additional form field
        // that has no counterpart in usedPIMItem fields.
        // The form field is used by "search by string" functionality.
        if(searchStringItem!=null){
            // Clear the content of searchStringItem.
            ((TextField) searchStringItem).setString(null);
            append(searchStringItem);
        }

        int[] supportedFields = currentPIMList.getSupportedFields();

        for (int i = 0; i < supportedFields.length; i++) {
            Item formField = null; // formField will be added to the form and shown.
            int fieldDataType = currentPIMList.getFieldDataType(supportedFields[i]);
            String fieldLabel = currentPIMList.getFieldLabel(supportedFields[i]);
            int fieldStyle = TextField.ANY; // Default field style.

            switch (fieldDataType) {
                case PIMItem.BOOLEAN: {
                    if(usedPIMItem.countValues(supportedFields[i])>0){
                        // The field has got value(s). The first can be read.
                        formField =
                            new StringItem(fieldLabel,
                                           usedPIMItem.getBoolean(supportedFields[i], 0) ?
                                           BOOLEAN_TRUE_STRING:BOOLEAN_FALSE_STRING);
                    } else {
                        // The field has no values. One cannot be read.
                        // Fill the formField with empty value.
                        formField = new StringItem(fieldLabel, null);
                    }
                    formField.setDefaultCommand(editBooleanCommand);
                    break;
                }
                case PIMItem.DATE: {
                    fieldStyle = DateField.DATE_TIME;
                    formField = new DateField(fieldLabel, fieldStyle);
                    if(usedPIMItem.countValues(supportedFields[i])>0){
                        ((DateField) formField).setDate(new Date(usedPIMItem.getDate(supportedFields[i],0)));
                    }
                    break;
                }
                case PIMItem.INT: {

                    if ((listTypeIndicator== PIM.CONTACT_LIST && supportedFields[i]==Contact.CLASS)
                        || (listTypeIndicator== PIM.EVENT_LIST && supportedFields[i]==Event.CLASS)
                        || (listTypeIndicator== PIM.TODO_LIST && supportedFields[i]==ToDo.CLASS)){
                        // Choose class, not implemented
                    } else if (listTypeIndicator== PIM.TODO_LIST && supportedFields[i]==ToDo.PRIORITY){
                        // Choose priority
                        formField = new ChoiceGroup(fieldLabel, ChoiceGroup.POPUP, priorityArray, null);
                        if(usedPIMItem.countValues(supportedFields[i])>0){
                            ((ChoiceGroup) formField).setSelectedIndex(usedPIMItem.getInt(supportedFields[i],0), true);
                        }
                    } else{
                        if(usedPIMItem.countValues(supportedFields[i])>0){
                            formField =
                                new TextField(fieldLabel,
                                              Integer.toString(usedPIMItem.getInt(supportedFields[i],0)),
                                              INT_VALUE_SIZE,
                                              TextField.DECIMAL);
                        } else {
                            formField = new TextField(fieldLabel, null, INT_VALUE_SIZE, TextField.DECIMAL);
                        }
                    }
                    break;
                }
                case PIMItem.STRING: {
                    // Fine tune the formField style.
                    if(listTypeIndicator == PIM.CONTACT_LIST){
                        if(supportedFields[i] == Contact.TEL){
                            fieldStyle = TextField.PHONENUMBER;
                        }else if(supportedFields[i] == Contact.EMAIL){
                            fieldStyle = TextField.EMAILADDR;
                        }else if(supportedFields[i] == Contact.URL){
                            fieldStyle = TextField.URL;
                        }
                    }
                    try {
                        String fieldValue = null;
                        boolean isUID = false;
                        if(supportedFields[i]==Contact.UID || supportedFields[i]==Event.UID
                           ||supportedFields[i]==ToDo.UID){
                          isUID = true;
                        }
                        if(usedPIMItem.countValues(supportedFields[i])>0){
                            fieldValue = usedPIMItem.getString(supportedFields[i], 0);
                        }
                        //Not display UID value to the user to avoid confusion, UID value
                        //is not editable for the user and the value may be binary data depends on
                        // the device implementation.
                        if (!isUID){
                          formField = new TextField(fieldLabel, fieldValue,
                              STRING_VALUE_SIZE, fieldStyle);
                        }
                    } catch (IllegalArgumentException e) {
                        formField = new TextField(fieldLabel, null, STRING_VALUE_SIZE, TextField.ANY);
                    }
                    break;
                }
                case PIMItem.STRING_ARRAY: {
                    // Show the label only.
                    formField = new StringItem(fieldLabel, null);

                    // Array values are shown in the separate screen if the field is selected.
                    formField.setDefaultCommand(editArrayCommand);

                    // Add the field value to stringArrays-hashtable.
                    if(usedPIMItem.countValues(supportedFields[i])>0){
                        // Get the array of field values.
                        String[] valueArray = usedPIMItem.getStringArray(supportedFields[i],0);
                        // Create String array of same size as value array.
                        String[] fieldArray = new String[valueArray.length];
                        // Copy the values (note that all elements of array might
                        // not be supported. Unsupported elements have null-value).
                        for(int k = 0;k < valueArray.length;k++){
                        if (valueArray[k] == null)
                        {
                          fieldArray[k] = null;
                        }
                        else
                        {
                            fieldArray[k] = new String(valueArray[k]);
                        }
                      }
                        stringArrays.put(new Integer(supportedFields[i]), fieldArray);
                    }else{
                        // If no values, put an empty string array to hashtable.
                        stringArrays.put(new Integer(supportedFields[i]),
                                         new String[currentPIMList.stringArraySize(supportedFields[i])]);
                    }
                    break;
                }
            }
            if (formField != null) {
              // Add the form field to the form.

              // If both NAME and FORMATTED_NAME are supported give preference
              // to NAME
              if ( (currentPIMList.isSupportedField(Contact.NAME) == true) &&
                   (supportedFields[i] == Contact.FORMATTED_NAME) )
               {
                 continue;
               }

                append(formField);
                // Put the field to the hashtable.
                formFields.put(formField, new Integer(supportedFields[i]));
                formField.setItemCommandListener(this);

                // Attributes are used only in case of Contacts (well..if ATTR_NONE is not
                // seen as an attribute).
                if(listTypeIndicator == PIM.CONTACT_LIST){
                    // Get the attributes from the PIM item and store the attribute data
                    // into the PIMItemAttributes Hashtable.
                    if(usedPIMItem.countValues(supportedFields[i]) > 0){
                        PIMItemAttributes.put(new Integer(supportedFields[i]),
                                  new Integer(usedPIMItem.getAttributes(supportedFields[i], 0)));
                    }
                    // Allow current field to be selected for viewing/changing
                    // (e.g. editArrayCommand is shown when the field is selected).
                    formField.addCommand(setAttributesCommand);
                    formField.setDefaultCommand(editArrayCommand);
                }
            }
        }
    }

    // Reads the attributes of the field from PIMItemAttributes Hashtable.
    // If no attribute is set ATTR_NONE is returned.
    private int getAttributes(int fieldCode){
        int attributes = PIMItem.ATTR_NONE;
        Object attributeValueObject = PIMItemAttributes.get(new Integer(fieldCode));
        if(attributeValueObject != null){
            attributes = ((Integer) attributeValueObject).intValue();
        }
        return attributes;
    }
}
