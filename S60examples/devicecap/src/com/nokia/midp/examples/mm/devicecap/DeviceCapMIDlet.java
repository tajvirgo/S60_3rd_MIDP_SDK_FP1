/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.mm.devicecap;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;

/**
 * Lists the media content types supported by this device, and
 * the protocols which they can be requested with.  Also lists
 * the supported media capture methods.
 *
 * @version 1.1
 */
public class DeviceCapMIDlet extends MIDlet
    implements CommandListener {
    /** Priority of commands relative to others of the same type. */
    private static final int COMMAND_PRIORITY = 2;
    /** Command to exit the application. */
    private static final Command CMD_EXIT = new Command("Exit", Command.EXIT, COMMAND_PRIORITY);
    /** Command to show supported network protocols for the selected content type. */
    private static final Command CMD_DETAILS = new Command("Details", Command.ITEM, COMMAND_PRIORITY);
    /** Command to show if audio capture and recording is supported. */
    private static final Command CMD_AUDIO_CAPTURE = new Command("Audio capture", Command.ITEM, COMMAND_PRIORITY);
    /** Command to show if video capture and recording is supported. */
    private static final Command CMD_VIDEO_CAPTURE = new Command("Video capture", Command.ITEM, COMMAND_PRIORITY);
    /** Command to show if video capture and recording is supported. */
    private static final Command CMD_POLYPHONY = new Command("Polyphonic sound", Command.ITEM, COMMAND_PRIORITY);
    /** List.getSelectedIndex() returns this if nothing is selected. */
    private static final int LIST_NONE_SELECTED = -1;

    /** Caches the result of Display.getDisplay(). */
    private Display display;
    /** A List containing the supported content types. */
    private List typeList;

    /**
     * Constructs and displays a List containing all supported media formats.
     * Also calls listCapabilities() to send the information to System.out.
     */
    public void startApp() {
        if (display == null) {
            // First time we've been called
            display = Display.getDisplay(this);

            typeList = new List("Device Capabilities",
                    List.IMPLICIT,
                    Manager.getSupportedContentTypes(null),
                    null);
            typeList.setCommandListener(this);
            typeList.addCommand(CMD_EXIT);
            typeList.addCommand(CMD_DETAILS);
            typeList.addCommand(CMD_AUDIO_CAPTURE);
            typeList.addCommand(CMD_VIDEO_CAPTURE);
            typeList.addCommand(CMD_POLYPHONY);

        }
        display.setCurrent(typeList);
    }

    /**
     * Must be defined but no implementation required because this MIDlet only
     * responds to user interaction.
     */
    public void pauseApp() {
    }

    /**
     * No further implementation is required because this MIDlet holds no
     * resources that require releasing.
     *
     * @param unconditional is ignored.
     */
    public void destroyApp(boolean unconditional) {
    }

    /**
     * Responds to a menu item.
     *
     *@param cmd the menu item or implicit SELECT_COMMAND of LCDUI.List
     *@param source the Displayable object where the event originated
     */
    public void commandAction(Command cmd, Displayable source) {
        if (cmd == CMD_EXIT) {
            destroyApp(true);
            notifyDestroyed();
        } else if (cmd == CMD_DETAILS || cmd == List.SELECT_COMMAND) {
            showDetails();
        } else if (cmd == CMD_AUDIO_CAPTURE) {
            display.setCurrent(new Alert(
                "audio",
                QueryProperty.getCaptureSupport("audio"),
                null,
                AlertType.INFO));
        } else if (cmd == CMD_VIDEO_CAPTURE) {
            display.setCurrent(new Alert(
                "video",
                QueryProperty.getCaptureSupport("video"),
                null,
                AlertType.INFO));
        } else if (cmd == CMD_POLYPHONY) {
            display.setCurrent(new Alert(
                "Polyphony",
                QueryProperty.getPolyphonySupport(),
                null,
                AlertType.INFO));
        } else {
            //a non-demo application should handle this unexpected condition
        }
    }

    /**
     * Displays the details of the content type selected in the List.
     */
    private void showDetails() {
        int index = typeList.getSelectedIndex();
        if (index == LIST_NONE_SELECTED) {
            return;
        }
        String type = typeList.getString(index);
        String protocols[] = Manager.getSupportedProtocols(type);
        Alert alert = new Alert(type, arrayToString(protocols), null, AlertType.INFO);
        display.setCurrent(alert);
    }

    /**
     * Convert an array of Strings to a single String, suitable for display.
     *@return the original items as a comma-seperated list.
     */
    private String arrayToString(String[] items) {
        if ((items == null) || (items.length == 0)) {
            return "";
        } else {
            StringBuffer buffer = new StringBuffer(items[0]);
            for (int i=1; i<items.length; i++) {
                buffer.append(", ");
                buffer.append(items[i]);
            }
            return buffer.toString();
        }
    }

}
