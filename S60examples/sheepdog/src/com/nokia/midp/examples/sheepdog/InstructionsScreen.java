/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sheepdog;

import javax.microedition.lcdui.*;


/**
 * InstructionsScreen: Implements the instructions screen for a simple MIDlet game.
 */
class InstructionsScreen
    extends Form
    implements CommandListener
{
    private final SheepdogMIDlet midlet;
    private final Command backCommand;

    private static final String instructions =
        "Herd the sheep into the fold as quickly as you can.\n" +
        "Sheep won't leave the fold once they've entered it.\n" +
        "If they're not behaving, bark using the Fire key!";


    InstructionsScreen(SheepdogMIDlet midlet)
    {
        super("Instructions");
        this.midlet = midlet;

        append(new StringItem(null, instructions));

        backCommand = new Command("Back", Command.BACK, 1);
        addCommand(backCommand);
        setCommandListener(this);
    }


    public void commandAction(Command c, Displayable d)
    {
        midlet.instructionsBack();
    }
}
