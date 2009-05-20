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
package org.jahia.ajax.gwt.engines.workflow.server.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowElement;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowHistoryEntry;
import org.jahia.ajax.gwt.client.data.GWTJahiaLabel;
import org.jahia.ajax.gwt.utils.JahiaObjectCreator;
import org.jahia.ajax.gwt.templates.components.actionmenus.server.helper.ActionMenuURIFormatter;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.lock.LockService;
import org.jahia.services.lock.LockKey;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaVersionService;
import org.jahia.services.version.StateModificationContext;
import org.jahia.services.workflow.*;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.utils.LanguageCodeConverters;
import org.quartz.JobDetail;
import org.quartz.JobDataMap;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 5 ao�t 2008 - 17:00:28
 */
public class WorkflowServiceHelper {

    private static final org.apache.log4j.Logger logger = Logger.getLogger(WorkflowServiceHelper.class);

    protected static final WorkflowService workflowService = ServicesRegistry.getInstance().getWorkflowService();
    protected static final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
    protected static final JahiaVersionService jahiaVersionService = ServicesRegistry.getInstance().getJahiaVersionService();
    protected static final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();

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
     * Retrieve all active languages for a given site.
     *
     * @param site the site
     * @return an ordered list of language codes
     */
    public static List<String> retrieveOrderedLanguageCodesForSite(JahiaSite site) {
        List<String> languageCodes = new ArrayList<String>();
        try {
            List<SiteLanguageSettings> languageSettings = site.getLanguageSettings(true);
            for (SiteLanguageSettings setting : languageSettings) {
                languageCodes.add(setting.getCode());
            }
        } catch (JahiaException e) {
            logger.error("Unable to retrieve languages for site " + site.getSiteKey(), e);
        }
        return languageCodes;
    }

    /**
     * Retrieve all active languages for a given site.
     *
     * @param site the site
     */
    public static Map<String,Locale> retrieveOrderedLocaleDisplayForSite(JahiaSite site) {
        Map<String,Locale> languageCodes = new HashMap<String, Locale>(3);
        try {
            List<SiteLanguageSettings> languageSettings = site.getLanguageSettings(true);
            for (SiteLanguageSettings setting : languageSettings) {
                final String code = setting.getCode();
                languageCodes.put(code, LanguageCodeConverters.languageCodeToLocale(code));
            }
        } catch (JahiaException e) {
            logger.error("Unable to retrieve languages for site " + site.getSiteKey(), e);
        }
        return languageCodes;
    }

    /**
     * Recursive method to retrieve flattened list of separate workflow children of the give parent content object.
     *
     * @param parent           the parent content object
     * @param object           the associated content object
     * @param depth            the depth to flatten
     * @param jParams          the processing context
     * @param enableValidation enable validation checks
     * @return flattened children list
     */
    public static List<GWTJahiaWorkflowElement> getSubElementsRec(GWTJahiaWorkflowElement parent, ContentObject object, int depth, boolean enableValidation, ProcessingContext jParams) {
        if (logger.isDebugEnabled()) {
            logger.debug("retrieving children for " + parent.getObjectKey());
        }
        List<GWTJahiaWorkflowElement> result = new ArrayList<GWTJahiaWorkflowElement>();

        // critical stop case (should not happen)
        if (parent == null) {
            return result;
        }

        // check if validation has been added to the parent (should not if it is the root, it comes from the tree)
        boolean workflowable = true;
        if (enableValidation && parent.getValidation() == null) {
            try {
                workflowable = ValidationHelper.checkValidation(parent, object, jParams);
                lock(parent, object, jParams);
            } catch (JahiaException e) {
                logger.error("Can't validate object " + parent.getObjectKey(), e);
            }
        }
        parent.setAccessibleInTable(workflowable);
        result.add(parent);

        // stop case
        if (depth == 0) {
            return result;
        }

        // recursive case
        if (logger.isDebugEnabled()) {
            logger.debug("Entering separate workflow children retrieval for " + parent.getObjectKey());
        }
        List<GWTJahiaWorkflowElement> children = WorkflowServiceHelper.getSeparateWorkflowChildren(parent, object, enableValidation, false, jParams);
        if (logger.isDebugEnabled()) {
            logger.debug("Finished separate workflow children retrieval for " + parent.getObjectKey());
        }
        for (GWTJahiaWorkflowElement el : children) {
            ContentObject elObject = null;
            try {
                elObject = JahiaObjectCreator.getContentObjectFromString(el.getObjectKey());
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Recursive call for " + el.getObjectKey());
            }
            result.addAll(getSubElementsRec(el, elObject, depth - 1, enableValidation, jParams));
        }
        return result;
    }

    /**
     * Retrieve sub elements of a given content object that are pages or have a separate workflow .
     *
     * @param parent  the parent content object
     * @param object  the associated content object
     * @param pagesOnly retrieve only page-typed content
     * @param jParams the processing context
     * @return the list of children
     */
    public static List<GWTJahiaWorkflowElement> getSeparateWorkflowChildren(GWTJahiaWorkflowElement parent, ContentObject object, boolean pagesOnly, ProcessingContext jParams) {
        return getSeparateWorkflowChildren(parent, object, false, pagesOnly, jParams);
    }

    /**
     * Retrieve sub elements of a given content object that are pages or have a separate workflow .
     *
     * @param parent           the parent content object
     * @param object           the associated content object
     * @param pagesOnly        retrieve only page-typed content
     * @param jParams          the processing context
     * @param enableValidation add validation information to the elements
     * @return the list of children
     */
    public static List<GWTJahiaWorkflowElement> getSeparateWorkflowChildren(GWTJahiaWorkflowElement parent, ContentObject object, boolean enableValidation, boolean pagesOnly, ProcessingContext jParams) {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting separate workflow children for object " + parent.getObjectKey());
        }
        List<GWTJahiaWorkflowElement> elements = new ArrayList<GWTJahiaWorkflowElement>();
        if (object != null && object.checkReadAccess(jParams.getUser())) {
            String parentPath = parent.getPath();
            for (ContentObject obj : getSeparateWorkflowChildren(object, jParams.getUser(), pagesOnly)) {
                if (obj.checkReadAccess(jParams.getUser())) {
                    ObjectKey key = obj.getObjectKey();
                    String title = obj.getDisplayName(jParams);
                    if (title == null || title.trim().length() == 0) {
                        title = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.workflow.display.notitle", jParams.getLocale());
                    }

                    GWTJahiaWorkflowElement wfEl = new GWTJahiaWorkflowElement(
                            obj.getPageID(),
                            key.getKey(),
                            key.getType(),
                            title,
                            new StringBuilder(parentPath).append("/").append(title).toString(),
                            WorkflowServiceHelper.hasSeparateWorkflowChildren(obj, jParams.getUser(), pagesOnly),
                            getWorkflowStates(obj)

                    );

                    try {
                        wfEl.setAvailableAction(getAvailableActionsForObject((ContentObjectKey) obj.getObjectKey(), wfEl.getWorkflowStates().keySet(), jParams));
                    } catch (JahiaException e) {
                        logger.error(e.getMessage(), e);
                    }
                    // add validation information
                    boolean workflowable = true;
                    if (enableValidation) {
                        try {
                            workflowable = ValidationHelper.checkValidation(wfEl, obj, jParams);
                            lock(wfEl, obj, jParams);
                        } catch (JahiaException e) {
                            logger.error("Can't validate object " + key.getKey(), e);
                        }
                    }
                    wfEl.setAccessibleInTable(workflowable);
                    elements.add(wfEl);
                }
            }
        }
        return elements;
    }

    /**
     * Ouch!
     * This method retrieves all the pages from the home page to the current page and arrange them hierarchically for tree display.
     *
     * @param targetPid
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public static List<GWTJahiaWorkflowElement> getParentAndSiblingPages(int targetPid, ProcessingContext jParams) throws JahiaException {
        List<ContentPage> path = ServicesRegistry.getInstance().getJahiaPageService().getContentPagePath(targetPid, jParams) ;
        if (path.size() == 0) {
            logger.debug("no path") ;
            return null ;
        } else  {
            List<GWTJahiaWorkflowElement> parentsAndSiblings = new ArrayList<GWTJahiaWorkflowElement>() ;
            if (path.size() == 1) {
                logger.debug("path size is 1") ;
                return null ; // this is the home page
            } else {
                try {
                    GWTJahiaWorkflowElement lastParent = null ;
                    List<GWTJahiaWorkflowElement> previousChildren = null ;
                    for (int i=path.size()-2; i>-1; i--) {
                        ContentPage parentPage = path.get(i) ;
                        GWTJahiaWorkflowElement parent = getWorkflowElement(parentPage, false, jParams) ;
                        if (parent == null) {
                            logger.debug("parent is null") ;
                            if (previousChildren != null && lastParent != null) {
                                lastParent.setChildren(previousChildren);
                                parentsAndSiblings.add(lastParent) ;
                            } else {
                                logger.debug("nothing exist") ;
                                ContentPage page = path.get(path.size()-1) ;
                                GWTJahiaWorkflowElement newRoot = getWorkflowElement(page, false, jParams) ;
                                if (newRoot != null) {
                                    newRoot.setHasChildren(WorkflowServiceHelper.hasSeparateWorkflowChildren(page, jParams.getUser(), true));
                                    logger.debug("newroot not null") ;
                                    parentsAndSiblings.add(newRoot) ;
                                } else {
                                    logger.debug("newroot is null") ;
                                }
                            }
                            break ;
                        }
                        List<GWTJahiaWorkflowElement> children = getSeparateWorkflowChildren(parent, parentPage, true, jParams) ;
                        if (previousChildren != null && lastParent != null) {
                            logger.debug("previous children not null, trying to add them to current model") ;
                            for (GWTJahiaWorkflowElement wfEl: children) {
                                if (wfEl.getObjectKey().equals(lastParent.getObjectKey())) {
                                    wfEl.setChildren(previousChildren);
                                    for (GWTJahiaWorkflowElement subEl: previousChildren) {
                                        subEl.setParent(wfEl);
                                    }
                                    break ;
                                }
                            }
                        }
                        previousChildren = children ;
                        lastParent = parent ;
                        if (i == 0) { // this is the last iteration
                            logger.debug("last iteration") ;
                            parent.setChildren(children);
                            for (GWTJahiaWorkflowElement wfEl: children) {
                                wfEl.setParent(parent);
                            }
                            parentsAndSiblings.add(parent);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            return parentsAndSiblings ;
        }
    }

    public static GWTJahiaWorkflowElement getWorkflowElement(ContentObject obj, boolean enableValidation, ProcessingContext jParams) {
        return getWorkflowElement(obj, enableValidation, true, jParams) ;
    }

    public static GWTJahiaWorkflowElement getWorkflowElement(ContentObject obj, boolean enableValidation, boolean enableWorkflowStates, ProcessingContext jParams) {
        if (obj.checkReadAccess(jParams.getUser())) {
            ObjectKey key = obj.getObjectKey();
            String title = obj.getDisplayName(jParams);
            if (title == null || title.trim().length() == 0) {
                title = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.workflow.display.notitle", jParams.getLocale());
            }

            GWTJahiaWorkflowElement wfEl = new GWTJahiaWorkflowElement(
                    obj.getPageID(),
                    key.getKey(),
                    key.getType(),
                    title,
                    "",
                    false,
                    enableWorkflowStates ? getWorkflowStates(obj) : new HashMap<String, String>()
            );

            try {
                wfEl.setAvailableAction(getAvailableActionsForObject((ContentObjectKey) obj.getObjectKey(), wfEl.getWorkflowStates().keySet(), jParams));
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
            // add validation information
            if (enableValidation) {
                try {
                    ValidationHelper.checkValidation(wfEl, obj, jParams);
                } catch (JahiaException e) {
                    logger.error("Can't validate object " + key.getKey(), e);
                }
            }
            return wfEl;
        }
        return null;
    }

    /**
     * Retrieves workflow states for a given content object
     *
     * @param object the content object
     * @return a map of (language code, extended workflow state)
     */
    public static Map<String, String> getWorkflowStates(ContentObject object) {
        Map<String, String> languageStates;
        try {
            languageStates = workflowService.getExtendedWorkflowStates(object);
        } catch (JahiaException e) {
            languageStates = new HashMap<String, String>();
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            languageStates = new HashMap<String, String>();
            logger.error(e.getMessage(), e);
        }
        return languageStates;
    }


    public static Map<String, Set<String>> getAvailableActionsForObject(ContentObjectKey obj, Collection<String> languages, ProcessingContext jParams) throws JahiaException {
        Map<String, Set<String>> actions = new HashMap<String, Set<String>>();
        if (workflowService.getWorkflowMode(obj) == WorkflowService.LINKED) {
            return actions ;
        }
        int mode = workflowService.getInheritedMode(obj);
        if (mode == WorkflowService.EXTERNAL) {
            String wn = workflowService.getInheritedExternalWorkflowName(obj);
            ExternalWorkflow ext = workflowService.getExternalWorkflow(wn);
            String pid = workflowService.getInheritedExternalWorkflowProcessId(obj);
            for (String language : languages) {
                if (ext.isProcessStarted(pid, obj.toString(), language)) {
                    try {
                        getLanguageSet(actions, language).addAll(ext.getAvailableActions(pid, obj.toString(), language, jParams));
                    } catch (Exception e) {
                        logger.error("Failed to retrieve actions for " + obj.getKey() + " in " + language, e);
                    }
                }
            }
        }
        try {
            ContentObject contentObject = ContentObject.getContentObjectInstance(obj);
            Map<String, Integer> states = workflowService.getLanguagesStates(contentObject);
            for (String language : states.keySet()) {
                int state = states.get(language);
                if (state != EntryLoadRequest.ACTIVE_WORKFLOW_STATE && contentObject.checkAdminAccess(jParams.getUser())) {
                    if ("shared".equals(language)) {
                        for (String s : languages) {
                            getLanguageSet(actions, s).add(AbstractActivationJob.PUBLISH_PENDING_PAGES);
                        }
                    } else {
                        getLanguageSet(actions, language).add(AbstractActivationJob.PUBLISH_PENDING_PAGES);
                    }
                }
                if (mode == WorkflowService.JAHIA_INTERNAL && state == EntryLoadRequest.STAGING_WORKFLOW_STATE && contentObject.checkWriteAccess(jParams.getUser())) {
                    if ("shared".equals(language)) {
                        for (String s : languages) {
                            getLanguageSet(actions, s).add(AbstractActivationJob.NOTIFY_PAGES);
                        }
                    } else {
                        getLanguageSet(actions, language).add(AbstractActivationJob.NOTIFY_PAGES);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }

        return actions;
    }

    private static Set<String> getLanguageSet(Map<String, Set<String>> actions, String language) {
        if (!actions.containsKey(language)) {
            Set<String> s = new HashSet<String>();
            actions.put(language, s);
        }
        return actions.get(language);
    }

    /**
     * Check if the current object has children using separared workflows.
     * This is sort of an optimization of the getSeparateWorkflowChildren method.
     *
     * @param object      the content object
     * @param currentUser the current user
     * @param pagesOnly process only pages
     * @return true if it has separated workflow children
     */
    public static boolean hasSeparateWorkflowChildren(final ContentObject object, final JahiaUser currentUser, boolean pagesOnly) {
        try {
            if (object == null || !object.checkReadAccess(currentUser) || object.getPickedObject() != null) {
                return false;
            } else {
                final List<JahiaObject> linked = workflowService.getLinkedContentObjects(object, false);
                for (Object aLinked : linked) {
                    final ContentObject contentObject = (ContentObject) aLinked;
                    if (contentObject.checkReadAccess(currentUser)) {
                        if (contentObject.getObjectKey().getType().equals(ContentPageKey.PAGE_TYPE)) {
                            final ContentPage contentPage = (ContentPage) contentObject;
                            if (contentPage.getPageType(EntryLoadRequest.STAGED) == JahiaPage.TYPE_DIRECT) {
                                if (contentPage.getParentID(EntryLoadRequest.STAGED) == object.getPageID() &&
                                        contentPage.getID() != object.getPageID()) {
                                    return true;
                                }
                            }
                        }
                    }
                }

                final List<ContentObject> fieldsHere = new ArrayList<ContentObject>();
                final List<JahiaObject> unlinked = workflowService.getUnlinkedContentObjects(object);
                for (Object anUnlinked : unlinked) {
                    final ContentObject contentObject = (ContentObject) anUnlinked;
                    if (contentObject.checkReadAccess(currentUser)) {
                        final int pageId = contentObject.getPageID();
                        if (contentObject.getObjectKey().getType().equals(ContentPageKey.PAGE_TYPE)
                                && (((ContentPage) contentObject).getPageType(EntryLoadRequest.STAGED) == JahiaPage.TYPE_DIRECT
                                && object.getID() != pageId)) {
                            return true;
                        }
                        if (!contentObject.getObjectKey().getType().equals(ContentPageKey.PAGE_TYPE)
                                || (((ContentPage) contentObject).getPageType(EntryLoadRequest.STAGED) == JahiaPage.TYPE_DIRECT
                                && object.getID() != pageId)) {
                            fieldsHere.add(contentObject);
                        }
                    }
                }

                for (Object aFieldsHere : fieldsHere) {
                    final ContentObject contentObject = (ContentObject) aFieldsHere;
                    if (contentObject.checkReadAccess(currentUser) && contentObject.hasActiveOrStagingEntries()) {
                        if (!(contentObject.getObjectKey().getType().equals(ContentPageKey.PAGE_TYPE))) {
                            final ContentObject main = workflowService.getMainLinkObject(contentObject);
                            if (main == null) {
                                logger.warn("Main object is null for the content object: " + contentObject.getObjectKey());
                                continue;
                            }
                            if ((main instanceof ContentPage && ((ContentPage) main).getParentID(EntryLoadRequest.STAGED) != object.getPageID()) ||
                                    (!(main instanceof ContentPage) && main.getPageID() != object.getPageID())) {
                                continue;
                            }
                            if (!pagesOnly) {
                                return true;
                            } else {
                                return main.getObjectKey().getType().equals(ContentPageKey.PAGE_TYPE) ;
                            }
                        } else {
                            return true;
                        }
                    }
                }
            }
        } catch (JahiaException e) {
            logger.error("Error checking content object children with separate workflow", e);
        }
        return false;
    }

    /**
     * Gets all the direct child objects of a given ContentObject, that have a separate WorkFlow associated to them.
     * This method consists of reused code from SiteMapAbstractAction.processWorkflowObject, we might want to clean
     * that when we have time...
     *
     * @param object      The parent ContentObject to search children from
     * @param currentUser The current logged Jahia user
     * @param pagesOnly filter by content type : pages only
     * @return the separated workflow children objects
     */
    private static List<ContentObject> getSeparateWorkflowChildren(final ContentObject object, final JahiaUser currentUser, boolean pagesOnly) {
        try {
            if (object.getPickedObject() == null) {
                final List<JahiaObject> linked = workflowService.getLinkedContentObjects(object, false);
                final List<ContentObject> linkedPages = new ArrayList<ContentObject>();
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

                final List<ContentObject> fieldsHere = new ArrayList<ContentObject>();
                final List<JahiaObject> unlinked = workflowService.getUnlinkedContentObjects(object);
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
                final List<ContentObject> v = new ArrayList<ContentObject>();

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

                            if (!pagesOnly) {
                                v.add(main);
                            } else if (main.getObjectKey().getType().equals(ContentPageKey.PAGE_TYPE)) {
                                v.add(main) ;
                            }
                            //}
                        } else {
                            v.add(contentObject);
                        }
                    }
                }
                return v ;
            } else {
                return new ArrayList<ContentObject>();
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<ContentObject>();
    }

    public static ActivationTestResults getActivationStatus(ProcessingContext jParams, ContentObject object) {
        Set<String> languageCodes = new HashSet<String>(retrieveOrderedLanguageCodesForSite(jParams.getSite()));
        StateModificationContext context = new StateModificationContext(object.getObjectKey(), languageCodes, false);
        try {
            return workflowService.isValidForActivation(object, languageCodes, jParams, context);
        } catch (JahiaException e) {
            logger.error("Unable to validate the content object : " + object.getObjectKey().getKey());
            return null;
        }
    }

    public static List<GWTJahiaLabel> getAvailableActions(Locale locale) {
        List<GWTJahiaLabel> all = new ArrayList<GWTJahiaLabel>();
        List<String> keys = new ArrayList<String>();
        Map<String, ExternalWorkflow> externals = workflowService.getExternalWorkflows();
        int i = 0;
        for (ExternalWorkflow externalWorkflow : externals.values()) {
            List<String> processes = externalWorkflow.getAvailableProcesses();
            for (String process : processes) {
                List<String> c = externalWorkflow.getAllActions(process);
                for (String key : c) {
                    if (!keys.contains(key)) {
                        keys.add(i, key);
                        all.add(i++, new GWTJahiaLabel(key, externalWorkflow.getActionName(process, key, locale)));
                    } else {
                        i = keys.indexOf(key) + 1 ;
                    }
                }
            }
        }
        return all;
    }

    public static List<GWTJahiaWorkflowHistoryEntry> getHistory(GWTJahiaWorkflowElement item, ProcessingContext jParams) {
        List<GWTJahiaWorkflowHistoryEntry> results = new ArrayList<GWTJahiaWorkflowHistoryEntry>();
        try {
            ContentObjectKey obj = (ContentObjectKey) ContentObjectKey.getInstance(item.getObjectKey());
            //Map<String, Set<String>> actions = new HashMap<String, Set<String>>();
            int mode = workflowService.getInheritedMode(obj);
            if (mode == WorkflowService.EXTERNAL) {
                String wn = workflowService.getInheritedExternalWorkflowName(obj);
                ExternalWorkflow ext = workflowService.getExternalWorkflow(wn);
                List<ExternalWorkflowHistoryEntry> hist = ext.getWorkflowHistoryByObject(obj.toString());
                for (ExternalWorkflowHistoryEntry entry : hist) {
                    results.add(new GWTJahiaWorkflowHistoryEntry(new Date(entry.getDate().getTime()), entry.getAction(), entry.getUser(), entry.getComment(), entry.getLanguage()));
                }

            }
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return results;
    }

    public static void quickValidate(String objectKey, String lang, String action, String comment, ProcessingContext ctx) {
        try {
            final JobDetail jobDetail = BackgroundJob.createJahiaJob(
                    "QuickValidation", QuickActivationJob.class, ctx);
            JobDataMap jobDataMap = jobDetail.getJobDataMap();

            jobDataMap.put(BackgroundJob.JOB_DESTINATION_SITE, ctx.getSite().getSiteKey());
            jobDataMap.put(BackgroundJob.JOB_TYPE, AbstractActivationJob.WORKFLOW_TYPE);

            jobDataMap.put(QuickActivationJob.CONTENT_OBJECT_KEY, objectKey);
            jobDataMap.put(QuickActivationJob.LANGUAGE, lang);
            jobDataMap.put(QuickActivationJob.ACTION, action);
            jobDataMap.put(AbstractActivationJob.COMMENTS_INPUT, comment);

            ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail);
        } catch (JahiaException e) {
            logger.error("Quick validate failed", e) ;
        }
    }

    public static void storeBatch(Map<String, Map<String, Set<String>>> batch, ProcessingContext ctx) {
        ctx.getSessionState().setAttribute("workflowBatch", batch);
    }

    public static Map<String, Map<String, Set<String>>> restoreBatch(ProcessingContext ctx) {
        return (Map<String, Map<String, Set<String>>>) ctx.getSessionState().getAttribute("workflowBatch");
    }


    public static void addToBatch(String objectKey, String lang, String action, ProcessingContext ctx) {
        Map<String, Map<String, Set<String>>> batch = restoreBatch(ctx);
        if (batch == null) {
            batch = new HashMap<String, Map<String, Set<String>>>();
            storeBatch(batch, ctx);
        }
        Map<String, Set<String>> m1 = batch.get(action);
        if (m1 == null) {
            m1 = new HashMap<String,Set<String>>();
            batch.put(action,m1);
        }
        Set<String> s = m1.get(objectKey);
        if (s == null) {
            s = new HashSet<String>();
            m1.put(objectKey,s);
        }
        s.add(lang);
    }



    public static void publishAll(String comment, ProcessingContext context) {
        try {
            final JobDetail jobDetail = BackgroundJob.createJahiaJob("PublishAll", PublishAllJob.class, context);
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put(BackgroundJob.JOB_TYPE, AbstractActivationJob.WORKFLOW_TYPE);
            jobDataMap.put(AbstractActivationJob.COMMENTS_INPUT, comment);
            ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail);
        } catch (JahiaException e) {
            logger.error("Publish all failed", e) ;
        }
    }

    private static void lock(GWTJahiaWorkflowElement workflowElement, ContentObject object, ProcessingContext jParams) throws JahiaException {
        final Map<String, Integer> languagesStates = workflowService.getLanguagesStates(object);
        final Set<String> languageCodes = new HashSet<String>(WorkflowServiceHelper.retrieveOrderedLanguageCodesForSite(jParams.getSite())) ;
        final ObjectKey objectKey = object.getObjectKey() ;
        JahiaUser currentUser = jParams.getUser() ;
        boolean accessAuthorized = object.checkAdminAccess(currentUser) || object.checkWriteAccess(currentUser);

        if (accessAuthorized) {
            boolean doLock = false;
            for (String languageCode : languageCodes) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found object: " + objectKey + " in " + languageCode);
                }
                Integer languageState = languagesStates.get(languageCode);
                Integer sharedLanguageState = languagesStates.get(ContentObject.SHARED_LANGUAGE);
                if (logger.isDebugEnabled()) {
                    logger.debug("language state : " + (languageState != null ? languageState : "null") + " / shared language state : " + (sharedLanguageState != null ? sharedLanguageState : "null")) ;
                }
                if (languageState != null && languageState != -1) {
                    if (sharedLanguageState != null && languageState < sharedLanguageState) {
                        languageState = sharedLanguageState;
                    }
                    doLock |= (languageState != EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
                    if (logger.isDebugEnabled()) {
                        logger.debug("lock object ? " + String.valueOf(doLock));
                    }
                }
            }
            if (jParams.settings().areLocksActivated() && doLock) {
                final LockKey lockKey = LockKey.composeLockKey(LockKey.WORKFLOW_ACTION + "_" + objectKey.getType(), object.getID());
                if (lockRegistry.acquire(lockKey, currentUser, currentUser.getUserKey(), jParams.getSessionState().getMaxInactiveInterval())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Lock acquired for object " + objectKey + " by " + currentUser.getUsername());
                    }
                    Set<LockKey> wl = (Set<LockKey>) jParams.getSessionState().getAttribute("workflowLocks");
                    if (wl == null) {
                        logger.debug("No workflow locks in session, creating set...") ;
                        wl = new HashSet<LockKey>() ;
                        jParams.getSessionState().setAttribute("workflowLocks", wl);
                    }
                    wl.add(lockKey);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Cannot acquire lock for object " + objectKey + " by " + currentUser.getUsername());
                    }
                    String stealLockUrl = ActionMenuURIFormatter.drawUrlCheckWriteAccess(jParams, "lock", lockKey, false, false) ;
                    if (stealLockUrl != null && stealLockUrl.trim().length() > 0) {
                        workflowElement.setStealLock(stealLockUrl);
                    }
                }
            }
        }

    }

}
