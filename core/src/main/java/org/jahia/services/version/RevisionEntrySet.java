/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
