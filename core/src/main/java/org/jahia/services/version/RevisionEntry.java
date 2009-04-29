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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.jahia.content.ObjectKey;

public class RevisionEntry implements EntryStateable, Comparable {
    
    private static final transient Logger logger = Logger
            .getLogger(RevisionEntry.class);

    public static final String REVISION_TITLE = "RevisionTitle";

    private ContentObjectEntryState entryState;
    private ObjectKey objectKey;

    // Additional Properties, mostly for display purpose
    private Map properties = new HashMap();

    /**
     *
     * @param entryState
     * @param objectKey
     */
    public RevisionEntry(ContentObjectEntryState entryState,
                              ObjectKey objectKey){
        this.entryState = entryState;
        this.objectKey = objectKey;
    }

    public String getLanguageCode(){
        return entryState.getLanguageCode();
    }

    public int getVersionID(){
        return entryState.getVersionID();
    }

    public int getWorkflowState(){
        return entryState.getWorkflowState();
    }

    public ContentObjectEntryState getEntryState(){
        return entryState;
    }

    public ObjectKey getObjectKey(){
        return objectKey;
    }

    public void setProperty(Object name, Object val ){
        this.properties.put(name,val);
    }

    public Object getProperty(Object name){
        return this.properties.get(name);
    }

    /**
     * Returns :
     *            objectKey.toString()
     *            + "_" + entryState.getWorkflowState()
     *            + "_" + entryState.getVersionID()
     *            + "_" + entryState.getLanguageCode()
     * @return
     */
    public String toString(){
        StringBuffer buff = new StringBuffer(objectKey.toString());
        buff.append("_");
        buff.append(entryState.getWorkflowState());
        buff.append("_");
        buff.append(entryState.getVersionID());
        buff.append("_");
        buff.append(entryState.getLanguageCode());
        return buff.toString();
    }

    /**
     * Ignore language codes equality.
     * Compare the revision on the objectkey, the workflow_state and the version_id
     * attributes.
     *
     * @param object
     * @return
     */
    public boolean equals(Object object){

        RevisionEntry revisionEntry = (RevisionEntry)object;
        if ( revisionEntry == null ){
            return false;
        }

        if ( !this.entryState.equals(revisionEntry.getEntryState()) ){
            return false;
        }
        return (getObjectKey().equals(revisionEntry.getObjectKey()));
    }

    public int hashCode(){
        StringBuffer buff = new StringBuffer();
        buff.append(getObjectKey());
        buff.append("_");
        buff.append(this.getVersionID());
        buff.append("_");
        buff.append(this.getWorkflowState());
        return buff.toString().hashCode();
    }
    /**
     *
     * @param revisionEntryKey
     * @return
     */
    public static RevisionEntry getRevisionEntry(String revisionEntryKey){

        RevisionEntry revisionEntry = null;
        try {
            StringTokenizer strToken = new StringTokenizer(revisionEntryKey, "_");
            String objType = "";
            String objID = "";
            String w = "";
            String v = "";
            String l = "";

            if (strToken.countTokens()==5) {
                objType = strToken.nextToken();
                objID = strToken.nextToken();
                w = strToken.nextToken();
                v = strToken.nextToken();
                l = strToken.nextToken();
                ContentObjectEntryState entryState =
                        new ContentObjectEntryState(Integer.parseInt(w),
                        Integer.parseInt(v),l);
                revisionEntry =
                        new RevisionEntry(entryState,
                        ObjectKey.getInstance(objType + "_" + objID));
            }
        } catch ( Exception e ){
            logger.error(e.getMessage(), e);
        }
        return revisionEntry;
    }

    /**
     * To do
     * @param o
     * @return
     */
    public int compareTo(Object o) {
        return 1;
    }
}
