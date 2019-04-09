package com.nokia.midp.examples.jsr234.mansion;

import javax.microedition.media.*;
import javax.microedition.amms.Spectator;
import javax.microedition.amms.GlobalManager;
import javax.microedition.amms.control.audio3d.OrientationControl;
import javax.microedition.amms.control.audio3d.LocationControl;
import javax.microedition.amms.control.audioeffect.ReverbControl;


/**
 * Spectator functionality.
 *
 */
public class Walker {
    private static final int STEP = 200; //step size in mm
    private Spectator spectator;
    private LocationControl specLocC; // locC of spectator
    private OrientationControl specOriC; // OriC of spectator
    private ReverbControl reverb;
    private String revName = "none";
    private int specX = 1000, specY = 6000; // spectator location
    private int specRot = 0; // spectator rotation
    // for checking the needs for a reverb settings updates:
    private int prevRoom = -999; 

    public Walker (){
    	initSpectator();
    }

    /**
     * 	Init spectator and get controls for it.
     */
    private void initSpectator() {   
	    try {
	    	spectator = GlobalManager.getSpectator();
	    } catch (MediaException me) {
	    	System.out.println(me);
	    }
    
	    if(spectator != null) {
	    	if ((specLocC =
	    		(LocationControl)spectator.getControl("javax.microedition.amms.control.audio3d.LocationControl"))
	    		!= null) {
	    			specLocC.setCartesian(specX, 0, specY); 
	    	} else {
	    		System.out.println("Spectator couldn't get LocationControl.");
	    }
      
	    if ((specOriC =
	    	(OrientationControl)
	    	spectator.getControl("javax.microedition.amms.control.audio3d.OrientationControl"))
	    	!= null) {
	    		specOriC.setOrientation(specRot*10, 0, 0); 
	    } else {
	    	System.out.println("Spectator couldn't get OrientationControl.");
	    }
      
	    try {
	    	if ((reverb =
	    		(ReverbControl)GlobalManager.getControl("javax.microedition.amms.control.audioeffect.ReverbControl"))
	    		!= null) {
	    			try {
	    				reverb.setPreset("mountains");
	    			}
	    			catch (IllegalArgumentException iae) {
	    				System.out.println(iae);
	    			}
	    			reverb.setEnabled(true);
	    			if(!(reverb.isEnabled()))
	    				System.out.println("MIDlet: enabling reverb failed.");
	    	} else {
	    		System.out.println("Spectator couldn't get Reverb.");
	    	}
      }
      catch (IllegalArgumentException iae) {
        System.out.println(iae);
      }
    }
  }//initSpectator()

    public void rotateLeft() {
		// rotate spectator left 
		specRot = specRot - 1;
		if (specRot == -1)
		    specRot = 35;
		updateSpecRot(specRot);
    }

    public void rotateRight() {
		// rotate spectator right
		specRot = (specRot + 1)%36;
		updateSpecRot(specRot);
    }
    
    public void moveForward(House house) {
		int newSpecX = specX + Trig.transX(0, STEP, specRot);
		int newSpecY = specY + Trig.transY(0, STEP, specRot);
		updateSpecLoc(newSpecX, newSpecY, house);
    }

    public void moveBackward(House house) {
		int newSpecX = specX + Trig.transX(0, -STEP, specRot);
		int newSpecY = specY + Trig.transY(0, -STEP, specRot);	
		updateSpecLoc(newSpecX, newSpecY, house);
    }

    public int getX() {
    	return specX;
    }

    public int getY() {
    	return specY;
    }

    public int getRot() {
    	return specRot;
    }

    private void updateSpecRot(int newRot) {
		newRot += 18;
		if(specOriC != null) specOriC.setOrientation(newRot*10, 0, 0);
    }

    /**
     * Updates location if collision to the wall doesn't prevent.
     * Updates possible new reverb settings as well, if the spy moves to a new room.
     */
    private void updateSpecLoc(int newX, int newY, House house) {
    	if (house.isInside(newX, newY)) {
		    specX = newX;
		    specY = newY;
		    if(specLocC != null)
			specLocC.setCartesian(newX, 0, newY);
		    
		    // update reverb settings if necessary
		    int currentRoom = house.inWhichRoom(specX);
		    if (currentRoom != prevRoom) {
				prevRoom = currentRoom;
			    if(reverb != null) {
					reverb.setPreset(House.presetName(currentRoom));
					revName = House.presetName(currentRoom);
					System.out.println("New reverb: " + House.presetName(currentRoom));
			    }
		    }
    	}
    }

    /**
     * Forces the Reverb of Spectator independently of the room.
     */
    protected void forceReverb(int index) {
	    revName = House.presetNameByIndex(index);
	    reverb.setPreset(revName);
	    System.out.println("Manually forced reverb: " + House.presetNameByIndex(index));
    }

    /**
     * Gets the current reverb preset's name.
     */
    protected String getRevName() {
    	return revName;
    }
}
