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
package org.jahia.engines.applications;

import org.jahia.engines.JahiaEngine;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.params.ProcessingContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.data.JahiaData;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointObjectKey;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.content.ObjectKey;
import org.jahia.content.ObjectLink;
import org.jahia.content.CategoryKey;

import java.util.*;

/**
 * Engine for categorising portlet
 *
 * @author ktlili
 */
public class ManageApplicationCategoriesEngine implements JahiaEngine {
    // logger
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ManageApplicationCategoriesEngine.class);

    // engine properties
    private final String JSPSOURCE_STR = "jspSource";
    private static final String ENGINE_JSP = "application_manage_categories";
    private static final String ENGINE_NAME = "portletCategories";
    private EngineToolBox toolBox;

    // sreen
    private final String SCREEN_STR = "screen";
    private final String SAVE_STR = "save";
    private final String EDIT_STR = "edit";
    private final String CANCEL_STR = "cancel";
    private final String APPLY_STR = "apply";
    private final String CLOSE_STR = "close";

    //categories parameter
    public static final String PORLET_CATEGORIES = "portletCategories";
    public static final String PORLET_CATEGORIES_TRUE = "true";
    public static final String PORLET_CATEGORIES_LINK = "org.jahia.engines.applications.ManageApplicationCategoriesEngine.portletCategoriesLink";

    //session attr. name
    public static final String SELECTED_PORTLETS_LIST_ATTR = "selectedPortletsList";
    public static final String PORTLETS_CATEGORIES_MAP_ATTR = "portletCategoriesMap";

    private ManageApplicationCategoriesEngine() {
        toolBox = EngineToolBox.getInstance();
    }

    public boolean authoriseRender(ProcessingContext jParams) {
        // TO DO:     check if we have admin role

        return true;
    }

    public String renderLink(ProcessingContext jParams, Object theObj) throws JahiaException {
        logger.debug("Call renderLink()");
        String link = createEngineUrl(jParams);
        return link;
    }

    private String createEngineUrl(ProcessingContext jParams) throws JahiaException {
        String params = "&" + PORLET_CATEGORIES + "=" + PORLET_CATEGORIES_TRUE;
        String link = jParams.composeEngineUrl(ManageApplicationCategoriesEngine.ENGINE_NAME, params);
        return link;
    }

    public boolean needsJahiaData(ProcessingContext jParams) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public EngineValidationHelper handleActions(ProcessingContext jParams, JahiaData jData) throws JahiaException {
        // get screen value
        String theScreen = jParams.getParameter(SCREEN_STR);
        logger.debug("Screen is " + theScreen);

        //case of save

        if (theScreen != null && !theScreen.equalsIgnoreCase(CANCEL_STR)) {
            logger.debug("Call Save()");
            // get selected object list
            List selectedEntryPointDefinitionList = getSelectedEntryPointDefinitionList(jParams);

            // ObjectKeys List
            List entryPointDefinitionsKey = new ArrayList();

            // compute category key
            List[] computedList = computeSelectedAndClickedCategoriesList(jParams);
            List selectedCategories = computedList[0];
            List clikedCategories = computedList[1];

            //for each selected object, add it to category
            for (int i = 0; i < selectedEntryPointDefinitionList.size(); i++) {
                EntryPointDefinition epd = (EntryPointDefinition) selectedEntryPointDefinitionList.get(i);
                String currentSelectedObjectKey = epd.getApplicationID() + "_" + epd.getName();
                logger.debug("triing to add entrypointdefintion [" + currentSelectedObjectKey + "].");

                // compute entryPointKey
                EntryPointObjectKey objKey = new EntryPointObjectKey(currentSelectedObjectKey);
                if (!entryPointDefinitionsKey.contains(objKey)) {
                    entryPointDefinitionsKey.add(objKey);
                }

                //first: load old categories for current entrypoint definition BEFORE doing changes
                List categoriyObjectKeysToRemoveList = getEntryPointDefinitionCategoriesKey(objKey);

                // link entrypoint/category
                logger.debug("Category to add: " + selectedCategories);
                for (int j = 0; j < selectedCategories.size(); j++) {
                    Category currentCat = (Category) selectedCategories.get(j);
                    CategoryKey categoryKey = (CategoryKey) currentCat.getObjectKey();

                    // add entrypoint point definition
                    if (clikedCategories.contains(currentCat)) {
                        addEntryPointDefinitionToCategory(categoryKey, objKey);
                    }

                    categoriyObjectKeysToRemoveList.remove(currentCat.getKey());

                }

                // at this level, categoriyObjectKeysToRemoveList contains only categoryKeys to remove
                logger.debug("Categories to remove: " + categoriyObjectKeysToRemoveList);
                for (int j = 0; j < categoriyObjectKeysToRemoveList.size(); j++) {
                    String currentCategoryKey = (String) categoriyObjectKeysToRemoveList.get(j);
                    Category currentCat = ServicesRegistry.getInstance().getCategoryService().getCategory(currentCategoryKey);
                    CategoryKey categoryKey = (CategoryKey) currentCat.getObjectKey();

                    //remove
                    removeEntryPointDefinitionToCategory(categoryKey, objKey);
                }
            }
        }

        logger.debug("Finish Parameter name enumeration.");

        // update engine map
        Map nextEngineMap = updateEngineMap(jParams);

        // displays the screen
        toolBox.displayScreen(jParams, nextEngineMap);

        return null;
    }

    private List[] computeSelectedAndClickedCategoriesList(ProcessingContext jParams) throws JahiaException {
        List selectedCategories = new ArrayList();
        List clickedCategories = new ArrayList();
        List[] result = new List[2];
        Iterator it = jParams.getParameterNames();
        while (it.hasNext()) {
            String currentParamName = (String) it.next();
            logger.debug("Found parameter with name: " + currentParamName);
            boolean isCategoryParam = currentParamName.indexOf("category") > -1;
            if (isCategoryParam) {
                String[] values = jParams.getParameterValues(currentParamName);
                logger.debug("params [" + currentParamName + "] is a category param." + values);
                boolean hasBeenClicked = values != null && values[0].equalsIgnoreCase("true");

                //patern param: category_categoryID
                StringTokenizer st = new StringTokenizer(currentParamName, "_");
                // remove category
                st.nextToken();
                // get ID
                int categoryID = Integer.parseInt(st.nextToken());
                //logger.debug("Category ID is: " + categoryID);
                Category currentCat = ServicesRegistry.getInstance().getCategoryService().getCategory(categoryID);
                selectedCategories.add(currentCat);
                logger.debug("Current Cat. key is: " + currentCat.getKey());
                if (hasBeenClicked) {
                    clickedCategories.add(currentCat);
                }

            } else {
                logger.debug("Current params is NOT a category param.");
            }
        }
        result[0] = selectedCategories;
        result[1] = clickedCategories;
        return result;
    }


    /*
    * get name
    * */
    public String getName() {
        return ManageApplicationCategoriesEngine.ENGINE_NAME;
    }

    /* init engine map*/
    private Map updateEngineMap(ProcessingContext jParams) throws JahiaException {
        logger.debug("Call updateEngineMap();");
        Map engineMap = getEngineMap(jParams);

        // put render type
        engineMap.put(RENDER_TYPE_PARAM, new Integer(JahiaEngine.RENDERTYPE_FORWARD));

        // put engine name
        engineMap.put(ENGINE_NAME_PARAM, ENGINE_NAME);

        // put engine url
        engineMap.put(ENGINE_URL_PARAM, createEngineUrl(jParams));

        // put jsp source
        String theScreen = jParams.getParameter(SCREEN_STR);
        if (theScreen != null) {
            // sets screen
            engineMap.put(SCREEN_STR, theScreen);
            if (theScreen.equals(SAVE_STR) || theScreen.equals(CANCEL_STR)) {
                engineMap.put(JSPSOURCE_STR, CLOSE_STR);
            } else {
                engineMap.put(JSPSOURCE_STR, ManageApplicationCategoriesEngine.ENGINE_JSP);
            }
        } else {
            engineMap.put(JSPSOURCE_STR, ManageApplicationCategoriesEngine.ENGINE_JSP);
            logger.debug("theScreen value is null.");
        }

        //put link to categories manager
        String[] objectIDs = (String[]) jParams.getSessionState().getAttribute("objectIDs");
        for (int i = 0; i < objectIDs.length; i++) {
            logger.debug("Found value with for objectIDs: " + objectIDs[i]);
        }

        //put selected entryPoint List
        try {
            //build selected Entrypoint List
            Object[] results = loadSelectedEntryPointDefinitons(objectIDs);
            List entryPointList = (List) results[0];
            List selectedCategoryObjectKeysList = (List) results[1];
            Map portletsCategoriesMap = (Map) results[2];
            List selectedAllsourceCategoriyObjectKeysList = (List) results[3];

            // set selectedObject
            setEntyPointDefinitionList(jParams, entryPointList);

            // set hashmap objectCatgeory
            setPortletsCategoriesMap(jParams, portletsCategoriesMap);

            //add into engineMap
            engineMap.put("selectedObjectList", entryPointList);
            engineMap.put(PORTLETS_CATEGORIES_MAP_ATTR, portletsCategoriesMap);

            jParams.getSessionState().setAttribute("org.jahia.engines.applications.ManageApplicationCategoriesEngine.categoriesKeyList", selectedCategoryObjectKeysList);
            jParams.getSessionState().setAttribute("org.jahia.engines.applications.ManageApplicationCategoriesEngine.allSourcesCategoriesKeyList", selectedAllsourceCategoriyObjectKeysList);
        } catch (JahiaException e) {
            logger.error("Can't init selectedObjectList due to:", e);
        }

        // load selected categories
        setEngineMap(jParams, engineMap);


        return engineMap;
    }

    private void setEngineMap(ProcessingContext jParams, Map engineMap) {
        //update engineMap
        jParams.setAttribute("org.jahia.engines.EngineHashMap", engineMap);
    }

    private Map getEngineMap(ProcessingContext jParams) {
        //get engineMap
        Map m = (Map) jParams.getAttribute("org.jahia.engines.EngineHashMap");
        if (m == null) {
            if (m == null) {
                logger.debug("Engine map not found in session");
                m = new HashMap();
            }
        }
        return m;
    }


    /*
   *
   *  build selected entry point list
   *
   * */
    public Object[] loadSelectedEntryPointDefinitons(String[] entryPointIds) throws JahiaException {
        List entryPointList = new ArrayList();
        List selectedCategoriyObjectKeysList = new ArrayList();
        List selectedAllsourceCategoriyObjectKeysList = new ArrayList();
        Map portletCategoriesMap = new HashMap();
        Object[] result = new Object[4];
        result[0] = new ArrayList();
        result[1] = new ArrayList();
        result[2] = new HashMap();
        result[3] = new ArrayList();
        for (int i = 0; i < entryPointIds.length; i++) {
            logger.debug("Current EntryPointId: " + entryPointIds[i]);

            // pattern of an entryPointId: applicationID::entryPointDefName
            StringTokenizer st = new StringTokenizer(entryPointIds[i], "::");
            String appID = st.nextToken();
            String entryPoindDefName = st.nextToken();

            // find application
            ApplicationBean theApplication = ServicesRegistry.getInstance().getApplicationsManagerService().getApplication(Integer.parseInt(appID));
            if (theApplication == null) {
                logger.error("ApplicationBean with id [" + appID + "] not found. Look in ApplicationList");
                return result;
            }

            //find Entry Point Def
            EntryPointDefinition epd = theApplication.getEntryPointDefinitionByName(entryPoindDefName);
            if (epd != null) {
                // add to list
                entryPointList.add(epd);
                logger.debug("EntryPoint with id [" + entryPointIds[i] + "] added to selectedEntryPointList ");
            } else {
                logger.error("EntryPoint with id [" + entryPointIds[i] + "] NOT FOUND.");
                return result;
            }

            //load selected categorie for current EntryPointDefinition
            logger.debug("load selected categories...");
            String currentObjectKey = appID + "_" + entryPoindDefName;
            EntryPointObjectKey objKey = new EntryPointObjectKey(currentObjectKey);
            List categoriesObjectKey = getEntryPointDefinitionCategoriesKey(objKey);

            // add to to hashmap
            portletCategoriesMap.put(currentObjectKey, categoriesObjectKey);

            // add to major categories list.
            for (int j = 0; j < categoriesObjectKey.size(); j++) {
                Object o = categoriesObjectKey.get(j);
                if (!selectedCategoriyObjectKeysList.contains(o)) {
                    // case of new categories
                    selectedCategoriyObjectKeysList.add(o);
                }
            }

            // update all selectedAllSources
            if (i == 0) {
                selectedAllsourceCategoriyObjectKeysList.addAll(categoriesObjectKey);
            } else {
                selectedAllsourceCategoriyObjectKeysList.retainAll(categoriesObjectKey);
            }
        }

        // set results
        result[0] = entryPointList;
        result[1] = selectedCategoriyObjectKeysList;
        result[2] = portletCategoriesMap;
        result[3] = selectedAllsourceCategoriyObjectKeysList;
        return result;
    }

    public void processCurrentScreen(ProcessingContext jParams, Map engineMap)
            throws JahiaException {
        logger.debug("Call procesCurrentScreen(...)");
    }

    public void processLastScreen(ProcessingContext jParams, Map engineMap)
            throws JahiaException,
            JahiaForbiddenAccessException {

        logger.debug("Call processLastScreen(...)");
    }

    /*
   *
   *  get selected portlets from session
   *
   * */
    private List getSelectedEntryPointDefinitionList(ProcessingContext jParams) {
        List selectedPortletsList = new ArrayList();

        Object selectedPortletsListObj = jParams.getSessionState().getAttribute(SELECTED_PORTLETS_LIST_ATTR);
        if (selectedPortletsListObj != null) {
            logger.debug("selectedPortletsList attr. found in session");
            if (selectedPortletsListObj instanceof List) {
                selectedPortletsList = (List) selectedPortletsListObj;
            } else {
                logger.error("selectedPortletsList attr. found in session MUST be java.util.List. ");
            }
        } else {
            logger.error("selectedPortletsList attr. not found in session");

        }
        return selectedPortletsList;
    }

    /**
     * set selected portlets in session
     */
    public void setEntyPointDefinitionList(ProcessingContext jParams, List selectedPortletList) {
        if (selectedPortletList != null) {
            jParams.getSessionState().setAttribute(SELECTED_PORTLETS_LIST_ATTR, selectedPortletList);
        } else {
            logger.error("selectedPortletsList has null value.");
        }
    }

    /**
     * set selected portlets in session
     */
    public void setPortletsCategoriesMap(ProcessingContext jParams, Map portletsCategoriesMap) {
        if (portletsCategoriesMap != null) {
            jParams.getSessionState().setAttribute(PORTLETS_CATEGORIES_MAP_ATTR, portletsCategoriesMap);
        } else {
            logger.error("portletsCategoriesMap has null value.");
        }
    }


    /**
     * link category to an EntryPointDefinition
     */
    private void addEntryPointDefinitionToCategory(CategoryKey categoryKey, ObjectKey childKey) throws JahiaException {
        // check if exist
        List l = ObjectLink.findByLeftAndRightObjectKeys(categoryKey, childKey);
        if (l == null || l.size() > 0) {
            logger.debug("link [" + categoryKey + "," + childKey + "] already exists");
            logger.debug(l + "," + l.size() + l.get(0));
            return;
        } else {

            // build parameter
            String type = childKey.getType();
            int status = 1;
            Date currentDate = new Date();
            String user = "root:0";
            Map leftObjectMetadata = new HashMap();
            Map rigthObjectMetadata = new HashMap();
            Map commonObjectMetadata = new HashMap();

            //create and save link
            logger.debug("link [" + categoryKey + "," + childKey + "] does not exist--> add link.");
            ObjectLink.createLink(categoryKey, childKey, type, status, currentDate, user, currentDate, user, leftObjectMetadata, rigthObjectMetadata, commonObjectMetadata);
        }

    }

    private void removeEntryPointDefinitionToCategory(CategoryKey categoryKey, ObjectKey childKey) throws JahiaException {
        // check if exist
        List links = ObjectLink.findByLeftAndRightObjectKeys(categoryKey, childKey);
        if (links == null || links.size() > 0) {
            logger.debug("link [" + categoryKey + "," + childKey + "] found");
            for (int j = 0; j < links.size(); j++) {
                ObjectLink objectLinkToRemove = (ObjectLink) links.get(j);
                objectLinkToRemove.remove();
            }
            return;
        } else {
            logger.debug("link [" + categoryKey + "," + childKey + "] not found");
        }

    }

    private void removeUnselectedCategories(List selectedEntryPointDefinitionKey, List selectedCategoriesKey) throws JahiaException {
        //for each EntryPointObjectKey, delete de-selected categories
        logger.debug("Call remove removeUnselectedCategories()");
        for (int i = 0; i < selectedEntryPointDefinitionKey.size(); i++) {
            EntryPointObjectKey currentEntryPointObjectKey = (EntryPointObjectKey) selectedEntryPointDefinitionKey.get(i);

            // compute object link to remocve for current  EntryPointObjectKey
            List objectLinkToRemoveList = computeObjectLinksListToRemove(currentEntryPointObjectKey, selectedCategoriesKey);

            //remove all link
            removeAllObjectLinks(objectLinkToRemoveList);

        }
        logger.debug("end removeUnselectedCategories(...)");
    }

    private List computeObjectLinksListToRemove(EntryPointObjectKey objectKey, List selectedCategoriesKey) throws JahiaException {

        // old_selected_categories - current_selected_categories = unselected_categories
        List objectLinkToRemoveList = getCategoryParentLinks(objectKey);
        objectLinkToRemoveList.removeAll(selectedCategoriesKey);
        return objectLinkToRemoveList;
    }

    private void removeAllObjectLinks(List links) throws JahiaException {
        for (int j = 0; j < links.size(); j++) {
            ObjectLink objectLinkToRemove = (ObjectLink) links.get(j);
            objectLinkToRemove.remove();
        }
    }


    /*
    *
    * get Category of EntryPointDefinition
    *
    * */
    private List getEntryPointDefinitionCategoriesKey(EntryPointObjectKey objectKey) throws JahiaException {
        List categoriesKeys = new ArrayList();

        // get category Parent links
        List links = getCategoryParentLinks(objectKey);

        // for each objectlink, get category key and add it to result list
        Iterator linkIter = links.iterator();
        while (linkIter.hasNext()) {
            ObjectLink curCategoriesLink = (ObjectLink) linkIter.next();

            // get category key
            int categoryID = curCategoriesLink.getLeftObjectKey().getIdInType();
            logger.debug("look for categoy with id [" + categoryID + "]");
            Category currentCat = ServicesRegistry.getInstance().getCategoryService().getCategory(categoryID);
            if (currentCat != null) {
                String categoryKey = currentCat.getKey();
                logger.debug("Found category with key [" + categoryKey + "]");

                // add category key to list
                categoriesKeys.add(categoryKey);
            } else {
                logger.error("Enable to find category with id [" + categoryID + "]");
            }
        }
        logger.debug("End enumerate selected categories");
        return categoriesKeys;
    }


    /*
   *
   * get link that refers to (Category,EntrypointDefinition)
   *
   * */
    private java.util.List getCategoryParentLinks(ObjectKey objectKey) throws JahiaException {
        String type = objectKey.getType();
        logger.debug("Look for Object link with type [" + type + "] and RightObjectKey [" + objectKey.getKey() + "]");
        return ObjectLink.findByTypeAndRightObjectKey(type, objectKey);
    }
}
