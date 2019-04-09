/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.fileconnection;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * This is the main class which inits the connection and create the screens.
 */
public class FileConnDemo
    extends MIDlet {
  private FileSelector fileSelector;

  //private MediaPlayer mediaPlayer;
  private AboutInfo aboutInfo;
  private int operationCode = -1;
  private static final int timeout = 5000; // five seconds

  /**
   * Constructor
   */
  public FileConnDemo() {
    // init basic parameters
    fileSelector = new FileSelector(this);
    aboutInfo = new AboutInfo(this);
  }

  /**
   * The midlet start app method implementation.
   */
  public void startApp() {
    Displayable current = Display.getDisplay(this).getCurrent();

    if (current == null) {
      // Checks whether the API is available
      boolean isAPIAvailable = System.getProperty(
          "microedition.io.file.FileConnection.version") != null;
      // shows splash screen
      String text = "File Conn MMAPI Demo      Music Folder =" +
          System.getProperty("fileconn.dir.music");

      if (!isAPIAvailable) {
        text += "\nFile Connection API is not available";
      }
      Alert splashScreen = new Alert(null, text, null, null);
      if (isAPIAvailable) {
        splashScreen.setTimeout(timeout);
        Display.getDisplay(this).setCurrent(splashScreen, fileSelector);
      }
      else {
        Display.getDisplay(this).setCurrent(splashScreen);
      }
    }
    else {
      Display.getDisplay(this).setCurrent(current);
    }
  }

  /**
   * The midlet pause app method implementation.
   */
  public void pauseApp() {
  }

  /**
   * The midlet destroy app method implementation.
   */
  public void destroyApp(boolean unconditional) {
    // stop the commands queue thread
    fileSelector.stop();
    notifyDestroyed();
  }

  /**
   * A method for exiting the app from the file selector.
   */
  void fileSelectorExit() {
    destroyApp(false);
  }

  /*void playMedia(String mediaName)
      {
   mediaPlayer.playMedia(mediaName);
      }*/

  /**
   * A method used to disply the file browser.
   */
  void displayFileBrowser() {
    Display.getDisplay(this).setCurrent(fileSelector);
  }

  /**
   * A method used to display the about info.
   */
  void displayAboutInfo() {
    Display.getDisplay(this).setCurrent(aboutInfo);
  }

  /**
   * A method used to show errors.
   * @param e Exception
   */
  void showError(Exception e) {
    ErrorScreen.showError(e.getMessage(), fileSelector);
  }

  /**
   * A method used to show messages.
   * @param text String
   */
  void showMsg(String text) {
    Alert infoScreen = new Alert(null, text, null, null);
    infoScreen.setTimeout(3000);
    Display.getDisplay(this).setCurrent(infoScreen, fileSelector);
  }

  /**
   * A method load a given image by name
   */
  static Image makeImage(String filename) {
    Image image = null;

    try {
      image = Image.createImage(filename);
    }
    catch (Exception e) {
      // use a null image instead
    }

    return image;
  }

}
