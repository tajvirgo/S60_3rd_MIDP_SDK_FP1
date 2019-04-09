/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.PersonalController;

import javax.microedition.lcdui.*;
import javax.microedition.pim.*;
import java.util.Hashtable;
/**
AttributeHandlingScreen handles showing/changing attributes of
selected contact field.
*/

public class AttributeHandlingScreen extends List implements CommandListener{

    private static final String TITLE_TEXT = "Attributes";

    private final PersonalController midlet;
    private final Displayable caller;
    private final PIMList currentPIMList;
    private final Hashtable PIMItemAttributes;
    private final int fieldCode;
    int[] attributes = null;
    private final Command cancelCommand = new Command("Cancel", Command.BACK, 1);
    private final Command OKCommand = new Command("Ok", Command.OK, 1);

    AttributeHandlingScreen(PersonalController midlet,
                Displayable caller,
                PIMList currentPIMList,
                Hashtable PIMItemAttributes,
                int fieldCode){
        super(TITLE_TEXT, List.MULTIPLE);
        this.midlet = midlet;
        this.caller = caller;
        this.currentPIMList=currentPIMList;
        this.PIMItemAttributes = PIMItemAttributes;
        this.fieldCode = fieldCode;

        addCommand(cancelCommand);
        addCommand(OKCommand);
        setCommandListener(this);
        attributes = currentPIMList.getSupportedAttributes(fieldCode);

        // Get attributes that are set for this field.
        int setAttributes = PIMItem.ATTR_NONE;
        Object setAttributesObject = PIMItemAttributes.get(new Integer(fieldCode));
        if(setAttributesObject != null){
            setAttributes = ((Integer) setAttributesObject).intValue();
        }

        // Put attributes into the shown list.
        for(int i = 0; i < attributes.length; i++){
            try{
              if (currentPIMList.getAttributeLabel(attributes[i])!=null)

              {
                append(currentPIMList.getAttributeLabel(attributes[i]), null);
              }else{
                break;
              }
              // Set the the attribute selected in the multiselection list if
              // the attribute is set for this field (binary and is used for testing
              // the condition).
              if((attributes[i] & setAttributes) != 0){
                setSelectedIndex(i, true);
              }
            }catch(UnsupportedFieldException ue){
              ErrorReporter.reportError(ue);
            }catch(IllegalArgumentException ie){
              ErrorReporter.reportError(ie);
            }catch(IndexOutOfBoundsException ie){
              ErrorReporter.reportError(ie);
            }
        }
    }

    public void commandAction(Command command, Displayable displayable){
        if(command == cancelCommand){
            Display.getDisplay(this.midlet).setCurrent(this.caller);
        }else if (command == OKCommand){
            saveTheResults();
            Display.getDisplay(this.midlet).setCurrent(this.caller);
        }
    }

   private void saveTheResults(){
        int resultAttributes = PIMItem.ATTR_NONE;
        if(attributes!=null){
          for (int i = 0; i < attributes.length; i++) {
            if (isSelected(i)) {
              // Since the attributes are powers of 2 they can be
              // added like this.
              resultAttributes += attributes[i];
            }
          }
          // Store the changes into the Hashtable.
          PIMItemAttributes.put(new Integer(fieldCode),
                                new Integer(resultAttributes));
        }
    }
}

