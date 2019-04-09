/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.m3g.maze3d;

import javax.microedition.lcdui.*;

/**
 * A utility class to display an error message screen.
 */
class ErrorScreen
  extends Alert
{
  /** Image to be displayed with error screen */
  private static Image image;
  /** Display used with error screen */
  private static Display display;
  /** Error screen to be displayed */
  private static ErrorScreen instance = null;

  /** Sets up ErrorScreen */
  private ErrorScreen()
  {
	super("Error");
	setType(AlertType.ERROR);
	setTimeout(5000);
	setImage(image);
  }

  /**
   * Initializes the image and display
   *
   * @param img is the image to be displayed
   * @param disp is the display object
   * */
  static void init(Image img, Display disp)
  {
	image = img;
	display = disp;
  }


  /**
   * Method used to show the error screen
   * @param message is the string to be displayed
   * @param next is the next Displayable object
   * */
  static void showError(String message, Displayable next)
  {
	if (instance == null)
	{
	  instance = new ErrorScreen();
	}
	instance.setTitle("Error");
	instance.setString(message);
	display.setCurrent(instance, next);
  }

}

