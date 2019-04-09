/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.wma.chat;

import java.util.*;
import javax.microedition.lcdui.*;

/**
 *  Handles the screens required by the MIDlet and the user
 *  interaction with them.
 *
 * @version 1.0
 */
public class ScreenHandler implements CommandListener {
    /** String constant used as the prompt */
    private static final String PROMPT = "> ";
    /** Command priority constant */
    private static final int COMMAND_PRIORITY = 1;
    /** Command for ok */
    public static final Command CMD_OK = new Command("Ok", Command.OK, COMMAND_PRIORITY);
    /** Command for exit */
    public static final Command CMD_EXIT = new Command("Exit", Command.EXIT, COMMAND_PRIORITY);
    /** Command for send */
    public static final Command CMD_SEND = new Command("Send", Command.OK, COMMAND_PRIORITY);
    /** Command to go to change peers screen */
    public static final Command CMD_PEERS = new Command("Change Peers", Command.OK, COMMAND_PRIORITY);
    /** Command to go to change peers screen */
    public static final Command CMD_ADD_PEER = new Command("Add Peer", Command.OK, COMMAND_PRIORITY);
    /** Command to go to write text screen */
    public static final Command CMD_WRITE = new Command("Write Message", Command.OK, COMMAND_PRIORITY);
    /** Command for cancel */
    public static final Command CMD_CANCEL = new Command("Cancel", Command.CANCEL, COMMAND_PRIORITY);
    /** TextField Size constant */
    public static final int TEXTFIELD_SIZE = 256;

    /** UI display */
    private Display display;
    /** instance of MessageHandler */
    private MessageHandler messageHandler;
    /** Form to set chat name */
    public ChatNameScreen chatNameScreen;
    /** Form to select peers and enter send text */
    private PeerListScreen peerListScreen;
    /** Form to display messages */
    private ChatScreen chatScreen;
    /** Form to write messages */
    private WriteScreen writeScreen;
    /** Form to add peers */
    private AddPeerScreen peerScreen;
    /** Alert to notify user of errors */
    private Alert alert;
    /** reference to MIDlet instance */
    private WMAMIDlet parent;
    /** String containing chosen chat name */
    private String chatName = "";
    /** Vector to hold names of chosen peers */
    private Vector peerNumbers = new Vector();

    /**
     *  Constructor to set up screens.
     *  Instantiates all the screens and sets the display to
     *  the ChatNameScreen.
     */
    public ScreenHandler(WMAMIDlet midlet) {
        parent = midlet;
        display = Display.getDisplay(parent);
        chatNameScreen = new ChatNameScreen(this);
        chatScreen = new ChatScreen(this);
        peerListScreen = new PeerListScreen(this, parent.numbersList);
        writeScreen = new WriteScreen(this);
        peerScreen = new AddPeerScreen(this, midlet);

        alert = new Alert("Information", "", null, AlertType.INFO);

        setScreen(chatNameScreen);
    }

    /**
     *  Reads the chosen peer names into a Vector. Gets the selected
     *  peers from the PeerListScreen and reads them into a Vector.
     */
    public void setPeers() {
        boolean[] peers = null;
        peerNumbers.removeAllElements();
        try {
            peers = peerListScreen.getSelectedPeers();
        } catch (Exception e) {
            setAlert("Exception encountered", writeScreen);
        }
        Enumeration enumeration = parent.numbersList.keys();
        int i = 0;
        while (enumeration.hasMoreElements()){
            String number = enumeration.nextElement().toString();
            if (peers[i] == true) {
                peerNumbers.addElement(number);
            }
            i++;
        }
    }

    /**
     *  Stores the chat name entered by the user on the ChatNameScreen.
     */
    public void setChatName() {
        chatName = chatNameScreen.getChatName();
    }

    /**
     *  Updates the ChatScreen.
     *  Calls the updateScreen() method of ChatScreen to update the screen
     *  with a message and then sets the ChatScreen as the current screen.
     */
    public void updateChatScreen(String message) {
        chatScreen.updateScreen(message);
        setScreen(chatScreen);
    }

    /**
     *  Sets the current screen.
     *
     *  @param screen Form to be displayed
     */
    public void setScreen(Form screen) {
        display.setCurrent(screen);
    }

    /**
     *  Sets the current screen.
     *
     *  @param list List to be displayed
     */
    public void setScreen(javax.microedition.lcdui.List list) {
    	display.setCurrent(list);
    }

    /** Sets the current screen to an alert. */
    public void setAlert(String info, Form screen) {
        alert.setString(info);
        display.setCurrent(alert, screen);
    }

    /**
     *  Handles commands on the UI.
     *
     *  @param cmd the command object identifying the command
     *  @param source the displayable on which the command has occured
     */
    public void commandAction(Command cmd, Displayable source) {
    	if(cmd == CMD_EXIT){
            parent.destroyApp(true);
            parent.notifyDestroyed();
        } else if (cmd == CMD_OK) {
            handleOkCommand(source);
        } else if (cmd == CMD_PEERS) {
            //sets display to peer list display
            setScreen(peerListScreen);
        } else if (cmd == CMD_ADD_PEER) {
        	setScreen(peerScreen);
        } else if (cmd == CMD_SEND) {
            handleSendCommand();
        } else if (cmd == CMD_WRITE) {
            writeScreen.updateScreen(peerNumbers);
            setScreen(writeScreen);
        } else if (cmd == CMD_CANCEL) {
            setScreen(chatScreen);
        }
    }

    /**
     *  Handles the Ok command. If the command is from the ChatNameScreen the
     *  chat name is set. If the command is from the PeerListScreen the peers
     *  are set.
     *
     *  @param source the displayable on which the command occured
     */
    public void handleOkCommand(Displayable source) {
        if (source == chatNameScreen) {
            setChatName();
        } else if (source == peerListScreen) {
            setPeers();
        } else if (source == peerScreen) {
        	String number = peerScreen.getPeerNumber();
        	String port = peerScreen.getPeerPort();
        	parent.numbersList.put(number,port);
        	peerListScreen.updatePeerList();
        	peerScreen.emptyField();
        }

        if(source == peerScreen)
        	setScreen(peerListScreen);
        else {
        	setScreen(chatScreen);
        }
    }

    /**
     *  Handles the Send command. Appends the chat name and prompt
     *  character to then beginning of the message. If the peerNumbers
     *  vector contains some peer numbers the doSend() method of the
     *  MessageHandler is called otherwise an alert is displayed notifying
     *  the user that no peers have been set.
     */
    public void handleSendCommand() {
        String text = chatName + PROMPT + writeScreen.getMessageText();
        if (!peerNumbers.isEmpty()) {
            parent.messageHandler.doSend(text, peerNumbers);
        } else {
            setAlert("No peers set", writeScreen);
        }
    }
}
