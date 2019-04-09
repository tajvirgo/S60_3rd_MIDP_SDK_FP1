/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.m3g.maze3d;

import javax.microedition.lcdui.*;

/**
 * A Screen containing list of choices.
 */
class MenuList
  extends List
  implements CommandListener
{
  private final MazeMIDlet midlet;
  private boolean gameStarted = false;

  /**
   * Constructor for the menulist
   * @param midlet a reference to the MazeMIDLet
   * @param apiAvailable an indicator if the api is available
   */
  MenuList(MazeMIDlet midlet, boolean apiAvailable)
  {
	super("Maze3D", List.IMPLICIT);

	this.midlet = midlet;

	setFitPolicy(List.TEXT_WRAP_ON);

	// in case the 3D API is not available
	if (apiAvailable)
	{
	  append("New Game", null);
	  append("3D Properties", null);
	}
	// add an exit command
	addCommand(new Command("Exit", Command.EXIT, 0));
	setCommandListener(this);
  }

  /**
   * Indicates that a command event has occurred on Displayable d
   * @param command the command event
   * @param displayable the displayable that had the command event
   */
  public void commandAction(Command command, Displayable displayable)
  {
	if (command == List.SELECT_COMMAND)
	{
	  int index = getSelectedIndex();
	  switch (index)
	  {
		case 0:
		  // add more commands the first time the game runs
		  if (!gameStarted)
		  {
			insert(1, "Top view", null);
			addCommand(new Command("Back", Command.BACK, 0));
			gameStarted = true;
		  }
                  //  new game
		  midlet.newGame();

		  break;
		case 1:
		  if (gameStarted)
		  {
			midlet.switchView();
		  }
		  else
		  {
			midlet.show3DProperties();
		  }
		  break;
		case 2:
		  midlet.show3DProperties();
		  break;
	  }
	}
	else if (command.getCommandType() == Command.BACK)
	{
	  midlet.showMain();
	}
	else if (command.getCommandType() == Command.EXIT)
	{
	  midlet.quitApp();
	}
  }


  /**
   * Switches the labels for normal/top view
   */
  void viewSwitched()
  {
	// switch the labels for normal/top view
	if (getString(1).equals("Top view"))
	{
	  set(1, "Normal view", null);
	}
	else
	{
	  set(1, "Top view", null);
	}
  }
}

