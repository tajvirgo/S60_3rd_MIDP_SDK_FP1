/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.svg;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;


/**
 * Simple midlet that displays a static SVG file.
 */
public class HelloWorldMidlet extends MIDlet implements CommandListener {

	/**
	 * Constructor.
	 */
    public HelloWorldMidlet() {
    	
        // *** create the SVG canvas
        svgCanvas = new SvgCanvas1 (false);
        svgCanvas.setCommandListener(this);
        
		// *** grab a reference to the display
        display = Display.getDisplay(this);
        display.setCurrent(svgCanvas);
        
        // *** set up the midlet menu
        int hotKey = 0;
        
        cmExit = new Command("Exit", Command.SCREEN, hotKey++);
        svgCanvas.addCommand(cmExit);
    }


	/**
	 * Required midlet method.
	 */
    public void startApp() {
    }


	/**
	 * Required midlet method.
	 */
    public void pauseApp() {
    }

    
	/**
	 * Required midlet method.
	 */
    public void destroyApp(boolean unconditional) {
    }


	/**
	 * Handle the various menu actions.
	 */
    public void commandAction(Command c, Displayable s) {
        if (c == cmExit) {
            destroyApp(false);
            notifyDestroyed();
        }	
    }
    

	/*
	 * Private members
	 */

    private Display display;
    private SvgCanvas1 svgCanvas;

    private Command cmExit;

}
