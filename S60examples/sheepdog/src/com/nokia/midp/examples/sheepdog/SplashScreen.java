/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.sheepdog;

import javax.microedition.lcdui.*;


/**
 * SplashScreen: Implements the splash screen for a simple MIDlet game.
 */
class SplashScreen
    extends Canvas
    implements Runnable
{
    private final SheepdogMIDlet midlet;
    private Image splashImage;
    private volatile boolean dismissed = false;


    SplashScreen(SheepdogMIDlet midlet)
    {
        this.midlet = midlet;
        setFullScreenMode(true);
        splashImage = SheepdogMIDlet.createImage("/com/nokia/midp/examples/sheepdog/res/splash.png");
        new Thread(this).start();
    }


    public void run()
    {
        synchronized(this)
        {
            try
            {
                wait(3000L);   // 3 seconds
            }
            catch (InterruptedException e)
            {
                // can't happen in MIDP: no Thread.interrupt method
            }
            dismiss();
        }
    }


    public void paint(Graphics g)
    {
        int width = getWidth();
        int height = getHeight();
        g.setColor(0x00FFFFFF);  // white
        g.fillRect(0, 0, width, height);

        g.setColor(0x00FF0000);  // red
        g.drawRect(1, 1, width-3, height-3);  // red border one pixel from edge

        if (splashImage != null)
        {
            g.drawImage(splashImage,
                        width/2,
                        height/2,
                        Graphics.VCENTER | Graphics.HCENTER);
            splashImage = null;
            midlet.splashScreenPainted();
        }
    }


    public synchronized void keyPressed(int keyCode)
    {
        dismiss();
    }


    private void dismiss()
    {
        if (!dismissed)
        {
            dismissed = true;
            midlet.splashScreenDone();
        }
    }
}
