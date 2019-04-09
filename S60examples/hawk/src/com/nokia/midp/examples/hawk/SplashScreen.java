/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.hawk;

import javax.microedition.lcdui.*;


/**
 * SplashScreen: Implements Splash Screen for game prototype.
 */
class SplashScreen extends Canvas implements Runnable
{
    private final HawkMIDlet midlet;
    private Image splashImage;
    private volatile boolean dismissed = false;


    SplashScreen(HawkMIDlet midlet) {
        this.midlet = midlet;
        setFullScreenMode(true);
        new Thread(this).start();
    }


    public void run() {
        synchronized(this) {
            try {
                wait(5000L);   // 5 seconds
            }
            catch (InterruptedException e) {
                // can't happen in MIDP: no Thread.interrupt method
            }
        }
        dismiss();
    }


    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        g.setColor(0x00FFFFFF);  // white
        g.fillRect(0, 0, width, height);

        g.setColor(0x00000000);  // black
        g.drawRect(1, 1, width-3, height-3);  // black border one pixel from edge
        g.drawRect(2, 2, width-5, height-5);  // black border two pixels from edge
        g.setColor(0x00FF0000);  // red
        g.drawRect(3, 3, width-7, height-7);  // red border three pixels from edge
        g.drawRect(4, 4, width-9, height-9);  // red border four pixels from edge
        g.setColor(0x00FFFF00);  // yellow
        g.drawRect(5, 5, width-11, height-11);// yellow border five pixels from edge
        g.drawRect(6, 6, width-13, height-13);// yellow border six pixels from edge

        if (getSplashImage((width - 16), (height - 16)) != null) {
            g.drawImage(this.splashImage,
                        width/2,
                        height/2,
                        Graphics.VCENTER | Graphics.HCENTER);
            this.splashImage = null;
            this.midlet.splashScreenPainted();
        }
    }


    private Image getSplashImage(int maxWidth, int maxHeight) {
        if (this.splashImage == null) {
            if ((maxWidth > 270) && (maxHeight > 177)) {
                this.splashImage = HawkMIDlet.createImage("/com/nokia/midp/examples/hawk/res/splash3.png");
            } else if ((maxWidth > 180) && (maxHeight > 118)) {
                this.splashImage = HawkMIDlet.createImage("/com/nokia/midp/examples/hawk/res/splash2.png");
            } else {    // Default to 90H by 59H splash image
                this.splashImage = HawkMIDlet.createImage("/com/nokia/midp/examples/hawk/res/splash.png");
            }
        }
        return this.splashImage;
    }


    public void keyPressed(int keyCode) {
        dismiss();
    }


    private synchronized void dismiss() {
        if (!this.dismissed) {
            this.dismissed = true;
            this.midlet.splashScreenDone();
        }
    }
}
