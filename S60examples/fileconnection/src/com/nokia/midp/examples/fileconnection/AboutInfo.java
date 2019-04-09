/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.fileconnection;

import javax.microedition.lcdui.*;

/**
 * This class provides an alert that acts as an "about" info box.
 */
public class AboutInfo
    extends Alert
    implements CommandListener {

  private FileConnDemo midlet;
  private Command closeCommand = new Command("Close", Command.SCREEN, 1);

  /**
   * Constructor
   * @param midlet FileConnectionDemo
   */
  public AboutInfo(FileConnDemo midlet) {
    super("File Connection/MM API Demo",
          "The MIDlet browses the file system via JSR-75 " +
          "FileConnection API. Users may save MIDI files to the SDK folder. The MIDlet " +
          "fetches MIDI files and plays them via JSR-135 Mobile Media API ", null,
          AlertType.INFO);

    this.midlet = midlet;
    setTimeout(Alert.FOREVER);
    addCommand(closeCommand);
    setCommandListener(this);
  }

  /**
   * Implements the command listener interface to process command actions.
   * @param c Command
   * @param d Displayable
   */
  public void commandAction(Command c, Displayable d) {
    if (c == closeCommand) {
      midlet.displayFileBrowser();
    }
  }

}
