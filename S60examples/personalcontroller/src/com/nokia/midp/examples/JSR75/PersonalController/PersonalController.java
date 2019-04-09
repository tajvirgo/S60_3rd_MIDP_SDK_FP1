/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.JSR75.PersonalController;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

/**

Personal Controller is an example program that demonstrates Personal Information Management (PIM).

*/

public class PersonalController extends MIDlet {

    public void startApp()
    {
        Displayable current = Display.getDisplay(this).getCurrent();
        InitialScreen initialScreen = new InitialScreen(this);
        Display.getDisplay(this).setCurrent(initialScreen);
      }

    protected void pauseApp(){
    }

    protected void destroyApp(boolean unconditional){
    }

    void exitRequested() {
        destroyApp(false);
        notifyDestroyed();
    }
}
