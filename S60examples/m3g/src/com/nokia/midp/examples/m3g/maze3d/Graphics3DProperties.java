/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.m3g.maze3d;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.lcdui.*;
import javax.microedition.m3g.*;

/**
 * A class that contains a list of 3D graphics properties.
 */
class Graphics3DProperties
  extends List
  implements CommandListener
{
  /** A reference to the MazeMIDlet */
  private final MazeMIDlet midlet;

  /**
   * Constructs the Graphics3DProperties and initializes properties and keys
   * @param midlet the client MazeMIDlet
   */
  Graphics3DProperties(MazeMIDlet midlet)
  {
	super("Graphics 3D Properties", List.IMPLICIT);

	this.midlet = midlet;

	Hashtable props = Graphics3D.getProperties();
	Enumeration propKeys = props.keys();
	while (propKeys.hasMoreElements())
	{
	  Object key = propKeys.nextElement();
	  append(key.toString() + ": " + props.get(key).toString(), null);
	}
	// some entries are too long
	setFitPolicy(List.TEXT_WRAP_ON);

	addCommand(new Command("Back", Command.BACK, 1));
	setCommandListener(this);
  }

  /**
   * This method invokes the MIDlet's showMenu method.
   * @param command not used
   * @param displayable not used
   */
  public void commandAction(Command command, Displayable displayable)
  {
	midlet.showMenu();
  }

}

