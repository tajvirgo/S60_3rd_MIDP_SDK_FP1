/*
 * @(#)PlayerPermission.java	1.10 06/03/11
 *
 * Copyright 2005, 2006 Sun Microsystems, Inc. All rights reserved.  
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package javax.microedition.media;

import java.io.Serializable;
import java.io.IOException;
import java.security.*;


/**
 * This class is for Media Player permissions.
 * <P>
 * Care should be taken before granting permission to record
 * or make a snapshot.  See the Addendum: Multi-media security for details.
 * <P>
 * The <code>name</code> is the locator of the resource for which snapshot
 * or record is to be allowed.
 * If <code>name</code> is "*" it applies to all locators.
 * If the last character of the locator is "*", 
 * the permission applies to all locators that start with 
 * the name (without the "*").
 * <P>
 * The actions to be granted are passed to the constructor in a string containing
 * a list of one or more comma-separated keywords. The possible keywords are
 * "record" and "snapshot". Their meaning is defined as follows:
 * <P>
 * <DL>
 *    <DT> <tt>record</tt>
 *    <DD> record permission.
 *         Allows {@link javax.microedition.media.control.RecordControl#setRecordLocation} 
 *         and {@link javax.microedition.media.control.RecordControl#setRecordStream} to be used
 *    <DT> <tt>snapshot</tt>
 *    <DD> snapshot permission.
 *         Allows {@link javax.microedition.media.control.VideoControl#getSnapshot}
 *         to be used
 * </DL>
 * <P>
 * The actions string is converted to lowercase before processing.
 * @see java.security.Permission
 * @see java.lang.SecurityManager
 * @serial exclude
 */

public final class PlayerPermission extends Permission {

    /**
     * Record action.
     */
    private final static int RECORD    = 0x1;

    /**
     * Snapshot action.
     */
    private final static int SNAPSHOT   = 0x2;
    /**
     * All actions (RECORD,SNAPSHOT);
     */
    private final static int ALL     = RECORD|SNAPSHOT;
    /**
     * No actions.
     */
    private final static int NONE    = 0x0;

    /**
     * The actions mask.
     *
     */
    private transient int mask;

    /**
     * The actions string.
     *
     * @serial 
     */
    private String actions; // Left null as long as possible, then
                            // created and re-used in the getAction function.

    /**
     * initialize a PlayerPermission object. Common to all constructors.
     * Also called during de-serialization.
     *
     * @param mask the actions mask to use.
     * @throws IllegalArgumentException if the mask contain zero bits or
     *     contains any bits other than RECORD or SNAPSHOT or
     *     if name has a zero length.
     * @throws NullPointerException if name is <code>null</code>.
     */
    private void init(int mask)
    {

	if ((mask & ALL) != mask)
		throw new IllegalArgumentException("invalid actions mask");

	if (mask == NONE)
		throw new IllegalArgumentException("invalid actions mask");

        if (getName().length() == 0) { // Throws NPE if name is null
            throw new IllegalArgumentException("invalid name");
        }
	this.mask = mask;
    }

    /**
     * Creates a new PlayerPermission object with the specified name
     * and actions.  <i>name</i> is the locator to which the permissions apply.
     * <i>actions</i> contains a comma-separated list of the
     * desired actions granted on the property. Possible actions are
     * "record" and "snapshot".
     * @param name the locator to which the permission applies.
     * @param actions the actions string.
     * @throws NullPointerException if <code>name</code> or <code>actions</code>
     * is <code>null</code>.
     * @throws IllegalArgumentException if <CODE>action</CODE> is invalid or
     *    if <code>name</code> is zero length.
     */
    public PlayerPermission(String name, String actions)
    {
	super(name);
	init(getMask(actions));
    }

    /**
     * Checks if this PlayerPermission object "implies" the specified
     * permission.
     * <P>
     * More specifically, this method returns true if:<p>
     * <ul>
     * <li> <i>p</i> is an instanceof PlayerPermission,<p>
     * <li> <i>p</i>'s actions are a subset of this object's actions.
     * <li> <i>p</i>'s name is implied by this object's name:
     *   <ul>
     *    <li>if this object's name does not end in "*" and 
     *        this object's name is equal to <i>p</i>'s name.
     *    <li>if this object's name ends in "*" and
     *        this object's name, without the final "*",
     *        is a prefix of <i>p</i>'s name.
     *   </ul>  
     *      For example, "*" implies any other locator, 
     *      "capture:*" implies "capture://audio".
     * </ul>
     * @param p the permission to check against.
     *
     * @return true if the specified permission is implied by this object,
     * false if not.
     */
    public boolean implies(Permission p) {
	if (!(p instanceof PlayerPermission))
	    return false;

	PlayerPermission that = (PlayerPermission) p;

        // Check of this PlayerPermission's name implies the other's name
        boolean match = false;
        String name = getName();
        if (name.endsWith("*")) {
            // Ends in a wildcard and the rest of this name is a prefix of that's
            match = that.getName().startsWith(name.substring(0,name.length()-1));
        } else {
            // No wildcard, must match exactly
            match = name.equals(that.getName());
        }
	// we get the effective mask. i.e., the "and" of this and that.
	// They must be equal to that.mask for implies to return true.

	return match && ((this.mask & that.mask) == that.mask);
    }


    /**
     * Checks two PlayerPermission objects for equality. Checks that <i>obj</i> is
     * a PlayerPermission, and has the same name and actions as this object.
     * <P>
     * @param obj the object we are testing for equality with this object.
     * @return true if obj is a PlayerPermission, and has the same name and
     * actions as this PlayerPermission object.
     */
    public boolean equals(Object obj) {
	if (obj == this)
	    return true;

	if (! (obj instanceof PlayerPermission))
	    return false;

	PlayerPermission that = (PlayerPermission) obj;
        
        if (getName() != that.getName())
            return false;

	return (this.mask == that.mask);
    }

    /**
     * Returns the hash code value for this object.
     * The value returned complies with the requirements of the
     * the hashCode method of the superclass.
     * @return a hash code value for this object.
     */

    public int hashCode() {
	return this.getName().hashCode();
    }


    /**
     * Converts an actions String to an actions mask.
     *
     * @param action the action string.
     * @return the actions mask.
     */
    private static int getMask(String actions) {

	int mask = NONE;

	char[] a = actions.toCharArray();

	int i = a.length - 1;
	if (i < 0)
	    return mask;

	while (i != -1) {
	    char c;

	    // skip whitespace
	    while ((i!=-1) && ((c = a[i]) == ' ' ||
			       c == '\r' ||
			       c == '\n' ||
			       c == '\f' ||
			       c == '\t'))
		i--;

	    // check for the known strings
	    int matchlen;

	    if (i >= 5 && (a[i-5] == 'r' || a[i-5] == 'R') &&
			  (a[i-4] == 'e' || a[i-4] == 'E') &&
			  (a[i-3] == 'c' || a[i-3] == 'C') &&
			  (a[i-2] == 'o' || a[i-2] == 'O') &&
                          (a[i-1] == 'r' || a[i-1] == 'R') &&
                          (a[i] == 'd' || a[i] == 'D'))
	    {
		matchlen = 6;
		mask |= RECORD;

	    } else if (i >= 7 && (a[i-7] == 's' || a[i-7] == 'S') &&
				 (a[i-6] == 'n' || a[i-6] == 'N') &&
				 (a[i-5] == 'a' || a[i-5] == 'A') &&
				 (a[i-4] == 'p' || a[i-4] == 'P') &&
                                 (a[i-3] == 's' || a[i-3] == 'S') &&
                    		 (a[i-2] == 'h' || a[i-2] == 'H') &&
                    		 (a[i-1] == 'o' || a[i-1] == 'O') &&
				 (a[i] == 't' || a[i] == 'T'))
	    {
		matchlen = 8;
		mask |= SNAPSHOT;

	    } else {
		// parse error
		throw new IllegalArgumentException(
			"invalid permission: " + actions);
	    }

	    // make sure we didn't just match the tail of a word
	    // like "ackbarfaccept".  Also, skip to the comma.
	    boolean seencomma = false;
	    while (i >= matchlen && !seencomma) {
		switch(a[i-matchlen]) {
		case ',':
		    seencomma = true;
		    /*FALLTHROUGH*/
		case ' ': case '\r': case '\n':
		case '\f': case '\t':
		    break;
		default:
		    throw new IllegalArgumentException(
			    "invalid permission: " + actions);
		}
		i--;
	    }

	    // point i at the location of the comma minus one (or -1).
	    i -= matchlen;
	}

	return mask;
    }


    /**
     * Return the canonical string representation of the actions.
     * Always returns present actions in the following order:
     * record, snapshot.
     *
     * @return the canonical string representation of the actions.
     */
    private static String getActions(int mask)
    {
        if (mask == ALL) {
            return "record,snapshot";
        } else if (mask == SNAPSHOT) {
            return "snapshot";
        } else {
            return "record";
        }
    }

    /**
     * Returns the "canonical string representation" of the actions.
     * That is, this method always returns present actions in the following order:
     * record, snapshot. For example, if this PlayerPermission object
     * allows both record and snapshot actions, a call to <code>getActions</code>
     * will return the string "record,snapshot".
     * @return the canonical string representation of the actions.
     */
    public String getActions()
    {
	if (actions == null)
	    actions = getActions(this.mask);

	return actions;
    }

    /**
     * Return the current action mask.
     *
     * @return the actions mask.
     */

    private int getMask() {
	return mask;
    }

    /**
     * WriteObject is called to save the state of the PlayerPermission
     * to a stream. The actions are serialized, and the superclass
     * takes care of the name.
     */

    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
	// Write out the actions. The superclass takes care of the name
	// call getActions to make sure actions field is initialized
	if (actions == null)
	    getActions();
	s.defaultWriteObject();
    }

    /**
     * readObject is called to restore the state of the PlayerPermission from
     * a stream.
     */
    private synchronized void readObject(java.io.ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
	// Read in the action, then initialize the rest
	s.defaultReadObject();
	init(getMask(actions));
    }
}
