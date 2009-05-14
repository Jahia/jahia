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
package org.jahia.engines.shared;

import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.FieldsEditHelper;
import org.jahia.data.fields.FieldsEditHelperAbstract;
import org.jahia.data.fields.JahiaField;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.JahiaEngineTools;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.categories.Category;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.utils.JahiaTools;

import java.util.*;

/**
 * Category field sub engine.
 * This sub engine has a behaviour that's a bit special, since it stores
 * category information in both ObjectLinks AND field values, so there is
 * a lot of code here to handle discrepencies that are permitted, notably
 * in the case of legacy content, that didn't yet store category information
 * in field values.
 * User: Serge Huber
 * Date: 23 ao�t 2005
 * Time: 09:08:13
 * Copyright (C) Jahia Inc.
 */
public class Category_Field implements FieldSubEngine {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(Category_Field.class);

    private static Category_Field theObject = null;
    private static final String JSP_FILE = "/engines/shared/category_field.jsp";
    private static final String READONLY_JSP = "/engines/shared/readonly_category_field.jsp";

    public static final String ORIGINALLYSELECTEDCATEGORIES_ENGINEMAPKEY = "originallySelectedCategories";
    public static final String SELECTEDCATEGORIES_ENGINEMAPKEY = "selectedCategories";
    public static final String DEFAULTCATEGORIES_SESSIONKEYPREFIX = "defaultCategories_";
    public static final String CATEGORYTREE_ENGINEMAPKEY = "categoryTree";
    public static final String FLATCATEGORYLIST_ENGINEMAPKEY = "flatCategoryList";

    public static final String CATEGORYPREFIX_HTMLPARAMETER = "category_";

    public static final String DEFAULT_CATEGORY_RESERVED_DEFINITION_NAME = "defaultCategory";

    public static final String NOSELECTION_MARKER = "$$$NO_SELECTION_MARKER$$$";
    public static final String CATEGORIESUPDATED_ENGINEMAPKEY = "categoriesUpdated";
    public static final String START_CATEGORY = "startCategory";

    public static synchronized Category_Field getInstance() {
        if (theObject == null) {
            theObject = new Category_Field();
        }
        return theObject;
    }

    public boolean handleField(ProcessingContext jParams, Integer modeInt, Map<String, Object> engineMap)
            throws JahiaException {

        int mode = modeInt.intValue();
        String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
        JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + "." + "theField");
        jParams.setAttribute(JahiaEngine.ENGINE_MODE_ATTRIBUTE, new Integer(mode));

        //JahiaField theField = (JahiaField) engineMap.get( "theField" );
        if (theField == null) {
            logger.debug("the field is null");
        }
        switch (mode) {
            case (JahiaEngine.LOAD_MODE):
                return composeEngineMap(jParams, engineMap, theField);
            case (JahiaEngine.UPDATE_MODE):
                return getFormData(jParams, engineMap, theField);
            case (JahiaEngine.SAVE_MODE):
                return saveData(jParams, engineMap, theField);
        }
        return false;
    }

    private boolean getFormData(ProcessingContext jParams, Map<String, Object> engineMap, JahiaField theField)
            throws JahiaException {

        String fieldValue = jParams.getParameter("_" + Integer.toString(theField.getID()));

        if (fieldValue != null) {
            theField.setRawValue(fieldValue);
        }

        updateCategories(theField, jParams, engineMap);
        return true;
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
    private boolean saveData(ProcessingContext jParams, Map<String, Object> engineMap,
                             JahiaField theField)
            throws JahiaException {

        return saveCategories(theField, jParams, engineMap, engineMap.get("categorySelected_" +
                theField.getDefinition().getName()) != null ? null : theField.getValue());
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
    private boolean composeEngineMap(ProcessingContext jParams, Map<String, Object> engineMap, JahiaField theField)
            throws JahiaException {

        String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
        JahiaContainer theContainer = (JahiaContainer) engineMap.get(fieldsEditCallingEngineName + ".theContainer");

        // warns that application is being instanciated
        // cannot use the field id to create a unique enginemap name,
        // because can field id can change (in addcontainer mode, for example)
        // so we use the field definition's name to create a unique enginemap name...
        engineMap.put("categorySelected_" + theField.getDefinition().getName(), Boolean.TRUE);


        loadCategories(theField, jParams, engineMap, true);

        boolean editable = false;
        if (theContainer == null) {
            // in case of a field , not a field in a container
            editable = true;
        } else {
            FieldsEditHelper feh = (FieldsEditHelper) engineMap.get(fieldsEditCallingEngineName + "."
                    + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);
            Map<Integer, Integer> ctnListFieldAcls = feh.getCtnListFieldAcls();
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

        String output;
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
        if (editable && ! readOnly){
            output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean) jParams, JSP_FILE);
        } else {
            output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean) jParams, READONLY_JSP);
        }
        engineMap.put(fieldsEditCallingEngineName + "." + "fieldForm", output);
        return true;
    }

    private boolean loadCategories(final JahiaField theField,
                                   final ProcessingContext jParams,
                                   final Map<String, Object> engineMap,
                                   final boolean useDefaults)
            throws JahiaException {
        logger.debug("Loading categories from field " + theField);
        jParams.setAttribute("ZimbraInclude", "true");
        final SessionState session = jParams.getSessionState();
        List<String> selectedCategories = (List<String>) engineMap.get(theField.getDefinition().getName() +
                SELECTEDCATEGORIES_ENGINEMAPKEY);

        List<String> defaultSelectedCategories = new ArrayList<String>();
        boolean foundNoSelectionMarker = false;
        
        String rootCategoryKey = theField.getDefinition().getItemDefinition().getSelectorOptions().get("root");
        Category startCategory = (rootCategoryKey != null) ? Category.getCategory(rootCategoryKey, jParams.getUser()) : null;
        
        String fieldDefaultValue = theField.getDefinition().getDefaultValue();

        // the field default value contains a combination of :
        // the root category key name, as well as category keys
        // selected by default. The format is :
        // [rootCategoryKey]defaultCategoryKey1,defaultCategory2,...
        // the rootCategoryKey part is optional.


        logger.debug("fieldDefaultValue: " + fieldDefaultValue);
        if ((fieldDefaultValue != null) && !"".equals(fieldDefaultValue)) {

            int defaultCategoryKeysStartPos = 0;
            if ((fieldDefaultValue.indexOf("]") > 0) &&
                    (fieldDefaultValue.indexOf("[") == 0)) {
                defaultCategoryKeysStartPos = fieldDefaultValue.indexOf("]") + 1;
            }

            for (final String curCategoryKey : JahiaTools.getTokens(
                fieldDefaultValue.substring(defaultCategoryKeysStartPos),
                JahiaField.MULTIPLE_VALUES_SEP)) {
                final Category curCategory = Category.getCategory(curCategoryKey, jParams.getUser());
                if (curCategory != null) {
                    defaultSelectedCategories.add(curCategoryKey);
                    logger.debug("defaultSelectedCategories.add(curCategoryKey): " + curCategoryKey);
                } else {
                    logger.warn("Category " + curCategoryKey +
                            " is defined in field default value but does not exist in category tree.");
                }
            }
        }
        if (startCategory == null) {
            startCategory = Category.getRootCategory(jParams.getUser());
        }

        // still possible if the user has no rights to see even the root
        // category.
        if (startCategory == null) {
            engineMap.put("NoCategories", "NoCategories");
            return false;
        }
        logger.debug(START_CATEGORY+": " + startCategory.getKey());
        engineMap.put(START_CATEGORY, startCategory.getKey());
        engineMap.remove("NoCategories");

        if (selectedCategories == null) {
            selectedCategories = new ArrayList<String>();

            // we can now compare what's in the object links and what
            // we have in the field. The field values should be a subset of
            // the links, otherwise we have detected a synchronization
            // problem and must correct it.

            String[] categoryKeys;
            if (NOSELECTION_MARKER.equals(theField.getValue())) {
                categoryKeys = null;
                foundNoSelectionMarker = true;
            } else {
                categoryKeys = theField.getValues();
            }

            if ((categoryKeys != null) && (categoryKeys.length > 0)) {
                for (int i = 0; i < categoryKeys.length; i++) {
                    logger.debug("selectedCategories.add(categoryKeys[i]);");
                    selectedCategories.add(categoryKeys[i]);
                }
            }

            engineMap.put(theField.getDefinition().getName() + ORIGINALLYSELECTEDCATEGORIES_ENGINEMAPKEY,
                    selectedCategories);

            // if after all this we still have no category selections, let's
            // insert the default values, if there are any. We also need to
            // check if the field has ever been edited, in which case we will
            // not insert the default values.
            if ((selectedCategories.size() == 0) && (!foundNoSelectionMarker)) {
                selectedCategories.addAll(defaultSelectedCategories);
            }

            engineMap.put(theField.getDefinition().getName() + SELECTEDCATEGORIES_ENGINEMAPKEY, selectedCategories);
        }
        if ((selectedCategories.size() == 0) && (useDefaults)) {
            // this mechanism allows templates to set the currently
            // selected categories into the session and we can use these
            // values to initialize the selected categories.
            selectedCategories = (List<String>) session.getAttribute(
                    theField.getDefinition().getName() + DEFAULTCATEGORIES_SESSIONKEYPREFIX);
            logger.debug("Looking for default selected categories in session key " +
                    theField.getDefinition().getName() + DEFAULTCATEGORIES_SESSIONKEYPREFIX);
            if (selectedCategories != null) {
                logger.debug("Found default categories " + selectedCategories +
                        "for session key " +
                        theField.getDefinition().getName() + DEFAULTCATEGORIES_SESSIONKEYPREFIX);
            } else {
                selectedCategories = new ArrayList<String>();
            }
            engineMap.put(theField.getDefinition().getName() + SELECTEDCATEGORIES_ENGINEMAPKEY, selectedCategories);
        }

        engineMap.put(theField.getDefinition().getName() + FLATCATEGORYLIST_ENGINEMAPKEY, null);
        return true;
    }

    private boolean updateCategories(JahiaField theField,
                                     ProcessingContext jParams,
                                     Map<String, Object> engineMap) throws JahiaException {
        logger.debug("Processing categories update");
        final Map<String, Object> parameterMap = jParams.getParameterMap();

        final Iterator<String> paramNameIter = parameterMap.keySet().iterator();
        final List<String> newSelectedCategories = new ArrayList<String>();
        while (paramNameIter.hasNext()) {
            String curParamName = paramNameIter.next();
            if (curParamName.startsWith(CATEGORYPREFIX_HTMLPARAMETER)) {
                try {
                    final String curCategoryId = curParamName.substring(CATEGORYPREFIX_HTMLPARAMETER.length());
                    final Category c = Category.getCategory(Integer.parseInt(curCategoryId), null);
                    newSelectedCategories.add(c.getKey());
                    logger.debug("Submitted category key : " + c.getKey());
                } catch (final Exception e) {
                    logger.debug(e, e);
                }
            }
        }
        engineMap.put(theField.getDefinition().getName() + CATEGORIESUPDATED_ENGINEMAPKEY, Boolean.TRUE);
        engineMap.put(theField.getDefinition().getName() + SELECTEDCATEGORIES_ENGINEMAPKEY, newSelectedCategories);
        return true;
    }

    private boolean saveCategories(JahiaField theField, ProcessingContext jParams, Map<String, Object> engineMap, String defaultCategories)
            throws JahiaException {
        boolean success;
        logger.debug("Saving categories selection");
        List<String> selectedCategories = (List<String>) engineMap.get(
                theField.getDefinition().getName() + SELECTEDCATEGORIES_ENGINEMAPKEY);
        List<String> originallySelectedCategories = (List<String>) engineMap.get(
                theField.getDefinition().getName() + ORIGINALLYSELECTEDCATEGORIES_ENGINEMAPKEY);
        logger.debug("selectedCategories: " + selectedCategories);
        logger.debug("originallySelectedCategories: " + originallySelectedCategories);

        // selectedCategories will be null here if we never went into the
        // categories tab

        if (selectedCategories == null) {
            // this mechanism allows templates to set the currently
            // selected categories into the session and we can use these
            // values to initialize the selected categories.
            selectedCategories = (List<String>) jParams.getSessionState().
                    getAttribute(theField.getDefinition().getName() + DEFAULTCATEGORIES_SESSIONKEYPREFIX);
            logger.debug("Looking for default selected categories in session key " +
                    theField.getDefinition().getName() + DEFAULTCATEGORIES_SESSIONKEYPREFIX);
            if (selectedCategories != null) {
                logger.debug("Found default categories " +
                        selectedCategories + "for session key " +
                        theField.getDefinition().getName() + DEFAULTCATEGORIES_SESSIONKEYPREFIX);
            } else if (defaultCategories != null) {
                int defaultCategoryKeysStartPos = 0;
                if ((defaultCategories.indexOf("]") > 0) &&
                        (defaultCategories.indexOf("[") == 0)) {
                    // we have detected a root category key
                    defaultCategoryKeysStartPos = defaultCategories.indexOf("]") + 1;
                }
                final StringTokenizer categoryKeyTokens = new StringTokenizer(defaultCategories.substring(defaultCategoryKeysStartPos));
                while (categoryKeyTokens.hasMoreTokens()) {
                    final String curCategoryKey = categoryKeyTokens.nextToken();
                    final Category curCategory = Category.getCategory(curCategoryKey, jParams.getUser());
                    if (curCategory != null) {
                        if (selectedCategories == null) {
                            selectedCategories = new ArrayList<String>();
                        }
                        selectedCategories.add(curCategoryKey);
                        logger.debug("selectedCategories.add(curCategoryKey): " + curCategoryKey);
                    }
                }
            }
        }

        logger.debug("saving now.... " + selectedCategories);
        if (selectedCategories == null) {
            return false;
        }

        if (selectedCategories.size() == 0) {
            theField.setValue(NOSELECTION_MARKER);
        } else {
            Iterator<String> selectedCategoryIter = selectedCategories.iterator();
            StringBuffer multipleValue = new StringBuffer();
            while (selectedCategoryIter.hasNext()) {
                String curCategoryKey = selectedCategoryIter.next();
                multipleValue.append(curCategoryKey);
                if (selectedCategoryIter.hasNext()) {
                    multipleValue.append(JahiaField.MULTIPLE_VALUES_SEP);
                }
            }
            theField.setValue(multipleValue.toString());
        }

        success = theField.save(jParams);


        engineMap.remove(theField.getDefinition().getName() + ORIGINALLYSELECTEDCATEGORIES_ENGINEMAPKEY);
        engineMap.remove(theField.getDefinition().getName() + SELECTEDCATEGORIES_ENGINEMAPKEY);
        // remove tree also.
        engineMap.remove(theField.getDefinition().getName() + CATEGORYTREE_ENGINEMAPKEY);

        return success;
    }

    public static String toJavascriptArray(final List list) {
        if (list == null || list.size() == 0) return "[]";
        final StringBuffer res = new StringBuffer();
        res.append("[");
        for (int i = 0; i < list.size(); i++) {
            res.append("\"");
            res.append(list.get(i));
            res.append("\",");
        }
        res.deleteCharAt(res.length() - 1);
        res.append("]");
        return res.toString();
    }
}
