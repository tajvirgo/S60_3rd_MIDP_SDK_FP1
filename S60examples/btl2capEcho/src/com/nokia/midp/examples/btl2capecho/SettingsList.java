/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.btl2capecho;


import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import javax.bluetooth.DiscoveryAgent;

/**
 * This class provides a list of user settings.
 */
class SettingsList
     extends List
     implements CommandListener
{
    // UI strings
    private static final String SERVER = "Server";
    private static final String CLIENT = "Client";
    // (I abbreviated the strings "Authentication",
    // "Authorization" and "Encryption" because they are a
    // bit long on some MIDP device's List items. Another
    // MIDlet might hard code its preference anyways.)
    private static final String AUTHENTICATION_TRUE =
                                "Authen.: true";
    private static final String AUTHENTICATION_FALSE =
                                "Authen.: false";
    private static final String AUTHORIZATION_TRUE =
                                "Authoriz.: true";
    private static final String AUTHORIZATION_FALSE =
                                "Authoriz.: false";
    private static final String ENCRYPTION_TRUE =
                                "Encrypt: true";
    private static final String ENCRYPTION_FALSE =
                                "Encrypt: false";
    private static final String RETRIEVE_DEVICES_TRUE = "Use known devices";
    private static final String RETRIEVE_DEVICES_FALSE = "Use inquiry";
    private static final String INQUIRY_TYPE_LIAC = "LIAC";
    private static final String INQUIRY_TYPE_GIAC = "GIAC";
    private static final String INQUIRY_TYPE_NOT_DISCOVERABLE =
                                "not discoverable";
    private static final String INQUIRY_TYPE_CACHED = "cached";
    private static final String INQUIRY_TYPE_PREKNOWN = "preknown";

    // settings
    private int inquiryType;
    private int protocol;
    private boolean isServer;
    private boolean useAuthorization;  // client-only
    private boolean useAuthentication;
    // when useAuthenication is false, useEncryption is also false
    private boolean useEncryption;

    // MIDlet stuff
    private final MIDletApplication midlet;
    private final Command startCommand;
    private final Command propCommand;
    private final Command exitCommand;


    SettingsList(MIDletApplication midlet)
    {
        super("Settings", List.IMPLICIT);
        this.midlet = midlet;

        // default setting values


        // You should think about what is the preferred
        // default inquiry type used to make your service
        // discoverable. This MIDlet uses LIAC as the default,
        // but you might want to use GIAC.
        inquiryType = DiscoveryAgent.LIAC;

        isServer = false; // client by default
        useAuthentication = false;
        useEncryption = false; // false when auth. not used
        useAuthorization = false; // false when auth. not used
        updateListElements();

        // add screen commands
        startCommand = new Command("Start application",
                                   Command.SCREEN,
                                   0);
        propCommand = new Command("BT properties",
                                  Command.SCREEN,
                                  1);
        exitCommand = new Command("Exit", Command.EXIT, 0);
        addCommand(startCommand);
        addCommand(propCommand);
        addCommand(exitCommand);
        setCommandListener(this);
    }


    private void updateListElements()
    {
        // remove all old list items
        while(size() > 0)
        {
            delete(0);
        }

        // Index 0: Server / Client
        String string;
        if (isServer)
        {
            string = SERVER;
        }
        else
        {
            string = CLIENT;
        }
        append(string, null);

        // Index 3: LIAC / GIAC
        if (inquiryType == DiscoveryAgent.LIAC)
        {
            append(makeInquiryLabel(isServer, INQUIRY_TYPE_LIAC),
                   null);
        }
        else if (inquiryType == DiscoveryAgent.GIAC)
        {
            append(makeInquiryLabel(isServer, INQUIRY_TYPE_GIAC),
                   null);
        }
        else if (inquiryType == DiscoveryAgent.PREKNOWN)
        {
            append(makeInquiryLabel(isServer,
                                    INQUIRY_TYPE_PREKNOWN),
                   null);
        }
        else if (inquiryType == DiscoveryAgent.CACHED)
        {
            append(makeInquiryLabel(isServer, INQUIRY_TYPE_CACHED),
                   null);
        }
        else if (inquiryType == DiscoveryAgent.NOT_DISCOVERABLE)
        {
            append(makeInquiryLabel(isServer, INQUIRY_TYPE_CACHED),
                   null);
        }


        // Index 2: use authentication true / false
        //          (encryption and authorization can only be
        //           used if authentication is used)
        if (useAuthentication)
        {
            append(AUTHENTICATION_TRUE, null);

            // Index 3: use encryption true / false
            if (useEncryption)
            {
                append(ENCRYPTION_TRUE, null);
            }
            else
            {
               append(ENCRYPTION_FALSE, null);
            }

            // Index 4: ConnectionService only : use auth. true / false
            if (!isServer)
            {
                if (useAuthorization)
                {
                    append(AUTHORIZATION_TRUE, null);
                }
                else
                {
                    append(AUTHORIZATION_FALSE, null);
                }
            }
        }
        else
        {
            useAuthentication = false;
            useEncryption = false;

            append(AUTHENTICATION_FALSE, null);
        }
    }


    private String makeInquiryLabel(boolean searching, String string)
    {
        if (searching)
        {
            // we are searching
            return "Discover: " + string;
        }
        else
        {
            // we will be searched for
            return "Discoverable: " + string;
        }
    }


    public void commandAction(Command command, Displayable d)
    {
        if (command == startCommand)
        {
            midlet.settingsListStart(isServer,
                                     inquiryType,
                                     useAuthentication,
                                     useAuthorization,
                                     useEncryption);
        }
        else if (command == propCommand)
        {
            midlet.settingsListPropertiesRequest();
        }
        else if (command == exitCommand)
        {
            midlet.settingsListExitRequest();
        }
        else if (command == List.SELECT_COMMAND)
        {
            int index = getSelectedIndex();
            switch(index)
            {
                // Index 0: "Server client" (isServer=true) or
                //          "Client server" (isServer=false)
                case 0:
                  isServer = !isServer;
                  break;


                // Index 1: "Discovery mode: LIAC" or
                //          "Discovery mode: GIAC"
                case 1:
                    // toggle between LIAC and GIAC
                    if (inquiryType == DiscoveryAgent.LIAC)
                    {
                       inquiryType = DiscoveryAgent.GIAC;
                    }
                    else
                    {
                        inquiryType = DiscoveryAgent.LIAC;
                    }
                    break;


                // Index 2: "Authentication: true" or
                //          "Authentication: false"
                case 2:
                    // toggle
                    useAuthentication = !useAuthentication;
                    if (!useAuthentication)
                    {
                        // Authorization and encryption are only
                        // settable if authentication is true, otherwise
                        // they are false and we should remove them.
                        // (The order of removal is important.)

                        // Only a Client-server has this setting option
                        // and not a Server-client, thus the size check.
                        if (size() == 5)
                        {
                            delete(4); // remove authorization from List
                            useAuthorization = false;
                        }

                        this.delete(3); // remove encryption from List
                        useEncryption = false;
                    }
                    break;


                // Index 3: "Encryption: true" or
                //          "Encryption: false"
                case 3:
                    useEncryption = !useEncryption;  // toggle
                    break;


                // Index 4: "Authorization: true" or
                //          "Authorization: false"
                case 4:
                    // toggle
                    useAuthorization = !useAuthorization;
                    break;

            }
            updateListElements();
            setSelectedIndex(index, true);
        }
    }
}
