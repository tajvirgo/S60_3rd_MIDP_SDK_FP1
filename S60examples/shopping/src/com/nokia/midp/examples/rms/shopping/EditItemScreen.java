/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.rms.shopping;

import javax.microedition.lcdui.*;

/**
 * UI to edit the details of a ShoppingItem.
 *
 * The implementation is discussed in the package documentation for
 * {@link com.nokia.midp.examples.rms.shopping} .
 */
public class EditItemScreen extends Form {
    /** Maximum number of characters in each text field. */
    private static final int TEXT_FIELD_MAX_SIZE = 30;

    /** Reference to the parent midlet. */
    private ShoppingListMIDlet midlet;

    /** Graphical component. */
    private TextField itemName;
    /** Graphical component. */
    private TextField itemQuantity;

    /** The item being editted. */
    private ShoppingItem item;

    /**
     * Constructor.
     * After construction, a call to editItem() is expected before the EditItemScreen is displayed.
     *
     *@param midlet The parent ShoppingListMIDlet.
     */
    public EditItemScreen(ShoppingListMIDlet midlet) {
        super("RMS-Shopping List");
        this.midlet = midlet;

        itemName = new TextField("Item", "", TEXT_FIELD_MAX_SIZE, TextField.ANY);
        itemQuantity = new TextField("Quantity", "", TEXT_FIELD_MAX_SIZE, TextField.NUMERIC);

        append(itemName);
        append(itemQuantity);
        //the parent ShoppingListMIDlet acts as the CommandListener for
        //this component, and also handles adding the Commands.
    }

    /**
     * Starts editing an item.  Any changes will be saved to the item itself,
     * not a copy.
     *
     *@param item The item to edit;
     */
    public void editItem(ShoppingItem item) {
        this.item = item;
        itemName.setString(item.getName());
        itemQuantity.setString(""+item.getQuantity());
    }

    /**
     * Saves the details into the original item, and returns it.
     */
    public ShoppingItem saveChanges() {
        item.setDetails(itemName.getString(), Integer.parseInt(itemQuantity.getString()));
        return item;
    }
}
