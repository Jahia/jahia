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
package org.jahia.services.version;

import java.io.Serializable;



/**
 * This class is just a bean holding one "entry state information" entity
 * concerning a single field
 */

/** @todo This class should be moved to version package and rename it 'EntryState' */
public class ContentObjectEntryState implements EntryStateable, Comparable, Cloneable, Serializable
{
    public static final int WORKFLOW_STATE_ACTIVE = 1; // active state
    public static final int WORKFLOW_STATE_START_STAGING = 2; // start status for staging/wf
    public static final int WORKFLOW_STATE_VERSIONING_DELETED = -1; // delete flag
    public static final int WORKFLOW_STATE_VERSIONED = 0; // versioned (archived) state

    private int workflowState;
    private int versionID;
    private String languageCode;

    public ContentObjectEntryState (int workflowState, int versionID, String languageCode)
    {
        this.workflowState = workflowState;
        this.versionID = versionID;
        this.languageCode = languageCode;
    }

    /**
     * Copy constructor.
     * @param entryStateable
     */
    public ContentObjectEntryState (EntryStateable entryStateable) {
        this.workflowState = entryStateable.getWorkflowState();
        this.versionID = entryStateable.getVersionID();
        this.languageCode = entryStateable.getLanguageCode();
    }

    public int getWorkflowState()   { return workflowState; }
    public int getVersionID()       { return versionID;     }
    public String getLanguageCode() { return languageCode;  }

    protected void setVersionID(int versionID)
       { this.versionID = versionID;  }

    public boolean isActive()       { return (workflowState == 1); }
    public boolean isStaging()      { return (workflowState > 1); }
    public boolean isVersioning()   { return (workflowState < 1); }

    /**
     * Needed when a ContentFieldVersionInfo is used as a Map key
     */
    public boolean equals(Object obj){
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final ContentObjectEntryState castOther = (ContentObjectEntryState) obj;
            return ((castOther.getWorkflowState() == this.workflowState) && 
                    (castOther.getVersionID() == this.versionID) &&
                    (this.languageCode.equals(castOther.getLanguageCode())));
        }
        return false;
    }

    /**
     * Comparator use mostly to sort ContentFieldEntryState when used to
     * display them or other operations. The keys for sorting are the
     * following :
     * 1. Workflow state
     * 2. Version ID
     * 3. Language code
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    public int compareTo(Object o) {
        ContentObjectEntryState rightEntryState = (ContentObjectEntryState) o;
        if (getWorkflowState() != rightEntryState.getWorkflowState()) {
            Integer leftState = new Integer(getWorkflowState());
            Integer rightState = new Integer(rightEntryState.getWorkflowState());
            return leftState.compareTo(rightState);
        } else {
            if (getVersionID() != rightEntryState.getVersionID()) {
                // version IDs are different
                Integer leftVersionID = new Integer(getVersionID());
                Integer rightVersionID = new Integer(rightEntryState.getVersionID());
                return leftVersionID.compareTo(rightVersionID);
            } else {
                // version IDs are equals, let's compare the language codes
                return getLanguageCode().compareTo(rightEntryState.getLanguageCode());
            }
        }
    }

    /**
     * Needed when a ContentFieldVersionInfo is used as a Map key
     */
    public int hashCode()
    {
        return languageCode.hashCode()+versionID+workflowState+1237*workflowState;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer("[ENTRY:workFlowState=");
        buf.append(getWorkflowState());
        buf.append(", versionID=");
        buf.append(getVersionID());
        buf.append(", languageCode=");
        buf.append(getLanguageCode());
        buf.append("]");
        return buf.toString();
    }


    /**
     * Returns the workflow state depending of the version ID
     * @param versionID
     * @return
     */
    public static int getWorkflowState(int versionID){
        int ws = WORKFLOW_STATE_VERSIONED;
        switch ( versionID )
        {
            case -1 : return WORKFLOW_STATE_START_STAGING;
            case 0 : return WORKFLOW_STATE_START_STAGING;
            case 1 : return WORKFLOW_STATE_ACTIVE;
        }
        return ws;
    }

    /**
     * Return an entry state for a given versionID and a languageCode
     *
     * @param versionID 1 = active, -1 or 0 = staging, other
     * @param languageCode
     * @return
     */
    public static ContentObjectEntryState getEntryState(int versionID, String languageCode){

        int ws = WORKFLOW_STATE_VERSIONED;
        int v = versionID;

        switch ( versionID )
        {
            case -1 : ws = WORKFLOW_STATE_START_STAGING;
                      v = 0;
                      break;
            case 0  : ws = WORKFLOW_STATE_START_STAGING;
                      v = 0;
                      break;
            case 1  : ws = WORKFLOW_STATE_ACTIVE;
                      v = 0;
        }
        return new ContentObjectEntryState(ws,v,languageCode);
    }

    public Object clone () {
        return new ContentObjectEntryState (this.getWorkflowState(),
                                            this.getVersionID(),
                                            this.getLanguageCode());
    }

}