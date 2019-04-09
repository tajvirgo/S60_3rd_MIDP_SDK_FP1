/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.btsppecho;

import java.util.Vector;
import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;

import com.nokia.midp.examples.btsppecho.client.ConnectionService;

/**
 * This is the midlet class for the example.
 */
public class MIDletApplication
    extends MIDlet
{
    private final static String UUID =
                                "A55665EE9F9146109085C2055C888B39";
    private final static String SERVICE_URL =
                                "btspp://localhost:" + UUID;
    private final SettingsList settingsList;
    private boolean restoreDiscoverableModeOnExit;
    private int initialDiscoverableMode;

    // Client server
    private ConnectionService ConnectionService = null;
    private ClientForm clientForm = null;

    // Server client
    private ServiceDiscoveryList serviceDiscoveryList = null;
    private ServerForm serverForm = null;
    boolean serverUseAuthentication = false;
    boolean serverUseEncryption = false;


    public MIDletApplication()
    {
        // Try to read the initial discoverable mode of
        // device, so it can be restored on exit.
        try
        {
            restoreDiscoverableModeOnExit = true;
            initialDiscoverableMode =
                LocalDevice.getLocalDevice()
                           .getDiscoverable();

        }
        catch (BluetoothStateException e)
        {
            restoreDiscoverableModeOnExit = false;
        }
        settingsList = new SettingsList(this);
    }


    private void exit()
    {
        destroyApp(false);
        notifyDestroyed();
    }


    public void startApp()
    {
        Display display = Display.getDisplay(this);
        display.setCurrent(settingsList);
        ErrorScreen.init(null, display);
    }


    public void pauseApp()
    {
        // we can ignore this
    }


    public void destroyApp(boolean unconditional)
    {
        // stop any networking, service discovery, etc.
        // threads that the MIDlet may have started

        if (serviceDiscoveryList != null)
        {
            serviceDiscoveryList.cancelPendingSearches();
            serviceDiscoveryList.abort();
        }

        if (ConnectionService != null)
        {
            ConnectionService.close();
        }

        if (clientForm != null)
        {
            clientForm.closeAll();
        }

        if (serverForm != null)
        {
            serverForm.closeAll();
        }

        // I restore the discoverable mode to the initial
        // value on exit, so the behaviour of this MIDlet
        // might be more similar in this respect on all
        // vendors' devices. (You might want to rethink
        // this in your MIDlet, for example if a device
        // allows you to run multiple simultaneous Bluetooth
        // applications, each having varying start &
        // exit times.)
        if (restoreDiscoverableModeOnExit)
        {
            try
            {
                LocalDevice ld = LocalDevice.getLocalDevice();
                ld.setDiscoverable(initialDiscoverableMode);
            }
            catch (BluetoothStateException e)
            {
                // there is nothing we can do
                // to handle this case: ignore it
            }
        }
    }


    // screen callbacks

    // ClientForm

    public void clientFormExitRequest()
    {
        exit();
    }


    public void clientFormViewLog(Displayable next)
    {
       LogScreen logScreen =
           new LogScreen(this,
                         next,
                         "Log",
                         "Back");
       Display.getDisplay(this).setCurrent(logScreen);
   }


    // SettingsList callbacks

    public void settingsListStart(boolean isServer,
                                  int inquiryAccessCode,
                                  boolean useAuthentication,
                                  boolean useAuthorization,
                                  boolean useEncryption)
    {
        // set inquiry access mode for ConnectionService
        if (!isServer)
        {
            try
            {
                LocalDevice.getLocalDevice()
                           .setDiscoverable(inquiryAccessCode);
            }
            catch(BluetoothStateException e)
            {
                String msg = "Error changing inquiry access type: " +
                          e.getMessage();
                ErrorScreen.showError(msg, settingsList);
            }
        }

        if (isServer)
        {
            // start application in server role

            // we only run one server at a time,
            // so the following is safe
            serverUseAuthentication = useAuthentication;
            serverUseEncryption = useEncryption;
            serviceDiscoveryList =
                new ServiceDiscoveryList(
                        this,
                        UUID,
                        inquiryAccessCode);
            Display.getDisplay(this)
                   .setCurrent(serviceDiscoveryList);
        }
        else
        {
            // start application in client role

            clientForm = new ClientForm(this);
            String url = SERVICE_URL;
            if (useAuthentication)
            {
                url += ";authenticate=true";
            }
            else
            {
                url += ";authenticate=false";
            }
            if (useAuthorization)
            {
                url += ";authorize=true";
            }
            else
            {
                url += ";authorize=false";
            }
            if (useEncryption)
            {
                url += ";encrypt=true";
            }
            else
            {
                url += ";encrypt=false";
            }

            url += ";name=btsppEcho";

            ConnectionService = new ConnectionService(url, clientForm);

            Display.getDisplay(this).setCurrent(clientForm);
        }
    }


    public void settingsListPropertiesRequest()
    {
        String[] keys =
        {
            "bluetooth.api.version",
            "bluetooth.connected.devices.max",
            "bluetooth.connected.inquiry",
            "bluetooth.connected.inquiry.scan",
            "bluetooth.connected.page",
            "bluetooth.connected.page.scan",
            "bluetooth.l2cap.receiveMTU.max",
            "bluetooth.master.switch",
            "bluetooth.sd.attr.retrievable.max",
            "bluetooth.sd.trans.max",
        };

        String str = "";
        try
        {
          str = "my bluetooth address: " +
                LocalDevice.getLocalDevice()
                           .getBluetoothAddress() + "\n";
        }
        catch (BluetoothStateException e)
        {
            // there is nothing we can do: ignore it
        }
        for (int i=0; i < keys.length; i++)
        {
           str += keys[i] + ": " +
                  LocalDevice.getProperty(keys[i]) + "\n";
        }

        TextScreen textScreen =
            new TextScreen(this,
                           settingsList,
                           "Device properties",
                           str,
                           "Back");
        Display.getDisplay(this).setCurrent(textScreen);
    }


    public void settingsListExitRequest()
    {
        exit();
    }


    // ServiceDiscoveryList callbacks

    public void serviceDiscoveryListFatalError(String errorMessage)
    {
        ErrorScreen.showError(errorMessage, serviceDiscoveryList);
        Display.getDisplay(this).setCurrent(settingsList);
    }


    public void serviceDiscoveryListError(String errorMessage)
    {
        ErrorScreen.showError(errorMessage, serviceDiscoveryList);
    }


    public void serviceDiscoveryListOpen(Vector selectedServiceRecords)
    {
        int security;
        if (serverUseAuthentication)
        {
           if (serverUseEncryption)
           {
               security = ServiceRecord.AUTHENTICATE_ENCRYPT;
           }
           else
           {
               security = ServiceRecord.AUTHENTICATE_NOENCRYPT;
           }
        }
        else
        {
           security = ServiceRecord.NOAUTHENTICATE_NOENCRYPT;
        }

        if (serverForm == null)
        {
            serverForm = new ServerForm(this);
        }
        serverForm.makeConnections(selectedServiceRecords, security);
        Display.getDisplay(this).setCurrent(serverForm);
    }


    public void serviceDiscoveryListExitRequest()
    {
        exit();
    }


    public void serviceDiscoveryListBackRequest(Displayable next)
    {
        Display.getDisplay(this).setCurrent(next);
    }


    public void serviceDiscoveryListViewLog(Displayable next)
    {
        LogScreen logScreen =
            new LogScreen(this,
                          next,
                          "Log",
                          "Back");
        Display.getDisplay(this).setCurrent(logScreen);
    }


    // TextScreen

    public void textScreenClosed(Displayable next)
    {
        Display.getDisplay(this).setCurrent(next);
    }


    // LogScreen

    public void logScreenClosed(Displayable next)
    {
        Display.getDisplay(this).setCurrent(next);
    }


    // ServerForm

    public void serverFormSearchRequest(int numConnectionsOpen)
    {
         // cleanup for new search
        serviceDiscoveryList.init(numConnectionsOpen);

        if (numConnectionsOpen > 0)
        {
            serviceDiscoveryList.addBackCommand(serverForm);
        }
        Display.getDisplay(this).setCurrent(serviceDiscoveryList);
    }


    public void serverFormExitRequest()
    {
        exit();
    }


    public void serverFormAddConnection(Vector alreadyOpen)
    {
        // I took a simple approach of simply changing the
        // screen to the ServiceDiscovery screen when the
        // user wants to try and add a new connection, or
        // perform both a new device inquiry + service search
        // and then add more connections.
        //
        // However, reality can be a bit more complicated:
        //   - How many previously discovered items (e.g. device
        //     running the desired service) have we already
        //     connected to, or not connected to?
        //   - How many additional new connections can this device
        //     open below its maximum limit? The maximum number
        //     of simultaneous connections can vary in different
        //     devices (i.e. see "bluetooth.connected.devices.max").
        //   - Can new inquiries/searches be started while
        //     already connected?
        // Depending on your MIDlet's needs + use cases and
        // the devices it is likely to be deployed in, it
        // might employ a bit more user friendly approach than
        // the simplistic/generic one used here.


        serviceDiscoveryList.remove(alreadyOpen);
        serviceDiscoveryList.addBackCommand(serverForm);
        Display.getDisplay(this).setCurrent(serviceDiscoveryList);
    }


    public void serverFormViewLog()
    {
        LogScreen logScreen =
            new LogScreen(this,
                          serverForm,
                          "Log",
                          "Back");
        Display.getDisplay(this).setCurrent(logScreen);
    }

}
