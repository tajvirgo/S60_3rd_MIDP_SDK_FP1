package com.nokia.midp.examples.jsr234.mansion;

import javax.microedition.lcdui.Graphics;


/**
 * Contains the walls and the reverb settings.
 *
 */
public class House {
    public static final int NUM_REV_PRESETS = 10;
    private static final int ALLEY=0,ARENA=1, AUDITORIUM=2,BATHROOM=3,CAVE=4,
	HALLWAY=5,HANGAR=6,LIVINGROOM=7,MOUNTAINS=8,ROOM=9;
    static final String REV_PRESETS[] = {
		"alley", "arena", "auditorium", "bathroom", "cave",
		"hallway", "hangar", "livingroom", "mountains", "room"
    };

    // coordinates and reverb settigns of the rooms:
    private static final int[][] ROOMS =
    {//{Y_MIN,Y_MAX,X_MAX,REVERB_PRESET} 
	{6000,6000,0,MOUNTAINS}, // dummy (sentinel)
	{3000,10000,4000,MOUNTAINS}, //yard
	{6000,7000,8000,HALLWAY},
	{2000,10000,19000,AUDITORIUM},
	{3500,3500,19000,ALLEY} // dummy (sentinel)
    };

    public House (){
	
    }

    /**
     * @return true, if given coordinates are inside of the house.
     */
    public boolean isInside(int x, int y) {
		boolean inside = false;
		final int room = inWhichRoom(x);
		if (y+100 < ROOMS[room][1] && y-100 > ROOMS[room][0])
		    inside = true;
		return inside;
    }
    
    /**
     * Tells in which room is the given x-coordinate in.
     *
     * @return room index
     */
    public int inWhichRoom(int x) {
		int room = ROOMS.length-1;
		for (int i = 0; i<ROOMS.length-1; i++) {
		    if (x > ROOMS[i][2]) {
		    	room = i+1;
		    } else {
		    	break;
		    }
		}
		if (room >= ROOMS.length)
		    System.out.println("bug in House.inWhichRoom()! room=" + room);
		return room;
    }

    /**
     * Gets the reverb preset name for the given room
     */
    public static String presetName(int room) {
    	if(room < ROOMS.length && room >= 0) {
    		return REV_PRESETS[ROOMS[room][3]];
    	} else {
    		return "";
    	}
    }

    /**
     * Gets reverb preset name by index
     */
    public static String presetNameByIndex(int index) {
    	if(index < NUM_REV_PRESETS && index >= 0) {
    		return REV_PRESETS[index];
    	} else {
    		return "";
    	}
    }
     
   /**
     * Draws the house to given Graphics.
     * @param dx x translation in millipixels
     * @param dy y translation in millipixels
     * @param rot rotation in deci-angles
     */
    public void draw(Graphics g, int dx, int dy, int rot) {
    	for (int i=1; i<ROOMS.length; i++) {
		    // door:
		    g.setColor(0xee1111); //brick
		    /*
		      following min/max magic is necessary, because if the door
		      ovelaps east wall, there will be artifacts visible, cause
		      overlaping doesn't seem to be perfect:
		    */
		    g.drawLine
			(Trig.transXS(ROOMS[i-1][2]-dx, 
					Math.min(ROOMS[i][1],ROOMS[i-1][1])-dy, rot),
			 Trig.transYS(ROOMS[i-1][2]-dx,
					Math.min(ROOMS[i][1],ROOMS[i-1][1])-dy, rot),
			 Trig.transXS(ROOMS[i-1][2]-dx,
					Math.max(ROOMS[i][0],ROOMS[i-1][0])-dy, rot),
			 Trig.transYS(ROOMS[i-1][2]-dx,
					Math.max(ROOMS[i][0],ROOMS[i-1][0])-dy, rot));
	
		    g.setColor(0x000000); // black
	
		    //east (norther from door) wall:
		    g.drawLine
			(Trig.transXS(ROOMS[i-1][2]-dx,ROOMS[i][1]-dy,rot),
			 Trig.transYS(ROOMS[i-1][2]-dx,ROOMS[i][1]-dy,rot),
			 Trig.transXS(ROOMS[i-1][2]-dx,ROOMS[i-1][1]-dy,rot),
			 Trig.transYS(ROOMS[i-1][2]-dx,ROOMS[i-1][1]-dy,rot));
		    
		    // east (souther from door) wall:
		    g.drawLine
			(Trig.transXS(ROOMS[i-1][2]-dx,ROOMS[i][0]-dy,rot),
			 Trig.transYS(ROOMS[i-1][2]-dx,ROOMS[i][0]-dy,rot),
			 Trig.transXS(ROOMS[i-1][2]-dx,ROOMS[i-1][0]-dy,rot),
			 Trig.transYS(ROOMS[i-1][2]-dx,ROOMS[i-1][0]-dy,rot));
	
		    //north wall:
		    g.drawLine
			(Trig.transXS(ROOMS[i-1][2]-dx,ROOMS[i][0]-dy,rot),
			 Trig.transYS(ROOMS[i-1][2]-dx,ROOMS[i][0]-dy,rot),
			 Trig.transXS(ROOMS[i][2]-dx,ROOMS[i][0]-dy,rot),
			 Trig.transYS(ROOMS[i][2]-dx,ROOMS[i][0]-dy,rot));
		    
		    //south wall:
		    g.drawLine
			(Trig.transXS(ROOMS[i-1][2]-dx,ROOMS[i][1]-dy,rot),
			 Trig.transYS(ROOMS[i-1][2]-dx,ROOMS[i][1]-dy,rot),
			 Trig.transXS(ROOMS[i][2]-dx,ROOMS[i][1]-dy,rot),
			 Trig.transYS(ROOMS[i][2]-dx,ROOMS[i][1]-dy,rot));
    	}
    }//draw()

}// House
