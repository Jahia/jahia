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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jahia.content.ContentObject;
import org.jahia.content.ContentTreeStatusInterface;
import org.jahia.content.ObjectKey;
import org.jahia.content.PageReferenceableInterface;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;

/**
 *
 * <p>Title: Concrete visitor for handling Content object revisions at page level</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class PageRevisionsCompositor extends ContentTreeRevisionsVisitor implements Serializable {

    /**
     *
     * @param rootContentObject
     * @param user
     * @param loadRequest
     * @param operationMode
     */
    public PageRevisionsCompositor(ContentObject rootContentObject,
                                        JahiaUser user,
                                        EntryLoadRequest loadRequest,
                                        String operationMode){
        super(rootContentObject,user,loadRequest,operationMode);
    }

    /**
     * process the current content object when traversing the tree
     *
     * @param contentObject
     * @param currentPageLevel
     * @throws JahiaException
     */
    public void processContentObjectBeforeChilds(ContentObject contentObject, int currentPageLevel)
    throws JahiaException{

        ContentTreeStatusInterface contentTreeStatus = (ContentTreeStatusInterface)
            this.getContentTree().getContentTreeStatusStack().peek();

        //check permission
        if (!contentObject.checkAdminAccess(this.getUser())
            && !contentObject.checkWriteAccess(this.getUser())) {
            // stop processing childs too.
            contentTreeStatus.setContinueWithChilds(false);
            contentTreeStatus.setContinueAfterChilds(false);
            return;
        }

        // check to skip revisions of deleted content or not
        boolean hasActiveEntries
                = contentObject.hasActiveEntries();
        if ( !this.isWithDeletedContent() && !hasActiveEntries && this.getEntryLoadRequest().getWorkflowState()<=1 ){
            // stop processing childs too.
            contentTreeStatus.setContinueWithChilds(false);
            contentTreeStatus.setContinueAfterChilds(false);
            return;
        }

        List revisions = new ArrayList();
        SortedSet entryStates = new TreeSet();
        if (this.revisionEntryType == CONTENT_REVISION_ENTRY){
            if (this.getEntryLoadRequest().isStaging()){
                entryStates = contentObject.getActiveAndStagingEntryStates();
            } else {
                entryStates = contentObject.getEntryStates();
            }
        } else if ( this.revisionEntryType == METADATA_REVISION_ENTRY ){
            entryStates = contentObject.getMetadataEntryStates();
        } else {
            if (this.getEntryLoadRequest().isStaging()){
                entryStates = contentObject.getActiveAndStagingEntryStates();
            } else {
                entryStates = contentObject.getEntryStates();
            }
            entryStates.addAll(contentObject.getMetadataEntryStates());
        }

        List<String> languageCodes = LanguageCodeConverters.localesToLanguageCodes(getEntryLoadRequest().getLocales());

        Iterator iterator = entryStates.iterator();
        while ( iterator.hasNext() ){
            try {
                ContentObjectEntryState entryState =
                        (ContentObjectEntryState)iterator.next();

                // skip staging entry
                if ( !this.isWithStagingRevisions() && entryState.getWorkflowState()
                     > ContentObjectEntryState.WORKFLOW_STATE_ACTIVE ){
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

                if (this.isApplyLanguageFiltering() && languageCodes != null
                        && !languageCodes.contains(entryState.getLanguageCode())){
                    continue;
                }

                if (this.getEntryLoadRequest().isStaging()
                        && entryState.getWorkflowState()<EntryLoadRequest.STAGING_WORKFLOW_STATE){
                    continue;
                }

                if ( inDateRange(entryState) ){

                    // replacing low level revisions to a higher level of revisions
                    // that are more usefull and easier to understand by the end user.
                    // Group all revisions as Page revisions

                    // skip Content Page Field revisions
                    if ( contentObject instanceof ContentPageField ){
                        if ( ((ContentField)contentObject).getContainerID() == 0 ){
                            continue;
                        }
                    }

                    RevisionEntrySet revisionEntrySet = null;

                    RevisionEntry revisionEntry = new RevisionEntry(
                            new ContentObjectEntryState(
                            entryState.getWorkflowState(),
                            entryState.getVersionID(),
                            entryState.getLanguageCode()),
                            ObjectKey.getInstance(contentObject.getObjectKey().getType()
                            + "_" + contentObject.getObjectKey().getIDInType()));

                    if (!checkSkipNotAvailablePageRevisions(contentObject,entryState)){
                        continue;
                    }

                    if ( !(contentObject instanceof ContentPage) ){
                        ContentPage objectPage =
                                ((PageReferenceableInterface)contentObject).getPage();
                        revisionEntrySet = new PageRevisionEntrySet(
                                entryState.getVersionID(),
                                ObjectKey.getInstance(objectPage.getObjectKey().getType()
                                + "_" + objectPage.getObjectKey().getIDInType()));
                        setRevisionTitle(revisionEntrySet);

                        addRevision(revisionEntry, revisionEntrySet, revisions);
                        continue;
                    }

                    /*
                    // Replace ContentField by JahiaContainer revision.
                    try {
                        int ctnID =
                                ((ContentField) contentObject).getContainerID();
                        if ( ctnID != 0 ){
                            JahiaContainer parentContainer =
                                    JahiaContainer.getContainer(ctnID);

                            revisionEntrySet = new RevisionEntrySet(
                                    entryState.getWorkflowState(),
                                    entryState.getVersionID(),
                                    ObjectKey.getInstance(parentContainer.getObjectKey().getType()
                                    + "_" + parentContainer.getObjectKey().getIDInType()));
                            setRevisionTitle(revisionEntrySet);

                            RevisionEntry revisionEntry = new RevisionEntry(
                                    new ContentObjectEntryState(
                                    entryState.getWorkflowState(),
                                    entryState.getVersionID(),
                                    entryState.getLanguageCode()),
                                    ObjectKey.getInstance(parentContainer.getObjectKey().getType()
                                    + "_" + parentContainer.getObjectKey().getIDInType()));

                            addRevision(revisionEntry, revisionEntrySet, revisions);
                            continue;
                        }
                    } catch ( Exception e ){
                        logger.error(e.getMessage(), e);
                    }
                    */

                    // default
                    revisionEntrySet = new PageRevisionEntrySet( entryState.getVersionID(),
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
     *
     * @param contentObject
     * @param entryState
     * @return
     * @throws JahiaException
     */
    private boolean checkSkipNotAvailablePageRevisions(ContentObject contentObject,
                                                       ContentObjectEntryState entryState)
    throws JahiaException {
        if (this.isSkipNotAvailablePageRevisions()){
            return true;
        }
        ContentPage contentPage = null;
        if ( !(contentObject instanceof ContentPage) ){
            contentPage = ((PageReferenceableInterface)contentObject).getPage();
        } else {
            contentPage = (ContentPage)contentObject;
        }
        if (entryState.getVersionID()<=0){
            return true;
        }
        return !this.isPageDeletedOrDoesNotExist(contentPage,entryState.getVersionID());
    }
}
