/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.security.satsa;

import javax.microedition.lcdui.*;

/**
 * DecryptScreen provides a UI to enter decrption information and decrypted
 * messages.
 */
class DecryptScreen
  extends Form
  implements CommandListener
{
  private final SATSAMIDlet midlet;
  private final Command commandBack;
  private int index;
  private String decryptedMessage;
  /**
   * Creates a new DecrptScreen object with "back" command and the form
   * @param midlet the {@link SATSAMIDlet} object
   */
  DecryptScreen(SATSAMIDlet midlet)
  {
    super(null);
    commandBack = new Command("Back", Command.BACK, 1);
    this.midlet = midlet;
    createForm();
  }

  /**
   * Sets the index number for the decrypted messages.
   * @param index the index number
   */
  void setIndex(int index)
  {
    this.index = index;
  }
 /**
  * Sets the decrypted messages.
  * @param decryptedMessage the decrypted message
  */
  void setMessage(String decryptedMessage)
  {
    this.decryptedMessage = decryptedMessage.trim();
    createForm();
  }
  /**
   * Creates the screen to display the message.
   */
  void createForm()
  {
    deleteAll();
    setTitle("Decrypted Message");
    StringItem messageItem = new StringItem(null, decryptedMessage);
    append(messageItem);
    addCommand(commandBack);
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
  }
}
