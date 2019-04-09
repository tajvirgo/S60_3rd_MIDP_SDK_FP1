/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.security.satsa;

import javax.microedition.lcdui.*;

/**
 * NewScreen creates UI for user to enter the message and the password need to
 * be encrypted.
 */
class NewScreen
  extends Form
  implements CommandListener
{

  private final SATSAMIDlet midlet;
  private final Command commandBack;
  private final Command commandSave;
  private int index;
  private TextField messageField;
  private TextField passwordField;

  /**
   * Creates a NewScreen with "Back" and "Save" commands.
   * @param midlet the {@link SATSAMIDlet} object
   */
  NewScreen(SATSAMIDlet midlet)
  {
    super(null);
    commandBack = new Command("Back", Command.BACK, 1);
    commandSave = new Command("Save", Command.ITEM, 1);
    this.midlet = midlet;
    createForm();
  }

  /**
   * Sets the index number foe the message.
   * @param index The index number to be set
   */
  void setIndex(int index)
  {
    this.index = index;
    createForm();
  }

  /**
   * Creates the UI for the user to enter password and messages.
   */
  void createForm()
  {
    deleteAll();
    setTitle("Add new message ");
    messageField = new TextField("Message", "", 80, TextField.ANY);
    passwordField = new TextField("Password", "", 16, TextField.ANY);
    append(messageField);
    append(passwordField);
    addCommand(commandBack);
    addCommand(commandSave);
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
    if (c==commandBack)
    {
      midlet.showMessageList();
    }
    if (c==commandSave)
    {
      midlet.addNewMessage(messageField.getString(), passwordField.getString() );
    }
  }
}
