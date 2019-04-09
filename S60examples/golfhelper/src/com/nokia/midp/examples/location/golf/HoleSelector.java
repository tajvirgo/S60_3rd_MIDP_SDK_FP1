/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.location.golf;

import javax.microedition.lcdui.*;
import javax.microedition.location.*;

/**
 * The HoleSelector class allows the user to select the current hole.
 * <p>
 * This class extends the Form class and uses an instance of ChoiceGroup to
 * provide an sets of choices to the user.
 * <p>
 */

public class HoleSelector
    extends Form implements CommandListener, LocationListener {

  /** String used to convey location, distance or status information to user. */
  private String distanceString;

  /**
   * Back reference to parent MIDlet class enabling this class to access
   * to access the callback methods provided.
   */
  private final GolfHelperMIDlet midlet;

  /** Priority of commands relative to others of the same type. */
  private static final int COMMAND_PRIORITY = 1;

  /** Command specified to dismiss the form and accept the choice. */
  private static final Command CMD_OK =
      new Command("OK", Command.OK, COMMAND_PRIORITY);

  /** Command specified to dismiss the form and reject the choice. */
  private static final Command CMD_CANCEL =
      new Command("Cancel", Command.CANCEL, COMMAND_PRIORITY);

  /** Choice of what is the current hole. */
  private ChoiceGroup choice;

  /** mutex used in conjunction with LocationService inner class */
  private Object mutex = new Object();

  private final int serviceTimeout = 10000;

  private Coordinates coordinates = null;
  private Location location = null;
  private Criteria criteria = null;
  private LocationProvider locationProvider = null;
  private LandmarkStore store = null;

  /* A canned set of golf hole locations.  */
  final int NUMBER_OF_HOLES = 9;
  final String storeName = "GolfCourse";
  final String category = "Holes";

  /**
   * The constructor initializes the class making it ready for use.
   * <p>
   * It initializes the reference to the parent MIDlet class with the paramater
   * value 'midlet'.
   *
   * @param midlet is a reference to the parent MIDlet. Enables callback to parent object.
   */
  HoleSelector(GolfHelperMIDlet midlet) {
    super("Golf Helper MIDlet");
    this.midlet = midlet;
    choice = new ChoiceGroup("What hole are you aiming for?",
                             Choice.EXCLUSIVE);

    // Add available choices
    for (int i = 1; i <= NUMBER_OF_HOLES; i++) {
      choice.append("" + i, null);
    }

    append(choice);

    // Add the Commands define to the command handling framework
    addCommand(CMD_OK);
    addCommand(CMD_CANCEL);
    setCommandListener(this);
  }

  /**
   * Handles commands received.
   * <p>
   * The CMD_OK command instructs the MIDlet to dismiss the form and accept
   * the choice.
   * <p>
   * The CMD_CANCEL command instructs the MIDlet to dismiss the form and
   * reject the choice.
   *
   * @param cmd is the identity of command received from the framework.
   * @param source is the identity of the UI area displayed which generated the
   * command.
   */
  public void commandAction(Command cmd, Displayable source) {

    if (cmd == CMD_OK) {
      LocationService service = new LocationService();
      service.start();
    }
    else if (cmd == CMD_CANCEL) {
      midlet.locationDone(null);
    }
    else {
      // Functionality to handle for unexpected commands may be added here...
    }
  }

  /**
   * Gets the location information for the midlet.  The method takes
   * one argument that is an index into an array of target coordinates.
   * The method then obtains the current location and determines the
   * distance to the selected target location.
   * @param index int
   * @return String
   */
  public String getLocation(int index) {

    index++;

    String locationString = null;
    try {

      // Create a Criteria object for defining desired selection criteria
      criteria = new Criteria();
      // Specify horizontal accuracy of 10 meters, leave other parameters
      // at default values.
      criteria.setHorizontalAccuracy(10);

      locationProvider = LocationProvider.getInstance(criteria);

      // if location provider cannot provide the desired accuracy
      // try another value
      if (locationProvider == null)
      {
        // try 50 meters and if that cannot be provided then
        // inform the user
        criteria.setHorizontalAccuracy(50);
        locationProvider = LocationProvider.getInstance(criteria);

        if (locationProvider == null)
        {
           locationString = "Requested accuracy is unavailable\n";
            return (locationString);
        }
      }

      locationProvider.setLocationListener(this, -1, -1, -1);

      //  determine if the location provider is available
      try {
        // If current state is not "available" or "out of service", wait for
        // state change (for 10 seconds maximum)
        if (locationProvider.getState() ==
            LocationProvider.TEMPORARILY_UNAVAILABLE) {
          synchronized (mutex) {
            mutex.wait(serviceTimeout);
          }
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }

      // Now lets get the current state
      int state = state = locationProvider.getState();
      if (state == LocationProvider.AVAILABLE) {
        // get the location, 10 second timeout
        location = locationProvider.getLocation(10);

        coordinates = location.getQualifiedCoordinates();
        QualifiedCoordinates qc = null;
        Landmark l = null;

        try {
            /* Get the landmark for this hole */
            java.util.Enumeration e = null;
            e = store.getLandmarks(category,String.valueOf(index));
            if (e.hasMoreElements())
            {
              l = (Landmark) e.nextElement();
            }

            if (l != null)
            {
              qc = l.getQualifiedCoordinates();
            }
        }
        catch(Exception e)
        {
          return ("Exception");
        }

        if (coordinates != null) {
          // use coordinate information
          locationString = "Current Location\n" +
              " Latitude =" + coordinates.getLatitude() + "\n" +
              " Longitude =" + coordinates.getLongitude() + "\n\n\n" +
              "Distance to hole:\n" + " " +
              (int)(qc.distance(coordinates)) +
              " meters";
        }
      }
      else if (state == LocationProvider.TEMPORARILY_UNAVAILABLE) {
        locationString = "Location server temporarily unavailable\n";
      }
      else {
        locationString = "Location server is unavailable\n";
      }

    }
    catch (Exception e) {
      return ("Exception");
    }
    return (locationString);
  }

  /**
   * A helper method to initialize a local set of landmarks
   *
   * The default store may be used instead of this local store however
   * the landmarks would need to entered externally.
   *
   * If the default store is used then the store may be obtained
   * with the followiong code:
   *
   *  store = LandmarkStore.getInstance(null);
   *
   *  Using null for the argument returns the default store.
   */
  public void initGolfCourseHoles() {

      /* Create a landmark store of golf hole locations */
      try {

        try {
          LandmarkStore.deleteLandmarkStore(storeName);
        }
        catch (Exception ex){} //intentionally take no action here.

        store = LandmarkStore.getInstance(storeName);

        if (store == null)
        {
          LandmarkStore.createLandmarkStore(storeName);
          store = LandmarkStore.getInstance(storeName);

          store.addCategory("Holes");
          store.addLandmark(new Landmark(String.valueOf(1), "Holes",
                                         new QualifiedCoordinates(hole1Long,
              hole1Lat,
              Float.NaN, Float.NaN, Float.NaN), null), category);
          store.addLandmark(new Landmark(String.valueOf(2), "Holes",
                                         new QualifiedCoordinates(hole2Long,
              hole2Lat,
              Float.NaN, Float.NaN, Float.NaN), null), category);
          store.addLandmark(new Landmark(String.valueOf(3), "Holes",
                                         new QualifiedCoordinates(hole3Long,
              hole3Lat,
              Float.NaN, Float.NaN, Float.NaN), null), category);
          store.addLandmark(new Landmark(String.valueOf(4), "Holes",
                                         new QualifiedCoordinates(hole4Long,
              hole4Lat,
              Float.NaN, Float.NaN, Float.NaN), null), category);
          store.addLandmark(new Landmark(String.valueOf(5), "Holes",
                                         new QualifiedCoordinates(hole5Long,
              hole5Lat,
              Float.NaN, Float.NaN, Float.NaN), null), category);
          store.addLandmark(new Landmark(String.valueOf(6), "Holes",
                                         new QualifiedCoordinates(hole6Long,
              hole6Lat,
              Float.NaN, Float.NaN, Float.NaN), null), category);
          store.addLandmark(new Landmark(String.valueOf(7), "Holes",
                                         new QualifiedCoordinates(hole7Long,
              hole7Lat,
              Float.NaN, Float.NaN, Float.NaN), null), category);
          store.addLandmark(new Landmark(String.valueOf(8), "Holes",
                                         new QualifiedCoordinates(hole8Long,
              hole8Lat,
              Float.NaN, Float.NaN, Float.NaN), null), category);
          store.addLandmark(new Landmark(String.valueOf(9), "Holes",
                                         new QualifiedCoordinates(hole9Long,
              hole9Lat,
              Float.NaN, Float.NaN, Float.NaN), null), category);
        }

      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
  }

  class LocationService
      extends Thread {
    public LocationService() {

    }

    public void run() {
      distanceString = getLocation(choice.getSelectedIndex());
      // set the location on the midlet screen.
      midlet.locationDone(distanceString);
    }

  }

  /**
   * Notification method for location listener interface.
   * This method does not take any action in this example.
   */
  public void locationUpdated() {

  }

  /**
   * Notification method for location listener interface.
   * This method does not take any action in this example.
   */
  public void locationUpdated(LocationProvider provider, Location location) {

  }

  public void providerStateChanged(LocationProvider provider, int newState) {
    // Provider state is changed => whatever the current provider state is, release locked provider thread.
    try {
      synchronized (mutex) {
        mutex.notify();
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

  }


  /* Constants used for coordinates when creating landmarks */
  final double hole1Long = 61.4480;
  final double hole1Lat = 23.8550;
  final double hole2Long = 61.4485;
  final double hole2Lat = 23.8575;
  final double hole3Long = 61.4487;
  final double hole3Lat = 23.8577;
  final double hole4Long = 61.4489;
  final double hole4Lat = 23.8579;
  final double hole5Long = 61.4492;
  final double hole5Lat = 23.8582;
  final double hole6Long = 61.4495;
  final double hole6Lat = 23.8585;
  final double hole7Long = 61.4497;
  final double hole7Lat = 23.8587;
  final double hole8Long = 61.4499;
  final double hole8Lat = 23.8589;
  final double hole9Long = 61.4500;
  final double hole9Lat = 23.8595;

}
