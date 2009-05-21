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

import org.jahia.content.ContentObject;
import org.jahia.content.ContentTreeStatusInterface;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentField;
import org.jahia.services.usermanager.JahiaUser;

import java.util.*;

/**
 *
 * <p>Title: Concrete visitor for handling Content object revisions at container level</p>
 * <p>Description: </p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class ContainerRevisionsCompositor extends ContentTreeRevisionsVisitor {

    private Locale languageLocale;

    /**
     *
     * @param rootContentObject
     * @param user
     * @param loadRequest
     * @param operationMode
     */
    public ContainerRevisionsCompositor(ContentObject rootContentObject,
                                        JahiaUser user,
                                        EntryLoadRequest loadRequest,
                                        String operationMode){
        super(rootContentObject,user,loadRequest,operationMode);
        this.languageLocale = loadRequest.getFirstLocale(true);
    }

    /**
     * process the current content object when traversing the tree
     *
     * @param contentObject
     * @param currentPageLevel
     * @throws org.jahia.exceptions.JahiaException
     */
    public void processContentObjectBeforeChilds(ContentObject contentObject, int currentPageLevel)
    throws JahiaException {

        ContentTreeStatusInterface contentTreeStatus = (ContentTreeStatusInterface)
            this.getContentTree().getContentTreeStatusStack().peek();

        //check permission
        /*
        if (!contentObject.checkAdminAccess(this.getUser())
            && !contentObject.checkWriteAccess(this.getUser())) {
        */
        if (!contentObject.checkReadAccess(this.getUser())) {
            // stop processing childs too.
            contentTreeStatus.setContinueWithChilds(false);
            contentTreeStatus.setContinueAfterChilds(false);
            return;
        }

        // check to skip revisions of deleted content or not
        boolean hasActiveEntries
                = contentObject.hasActiveEntries();
        if ( !this.isWithDeletedContent() && !hasActiveEntries && !this.isWithStagingRevisions() ){
            // stop processing childs too.
            contentTreeStatus.setContinueWithChilds(false);
            contentTreeStatus.setContinueAfterChilds(false);
            return;
        }

        // skip sub container list revisions
        if ( contentObject instanceof ContentContainerList ){
            // stop processing childs too.
            contentTreeStatus.setContinueWithChilds(false);
            contentTreeStatus.setContinueAfterChilds(false);
            return;
        }

        List revisions = new ArrayList();
        SortedSet entryStates = new TreeSet();
        if ( contentObject.getObjectKey().equals(
                this.getContentTree().getRootContentObject().getObjectKey()) ){
            contentTreeStatus.setContinueAfterChilds(false);
            if ( this.revisionEntryType == METADATA_REVISION_ENTRY ){
                entryStates = contentObject.getMetadataEntryStates();
            } else if ( (this.revisionEntryType & CONTENT_AND_METADATA_REVISION_ENTRY) == 1){
                entryStates = contentObject.getMetadataEntryStates();
            }
        } else {
            if (this.revisionEntryType == CONTENT_REVISION_ENTRY){
                entryStates = contentObject.getEntryStates();
            } else if ( this.revisionEntryType == METADATA_REVISION_ENTRY ){
                entryStates = contentObject.getMetadataEntryStates();
            } else {
                entryStates = contentObject.getEntryStates();
                entryStates.addAll(contentObject.getMetadataEntryStates());
            }
        }
        Iterator iterator = entryStates.iterator();
        while ( iterator.hasNext() ){
            try {
                ContentObjectEntryState entryState =
                        (ContentObjectEntryState)iterator.next();

                // skip staging entry
                /*
                if ( entryState.getWorkflowState()
                     > ContentObjectEntryState.WORKFLOW_STATE_ACTIVE ){
                    continue;
                }*/

                if ( this.getEntryLoadRequest().isVersioned()
                        && entryState.getWorkflowState()>EntryLoadRequest.ACTIVE_WORKFLOW_STATE){
                    continue;
                }

                // check to skip active revisions or not
                if ( !this.isWithActiveRevisions() && entryState.getWorkflowState()
                     == ContentObjectEntryState.WORKFLOW_STATE_ACTIVE ){
                    continue;
                }

                // check to skip deleted revisions or not
                if ( !this.isWithDeletedRevisions() && entryState.getWorkflowState()
                     == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED ){
                    continue;
                }

                if ( inDateRange(entryState) ){

                    // replacing low level revisions to a higher level of revisions
                    // that are more usefull and easier to understand by the end user.
                    // Group all revisions as Container revisions

                    RevisionEntrySet revisionEntrySet = null;

                    RevisionEntry revisionEntry = new RevisionEntry(
                            new ContentObjectEntryState(
                            entryState.getWorkflowState(),
                            entryState.getVersionID(),
                            entryState.getLanguageCode()),
                            ObjectKey.getInstance(contentObject.getObjectKey().getType()
                            + "_" + contentObject.getObjectKey().getIDInType()));

                    if ( contentObject instanceof ContentField ){
                        revisionEntrySet = new ContainerRevisionEntrySet(
                            entryState.getVersionID(), this.getContentTree().getRootContentObject().getObjectKey());
                        setRevisionTitle(revisionEntrySet);
                        addRevision(revisionEntry, revisionEntrySet, revisions);
                        continue;
                    }

                    // default
                    revisionEntrySet = new ContainerRevisionEntrySet( entryState.getVersionID(),
                            ObjectKey.getInstance(contentObject.getObjectKey().getType()
                            + "_" + contentObject.getObjectKey().getIDInType()));
                    setRevisionTitle(revisionEntrySet);
                    addRevision(revisionEntry, revisionEntrySet, revisions);
                }
            } catch ( Exception t ){
                throw new JahiaException("Exception creating revision entry ",
                        "Exception creating revision entry ",
                        JahiaException.DATA_ERROR,
                        JahiaException.DATA_ERROR,t);
            }
        }

        this.getRevisions().addAll(revisions);
    }

    /**
     * Process the Last Content page Field on which, we reached the page level limit when
     * descending in subtree.
     *
     * @param contentObject the last content page field on which we reach the page level limit.
     * @param currentPageLevel
     * @throws JahiaException
     */
    public void processLastContentPageField(
            ContentObject contentObject, int currentPageLevel)
    throws JahiaException{
        processContentObjectBeforeChilds(contentObject, currentPageLevel);
    }

    public Locale getLanguageLocale() {
        return languageLocale;
    }

    public void setLanguageLocale(Locale languageLocale) {
        this.languageLocale = languageLocale;
    }

    /**
     *
     * @param revisionEntry
     * @param revisionEntrySet
     * @param revisions
     */
    protected void addRevision(RevisionEntry revisionEntry,
                               RevisionEntrySet revisionEntrySet,
                               List revisions){
        if ( this.languageLocale == null ||
             ContentObject.SHARED_LANGUAGE.equalsIgnoreCase(revisionEntry.getLanguageCode().toString())
            || this.languageLocale.toString().equals(revisionEntry.getLanguageCode().toString()) ){
            super.addRevision(revisionEntry,revisionEntrySet,revisions);
        }
    }
}
