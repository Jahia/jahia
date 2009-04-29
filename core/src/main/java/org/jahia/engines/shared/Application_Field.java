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
//
//  Application_Field
//  EV  14.01.20001
//	NK	19.04.2001 Lot of changes with roles to work with new user/group and in multi site

package org.jahia.engines.shared;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.jahia.data.applications.*;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.FieldsEditHelper;
import org.jahia.data.fields.FieldsEditHelperAbstract;
import org.jahia.data.fields.JahiaField;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.JahiaEngineTools;
import org.jahia.engines.filemanager.TableEntry;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.registries.EnginesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockKey;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPortletNode;
import org.jahia.content.ObjectKey;
import org.jahia.content.ObjectLink;
import org.jahia.content.CategoryKey;

import javax.jcr.RepositoryException;

import java.io.Serializable;
import java.security.Principal;
import java.util.*;

public class Application_Field implements FieldSubEngine {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(Application_Field.class);

    private static Application_Field theObject = null;
    private static final String JSP_FILE = "/engines/shared/application_field.jsp";
    private static final String READONLY_JSP = "/engines/shared/readonly_application_field.jsp";
    public static final String APPLICATION_ROLES = "applicationRoles";
    private Map<Integer, List> appRoleMembers = null;

    /**
     * AK    19.12.2000
     *
     * @return a single instance of the object.
     */
    public static synchronized Application_Field getInstance() {
        if (theObject == null) {
            theObject = new Application_Field();
        }
        return theObject;
    }

    /**
     * handles the field actions
     *
     * @param jParams   a ProcessingContext object
     * @param modeInt   the mode, according to JahiaEngine
     * @param engineMap the engine parameters stored in a HashMap
     * @return true if everything went okay, false if not
     * @throws JahiaException
     * @see org.jahia.engines.JahiaEngine
     */
    public boolean handleField(ProcessingContext jParams, Integer modeInt, Map<String, Object> engineMap)
            throws JahiaException {

        int mode = modeInt.intValue();
        String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
        JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + "." + "theField");
        jParams.setAttribute(JahiaEngine.ENGINE_MODE_ATTRIBUTE, Integer.valueOf(mode));

        //JahiaField theField = (JahiaField) engineMap.get( "theField" );
        if (theField == null) {
            logger.debug("the field is null");
        }
        switch (mode) {
            case(JahiaEngine.LOAD_MODE):
                return composeEngineMap(jParams, engineMap, theField);
            case(JahiaEngine.UPDATE_MODE):
                return getFormData(jParams, engineMap, theField);
            case(JahiaEngine.SAVE_MODE):
                return saveData(jParams, engineMap, theField);
        }
        return false;
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
    private boolean getFormData(ProcessingContext jParams, Map<String, Object> engineMap, JahiaField theField)
            throws JahiaException {
        logger.debug("getFormaData()");
        String fieldValue = jParams.getParameter("_" + Integer.toString(theField.getID()));
        final String fileID = TableEntry.javascriptDecode(jParams.getParameter("file_id"));
        if (fieldValue != null && !"-1".equals(fieldValue)) {
            theField.setRawValue(fieldValue);
        } else if(fileID!=null) {
            try {
                final JCRNodeWrapper node = ServicesRegistry.getInstance().getJCRStoreService().getFileNode(fileID,jParams.getUser());
                if(node!=null && node instanceof JCRPortletNode)
            theField.setRawValue(node.getUUID());
            } catch (RepositoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        Integer oldAppID = (Integer) engineMap.get(theField.getDefinition().getName() + "_appID");
        if ((oldAppID == null) || (oldAppID.intValue() == -1)) {
            logger.debug("No webapp was selected.");
            //return true;
        } else {

            WebAppContext appContext = ServicesRegistry.getInstance()
                    .getApplicationsManagerService()
                    .getApplicationContext(oldAppID.intValue());
            List roles = appContext.getRoles();
            // Handle roles changes
            List<Set<Principal>> roleMembersList = new ArrayList<Set<Principal>>();
            if (roles != null) {
                String role = null;
                for (int roleNb = 0; roleNb < roles.size(); roleNb++) {
                    Set<Principal> roleMembers = getFormMembers(jParams, roleNb);
                    roleMembersList.add(roleMembers);
                }
            }
            appRoleMembers.put(oldAppID, roleMembersList);
            engineMap.put(APPLICATION_ROLES, appRoleMembers);
        }

        // update engineMap values for selected webapp from field value
        if (fieldValue != null) {
            int separatorPos = fieldValue.indexOf("_");
            if (separatorPos != -1) {
                String appIDStr = fieldValue.substring(0, separatorPos);
                int selectedAppID = Integer.parseInt(appIDStr);
                String selectedEntryPointDefName = fieldValue.substring(
                        separatorPos + 1);
                engineMap.put(theField.getDefinition().getName() + "_selectedEntryPointDefName", selectedEntryPointDefName);
                engineMap.put(theField.getDefinition().getName() + "_appID", Integer.valueOf(selectedAppID));
                engineMap.remove(theField.getDefinition().getName() + "_unAuthorized");
            }
        }
        return true;
    }

    /**
     * Find if an app. is selected
     *
     * @param theField the field we are working on
     * @return true if an app. is selected .
     */
    private boolean isAppSelected(JahiaField theField) {
        String selectedEntryPoint = (String) theField.getRawValue();
        if (selectedEntryPoint == null) {
            logger.debug("No app. selected");
            return false;
        }

        if (selectedEntryPoint.equalsIgnoreCase("") || selectedEntryPoint.equalsIgnoreCase("-1")) {
            logger.debug("No app. selected");
            return false;
        }
        int separatorPos = selectedEntryPoint.indexOf("_");
        if (separatorPos != -1) {
            String appIDStr = selectedEntryPoint.substring(0, separatorPos);
            if (appIDStr != null) {
                logger.debug("App. selected: Application[" + appIDStr + "]");
                return true;
            }
            logger.debug("Not app. selected");
            return false;
        }
        logger.debug("Not app. selected");
        return false;
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

        logger.debug("started");

        boolean success = true;
        if (engineMap.get("createApplication_" +
                theField.getDefinition().getName()) != null) {

                // this is an uninitialized field, let's keep it that way :
                theField.setValue((String) theField.getRawValue());
            success = theField.save(jParams);


            Boolean flushApp = Boolean.FALSE;
            if (engineMap.get("flushApp") != null) {
                flushApp = (Boolean) engineMap.get("flushApp");
                if (flushApp) {
                    SessionState session = jParams.getSessionState();
                }
            }
        }
        engineMap.remove(APPLICATION_ROLES);
        appRoleMembers = null;
        return success;
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

        logger.debug("started");

        String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
        JahiaContainer theContainer = (JahiaContainer) engineMap.get(fieldsEditCallingEngineName + "." + "theContainer");

        // warns that application is being instanciated
        // cannot use the field id to create a unique enginemap name,
        // because can field id can change (in addcontainer mode, for example)
        // so we use the field definition's name to create a unique enginemap name...
        final String definitionName = theField.getDefinition().getName();
        engineMap.put("createApplication_" + definitionName, Boolean.TRUE);

        // get allowed app. and entryPoint
        List[] authAppListAndEntryPointFilter = getAllowedApplicationsAndEntryPointFilter(jParams);
        List authAppList = authAppListAndEntryPointFilter[0];
        List entryPoinDefinitionFilter = authAppListAndEntryPointFilter[1];

        //put in engine map
        engineMap.put("appList", authAppList.iterator());
        engineMap.put("entryPoinDefinitionFilter", entryPoinDefinitionFilter);

        String entryPointInstanceID = null;
        String selectedEntryPointDefName = null;
        ApplicationBean appBean = null;
        int appID = -1;
        try {
            String fieldValue = theField.getRawValue();
            logger.debug("Field value: " + fieldValue);
            // fieldValue could contain appID_defName if we come from an
            // update, otherwise it simply contains an instanceID.
            if ((fieldValue != null) && (fieldValue.indexOf("_") != -1)) {
                // we found a field value in the form appID_defName
                appID = ((Integer) engineMap.get(definitionName + "_appID")).intValue();
                logger.debug("Application id: " + appID);
                selectedEntryPointDefName = (String) engineMap.get(definitionName + "_selectedEntryPointDefName");
                appBean = ServicesRegistry.getInstance().getApplicationsManagerService().getApplication(appID);
            } else {
                entryPointInstanceID = fieldValue;
            }
        } catch (NumberFormatException nfe) {
            // Set a correct default value.
            if (authAppList.size() > 0) {
                // appID = new Integer(((ApplicationBean)authAppList.get(0)).getID());
            }
        }

        if (appBean == null) {
            EntryPointInstance epInstance = ServicesRegistry.getInstance().
                    getApplicationsManagerService().
                    getEntryPointInstance(entryPointInstanceID);
            engineMap.put(definitionName+"_displaySelectInstance",Boolean.TRUE);
            if (epInstance != null) {
                selectedEntryPointDefName = epInstance.getDefName();
                int separatorPos = selectedEntryPointDefName.indexOf("###");
                if (separatorPos != -1) {
                    String portletDefName = selectedEntryPointDefName.substring(0, separatorPos);
                    selectedEntryPointDefName = portletDefName;
                }
                String contextName = epInstance.getContextName();
                logger.debug("application ID from entry point instance: " + contextName);
                appBean = ServicesRegistry.getInstance().getApplicationsManagerService().getApplication(contextName);
                appID = appBean.getID();
                try {
                    final JCRNodeWrapper uuid = ServicesRegistry.getInstance().getJCRStoreService().getNodeByUUID(entryPointInstanceID,jParams.getUser());
                    engineMap.put(definitionName+"_entryPointInstancePath",uuid.getPath());
                } catch (RepositoryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            engineMap.put(definitionName + "_entryPointInstanceID", entryPointInstanceID);
            engineMap.put(definitionName + "_selectedEntryPointDefName", selectedEntryPointDefName);
            engineMap.put(definitionName + "_appID", new Integer(appID));

            if(logger.isDebugEnabled()){
                logger.debug("Number of allowed application: " + authAppList.size());
                if (appBean != null) {
                    logger.debug("Selected application ID : " + appBean.getID());
                    logger.debug("Selected application is allowed for current user! " + authAppList.contains(appBean));
                } else {
                    logger.debug("No portlet selected. ");
                }
            }
        }
        else {
            engineMap.put(definitionName+"_displaySelectInstance",Boolean.FALSE);
        }
        // set auth flag
        if (authAppList != null && authAppList.size() > 0 && appBean != null && !authAppList.contains(appBean) && appID != -1) {
            engineMap.put(definitionName + "_unAuthorized", Boolean.TRUE);
        }

        List roles = new ArrayList();
        if (appID >= 0) {
            WebAppContext appContext = ServicesRegistry.getInstance().
                    getApplicationsManagerService().
                    getApplicationContext(appID);
            roles = appContext.getRoles();
        }
        engineMap.put("roles", roles);

        // Get role members for each web apps
        appRoleMembers = (Map<Integer, List>) engineMap.get(APPLICATION_ROLES);
        if (appRoleMembers == null) {
            appRoleMembers = new HashMap<Integer, List>();
        }
        List<Set> roleMembersList = appRoleMembers.get(new Integer(appID));
        if (roleMembersList == null) {
            roleMembersList = new ArrayList<Set>();
            // Fill role members in the array list.
            for (int i = 0; i < roles.size(); i++) {
                String role = (String) roles.get(i);
                JahiaGroup grp = ServicesRegistry.getInstance().
                        getJahiaGroupManagerService().
                        lookupGroup(0,
                                entryPointInstanceID + "_" + role);
                if (grp != null) {
                    roleMembersList.add(getAppMembers(grp, i));
                } else {
                    roleMembersList.add(new HashSet());
                }
            }
            appRoleMembers.put(new Integer(appID), roleMembersList);
        }
        engineMap.put(APPLICATION_ROLES, appRoleMembers);
        engineMap.put("selectUsrGrp",
                EnginesRegistry.getInstance().getEngineByBeanName("selectUGEngine").renderLink(jParams, ""));

        /* Hollis
         * Why request application output when simply editing the field properties ?
         * Doing so , we sometimes expect with an out of memory range exception ( random error )
         * So I comment it .
         */
        //theField.setValue( FormDataManager.formDecode(theField.getValue()) );
        //theField.setValue( FormDataManager.decode(theField.getValue()) );


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

        String output = "";
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
        if (editable && !readOnly) {
            output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean) jParams, JSP_FILE);
        } else {
            output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean) jParams, READONLY_JSP);
        }
        engineMap.put(fieldsEditCallingEngineName + "." + "fieldForm", output);
        return true;
    }

    private Set<Principal> getFormMembers(ProcessingContext jParams, int roleNb) {
        String[] authMembersStr = jParams.getParameterValues("authMembers" + roleNb);
        Set<Principal> membersSet = new HashSet<Principal>();
        if (authMembersStr != null) {
            for (int i = 0; i < authMembersStr.length; i++) {
                if (authMembersStr[i].charAt(0) == 'u') {
                    JahiaUser user = ServicesRegistry.getInstance().
                            getJahiaUserManagerService().
                            lookupUserByKey(authMembersStr[i].substring(1));
                    membersSet.add(user);
                } else {
                    JahiaGroup group = ServicesRegistry.getInstance().
                            getJahiaGroupManagerService().
                            lookupGroup(authMembersStr[i].substring(1));
                    membersSet.add(group);
                }
            }
        }
        return membersSet;
    }

    private Set<Principal> getAppMembers(JahiaGroup grp, int role) {
        Set<Principal> membersSet = new HashSet<Principal>();
        Iterator memberEnum = new EnumerationIterator(grp.members());
        while (memberEnum.hasNext()) {
            membersSet.add((Principal) memberEnum.next());
        }
        return membersSet;
    }

    // Get all entryPointDefinition
    private List[] getAllowedApplicationsAndEntryPointFilter(ProcessingContext jParams) throws JahiaException {
        // all application
        List appList = ServicesRegistry.getInstance().getApplicationsManagerService().getApplications();

        // CategoryKey
        String catId = jParams.getParameter("catId");
        CategoryKey catKey = null;
        if (catId != null && !catId.equalsIgnoreCase("")) {
            catKey = new CategoryKey(Integer.parseInt(catId));
        }

        //result Object
        List<Serializable> entryPointFilter = new ArrayList<Serializable>();
        List<Serializable> authAppList = new ArrayList<Serializable>();

        Iterator it = appList.iterator();
        while (it.hasNext()) {
            ApplicationBean appBean = (ApplicationBean) it.next();
            if (appBean.getACL().getPermission(null, null, jParams.getUser(), JahiaBaseACL.READ_RIGHTS, true, jParams.getSiteID())) {
                // add to auhorised list
                authAppList.add(appBean);

                // update  entryPointFilter
                List entryPointDefinitionList = appBean.getEntryPointDefinitions();
                for (int i = 0; i < entryPointDefinitionList.size(); i++) {
                    EntryPointDefinition epd = (EntryPointDefinition) entryPointDefinitionList.get(i);
                    String currentEntryPointDefinitionKey = epd.getApplicationID() + "_" + epd.getName();
                    logger.debug("Check for [" + currentEntryPointDefinitionKey + "].");

                    // compute entryPointKey
                    EntryPointObjectKey objKey = new EntryPointObjectKey(currentEntryPointDefinitionKey);
                    if (catKey != null) {
                        if (isInCategory(catKey, objKey)) {
                            entryPointFilter.add(currentEntryPointDefinitionKey);
                        }
                    } else {
                        entryPointFilter.add(currentEntryPointDefinitionKey);
                        logger.debug("Add all entryPoint.");
                    }
                }
            } else {
                logger.debug("Portlet of application [" + appBean.getName() + "] not allowed for user: " + jParams.getUser());
            }

        }

        //result
        List[] result = new ArrayList[2];
        result[0] = authAppList;
        result[1] = entryPointFilter;
        return result;
    }

    private boolean isInCategory(CategoryKey catKey, EntryPointObjectKey epok) {
        try {
            List result = ObjectLink.findByLeftAndRightObjectKeys(catKey, epok);
            return !result.isEmpty();
        } catch (JahiaException e) {
            logger.error("Can't find object link with parameters: [" + catKey + "," + epok + "]");
        }
        return false;
    }


    // Get entryPointDefinition by category
    private java.util.List<EntryPointDefinition> getAllowedEntyPointDefinitionByCategory(ProcessingContext jParams, ObjectKey objectKey) throws JahiaException {
        List<EntryPointDefinition> entryPointDefinitionList = new ArrayList<EntryPointDefinition>();
        // find all link that refers to an EntryPointDefintion

        List entryPointDefinitionLink = ObjectLink.findByTypeAndLeftObjectKey(EntryPointObjectKey.ENTRY_POINT_TYPE, objectKey);
        logger.debug("Nb portlets:" + entryPointDefinitionLink.size());
        for (int i = 0; i < entryPointDefinitionLink.size(); i++) {
            ObjectLink link = (ObjectLink) entryPointDefinitionLink.get(i);
            EntryPointObjectKey epok = (EntryPointObjectKey) link.getRightObjectKey();
            if (epok == null) {
                logger.error("EntryPointKey Not defined: " + link.getRightObjectKey() + "," + link.getLeftObjectKey());
            } else {

                // get application Id and entrypoint definition name. pattern: entrypoint_appid_epfdName
                String key = epok.getKey();
                logger.debug("EntryPointDefinitionkey:" + key);
                StringTokenizer strg = new StringTokenizer(key, "_");
                //remove 'entrypoint'
                strg.nextToken();
                // get appId
                int appId = Integer.parseInt(strg.nextToken());
                // get entrypointName
                String entryPointName = strg.nextToken();
                //find application bean
                ApplicationBean aBean = ServicesRegistry.getInstance().getApplicationsManagerService().getApplication(appId);
                if (aBean.getACL().getPermission(null, null, jParams.getUser(), JahiaBaseACL.READ_RIGHTS, true, jParams.getSiteID())) {
                    // get entryPoint definition
                    EntryPointDefinition epd = aBean.getEntryPointDefinitionByName(entryPointName);
                    entryPointDefinitionList.add(epd);
                } else {
                    logger.debug("Portlet of application [" + aBean.getName() + "] not allowed for user: " + jParams.getUser());
                }
            }

        }

        return entryPointDefinitionList;
    }
}
