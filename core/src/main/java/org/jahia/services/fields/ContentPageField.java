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

package org.jahia.services.fields;

import org.jahia.content.ContentFieldKey;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentPageKey;
import org.jahia.content.ContentObjectKey;
import org.jahia.engines.EngineMessage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.exceptions.JahiaTemplateNotFoundException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.*;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.xml.XMLSerializationOptions;
import org.jahia.utils.xml.XmlWriter;
import org.jahia.utils.LanguageCodeConverters;

import java.util.*;

/**
 * <p>Title: The content field containing page related information. </p>
 * <p>Description: This content element is associated with the JahiaPage
 * object that describes a JahiaPage entry. These may be either hard or
 * soft link, or remote URLs. By hard link we mean that if the link is created
 * or disappears, so does the page it's related to, making this in fact an
 * ownership relationship. We will probably change the model behing this for
 * the next version of Jahia (4.0).</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class ContentPageField extends ContentField {

    private static org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger (ContentPageField.class);

    int pageID = -1;

    protected ContentPageField (Integer ID,
                                Integer jahiaID,
                                Integer pageID,
                                Integer ctnid,
                                Integer fieldDefID,
                                Integer fieldType,
                                Integer connectType,
                                Integer aclID,
                                List<ContentObjectEntryState> activeAndStagingEntryStates,
                                Map<ContentObjectEntryState, String> activeAndStagedDBValues) throws JahiaException {
        super (ID.intValue (), jahiaID.intValue (), pageID.intValue (), ctnid.intValue (), fieldDefID.intValue (),
                fieldType.intValue (), connectType.intValue (), aclID.intValue (), activeAndStagingEntryStates,
                activeAndStagedDBValues);
    }

    /**
     * Set the page to the DB value field.
     *
     * @param pageID The page id in question.
     * @param user   The user actually logged on Jahia.
     *
     * @throws JahiaException
     */
    public void setPageID (int pageID, JahiaUser user, boolean isNew)
            throws JahiaException {
        this.pageID = pageID;
        EntrySaveRequest entrySaveRequest = new EntrySaveRequest (user,
                ContentField.SHARED_LANGUAGE, isNew); // Page field are always in shared language.
        ContentObjectEntryState verInfo = preSet (String.valueOf (pageID), entrySaveRequest);
        logger.debug ("Saving page field..." + verInfo.toString ());
        postSet(entrySaveRequest);
        if (pageID > 0) {
            WorkflowService.getInstance().flushCacheForPageCreatedOrDeleted(new ContentFieldKey(getID()));
        }
    }

    protected void copyEntry (EntryStateable fromEntryState, EntryStateable toEntryState)
            throws JahiaException {
        ContentPage contentPage = getContentPage (fromEntryState);
        if (contentPage != null) {
            EntryLoadRequest loadRequest = new EntryLoadRequest (fromEntryState);
            if ((contentPage.getPageType (loadRequest) == JahiaPage.TYPE_URL) ||
                    (contentPage.getPageType (loadRequest) == JahiaPage.TYPE_LINK)) {
                contentPage.copyEntry (fromEntryState, toEntryState);
            }
        }
        super.copyEntry (fromEntryState, toEntryState);
    }

    protected ActivationTestResults changeEntryState (
            ContentObjectEntryState fromEntryState,
            ContentObjectEntryState toEntryState,
            ProcessingContext jParams, StateModificationContext stateModifContext)
            throws JahiaException {
        ActivationTestResults activationResults = new ActivationTestResults ();
        ContentPage contentPage = getContentPage (fromEntryState, false);
        if (toEntryState.getWorkflowState () == -1) {
            logger.debug ("Attempting to delete page field");
        }

        // this could happen if the database had a jahia_link_only value
        // FIXME NK :
        // In case of page field of link type, they can be created with a value = -1 ( meaning no link )
        // that is why the content page can be null and this case should not abort validating the page field
        /*
        if (contentPage == null) {
            activationResults.setStatus(ActivationTestResults.FAILED_OPERATION_STATUS);
            activationResults.appendError("Field " + getID() + " value is not correctly set. Current value=" + getDBValue(fromEntryState));
            return activationResults;
        }
        */

        if (contentPage == null) {
            return activationResults;
        }

        if ((toEntryState.getWorkflowState () == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) ||
                (toEntryState.getWorkflowState () == EntryLoadRequest.DELETED_WORKFLOW_STATE)) {
            boolean versioningActive = ServicesRegistry.getInstance ().getJahiaVersionService ()
                    .isVersioningEnabled (this.getSiteID ());
            JahiaSaveVersion saveVersion = new JahiaSaveVersion (true, versioningActive,
                    toEntryState.getVersionID ());
            EntryLoadRequest loadRequest = new EntryLoadRequest (fromEntryState);
//            if ((contentPage.getPageType (loadRequest) == JahiaPage.TYPE_DIRECT) &&
//                    contentPage.hasActiveEntries() &&
//                    isMarkedForDelete() && contentPage.isMarkedForDelete()) {
//
//                activationResults.setStatus(ActivationTestResults.FAILED_OPERATION_STATUS);
//                try {
//                    activationResults.appendError (
//                            new IsValidForActivationResults (ContentFieldKey.FIELD_TYPE,
//                                    getID (),
//                                    ContentField.SHARED_LANGUAGE,
//                                    "Cannot validate page deletion until all subpages have been validated"));
//                } catch (ClassNotFoundException cnfe) {
//                    logger.debug ("Error while creating activation test node result",
//                            cnfe);
//                }
//                return activationResults;
//            }
            if ((contentPage.getPageType (loadRequest) == JahiaPage.TYPE_URL) ||
                    (contentPage.getPageType (loadRequest) == JahiaPage.TYPE_LINK)) {
                activationResults =
                        contentPage.activate (
                                stateModifContext.getLanguageCodes (),
                                versioningActive, saveVersion, jParams.getUser (), jParams,
                                stateModifContext);
            } else {
                if (stateModifContext.isDescendingInSubPages ()) {
                    activationResults =
                            contentPage.activate(
                                    stateModifContext.getLanguageCodes (), versioningActive,
                                    saveVersion, jParams.getUser (), jParams,
                                    stateModifContext);
                }
            }
        } else {
            EntryLoadRequest loadRequest = new EntryLoadRequest (fromEntryState);
            if ((contentPage.getPageType (loadRequest) == JahiaPage.TYPE_URL) ||
                    (contentPage.getPageType (loadRequest) == JahiaPage.TYPE_LINK)) {
                contentPage.setWorkflowState (stateModifContext.getLanguageCodes (),
                        toEntryState.getWorkflowState (), jParams, stateModifContext);
            } else {
                if (stateModifContext.isDescendingInSubPages ()) {
                    // TODO: toto/should not work anymore since the isValidForActivation do not recursively check containers and fields
                    // might be possible to call WorkflowService method to get the same result
                    contentPage.setWorkflowState (stateModifContext.getLanguageCodes (),
                            toEntryState.getWorkflowState (), jParams, stateModifContext);
                }
            }
        }
        return activationResults;
    }

    protected ActivationTestResults isContentValidForActivation(
            Set<String> languageCodes,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException {
        ActivationTestResults activationTestResults = new ActivationTestResults();

        if (isMarkedForDelete()) {
            return activationTestResults;
        }

        ContentPage contentPage = null;

        // 1) Try with staged value
        ContentObjectEntryState activeEntryState = null;
        ContentObjectEntryState stagedEntryState = null;

        for (final ContentObjectEntryState entryState : this.getActiveAndStagingEntryStates()) {
            if (entryState.getWorkflowState() > ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
                stagedEntryState = entryState;
            } else {
                activeEntryState = entryState;
            }
        }

        boolean movedPage = false;
        if (stagedEntryState != null) {
            try {
                final String value = this.getValue(stagedEntryState);
                final int pid = Integer.parseInt(value);
                if (value != null &&  pid > 0) {
                    try {
                        contentPage = ContentPage.getPage(pid);
                        if (contentPage != null) {
                            // check if page not currently moved
                            if (ServicesRegistry.getInstance().getJahiaPageService()
                                    .getStagingAndActivePageFieldIDs(contentPage.getID()).size() > 1) {
                                // two different page fields are pointing the same page
                                // It's a moved page !
                                movedPage = true;
                            }
                        }
                    } catch (JahiaPageNotFoundException pnfe) {
                        logger.debug("Page not found." + value);
                        // this is not considered an error
                    }
                }
            } catch (NumberFormatException nfe) {
                logger.debug("Page link seems to have an invalid value.");
                // this is not considered an error because we must be able to
                // validate containers that have "unfilled" values.
            }
        }

        if (!movedPage && activeEntryState != null) {
            try {
                final String value = this.getValue(activeEntryState);
                final int pid = Integer.parseInt(value);
                if (value != null &&  pid > 0) {
                    try {
                        contentPage = ContentPage.getPage(Integer.parseInt(value));
                        if (contentPage != null) {
                            // check if page not currently moved
                            if (ServicesRegistry.getInstance().getJahiaPageService()
                                    .getStagingAndActivePageFieldIDs(contentPage.getID()).size() > 1) {
                                // two different page fields are pointing the same page
                                // It's a moved page !
                                movedPage = true;
                            }
                        }
                    }
                    catch (JahiaPageNotFoundException pnfe) {
                        logger.debug("Page not found." + value);
                        // this is not considered an error
                    }
                }
            } catch (NumberFormatException nfe) {
                logger.debug("Page link seems to have an invalid value.");
                // this is not considered an error because we must be able to
                // validate containers that have "unfilled" values.
            }
        }
/*
        if (movedPage) {
            // the page is currently in a move state, but not activated
            if (!stateModifContext.getStartObject().equals(contentPage.getObjectKey())) {
                activationTestResults.setStatus(ActivationTestResults.FAILED_OPERATION_STATUS);
                try {
                    final int id = getID();
                    final EngineMessage msg = new EngineMessage(
                            "org.jahia.services.fields.ContentPageField.referedToMovedPageError",
                            Integer.toString(id));
                    final IsValidForActivationResults activationResults = new IsValidForActivationResults(
                            ContentFieldKey.FIELD_TYPE, id, jParams.getLocale()
                                    .toString(), msg);
                    activationTestResults.appendError(activationResults);
                } catch (ClassNotFoundException cnfe) {
                    logger.error(cnfe);
                }
                return activationTestResults;
            }
        }
*/
        if (contentPage == null) {
            // this could happen if the database had a jahia_link_only value
            return activationTestResults;
        }

        try {
            // allow loading marked for delete page
            final EntryLoadRequest loadRequest = new EntryLoadRequest(jParams.getEntryLoadRequest().getWorkflowState(),
                            jParams.getEntryLoadRequest().getVersionID(),
                            jParams.getEntryLoadRequest().getLocales(), true);
            final JahiaPage thePage = contentPage.getPage(loadRequest, ProcessingContext.EDIT, jParams.getUser());
            ContentObjectKey mainKey = ServicesRegistry.getInstance().getWorkflowService().getMainLinkObject((ContentObjectKey) getObjectKey());
            
            if (thePage == null) {
                activationTestResults.setStatus (ActivationTestResults.PARTIAL_OPERATION_STATUS);
                try {
                    final EngineMessage msg = new EngineMessage(
                            "org.jahia.services.fields.ContentPageField.pageLookupError",
                            Integer.toString(pageID));
                    final IsValidForActivationResults activationResults = new
                            IsValidForActivationResults(mainKey,jParams.getLocale().toString(), msg);
                    activationTestResults.appendError(activationResults);
                } catch (ClassNotFoundException cnfe) {
                    logger.error(cnfe);
                }
                return activationTestResults;
            }

            if (!stateModifContext.isDescendingInSubPages()) {
                // sub pages is not activated, let's check the page type to know
                // if we must validate or not.
                final int pageType = thePage.getPageType();
                switch (pageType) {
                    case JahiaPage.TYPE_DIRECT :
                        // Page publication is linked to its jahia field - so publication of the field is always ok
                        break;
                    case JahiaPage.TYPE_LINK:
                        JahiaPage linkPage;
                        try {
                            List<Locale> l = new ArrayList<Locale>();
                            for (String lang : languageCodes) {
                                l.add(LanguageCodeConverters.languageCodeToLocale(lang));
                            }
                            EntryLoadRequest elr = new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, l);
                            linkPage = ServicesRegistry.getInstance().getJahiaPageService().
                                    lookupPage(thePage.getPageLinkID(), elr, jParams.getUser(), true);
                        } catch (JahiaPageNotFoundException jpnfe) {
                            linkPage = null;
                        }
                        if (linkPage != null) {
                            if (linkPage.hasActiveEntries() || isMarkedForDelete() ||
                                    stateModifContext.isModifiedObject(WorkflowService.getInstance().getMainLinkObject(new ContentPageKey(linkPage.getID())))) {
                                activationTestResults.setStatus(ActivationTestResults.COMPLETED_OPERATION_STATUS);
                            } else {
                                activationTestResults.setStatus(ActivationTestResults.PARTIAL_OPERATION_STATUS);
                                try {
                                    final EngineMessage msg = new EngineMessage(
                                            "org.jahia.services.fields.ContentPageField.pageOnlyInStagingWarning",
                                            Integer.toString(linkPage.getID()));
                                    final IsValidForActivationResults activationResults = new
                                            IsValidForActivationResults(mainKey,
                                            jParams.getLocale().toString(), msg);
                                    activationTestResults.appendWarning(activationResults);
                                } catch (ClassNotFoundException cnfe) {
                                    logger.error(cnfe);
                                }
                            }
                        } else {
                            activationTestResults.setStatus(ActivationTestResults.PARTIAL_OPERATION_STATUS);
                            try {
                                final EngineMessage msg = new EngineMessage(
                                        "org.jahia.services.fields.ContentPageField.pageLinkNotFoundWarning",
                                        Integer.toString(pageID), Integer.toString(thePage.getPageLinkID()));
                                final IsValidForActivationResults activationResults = new
                                        IsValidForActivationResults(mainKey,
                                        jParams.getLocale().toString(), msg);
                                activationTestResults.appendWarning(activationResults);
                            } catch (ClassNotFoundException cnfe) {
                                logger.error(cnfe);
                            }
                        }
                        break;
                    case JahiaPage.TYPE_URL:
                        break;
                }
            }
        } catch (NumberFormatException nfe) {
            logger.error(nfe);
        }
        return activationTestResults;
    }

    public boolean isShared () {
        return true;
    }

    public String getValue (ProcessingContext jParams, ContentObjectEntryState entryState)
            throws JahiaException {
        return getDBValue (entryState);
    }

    public void setValue (int value, ProcessingContext jParams)
            throws JahiaException {
        EntrySaveRequest entrySaveRequest = new EntrySaveRequest (jParams.getUser (),
                ContentField.SHARED_LANGUAGE); // Page field are always in shared language.
        preSet (String.valueOf (value), entrySaveRequest);
        postSet(entrySaveRequest);
    }

    public JahiaPage getPage (ProcessingContext jParams, EntryLoadRequest loadRequest)
            throws JahiaTemplateNotFoundException, JahiaException {
        return getPage (jParams, loadRequest, true);
    }

    public JahiaPage getPage (ProcessingContext jParams, EntryLoadRequest loadRequest, boolean withTemplate)
            throws JahiaTemplateNotFoundException, JahiaException {
        ContentPage contentPage = getContentPage (jParams, loadRequest, withTemplate);
        if (contentPage == null) {
            return null;
        }
        JahiaPage thePage = contentPage.getPage (loadRequest, (jParams!=null)?jParams.getOperationMode():null, (jParams!=null)?jParams.getUser():null);
        // now we must check whether this page has been moved or not.
        int movedFrom = contentPage.hasSameParentID ();
        if (movedFrom != ContentPage.SAME_PARENT) {
            // page has been moved.
            if (!ProcessingContext.NORMAL.equals (jParams.getOperationMode ())
                    && !ProcessingContext.COMPARE.equals (jParams.getOperationMode ())) {
                // we are in preview or edit mode, let's check where we are
                // before returning the page, because the page "exists" still
                // at two locations, but only one should be returned.
                if (getPageID () == movedFrom) {
                    // we are at the old location, let's return null for
                    // the page.
                    return null;
                }
            }
        }

        return thePage;
    }

    public String getValueForSearch (ProcessingContext jParams,
                                     ContentObjectEntryState entryState)
            throws JahiaException {
        ContentPage contentPage = getContentPage (entryState);
        if (contentPage == null) {
            // this could happen if the database had a jahia_link_only value
            return getDBValue (entryState);
        }
        return contentPage.getTitle (jParams);
    }

    /**
     * This method is called when an entry should be deleted for real.
     * It is called when a field is deleted, and versioning is disabled, or
     * when staging values are undone.
     * For a bigtext for instance, this method should delete the text file
     * corresponding to this field entry
     *
     *
     * @throws JahiaException in case we cannot lookup the page content or
     *                        while deleting the entry (and the whole page in the case that it's the
     *                        last entry)
     */
    public void deleteEntry (EntryStateable deleteEntryState) throws JahiaException {
        ContentPage contentPage = getContentPage (deleteEntryState);

        /**
         * FIXME NK :
         * When markfordeletion is called, the contentPage could be deleted at this point,
         * so why couln't the process continue if the contentpage is null ?
         *
         if (contentPage == null) {
         // this could happen if the database had a jahia_link_only value
         logger.debug("Cannot delete page from jahia_pages_data table because it is a uninitialized page value=" + getDBValue(deleteEntryState));
         return;
         }*/
        if (contentPage != null) {
            contentPage.deleteEntry (deleteEntryState);
        }
        super.deleteEntry (deleteEntryState);
    }

    /**
     * This is called on all content fields to have them serialized only their
     * specific part. The actual field metadata seriliazing is handled by the
     * ContentField class. This method is called multiple times per field
     * according to the workflow state, languages and versioning entries we
     * want to serialize.
     *
     * @param xmlWriter               the XML writer object in which to write the XML output
     * @param xmlSerializationOptions options used to activate/bypass certain
     *                                output of elements.
     * @param entryState              the ContentFieldEntryState for which to generate the
     *                                XML export.
     * @param processingContext               specifies context of serialization, such as current
     *                                user, current request parameters, entry load request, URL generation
     *                                information such as ServerName, ServerPort, ContextPath, etc... URL
     *                                generation is an important part of XML serialization and this is why
     *                                we pass this parameter down, as well as user rights checking.
     *
     * @throws java.io.IOException in case there was an error writing to the Writer
     *                     output object.
     */
    protected void serializeContentToXML (XmlWriter xmlWriter,
                                          XMLSerializationOptions xmlSerializationOptions,
                                          ContentObjectEntryState entryState,
                                          ProcessingContext processingContext) throws java.io.IOException {

        try {
            ContentPage contentPage = getContentPage (entryState);
            if (contentPage == null) {
                // this could happen if the database had a jahia_link_only value
                logger.debug (
                        "Cannot delete page from jahia_pages_data table because it is a uninitialized page value=" + getDBValue (
                                entryState));
                xmlWriter.writeText ("Error while serializing page " + getID () +
                        " to XML : Cannot delete page from jahia_pages_data table because it is a uninitialized page value=" +
                        getDBValue (entryState));
                return;
            }
            contentPage.serializeToXML (xmlWriter, xmlSerializationOptions, processingContext);
        } catch (JahiaException je) {
            logger.debug ("Error while serializing page " + getID () + " to XML", je);
            xmlWriter.writeText ("Error while serializing page " + getID () + " to XML");
        } catch (NumberFormatException nfe) {
            logger.debug ("Error while serializing page " + getID () + " to XML", nfe);
            xmlWriter.writeText ("Error while serializing page " + getID () + " to XML");
        }
    }

    protected Map<String, Integer> getContentLanguageStates (ContentObjectEntryState entryState) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        try {
            EntryLoadRequest loadRequest = new EntryLoadRequest (entryState);
            ContentPage contentPage = getContentPage (entryState, false);
            if (contentPage != null) {
                int pageType = contentPage.getPageType (loadRequest);
                if (/*pageType != JahiaPage.TYPE_DIRECT && */ pageType != -1) {
                    result = contentPage.getLanguagesStates (false);
                }
            }
        } catch (Exception e) {
            logger.debug (
                    "Error while retrieving page content object, pagefieldid=" + this.getID (),
                    e);
        }
        return result;
    }


    public synchronized ActivationTestResults activate(
            Set<String> languageCodes,
            int newVersionID,
            ProcessingContext jParams,
            StateModificationContext stateModifContext) throws JahiaException {
        boolean needBackup = false;
        for (ContentObjectEntryState contentObjectEntryState : getActiveAndStagingEntryStates()) {
            if (contentObjectEntryState.isActive()) {
                if (!"-1".equals(getValue(contentObjectEntryState))) {
                    needBackup = true;
                }
            }
        }
        return super.activate(languageCodes, newVersionID, jParams, stateModifContext, needBackup);
    }

    protected void markContentLanguageForDeletion (JahiaUser user,
                                                   String languageCode,
                                                   StateModificationContext stateModifContext)
            throws JahiaException {

        ContentObjectEntryState entryState =
                new ContentObjectEntryState (
                        ContentObjectEntryState.WORKFLOW_STATE_START_STAGING, 0,
                        ContentObject.SHARED_LANGUAGE);

        // we use entry load request resolving to have the active returned if the staging doesn't exist
        entryState = this.getEntryState (new EntryLoadRequest (entryState));
        if (entryState == null) {
            return;
        }

        ContentPage contentPage = getContentPage (entryState, false);
        if (contentPage == null) {
            return;
        }

        EntryLoadRequest loadRequest = new EntryLoadRequest (entryState);
        if ((contentPage.getPageType (loadRequest) == JahiaPage.TYPE_URL) ||
                (contentPage.getPageType (loadRequest) == JahiaPage.TYPE_LINK)) {
            contentPage.markLanguageForDeletion (user, languageCode, stateModifContext);
        } else {
            if (stateModifContext.isDescendingInSubPages ()) {
                // We have to check here that the start object is not the current content page child
                // to avoid infinite loop.
                if (!contentPage.getObjectKey ().equals (stateModifContext.getStartObject ())) {
                    contentPage.markLanguageForDeletion (user, languageCode, stateModifContext);
                }
            }


            /*
            // The code bellow try to address the issue bellow
            // Issue :
            //        1) Home page activated
            //        2) add ->subpage A1 ( simple), add sub-sub A11, then active from home
            //        3) move A11 to Home ( top menu ) , do not active
            //        4) restore (exact) from home just before move ( last active date ), select only home page !!!
            //       --> A1 is not deleted but not attached anywhere, because it's staged parent page field is marked for delete here
            //
            */

            // check for page move and restore version
            if ( stateModifContext instanceof RestoreVersionStateModificationContext ){
                if ( !stateModifContext.getStartObject().equals(contentPage.getObjectKey()) ){
                    RestoreVersionStateModificationContext smc = (RestoreVersionStateModificationContext)
                         stateModifContext;
                    RestoreVersionStateModificationContext rvsmc =
                        new RestoreVersionStateModificationContext(contentPage.
                        getObjectKey(),
                        stateModifContext.getLanguageCodes(),
                        smc.getEntryState(),smc.isUndelete());
                    // we actually perform a restore version
                    contentPage.restoreVersion(user, ProcessingContext.EDIT,
                                               rvsmc.getEntryState(),
                                               false, false, rvsmc);
                }
            }

        }
    }


    protected void purgeContent () throws JahiaException {

        // first let's retrieve all the different page IDs for this field.
        Map<ContentObjectEntryState, String> dbValues = getAllDBValues ();
        Set<Integer> pageSet = new HashSet<Integer> ();
        for (Map.Entry<ContentObjectEntryState, String> curMapEntry : dbValues.entrySet ()) {
            String curDBValue = curMapEntry.getValue ();
            if (curDBValue != null) {
                int pageID = -1;
                try {
                    pageID = Integer.parseInt (curDBValue);
                } catch (NumberFormatException nfe) {
                    pageID = -1;
                }
                if (pageID != -1) {
                    pageSet.add (new Integer (pageID));
                }
            }
        }

        // now let's remove the related pages. Some of these pages might have
        // already been removed since we are removing pages in active and
        // staging mode. Notably this could occur after a page has been moved.
        for (Integer curPageID : pageSet) {
            ContentPage contentPage;
            try {
                contentPage = ServicesRegistry.getInstance ().getJahiaPageService ().
                        lookupContentPage (curPageID.intValue (), EntryLoadRequest.CURRENT,
                                true);
            } catch (JahiaPageNotFoundException jpnfe) {
                try {
                    contentPage = ServicesRegistry.getInstance ().
                            getJahiaPageService ().
                            lookupContentPage (curPageID.intValue (),
                                    EntryLoadRequest.DELETED, true);
                } catch (JahiaPageNotFoundException jpnfe2) {
                    contentPage = null;
                }
            }
            if (contentPage != null) {
                contentPage.purge (EntryLoadRequest.CURRENT);
            }
        }
    }

    // The two following methods are necessary because we cannot keep a
    // reference to a ContentPage object as this field is a singleton
    // but ContentPage is not ! Do not optimize by making a reference
    // ContentPage object until ContentPage becomes a "singleton" for
    // a single page instance.

    private ContentPage getContentPage (EntryStateable entryState)
            throws NumberFormatException, JahiaException {
        return getContentPage (entryState, true);
    }

    // The two following methods are necessary because we cannot keep a
    // reference to a ContentPage object as this field is a singleton
    // but ContentPage is not ! Do not optimize by making a reference
    // ContentPage object until ContentPage becomes a "singleton" for
    // a single page instance.

    private ContentPage getContentPage (EntryStateable entryState, boolean withTemplate)
            throws NumberFormatException, JahiaException {
        if (entryState == null) {
            return null;
        }
        if (pageID == -1) {
            String dbValue = getDBValue(entryState);
            if(dbValue==null) return null;
            dbValue = dbValue.toLowerCase ();
            if (dbValue.indexOf ("jahia_linkonly") != -1 ||
                    dbValue.indexOf ("empty") != -1) {
                // possible if using an old database format that still had
                // jahia_linkonly values in it's database.
                return null;
            }
            try {
            	pageID = Integer.parseInt (dbValue);
            } catch (NumberFormatException ex) {
            	pageID = -1;
            }
            if (pageID < 0) {
                return null;
            }
        }
        ContentPage contentPage;
        try {
            contentPage =
                    ServicesRegistry.getInstance ().getJahiaPageService ().
                    lookupContentPage (pageID, new EntryLoadRequest (entryState), withTemplate);
        } catch (JahiaPageNotFoundException jpnfe) {
            contentPage = null;
        }
        return contentPage;
    }

    public ContentPage getContentPage(EntryLoadRequest loadRequest) throws JahiaException {
        return getContentPage(getEntryState(loadRequest));
    }

    public ContentPage getContentPage (ProcessingContext jParams)
            throws NumberFormatException, JahiaException {
        return getContentPage (jParams, true);
    }

    public ContentPage getContentPage (ProcessingContext jParams, boolean withTemplate)
            throws NumberFormatException, JahiaException {
        return getContentPage(jParams, (jParams!=null)?jParams.getEntryLoadRequest():null,withTemplate);
    }

    public ContentPage getContentPage (ProcessingContext jParams, EntryLoadRequest loadRequest)
            throws NumberFormatException, JahiaException {
        return getContentPage (jParams, loadRequest, true);
    }

    public ContentPage getContentPage (ProcessingContext jParams, EntryLoadRequest loadRequest, boolean withTemplate)
            throws NumberFormatException, JahiaException {
        if (pageID == -1) {
            String dbValue = getValue (jParams, loadRequest);
            if (dbValue == null) {
                dbValue = "-1";
            } else {
                dbValue = dbValue.toLowerCase ();
            }
            if (dbValue.indexOf ("jahia_linkonly") != -1 ||
                    dbValue.indexOf ("empty") != -1) {
                // possible if using an old database format that still had
                // jahia_link_only values in it's database.
                return null;
            }
            try {
            	pageID = Integer.parseInt (dbValue);
            } catch (NumberFormatException ex) {
            	pageID = -1;
            }
            if (pageID < 0) {
                return null;
            }
        }
        ContentPage contentPage;
        try {
            contentPage = ServicesRegistry.getInstance ().getJahiaPageService ().
                    lookupContentPage (pageID, withTemplate);
        } catch (JahiaTemplateNotFoundException tnf) {
            try {
                logger.debug ("Template not found for page :" + pageID);
                logger.debug ("Try to request page without template :");
                contentPage = ServicesRegistry.getInstance ().getJahiaPageService ().
                        lookupContentPage (pageID, false);
            } catch (JahiaPageNotFoundException jpnfe) {
                contentPage = null;
            }
        } catch (JahiaPageNotFoundException jpnfe) {
            contentPage = null;
        }
        return contentPage;
    }

    public List<ContentObject> getChilds(JahiaUser user,
                               EntryLoadRequest loadRequest
    )
            throws JahiaException {
        List<ContentObject> resultList = new ArrayList<ContentObject> ();
        ContentPage contentPage = null;
        try {
        if (loadRequest != null) {
            ContentObjectEntryState entryState = getEntryState (loadRequest);
            if (entryState != null) {
                if (entryState.getWorkflowState ()
                        != ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {
                    // we don't want to return the child page if the current page field
                    // is marked for delete indicating the page was moved
                    contentPage = getContentPage (entryState, false);
                } else {
                    // was the page deleted or not
                    contentPage = getContentPage (entryState, false);
                    ContentObjectEntryState closestEntryState = contentPage.getClosestVersionedEntryState (
                            entryState);
                    if (contentPage != null && closestEntryState != null &&
                            closestEntryState.getWorkflowState () !=
                            ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {
                        // the page wasn't deleted, check if it's parent field
                        // is different than this field
                        EntryLoadRequest lr = new EntryLoadRequest (closestEntryState);
                        int parentFieldID = contentPage.getParentID (lr);
                        if (parentFieldID != this.getID ()) {
                            // the page was moved and wasn't the child of this current field
                            // so return empty array
                            return resultList;
                        }
                    }
                }
            }
        } else {
            contentPage = this.getContentPage((ContentObjectEntryState)getEntryStates().first());
        }
        } catch ( JahiaPageNotFoundException pnfe ){
            // this exception can if the page doesn't exist in db
            logger.debug("Page not found [" + this.getPageID(), pnfe);
        }
        if (contentPage != null) {
            resultList.add (contentPage);
        }
        return resultList;
    }

    public RestoreVersionTestResults isValidForRestore (JahiaUser user,
                                                        String operationMode,
                                                        ContentObjectEntryState entryState,
                                                        boolean removeMoreRecentActive,
                                                        StateModificationContext stateModificationContext)
            throws JahiaException {
        // first let's check if we have entries that correspond for this
        // page
        RestoreVersionTestResults opResult = new RestoreVersionTestResults ();
        opResult.merge (
                super.isValidForRestore (user, operationMode, entryState,
                        removeMoreRecentActive, stateModificationContext));
        if (opResult.getStatus () == RestoreVersionTestResults.FAILED_OPERATION_STATUS) {
            return opResult;
        }
        ContentPage contentPage = getContentPage (entryState, false);
        if (contentPage != null) {
            opResult.merge (
                    contentPage.isValidForRestore (user, operationMode, entryState,
                            removeMoreRecentActive, stateModificationContext));
        } else {
            /** This test has no meaning because a page field can be created without assigned content page
             opResult.setStatus(RestoreVersionTestResults.FAILED_OPERATION_STATUS);
             opResult.appendError(new RestoreVersionNodeTestResult(getObjectKey(),
             entryState.getLanguageCode(),
             "Couldn't find ContentPage associated with this ContentPageField"));
             */
            opResult.setStatus (RestoreVersionTestResults.PARTIAL_OPERATION_STATUS);
            opResult.appendWarning (new RestoreVersionNodeTestResult (getObjectKey (),
                    entryState.getLanguageCode (),
                    "Couldn't find ContentPage associated with this ContentPageField"));
        }
        return opResult;
    }

    public RestoreVersionTestResults restoreVersion (JahiaUser user,
                                                     String operationMode,
                                                     ContentObjectEntryState entryState,
                                                     boolean removeMoreRecentActive,
                                                     RestoreVersionStateModificationContext stateModificationContext)
            throws JahiaException {

        final RestoreVersionTestResults opResult = new RestoreVersionTestResults ();

        ContentPage contentPage;
        try {
            contentPage = getContentPage (entryState, false);
            if ( contentPage != null && contentPage.hasArchiveEntryState(entryState.getVersionID()) ){
                ContentObjectEntryState closestEntryState =
                contentPage.getClosestVersionedEntryState (entryState);
                EntryLoadRequest loadRequest = new EntryLoadRequest (entryState);
                int pageType = contentPage.getPageType(loadRequest);
                if (closestEntryState != null && closestEntryState.getWorkflowState ()
                        != ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {
                    if ((pageType == JahiaPage.TYPE_URL) ||
                            (pageType == JahiaPage.TYPE_LINK)) {
                        if (!stateModificationContext.isObjectIDInPath (
                                new ContentPageKey (contentPage.getID ()))) {
                            opResult.merge (
                                    contentPage.restoreVersion (user, operationMode,
                                            entryState, removeMoreRecentActive,
                                            stateModificationContext));
                        }
                    } else {
                        if (stateModificationContext.isDescendingInSubPages()) {
                            opResult.merge(contentPage.restoreVersion(user, operationMode,
                                    entryState, removeMoreRecentActive, stateModificationContext));
                        }
                        if (!stateModificationContext.getStartObject().equals(contentPage.getObjectKey())) {
                            // we don't restore this page field if we are not restoring from the page it reffered.
                            return opResult;
                        }
                    }
                } else if ( pageType == JahiaPage.TYPE_DIRECT ){
                    if (!stateModificationContext.getStartObject().equals(contentPage.getObjectKey())) {
                        // we don't restore this page field if we are not restoring from the page it reffered.
                        return opResult;
                    }
                    /*
                    // MOVE ISSUE
                    List locales = new ArrayList();
                    locales.add(LanguageCodeConverters.languageCodeToLocale(entryState.
                            getLanguageCode()));
                    EntryLoadRequest lr =
                            new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,
                                    locales);
                    ContentObject parentField = contentPage.getParent(user, loadRequest,
                            operationMode);
                    if (parentField == null) {
                        loadRequest = new EntryLoadRequest(EntryLoadRequest.
                                ACTIVE_WORKFLOW_STATE, 0, locales);
                        parentField = contentPage.getParent(user, loadRequest, operationMode);
                    }

                    opResult.merge(super.restoreVersion(user, operationMode, entryState,
                            removeMoreRecentActive, stateModificationContext));

                    boolean movedPage = false;
                    movedPage = (parentField != null && parentField.getID() != this.getID());

                    Set languageCodes = new HashSet();
                    languageCodes.add(entryState.getLanguageCode());
                    languageCodes.add(ContentObject.SHARED_LANGUAGE);
                    if (movedPage) {
                        // PAGE_MOVE_LOGIC
                        // page was moved, change current page field to -1.
                        ((ContentPageField) parentField).setValue(-1,
                                Jahia.getThreadParamBean());
                        // @FIXME : is it wanted to delete the whole container on page move....
                        // mark for delete the current parent container
                        ContentObject currentParentContainer =
                                parentField.getParent(user, EntryLoadRequest.STAGED, operationMode);
                        if ( currentParentContainer != null ){
                            currentParentContainer.markLanguageForDeletion(user,ContentObject.SHARED_LANGUAGE,
                                stateModificationContext);
                        }
                    }

                    if ( parentField.getID() != this.getID() ){
                        // ensure to reparent the referred page
                        contentPage.setParentID(this.getPageID(),Jahia.getThreadParamBean());
                        contentPage.commitChanges(true,true,user);
                        contentPage.getACL().setParentID(this.getAclID());
                        contentPage.updateContentPagePath(Jahia.getThreadParamBean());
                        if (ServicesRegistry.getInstance().getWorkflowService().getWorkflowMode(contentPage) == WorkflowService.LINKED) {
                            ServicesRegistry.getInstance().getWorkflowService().setWorkflowMode(contentPage, WorkflowService.INHERITED, null, null, Jahia.getThreadParamBean());
                        }
                    }
                    return opResult;
                    */
                }
            }
        } catch ( Exception t ){
            logger.debug ("Content page not found for field [" + getID () + "]");
        }
        opResult.merge(super.restoreVersion(user, operationMode, entryState,
                removeMoreRecentActive, stateModificationContext));
        /*
        if ( !(this.isMarkedForDelete() || this.isDeletedOrDoesNotExist((new Long(System.currentTimeMillis()/1000L).intValue())))
             && (this.hasActiveEntries()||this.hasStagingEntries()) ){
            // if this page reffers to an internal page link or a direct page and if
            // this page doesn't exist in staging or live, we set this page field to -1
            // to avoid page 404 exception.
            EntryLoadRequest loadRequest = EntryLoadRequest.STAGED;
            loadRequest.setWithDeleted(false);
            loadRequest.setWithMarkedForDeletion(false);
            List v = new ArrayList(this.getActiveAndStagingEntryStates());
            ContentObjectEntryState resolvedEntryState =
                (ContentObjectEntryState) ServicesRegistry.getInstance()
                .getJahiaVersionService().resolveEntry(v, loadRequest);
            if (resolvedEntryState == null) {
                this.setValue( -1, Jahia.getThreadParamBean());
            }
        }*/
        return opResult;
    }

    protected boolean isEntryInitialized (ContentObjectEntryState curEntryState)
        throws JahiaException {
        String entryValue = getDBValue(curEntryState);
        if (entryValue == null) {
            return false;
        }
        return !entryValue.equals("") &&
                !entryValue.equals("<empty>") &&
                !entryValue.equals("-1");
    }

}
