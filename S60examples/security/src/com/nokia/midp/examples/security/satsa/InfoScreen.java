/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.security.satsa;

import javax.microedition.lcdui.*;

/**
 * An InfoScreen is a screen that shows errors to the user.
 */
class InfoScreen
  extends Alert
{
  /**
   * Creates a new InfoScreen.
   */
  InfoScreen()
  {
    super("InfoScreen");
  }

  /**
   * Displays error messages.
   * @param message The error message
   * @param display The Displayable object
   */
  void showError(String message, Display display)
  {
    setTitle("Error");
    setType(AlertType.ERROR);
    setTimeout(5000);
    setString(message);
    display.setCurrent(this);
  }

}
