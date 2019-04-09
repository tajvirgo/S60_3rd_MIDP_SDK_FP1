/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.m3g.maze3d;


import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

 /**
  * A MIDLet that implements a 3D maze game
  * @version 1.1
  */
 public class MazeMIDlet
  extends MIDlet
{
  /** The 3D MazeCanvas */
  private final MazeCanvas canvas3D;
  /** An image containing a logo */
  private final Image logo;
  /** A displayable screen that shows a menu */
  private MenuList menuList;

  /**
   * Constructor for the MIDLet
   */
  public MazeMIDlet()
  {
	logo = makeImage("/com/nokia/midp/examples/m3g/maze3d/content/logo.png");
	canvas3D = new MazeCanvas(this);
	ErrorScreen.init(logo, Display.getDisplay(this));
  }

  /**
   * Starts the application
   */
  public void startApp()
  {
	Displayable current = Display.getDisplay(this).getCurrent();
	if (current == null)
	{
	  // check that the API is available
	  boolean isApiAvailable =
		(System.getProperty("microedition.m3g.version") != null);
	  menuList = new MenuList(this, isApiAvailable);
	  if (!isApiAvailable)
	  {
		quitApp();
	  }
	  else
	  {
		// Display a splash screen and then the game
		StringBuffer splashText = new StringBuffer("")
		  .append(getAppProperty("MIDlet-Name"));
		Alert splashScreen = new Alert("Maze3D",
		  splashText.toString(),
		  logo,
		  AlertType.INFO);
		splashScreen.setTimeout(3000);

		Display.getDisplay(this).setCurrent(splashScreen, menuList);
	  }
	}
	else
	{
	  // In case the MIDlet has been hidden
	  if (current == canvas3D)
	  {
		canvas3D.start();
	  }
	  Display.getDisplay(this).setCurrent(current);
	}
  }

  /**
   * Pauses the application
   */
  public void pauseApp()
  {
	canvas3D.stop();
  }

  /**
   * Destroys the application
   * @param unconditional not used
   */
  public void destroyApp(boolean unconditional)
  {
	canvas3D.stop();
  }

  /**
   * Gets informed when the canvas itself switches the view
   */
  void viewSwitched()
  {
	menuList.viewSwitched();
  }

  /**
   * Starts a new game
   */
  void newGame()
  {
	canvas3D.stop();
	canvas3D.init();
	Display.getDisplay(this).setCurrent(canvas3D);
  }

  /**
   * Shows the menu thread
   */
  void showMenu()
  {
	canvas3D.stop();
	Display.getDisplay(this).setCurrent(menuList);
  }

  /**
   * Quits the app but first stops the game thread
   */
  void quitApp()
  {
	canvas3D.stop();
	notifyDestroyed();
  }

  /**
   * Shows the Graphics3D properties list
   */
  void show3DProperties()
  {
	Display.getDisplay(this).setCurrent(
	  new Graphics3DProperties(this));
  }

  /**
   * switches the canvas' view
   */
  void switchView()
  {
	canvas3D.switchView();
	canvas3D.start();
	Display.getDisplay(this).setCurrent(canvas3D);
  }

  /**
   * Shows the main canvas
   */
  void showMain()
  {
	canvas3D.start();
	Display.getDisplay(this).setCurrent(canvas3D);
  }

  /**
   * Loads a given image by name
   * @param filename the name of the image file
   * @return the image
   */
  static Image makeImage(String filename)
  {
	Image image = null;

	try
	{
	  image = Image.createImage(filename);
	}
	catch (Exception e)
	{
	  // use a null image instead
	}

	return image;
  }

}

