/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.lcdui.helloworldplus;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * This class illustrates the implementation of a simple MIDlet that initially
 * displays a "HelloWorld" message to the screen and allows the user to edit
 * that message.
 * <p>
 * This class extends the class javax.microedition.midlet.MIDlet. It
 * creates and maintains references to a TextScreen object and a
 * TextEditor object.
 * <p>
 * Note that the HelloWorldMIDlet class has no constructor. MIDlet
 * contructors are not required to do anything because intializing of the
 * object is better performed in the startApp() method.
 */
public class HelloWorldPlusMIDlet extends MIDlet {

    /** Displays the message on the screen. */
    private TextScreen textScreen;

    /** Allows the user to edit the message displayed. */
    private TextEditor textEditor;

    /** A generic way of indicating whether startApp() has previously been called. */
    private Display display;

    /**
     * Creates an instance of TextScreen if one has not already been
     * created and tells the framework to set this instance of TextScreen
     * as the current screen.
     */
    public void startApp() {
        if (display == null) {
            // First time we've been called.
            display=Display.getDisplay(this);
            textScreen = new TextScreen(this, "Hello World!");
        }
        display.setCurrent(textScreen);
    }

    /**
     * This must be defined but no implementation is required because the MIDlet
     * only responds to user interaction.
     */
    public void pauseApp() {
    }

    /**
     * No further implementation is required because the MIDlet holds no
     * resources that require releasing.
     *
     * @param unconditional is ignored.
     */
    public void destroyApp(boolean unconditional) {
    }

    /**
     * A convenience method for exiting.
     */
    public void exitRequested(){
        destroyApp(false);

        /*
         * notifyDestroyed() tells the scheduler that this MIDlet is now in a
         * destroyed state and is ready for disposal.
         */
        notifyDestroyed();
    }

    /**
     * Implements the transition from the TextEditor screen to the TextScreen screen.
     *
     * @param string is the new text to be displayed. It is null if the text is
     * not to be changed.
     */
    public void textEditorDone(String string) {
        if (string != null) {
            textScreen.setCurrentText(string);
        }
        display.setCurrent(textScreen);
    }

    /**
     * Implements the transition from the TextScreen screen to the TextEditor screen.
     */
    public void textEditorRequested() {
        String currentText = textScreen.getCurrentText();
        if (textEditor == null) {
            textEditor = new TextEditor(this, currentText);
        } else {
            textEditor.setText(currentText);
        }
        display.setCurrent(textEditor);
    }
}
