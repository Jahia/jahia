/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
