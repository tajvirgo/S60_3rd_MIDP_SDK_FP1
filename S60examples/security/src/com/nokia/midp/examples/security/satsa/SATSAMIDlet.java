/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.security.satsa;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;
import java.security.*;

/**
 * SATSAMIDlet implements a simple set of security API defined in JSR 177.
 */
public class SATSAMIDlet extends MIDlet
{

    private Display display;

    private Codec codec;
    private MessageStore messageStore;
    private ListScreen listScreen;
    private EncryptScreen encryptScreen;
    private PasswordScreen passwordScreen;
    private DecryptScreen decryptScreen;
    private InfoScreen infoScreen;
    private NewScreen newScreen;
    private static final int KEYLEN = 16;

    /**
     * Creates a SATSAMIDlet that provides some security functions.
     */
    public SATSAMIDlet()
    {
      codec = new Codec(this);
    }

    protected void destroyApp(boolean unconditional)
      throws MIDletStateChangeException
    {
      exitMIDlet();
    }

    protected void pauseApp()
    {
    }

    protected void startApp()
      throws MIDletStateChangeException
    {
      if (display == null)
      {
        initMIDlet();
      }
    }

    private void initMIDlet()
    {
      display = Display.getDisplay(this);
      listScreen = new ListScreen(this);
      encryptScreen = new EncryptScreen(this);
      passwordScreen = new PasswordScreen(this);
      decryptScreen = new DecryptScreen(this);
      infoScreen = new InfoScreen();
      newScreen = new NewScreen(this);
      messageStore = new MessageStore(this);
      showMessageList();
    }


    /**
     * Displays the list of the messages.
     */
    void showMessageList()
    {
      messageStore.fillList(listScreen);
      display.setCurrent(listScreen);
    }

    /**
     * Displays the encrypted messages.
     * @param index The index number of the encrypted message
     */
    void showEncryptedMessage(int index)
    {
      encryptScreen.setIndex(index);
      encryptScreen.setMessage(codec.byteToHex( messageStore.getMessage(index+1) ) );
      display.setCurrent(encryptScreen);
    }
    /**
     * Displays the screen for the user to enter the password.
     * @param index The index number for the password
     */
    void showPasswordScreen(int index)
    {
      passwordScreen.setIndex(index);
      display.setCurrent(passwordScreen);
    }
    /**
     * Displays the decrypted message.
     * @param index The index number for the decrypted message
     * @param password The password for the encryption
     */
    void showDecryptedMessage(int index, String password)
    {
      // 128bit (16 characters) key is needed, just fill with blanks if too short
      while (password.length() <KEYLEN )
      {
        password = password.concat(" ");
      }
      decryptScreen.setIndex(index);
      try
      {
        byte decryptedMessage[] = codec.decrypt(password.getBytes(), messageStore.getMessage(index+1));
        decryptScreen.setMessage( new String(decryptedMessage) );
        display.setCurrent(decryptScreen);
      }
      catch (GeneralSecurityException gse)
      {
        showError("Security Exception while decrypting. " + gse.toString());
      }
      catch (Exception e)
      {
        showError("Exception while decrypting. " + e.toString());
      }
    }
    /**
     * Displays error messages.
     * @param messageString The messages to be displayed
     */
    void showError(String messageString)
    {
      infoScreen.showError(messageString, Display.getDisplay(this) );
    }
    /**
     * Deletes the messages.
     * @param index The index number of the message to be deleted
     */
    void deleteMessage(int index)
    {
      messageStore.deleteMessage(index+1);
      messageStore.fillList(listScreen);
    }
    /**
     * Display the new message.
     */
    void showNewMessage()
    {
      display.setCurrent(newScreen);
    }
    /**
     * Adds the new message to the <code>MessageStore<\code>
     * @param message The new message
     * @param password The password to encrypt the new message
     */
    void addNewMessage(String message, String password)
    {
      // 128bit (16 characters) key is needed, just fill with blanks if too short
      while (password.length() <KEYLEN )
      {
        password = password.concat(" ");
      }

      try
      {
        messageStore.addMessage(codec.encrypt(password.getBytes(), message.getBytes() ) );
      }
      catch (GeneralSecurityException gse)
      {
        showError("Exception: " + gse.toString());
      }
      showMessageList();
    }
    /**
     * Closes the midlet.
     */
    void exitMIDlet()
    {
      messageStore.close();
      notifyDestroyed();
    }
}
