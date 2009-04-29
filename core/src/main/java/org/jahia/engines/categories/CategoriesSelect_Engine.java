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

import org.apache.commons.lang.StringUtils;
import org.jahia.data.JahiaData;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.shared.Category_Field;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.services.categories.Category;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 30 nov. 2006
 * Time: 10:57:05
 * To change this template use File | Settings | File Templates.
 */
public class CategoriesSelect_Engine implements JahiaEngine {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(CategoriesSelect_Engine.class);

    private static final String TEMPLATE_JSP = "select_categories";

    public static final String ENGINE_NAME = "categoriesSelect";
    private EngineToolBox toolBox;

    /**
     *
     */
    public CategoriesSelect_Engine() {
        toolBox = EngineToolBox.getInstance();
    }

    /**
     * @param jParams
     */
    public boolean authoriseRender(ProcessingContext jParams) {
        return true; // we do not check if we are in Edit mode
    }

    /**
     * @param jParams
     */
    public boolean needsJahiaData(ProcessingContext jParams) {
        return false;
    }

    /**
     * @param jParams
     * @param theObj
     * @return
     * @throws JahiaException
     */
    public String renderLink(ProcessingContext jParams, Object theObj)
            throws JahiaException {
        String params = "";
        if (theObj instanceof String) {
            params = (String) theObj;
        }
        return jParams.composeEngineUrl(ENGINE_NAME, params);
    }

    /**
     * @param jParams
     * @param jData
     * @return
     * @throws JahiaException
     */
    public EngineValidationHelper handleActions(ProcessingContext jParams, JahiaData jData)
            throws JahiaException {
        // initalizes the hashmap
        Map<String, Object> engineMap = initEngineMap(jParams);
        processScreen(jParams, engineMap);
        // displays the screen
        toolBox.displayScreen(jParams, engineMap);

        return null;
    }

    /**
     * prepares the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public void processScreen(final ProcessingContext jParams, final Map<String, Object> engineMap)
            throws JahiaException, JahiaForbiddenAccessException {
        logger.debug("processScreen");

        // gets the current screen
        // screen   = edit, rights, logs
        final String theScreen = (String) engineMap.get("screen");
        if (theScreen.equals("edit")) {
            loadCategories(jParams, engineMap);
        } else if (theScreen.equals("save") || theScreen.equals("apply")) {
            updateCategories(jParams, engineMap);
            saveCategories(jParams, engineMap);
        }
    }

    /**
     * inits the engine map
     * <p/>
     * The List "containers" contains all the containers of the container list. The List
     * "fieldInfoToDisplay" contains the fields values on which the sort is done. a value in
     * "fieldInfoToDisplay" correspond to the container at the same index into "containers".
     *
     * @param jParams a ProcessingContext object (with request and response)
     * @return a Map object containing all the basic values needed by an engine
     */
    private Map<String, Object> initEngineMap(ProcessingContext jParams)
            throws JahiaException, JahiaSessionExpirationException {

        final Map<String, Object> engineMap;
        String theScreen = jParams.getParameter("screen");

        // gets session values
        //HttpSession theSession = jParams.getRequest().getSession (true);
        final SessionState theSession = jParams.getSessionState();
        if (theScreen != null) {
            // if no, load the container value from the session
            engineMap = (Map<String, Object>) theSession.getAttribute("jahia_session_engineMap");

            ///////////////////////////////////////////////////////////////////////////////////////
            // FIXME -Fulco-
            //
            //      This is a quick hack, engineMap should not be null if the session didn't
            //      expired. Maybe there are other cases where the engineMap can be null, I didn't
            //      checked them at all.
            ///////////////////////////////////////////////////////////////////////////////////////
            if (engineMap == null) {
                throw new JahiaSessionExpirationException();
            }
        } else {
            theScreen = "edit";
            engineMap = new HashMap<String, Object>();
        }

        String contextId = jParams.getParameter("contextId");
        if (contextId == null) {
            contextId = (String) engineMap.get("contextId");
        }
        if (contextId == null) {
            contextId = (String) theSession.getAttribute("contextId");
        }
        if (contextId != null) {
            engineMap.put("contextId", contextId);
            theSession.setAttribute("contextId", contextId);
        }

        engineMap.put(RENDER_TYPE_PARAM, new Integer(JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put(ENGINE_NAME_PARAM, ENGINE_NAME);
        engineMap.put(ENGINE_URL_PARAM, jParams.composeEngineUrl(ENGINE_NAME, ""));
        engineMap.put("noApply", "");
        theSession.setAttribute("jahia_session_engineMap", engineMap);

        // sets screen
        engineMap.put("screen", theScreen);
        if (theScreen.equals("save")) {
            engineMap.put("jspSource", "select_categories_close");
        } else if (theScreen.equals("apply")) {
            engineMap.put("jspSource", "apply");
        } else if (theScreen.equals("cancel")) {
            engineMap.put("jspSource", "close");
        } else {
            engineMap.put("jspSource", TEMPLATE_JSP);
        }

        // sets engineMap for JSPs
        jParams.setAttribute("engineTitle", "Select Categories");
        jParams.setAttribute("org.jahia.engines.EngineHashMap", engineMap);
        return engineMap;
    }

    /**
     * @param jParams
     * @param engineMap
     */
    protected boolean loadCategories(final ProcessingContext jParams,
                                     final Map<String, Object> engineMap) throws JahiaException {
        final SessionState session = jParams.getSessionState();
        final String contextId = (String) engineMap.get("contextId");
        Map<String, Object> categoriesDataMap = (Map<String, Object>) session.getAttribute(ENGINE_NAME + ".categoriesDataMap." + contextId);
        if (categoriesDataMap == null) {
            categoriesDataMap = new HashMap<String, Object>();
            session.setAttribute(ENGINE_NAME + ".categoriesDataMap." + contextId, categoriesDataMap);
        }
        List<String> selectedCategories = (List<String>) session.getAttribute(
                Category_Field.SELECTEDCATEGORIES_ENGINEMAPKEY + contextId);
        String selectedCategoriesFromRequest = jParams.getParameter(Category_Field.SELECTEDCATEGORIES_ENGINEMAPKEY);
        if (selectedCategoriesFromRequest != null) {
            String[] values = StringUtils.split(selectedCategoriesFromRequest,
                    ',');
            for (int i = 0; i < values.length; i++) {
                values[i] = values[i].trim();
            }
            selectedCategories = new ArrayList<String>(Arrays.asList(values));
            
            session.setAttribute(
                    Category_Field.SELECTEDCATEGORIES_ENGINEMAPKEY + contextId, selectedCategories);
            categoriesDataMap.put("defaultSelectedCategories", selectedCategories);
        }
        logger.debug("Load Categories selection from Session: " + selectedCategories);

        logger.debug("Load Categories selection from categoriesDataMap: " + categoriesDataMap);
        final String rootCategoryKey = (String) categoriesDataMap.get("rootCategoryKey");
        final Category startCategory;
        if (rootCategoryKey != null && rootCategoryKey.length() > 0) {
            startCategory = Category.getCategory(rootCategoryKey, jParams.getUser());
        } else {
            startCategory = Category.getRootCategory(jParams.getUser());
        }

        // still possible if the user has no rights to see even the root
        // category.
        if (startCategory == null) {
            return false;
        }
        List<String> defaultSelectedCategories = (List<String>)categoriesDataMap.get("defaultSelectedCategories");
        if (defaultSelectedCategories == null) {
            defaultSelectedCategories = new ArrayList<String>();
        }

        boolean foundNoSelectionMarker = false;

        if (selectedCategories == null) {
            selectedCategories = new ArrayList<String>();

            session.setAttribute(Category_Field.ORIGINALLYSELECTEDCATEGORIES_ENGINEMAPKEY + contextId,
                    selectedCategories);

            // if after all this we still have no category selections, let's
            // insert the default values, if there are any. We also need to
            // check if the field has ever been edited, in which case we will
            // not insert the default values.
            if ((selectedCategories.size() == 0) && (!foundNoSelectionMarker)) {
                selectedCategories.addAll(defaultSelectedCategories);
            }

            session.setAttribute(Category_Field.SELECTEDCATEGORIES_ENGINEMAPKEY + contextId, selectedCategories);
        }
        if (selectedCategories.size() == 0) {
            // this mechanism allows templates to set the currently
            // selected categories into the session and we can use these
            // values to initialize the selected categories.
            selectedCategories = (List<String>) jParams.getSessionState().getAttribute(
                    Category_Field.DEFAULTCATEGORIES_SESSIONKEYPREFIX + contextId);
            logger.debug("Looking for default selected categories in session key " +
                    Category_Field.DEFAULTCATEGORIES_SESSIONKEYPREFIX + contextId);
            if (selectedCategories != null) {
                logger.debug("Found default categories " + selectedCategories + "for session key " +
                        Category_Field.DEFAULTCATEGORIES_SESSIONKEYPREFIX + contextId);
            } else {
                selectedCategories = new ArrayList<String>();
            }
            session.setAttribute(Category_Field.SELECTEDCATEGORIES_ENGINEMAPKEY + contextId, selectedCategories);
        }

        jParams.setAttribute("ZimbraInclude", "true");
        return true;
    }

    private boolean saveCategories(final ProcessingContext jParams, Map<String, Object> engineMap) {
        final SessionState session = jParams.getSessionState();
        final String contextId = (String) engineMap.get("contextId");

        List<String> selectedCategories = (List<String>) session.getAttribute(Category_Field.SELECTEDCATEGORIES_ENGINEMAPKEY +
                contextId);
        logger.debug("Saving categories selection");
        if (selectedCategories == null) {
            // this mechanism allows templates to set the currently
            // selected categories into the session and we can use these
            // values to initialize the selected categories.
            selectedCategories = (List<String>) jParams.getSessionState().getAttribute(
                    Category_Field.DEFAULTCATEGORIES_SESSIONKEYPREFIX + contextId);
            logger.debug("Looking for default selected categories in session key " +
                    Category_Field.DEFAULTCATEGORIES_SESSIONKEYPREFIX + contextId);
            if (selectedCategories != null) {
                logger.debug("Found default categories " + selectedCategories + "for session key " +
                        Category_Field.DEFAULTCATEGORIES_SESSIONKEYPREFIX + contextId);
            }
        }

        if (selectedCategories == null) {
            return true;
        }

        final Map<String, Object> categoriesDataMap = (Map<String, Object>) jParams.getSessionState().getAttribute(ENGINE_NAME +
                ".categoriesDataMap." + contextId);
        if (categoriesDataMap != null) {
            categoriesDataMap.put("contextId", contextId);
            categoriesDataMap.put("defaultSelectedCategories", selectedCategories);
            categoriesDataMap.put("updated", Boolean.TRUE);
            jParams.getSessionState().setAttribute(ENGINE_NAME + ".categoriesDataMap." + contextId, categoriesDataMap);
        }

        jParams.getSessionState().removeAttribute(Category_Field.ORIGINALLYSELECTEDCATEGORIES_ENGINEMAPKEY + contextId);
        jParams.getSessionState().removeAttribute(Category_Field.SELECTEDCATEGORIES_ENGINEMAPKEY + contextId);
        // remove tree also.
        jParams.getSessionState().removeAttribute(Category_Field.CATEGORYTREE_ENGINEMAPKEY + contextId);
        return true;
    }

    private boolean updateCategories(ProcessingContext jParams, Map<String, Object> engineMap) {
        final SessionState session = jParams.getSessionState();
        final String contextId = (String) engineMap.get("contextId");

        final Map<String, Object> parameterMap = jParams.getParameterMap();
        final List<String> newSelectedCategories = new ArrayList<String>();
        final List<String> oldSelectedCategories = (List<String>) session.getAttribute(
                Category_Field.SELECTEDCATEGORIES_ENGINEMAPKEY + contextId);
        try {
            for (final String curParamName : parameterMap.keySet()) {
                if (curParamName.startsWith(Category_Field.CATEGORYPREFIX_HTMLPARAMETER)) {
                    final String curCategoryId = curParamName.substring(Category_Field.CATEGORYPREFIX_HTMLPARAMETER.length());
                    final Category c = Category.getCategory(Integer.parseInt(curCategoryId), null);
                    newSelectedCategories.add(c.getKey());
                    logger.debug("Submitted category key : " + c.getKey());
                } else if (curParamName.equals("categoryKeySearch")) {
                    final Category c = Category.getCategory(jParams.getParameter(curParamName));
                    if (c != null) {
                        if (oldSelectedCategories != null && oldSelectedCategories.contains(c.getKey())) {
                            // we remove the category actually
                            logger.debug("deleting category key : " + c.getKey());
                            oldSelectedCategories.remove(c.getKey());
                        } else {
                            logger.debug("Submitted category key through search: " + c.getKey());
                            newSelectedCategories.add(c.getKey());
                        }
                    }
                }
            }
        } catch (final Exception e) {
            logger.debug("Error in updateCategories", e);
        }

//        if (oldSelectedCategories != null && adding) {
//            final Iterator oldSelectedIter = oldSelectedCategories.iterator();
//            while (oldSelectedIter.hasNext()) {
//                final String curOldSelectedCategoryKey = (String) oldSelectedIter.next();
//                if (newSelectedCategories.contains(curOldSelectedCategoryKey)) {
//                    // nothing to do here
//                } else {
//                    logger.debug("Submitted category key: " + curOldSelectedCategoryKey);
//                    newSelectedCategories.add(curOldSelectedCategoryKey);
//                }
//            }
            session.setAttribute(Category_Field.CATEGORIESUPDATED_ENGINEMAPKEY + contextId, Boolean.TRUE);
            session.setAttribute(Category_Field.SELECTEDCATEGORIES_ENGINEMAPKEY + contextId, newSelectedCategories);
//        }
        return true;
    }


    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName() {
        return ENGINE_NAME;
    }

}
