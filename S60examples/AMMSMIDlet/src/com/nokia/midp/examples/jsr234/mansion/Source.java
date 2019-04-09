package com.nokia.midp.examples.jsr234.mansion;

import javax.microedition.lcdui.Graphics;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import java.io.InputStream;
import java.io.IOException;
import javax.microedition.amms.control.audio3d.LocationControl;
import javax.microedition.amms.*;

import java.util.Random;
import javax.microedition.lcdui.Canvas;


/**
 * Sound source with graphics, too
 */
public class Source implements Runnable {
    final static int BALL_RADIUS = 5;
	final static int BALL_DIAM = BALL_RADIUS*2;
    private int x = 0, y = 0; //sound source's location
    private Player p;
    private VolumeControl volC;
    private LocationControl locC;
    private SoundSource3D ss3D;

    /** keeps the mover thread active until true*/
    protected boolean closed = false;

    // needed for collision detection in random movements:
    House house = null; 
    Canvas canvas = null; // for repaint after movement

    static Random random = new Random();
    private static int yellow = 0xcccc00;

    public Source(int x, int y, String fileName, House house, Canvas canvas) {
		this.x=x;
		this.y=y;
		this.house = house;
		this.canvas = canvas;
	
		InputStream is = getClass().getResourceAsStream(fileName);
		if (is == null)
			System.out.println("MIDlet: error creating InputStream of" + fileName);
		try {
			System.out.println("MIDlet: Creating Player " + fileName);
			p = Manager.createPlayer(is, "audio/X-wav");	  
			if (p == null)
				System.out.println("error creating Player of" + fileName);
			p.realize();
			p.setLoopCount(-1); //indefinetely
			System.out.println("MIDlet: realized: " + fileName);
		} catch (IOException e) {
			System.out.println(e);
		} catch (MediaException e) {
			System.out.println(e);
		}
		volC=(VolumeControl)p.getControl("VolumeControl");
		if (volC!=null) {
			System.out.println("MIDlet: got VolumeControl");
			int v = volC.setLevel(100);
			System.out.println("MIDlet: set Volume to "+v);
		}
	       
		try {
			ss3D = GlobalManager.createSoundSource3D();        
			if (ss3D != null) {
		        System.out.println("MIDlet: got a new SS3D");
		        ss3D.addPlayer(p);
		        System.out.println("MIDlet: Player added to a SS3D");
		        locC=(LocationControl)ss3D.getControl("javax.microedition.amms.control.audio3d.LocationControl");
		        if (locC != null) {
		        	System.out.println("MIDlet: got LocationControl from a SS3D");
		        	locC.setCartesian(x, 0, y);
		        } else {
		        	System.out.println("LocationControl not available for " + fileName);
		        	ss3D.removePlayer(p);
		        }
		        p.prefetch();
			}
	    } catch (MediaException e) {
	    	System.out.println(e);
	    }	
    }

    /**
     * Deinits players
     */
    protected void destroy() {
		closed = true;
		if(p != null) { 
		    p.close();
		}
    }

    public final void start() {
    	try {
    		p.start();
    	} catch (MediaException e) {
		    System.out.println(e);
		} catch (IllegalStateException ise) {
			    //synchronization problems with close.
		}
   }

    public final void stop() {
    	if(p.getState() == Player.STARTED) {
    		Thread t = new Thread () {
    			public void run() {
    				try {
    					p.stop();
    				} catch (MediaException e) {
    					System.out.println(e);
    				} catch (IllegalStateException ise) {
    					//synchronization problems with close.
    				}
    			}
    		};
	    t.start();
    	}
    }

    public final void setLocation(int x, int y) {
		this.x=x;
		this.y=y;
		if (locC != null) { 
			locC.setCartesian(x, 0, y);
		} 
    }

    public final int getX() {
    	return x;
    }
    
    final int getY() { //package visibility for FastSource
    	return y;
    }

    public void run() {
		int tempX = 0;
		int tempY = 0;
		while(!closed) {
		    tempX = x + (random.nextInt() & 256) -128;
		    tempY = y + (random.nextInt() & 256) -128;
		    if (house.isInside(tempX, tempY)) {
				setLocation(tempX, tempY);
				canvas.repaint();
		    }
		    try {
		    	Thread.sleep(250);
		    } catch (InterruptedException e) {}
		}
    }

    /**
     * Draws the sound source to given Graphics.
     * @param dx x translation in millipixels
     * @param dy y translation in millipixels
     * @param rot rotation in deci-angles
     */
    public void draw(Graphics g, int dx, int dy, int rot) {
		int newX = Trig.transXS(x-dx, y-dy, rot) ;//+g.getClipWidth()/2;
		int newY = Trig.transYS(x-dx, y-dy, rot) ;//+g.getClipHeight()/2;
		drawFace(g, newX, newY);   
    }

    /**
     * Draws a face to given graphics
     */
    void drawFace(Graphics g, int x, int y) { 
		// Draw ball:
		g.setColor(yellow);
		g.fillArc(x-BALL_RADIUS, y-BALL_RADIUS, BALL_DIAM, BALL_DIAM, 0, 360);
		// Draw eyes:
		g.setColor(0);
		g.fillArc(x-2, y-2, 2, 2, 0, 360);
		g.fillArc(x+2, y-2, 2, 2, 0, 360);
		// mouth:
		g.drawArc(x-BALL_RADIUS+1, y-BALL_RADIUS+1, BALL_DIAM-3, BALL_DIAM-3, 200, 140);
    }
}
