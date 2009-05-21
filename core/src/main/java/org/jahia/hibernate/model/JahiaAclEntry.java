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
 package org.jahia.hibernate.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import java.io.Serializable;

/**
 * @hibernate.class table="jahia_acl_entries" lazy="false"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaAclEntry implements Cloneable,Serializable {
    public static final int ACL_YES = 0;
    public static final int ACL_NO = 1;
    public static final int ACL_NEUTRAL = 2;
    /**
     * identifier field
     */
    private org.jahia.hibernate.model.JahiaAclEntryPK comp_id;

    /**
     * persistent field
     */
    private int entryState;

    /**
     * persistent field
     */
    private int entryTri;

    /**
     * full constructor
     */
    public JahiaAclEntry(org.jahia.hibernate.model.JahiaAclEntryPK comp_id, int entryStateJahiaAclEntries,
                         int entryTristJahiaAclEntries) {
        this.comp_id = comp_id;
        this.entryState = entryStateJahiaAclEntries;
        this.entryTri = entryTristJahiaAclEntries;
    }

    /**
     * default constructor
     */
    public JahiaAclEntry() {
    }

    public JahiaAclEntry(int entryState, int entryTri) {
        this.entryState = entryState;
        this.entryTri = entryTri;
    }

    /**
     * @hibernate.id generator-class="assigned" unsaved-value="none"
     */
    public org.jahia.hibernate.model.JahiaAclEntryPK getComp_id() {
        return this.comp_id;
    }

    public void setComp_id(org.jahia.hibernate.model.JahiaAclEntryPK comp_id) {
        this.comp_id = comp_id;
    }

    /**
     * @hibernate.property column="entry_state_jahia_acl_entries"
     * length="11"
     * not-null="true"
     */
    public int getEntryState() {
        return this.entryState;
    }

    public void setEntryState(int entryState) {
        this.entryState = entryState;
    }

    /**
     * @hibernate.property column="entry_trist_jahia_acl_entries"
     * length="11"
     * not-null="true"
     */
    public int getEntryTri() {
        return this.entryTri;
    }

    public void setEntryTri(int entryTri) {
        this.entryTri = entryTri;
    }

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

        if ((entryTri & mask) != 0) {
            return ACL_NEUTRAL;
        }

        if ((entryState & mask) != 0) {
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
            entryState |= tmp;
        } else {
            entryState &= ~tmp;
        }

        // Set the tristate bit
        tmp = 1;
        tmp = tmp << permission;

        if (state == ACL_NEUTRAL) {
            entryTri |= tmp;
        } else {
            entryTri &= ~tmp;
        }
    }

    public final void clear () {
        entryState=0;
        entryTri=0xFFFF;
    }
    
    public String toString() {
        return new StringBuffer(getClass().getName())
                .append(": comp_id = ").append(getComp_id())
                .append(", entryState = ").append(getEntryState())
                .append(", entryTri = ").append(getEntryTri())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaAclEntry castOther = (JahiaAclEntry) obj;
            return new EqualsBuilder()
                .append(this.getComp_id(), castOther.getComp_id())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getComp_id())
                .toHashCode();
    }
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
    public boolean hasSameBitsActivated (JahiaAclEntry entry) {
        if (entry != null) {
            int mask = (entry.getEntryState() & entryState);
            if (mask == entry.getEntryState()) {
                return (((entryTri & mask) == 0) && ((entry.getEntryTri() & mask) == 0));
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Clone the ACL Entry.
     *
     * @return Return a new ACL Entry with the same permission sets as the
     *         encapsulated entry.
     */
    public Object clone() throws CloneNotSupportedException {
        return new JahiaAclEntry(comp_id, entryState, entryTri);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
