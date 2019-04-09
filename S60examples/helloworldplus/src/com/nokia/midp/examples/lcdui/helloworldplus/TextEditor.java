/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.lcdui.helloworldplus;

import javax.microedition.lcdui.*;

/**
 * The TextEditor class allows the user to edit the message displayed.
 * <p>
 * This class extends the Form class and uses an instance of TextField to
 * provide an editable text field.
 * <p>
 * The initial text message can set via the constructor or via setText() method.
 */

public class TextEditor extends Form implements CommandListener {

    /** Reference to the instance of the TextField class created and appended to the form. */
    private TextField textField;

    /**
     * Back reference to parent MIDlet class enabling this class to access
     * to access the callback methods provided by HelloWorldMIDlet.
     */
    private final HelloWorldPlusMIDlet midlet;

    /** Priority of commands relative to others of the same type. */
    private static final int COMMAND_PRIORITY = 1;

    /** Command specified to dismiss the TextEditor form and accept the edited text. */
    private static final Command CMD_OK =
            new Command("OK", Command.OK, COMMAND_PRIORITY);

    /** Command specified to dismiss the TextEditor form and reject the edited text. */
    private static final Command CMD_CANCEL =
            new Command("Cancel", Command.CANCEL, COMMAND_PRIORITY);

    /** The maximum size of the text allowed in the text field.   */
    private static final int MAX_TEXT_SIZE = 40;

    /**
     * The constructor initializes the class making it ready for use.
     * <p>
     * It initializes the reference to the parent MIDlet class with the paramater
     * value 'midlet'.
     * <br>It creates an instance of TextField initialized with the parameter 'message'.
     * <br>It appends the created instance of TextField to the form.
     * <br>It adds the Command objects defined to the command handling framework.
     * <br>It calls setCommandListener() to register itself with the command framework
     * as the command listener and will be notified when a command is activated.
     *
     * @param midlet is a reference to the parent MIDlet. Enables callback to parent object.
     * @param message sets the opening text in the text field.
     */
    TextEditor(HelloWorldPlusMIDlet midlet, String message) {
        super("Hello World Plus MIDlet");
        this.midlet = midlet;

        // Create and append an instance of  TextField to the form.
        textField = new TextField("Edit message",
                                  message,
                                  MAX_TEXT_SIZE,
                                  TextField.ANY);
        append(textField);

        // Add the Commands define to the command handling framework
        addCommand(CMD_OK);
        addCommand(CMD_CANCEL);
        setCommandListener(this);
    }

    /**
     * Handles commands received.
     * <p>
     * The CMD_OK command instructs the MIDlet to dismiss the form and accept
     * the edited text. The method handles this command by calling the parent
     * method textEditorDone() passing an initialised instance of String.
     * <p>
     * The CMD_CANCEL command instructs the MIDlet to dismiss the form and
     * reject the edited text. The method handles this command by calling the
     * parent method textEditorDone() passing an uninitialised reference.
     *
     * @param cmd is the identity of command received from the framework.
     * @param source is the identity of the UI area displayed which generated the
     * command.
     */
    public void commandAction(Command cmd, Displayable source) {
        if (cmd == CMD_OK) {
            midlet.textEditorDone(textField.getString());
        } else if (cmd == CMD_CANCEL) {
            midlet.textEditorDone(null);
        } else {
            // Functionality to handle for unexpected commands may be added here...
        }
    }

    /**
     * Sets the opening text in the textfield.
     *
     * @param message is the opening text to be displayed
     */
    public void setText(String message) {
        textField.setString(message);
    }

}