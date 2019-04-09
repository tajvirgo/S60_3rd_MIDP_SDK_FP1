/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.hawk;

import javax.microedition.lcdui.*;


/**
 * InstructionsScreen: Instructions Screen for game prototype.
 */
class InstructionsScreen
    extends Form
    implements CommandListener
{
    private final HawkMIDlet parent;
    private final Command backCommand;

    private static final String instructions =
        "This is an early prototype of an action game.\n\n" +
        "In this version, all you can do is move around and fire.";


    InstructionsScreen(HawkMIDlet parent)
    {
        super("Instructions");
        this.parent = parent;

        append(new StringItem(null, instructions));

        backCommand = new Command("Back", Command.BACK, 1);
        addCommand(backCommand);
        setCommandListener(this);
    }


    public void commandAction(Command c, Displayable d)
    {
        // There is only one command: Back
        parent.instructionsBack();
    }
}
