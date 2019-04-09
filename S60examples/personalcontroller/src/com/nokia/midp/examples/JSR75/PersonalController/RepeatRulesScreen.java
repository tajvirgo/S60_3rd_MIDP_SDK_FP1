/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.PersonalController;

import javax.microedition.lcdui.*;

import javax.microedition.pim.*;
import javax.microedition.pim.RepeatRule;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Calendar;
import java.util.Date;

/**
    RepeatRulesScreen handles Repeat Rules

*/



public class RepeatRulesScreen extends Form implements CommandListener{

    private static final String TITLE_TEXT = "View/modify Repeat Rule data";
    private static final String TITLE_TEXT_SEARCH = "Search by item/string";
    private static final String SEARCH_BY_STRING_TEXT = "Search by string only (fill to exclude other fields):";
    private static final String BOOLEAN_TRUE_STRING = "Yes";
    private static final String BOOLEAN_FALSE_STRING = "No";
    private static final int STRING_VALUE_SIZE = 150;
    private static final int STRING_ARRAY_ELEMENT_SIZE = STRING_VALUE_SIZE;
    private static final int INT_VALUE_SIZE = 50;

    private final Command backCommand = new Command("Back", Command.BACK, 1);
    private final Command newCommand = new Command("Create default Repeat", Command.OK, 3);
    private final Command clearCommand = new Command("Clear", Command.OK, 2);
    private final Command saveCommand = new Command("Save", Command.OK, 1);

    private final int listTypeIndicator;
    private PIMList currentPIMList;
    private final Displayable caller;
    private final PersonalController midlet;
    private PIMItem usedPIMItem;
    private EventList currentEventList;
    private Event currentEvent;
    private RepeatRule repeatRule;
    private ChoiceGroup group;

    private final int DATA_SCREEN_TYPE_VIEW = 0;
    private final int DATA_SCREEN_TYPE_SEARCH = 1;

    private final Hashtable formFields = new Hashtable(); // Stores fields of form.

    public RepeatRulesScreen(PersonalController midlet, Displayable caller, int listTypeIndicator, PIMList currentPIMList, Event currentEvent) throws PIMException{
        super(TITLE_TEXT);
        this.midlet = midlet;
        this.caller = caller;
        this.currentPIMList=currentPIMList;
        currentEventList=(EventList)currentPIMList;
        this.listTypeIndicator = listTypeIndicator;
        if(currentEvent !=null){
            this.currentEvent = currentEvent;
        } else{
            currentEvent = currentEventList.createEvent();
        }
        this.usedPIMItem = currentEvent;

        addCommand(backCommand);
        addCommand(newCommand);
        addCommand(saveCommand);
        addCommand(clearCommand);
        setCommandListener(this);

        initializeTheForm();
    }

    // Handle the menu commands (CommandListener).
    public void commandAction(Command command, Displayable displayable){
        if (command == backCommand) {
                Display.getDisplay(midlet).setCurrent(caller);
        }else if (command == clearCommand) {
            try{
                this.usedPIMItem = GlobalServices.createNewPIMItem(listTypeIndicator, currentEventList);
                initializeTheForm();
            } catch (Exception e) {
                ErrorReporter.reportError(e);
            }

        }else if (command == newCommand) {
            createNewRepeatRule();

        }else if (command == saveCommand) {
            getFormFieldValues();
            try {
                currentEvent.setRepeat(repeatRule);
                currentEvent.commit();
                initializeTheForm();
            } catch (Exception e) {
                ErrorReporter.reportError(e);
            }
        }
    }

    // Get the values from the form fields (e.g. get the values the user has written).
    private void getFormFieldValues(){
        for(Enumeration e = formFields.keys(); e.hasMoreElements();){
            Object formField = (Item) e.nextElement();
            int field = ((Integer) formFields.get(formField)).intValue();

            String str;
            int intValue;
            int fieldValue;
            switch(field){
                case RepeatRule.END:
                    try {
                        repeatRule.setDate(field, ((DateField)formField).getDate().getTime());
                    } catch (Exception ex) {
                        // The Date is null
                    }
                    break;
                // set int values
                case RepeatRule.FREQUENCY:
                    str=((ChoiceGroup) formField).getString(((ChoiceGroup) formField).getSelectedIndex()) ;
                    fieldValue = RepeatRuleData.getIntValues(str);
                    if (fieldValue!=0) {
                        repeatRule.setInt(field, fieldValue);
                    }
                    break;
                case RepeatRule.COUNT:
                case RepeatRule.INTERVAL:
                case RepeatRule.DAY_IN_MONTH:
                case RepeatRule.DAY_IN_YEAR:
                    str=((TextField) formField).getString();
                    try {
                        intValue=Integer.parseInt(str);
                        repeatRule.setInt(field,  intValue);
                    } catch (NumberFormatException nfe) {
                        // The String cannot be parsed as an int, e.g. it is NULL
                    } catch (IllegalArgumentException iae) {
                        // Field is not one of the the valid RepeatRule fields for this method,
                        // or the value provided is not a valid value for the given field
                    }
                    break;
                case RepeatRule.MONTH_IN_YEAR:
                    str=((ChoiceGroup) formField).getString(((ChoiceGroup) formField).getSelectedIndex()) ;
                    fieldValue = RepeatRuleData.getIntValues(str);
                    repeatRule.setInt(field, fieldValue);
                    break;
                case RepeatRule.WEEK_IN_MONTH:
                    str=((ChoiceGroup) formField).getString(((ChoiceGroup) formField).getSelectedIndex()) ;
                    fieldValue = RepeatRuleData.getIntValues(str);
                    repeatRule.setInt(field, fieldValue);
                    break;
                case RepeatRule.DAY_IN_WEEK:
                    str=((ChoiceGroup) formField).getString(((ChoiceGroup) formField).getSelectedIndex()) ;
                    fieldValue = RepeatRuleData.getIntValues(str);
                    repeatRule.setInt(field, fieldValue);
                    break;

            }
        }
    }


// Create new Repeat Rule for currentEvent with predefined values
// This is not normal use, this only for demo purposes
// There are few examples of how to use this functionality.
// Some of those are in comments, to show different possibilities
    private void createNewRepeatRule(){
        if (currentEventList.isSupportedField(Event.SUMMARY)){
            if( currentEvent.countValues(Event.SUMMARY) == 0 ){
                // There is no SUMMARY yet
                currentEvent.addString(Event.SUMMARY, PIMItem.ATTR_NONE, "New summary");
            }
            else if( currentEvent.countValues(Event.SUMMARY)!=0 ){
                // There is SUMMARY
            }
        } else {
            // SUMMARY is not supported
        }
        // repeatRule.setInt(RepeatRule.FREQUENCY, RepeatRule.MONTHLY);
        // repeatRule.setInt(RepeatRule.FREQUENCY, RepeatRule.WEEKLY);
        repeatRule.setInt(RepeatRule.COUNT, 5);
        // repeatRule.setInt(RepeatRule.INTERVAL, 7);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2005);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 15);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 30);
        Date date= cal.getTime();
        repeatRule.setDate(RepeatRule.END, date.getTime());

        // repeatRule.setInt(RepeatRule.DAY_IN_WEEK, RepeatRule.FRIDAY);

        try {
            initializeTheForm();
        } catch (PIMException e) {
        }
    }

    // Initializes the form fields.
    // Gets the Event's field values and puts them to corresponding form
    // fields so that user can view and change the values.
    private void initializeTheForm() throws PIMException {
        deleteAll();
        formFields.clear();

        int[] repeatFields = repeatRule.getFields();
        Item formField = null; // formField will be added to the form and shown.
        formField = new ChoiceGroup("Frequency", ChoiceGroup.POPUP, RepeatRuleData.frequencyArray, null);
        for (int i = 0; i < repeatFields.length; i++) {
            if (repeatFields[i] ==RepeatRule.FREQUENCY){
                ((ChoiceGroup) formField).setSelectedIndex(
                    RepeatRuleData.getArrayIndex(repeatRule.getInt(RepeatRule.FREQUENCY), RepeatRuleData.frequencyArray), true);
            }
        }
        // Add the form field to the form.
        if (formField != null) {
            append(formField);
            formFields.put(formField, new Integer(RepeatRule.FREQUENCY));
        }

/**
        RepeatRule.DAILY supports   COUNT,INTERVAL, END
        RepeatRule.WEEKLY supports  COUNT,INTERVAL, END, DAY_IN_WEEK
        RepeatRule.MONTHLY supports COUNT,INTERVAL, END, DAY_IN_WEEK, WEEK_IN_MONTH, DAY_IN_MONTH
        RepeatRule.YEARLY supports  COUNT,INTERVAL, END, MONTH_IN_YEAR, DAY_IN_WEEK, WEEK_IN_MONTH, DAY_IN_MONTH, DAY_IN_YEAR
*/
        // int[] supportedFields = currentEventList.getSupportedRepeatRuleFields(RepeatRule.DAILY);
        // int[] supportedFields = currentEventList.getSupportedRepeatRuleFields(RepeatRule.WEEKLY);
        // int[] supportedFields = currentEventList.getSupportedRepeatRuleFields(RepeatRule.MONTHLY);
        int[] supportedFields = currentEventList.getSupportedRepeatRuleFields(RepeatRule.YEARLY);

        for (int i = 0; i < supportedFields.length; i++) {
            formField = null; // formField will be added to the form and shown.
            int supportedFieldDataType=supportedFields[i];
            boolean repeatFound=false;
            for (int j = 0; j < repeatFields.length; j++) {
                if (repeatFields[j]== supportedFields[i]){
                    repeatFound=true;
                    // break;
                }
            }
        int fieldValue;
        String str=null;

            switch (supportedFieldDataType) {

                case RepeatRule.COUNT:
                    formField = new TextField("Count", null, INT_VALUE_SIZE, TextField.DECIMAL);
                    if(repeatFound){
                        fieldValue=repeatRule.getInt(supportedFieldDataType);
                        ((TextField) formField).setString(Integer.toString(fieldValue));
                    }
                    break;

                case RepeatRule.INTERVAL:
                    formField = new TextField("Interval", null, INT_VALUE_SIZE, TextField.DECIMAL);
                    if(repeatFound){
                        fieldValue=repeatRule.getInt(supportedFieldDataType);
                        ((TextField) formField).setString(Integer.toString(fieldValue));
                    }
                    break;

                case RepeatRule.END:
                    formField = new DateField("Ending date", DateField.DATE_TIME);
                    if(repeatFound){
                        ((DateField) formField).setDate(new Date(repeatRule.getDate(supportedFields[i])));
                    }
                    break;

                case RepeatRule.MONTH_IN_YEAR:
                    formField = new ChoiceGroup("Month in year", ChoiceGroup.POPUP, RepeatRuleData.monthInYearArray, null);
                    if(repeatFound){
                        ((ChoiceGroup) formField).setSelectedIndex(RepeatRuleData.getArrayIndex((repeatRule.getInt(supportedFields[i])), RepeatRuleData.monthInYearArray), true);
                    }
                    break;

                case RepeatRule.WEEK_IN_MONTH:
                    formField = new ChoiceGroup("Week in month", ChoiceGroup.POPUP, RepeatRuleData.weekInMonthArray, null);
                    if(repeatFound){
                        ((ChoiceGroup) formField).setSelectedIndex(RepeatRuleData.getArrayIndex((repeatRule.getInt(supportedFields[i])), RepeatRuleData.weekInMonthArray), true);
                    }
                    break;

                case RepeatRule.DAY_IN_WEEK:
                    formField = new ChoiceGroup("Day in week", ChoiceGroup.POPUP, RepeatRuleData.dayInWeekArray, null);
                    if(repeatFound){
                        ((ChoiceGroup) formField).setSelectedIndex(RepeatRuleData.getArrayIndex((repeatRule.getInt(supportedFields[i])), RepeatRuleData.dayInWeekArray), true);
                    }
                    break;

                case RepeatRule.DAY_IN_MONTH:
                    formField = new TextField("Day in month", null, INT_VALUE_SIZE, TextField.DECIMAL);
                    if(repeatFound){
                        fieldValue=repeatRule.getInt(supportedFieldDataType);
                        ((TextField) formField).setString(Integer.toString(fieldValue));
                    }
                    break;

                case RepeatRule.DAY_IN_YEAR:
                    formField = new TextField("Day in year", null, INT_VALUE_SIZE, TextField.DECIMAL);
                    if(repeatFound){
                        fieldValue=repeatRule.getInt(supportedFieldDataType);
                        ((TextField) formField).setString(Integer.toString(fieldValue));
                    }
                    break;

            }
            if (formField != null) {
            // Add the form field to the form.
                append(formField);
                // Put the field to the hashtable.
                formFields.put(formField, new Integer(supportedFieldDataType));
            }
        }
    }


}
