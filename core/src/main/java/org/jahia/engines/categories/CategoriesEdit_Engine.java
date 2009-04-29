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
 package org.jahia.engines.categories;

import org.jahia.admin.categories.ManageCategories;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.data.events.JahiaEvent;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.audit.ManageLogs_Engine;
import org.jahia.engines.rights.ManageRights;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.hibernate.manager.JahiaSiteLanguageListManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.acl.ACLResource;
import org.jahia.services.categories.Category;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.JahiaObjectTool;
import org.jahia.utils.LanguageCodeConverters;

import java.util.*;

/**
 * Used in administration to edit categories.
 * User: Serge Huber
 * Date: 17 janv. 2006
 * Time: 15:58:26
 * Copyright (C) Jahia Inc.
 */
public class CategoriesEdit_Engine implements JahiaEngine {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (CategoriesEdit_Engine.class);

    private static final String CATEGORY_JSP = "edit_category";
    public static final String CATEGORY_SESSION_NAME = "category";
    public static final String CATEGORYKEY_SESSION_NAME = "categoryKey";
    public static final String ENGINE_MODE_SESSION_NAME = "categoryEngineMode";

    // the temporary template to hold change until they have to be saved.
    public static final String TEMPORARY_CATEGORY_SESSION_NAME = "temporaryCategory";

    public static final String ENGINE_NAME = "categoryEdit";
    private EngineToolBox toolBox;

    private final String WRITE_ACCESS_STR = "writeAccess";
    private final String ADMIN_ACCESS_STR = "adminAccess";

    private final String SCREEN_STR = "screen";

    private final String EDIT_STR = "edit";
    private final String SAVE_STR = "save";
    private final String CANCEL_STR = "cancel";
    private final String LOGS_STR = "logs";
    private final String APPLY_STR = "apply";
    private final String CLOSE_STR = "close";
    private final String LASTSCREEN_STR = "lastscreen";
    private final String JSPSOURCE_STR = "jspSource";

    public class CategoryTemporaryBean {
        private String key = null;
        private Map titles;
        private Properties properties;
        private boolean newCategory = true;

        public CategoryTemporaryBean() {
            titles = new HashMap();
            properties = new Properties();
            JahiaSiteLanguageListManager listManager = (JahiaSiteLanguageListManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageListManager.class.getName());
            List allLanguageCodes = listManager.getAllSitesLanguages();
            Iterator allLanguageCodeIter = allLanguageCodes.iterator();
            while (allLanguageCodeIter.hasNext()) {
                String curLanguageCode = (String) allLanguageCodeIter.next();
                titles.put(curLanguageCode, "");
            }
        }

        public CategoryTemporaryBean(Category category) {
            this.newCategory = false;
            this.key = category.getKey();
            this.titles = new HashMap();
            JahiaSiteLanguageListManager listManager = (JahiaSiteLanguageListManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageListManager.class.getName());
            List allLanguageCodes = listManager.getAllSitesLanguages();
            Iterator allLanguageCodeIter = allLanguageCodes.iterator();
            while (allLanguageCodeIter.hasNext()) {
                String curLanguageCode = (String) allLanguageCodeIter.next();
                this.titles.put(curLanguageCode,
                             category.getTitle(
                    LanguageCodeConverters.
                    languageCodeToLocale(curLanguageCode)));
            }
            properties = category.getProperties();
        }

        public Map getTitles() {
            return titles;
        }

        public Properties getProperties() {
            return properties;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public boolean isNewCategory() {
            return newCategory;
        }

    }

    /**
     * constructor
     */
    public CategoriesEdit_Engine () {
        toolBox = EngineToolBox.getInstance ();
    }

    /**
     * authoriseRender
     */
    public boolean authoriseRender (ProcessingContext jParams) {
        return true; // we do not check if we are in Edit mode
    }

    /**
     * renderLink
     */
    public String renderLink (ProcessingContext jParams, Object theObj)
            throws JahiaException {
        String params = null;

        if (theObj instanceof Category) {
            Category category = (Category) theObj;
            if (!ACLResource.checkWriteAccess(category, category, jParams.getUser())) {
                return "";
            }
            params = "?mode=display&categoryKey=" + category.getObjectKey().getIdInType();
        } else {
            /* @todo we need to check parent category ACL for write access before rendering this link */
            String parentCategoryKey = (String) theObj;
            Category parentCategory = Category.getCategory(Integer.parseInt(parentCategoryKey), jParams.getUser());
            if (parentCategory == null) {
                return "";
            }
            if (!ACLResource.checkWriteAccess(parentCategory, parentCategory, jParams.getUser())) {
                return "";
            }
            params = "?mode=display&parentCategoryKey=" + parentCategory.getObjectKey().getIdInType();
        }

        return jParams.composeEngineUrl (ENGINE_NAME, params);
    }

    /**
     * needsJahiaData
     */
    public boolean needsJahiaData (ProcessingContext jParams) {
        return false;
    }

    /**
     * handles the engine actions
     *
     * @param jParams a ProcessingContext object
     * @param jData   a JahiaData object (not mandatory)
     */
    public EngineValidationHelper handleActions (ProcessingContext jParams, JahiaData jData)
            throws JahiaException,
            JahiaSessionExpirationException,
            JahiaForbiddenAccessException {
        // initalizes the hashmap
        Map engineMap = initEngineMap (jParams);

        // checks if the user has the right to display the engine
        Category category =
                (Category) engineMap.get (CATEGORY_SESSION_NAME);
        JahiaUser theUser = jParams.getUser ();
        String theScreen = (String) engineMap.get (SCREEN_STR);
        if (theScreen.equals (CANCEL_STR)) {
            engineMap.put (ENGINE_OUTPUT_FILE_PARAM, JahiaEngine.CANCEL_JSP);
        } else {
        if (category == null) {
            engineMap.put("enableAuthoring", Boolean.TRUE);
            engineMap.put (WRITE_ACCESS_STR, Boolean.TRUE);
        } else if (ACLResource.checkAdminAccess (category, category, theUser)) {
            engineMap.put (ADMIN_ACCESS_STR, Boolean.TRUE);
            engineMap.put("enableAuthoring", Boolean.TRUE);
            engineMap.put ("enableRightView", Boolean.TRUE);
            engineMap.put (WRITE_ACCESS_STR, Boolean.TRUE);

        } else if (ACLResource.checkWriteAccess (category, category, theUser)) {
            engineMap.put (WRITE_ACCESS_STR, Boolean.TRUE);
        }

        if (engineMap.get (WRITE_ACCESS_STR) != null) {
            processLastScreen (jParams, engineMap);
            processCurrentScreen (jParams, engineMap);
        } else {
            throw new JahiaForbiddenAccessException ();
        }  }

        // displays the screen
        toolBox.displayScreen (jParams, engineMap);

        return null;
    }

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName () {
        return ENGINE_NAME;
    }


    /**
     * processes the last screen sent by the user
     *
     * @param jParams   a ProcessingContext object
     * @param engineMap the engine map
     */
    public void processLastScreen (ProcessingContext jParams, Map engineMap)
            throws JahiaException,
            JahiaForbiddenAccessException {
        Category category =
                (Category) engineMap.get (CATEGORY_SESSION_NAME);

        // gets the last screen
        // lastscreen   = edit, rights, logs
        String lastScreen = jParams.getParameter (LASTSCREEN_STR);
        if (lastScreen == null) {
            lastScreen = EDIT_STR;
        }

        int mode = JahiaEngine.UPDATE_MODE;

        if (lastScreen.equals (EDIT_STR)) {
            if (!processCategoryEdit (jParams, mode, engineMap)) {
                // if there was an error, come back to last screen
                engineMap.put (SCREEN_STR, lastScreen);
                engineMap.put (JSPSOURCE_STR, CATEGORY_JSP);
            }
        } else if (lastScreen.equals ("rightsMgmt")) {
            if (engineMap.get (ADMIN_ACCESS_STR) != null) {
                ManageRights.getInstance ()
                        .handleActions (jParams, mode, engineMap, category.getAclID (), category, category);
            } else {
                throw new JahiaForbiddenAccessException ();
            }
        } else if (lastScreen.equals (LOGS_STR)) {
            if (engineMap.get (ADMIN_ACCESS_STR) != null) {
                ManageLogs_Engine.getInstance ().handleActions (jParams, mode, engineMap, null);
            } else {
                throw new JahiaForbiddenAccessException ();
            }
        }
    }


    /**
     * prepares the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public void processCurrentScreen (ProcessingContext jParams, Map engineMap)
            throws JahiaException,
            JahiaForbiddenAccessException {
        // gets the current screen
        // screen   = edit, rights, logs
        String theScreen = (String) engineMap.get (SCREEN_STR);
        Category category =
                (Category) engineMap.get (CATEGORY_SESSION_NAME);

        // indicates to sub engines that we are processing last screen
        int mode = JahiaEngine.LOAD_MODE;

        // dispatches to the appropriate sub engine
        if (theScreen.equals (EDIT_STR)) {
            displayEditCategory (jParams, mode, engineMap);
        } else if (theScreen.equals (LOGS_STR)) {
            toolBox.loadLogData (jParams, JahiaObjectTool.CATEGORY_TYPE, engineMap);
        } else if (theScreen.equals ("rightsMgmt")) {
            if (engineMap.get (ADMIN_ACCESS_STR) != null) {
                ManageRights.getInstance ()
                        .handleActions (jParams, mode, engineMap, category.getAclID (), category, category);
            } else {
                throw new JahiaForbiddenAccessException ();
            }
        } else if (theScreen.equals (SAVE_STR) || theScreen.equals (APPLY_STR)) {
            mode = JahiaEngine.SAVE_MODE;
            if (processCategorySave (jParams, engineMap)) {
                if (engineMap.get (ADMIN_ACCESS_STR) != null) {
                    engineMap.put ("logObjectType",
                            Integer.toString (JahiaObjectTool.CATEGORY_TYPE));
                    engineMap.put ("logObject", category);
                    ManageRights.getInstance ()
                            .handleActions (jParams, mode, engineMap, category.getAclID (), category, category);
                }

                JahiaEvent theEvent = new JahiaEvent (this, jParams, category);
                ServicesRegistry.getInstance ().getJahiaEventService ().fireUpdateCategory (
                        theEvent);
            } else {
                // if there was an error, come back to last screen
                engineMap.put (SCREEN_STR, EDIT_STR);
                engineMap.put (JSPSOURCE_STR, CATEGORY_JSP);
            }
            if (theScreen.equals (APPLY_STR)) {

                engineMap.put (SCREEN_STR, jParams.getParameter (LASTSCREEN_STR));
            }
            logger.debug ("Saving !!");
        } else if (theScreen.equals (CANCEL_STR)) {
            engineMap.put (ENGINE_OUTPUT_FILE_PARAM, JahiaEngine.CANCEL_JSP);
        }
    }


    /**
     * inits the engine map
     *
     *
     * @return HashMap, a Map object containing all the basic values needed by an engine
     */
    private Map initEngineMap (ProcessingContext jParams)
            throws JahiaException,
            JahiaSessionExpirationException {

        Map engineMap = new HashMap();
        Category category = null;

        // gets session values
        SessionState theSession = jParams.getSessionState ();

        // tries to find if this is the first screen generated by the engine
        String theScreen = jParams.getParameter (SCREEN_STR);
        if (theScreen != null) {
            // if no, load the engine map value from the session
            engineMap = (Map) theSession.getAttribute (ProcessingContext.SESSION_JAHIA_ENGINEMAP);
            if (engineMap == null) {
                throw new JahiaSessionExpirationException ();
            }
            category = (Category) engineMap.get (CATEGORY_SESSION_NAME);
            if(category != null){
                engineMap.put (ENGINE_URL_PARAM,jParams.composeEngineUrl (ENGINE_NAME, "?categoryKey=" + category.getObjectKey().getIdInType())); 
            }
        } else {

            // first screen generated by engine -> init sessions

            CategoryTemporaryBean categoryTemporaryBean = null;

            String categoryKey = jParams.getParameter ("categoryKey");
            if ((categoryKey == null) &&
                    (jParams.getParameter("parentCategoryKey") != null)) {
                engineMap.put(ENGINE_MODE_SESSION_NAME, "addCategory");
                categoryTemporaryBean = new CategoryTemporaryBean();
                engineMap.put (ENGINE_URL_PARAM,
                        jParams.composeEngineUrl (ENGINE_NAME, "?parentCategoryKey=" + jParams.getParameter("parentCategoryKey")));
                engineMap.put("noApply", EMPTY_STRING);
            } else {
                category = Category.getCategory(Integer.parseInt(categoryKey), jParams.getUser());
                categoryTemporaryBean = new CategoryTemporaryBean(category);
                engineMap.put(ENGINE_MODE_SESSION_NAME, "updateCategory");
                engineMap.put (ENGINE_URL_PARAM,
                        jParams.composeEngineUrl (ENGINE_NAME, "?categoryKey=" + category.getObjectKey().getIdInType()));
            }

            theScreen = EDIT_STR;

            // init the temporary template bean

            // init session
            engineMap.put (CATEGORY_SESSION_NAME, category);
            engineMap.put (TEMPORARY_CATEGORY_SESSION_NAME, categoryTemporaryBean);
        }

        engineMap.put (RENDER_TYPE_PARAM, new Integer (JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put (ENGINE_NAME_PARAM, ENGINE_NAME);

        theSession.setAttribute (ProcessingContext.SESSION_JAHIA_ENGINEMAP, engineMap);

        // sets screen
        engineMap.put (SCREEN_STR, theScreen);
        if (theScreen.equals (SAVE_STR)) {
            engineMap.put (JSPSOURCE_STR, CLOSE_STR);
        } else if (theScreen.equals (APPLY_STR)) {
            engineMap.put (JSPSOURCE_STR, APPLY_STR);
        } else if (theScreen.equals (CANCEL_STR)) {
            engineMap.put (JSPSOURCE_STR, CLOSE_STR);
        } else {
            engineMap.put (JSPSOURCE_STR, CATEGORY_JSP);
        }

        // sets engineMap for JSPs
        jParams.setAttribute (ENGINE_NAME_PARAM, "Edit category");
        jParams.setAttribute ("org.jahia.engines.EngineHashMap", engineMap);
        jParams.setAttribute ("CategoriesEdit_Engine.warningMsg", EMPTY_STRING);
        return engineMap;
    }


    /**
     * Prepare data to display or retrieves submitted values and store them in session
     */
    private boolean processCategoryEdit (ProcessingContext processingContext,
                                         int mode, Map engineMap)
        throws JahiaException {

        if (mode == JahiaEngine.LOAD_MODE) {
            // everything is in the session , so do nothing
            return true;
        } else if (mode == JahiaEngine.UPDATE_MODE) {
            // check the last screen
            String lastScreen = processingContext.getParameter (LASTSCREEN_STR);
            if (lastScreen == null)
                lastScreen = EMPTY_STRING;

            if (lastScreen.equals (EDIT_STR)) {
                // retrieve submitted data
                CategoryTemporaryBean categoryTemporaryBean = (CategoryTemporaryBean)
                        engineMap.get (TEMPORARY_CATEGORY_SESSION_NAME);

                JahiaData jData = (JahiaData) processingContext.getAttribute(
                    "org.jahia.data.JahiaData");
                ProcessingContext jParams = null;
                if (jData != null) {
                    jParams = jData.getProcessingContext();
                }

                processingContext.setAttribute("warningMsg", "");

                if (categoryTemporaryBean.isNewCategory()) {
                    String newCategoryKey = processingContext.getParameter("newCategoryKey");
                    if ((newCategoryKey != null) && (!"".equals(newCategoryKey.trim()))) {
                        categoryTemporaryBean.setKey(newCategoryKey);
                    }
                }

                Map parameterMap = processingContext.getParameterMap();
                Iterator keyIter = parameterMap.keySet().iterator();
                while (keyIter.hasNext()) {
                    String curKey = (String) keyIter.next();
                    if (curKey.startsWith("title_")) {
                        String languageCode = curKey.substring("title_".length());
                        String newTitle = processingContext.getParameter(curKey);
                        if ( (newTitle != null) && (!"".equals(newTitle))) {
                            categoryTemporaryBean.getTitles().put(languageCode, newTitle);
                        }
                    }
                }

                String parentCategoryKey = processingContext.getParameter("parentCategoryKey");
                if(parentCategoryKey!= null)
                    Category.getCategory(Integer.parseInt(parentCategoryKey), jParams.getUser());

                keyIter = parameterMap.keySet().iterator();
                while (keyIter.hasNext()) {
                    String curKey = (String) keyIter.next();
                    if (curKey.startsWith("setProperty_")) {
                        String propertyName = curKey.substring("setProperty_".
                            length());
                        String propertyValue = processingContext.getParameter(curKey);
                        if ( (propertyName != null) && (!"".equals(propertyValue))) {
                            logger.debug("Setting property name=[" + propertyName +
                                         "] value=[" + propertyValue +
                                         "] for category [" +
                                         categoryTemporaryBean.getKey() + "]");
                            categoryTemporaryBean.getProperties().setProperty(propertyName, propertyValue);
                        }
                    }
                }

                String newPropertyName = processingContext.getParameter("newPropertyName");
                if (newPropertyName != null) {
                    if (!"".equals(newPropertyName)) {
                        String newPropertyValue = processingContext.getParameter(
                            "newPropertyValue");
                        if (newPropertyValue != null) {
                            logger.debug("Setting property name=[" +
                                         newPropertyName + "] value=[" +
                                         newPropertyValue + "] for category [" +
                                         categoryTemporaryBean.getKey() + "]");
                            categoryTemporaryBean.getProperties().setProperty(newPropertyName,
                                newPropertyValue);
                        }
                    }
                }

                String propertyToDelete = processingContext.getParameter("propertyToDelete");
                if ( (propertyToDelete != null) && (!"".equals(propertyToDelete))) {
                    categoryTemporaryBean.getProperties().remove(propertyToDelete);
                }

                String dspMsg = JahiaResourceBundle.getJahiaInternalResource(
                    "org.jahia.admin.JahiaDisplayMessage.changeCommitted.label",
                    jParams.getLocale());
                processingContext.setAttribute("jahiaDisplayMessage", dspMsg);
            }
            return true;
        }
        return false;
    }


    /**
     * Save data
     */
    private boolean processCategorySave (ProcessingContext processingContext, Map engineMap)
            throws JahiaException {

        CategoryTemporaryBean categoryTemporaryBean = (CategoryTemporaryBean)
                engineMap.get (TEMPORARY_CATEGORY_SESSION_NAME);

        // check data integrity

        // If everything is ok save new values
        String parentCategoryKey = processingContext.getParameter("parentCategoryKey");
        Category parentCategory = null;
        if(parentCategoryKey!= null)
            parentCategory = Category.getCategory(Integer.parseInt(parentCategoryKey), processingContext.getUser());

        String categoryKey = processingContext.getParameter("newCategoryKey");
        boolean addingNewCategory = false;
        Category currentCategory = null;
        if (categoryKey == null) {
            // we are editing an existing category
            categoryKey = (String) processingContext.getSessionState().getAttribute(CATEGORYKEY_SESSION_NAME);
            if(categoryKey == null) {
                categoryKey = categoryTemporaryBean.getKey();
                addingNewCategory = categoryTemporaryBean.isNewCategory();
            } else {
                currentCategory = Category.getCategory(Integer.parseInt(categoryKey), processingContext.getUser());
            }
        } else {
            // we are adding a new category.
            addingNewCategory = true;
        }
        
        if(addingNewCategory) {
            if ("".equals(categoryKey.trim())) {
                // user has entered an empty category key, this is not allowed.
                return false;
            }
            if (Category.getCategory(categoryKey, processingContext.getUser()) != null) {
                String dspMsg = JahiaResourceBundle.getJahiaInternalResource(
                    "org.jahia.admin.categories.ManageCategories.editCategory.categoryAlreadyExists.label",
                    processingContext.getLocale());
                processingContext.getSessionState().setAttribute(JahiaAdministration.CLASS_NAME +
                                     "jahiaDisplayMessage", dspMsg);
                return false;
            }
            currentCategory = Category.createCategory(categoryKey,
                parentCategory);
        }

        Iterator titleEntryIter = categoryTemporaryBean.titles.entrySet().iterator();
        while (titleEntryIter.hasNext()) {
            Map.Entry titleEntry = (Map.Entry) titleEntryIter.next();
            Locale titleLocale = LanguageCodeConverters.languageCodeToLocale((String) titleEntry.getKey());
            currentCategory.setTitle(titleLocale, (String) titleEntry.getValue());
        }
        currentCategory.setProperties(categoryTemporaryBean.properties);

        processingContext.getSessionState().removeAttribute(ManageCategories.CURRENTCATEGORY_SESSIONKEY);
        processingContext.getSessionState().removeAttribute(ManageCategories.CURRENTCATEGORYCHILDS_SESSIONKEY);
        processingContext.getSessionState().removeAttribute(ManageCategories.CATEGORYTREE_SESSIONKEY);
        return true;
    }

    //-------------------------------------------------------------------------
    private void displayEditCategory (ProcessingContext processingContext,
                                      int mode,
                                      Map engineMap)
        throws JahiaException {
        JahiaData jData = (JahiaData) processingContext.getAttribute(
            "org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }
        Category category = (Category) engineMap.get(CATEGORY_SESSION_NAME);
        String currentCategoryKey = null;
        if (category == null) {
            currentCategoryKey = processingContext.getParameter("currentCategoryKey");
        } else {
            currentCategoryKey = category.getObjectKey().getIDInType();
        }
        String parentCategoryKey = processingContext.getParameter("parentCategoryKey");
        if (currentCategoryKey != null) {
            Category currentCategory = Category.getCategory(
                Integer.parseInt(currentCategoryKey), jParams.getUser());
            processingContext.getSessionState().setAttribute(CATEGORYKEY_SESSION_NAME,
                                 currentCategoryKey);
            if(parentCategoryKey==null) {
                    List parentCategories = currentCategory.getParentCategories(jParams.getUser());
                    if(parentCategories != null && parentCategories.size()>0)
                        parentCategoryKey = ((Category)parentCategories.get(0)).getObjectKey().getIDInType();
                }
        } else {
            processingContext.getSessionState().removeAttribute(CATEGORYKEY_SESSION_NAME);
        }

        engineMap.put("parentCategoryKey", parentCategoryKey);

        // make sure that select user/group sub engine allows for selection of site
        engineMap.put("selectSiteInSelectUsrGrp", Boolean.TRUE);

    }

}
