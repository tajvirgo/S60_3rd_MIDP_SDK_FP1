/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.rms.shopping;

import java.io.*;

/**
 * Encapsulates an item on the list.
 *
 * The implementation is discussed in the package documentation for
 * {@link com.nokia.midp.examples.rms.shopping} .
 */
public class ShoppingItem {
    /** If this is not yet recorded in the RMS, then this.recordId == NOT_IN_DATABASE. */
    public static final int NOT_IN_DATABASE = 0;
    /** Id of the record in the RMS, or NOT_IN_DATABASE. */
    private int recordId;
    /** The name of the item (for example "light bulbs"). */
    private String name;
    /** The number that need to be bought. */
    private int quantity;
    /** False if this is an exact copy of data in the RMS, true if the RMS needs to be updated for this item. */
    boolean modified;

    /**
     * Creates a new blank ShoppingItem.
     */
    public ShoppingItem() {
        this.name = "";
        this.quantity = 1;
        recordId = NOT_IN_DATABASE;
        modified = true;
    }

    /**
     * Creates a new ShoppingItem from the specified byte array.
     *
     *@param record A record from the database.
     */
    public ShoppingItem(int recordId, byte record[]) throws IOException {
        this.recordId = recordId;

        ByteArrayInputStream byteArrayStream = new ByteArrayInputStream(record);
        DataInputStream in = new DataInputStream(byteArrayStream);
        name = in.readUTF();
        quantity = in.readInt();
        in.close();
    }

    /**
     * Sets altered details after editing.
     */
    public void setDetails(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
        modified = true;
    }

    /**
     * Converts this object to a String, suitable for display in the List.
     */
    public String toString() {
        return quantity+"x "+name;
    }

    /**
     * Converts this object to a byte array, suitable for storage in the RMS.
     */
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteArrayStream);
        out.writeUTF(name);
        out.writeInt(quantity);
        out.flush();
        byte record[] = byteArrayStream.toByteArray();
        out.close();

        return record;
    }

    /**
     * Returns the Id of the database record this was loaded from,
     * or {@link #NOT_IN_DATABASE} if it was created by the
     * {@link #ShoppingItem() non-database constructor}.
     */
    public int getRecordId() {
        return recordId;
    }

    /**
     * Returns false if this is an exact copy of data in the RMS, true
     * if the RMS needs to be updated for this item.
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Returns the name (for example "light bulbs").
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the quantity.
     */
    public int getQuantity() {
        return quantity;
    }

}
