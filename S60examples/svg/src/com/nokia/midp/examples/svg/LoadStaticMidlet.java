/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.svg;


import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;


/**
 * Simple midlet that displays a static SVG file.
 */
public class LoadStaticMidlet extends MIDlet implements CommandListener {

	/**
	 * Constructor.
	 */
    public LoadStaticMidlet() {
    	
        // *** create the SVG canvas
        svgCanvas = new SvgCanvas2 (false);
        svgCanvas.setCommandListener(this);
        
		// *** grab a reference to the display
        display = Display.getDisplay(this);
        display.setCurrent(svgCanvas);
        
        // *** set up the midlet menu
        int hotKey = 0;
        
        cmZoomIn = new Command("Zoom In", Command.SCREEN, hotKey++);
        svgCanvas.addCommand(cmZoomIn);
        
        cmZoomOut = new Command("Zoom Out", Command.SCREEN, hotKey++);
        svgCanvas.addCommand(cmZoomOut);

        cmRotateIn = new Command("Rotate Counter", Command.SCREEN, hotKey++);
        svgCanvas.addCommand(cmRotateIn);

        cmRotateOut = new Command("Rotate Clockwise", Command.SCREEN, hotKey++);
        svgCanvas.addCommand(cmRotateOut);
        
        cmRestoreView = new Command("Restore View", Command.SCREEN, hotKey++);
        svgCanvas.addCommand(cmRestoreView);
        
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
        } else if ( c == cmZoomIn ) {
            svgCanvas.zoomIn();
        } else if ( c == cmZoomOut ) {
            svgCanvas.zoomOut();
        } else if ( c == cmRotateIn ) {
            svgCanvas.rotateIn();
        } else if ( c == cmRotateOut ) {
            svgCanvas.rotateOut();
        } else if ( c == cmRestoreView ) {
            svgCanvas.restoreView();
        }	
    }
    

	/*
	 * Private members
	 */

    private Display display;
    private SvgCanvas2 svgCanvas;

    private Command cmZoomIn;
    private Command cmZoomOut;
    private Command cmRotateIn;
    private Command cmRotateOut;
    private Command cmRestoreView;
    private Command cmExit;

}
