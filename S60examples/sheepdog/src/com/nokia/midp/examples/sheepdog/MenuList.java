/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sheepdog;

import javax.microedition.lcdui.*;


/**
 * MenuList: Implements the command menu for a simple game MIDlet.
 */
class MenuList
    extends List
    implements CommandListener
{
    private SheepdogMIDlet midlet;
    private Command exitCommand;
    private boolean gameActive = false;


    MenuList(SheepdogMIDlet midlet)
    {
        super("Sheepdog", List.IMPLICIT);
        this.midlet = midlet;

        append("New game", null);
        append("High score", null);
        append("Instructions", null);

        exitCommand = new Command("Exit", Command.EXIT, 1);
        addCommand(exitCommand);

        setCommandListener(this);
    }


    void setGameActive(boolean active)
    {
        if (active && !gameActive)
        {
            gameActive = true;
            insert(0, "Continue", null);
        }
        else if (!active && gameActive)
        {
            gameActive = false;
            delete(0);
        }
    }


    public void commandAction(Command c, Displayable d)
    {
        if (c == List.SELECT_COMMAND)
        {
            int index = getSelectedIndex();
            if (index != -1)  // should never be -1
            {
                if (!gameActive)
                {
                    index++;
                }
                switch (index)
                {
                case 0:   // Continue
                    midlet.menuListContinue();
                    break;
                case 1:   // New game
                    midlet.menuListNewGame();
                    break;
                case 2:   // High score
                    midlet.menuListHighScore();
                    break;
                case 3:
                    midlet.menuListInstructions();
                    break;
                default:
                    // can't happen
                    break;
                }
            }
        }
        else if (c == exitCommand)
        {
            midlet.menuListQuit();
        }
    }
}
