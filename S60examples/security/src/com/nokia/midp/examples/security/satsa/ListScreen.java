/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.security.satsa;

import javax.microedition.lcdui.*;

/**
 * ListScreen creates a menu list for the user to naviage to different
 * screens.
 */
class ListScreen
  extends List
  implements CommandListener
{

  private final SATSAMIDlet midlet;
  private final Command commandExit = new Command("Exit", Command.EXIT, 1);
  private final Command commandShow = new Command("Show", Command.ITEM, 1);
  private final Command commandNew = new Command("Create new", Command.ITEM, 2);
  private final Command commandDelete = new Command("Delete this", Command.ITEM, 3);

  /**
   * Creates a new ListScreen with all the menu items.
   * @param midlet the {@link SATSAMIDlet} object
   */
  ListScreen(SATSAMIDlet midlet)
  {
    super("Messages", List.IMPLICIT);
    this.midlet = midlet;
    createList();
  }

  /**
   * Sets the number for each message.
   * @param number The number to be set
   */
  void setNumber(int number)
  {
    deleteAll();
    for(int i=1; i<=number; i++)
    {
      append("Message "+i, null);
    }
  }

  /**
   * Creates the menu items.
   */
  void createList()
  {
    addCommand(commandExit);
    addCommand(commandDelete);
    addCommand(commandNew);
    addCommand(commandShow);
    setCommandListener(this);
  }

  /**
   * Executes the command that the user invokes.
   * @param c the command that the user sent
   * @param d an object that has the capability of being placed
   * on the display
   */
  public void commandAction(Command c, Displayable d)
  {
    if (c==commandExit)
    {
      midlet.exitMIDlet();
    }
    if (c==commandShow)
    {
      midlet.showEncryptedMessage(getSelectedIndex());
    }
    if (c==commandDelete)
    {
      midlet.deleteMessage(getSelectedIndex());
    }
    if (c==commandNew)
    {
      midlet.showNewMessage();
    }

  }
}
