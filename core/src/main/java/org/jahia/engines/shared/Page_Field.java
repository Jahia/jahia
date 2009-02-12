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

package org.jahia.engines.shared;

import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.*;
import org.jahia.engines.*;
import org.jahia.engines.pages.PageProperties_Engine;
import org.jahia.engines.selectpage.SelectPage_Engine;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaTemplateNotFoundException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.registries.EnginesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.cache.Cache;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.pages.*;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.StateModificationContext;
import org.jahia.services.workflow.WorkflowEvent;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockKey;
import org.jahia.services.timebasedpublishing.RetentionRule;
import org.jahia.services.timebasedpublishing.RetentionRuleEvent;
import org.jahia.services.timebasedpublishing.TimeBasedPublishingJob;
import org.jahia.services.timebasedpublishing.TimeBasedPublishingService;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.manager.JahiaObjectDelegate;
import org.jahia.hibernate.model.JahiaAclEntry;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class Page_Field implements FieldSubEngine {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(Page_Field.class);

    public static final String READONLY_JSP = "/jsp/jahia/engines/shared/readonly_page_field.jsp";
    public static final String ACCESSDENIED_JSP = "/jsp/jahia/engines/shared/accessdenied_page_field.jsp";
    public static final String CREATE_PAGE = "createPage";
    // Page update consists to change templae, change title or change (if possible)
    // page type.
    public static final String UPDATE_PAGE = "updatePage";
    public static final String LINK_JAHIA_PAGE = "linkJahiaPage";
    public static final String LINK_URL = "linkURL";
    public static final String MOVE_PAGE = "movePage";
    public static final String COPY_PAGE = "copyPage";
    public static final String RESET_LINK = "removeLink";

    private static final int MAX_PAGETITLE_LENGTH = 250;

    private static Page_Field instance;
    private static final String[] DEFAULT_OPERATION = {UPDATE_PAGE, LINK_JAHIA_PAGE, LINK_URL};
    private static final String JSP_FILE = "/jsp/jahia/engines/shared/page_field.jsp";


    /**
     * Retrieves the unique instance of this class.
     *
     * @return the unique instance of this class
     */
    public static synchronized Page_Field getInstance() {
        if (instance == null) {
            instance = new Page_Field();
        }
        return instance;
    }


    /**
     * Handles the field actions
     *
     * @param jParams   a ProcessingContext object
     * @param modeInt   the mode, according to JahiaEngine
     * @param engineMap the engine map prepared from the parent engine.
     * @return true if everything went okay, false if not
     * @throws JahiaException
     * @see org.jahia.engines.JahiaEngine
     */
    public boolean handleField(ProcessingContext jParams, Integer modeInt, Map engineMap)
            throws JahiaException {
        // @todo : find a better way
        Locale processingLocale = (Locale) engineMap.get(JahiaEngine.PROCESSING_LOCALE);
        EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        EntryLoadRequest entryLoadRequest = elh.getCurrentEntryLoadRequest();
        entryLoadRequest.setFirstLocale(processingLocale.toString());
        EntryLoadRequest savedEntryLoadRequest =
                jParams.getSubstituteEntryLoadRequest();
        jParams.setSubstituteEntryLoadRequest(entryLoadRequest);

        String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
        JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + "." + "theField");

        //JahiaField theField = (JahiaField)engineMap.get("theField");

        switch (modeInt.intValue()) {
            case (JahiaEngine.LOAD_MODE):
                logger.debug("Loading pagefield");
                return composeEngineMap(jParams, engineMap, theField);
            case (JahiaEngine.UPDATE_MODE):
                logger.debug("Updating pagefield");
                return getFormData(jParams, engineMap, theField);
            case (JahiaEngine.SAVE_MODE):
                logger.debug("Saving pagefield");
                return saveData(jParams, engineMap, theField);
        }
        jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);
        return false;
    }

    /**
     * Remove all page beans from session before calling this engine for adding
     * container containing page field for example.
     *
     * @param jParams The ProcessingContext object
     */
    public static void resetPageBeanSession(ProcessingContext jParams) {
        jParams.getSessionState().removeAttribute("Page_Field.PageBeans");
    }

    /**
     * Initialize the page bean object before calling the engine.
     *
     * @param jParams  A ParamBeam object
     * @param theField The page field ID corresponding to the field to edit
     * @throws JahiaException
     */
    public static void initPageBeanSession(ProcessingContext jParams, JahiaField theField)
            throws JahiaException {
        Map pageBeans =
                (Map) jParams.getSessionState().getAttribute("Page_Field.PageBeans");
        if (pageBeans != null) {
            pageBeans.remove(theField.getDefinition().getName());
        }
    }

    /**
     * composes engine hash map
     *
     * @param jParams   a ProcessingContext object
     * @param engineMap the engine hashmap
     * @param theField  the field we are working on
     * @return true if everything went okay, false if not
     * @throws JahiaException
     */
    private boolean composeEngineMap(ProcessingContext jParams, Map engineMap, JahiaField theField)
            throws JahiaException {

        logger.debug("Start compose Jahia page field engine map");
        String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");

        boolean editable = false;
        JahiaContainer theContainer = (JahiaContainer) engineMap.get(fieldsEditCallingEngineName + "." + "theContainer");
        if (theContainer == null) {
            // in case of a field , not a field in a container
            editable = true;
        } else {
            FieldsEditHelper feh = (FieldsEditHelper) engineMap.get(fieldsEditCallingEngineName + "."
                    + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);
            Map ctnListFieldAcls = feh.getCtnListFieldAcls();
            int fieldId = theField.getID();
            if (theContainer.getListID() != 0 && ctnListFieldAcls != null && ctnListFieldAcls.size() > 0) {
                JahiaBaseACL acl = JahiaEngineTools.getCtnListFieldACL(ctnListFieldAcls, fieldId);
                if (acl != null) {
                    editable = acl.getPermission(jParams.getUser(), JahiaBaseACL.WRITE_RIGHTS, JahiaEngineTools.isCtnListFieldACLDefined(ctnListFieldAcls, fieldId));
                }
            } else {
                editable = true;
            }
        }
        String output = "";

        String forward = theField.getDefinition().getProperty(JahiaFieldDefinitionProperties.FIELD_UPDATE_JSP_FILE_PROP);
        if (forward == null) {
            forward = Page_Field.JSP_FILE;
            final LockPrerequisitesResult results = LockPrerequisites.getInstance().getLockPrerequisitesResult((LockKey) engineMap.get("LockKey"));
            final String screen = (String) engineMap.get("screen");
            boolean isLocked = false;
            if (results != null) {
                if ("edit".equals(screen)) {
                    isLocked = results.getReadOnlyTabs().contains(LockPrerequisites.EDIT) ||
                            results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT);
                } else if ("metadata".equals(screen)) {
                    isLocked = results.getReadOnlyTabs().contains(LockPrerequisites.METADATA) ||
                            results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT);
                }
            }
            final boolean readOnly = (results != null && isLocked);
            if (!editable || readOnly) {
                forward = Page_Field.READONLY_JSP;
            }
        }

        if (editable) {
            JahiaPageEngineTempBean pageBean = composePage(jParams, theField);
            if (pageBean == null) {
                // this can happen if we don't have the rights to the page
                // or if the page field has a corrupted value.
                output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean) jParams, ACCESSDENIED_JSP);
                engineMap.put("fieldForm", output);
                return true;
            }
            engineMap.put("pageTempBean", pageBean);
            int selectedPageID = pageBean.getPageLinkID();
            if (jParams.getParameter("shouldSetPageLinkID") != null) {
                selectedPageID = Integer.parseInt(jParams.getParameter("pageSelected"));
            }
            if (jParams.getParameter("pageMoveDeleteOldContainer") != null) {
                String val = jParams.getParameter("pageMoveDeleteOldContainer");
                pageBean.setDeleteOldContainer("1".equals(val));
            }

            if (selectedPageID != pageBean.getPageLinkID()) {
                pageBean.setPageLinkID(selectedPageID);
                ContentPage contentPage = JahiaPageBaseService.getInstance().
                        lookupContentPage(selectedPageID, jParams.getEntryLoadRequest(), false);
                Map titles = contentPage.getTitles(ContentPage.LAST_UPDATED_TITLES);
                Iterator titleKeys = titles.keySet().iterator();
                while (titleKeys.hasNext()) {
                    String languageCode = (String) titleKeys.next();
                    String title = pageBean.getTitle(languageCode);
                    if ((title == null) || (title.length() == 0)) {
                        pageBean.setTitle(languageCode, (String) titles.get(languageCode));
                    }
                }
            }

            boolean templateNotFound = false;

            // template list
            Iterator templateEnum = ServicesRegistry.getInstance().getJahiaPageTemplateService()
                    .getPageTemplates(pageBean.getSiteID(), false);

            // check for acls
            JahiaPageDefinition def;
            while (templateEnum.hasNext()) {
                def = (JahiaPageDefinition) templateEnum.next();
                if (def.getACL() == null) {
                    ServicesRegistry.getInstance().getJahiaPageTemplateService().createPageTemplateAcl(def);
                }
            }
            templateEnum = ServicesRegistry.getInstance().getJahiaPageTemplateService()
                    .getPageTemplates(jParams.getUser(), pageBean.getSiteID(), true);

            // get current page's template too even though it is desactivated
            if (pageBean.getID() > 0) {
                ContentPage contentPage;
                try {
                    contentPage = ContentPage.getPage(pageBean.getID());
                } catch (JahiaTemplateNotFoundException tnfe) {
                    logger.debug("Template not foudn for page[" + pageBean.getID() + "], try return a ContentPage without template");
                    contentPage = JahiaPageBaseService.getInstance().
                            lookupContentPage(pageBean.getID(), jParams.getEntryLoadRequest(), false);
                }

                EntryLoadRequest loadRequest =
                        new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0,
                                jParams.getEntryLoadRequest().getLocales());

                // active page def
                JahiaPageDefinition activePageDef = contentPage.getPageTemplate(loadRequest);

                JahiaPageDefinition currentPageDef = null;
                try {
                    currentPageDef = ServicesRegistry.getInstance().getJahiaPageTemplateService()
                            .lookupPageTemplate(pageBean.getPageTemplateID());
                } catch (JahiaTemplateNotFoundException tnfe) {
                    logger.debug("Current Template not found for page[" + pageBean.getID() + "]");
                    if (pageBean.getPageType() == JahiaPage.TYPE_DIRECT) {
                        templateNotFound = true;
                    }
                }

                boolean addActivePageDef = true;
                boolean addCurrentPageDef = true;

                List vec = new ArrayList();
                while (templateEnum.hasNext()) {
                    JahiaPageDefinition tmpPageDef = (JahiaPageDefinition) templateEnum.next();
                    if (activePageDef != null && (tmpPageDef.getID() == activePageDef.getID())) {
                        addActivePageDef = false;
                    }
                    if (currentPageDef != null && (tmpPageDef.getID() == currentPageDef.getID())) {
                        addCurrentPageDef = false;
                    }
                    vec.add(tmpPageDef);
                }

                if (addActivePageDef && activePageDef != null) {
                    vec.add(activePageDef);
                }
                if (addCurrentPageDef && currentPageDef != null) {
                    if (activePageDef == null
                            || activePageDef.getID() != currentPageDef.getID()) {
                        vec.add(currentPageDef);
                    }
                }

                // sort it
                if (currentPageDef != null) {
                    Collections.sort(vec, currentPageDef);
                } else if (activePageDef != null) {
                    Collections.sort(vec, activePageDef);
                }
                templateEnum = vec.iterator();
            }

            engineMap.put("templateNotFound", Boolean.valueOf(templateNotFound));
            engineMap.put("templateList", templateEnum);
            Map selectPageURLParams = new HashMap();
            selectPageURLParams.put(SelectPage_Engine.OPERATION, pageBean.getOperation());
            selectPageURLParams.put(SelectPage_Engine.PARENT_PAGE_ID, new Integer(pageBean.getParentID()));
            selectPageURLParams.put(SelectPage_Engine.PAGE_ID, new Integer(pageBean.getID()));
            String selectPageURL = EnginesRegistry.getInstance().getEngineByBeanName("selectPageEngine").renderLink(jParams, selectPageURLParams);
            engineMap.put("selectPageURL", selectPageURL);
            checkTimeBasedPublishingStatus(jParams, pageBean);
            output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean) jParams, forward);
        } else {
            output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean) jParams, forward);
        }
        engineMap.put(fieldsEditCallingEngineName + "." + "fieldForm", output);
        return true;
    }

    protected void checkTimeBasedPublishingStatus(ProcessingContext jParams,
                                                  JahiaPageEngineTempBean pageBean) {
        jParams.removeAttribute("Page_Field.enableTimeBasedPublishingStatus");
        if (pageBean == null) {
            return;
        }
        try {
            if (pageBean.getPageType() == JahiaPage.TYPE_LINK) {
                int linkId = pageBean.getPageLinkID();
                if (linkId != -1) {
                    TimeBasedPublishingService tbpService = ServicesRegistry.getInstance()
                            .getTimeBasedPublishingService();
                    ContentPage contentPage = ContentPage.getPage(linkId);
                    JahiaObjectManager jahiaObjectManager =
                            (JahiaObjectManager) SpringContextSingleton.getInstance()
                                    .getContext().getBean(JahiaObjectManager.class.getName());
                    ContentObject currentObject = contentPage;
                    JahiaObjectDelegate jahiaObjectDelegate =
                            jahiaObjectManager.getJahiaObjectDelegate(currentObject.getObjectKey());
                    RetentionRule retRule = tbpService.getRetentionRule(currentObject.getObjectKey());
                    if ((retRule == null || retRule.getInherited().booleanValue()) && jahiaObjectDelegate.isValid()) {
                        ObjectKey tbpObjectKey =
                                tbpService.getParentObjectKeyForTimeBasedPublishing(contentPage.getObjectKey(), jParams.getUser(),
                                        jParams.getEntryLoadRequest(), jParams.getOperationMode());
                        if (tbpObjectKey != null) {
                            currentObject = ContentObject.getContentObjectInstance(tbpObjectKey);
                            jahiaObjectDelegate =
                                    jahiaObjectManager.getJahiaObjectDelegate(tbpObjectKey);
                            retRule = tbpService.getRetentionRule(tbpObjectKey);
                        }
                    }
                    final long now = System.currentTimeMillis();
                    String statusLabel = "";
                    if (retRule != null) {
                        final boolean inherited = retRule.getInherited().booleanValue();
                        final boolean isValid = jahiaObjectDelegate.isValid();
                        final boolean isExpired = jahiaObjectDelegate.isExpired();
                        final boolean willExpire = jahiaObjectDelegate.willExpire(now);
                        final boolean willBecomeValid = jahiaObjectDelegate.willBecomeValid(now);
                        if (isExpired) {
                            if (willBecomeValid) {
                                statusLabel = "expired_but_will_become_valid"; // yellow
                            } else {
                                statusLabel = "expired"; // red
                            }
                        } else if (isValid) {
                            if (willExpire) {
                                statusLabel = "valid_but_will_expire"; // orange
                            } else {
                                statusLabel = "valid"; // green
                            }
                        } else {
                            if (willBecomeValid) {
                                statusLabel = "not_valid_but_will_become_valid"; // yellow
                            } else {
                                // is not valid
                                statusLabel = "unknown";
                            }
                        }
                        if (statusLabel.length() > 0 && inherited) {
                            statusLabel = "inherited_" + statusLabel;
                        }
                    }
                    // Don't display any icons for objects that have no rules
                    if (!"".equals(statusLabel) && !("inherited_valid".equals(statusLabel))
                            && !(statusLabel.equals("valid"))) {
                        jParams.setAttribute("Page_Field.enableTimeBasedPublishingStatus", currentObject);
                    }
                }
            }
        } catch (Exception t) {
            logger.debug("Exception preparing time base publishing for page engine", t);
        }
    }

    /**
     * gets POST data from the form and saves it in session
     *
     * @param jParams   a ProcessingContext object
     * @param engineMap the engine hashmap
     * @param theField  the field we are working on
     * @return true if everything went okay, false if not
     * @throws JahiaException
     */
    private boolean getFormData(ProcessingContext jParams, Map engineMap, JahiaField theField)
            throws JahiaException {

        logger.debug("Gets POST data from the form and saves it in session");

        Map pageBeans = (Map) jParams.
                getSessionState().getAttribute("Page_Field.PageBeans");

        if (pageBeans == null) {
            engineMap.put("pageTempBean", composePage(jParams, theField));
            pageBeans = (Map) jParams.
                    getSessionState().getAttribute("Page_Field.PageBeans");
        }

        JahiaPageEngineTempBean pageBean =
                (JahiaPageEngineTempBean) pageBeans.get(theField.getDefinition().getName());

        if (pageBean == null) {
            // this can happen if we are processing a page field for a page
            // that denies access to it or in the case of a page field that
            // has a value to an invalid page ID.
            return true;
        }

        String operation = jParams.getParameter("operation"); // Value from FORM
        // Invalidate the last seleted page when operatin change.
        if (operation == null) {
            return true;
        }
        if (!operation.equals(pageBean.getOperation())) {
            pageBean.setPageLinkID(-1);
        }
        pageBean.setOperation(operation);

        String title = jParams.getParameter("page_title");
        // we must check the length here, by correctly handling multibyte
        // characters in the full byte length (unfortunately some databases
        // such as Oracle using byte length instead of character length).
        String encoding = jParams.getCharacterEncoding();
        if (encoding == null) {
            encoding = org.jahia.settings.SettingsBean.getInstance().getDefaultResponseBodyEncoding();
        }
        try {
            int byteLength = title.getBytes(encoding).length;
            while (byteLength > MAX_PAGETITLE_LENGTH) {
                logger.debug("Byte length of field value is over limit, truncating one byte from end...");
                // here we remove one character at a time because the byte
                // length of a character may vary a lot.
                title = title.substring(0, title.length() - 1);
                byteLength = title.getBytes(encoding).length;
            }
        } catch (UnsupportedEncodingException uee) {
            logger.error("Error while calculating byte length of field value for encoding " + encoding, uee);
        }

        Locale locale = (Locale) engineMap.get(JahiaEngine.PROCESSING_LOCALE);
        boolean result = true;
        final String sharedTitle = jParams.getParameter("shared_title");
        // Verify if the page title is shared
        if ("true".equals(sharedTitle)) {
            pageBean.sharedTitle(true);
        } else if (pageBean.isSharedTitle()) { // V removed from the checkbox.
            pageBean.sharedTitle(false);
        }

        if (CREATE_PAGE.equals(operation) || UPDATE_PAGE.equals(operation)) {
            int templateID = Integer.parseInt(jParams.getParameter("template_id"));
            pageBean.setPageTemplateID(templateID);
            pageBean.setPageType(JahiaPage.TYPE_DIRECT);
            final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
            if (aclService.getSiteActionPermission(LockPrerequisites.URLKEY, jParams.getUser(),
                    JahiaBaseACL.READ_RIGHTS, jParams.getSiteID()) > 0) {
                result = setPageURLKeyIfValidAndNotEmpty(jParams, engineMap);
            }

            if (aclService.getSiteActionPermission(LockPrerequisites.HIDE_FROM_NAVIGATION_MENU, jParams.getUser(),
                    JahiaBaseACL.READ_RIGHTS, jParams.getSiteID()) > 0) {
                setHideFromNavigationMenu(jParams, engineMap);
            }

        } else if (LINK_URL.equals(operation)) {
            String remoteURL = jParams.getParameter("remote_url");

            if (pageBean.isSharedTitle() && remoteURL != null && remoteURL.length() > 0) {
                // Set the same title to all languages
                List languageSettings = jParams.getSite().getLanguageSettings();
                Iterator languageEnum = languageSettings.iterator();
                while (languageEnum.hasNext()) {
                    String langCode = ((SiteLanguageSettings) languageEnum.next()).getCode();
                    pageBean.setRemoteURL(langCode, remoteURL);
                }
            } else if (pageBean.isSharedTitle()) { // V removed from the checkbox.
                pageBean.sharedTitle(false);
            }
            if (remoteURL != null && remoteURL.length() > 0) {
                pageBean.setRemoteURL(locale.toString(), remoteURL);
            } else {
                pageBean.removeTitle(locale.toString());
            }

            // hack to avoid bad references - default value is 1 !!
            int templateID = 1;
            // Fix for http://www.jahia.net/jira/browse/JAHIA-1690: Add try-catch block
            try {
                templateID = Integer.parseInt(jParams.getParameter("template_id"));
            } catch (final Exception e) {
                logger.debug("Unable to convert parameter template_id to int", e);
                templateID = jParams.getSite().getDefaultTemplateID();
            }
            pageBean.setPageTemplateID(templateID);
            pageBean.setPageType(JahiaPage.TYPE_URL);
            if (("".equals(title))) {
                if (remoteURL.startsWith("http://")) {
                    title = remoteURL.substring("http://".length());
                } else if (remoteURL.startsWith("https://")) {
                    title = remoteURL.substring("https://".length());
                } else {
                    title = remoteURL;
                }
                if ("".equals(title)) {
                    title = "Undefined URL";
                }
            }
            theField.setHasChanged(true);
        } else if (LINK_JAHIA_PAGE.equals(operation) ||
                MOVE_PAGE.equals(operation)) {
            int selectedPageID = pageBean.getPageLinkID();
            if (jParams.getParameter("shouldSetPageLinkID") != null) {
                selectedPageID = Integer.parseInt(jParams.getParameter("pageSelected"));
            }
            pageBean.setPageLinkID(selectedPageID);
            pageBean.setPageType(JahiaPage.TYPE_LINK);
            if ("".equals(title) && selectedPageID > -1) {
                ContentPage contentPage = JahiaPageBaseService.getInstance().
                        lookupContentPage(selectedPageID, jParams.getEntryLoadRequest(), false);
                Map titles = contentPage.getTitles(ContentPage.LAST_UPDATED_TITLES);
                for (Iterator iterator = titles.keySet().iterator(); iterator.hasNext();) {
                    String l = (String) iterator.next();
                    pageBean.setTitle(l, (String) titles.get(l));
                }
            }
            theField.setHasChanged(true);
        } else if (COPY_PAGE.equals(operation)) {
            /** todo : not implemented */
        } else if (RESET_LINK.equals(operation)) {
            theField.setHasChanged(true);
        }
        // Verify if the page title is shared
        if ("true".equals(sharedTitle) && title != null && title.length() > 0) {
            // Set the same title to all languages
            List languageSettings = jParams.getSite().getLanguageSettings();
            Iterator languageEnum = languageSettings.iterator();
            while (languageEnum.hasNext()) {
                String langCode = ((SiteLanguageSettings) languageEnum.next()).getCode();
                pageBean.setTitle(langCode, title);
            }
            pageBean.sharedTitle(true);
        }
        if (title != null && title.length() > 0) {
            pageBean.setTitle(locale.toString(), title);
        } else {
            pageBean.removeTitle(locale.toString());
        }
        checkTimeBasedPublishingStatus(jParams, pageBean);
        return result;
    }

    /**
     * saves data in datasource
     *
     * @param jParams   a ProcessingContext object
     * @param engineMap the engine hashmap
     * @param theField  the field we are working on
     * @return true if everything went okay, false if not
     * @throws JahiaException
     */
    private boolean saveData(ProcessingContext jParams, Map engineMap, JahiaField theField)
            throws JahiaException {

        logger.debug("Save data from the session");
        ContentPage contentPage = null;

        Map pageBeans = (Map) jParams.
                getSessionState().getAttribute("Page_Field.PageBeans");

        JahiaPageEngineTempBean pageBean = null;
        if (pageBeans != null) {
            pageBean = (JahiaPageEngineTempBean) pageBeans.get(theField.getDefinition().getName());
        }

        if (pageBean == null) {
            // In the case we never went to the page_field engine or if we
            // are processing a field for a page we don't have access to or
            // even in the case where the page field points to an invalid
            // page ID.
            return true;
        }
        boolean contentPageUpdated = true;
        boolean pageKeyHasChanged = false;
        String operation = pageBean.getOperation();
        if (CREATE_PAGE.equals(operation) ||
                LINK_JAHIA_PAGE.equals(operation) ||
                LINK_URL.equals(operation)) {
            Locale locale = (Locale) engineMap.get(JahiaEngine.PROCESSING_LOCALE);
            JahiaPage jahiaPage;
            if (theField.getObject() == null) { // Is it a new page ?
                if ((LINK_JAHIA_PAGE.equals(operation)) &&
                        (pageBean.getPageLinkID() == -1)) {
                    // this means we have never initialized the page we want
                    // to link to.
                    return false;
                }
                int parentAclID = theField.getAclID();

                jahiaPage = ServicesRegistry.getInstance().getJahiaPageService().
                        createPage(pageBean.getSiteID(),
                                pageBean.getParentID(),
                                pageBean.getPageType(),
                                pageBean.getTitle(locale.toString()),
                                pageBean.getPageTemplateID(),
                                pageBean.getRemoteURL(locale.toString()),
                                pageBean.getPageLinkID(),
                                null,
                                parentAclID,
                                jParams, (JahiaPageField) theField);
                final String urlKey = pageBean.getUrlKey();
                if (urlKey != null && urlKey.length() > 0) {
                    jahiaPage.getContentPage().setPageKey(urlKey);
                }

                final boolean hideFromNavigationMenu = pageBean.isHideFromNavigationMenu();
                jahiaPage.getContentPage().setProperty(PageProperty.HIDE_FROM_NAVIGATION_MENU, String.valueOf(hideFromNavigationMenu));

//                if (CREATE_PAGE.equals(operation)) {
//                    jahiaPage = ServicesRegistry.getInstance().getJahiaPageService().
//                            createPage(pageBean.getSiteID(),
//                                    pageBean.getParentID(),
//                                    JahiaPage.TYPE_LINK,
//                                    pageBean.getTitle(locale.toString()),
//                                    pageBean.getPageTemplateID(),
//                                    pageBean.getRemoteURL(),
//                                    jahiaPage.getID(),
//                                    null,
//                                    parentAclID,
//                                    jParams, (JahiaPageField) theField);
//                }
                contentPage = ServicesRegistry.getInstance().getJahiaPageService().
                        lookupContentPage(jahiaPage.getID(), false);

            } else {
                jahiaPage = (JahiaPage) theField.getObject();
                contentPage = ServicesRegistry.getInstance().getJahiaPageService().
                        lookupContentPage(jahiaPage.getID(), false);
                if (LINK_JAHIA_PAGE.equals(operation)) {
                    contentPage.setPageLinkID(pageBean.getPageLinkID(), jParams.getEntryLoadRequest());
                    // Has the page change to a LINK or URL page type. In this case
                    // the ACL has to be set to the appropriate ID.
                    if (jahiaPage.getPageType() == JahiaPage.TYPE_URL) {
                        ServicesRegistry.getInstance().getJahiaSiteMapService().
                                removeUserSiteMap(jParams.getUser().getUserKey());
                    }
                } else if (LINK_URL.equals(operation)) {
//                    contentPage.setRemoteURL(pageBean.getRemoteURL(), jParams.getEntryLoadRequest());
                    // Has the page change to an URL page type ? In this case (re)create an ACL.
                    ServicesRegistry.getInstance().getJahiaSiteMapService().
                            removeUserSiteMap(jParams.getUser().getUserKey());
                }
                contentPage.setPageType(pageBean.getPageType(), jParams.getEntryLoadRequest());
            }
            theField.setValue(Integer.toString(jahiaPage.getID()));
            theField.setObject(jahiaPage);
            theField.save(jParams);
            String lang = (String) contentPage.getLanguagesStates(false).keySet().iterator().next();

            WorkflowEvent theEvent = new WorkflowEvent(this, contentPage, jParams.getUser(), lang, false);
            ServicesRegistry.getInstance().getJahiaEventService().fireObjectChanged(theEvent);
        } else if (UPDATE_PAGE.equals(operation)) {
            // Set the object field

            JahiaPage jahiaPage = ServicesRegistry.getInstance().getJahiaPageService().
                    lookupPage(pageBean.getID(), jParams.getEntryLoadRequest(),
                            jParams.getOperationMode(), jParams.getUser(), false);
            if (jahiaPage == null) {
                logger.warn("Error during update of page " + pageBean.getID() + " for user " + jParams.getUser() + "in operation mode " + jParams.getOperationMode() + " with en entry load request " + jParams.getEntryLoadRequest() + ".It seems the page is no longer accessible for this user");
                return false;
            }

            final String urlKey = pageBean.getUrlKey();
            String oldPageKey = jahiaPage.getProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
            pageKeyHasChanged = (oldPageKey == null && (urlKey != null && urlKey.length() > 0)) || (oldPageKey != null && (urlKey == null
                    || !urlKey.equals(oldPageKey)));
            if (pageKeyHasChanged) {
                jahiaPage.getContentPage().setPageKey(urlKey);
                jahiaPage.getContentPage().setUnversionedChanged();
            }

            final boolean hideFromNavigationMenu = pageBean.isHideFromNavigationMenu();
            final boolean oldHideFromNavigationMenu = Boolean.valueOf(jahiaPage.getProperty(PageProperty.HIDE_FROM_NAVIGATION_MENU));
            if (hideFromNavigationMenu != oldHideFromNavigationMenu) {
                jahiaPage.getContentPage().setProperty(PageProperty.HIDE_FROM_NAVIGATION_MENU, String.valueOf(hideFromNavigationMenu));
            }

            contentPage = ServicesRegistry.getInstance().getJahiaPageService().
                    lookupContentPage(jahiaPage.getID(), false);

            // Has the page change to a DIRECT page type ? In this case (re)create an ACL.
            if (jahiaPage.getPageType() == ContentPage.TYPE_LINK) {
                JahiaBaseACL acl = new JahiaBaseACL();
                acl.create(theField.getAclID());
                /** todo reset the previous ACL entry if needed */
                contentPage.setAclID(acl.getID(), jParams.getEntryLoadRequest());
                ServicesRegistry.getInstance().getJahiaSiteMapService().
                        removeUserSiteMap(jParams.getUser().getUserKey());
            }
            if (pageBean.getPageTemplateID() != contentPage.getPageTemplateID(jParams.getEntryLoadRequest())) {
                contentPage.setPageTemplateID(pageBean.getPageTemplateID(), jParams.getEntryLoadRequest());
            } else {
                contentPageUpdated = false;
            }

            if (!Integer.toString(pageBean.getID()).equals(theField.getValue())) {
                theField.setObject(jahiaPage);
                // Save the field modifications
                theField.save(jParams);
            }

        } else if (MOVE_PAGE.equals(operation)) {

            // Get the cache instance BEFORE applying any modification in case the cache cannot
            // be found for any mysterious reason ;o) THIS SHOULD STAY THE FIRST ACTION TO
            // PREVENT THE NECESSITY OF ROLLLING BACK PREVIOUS OPERATIONS IN CASE THE CACHE
            // IN NOT AVAILABLE!!
            Cache pageChildCache = ServicesRegistry.getInstance().getCacheService().getCache(JahiaPageService.PAGE_CHILD_CACHE);
            if (pageChildCache == null)
                throw new JahiaException("Internal Cache error", "Could not get the cache [" +
                        JahiaPageService.PAGE_CHILD_CACHE + "] instance.",
                        JahiaException.CACHE_ERROR, JahiaException.CRITICAL_SEVERITY);

            int movedPageID = pageBean.getPageLinkID();
            if (movedPageID == -1) return false;

            contentPage = ContentPage.getPage(movedPageID);

            int oldParentFieldID = ServicesRegistry.getInstance().getJahiaPageService()
                    .getPageFieldID(contentPage.getID());

            ContentPageField contentPageField = null;
            ContentContainer oldParentContainer = null;
            ContentObject newParentContainer = null;

            boolean isAclSameAsParent = contentPage.isAclSameAsParent();
            int containerAclIDToSet = 0;
            if (isAclSameAsParent) {
                contentPageField = (ContentPageField) ContentPageField
                        .getField(oldParentFieldID);
                if (contentPageField != null && contentPageField.isAclSameAsParent()) {
                    oldParentContainer = ContentContainer
                            .getContainer(contentPageField.getContainerID());
                    if (oldParentContainer != null && !oldParentContainer.isAclSameAsParent()) {
                        containerAclIDToSet = oldParentContainer.getAclID();
                    }
                }
            }

            TimeBasedPublishingService tbpService = ServicesRegistry.getInstance().getTimeBasedPublishingService();
            JahiaObjectManager jahiaObjectMgr = (JahiaObjectManager) SpringContextSingleton.getInstance().getContext()
                    .getBean(JahiaObjectManager.class.getName());
            RetentionRule retRule = tbpService.getRetentionRule(contentPage.getObjectKey());

            // check whether there is a TBP rule on the container, which needs to be moved
            if (retRule == null || retRule.getInherited().booleanValue()) {
                retRule = null;
                ObjectKey tbpObjectKey = tbpService.getParentObjectKeyForTimeBasedPublishing(
                        contentPage.getObjectKey(), jParams.getUser(), jParams.getEntryLoadRequest(), jParams
                                .getOperationMode(), true);

                if (tbpObjectKey != null) {
                    if (oldParentFieldID != -1) {
                        ObjectKey oldContaienrObjKey = null;
                        if (contentPageField == null) {
                            contentPageField = (ContentPageField) ContentPageField
                                    .getField(oldParentFieldID);
                        }

                        if (contentPageField != null) {
                            if (oldParentContainer == null) {
                                oldParentContainer = ContentContainer
                                        .getContainer(contentPageField
                                                .getContainerID());
                            }
                            oldContaienrObjKey = oldParentContainer.getObjectKey();
                        }
                        if (tbpObjectKey.equals(oldContaienrObjKey)) {
                            retRule = tbpService.getRetentionRule(tbpObjectKey);
                        }
                    }
                }
            } else {
                // set null as the rule will stay assigned to the moved page
                retRule = null;
            }

            // 0. Flush moved page cache to garantize to have the last staged.
            ServicesRegistry.getInstance().getJahiaPageService().invalidatePageCache(movedPageID);

            // 1. Relink the parent page to the new one, this also marks for
            // deletion the parent field and container
            int oldParentPageID = contentPage.getParentID(jParams.getEntryLoadRequest());
            contentPage.setParentID(pageBean.getParentID(), jParams);
            contentPage.commitChanges(true, true, jParams.getUser());

            if (oldParentPageID != pageBean.getParentID()) {
                contentPage.updateContentPagePath(jParams);
            }

            if (ServicesRegistry.getInstance().getWorkflowService().getWorkflowMode(contentPage) == WorkflowService.LINKED) {
                ServicesRegistry.getInstance().getWorkflowService().setWorkflowMode(contentPage, WorkflowService.INHERITED, null, null, jParams);
            }
            JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, contentPage);
            ServicesRegistry.getInstance().getJahiaEventService()
                    .fireContentObjectUpdated(objectCreatedEvent);

            // 2. Link the page ID to the new field
            theField.setValue(Integer.toString(movedPageID));
            // Set the object field
            JahiaPage jahiaPage = ServicesRegistry.getInstance().getJahiaPageService().
                    lookupPage(movedPageID, jParams);
            theField.setObject(jahiaPage);
            // Save the field modifications
            theField.save(jParams);

            // Reload the content page
            contentPage = ContentPage.getPage(contentPage.getID());

            // 3. Relink the parent ACL
            if (containerAclIDToSet != 0) {
                ContentObject contentObject = contentPage.getParent(jParams
                        .getEntryLoadRequest());
                if (contentObject != null) {
                    newParentContainer = contentObject.getParent(jParams
                            .getEntryLoadRequest());
                }
                if (newParentContainer != null && newParentContainer.isAclSameAsParent()) {
                    int oldAclId = newParentContainer.getAclID();
                    if (pageBean.deleteOldContainer()) {
                        newParentContainer.setAclID(containerAclIDToSet);
                        newParentContainer.getACL().setParentID(oldAclId);
                    } else {
                        JahiaBaseACL oldAcl = new JahiaBaseACL(containerAclIDToSet);

                        JahiaBaseACL acl = new JahiaBaseACL();
                        acl.create(newParentContainer.getAclID());
                        acl.setParentID(oldAclId);

                        JahiaGroupManagerService grpMgrService = ServicesRegistry
                                .getInstance().getJahiaGroupManagerService();
                        List<String> groupNames = oldAcl.getGroupnameList(null);

                        for (String groupName : groupNames) {
                            JahiaGroup group = grpMgrService
                                    .lookupGroup(groupName);
                            JahiaAclEntry entry = oldAcl.getGroupEntry(group);
                            acl.setGroupEntry(group, entry);
                        }

                        JahiaUserManagerService userMgrService = ServicesRegistry
                                .getInstance().getJahiaUserManagerService();
                        List<String> userNames = oldAcl.getUsernameList(null);

                        for (String userName : userNames) {
                            JahiaUser user = userMgrService
                                    .lookupUserByKey(userName);
                            JahiaAclEntry entry = oldAcl.getUserEntry(user);
                            acl.setUserEntry(user, entry);
                        }
                        newParentContainer.updateAclForChildren(acl.getID());
                    }
                }
            } else if (theField.getAclID() != contentPage.getAclID()) {
                if (isAclSameAsParent) {
                    contentPage.updateAclForChildren(theField.getACL().getID());
                } else {
                    contentPage.getACL().setParentID(theField.getACL().getID());
                }
            }

            // 4. Invalidate all site maps.
            ServicesRegistry.getInstance().getJahiaSiteMapService().resetSiteMap();

            // 5. Flush page children cache.
            pageChildCache.remove(Integer.toString(pageBean.getParentID()));
            pageChildCache.remove(Integer.toString(oldParentPageID));

            // 6. Optionally move any TBP settings
            if (retRule != null) {
                if (newParentContainer == null) {
                    ContentObject contentObject = contentPage.getParent(jParams
                            .getEntryLoadRequest());
                    if (contentObject != null) {
                        newParentContainer = contentObject.getParent(jParams
                                .getEntryLoadRequest());
                    }
                }
                if (newParentContainer != null) {
                    JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectMgr.getJahiaObjectDelegate(newParentContainer
                            .getObjectKey());
                    if (jahiaObjectDelegate.getRule() == null
                            || jahiaObjectDelegate.getRule().getInherited().booleanValue()) {
                        jahiaObjectDelegate.setRule(retRule);
                        jahiaObjectMgr.save(jahiaObjectDelegate);

                        try {
                            tbpService.scheduleBackgroundJob(newParentContainer.getObjectKey(),
                                    TimeBasedPublishingJob.UPDATE_OPERATION, retRule, jParams);
                        } catch (Exception e) {
                            throw new JahiaException("Error scheduling Retention Rule at import",
                                    "Error scheduling Retention Rule at import", JahiaException.ENGINE_ERROR,
                                    JahiaException.ERROR_SEVERITY, e);
                        }
                        RetentionRuleEvent event = new RetentionRuleEvent(this, jParams, retRule.getId().intValue(),
                                RetentionRuleEvent.UPDATING_RULE, -1);
                        ServicesRegistry.getInstance().getJahiaEventService().fireTimeBasedPublishingStateChange(event);
                    }
                }
            }

            // 7. Delete or not the old parent container
            if (pageBean.deleteOldContainer()) {
                if (oldParentFieldID != -1) {
                    // 1. Cut the page link in the parent field value
                    if (contentPageField == null) {
                        contentPageField = (ContentPageField) ContentPageField.getField(oldParentFieldID);
                    }
                    // mark the parent container for deletion
                    contentPageField.setPageID(-1, jParams.getUser(), false);

                    try {
                        if (oldParentContainer == null) {
                            oldParentContainer = ContentContainer.getContainer(contentPageField.getContainerID());
                        }
                        StateModificationContext stateModificationContext = new StateModificationContext(
                                oldParentContainer.getObjectKey(), null, true);
                        stateModificationContext.popAllLanguages();
                        oldParentContainer.markLanguageForDeletion(jParams.getUser(), ContentObject.SHARED_LANGUAGE,
                                stateModificationContext);
                    } catch (Exception t) {
                        logger.debug("Parent Container [" + contentPageField.getContainerID() + "] of page field["
                                + contentPageField.getID() + "] not found");
                    }
                }
            }

        } else if (COPY_PAGE.equals(operation)) {
            /** todo To implement */
        } else if (RESET_LINK.equals(operation)) {

            if (pageBean.getID() > -1) {
                /*
                 * PAGE_MOVE_LOGIC we dont wan't to mark anything for delete
                 * with page move issue !!!! // 1. mark actual content for
                 * delete contentPage =
                 * ServicesRegistry.getInstance().getJahiaPageService().
                 * lookupContentPage(pageBean.getID(), false); ContentPageField
                 * contentPageField =
                 * (ContentPageField)ContentPageField.getField(theField.getID());
                 * Map languageStates = contentPage.getLanguagesStates(false);
                 * Set languageCodes = languageStates.keySet();
                 * StateModificationContext stateModifContext = new
                 * StateModificationContext(new
                 * ContentFieldKey(theField.getID()),languageCodes); Iterator
                 * languageCodeIter = languageCodes.iterator(); while
                 * (languageCodeIter.hasNext()) { String curLanguageCode =
                 * (String) languageCodeIter.next();
                 * contentPageField.markLanguageForDeletion( jParams.getUser(),
                 * curLanguageCode, stateModifContext); }
                 */
                // 2. set field page field value to -1.
                ContentPageField contentPageField =
                        (ContentPageField) ContentPageField.getField(theField.getID());
                contentPageField.setPageID(-1, jParams.getUser(), false);
            }

            // Because the container and the associated field is always created
            // before this sequence, they will be always deleted.
            // Mark field and/or container for deletion to avoid having fake
            // DB entries. Fake container entries are displayed in the parent container list.

            /**
             *
             ContentPageField contentPageField = (ContentPageField)ContentPageField.getField(theField.getID());
             boolean shouldDeleteFieldOnly = true;
             int containerID = contentPageField.getContainerID();
             if (containerID != 0) {
             JahiaContainer theContainer = ServicesRegistry.getInstance().getJahiaContainersService().
             loadContainer(containerID, LoadFlags.ALL, jParams);
             if (theContainer.getNbFields() == 1) { // Is it a one page field container ?
             // In this case it can be a fake container, remove it.
             ServicesRegistry.getInstance().getJahiaContainersService().
             deleteContainer(contentPageField.getContainerID(), jParams);
             shouldDeleteFieldOnly = false; // Field is deleted too.
             }
             }
             if (shouldDeleteFieldOnly && pageBean.getID() > -1) {
             contentPage = ServicesRegistry.getInstance().getJahiaPageService().
             lookupContentPage(pageBean.getID(), false);
             Map languageStates = contentPage.getLanguagesStates(false);
             Set languageCodes = languageStates.keySet();
             StateModificationContext stateModifContext = new StateModificationContext(new ContentFieldKey(theField.getID()),languageCodes);
             Iterator languageCodeIter = languageCodes.iterator();
             while (languageCodeIter.hasNext()) {
             String curLanguageCode = (String) languageCodeIter.next();
             contentPageField.markLanguageForDeletion(
             jParams.getUser(), curLanguageCode,
             stateModifContext);
             }
             contentPageField.markLanguageForDeletion(
             jParams.getUser(), ContentObject.SHARED_LANGUAGE,
             stateModifContext);

             ServicesRegistry.getInstance().getJahiaPageService()
             .invalidatePageCache(contentPage.getID());
             }
             */

            // FIXME NK :Why not ?
            // contentPage = null; // Don't store title !
        }
        // Set Jahia page titles only if changed.
        if (contentPage != null && (contentPage.hasActiveEntries() || contentPage.hasStagingEntries())) {
            Iterator titlesEnum = pageBean.getTitles().keySet().iterator();
            Map contentPageTitles = contentPage.getTitles(true);
            // Look for all page titles stored in the page bean and set the page
            // that has changed.
            boolean titleHasChanged = false;
            while (titlesEnum.hasNext()) {
                String languageCode = (String) titlesEnum.next();
                String titleCandidate = pageBean.getTitle(languageCode);
                if (!titleCandidate.equals(contentPageTitles.get(languageCode))) {
                    contentPage.setTitle(languageCode, titleCandidate, jParams.getEntryLoadRequest());
                    titleHasChanged = true;
                }
            }
            Iterator it = pageBean.getRemoteURLs().keySet().iterator();
            Map remoteUrl = contentPage.getRemoteURLs(true);

            while (it.hasNext()) {
                String languageCode = (String) it.next();
                String urlCandidate = pageBean.getRemoteURL(languageCode);
                if (!urlCandidate.equals(remoteUrl.get(languageCode))) {
                    contentPage.setRemoteURL(languageCode, urlCandidate, jParams.getEntryLoadRequest());
                }
            }

            if (contentPageUpdated || titleHasChanged || pageKeyHasChanged) {
                contentPage.commitChanges(true, true, jParams.getUser());

                JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, contentPage);
                ServicesRegistry.getInstance().getJahiaEventService()
                        .fireContentObjectUpdated(objectCreatedEvent);

                // we need this as we want to parent container to fire the correct event so that the container
                // will be re-indexed with the correct page title.
                jParams.getSessionState().setAttribute("FireContainerUpdated", "true");
            }
            if (titleHasChanged) {
                // handled by previous event
                //ServicesRegistry.getInstance().getJahiaSearchService()
                //        .indexPage(contentPage.getID(), jParams.getUser());

                ServicesRegistry.getInstance()
                        .getJahiaSiteMapService().resetSiteMap();

                /*
                // index page title
                int pageFieldID = ServicesRegistry.getInstance()
                        .getJahiaPageService().getPageFieldID(contentPage.getID());
                if (pageFieldID != -1) {
                    try {
                        ContentField field = ContentField.getField(pageFieldID);
                        ServicesRegistry.getInstance()
                            .getJahiaSearchService().indexContainer(field.getContainerID(), jParams.getUser());
                    } catch ( Exception t ){
                    }
                }*/
            }
        }

        // handled by contentObjectUpdated event
        //ServicesRegistry.getInstance().getJahiaSearchService()
        //        .indexPage(contentPage.getID(), jParams.getUser());

        return true;
    }

    /**
     * Compose a Jahia temporary page for the field edition.
     * Three cases should be considered :
     * 1) The page already exists in the session (PageBean...) meaning that
     * the page field is currently edited.
     * 2) The page does not exist in the session and no object is set to the
     * field meaning that a new page shoud be created. In this case, a temp page
     * is created and set to the session.
     * 3) The page does not exist in the session and an object (a page in fact)
     * is already defined to the field object.
     *
     * @param jParams  a ProcessingContext object
     * @param theField the field we are working on
     * @return The temp page object
     * @throws JahiaException
     */
    private JahiaPageEngineTempBean composePage(ProcessingContext jParams, JahiaField theField)
            throws JahiaException {

        JahiaPageEngineTempBean pageBean;

        Map pageBeans = (Map) jParams.
                getSessionState().getAttribute("Page_Field.PageBeans");
        if (pageBeans == null) {
            pageBeans = new HashMap();
            jParams.getSessionState().setAttribute("Page_Field.PageBeans", pageBeans);
        }

        // Verify if this page field was not already edited in this session.
        pageBean = (JahiaPageEngineTempBean) pageBeans.get(theField.getDefinition().getName());

        if (pageBean == null) {
            // First call or recall of engine.
            if (theField.getObject() == null) {
                // Is it a new page ?

                // Is there a valid page ID in the field value ? If yes,
                // this could mean we are denied access to the page.
                int testPageID;
                try {
                    testPageID = Integer.parseInt(theField.getValue());
                } catch (NumberFormatException nfe) {
                    testPageID = -1;
                }
                if (testPageID > 0) {
                    ContentPage contentPage = null;
                    try {
                        contentPage = ContentPage.getPage(testPageID);
                    } catch (JahiaException je) {
                        logger.error(je.getMessage(), je);
                    }
                    if (contentPage != null) {
                        // if we reach this case, the page ID is valid,
                        // which means we are dealing with a page we do
                        // not have access to.
                        return null;
                    }
                }
                logger.debug("New temp page... (theField.getObject() was null)");
                boolean isLinkOnly = theField.getValue().toLowerCase().indexOf("jahia_linkonly") != -1;
                pageBean = new JahiaPageEngineTempBean(
                        -1, // Page ID
                        theField.getJahiaID(),
                        theField.getPageID(),
                        isLinkOnly ? JahiaPage.TYPE_URL : // URL type per default
                                JahiaPage.TYPE_DIRECT, // or create a new page per default
                        jParams.getSite().getDefaultTemplateID(), // Page default template
                        // URL undefined
                        -1, // Link ID undefined
                        jParams.getUser().getUserKey(),
                        theField.getID()); // value should be < 0 if new field.
                // pageBean.setOperation(isLinkOnly ? LINK_URL : CREATE_PAGE);
                pageBean.setOperation(RESET_LINK);
            } else {
                // We've got something in theField.object

                logger.debug("Get existing field page... (We've got something in theField.object())");
                JahiaPage jahiaPage = (JahiaPage) theField.getObject();

                ContentPage contentPage;
                try {
                    contentPage = JahiaPageBaseService.getInstance().
                            lookupContentPage(jahiaPage.getID(), jParams.getEntryLoadRequest(), true);
                } catch (JahiaTemplateNotFoundException tnfe) {
                    logger.debug("Template not foudn for page[" + jahiaPage.getID() + "], try return a ContentPage without template");
                    contentPage = JahiaPageBaseService.getInstance().
                            lookupContentPage(jahiaPage.getID(), jParams.getEntryLoadRequest(), false);
                }
                pageBean = new JahiaPageEngineTempBean(
                        contentPage.getID(),
                        contentPage.getJahiaID(),
                        contentPage.getParentID(jParams.getEntryLoadRequest()),
                        contentPage.getPageType(jParams.getEntryLoadRequest()),
                        contentPage.getPageTemplateID(jParams),
                        contentPage.getPageLinkID(jParams.getEntryLoadRequest()),
                        null,
                        theField.getID());

                final PageProperty urlKeyProp = jahiaPage.getPageLocalProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
                if (urlKeyProp != null) {
                    pageBean.setUrlKey(urlKeyProp.getValue());
                }

                final PageProperty hideFromMenuProp = jahiaPage.getPageLocalProperty(PageProperty.HIDE_FROM_NAVIGATION_MENU);
                if (hideFromMenuProp != null) {
                    pageBean.setHideFromNavigationMenu(Boolean.valueOf(hideFromMenuProp.getValue()));
                }

                if (contentPage.getPageType(jParams.getEntryLoadRequest()) != -1) {
                    pageBean.setOperation(DEFAULT_OPERATION[contentPage.getPageType(jParams.getEntryLoadRequest())]);
                }
                pageBean.setTitles(contentPage.getTitles(ContentPage.LAST_UPDATED_TITLES));
                pageBean.setRemoteURLs(contentPage.getRemoteURLs(true));
            }
            pageBeans.put(theField.getDefinition().getName(), pageBean);
        }
        logger.debug(pageBean.toString());
        return pageBean;
    }

    /**
     * Validate and set page URL key if its present.
     * To be valid, the key must :
     * - not be a jahia reserved word
     * - be composed of caracters in 0 to 9, a to z or A to Z, length min 2 max 250
     * - not be used by another page on current jahia site
     * <p/>
     * The validation stops as soon as one error condition is encountered. Higher cost validation is performed last in the chain.
     *
     * @param jParams   ProcessingContext
     * @param engineMap engineMap
     * @return true if the key is valid and was setted, false if empty or not valid.
     */
    private boolean setPageURLKeyIfValidAndNotEmpty(final ProcessingContext jParams,
                                                    final Map engineMap) throws JahiaException {
        final String pageURLKey = jParams.getParameter("pageURLKey");
        if (pageURLKey == null) return false;
        final JahiaPageEngineTempBean pageTempBean = (JahiaPageEngineTempBean) engineMap.get("pageTempBean");
        if (pageURLKey.trim().length() == 0) {
            pageTempBean.setUrlKey(null);
            engineMap.remove("dataPageURLKey");
            return true;
        }

        engineMap.put("dataPageURLKey", pageURLKey);
        pageTempBean.setUrlKey(pageURLKey);
        final EngineMessages engineMessages = new EngineMessages();
        if (pageURLKey.length() > 250) {
            engineMessages.add("pageProperties", new EngineMessage("org.jahia.engines.pages.PageProperties_Engine.urlKeytooLong.label", pageURLKey.substring(0, 50) + "..."));
            engineMessages.saveMessages(((ParamBean) jParams).getRequest());
            return false;
        } else if (ParamBean.isReservedKeyword(pageURLKey)) {
            engineMessages.add("pageProperties", new EngineMessage("org.jahia.engines.pages.PageProperties_Engine.urlKeyIsReservedWord.label", pageURLKey));
            engineMessages.saveMessages(((ParamBean) jParams).getRequest());
            return false;
        } else if (!PageProperties_Engine.isValidURLKey(pageURLKey)) {
            engineMessages.add("pageProperties", new EngineMessage("org.jahia.engines.pages.PageProperties_Engine.urlKeyHasInvalidChars.label", pageURLKey));
            engineMessages.saveMessages(((ParamBean) jParams).getRequest());
            return false;
        } else if (PageProperties_Engine.isURLKeyAlreadyUsed(pageURLKey, pageTempBean)) {
            engineMessages.add("pageProperties", new EngineMessage("org.jahia.engines.pages.PageProperties_Engine.urlKeyIsDuplicate.label", pageURLKey));
            engineMessages.saveMessages(((ParamBean) jParams).getRequest());
            return false;
        }
        return true;
    }

    private void setHideFromNavigationMenu(final ProcessingContext jParams,
                                           final Map engineMap) {
        final boolean hideFromNavigationMenu = jParams.getParameter("hideFromNavigationMenu") != null;
        final JahiaPageEngineTempBean pageTempBean = (JahiaPageEngineTempBean) engineMap.get("pageTempBean");
        engineMap.put("hideFromNavigationMenu", hideFromNavigationMenu);
        pageTempBean.setHideFromNavigationMenu(hideFromNavigationMenu);
    }

}
