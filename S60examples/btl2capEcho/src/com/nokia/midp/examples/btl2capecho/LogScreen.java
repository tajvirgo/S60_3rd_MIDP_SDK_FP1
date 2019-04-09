/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.btl2capecho;

import java.util.Vector;
import javax.microedition.lcdui.*;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;


// This class represents a simple log screen. In the ideal case,
// the actual log would be encapsulated by a different class
// than the presentation (LogScreen). It is a bit less elegant,
// but eliminates one class, to combine the log and log
// screen in the same class. This  MIDlet uses the LogScreen
// mainly as a simple aid during device inquiry + service discovery
// to help a user follow the progress if they wish to. So the
// log isn't persistently saved in the record store. (That would
// be easy to add if it were needed.) For unsophisticated users,
// a MIDlet would use some other visual aid rather than a log to show
// progress through the device inquiry + service discovery phases.
// The target users of this MIDlet are mainly developers learning
// to use Bluetooth, so a LogScreen is probably more helpful for them,
// as it helps show how which Bluetooth devices were found during
// device inquiry, which devices of those are running the desired
// service, and so on.

/**
 * This class represents a simple log screen.
 */
public class LogScreen
    extends Form
    implements CommandListener
{
    private static final Vector entries = new Vector();
    private static final String FIRST_ENTRY = "-- Log started: --\n\n";

    // We place a limit the maximum number of entries logged.
    // Only the 1 .. MAX_ENTRIES last entries will be kept
    // in the log. If the log exceeds MAX_ENTRIES, the
    // earliest entries will be deleted.
    private static final int MAX_ENTRIES = 300;

    static
    {
        log(FIRST_ENTRY);
    }


    private final MIDletApplication midlet;
    private final Displayable next;
    private final Command refreshCommand;
    private final Command deleteCommand;
    private final Command closeCommand;


    public LogScreen(MIDletApplication midlet,
                     Displayable next,
                     String title,
                     String closeLabel)
    {
        super(title);
        this.midlet = midlet;
        this.next = next;

        refresh(); // add any text already present

        refreshCommand = new Command("Refresh", Command.SCREEN, 1);
        deleteCommand = new Command("Delete", Command.SCREEN, 2);
        closeCommand = new Command(closeLabel, Command.SCREEN, 3);
        addCommand(refreshCommand);
        addCommand(deleteCommand);
        addCommand(closeCommand);
        setCommandListener(this);
    }


    public static void log (String string)
    {
        if (entries.size() > MAX_ENTRIES)
        {
            entries.removeElementAt(0);
        }
        entries.addElement(string);
    }


    private void refresh()
    {
        // clear the display's text
        while(size() > 0)
        {
            delete(0);
        }

        // get the lastest status and display that as text
        String text = "";
        for (int i=0; i < entries.size(); i++)
        {
            String str = (String) entries.elementAt(i);
            if (str != null)
            {
                text += str;
            }
        }
        append(text);
    }


    public void commandAction(Command command, Displayable d)
    {
        if (command == closeCommand)
        {
            midlet.logScreenClosed(next);
        }
        else if (command == refreshCommand)
        {
            refresh();
        }
        else if (command == deleteCommand)
        {
            // The deletion of all log strings affects
            // all LogScreen instances.

            synchronized(this)
            {
                entries.removeAllElements();
                log(FIRST_ENTRY);
            }

            refresh();
        }
    }


    // It was somewhat convenient to place these helper
    // methods inside the LogScreen class.

    public static String inquiryAccessCodeString(int iac)
    {
        String str = null;
        switch(iac)
        {
            case DiscoveryAgent.CACHED:
                str = "CACHED";
                break;

            case DiscoveryAgent.GIAC:
                str = "GIAC";
                break;

            case DiscoveryAgent.LIAC:
                str = "LIAC";
                break;

            case DiscoveryAgent.PREKNOWN:
                str = "PREKNOWN";
                break;
        }
        return str;
    }


    public static String responseCodeString(int responseCode)
    {
        String str = null;
        switch (responseCode)
        {
          case DiscoveryListener.SERVICE_SEARCH_COMPLETED:
              str = "SERVICE_SEARCH_COMPLETED";
              break;
          case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
              str = "SERVICE_SEARCH_DEVICE_NOT_REACHABLE";
              break;
          case DiscoveryListener.SERVICE_SEARCH_ERROR:
              str = "SERVICE_SEARCH_ERROR";
              break;
          case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS:
              str = "SERVICE_SEARCH_NO_RECORDS";
              break;
          case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
              str = "SERVICE_SEARCH_TERMINATED";
              break;
        }
        return str;
    }
}
