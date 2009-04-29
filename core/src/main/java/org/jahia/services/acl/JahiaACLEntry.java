/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.acl;

import java.io.Serializable;

public class JahiaACLEntry implements Cloneable, Serializable {
    public static final int ACL_YES = 0;
    public static final int ACL_NO = 1;
    public static final int ACL_NEUTRAL = 2;

    private int mState = 0;
    private int mTriState = 0xFFFFFFFF;    // inherite by default


    //-------------------------------------------------------------------------
    /**
     * Default Constructor
     */
    public JahiaACLEntry () {
    }


    //-------------------------------------------------------------------------
    /**
     * Constructor. Before using this constructor, be sure to understand perfectly
     * the meaning of the state and tri-state values. Wrong values will lead to
     * incorrect rights accesses.
     *
     * @param state    State value of the ACL entry.
     * @param tristate Tri-state value of the ACL entry.
     */
    public JahiaACLEntry (int state, int tristate) {
        mState = state;
        mTriState = tristate;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the bit state of the ACL entry.
     *
     * @param permission bit index.
     *
     * @return Return ACL_YES, ACL_NO or ACL_NEUTRAL depending on the bit state.
     */
    public int getPermission (int permission) {
        int mask;

        mask = 1;
        mask = mask << permission;

        if ((mTriState & mask) != 0) {
            return ACL_NEUTRAL;
        }

        if ((mState & mask) != 0) {
            return ACL_YES;
        }
        return ACL_NO;
    }


    //-------------------------------------------------------------------------
    /**
     * Set the new bit state.
     *
     * @param permission Bit index.
     * @param state      Bit state, must be ACL_YES, ACL_NO or ACL_NEUTRAL. All other
     *                   values will be considered as ACL_NO.
     */
    public void setPermission (int permission, int state) {
        int tmp = 0;

        // Set the data bit
        tmp = 1;
        tmp = tmp << permission;

        if (state == ACL_YES) {
            mState |= tmp;
        } else {
            mState &= ~tmp;
        }

        // Set the tristate bit
        tmp = 1;
        tmp = tmp << permission;

        if (state == ACL_NEUTRAL) {
            mTriState |= tmp;
        } else {
            mTriState &= ~tmp;
        }
    }


    //-------------------------------------------------------------------------
    /**
     * Return the state of the entry. Be sure to know the meaning of this value
     * before using it.
     *
     * @return Return the internal state value of the ACL entry.
     */
    public final int getState () {
        return mState;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the tri-state of the entry. Be sure to know the meaning of this
     * value before using it.
     *
     * @return Return the tri-state state value of the ACL entry.
     */
    public final int getTriState () {
        return mTriState;
    }


    //-------------------------------------------------------------------------
    /**
     * Initialize the state and tri-state value of the ACL entry. Before using
     * this method, be sure to understand perfectly the meaning of these values.
     * Incorrect values will lead to incorrect right accesses.
     *
     * @param state    State value.
     * @param tristate Tri-state value.
     */
    public final void set (int state, int tristate) {
        mState = state;
        mTriState = tristate;
    }


    //-------------------------------------------------------------------------
    /**
     * Clear the state and tri-state value of the ACL entry. Calling this method
     * will remove all the permission of the ACL entry.
     */
    public final void clear () {
        set (0, 0xFFFF);
    }

    //-------------------------------------------------------------------------
    /**
     * Return true if the passed ACL entry has the same permission as the
     * encapsulated ACL entry. This method has not the same result as the equal()
     * method.
     *
     * @param entry ACL entry to check.
     *
     * @return Return true if the passed ACL entry has the same permission activated
     *         as the encapsulated ACL entry.
     */
    public boolean hasSameBitsActivated (JahiaACLEntry entry) {
        if (entry != null) {
            int mask = (entry.mState & mState);
            if (mask == entry.mState) {
                return (((mTriState & mask) == 0) && ((entry.mTriState & mask) == 0));
            } else {
                return false;
            }
        }
        return false;
    }

    //-------------------------------------------------------------------------
    /**
     * Return a string representation of the ACL entry content.
     *
     * @return Return the string representation of the ACL entry.
     */
    public String toString () {
        String result = "state=[" + Integer.toBinaryString (mState) +
                "], tristate=[" + Integer.toBinaryString (mTriState) + "]";
        return result;
    }

    //-------------------------------------------------------------------------
    /**
     * Merge specified permissions set as non inherited into the encapsulated
     * ACL.
     *
     * @param entry ACL entry to merge
     */
    public void merge (JahiaACLEntry entry) {
        if (entry != null) {
            mState = mState | entry.getState ();
            mTriState = mTriState & entry.getTriState ();
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Clone the ACL Entry.
     *
     * @return Return a new ACL Entry with the same permission sets as the
     *         encapsulated entry.
     */
    public Object clone () {
        return new JahiaACLEntry (mState, mTriState);
    }
}
