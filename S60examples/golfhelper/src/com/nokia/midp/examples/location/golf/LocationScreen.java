/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.location.golf;

import javax.microedition.lcdui.*;

/**
 * The LocationScreen class displays a message on the screen.
 * <p>
 * This class extends the Form class and uses an instance of StringItem to
 * display the text message. The Form with StringItem implementation was chosen
 * because it allows a non-editable text message to be written onto the screen.
 * <p>
 * The text message can set via the constructor and setCurrentText() method.
 */

public class LocationScreen extends Form implements CommandListener {

    /** Reference to the string item added to the form for displaying a message. */
    private StringItem stringItem;

    /**
     *  Reference to the parent MIDlet class enabling this class
     *  to access the callback methods provided by HelloWorldMIDlet.
     */
    private final GolfHelperMIDlet midlet;

    /** Priority of commands relative to others of the same type. */
    private static final int COMMAND_PRIORITY = 1;

    /** Command specified to initiate the termination of the MIDlet. */

    private static final Command CMD_EXIT =
            new Command("Exit", Command.EXIT, COMMAND_PRIORITY);

    /** Command specified to initiate the launch of the TextEditor screen. */
    private static final Command CMD_EDIT =
            new Command("Select Hole", Command.ITEM, COMMAND_PRIORITY);

    /**
     * The constructor initializes the class making it ready for use.
     * <p>
     * It initializes the reference to the parent MIDlet class with the paramater
     * value 'midlet'.
     * <br>It creates an instance of the class ItemString and initializes it with
     * the parameter value 'message'.
     * <br>It appends the created instance of ItemString to the form.
     * <br>It adds the Command objects defined to the command handling framework.
     * <br>It calls setCommandListener() to register itself with the command framework
     * as the command listener and will be notified when a command is activated.
     *
     * @param midlet is a reference to the parent MIDlet. Enables callback to parent object.
     * @param message sets displayed text.
     */
    LocationScreen(GolfHelperMIDlet midlet, String message) {
        super("Golf Location Helper");
        this.midlet = midlet;

        // Create and append a StringItem to the form.
        stringItem = new StringItem(null,message);
        stringItem.setLayout(Item.LAYOUT_CENTER);
        append(stringItem);

        // Add Commands objects to the command handling framework
        addCommand(CMD_EXIT);
        addCommand(CMD_EDIT);
        setCommandListener(this);
    }

    /**
     * Sets the text to be displayed.
     */
    public void setCurrentText(String message) {
        stringItem.setText(message);
    }

    /**
     * Returns the text currently displayed
     */
    public String getCurrentText() {
        return stringItem.getText();
    }

    /**
     * Handles commands received.
     * <p>
     * The CMD_EXIT command initiates the termination of the MIDlet. The method
     * handles this command by calling the parent method exitRequested().
     * <p>
     * The CMD_EDIT command initiates the launch of the hole selector screen. The
     * method handles this command by calling the parent method locationRequested().
     *
     * @param cmd is the identity of command received from the framework.
     * @param source is the identity of the UI area displayed which generated the
     * command.
     */
    public void commandAction(Command cmd, Displayable source) {
        if (cmd == CMD_EXIT) {
            midlet.exitRequested();
        } else if (cmd == CMD_EDIT) {
            midlet.locationRequested();
        } else {
            // Functionality to handle for unexpected commands may be added here...
        }
    }
}
