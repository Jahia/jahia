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