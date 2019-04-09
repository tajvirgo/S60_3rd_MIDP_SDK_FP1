/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.fileconnection;

import javax.microedition.lcdui.*;

/**
 * This class provides a alert to serve as an error screen.
 */
class ErrorScreen
    extends Alert {
  private static Image image;
  private static Display display;
  private static ErrorScreen instance = null;

  /**
   * Constructor.
   */
  private ErrorScreen() {
    super("Error");
    setType(AlertType.ERROR);
    setTimeout(5000);
    setImage(image);
  }

  /**
   * Initializes the image and display objects references.
   * @param img Image
   * @param disp Display
   */
  static void init(Image img, Display disp) {
    image = img;
    display = disp;
  }

  /**
   * A method used to display error messages.
   * @param message String
   * @param next Displayable
   */
  static void showError(String message, Displayable next) {
    if (instance == null) {
      instance = new ErrorScreen();
    }
    instance.setTitle("Error");
    instance.setString(message);
    display.setCurrent(instance, next);
  }

}
