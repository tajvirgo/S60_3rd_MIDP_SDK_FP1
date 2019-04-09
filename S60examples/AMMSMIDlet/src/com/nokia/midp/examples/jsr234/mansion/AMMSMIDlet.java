
package com.nokia.midp.examples.jsr234.mansion;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * AMMSMIDlet class.
 *
 */
public class AMMSMIDlet extends MIDlet implements CommandListener {

    Display display;
    MansionCanvas canvas;		// The main screen
    private Command exitCommand = new Command("Exit", Command.EXIT, 1);
    private Command toggleCommand = new Command("Start", Command.SCREEN, 1);
    private Command helpCommand = new Command("Help", Command.HELP, 1);
    private Command aboutCommand = new Command("About", Command.HELP, 2);
    private Form helpScreen, aboutScreen;

    /*
     * Create the canvas
     */
    public AMMSMIDlet() {
	display = Display.getDisplay(this);
	canvas = new MansionCanvas(display);
	canvas.addCommand(exitCommand);
	canvas.addCommand(toggleCommand);
	canvas.addCommand(helpCommand);
	canvas.addCommand(aboutCommand);
	canvas.setCommandListener(this);
    }

    public void startApp() throws MIDletStateChangeException {
    	canvas.start();
    }

    public void pauseApp() {
    	canvas.pause();
    }

    public void destroyApp(boolean unconditional) throws MIDletStateChangeException {
    	canvas.destroy();
    }

    /*
     * Respond to a command issued on the Canvas.
     */
    public void commandAction(Command c, Displayable s) {
		if (c == toggleCommand) {
		    if (canvas.isPaused()) canvas.start();
		    else canvas.pause();
		} 
		else if (c == helpCommand) {
		    canvas.pause();
		    showHelp();
		} 
		else if (c == exitCommand) {
		    try {
				destroyApp(false);
				notifyDestroyed();
			} catch (MIDletStateChangeException ex) {
		    }
		} 
		else if (c == aboutCommand) {
		    canvas.pause();
		    showAbout();
		}
    }

    /*
     * Put up the help screen. Create it if necessary.
     * Add only the Resume command.
     */
    void showHelp() {
		if (helpScreen == null) {
		    helpScreen = new Form("Walking Help");
		    helpScreen.append("^ = walk forward\n");
		    helpScreen.append("v = walk backwards\n");
		    helpScreen.append("< = turn left\n");
		    helpScreen.append("> = turn right\n");
		    helpScreen.append("fire = change reverb manually\n");
		}
		helpScreen.addCommand(toggleCommand);
		helpScreen.setCommandListener(this);
		display.setCurrent(helpScreen);
    }

    /*
     * Put up the about screen. Create it if necessary.
     * Add only the Resume command.
     */
    void showAbout() {
		if (aboutScreen == null) {
		    aboutScreen = new Form("About AMMS Mansion demo");
		    aboutScreen.append("This MIDlet demonstrates the 3D audio capabilities of AMMS API.\n");
		    aboutScreen.append("\n");
		    aboutScreen.append("Copyright (c) 2006 Nokia. All rights reserved.\n");
		    aboutScreen.append("\n");
		    aboutScreen.append("\n");
		}
		aboutScreen.addCommand(toggleCommand);
		aboutScreen.setCommandListener(this);
		display.setCurrent(aboutScreen);
    }
}
