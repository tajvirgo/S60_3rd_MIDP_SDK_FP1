/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.location.golf;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import javax.microedition.rms.*;
import java.util.*;
import java.io.*;

/**
 * This class illustrates the implementation of a simple MIDlet to demonstrate
 * the use of JSR-179 the location API.
 * <p>
 * This class extends the class javax.microedition.midlet.MIDlet. It
 * creates and maintains references to a LocationScreen object and a
 * HoleSelector object.
 * <p>
 * Note that the class has no defined constructor.
 */
public class GolfHelperMIDlet extends MIDlet {

    /** Displays the message on the screen. */
    private LocationScreen locationScreen;

    /** Allows the user to edit the select the golf hole they are playing on. */
    private HoleSelector holeSelector;

    /** A generic way of indicating whether startApp() has previously been called. */
    private Display display;

    /**
     * Creates an instance of LocationScreen if one has not already been
     * created and tells the framework to set this instance
     * as the current screen.
     */
    public void startApp() {

      Displayable current = Display.getDisplay(this).getCurrent();

      String message=null;

      // Check the system properties to see that location API is
      // supported
      if (!(System.getProperty("microedition.location.version").equalsIgnoreCase("1.0")))
      {
          // API not present display alert
          message = "Location API not present!";
          Alert splashScreen = new Alert(null, message, null, AlertType.INFO);
          splashScreen.setTimeout(Alert.FOREVER);
          Display.getDisplay(this).setCurrent(splashScreen);
      }
      else
      {
        try {

          holeSelector = new HoleSelector(this);
          holeSelector.initGolfCourseHoles();

          Item[] images = new Item[] {
          new ImageItem("How far is the hole?",
                    Image.createImage("/com/nokia/midp/examples/location/golf/res/splash.jpg"),
                    Item.LAYOUT_CENTER,
                    "How far is the hole?")};

          Form f = new Form(" ", images);

          Display.getDisplay(this).setCurrent(f);
          Thread.sleep(3000);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }

        if (display == null) {
          // First time we've been called.
          display = Display.getDisplay(this);
          locationScreen = new LocationScreen(this, "\nUse Options\nSelect Current Hole\n ");
        }
        display.setCurrent(locationScreen);
      }
    }

    /**
     * This must be defined but no implementation is required because the MIDlet
     * only responds to user interaction.
     */
    public void pauseApp() {
    }

    /**
     * No further implementation is required because the MIDlet holds no
     * resources that require releasing.
     *
     * @param unconditional is ignored.
     */
    public void destroyApp(boolean unconditional) {
    }

    /**
     * A convenience method for exiting.
     */
    public void exitRequested(){
        destroyApp(false);

        /*
         * notifyDestroyed() tells the scheduler that this MIDlet is now in a
         * destroyed state and is ready for disposal.
         */
        notifyDestroyed();
    }

    /**
     * Implements the transition from the selector screen to the hole
     * location screen.
     *
     * @param string is the new text to be displayed. It is null if the text is
     * not to be changed.
     */
    public void locationDone(String string) {
        if (string != null) {
            locationScreen.setCurrentText(string);
        }
        display.setCurrent(locationScreen);
    }

    /**
     * Implements the transition from the hole selection screen to the
     * display of distance and direction screen.
     */
    public void locationRequested() {
      if (holeSelector == null)
        holeSelector = new HoleSelector(this);

      display.setCurrent(holeSelector);
    }

}
