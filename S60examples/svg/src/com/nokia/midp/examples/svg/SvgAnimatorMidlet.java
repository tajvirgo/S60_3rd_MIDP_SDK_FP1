/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.svg;


import java.io.*;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import javax.microedition.m2g.*;


/**
 * Simple midlet that displays an animation using SVGAnimator,
 * It also demonstrates how SVGEventListener works.
 */
public class SvgAnimatorMidlet extends MIDlet implements CommandListener {

	/**
	 * Constructor.
	 */
    public SvgAnimatorMidlet() {

        // *** load a test svg image
        SVGImage svgImage = null;
    	try {
	        InputStream svgStream = getClass().getResourceAsStream("content3.svg");
	        svgImage = (SVGImage)( SVGImage.createImage( svgStream, null ) );
		} catch ( Exception e ){
      	    e.printStackTrace();
	    }

		// *** setup the SVGAnimator
        svgAnimator = SVGAnimator.createAnimator( svgImage );
        svgAnimator.setSVGEventListener( new MySvgEventListener() );
        Canvas c = (Canvas)(svgAnimator.getTargetComponent());
        Display.getDisplay(this).setCurrent( c );
        c.setCommandListener(this);

        // *** set up the midlet menu
        int hotKey = 0;

        cmPlay = new Command("Play", Command.SCREEN, hotKey++);
        c.addCommand(cmPlay);

        cmPause = new Command("Pause", Command.SCREEN, hotKey++);
        c.addCommand(cmPause);

        cmStop = new Command("Stop", Command.SCREEN, hotKey++);
        c.addCommand(cmStop);

        cmIncrease = new Command("Increase Frame Rate", Command.SCREEN, hotKey++);
        c.addCommand(cmIncrease);

        cmDecrease = new Command("Decrease Frame Rate", Command.SCREEN, hotKey++);
        c.addCommand(cmDecrease);

        cmExit = new Command("Exit", Command.SCREEN, hotKey++);
        c.addCommand(cmExit);
    }


	/**
	 * Required midlet method.
         * Not to start play yet, just present the still image at this point.
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
        } else if ( c == cmPlay ) {
			svgAnimator.play();
        } else if ( c == cmPause ) {
			svgAnimator.pause();
    	} else if ( c == cmStop ) {
    		svgAnimator.stop();
    	} else if ( c == cmIncrease ) {
    		float dt = svgAnimator.getTimeIncrement();
    		dt = dt - 0.01f;
    		if ( dt < 0 )
                      dt = 0f;
    		svgAnimator.setTimeIncrement( dt );

    	} else if ( c == cmDecrease ) {
    		float dt = svgAnimator.getTimeIncrement();
    		dt = dt + 0.01f;
    		svgAnimator.setTimeIncrement( dt );

        }
    }


	/*
	 * Private members
	 */

    private SVGAnimator svgAnimator;

    private Command cmPlay;
    private Command cmPause;
    private Command cmStop;
    private Command cmIncrease;
    private Command cmDecrease;
    private Command cmExit;

}


/**
 * Simple SVGEventListener with no action taken in event handling methods.
 */
class MySvgEventListener implements SVGEventListener {
	public void keyPressed(int keyCode) {
	}

	public void keyReleased(int keyCode) {
	}

	public void pointerPressed(int x, int y) {
	}

	public void pointerReleased(int x, int y) {
	}

	public void hideNotify() {
	}

	public void showNotify() {
	}

	public void sizeChanged(int w, int h) {
	}
}
