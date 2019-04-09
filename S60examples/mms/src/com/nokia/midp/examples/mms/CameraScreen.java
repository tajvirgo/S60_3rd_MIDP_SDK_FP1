/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.mms;

import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import java.io.IOException;

/**
 * CameraScreen displays camera video capture screen.
 */
class CameraScreen
extends Canvas
implements CommandListener
{
    private final MMSMIDlet midlet;
    private final Command exitCommand;
    private Player player = null;
    private Command captureCommand = null;
    private VideoControl videoControl = null;

    private final String snapshot_header = "encoding=jpeg";
    private final String player_schema = "capture://video";
    private final String exit_command = "Exit";
    private final String capture_command = "Capture";
    private final String videoControlLabel = "VideoControl";

    private boolean active = false;
    /**
     * Constructs a new CameraScreen object with given MMSMIDlet
     * @param midlet the given MMSMIDlet
     */
    CameraScreen(MMSMIDlet midlet)
    {
        this.midlet = midlet;

        exitCommand = new Command(exit_command, Command.EXIT, 1);
        addCommand(exitCommand);
        setCommandListener(this);

        try
        {
            player = Manager.createPlayer(player_schema);
            player.realize();

            // Get VideoControl for the viewfinder
            videoControl = (VideoControl)(player.getControl(videoControlLabel));
            if (videoControl == null)
            {
                discardPlayer();
                midlet.showError("Cannot get the video control.\n"
                        +"Capture may not be supported.");
            }
            else
            {
                videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
                int canvasWidth = getWidth();
                int canvasHeight = getHeight();
                int displayWidth = videoControl.getDisplayWidth();
                int displayHeight = videoControl.getDisplayHeight();
                int x = (canvasWidth - displayWidth) / 2;
                int y = (canvasHeight - displayHeight) / 2;
                videoControl.setDisplayLocation(x, y);
                captureCommand = new Command(capture_command, Command.SCREEN, 1);
                addCommand(captureCommand);
            }
        }
        catch (IOException ioe)
        {
            discardPlayer();
            midlet.showError("IOException: " + ioe.getMessage());
        }
        catch (MediaException me)
        {
            discardPlayer();
            midlet.showError("MediaException: " + me.getMessage());
        }
        catch (SecurityException se)
        {
            discardPlayer();
            midlet.showError("SecurityException: " + se.getMessage());
        }
    }


    private void discardPlayer()
    {
        if (player != null)
        {
            player.close();
            player = null;
        }
        videoControl = null;
    }

    /**
     * Fills the area with certain color.
     * @param g the given graphics object
     */
    public void paint(Graphics g)
    {
        g.setColor(0x00000000); // black background
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Starts the Player as soon as possible.
     */
    synchronized void start()
    {
        if ((player != null) && !active)
        {
            try
            {
                player.start();
                videoControl.setVisible(true);
            }
            catch (MediaException me)
            {
                midlet.showError("MediaException: " + me.getMessage());
            }
            catch (SecurityException se)
            {
                midlet.showError("SecurityException: " + se.getMessage());
            }
            active = true;
        }
    }

    /**
     * Stops the player.
     */
    synchronized void stop()
    {
        if ((player != null) && active)
        {
            try
            {
                videoControl.setVisible(false);
                player.stop();
            }
            catch (MediaException me)
            {
                midlet.showError("MediaException: " + me.getMessage());
            }
            active = false;
        }
    }

    /**
     * Invokes certain methods as responding to the exit and capture events that
     * have occurred.
     * @param c a Command object identifying the command
     * @param d  the Displayable on which this event has occurred
     */
    public void commandAction(Command c, Displayable d)
    {
        if (c == exitCommand)
        {
            midlet.exitApplication();
        }
        else if (c == captureCommand)
        {
            captureImage();
        }
    }

    /**
     * Called when a key is released.
     * @param keyCode the key code of the key that was released
     */
    public void keyPressed(int keyCode)
    {
        if (getGameAction(keyCode) == FIRE)
        {
            captureImage();
        }
    }


    private void captureImage()
    {
        if (player != null)
        {
            new Thread()
            {
                public void run()
                {
                    try
                    {
                        byte[] jpgImage = videoControl.getSnapshot(snapshot_header);
                        midlet.imageCaptured(jpgImage);
                    }
                    catch (MediaException me)
                    {
                        midlet.showError("MediaException: " + me.getMessage());
                    }
                }
            }.start();
        }
    }
}

