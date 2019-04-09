/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sheepdog;

import javax.microedition.lcdui.*;


/**
 * HighScoreScreen: Implements the high score screen as a Form extension.
 */
class HighScoreScreen
    extends Form
    implements CommandListener
{
    private final SheepdogMIDlet midlet;
    private final Command backCommand;


    HighScoreScreen(SheepdogMIDlet midlet)
    {
        super("High score");
        this.midlet = midlet;

        long bestTime = midlet.getBestTime();
        String text = (bestTime == -1) ? "none yet"
                                       : (Long.toString(bestTime) + "s");
        append(new StringItem("Best time", text));

        backCommand = new Command("Back", Command.BACK, 1);
        addCommand(backCommand);
        setCommandListener(this);
    }


    public void commandAction(Command c, Displayable d)
    {
        midlet.highScoreBack();
    }
}
