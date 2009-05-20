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

import java.util.*;

import org.jahia.content.ObjectKey;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.params.ProcessingContext;
import org.jahia.utils.i18n.JahiaResourceBundle;

public abstract class RevisionEntrySet implements Comparable {

    public static final String REVISION_TITLE = "RevisionTitle";

    private int versionID;
    private ObjectKey objectKey;

    // a displayable version number 1, 2, 3, ...
    private int versionNumber;

    // Additional Properties, mostly for display purpose
    private Map properties = new HashMap();

    private Set revisions = new HashSet();

    private Integer forcedWorkflowState = null;

    public RevisionEntrySet(){}

    /**
     *
     * @param versionID
     * @param objectKey
     */
    public RevisionEntrySet(int versionID, ObjectKey objectKey){
        this.versionID = versionID;
        this.objectKey = objectKey;
    }

    public abstract int getWorkflowState();

    public abstract int getRevisionType();

    public abstract String getDescription(String languageCode);

    /**
     * If not null, this value should be taken in place of <code>getWorkflowState()</code>
     *
     * @return
     */
    public Integer getForcedWorkflowState() {
        return forcedWorkflowState;
    }

    /**
     * Used to assign a workflow state independently of what is returned by <code>getWorkflowState()</code>
     *
     * @param forcedWorkflowState
     */
    public void setForcedWorkflowState(Integer forcedWorkflowState) {
        this.forcedWorkflowState = forcedWorkflowState;
    }

    public int getVersionID(){
        return this.versionID;
    }

    public void setVersionID(int versionID){
        this.versionID = versionID;
    }

    public ObjectKey getObjectKey(){
        return objectKey;
    }

    public Set getRevisions(){
        return this.revisions;
    }

    public void addRevision(RevisionEntry revisionEntry){
        if ( revisionEntry != null ) {
            this.revisions.add(revisionEntry);
        }
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
     * @return
     */
    public String toString(){
        StringBuffer buff = new StringBuffer(objectKey.toString());
        buff.append("_");
        buff.append(getWorkflowState());
        buff.append("_");
        buff.append(getVersionID());
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

        RevisionEntrySet revisionEntrySet = (RevisionEntrySet)object;
        if ( revisionEntrySet == null ){
            return false;
        }

        if ( this.getVersionID() != revisionEntrySet.getVersionID() ){
            return false;
        }

        return (getObjectKey().equals(revisionEntrySet.getObjectKey()));
    }

    public int hashCode(){
        return this.toString().hashCode();
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getLastContributor(){
        String lastContributor = (String)this.getProperty(CoreMetadataConstant.LAST_CONTRIBUTOR);
        if ( lastContributor == null ){
            lastContributor = "";
        }
        return lastContributor;
    }

    public String getValidator(){
        String validator = (String)this.getProperty(CoreMetadataConstant.LAST_PUBLISHER);
        if ( validator == null ){
            validator = "";
        }
        return validator;
    }

    public static String getVersionNumber(RevisionEntrySet revEntrySet,
                                        ProcessingContext jParams,
                                        Locale locale)
    {
        String versionNumber = "";
        if ( revEntrySet == null ){
            return "v.?";
        }
        versionNumber = "v." + revEntrySet.getVersionNumber();
        if ( revEntrySet.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
            versionNumber += " - " + JahiaResourceBundle
            .getJahiaInternalResource( "org.jahia.engines.version.liveVersion", locale,"live");
        } else if ( revEntrySet.getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
            versionNumber += " - " + JahiaResourceBundle
            .getJahiaInternalResource( "org.jahia.engines.version.stagingVersion", locale,"staging");
        }
        return versionNumber;
    }

    public static String getWorkflowState(RevisionEntrySet revEntrySet,
                                        ProcessingContext jParams,
                                        Locale locale)
    {
        String workflowState = "";
        if ( revEntrySet.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
            workflowState = JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.liveVersion",
                    locale,"live");
        } else if ( revEntrySet.getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
            workflowState = JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.stagingVersion",
                    locale,"staging");
        } else {
            workflowState = JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.archivedVersion",
                    locale,"archived");
        }
        return workflowState;
    }

    public static String getLastContributor( RevisionEntrySet revEntrySet,
                                ProcessingContext jParams,
                                Locale locale,
                                String unknownKey,
                                String defaultValue){
        String lastContributor = null;
        if ( revEntrySet == null ) {
            return JahiaResourceBundle.getJahiaInternalResource( unknownKey,locale, defaultValue);
        }
        lastContributor = revEntrySet.getLastContributor();
        if ( lastContributor == null || "".equals(lastContributor) ){
            lastContributor = JahiaResourceBundle.getJahiaInternalResource( unknownKey, locale, defaultValue);
        }
        return lastContributor;
    }

    public static String getValidator( RevisionEntrySet revEntrySet,
                                ProcessingContext jParams,
                                Locale locale,
                                String unknownKey,
                                String defaultValue){
        String validator = null;
        if ( revEntrySet == null ) {
            return JahiaResourceBundle.getJahiaInternalResource( unknownKey,locale, defaultValue);
        }
        validator = revEntrySet.getLastContributor();
        if ( validator == null || "".equals(validator) ){
            validator = JahiaResourceBundle.getJahiaInternalResource( unknownKey,locale, defaultValue);
        }
        return validator;
    }

    public static String getVersionDate( RevisionEntrySet revEntrySet, Locale locale){
        final java.util.Date dateVers = new java.util.Date(revEntrySet.getVersionID() * 1000L);
        return java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.LONG,
                java.text.DateFormat.MEDIUM, locale).format(dateVers);
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
