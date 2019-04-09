/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.wma.chat;

import java.util.*;
import javax.microedition.lcdui.*;

/**
 *  Extends the WMAForm class and provides a ChoiceGroup populated with the possible
 *  peers in order to select the Peers to send messages to.
 *
 * @version 1.0
 */
public class PeerListScreen extends WMAForm{
    /** ChoiceGroup to contain possible peers */
    private ChoiceGroup peerListChoice;
    private Hashtable numbersList;

    /**
     *  Sets up the screen with a ChoiceGroup containing the possible peers and
     *  adds the Ok and Cancel commands.
     *
     *  @param screenHandler the current ScreenHandler
     *  @param numbersList hashtable of all the possible peers
     */
    public PeerListScreen (ScreenHandler screenHandler, Hashtable numbersList) {
        super(screenHandler);
        this.numbersList = numbersList;
        peerListChoice = new ChoiceGroup("Peer List", ChoiceGroup.MULTIPLE);

        updatePeerList();
        append(peerListChoice);

        addCommand(ScreenHandler.CMD_OK);
        addCommand(ScreenHandler.CMD_CANCEL);
        addCommand(ScreenHandler.CMD_ADD_PEER);
    }

    /**
     * Updates list from numbersList instance
     */
    public void updatePeerList() {
    	peerListChoice.deleteAll();
        for(Enumeration enumeration = numbersList.keys(); enumeration.hasMoreElements(); ) {
            peerListChoice.append(enumeration.nextElement().toString(), null);
        }

    }

    /**
     *  Returns the selected peers.
     *
     *  @return a boolean array indicating the chosen members of the ChoiceGroup
     */
    public boolean[] getSelectedPeers() {
        boolean[] flags = new boolean[peerListChoice.size()];
        peerListChoice.getSelectedFlags(flags);
        return flags;
    }
}