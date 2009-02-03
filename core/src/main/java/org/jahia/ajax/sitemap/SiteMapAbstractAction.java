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

package org.jahia.ajax.sitemap;

import org.jahia.ajax.AjaxAction;
import org.jahia.bin.Jahia;
import org.jahia.content.*;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.data.viewhelper.sitemap.PagesFilter;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.selectpage.SelectPage_Engine;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaObjectDelegate;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.fields.ContentSmallTextField;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.timebasedpublishing.RetentionRule;
import org.jahia.services.timebasedpublishing.TimeBasedPublishingService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaVersionService;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.views.engines.JahiaEngineViewHelper;
import org.jahia.views.engines.versioning.pages.PagesVersioningViewHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Abstract Class that simply holds common methods regarding SiteMaps.
 *
 * @author Xavier Lawrence
 */
public abstract class SiteMapAbstractAction extends AjaxAction {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(SiteMapAbstractAction.class);

    protected static final WorkflowService workflowService = servicesRegistry.getWorkflowService();
    protected static final LockService lockRegistry = servicesRegistry.getLockService();
    protected static final JahiaVersionService jahiaVersionService = servicesRegistry.getJahiaVersionService();
    protected static final JahiaACLManagerService aclService = servicesRegistry.getJahiaACLManagerService();

    protected static final String DISPLAY_PARAM = "display";
    protected static final String INACTIVE = "Inactive";
    protected static final String STANDARD_IMAGE = "7";
    protected static final String N_STEP_IMAGE = "8";
    protected static final String NO_WFLOW_IMAGE = "9";
    protected static final String LINKED_IMAGE = "26";
    protected static final int NORMAL = 1;
    protected static final int VERSIONNING = 2;

    protected static final String TBP_EXPIRED_BUT_WILL_BECOME_VALID = "20";
    protected static final String TBP_EXPIRED = "21";
    protected static final String TBP_VALID_BUT_WILL_EXPIRE = "22";
    protected static final String TBP_VALID = "23";
    protected static final String TBP_NOT_VALID_BUT_WILL_BECOME_VALID = "24";
    protected static final String TBP_UNKNOWN = "25";

    /**
     * Gets all the direct child objects of a given ContentObject, that have a separate WorkFlow associated to them.
     *
     * @param object      The parent ContentObject to search children from
     * @param currentUser The current logged Jahia user
     */
    protected Collection getChildObjects(final ContentObject object,
                                         final JahiaUser currentUser) throws JahiaException {
        if (object.getPickedObject() == null) {
            final List linked = workflowService.getLinkedContentObjects(object, false);
            final List linkedPages = new ArrayList();
            for (Object aLinked : linked) {
                final ContentObject contentObject = (ContentObject) aLinked;
                if (contentObject.getObjectKey().getType().equals(ContentPageKey.PAGE_TYPE)) {
                    final ContentPage contentPage = (ContentPage) contentObject;
                    if (contentPage.getPageType(EntryLoadRequest.STAGED) == JahiaPage.TYPE_DIRECT) {
                        if (contentPage.getParentID(EntryLoadRequest.STAGED) == object.getPageID()) {
                            linkedPages.add(contentObject);
                        }
                    }
                }
            }

            final List fieldsHere = new ArrayList();
            final List unlinked = workflowService.getUnlinkedContentObjects(object);
            unlinked.addAll(linkedPages);
            for (Object anUnlinked : unlinked) {
                final ContentObject contentObject = (ContentObject) anUnlinked;
                final int pageId = contentObject.getPageID();
                if (!contentObject.getObjectKey().getType().equals(ContentPageKey.PAGE_TYPE)
                        || (((ContentPage) contentObject).getPageType(EntryLoadRequest.STAGED) == JahiaPage.TYPE_DIRECT
                        && object.getID() != pageId)) {
                    fieldsHere.add(contentObject);
                }
            }

            // v contains all the ContentObjects linked to a WorkFlow
            final List v = new ArrayList();

            for (Object aFieldsHere : fieldsHere) {
                final ContentObject contentObject = (ContentObject) aFieldsHere;
                if (contentObject.checkReadAccess(currentUser) && contentObject.hasActiveOrStagingEntries()) {
                    if (!(contentObject.getObjectKey().getType().equals(ContentPageKey.PAGE_TYPE))) {
                        final ContentObject main = workflowService.getMainLinkObject(contentObject);
                        // check for cyclic situation
                        //if ( main != null && !main.getObjectKey().equals(object.getObjectKey()) ){
                        if (main == null) {
                            logger.warn("Main object is null for the content object: " + contentObject.getObjectKey());
                            continue;
                        }
                        if ((main instanceof ContentPage && ((ContentPage) main).getParentID(EntryLoadRequest.STAGED) != object.getPageID()) ||
                                (!(main instanceof ContentPage) && main.getPageID() != object.getPageID())) {
                            continue;
                        }

                        v.add(main);
                        //}
                    } else {
                        v.add(contentObject);
                    }
                }
            }

            return v;
        } else return new ArrayList();
    }

    /**
     * Returns true if the given object has at least 1 accessible child for the given user.
     *
     * @param object      The ContentObject to search children from
     * @param currentUser The current logged Jahia user
     */
    public boolean hasChildren(final ContentObject object,
                               final JahiaUser currentUser) throws JahiaException {
        return !getChildObjects(object, currentUser).isEmpty();
    }

    /**
     * Process a page
     */
    protected void processPage(final ContentPage page,
                               final ProcessingContext jParams,
                               final HttpSession session,
                               final JahiaUser currentUser,
                               final List locales,
                               final Document resp,
                               final Element root,
                               final int loadFlags,
                               final int parentID,
                               final int mode,
                               final PagesFilter pagesFilter) throws JahiaException {

        // Moved page, only show the actual moved page, not where it was previously
        if ((page.getParentID(jParams) != parentID) && (page.getID() != parentID)) {
            return;
        }

        if (!page.checkReadAccess(currentUser)) {
            return;
        }

        final Map titles;
        final String selectedPageOperation = (String) session.getAttribute("selectedPageOperation");
        String entryPoint = (String) session.getAttribute("Select_Page_Entry");
        final boolean moveOp = SelectPage_Engine.MOVE_OPERATION.equals(selectedPageOperation);
        final boolean isLiveMode = jParams.getOperationMode().equals(ProcessingContext.NORMAL);
        if (entryPoint == null && moveOp) {
            entryPoint = String.valueOf(jParams.getPageID());
        }
        final int pid = page.getID();
        final int opMode;

        if (isLiveMode) {
            if (page.getActiveVersionID() <= 0 || !page.isAvailable()) return;
            titles = page.getTitles(ContentPage.ACTIVATED_PAGE_TITLES);
            opMode = EntryLoadRequest.ACTIVE_WORKFLOW_STATE;

        } else {
            if (page.getActiveVersionID() <= 0 && !page.checkWriteAccess(currentUser)) return;
            titles = page.getTitles(ContentPage.LAST_UPDATED_TITLES);
            opMode = EntryLoadRequest.STAGING_WORKFLOW_STATE;
        }

        final Map engineMap = (Map) jParams.getSessionState().getAttribute("jahia_session_engineMap");
        final Locale currentLocale;
        if (engineMap == null) {
            currentLocale = jParams.getLocale();

        } else {
            EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
            if (elh == null) {
                elh = new EngineLanguageHelper(jParams.getLocale());
            }
            currentLocale = elh.getCurrentLocale();
        }

        String pageTitle1 = (String) titles.get(currentLocale.toString());
        final int siteMapTitlesLength = org.jahia.settings.SettingsBean.getInstance().getSiteMapTitlesLength();
        if (pageTitle1 == null || pageTitle1.length() == 0) {
            pageTitle1 = "N.A.";
        } else if (pageTitle1.length() > siteMapTitlesLength) {
            pageTitle1 = pageTitle1.substring(0, siteMapTitlesLength) + "...";
        }

        if (!page.isAvailable()) {
            final String labelResourceName = "org.jahia.engines.timebasedpublishing.timebpstatus." +
                    page.getTimeBasedPublishingState() + ".label";
            pageTitle1 += " (" + JahiaResourceBundle.getEngineResource(labelResourceName, jParams, currentLocale) + ")";
        }

        if (isLiveMode && "N.A.".equals(pageTitle1)) return;
        final Element item = resp.createElement("page");

        if (moveOp && (String.valueOf(pid).equals(entryPoint))) {
            pageTitle1 += " (" + JahiaResourceBundle.
                    getEngineResource("org.jahia.engines.selectpage.SelectPage_Engine.insertionPoint.label",
                            jParams, currentLocale) + ")";
            item.setAttribute("Disable", "Disable");
        }

        final List directParents = (List) session.getAttribute("InsertionPointParents");
        final ObjectKey key = page.getObjectKey();
        if (moveOp && (String.valueOf(parentID).equals(entryPoint) ||
                key.equals(jParams.getSite().getHomeContentPage().getObjectKey())) ||
                (directParents != null && directParents.contains(key.toString()))) {
            item.setAttribute("Disable", "Disable");
        }

        if (moveOp) {
            // Only check locks for a move operation
            final LockKey k = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE, pid);
            final JahiaUser user = jParams.getUser();
            if (!lockRegistry.isAcquireable(k, user, user.getUserKey()) || !page.checkWriteAccess(currentUser)) {
                item.setAttribute("Disable", "Disable");
            }
        }

        if (pagesFilter != null) {
            if (pagesFilter.filterForSelection(page, jParams)) {
                item.setAttribute("Disable", "Disable");
            }
        }

        item.setAttribute("id", String.valueOf(pid));
        item.setAttribute("key", page.getObjectKey().toString());
        item.setAttribute("title", pageTitle1);
        item.setAttribute("url", getPageURL(jParams, pid, currentLocale.toString()));
        item.setAttribute("parentID", String.valueOf(parentID));

        if (!page.getContentPageChilds(currentUser, loadFlags, null, true).hasNext()) {
            item.setAttribute("NoChildren", "NoChildren");
        }

        final SortedSet entries;
        final Map languagesStates;
        if (isLiveMode) {
            entries = null;
            languagesStates = null;
        } else {
            entries = page.getEntryStates();
            languagesStates = workflowService.getLanguagesStates(page);
        }

        for (Object locale : locales) {
            final String languageCode = ((SiteLanguageSettings) locale).getCode();
            if (logger.isDebugEnabled()) {
                logger.debug("Found page: " + page.getID() + " in " + languageCode);
            }
            final Element lang = resp.createElement("lang");
            lang.setAttribute("code", languageCode);
            lang.setAttribute("langURL", getPageURL(jParams, pid, languageCode));

            if (isLiveMode) {
                if (page.isReachable(ProcessingContext.NORMAL, languageCode, currentUser)) {
                    lang.appendChild(resp.createTextNode("10"));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Page reachable in Live Mode");
                    }
                } else {
                    lang.appendChild(resp.createTextNode("-1"));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Page Not reachable in Live Mode");
                    }
                }

            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("getEntryStates: " + entries);
                }
                Integer languageState = (Integer) languagesStates.get(languageCode);

                if (mode == NORMAL) {
                    if (languageState != null && languageState == EntryLoadRequest.STAGING_WORKFLOW_STATE) {
                        for (Object entry : entries) {
                            final ContentObjectEntryState entryState = (ContentObjectEntryState) entry;
                            if (!entryState.getLanguageCode().equals(languageCode)) continue;
                            if (entryState.getWorkflowState() != opMode) continue;
                            if (entryState.getVersionID() == 0 &&
                                    jParams.getOperationMode().equals(ProcessingContext.NORMAL)) {
                                languageState = null;
                            } else if (entryState.getVersionID() < 0) {
                                return;
                            }
                        }
                    }

                    final String langTitle = (String) titles.get(languageCode);
                    if (languageState == null || languageState == -1 || langTitle == null || langTitle.length() == 0) {
                        lang.appendChild(resp.createTextNode("-1"));
                    } else {
                        String extState = languageState.toString();
                        try {
                            extState = workflowService.getExtendedWorkflowState(page, languageCode);
                        } catch (Exception e) {
                            logger.error(
                                    "Unable to retrieve extended workflow state for object: "
                                            + page.getObjectKey()
                                            + " and language: " + languageCode,
                                    e);
                        }
                        lang.appendChild(resp.createTextNode(extState));
                    }

                } else {
                    // Versionning logic
                    final int now = jahiaVersionService.getCurrentVersionID();

                    final PagesVersioningViewHelper pagesVersViewHelper = (PagesVersioningViewHelper) session.
                            getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
                    final int restoreVersionID = (int) (pagesVersViewHelper.getRestoreDateCalendar().getDateLong() / 1000);

                    final boolean isPageMarkedForDeletion = page.isMarkedForDelete();

                    // check that we can undo the page that exist only in staging
                    final boolean existOnlyInStaging = !(page.hasArchiveEntryState(now, false));

                    // check if the content object exists before a given date
                    final boolean hasArchiveEntryBeforeRestoreDate = page.hasArchiveEntryState(restoreVersionID);

                    final boolean hasActive = page.hasActiveEntries();

                    final EntryLoadRequest loadRequest = new EntryLoadRequest(0, restoreVersionID, new ArrayList());
                    loadRequest.getLocales().add(EntryLoadRequest.SHARED_LANG_LOCALE);
                    loadRequest.setWithMarkedForDeletion(true);
                    ContentObjectEntryState closestEntryState = new ContentObjectEntryState(
                            ContentObjectEntryState.WORKFLOW_STATE_VERSIONED, restoreVersionID, ContentObject.SHARED_LANGUAGE);
                    closestEntryState = page.getClosestVersionedEntryState(closestEntryState);

                    // will be deleted if restore at given date
                    final boolean wasDeleted = (closestEntryState != null && closestEntryState.getWorkflowState() == -1 &&
                            page.wasDeleted(closestEntryState.getVersionID()));

                    boolean isDeleted = (isPageMarkedForDeletion || (!page.hasStagingEntries() && !page.hasActiveEntries()));

                    if (isDeleted) {
                        item.setAttribute("Style", "MarkedForDelete");
                    }

                    if ((pagesVersViewHelper.getOperationType() == 2)
                            && ((!hasArchiveEntryBeforeRestoreDate && restoreVersionID > 0) || wasDeleted)) {
                        // Added after the restore date
                        if (!isDeleted) {
                            item.setAttribute("Style", "blueColor");
                        } else {
                            item.setAttribute("Style", "MarkedForDeleteAndBlueColor");
                        }
                    }

                    if ((pagesVersViewHelper.getOperationType() == 1) && !hasActive &&
                            !page.hasArchiveEntryState(jahiaVersionService.getCurrentVersionID())) {
                        // Going to be deleted if apply an undo staging !!!!
                        item.setAttribute("Style", "blueColor");
                    } else if (pagesVersViewHelper.getOperationType() == 1 && !hasActive && !isDeleted) {
                        item.setAttribute("Style", "redColor");
                    }

                    final Map pageOnlyLanguagesStates = page.getLanguagesStates(false);

                    final Integer sharedLanguageState = (Integer) languagesStates.get(ContentObject.SHARED_LANGUAGE);
                    final Integer pageLanguageState = (Integer) pageOnlyLanguagesStates.get(languageCode);

                    if (languageState != null && languageState != -1) {
                        if (sharedLanguageState != null && languageState < sharedLanguageState) {
                            languageState = sharedLanguageState;
                        }
                        if (languageState > EntryLoadRequest.STAGING_WORKFLOW_STATE) {
                            if (pageLanguageState == null || pageLanguageState < languageState) {
                                // the page cannot be in waiting for approval if it has no entry ( jahia page info ) at waiting for approval state.
                                languageState = EntryLoadRequest.STAGING_WORKFLOW_STATE;
                            }
                        }
                        final boolean isStaging = languageState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE;
                        final boolean isLocked;
                        if (jParams.settings().areLocksActivated()) {
                            final LockKey lockKey = LockKey.composeLockKey(LockKey.WORKFLOW_ACTION + "_" +
                                    ContentPageKey.PAGE_TYPE, page.getID());
                            final LockService lockService = ServicesRegistry.getInstance().getLockService();
                            isLocked = !lockService.acquire(lockKey, currentUser,
                                    currentUser.getUserKey(),
                                    jParams.getSessionState().getMaxInactiveInterval());
                            if (isLocked) {
                                item.setAttribute("locked", "locked");
                            } else {
                                Set locks = (Set) jParams.getSessionState().getAttribute("VersionningLocks");
                                if (locks == null) {
                                    locks = new HashSet();
                                }
                                locks.add(lockKey);
                                jParams.getSessionState().setAttribute("VersionningLocks", locks);
                            }
                        } else {
                            isLocked = false;
                        }

                        final boolean allowPageSelection = page.checkWriteAccess(jParams.getUser());
                        final Set selectedPages = pagesVersViewHelper.getSelectedPages();

                        if (pagesVersViewHelper.getOperationType() == 1) { // Undo Staging
                            if (isLocked || !allowPageSelection || existOnlyInStaging || !isStaging) {
                                lang.setAttribute("Disable", "Disable");
                            }
                            if (!isLocked && isStaging && hasArchiveEntryBeforeRestoreDate &&
                                    selectedPages.contains(page.getObjectKey())) {
                                lang.setAttribute("checked", "checked");
                            }
                        } else if (pagesVersViewHelper.getOperationType() == 2) { // Restore Archive
                            if (isLocked || !allowPageSelection || !hasArchiveEntryBeforeRestoreDate || wasDeleted) {
                                lang.setAttribute("Disable", "Disable");
                            }
                            if (!isLocked && hasArchiveEntryBeforeRestoreDate && !wasDeleted &&
                                    selectedPages.contains(page.getObjectKey())) {
                                lang.setAttribute("checked", "checked");
                            }
                        } else { // page undelete
                            if (!allowPageSelection || !isDeleted) {
                                lang.setAttribute("Disable", "Disable");
                            }
                            if (isDeleted && selectedPages.contains(page.getObjectKey())) {
                                lang.setAttribute("checked", "checked");
                            }
                        }

                        if (languageState < 1 && isDeleted) {
                            lang.appendChild(resp.createTextNode("-2"));
                            if (logger.isDebugEnabled()) {
                                logger.debug("Page: " + key + " is deleted");
                            }
                        } else {
                            lang.appendChild(resp.createTextNode(languageState.toString()));

                        }
                    } else {
                        lang.appendChild(resp.createTextNode("-3"));
                        lang.setAttribute("Disable", "Disable");
                    }
                }
            }
            item.appendChild(lang);
        }
        if (item != null) {
            processTimeBasedPublishing(page, jParams, item);
        }
        root.appendChild(item);
    }

    protected void processTimeBasedPublishing(ContentObject contentObject, ProcessingContext jParams, Element item) {
        try {
            final JahiaObjectManager jahiaObjectManager =
                    (JahiaObjectManager) SpringContextSingleton.getInstance()
                            .getContext().getBean(JahiaObjectManager.class.getName());

            final JahiaObjectDelegate jahiaObjectDelegate =
                    jahiaObjectManager.getJahiaObjectDelegate(contentObject.getObjectKey());
            TimeBasedPublishingService tbpService = ServicesRegistry.getInstance()
                    .getTimeBasedPublishingService();
            final RetentionRule retRule = tbpService.getRetentionRule(contentObject.getObjectKey());
            final long now = System.currentTimeMillis();
            String statusLabel = "";
            if (retRule != null) {
                final boolean isValid = jahiaObjectDelegate.isValid();
                final boolean isExpired = jahiaObjectDelegate.isExpired();
                final boolean willExpire = jahiaObjectDelegate.willExpire(now);
                final boolean willBecomeValid = jahiaObjectDelegate.willBecomeValid(now);
                if (isExpired) {
                    if (willBecomeValid) {
                        statusLabel = TBP_EXPIRED_BUT_WILL_BECOME_VALID; // yellow
                    } else {
                        statusLabel = TBP_EXPIRED; // red
                    }
                } else if (isValid) {
                    if (willExpire) {
                        statusLabel = TBP_VALID_BUT_WILL_EXPIRE; // orange
                    } else {
                        statusLabel = TBP_VALID; // green
                    }
                } else {
                    if (willBecomeValid) {
                        statusLabel = TBP_NOT_VALID_BUT_WILL_BECOME_VALID; // yellow
                    } else {
                        // is not valid
                        statusLabel = TBP_UNKNOWN;
                    }
                }
            }
            if (!"".equals(statusLabel) && !TBP_VALID.equals(statusLabel)) {
                item.setAttribute("displayImage", statusLabel);
                final String contextPath = Jahia.getContextPath();
                String actionURL = contextPath + "/ajaxaction/GetTimeBasedPublishingState?params=/op/edit/pid/" +
                        jParams.getPageID() + "&key=" + contentObject.getObjectKey();
                String serverURL = actionURL + "&displayDialog=true";

                String dialogTitle = JahiaResourceBundle.getEngineResource("org.jahia.engines.timebasedpublishing.dialogTitle",
                        jParams, jParams.getLocale(), "Informational");
                item.setAttribute("timeBasedPublishingActionURL", serverURL);
                item.setAttribute("timeBasedPublishingDialogTitle", dialogTitle);
                item.setAttribute("timeBasedPublishingPageId", String.valueOf(jParams.getPageID()));
            }
        } catch (final Exception e) {
            logger.error("Unable to process the request !", e);
        }
    }

    /**
     * Utility method to get a display Title for a ContentObject in a given language
     *
     * @param object       The ContentObject to get the title from
     * @param languageCode The language of the title
     * @return The title value in a String object
     */
    protected String getAPageTitleAnyway(
            final ContentObject object,
            final String languageCode,
            final ProcessingContext jParams,
            final int parentID) {
        final ObjectKey objectKey = object.getObjectKey();
        String pageTitle1;
        if (objectKey.getType().equals(ContentPageKey.PAGE_TYPE)) {
            final ContentPage contentPage = (ContentPage) object;
            final Map titles = contentPage.getTitles(ContentPage.LAST_UPDATED_TITLES);
            pageTitle1 = (String) titles.get(languageCode);

            if (pageTitle1 == null || pageTitle1.length() == 0) {
                pageTitle1 = "N.A.";
            }
            // Moved page
            if ((contentPage.hasSameParentID() != ContentPage.SAME_PARENT) && (contentPage.getParentID(jParams) != parentID)) {
                pageTitle1 += " (" + JahiaResourceBundle.getEngineResource("org.jahia.moved.label", jParams,
                        LanguageCodeConverters.languageCodeToLocale(languageCode)) + ")";
            }

        } else {
            try {
                final ContentDefinition def = ContentDefinition.getContentDefinitionInstance(object.getDefinitionKey(null));
                pageTitle1 = def.getName();
                if (objectKey.getType().equals(ContentContainerKey.CONTAINER_TYPE)) {
                    pageTitle1 += " (Container " + objectKey.getIDInType() + ")";
                    try {
                        final List l = object.getChilds(null, null, JahiaContainerStructure.JAHIA_FIELD);
                        for (final Iterator iterator = l.iterator(); iterator.hasNext();) {
                            final ContentObject contentObject = (ContentObject) iterator.next();
                            if (contentObject instanceof ContentSmallTextField) {
                                ContentObjectEntryState closestentryState = contentObject.getEntryState(ContentObjectEntryState.getEntryState(0, jParams.getLocale().getLanguage()), true, true);
                                if (closestentryState == null) {
                                    closestentryState = (ContentObjectEntryState) contentObject.getActiveAndStagingEntryStates().last();
                                }
                                pageTitle1 = ((ContentSmallTextField) contentObject).
                                        getValue(closestentryState) +
                                        " (Container " + objectKey.getIDInType() + ")";
                                break;
                            }
                        }
                    } catch (JahiaException e) {
                        logger.error(e.getMessage(), e);
                    }
                } else if (objectKey.getType().equals(ContentContainerListKey.CONTAINERLIST_TYPE)) {
                    pageTitle1 += " (List " + objectKey.getIDInType() + ")";
                } else if (objectKey.getType().equals(ContentFieldKey.FIELD_TYPE)) {
                    pageTitle1 += " (Field " + objectKey.getIDInType() + ")";
                }
            } catch (ClassNotFoundException e) {
                pageTitle1 = objectKey.toString();
                logger.error(e, e);
            }
        }

        final int workflowTitlesLength = org.jahia.settings.SettingsBean.getInstance().getWorkflowTitlesLength();
        if (pageTitle1.length() > workflowTitlesLength) {
            pageTitle1 = pageTitle1.substring(0, workflowTitlesLength) + "...";
        }

        return pageTitle1;
    }

    /**
     * Utility method that converts a List to a Set
     *
     * @param locales The list that will be converted
     * @return The Set made of the objects contained in the given List
     */
    protected final Set getLanguageSet(final List locales) {
        final Set languageCodes = new HashSet();
        for (Object locale : locales) {
            final String languageCode = ((SiteLanguageSettings) locale).getCode();
            languageCodes.add(languageCode);
        }
        return languageCodes;
    }
}
