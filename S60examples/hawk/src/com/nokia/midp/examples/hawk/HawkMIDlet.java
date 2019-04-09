/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.hawk;


import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;


/**
 * HawkMIDlet: Implements a simple game prototype that uses the MIDP 2.0 Game Canvas.
 */
public class HawkMIDlet
    extends MIDlet
    implements Runnable
{
    private MenuList menuList;
    private InstructionsScreen instructionsScreen;
    private HawkCanvas hawkCanvas;
    private boolean initDone = false;


    public HawkMIDlet()
    {
    }


    public void startApp()
    {
        Displayable current = Display.getDisplay(this).getCurrent();
        if (current == null)
        {
            // first time we've been called
            Display.getDisplay(this).setCurrent(new SplashScreen(this));
        }
        else
        {
            if (current == hawkCanvas)
            {
                hawkCanvas.start();
            }
            Display.getDisplay(this).setCurrent(current);
        }
    }


    public void pauseApp()
    {
        Displayable current = Display.getDisplay(this).getCurrent();
        if (current == hawkCanvas)
        {
            hawkCanvas.stop();
        }
    }


    public void destroyApp(boolean unconditional)
    {
        if (hawkCanvas != null)
        {
            hawkCanvas.stop();   // kill its animation thread
        }
    }


    private void quit()
    {
        destroyApp(false);
        notifyDestroyed();
    }


    public void run()
    {
        init();
    }


    private synchronized void init()
    {
        if (!initDone)
        {
            menuList = new MenuList(this);
            hawkCanvas = new HawkCanvas(this);
            initDone = true;
        }
    }


    void splashScreenPainted()
    {
        new Thread(this).start();  // start background initialization
    }


    void splashScreenDone()
    {
        init();   // if not already done
        Display.getDisplay(this).setCurrent(menuList);
    }


    void menuListContinue()
    {
        Display.getDisplay(this).setCurrent(hawkCanvas);
        hawkCanvas.start();
    }


    void menuListNewGame()
    {
        hawkCanvas.init();
        Display.getDisplay(this).setCurrent(hawkCanvas);
        hawkCanvas.start();
    }


    void menuListInstructions()
    {
        if (instructionsScreen == null)
        {
            instructionsScreen = new InstructionsScreen(this);
        }
        Display.getDisplay(this).setCurrent(instructionsScreen);
    }


    void menuListQuit()
    {
        quit();
    }


    void hawkCanvasMenu()
    {
        hawkCanvas.stop();
        menuList.setGameActive(true);
        Display.getDisplay(this).setCurrent(menuList);
    }


    void instructionsBack()
    {
        Display.getDisplay(this).setCurrent(menuList);
    }


    // method needed by lots of classes, shared by putting it here
    static Image createImage(String filename)
    {
        Image image = null;
        try
        {
            image = Image.createImage(filename);
        }
        catch (java.io.IOException e)
        {
            // just let return value be null
        }
        return image;
    }
}

