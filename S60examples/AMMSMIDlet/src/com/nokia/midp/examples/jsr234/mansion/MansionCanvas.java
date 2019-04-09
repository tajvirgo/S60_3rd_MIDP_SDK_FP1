
package com.nokia.midp.examples.jsr234.mansion;

import javax.microedition.lcdui.*;


/**
 * Handles canvas and also Spectator.
 */
public class MansionCanvas extends Canvas {
    final Display display;
    final int width, height;
    boolean paused;
    final static int NUM_SOURCES = 4;
    private Source[] sources = new Source[NUM_SOURCES];
    private House house;
    private Walker walker;
    int forcedReverb = -1;
    String msg = "";
    private int msgColor = 0xcc11cc;
 
    public MansionCanvas(Display d) {
		display = d;
		paused = true;
		width = getWidth();
		height = getHeight();
		house = new House();
		
		// init sources:
		sources[0] = new Source(6000, 6500, "/com/nokia/midp/examples/jsr234/mansion/res/largedog16k.wav", house, this);
		sources[1] = new Source(10000, 3500, "/com/nokia/midp/examples/jsr234/mansion/res/budgie_16k.wav", house, this);
		sources[2] = new Source(1100, 6000, "/com/nokia/midp/examples/jsr234/mansion/res/owl_16k.wav", house, this);
		sources[3] = new Source(1150, 6000, "/com/nokia/midp/examples/jsr234/mansion/res/cock_16k.wav", house, this);
	
		for (int i = 0; i<NUM_SOURCES; i++){
		    System.out.println("starting source "+ i);
		    Thread t = new Thread(sources[i]);
		    t.start();
		}
		walker = new Walker();
		msg = "Welcome to Mansion!";
    }

    /**
     * Draws everything.
     */
    protected void paint(Graphics g) {
        int x_min = g.getClipX();
        int y_min = g.getClipY();
        int w = g.getClipWidth();
        int h = g.getClipHeight();

		// Empty the frame 
		g.setColor(0xffffff);
		g.fillRect(x_min, y_min, w, h);
		g.translate(width/2, height/2); // move origin to the center
	
		// Draw sources
		for (int i = 0; i<NUM_SOURCES; i++) {
		    sources[i].draw(g, walker.getX(), walker.getY(), walker.getRot());
		}
	
		// Draw house (walls and doors)
		house.draw(g, walker.getX(), walker.getY(), walker.getRot());
	
		// Draw player (walker) at origin:
		g.setColor(0xdd2222);
		g.fillRect(-4, -1, 9, 2); // shoulders
		g.setColor(0x66660a);
		g.drawLine(0, -3, 0, -3); // nose
		g.setColor(0);
		g.fillArc(-2, -2, 5, 5, 0, 360); // head
		g.translate(-width/2, -height/2); // reset translation
	
		// Draw the frame
		g.setColor(0);
		g.drawRect(0, 0, width-1, height-1);
	
		// Draw message:
        if (msg != null) {
            g.setColor(0xffffff);
            g.setClip(0, height-14, width, height);
            g.fillRect(0, height-20, width-2, 18);
            g.setColor(msgColor);
            g.drawString(msg, 5, height-14, 0);
		    g.setColor(0);
            g.drawRect(0, 0, width-1, height-1);
        }
		occludeSources();
    }//paint()

    /**
     * Starts sources that are in the same room (or space) and stops the rest
     */
    private void occludeSources() {
		int listeningRoom = house.inWhichRoom(walker.getX());
		for (int i = 0; i<NUM_SOURCES; i++) {
		    if(listeningRoom == house.inWhichRoom(sources[i].getX())) {
		    	sources[i].start();
		    } else {
		    	sources[i].stop();
		    }
		}
    }

    public void keyPressed(int keyCode) {
    	keyAction(keyCode);
    }

    public void keyRepeated(int keyCode) {
    	keyAction(keyCode);
    }
    
    private void keyAction(int keyCode) {
		int action = getGameAction(keyCode);	
		switch (action) {
		case LEFT:
		    walker.rotateLeft();
		    break;
		case RIGHT:
		    walker.rotateRight();
		    break;
		case UP:
		    walker.moveForward(house);
		    break;
		case DOWN:
		    walker.moveBackward(house);
		    break;
		case FIRE:
		    forcedReverb += 1;
		    forcedReverb = forcedReverb % House.NUM_REV_PRESETS;
		    walker.forceReverb(forcedReverb);
		    break;
		}
		msg = "" + walker.getRevName();
		/*msg = "X:" + walker.getX() + "Y:" + walker.getY()
		    + "r:" + walker.getRot()*10 + " " + walker.getRevName();*/
		repaint();
    }//keyAction()

    /**
     * Closes the sound sources.
     */
    void destroy() {
		for(int i=0; i<NUM_SOURCES; i++) {
	 	    sources[i].destroy();
	 	}
    }

    boolean isPaused() {
    	return paused;
    }

    void pause() {
		if (!paused) {
		    paused = true;	
		}
		repaint();
    }

    void start() {
		if (paused) {
		    paused = false;
		    display.setCurrent(this);
		}
		repaint();
    }
}