/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.security.satsa;

import javax.microedition.lcdui.*;

/**
 * EncryptScreen provides a UI to enter encryption information and encrypted
 * messages.
 */
class EncryptScreen
  extends Form
  implements CommandListener
{

  private final SATSAMIDlet midlet;
  private final Command commandBack = new Command("Back", Command.BACK, 1);
  private final Command commandShowDecrypted = new Command("Decrypt", Command.ITEM, 1);
  private int index;
  private String encryptedMessage;
  /**
   * Creates a new DecrptScreen with the form.
   * @param midlet the {@link SATSAMIDlet} object
   */
  EncryptScreen(SATSAMIDlet midlet)
  {
    super(null);
    this.midlet = midlet;
    createForm();
  }
 /**
  * Sets the index number for the encrypted messages.
  * @param index the index number
  */
  public void setIndex(int index)
  {
    this.index = index;
  }
 /**
  * Sets the decrypted messages.
  * @param encryptedMessage the encrypted message
  */
  void setMessage(String encryptedMessage)
  {
    this.encryptedMessage = encryptedMessage;
    createForm();
  }
  /**
   * Creates the screen to display the message
   */
  void createForm()
  {
    deleteAll();
    setTitle("Encrypted Message");
    StringItem messageItem = new StringItem(null, encryptedMessage );
    append(messageItem);
    addCommand(commandBack);
    addCommand(commandShowDecrypted);
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
    if (c==commandShowDecrypted)
    {
      midlet.showPasswordScreen(index);
    }
  }
}
