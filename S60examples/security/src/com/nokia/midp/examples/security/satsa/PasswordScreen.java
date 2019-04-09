/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.security.satsa;

import javax.microedition.lcdui.*;

/**
 * PasswordScreen allows the user to enter password to decrypt the message.
 */
class PasswordScreen
  extends Form
  implements CommandListener
{
  private final SATSAMIDlet midlet;
  private final Command commandBack;
  private final Command commandDecrypt;
  private int index;
  TextField passwordField;

  /**
   * Creates a new PasswordScreen with "Back" and "Decrypt" commands.
   * @param midlet the {@link SATSAMIDlet} object
   */
  PasswordScreen(SATSAMIDlet midlet)
  {
    super(null);
    commandBack = new Command("Back", Command.BACK, 1);
    commandDecrypt = new Command("Decrypt", Command.ITEM, 1);
    this.midlet = midlet;
    createForm();
  }
  /**
   * Sets the index number for the password.
   * @param index the index number
   */
  void setIndex(int index)
  {
    this.index = index;
    createForm();
  }
  /**
   * Creates the UI for the user to enter password to decrpt.
   */
  void createForm()
  {
    deleteAll();
    setTitle("Enter password to decrypt.");
    passwordField = new TextField("Password", "", 16, TextField.ANY);
    append(passwordField);
    addCommand(commandBack);
    addCommand(commandDecrypt);
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
      midlet.showEncryptedMessage(index);
    }
    if (c==commandDecrypt)
    {
      midlet.showDecryptedMessage(index, passwordField.getString() );
    }
  }
}
