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
package org.jahia.engines.containerlistproperties;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.data.containers.JahiaContentContainerFacade;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.LoadFlags;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.categories.ManageCategories;
import org.jahia.engines.metadata.Metadata_Engine;
import org.jahia.engines.rights.ManageRights;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.engines.workflow.ManageWorkflow;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.audit.LoggingEventListener;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTypes;
import org.jahia.services.lock.Lock;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.lock.LockService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.comparator.NumericStringComparator;

import java.util.*;

public class ContainerListProperties_Engine implements JahiaEngine {

    /**
     * The engine's name
     */
    public static final String ENGINE_NAME = "containerlistproperties";

    /**
     * logging
     */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ContainerListProperties_Engine.class);

    private static final String TEMPLATE_JSP = "container_list_properties";

    private EngineToolBox toolBox;

    /**
     * Default constructor, creates a new <code>ContainerListProperties_Engine</code> instance.
     */
    public ContainerListProperties_Engine () {
        toolBox = EngineToolBox.getInstance ();
    } // end constructor

    /**
     * authoriseRender
     */
    public boolean authoriseRender (final ProcessingContext jParams) {
        return toolBox.authoriseRender (jParams);
    }

    /**
     * renderLink
     */
    public String renderLink (final ProcessingContext jParams, final Object theObj) throws JahiaException {
        final Properties extraParams = new Properties ();
        String params = EMPTY_STRING;
        final ContentContainerList contentContainerList = (ContentContainerList) theObj;
        final JahiaContainerDefinition containerListDefinition = JahiaContainerDefinitionsRegistry.getInstance ().
                getDefinition (contentContainerList.getContainerListDefinitionID ());
        final String containerListName = containerListDefinition.getName ();
        final List<Integer> ctnIDs = ServicesRegistry.getInstance ().getJahiaContainersService ().getctnidsInList (
                contentContainerList.getID (), jParams.getEntryLoadRequest ());
        String scrollStr = Integer.toString (ctnIDs.size ()) + "_0";
        if (scrollStr.length() > 0) {
            extraParams.setProperty(ProcessingContext.CONTAINER_SCROLL_PREFIX_PARAMETER + containerListName, scrollStr);
        }
        params += "?mode=display";
        params += "&clid=" + contentContainerList.getID ();
        return jParams.composeEngineUrl (ENGINE_NAME, extraParams, params);
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
    public EngineValidationHelper handleActions (final ProcessingContext jParams, final JahiaData jData)
            throws JahiaException {
        // initalizes the hashmap
        final Map<String, Object> engineMap = initEngineMap (jParams);

        // checks if the user has the right to display the engine
        final JahiaContainerList theContainerList = (JahiaContainerList) engineMap.get ("theContainerList");
        final JahiaUser user = jParams.getUser();

        if (theContainerList.checkAdminAccess (user)) {
            engineMap.put ("adminAccess", Boolean.TRUE);
            engineMap.put ("enableAuthoring", Boolean.TRUE);
            engineMap.put ("enableMetadata", Boolean.TRUE);
            engineMap.put ("enableRightView", Boolean.TRUE);
            engineMap.put ("enableAdvancedWorkflow", Boolean.TRUE);
            engineMap.put ("writeAccess", Boolean.TRUE);
            engineMap.put ("enableVersioning", Boolean.TRUE);
        } else if (theContainerList.checkWriteAccess (user)) {
            engineMap.put ("enableAuthoring", Boolean.TRUE);
            engineMap.put ("enableMetadata", Boolean.TRUE);
            engineMap.put ("writeAccess", Boolean.TRUE);
            engineMap.put ("enableVersioning", Boolean.TRUE);
        }

        if (engineMap.get ("writeAccess") != null) {
            // #ifdef LOCK
            final LockService lockRegistry = ServicesRegistry.getInstance ().getLockService ();
            if (jParams.settings ().areLocksActivated ()) {
                final LockKey lockKey = LockKey.composeLockKey (LockKey.UPDATE_CONTAINERLIST_TYPE,
                        theContainerList.getID ());
                if (lockRegistry.acquire (lockKey, user,
                        user.getUserKey(), jParams.getSessionState ().getMaxInactiveInterval ())) {
                    // #endif
                    engineMap.put("lock", lockKey);

                    processLastScreen (jParams, engineMap);
                    processCurrentScreen (jParams, engineMap);

                    // #ifdef LOCK
                } else {
                    final Map<String, Set<Lock>> m = lockRegistry.getLocksOnObject(lockKey);
                    if (! m.isEmpty()) {
                        final String action = m.keySet().iterator().next();
                        engineMap.put("LockKey", LockKey.composeLockKey(lockKey.getObjectKey(), action));
                    } else {
                        final LockPrerequisitesResult results = LockPrerequisites.getInstance().getLockPrerequisitesResult(lockKey);
                        engineMap.put("LockKey", results.getFirstLockKey());
                    }
                    if ("edit".equals(jParams.getParameter("lastscreen"))) {
                        processLastScreen(jParams, engineMap);
                    }
                    processCurrentScreen(jParams, engineMap);
                }
            }
            // #endif
        } else {
            throw new JahiaForbiddenAccessException ();
        }

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
     * @param jParams a ProcessingContext object
     */
    public void processLastScreen (final ProcessingContext jParams, final Map<String, Object> engineMap)
            throws JahiaException {
        // Sets the container list
        JahiaContainerList theContainerList = (JahiaContainerList) engineMap.get ("theContainerList");

        // gets the last screen
        // lastscreen   = edit, rights, logs
        String lastScreen = jParams.getParameter ("lastscreen");
        logger.debug ("processLastScreen: " + lastScreen);
        engineMap.put ("lastscreen", lastScreen);
        if (lastScreen == null) {
            lastScreen = "edit";
        }

        // indicates to sub engines that we are processing last screen
        int mode = JahiaEngine.UPDATE_MODE;

        // dispatches to the appropriate sub engine
        if (lastScreen.equals ("edit")) {
            if (!updateContainerListData (jParams, engineMap)) {
                // if there was an error, come back to last screen
                engineMap.put ("screen", lastScreen);
                engineMap.put ("jspSource", TEMPLATE_JSP);
            }
        } else if (lastScreen.equals("versioning")) {
            engineMap.put(RENDER_TYPE_PARAM, JahiaEngine.RENDERTYPE_FORWARD);
            // reset engine map to default value
            engineMap.remove(ENGINE_OUTPUT_FILE_PARAM);
        } else if (lastScreen.equals ("rightsMgmt") ||
                lastScreen.equals ("ctneditview_rights")) {
            if (engineMap.get ("adminAccess") != null) {
                engineMap.put ("logObjectType",
                        Integer.toString (LoggingEventListener.
                                CONTAINERLIST_TYPE));
                engineMap.put ("logObject", theContainerList);
                ContentContainerList contentContainerList = ContentContainerList.getContainerList(theContainerList.getID());
                if (lastScreen.equals ("rightsMgmt")) {
                    ManageRights.getInstance ().handleActions (jParams, mode,
                            engineMap,
                            contentContainerList.getAclID (), null, null,contentContainerList.isAclSameAsParent(),theContainerList.getContentContainerList().getObjectKey().toString());
                } else {
                    Map<String, Integer> acls = (Map<String, Integer>) engineMap.get ("fieldAcls");
                    if (acls == null)
                        return;
                    String val = (String) engineMap.get ("aclfieldname");
                    if (val != null) {
                        Integer I = acls.get (val);
                        if (I != null) {
                            ManageRights.getInstance ().handleActions (jParams,
                                    mode, engineMap,
                                    I, null, null,contentContainerList.getAclID()==I,val);
                        }
                    }
                }
            } else {
                throw new JahiaForbiddenAccessException ();
            }
        } else if (lastScreen.equals ("categories")) {
            ManageCategories.getInstance ().handleActions (jParams, mode,
                    engineMap, new ContentContainerListKey (theContainerList.getID ()),
                    theContainerList.getDefinition (), false);
        } else if (lastScreen.equals("workflow")) {
            final boolean isReadOnly = LockPrerequisites.getInstance().
                    getLockPrerequisitesResult((LockKey) engineMap.get("LockKey")) != null;
            if (engineMap.get("adminAccess") != null || isReadOnly) {
                ManageWorkflow.getInstance().handleActions(jParams, mode,
                        engineMap,  theContainerList.getContentContainerList());
            } else {
                throw new JahiaForbiddenAccessException();
            }
        } else if (lastScreen.equals ("metadata")) {
            final ObjectKey objectKey;
            if ( theContainerList.getID()>0 ){
                ContentContainerList contentContainerList = ContentContainerList.
                        getContainerList(theContainerList.getID());
                objectKey = contentContainerList.getObjectKey();
            } else {
                JahiaContainerDefinition def = theContainerList.getDefinition();
                objectKey = def.getObjectKey();
            }
            Metadata_Engine.getInstance().handleActions(jParams,mode,objectKey);
        }
    }

    /**
     * prepares the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public void processCurrentScreen (final ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {
        // Sets the container list
        final JahiaContainerList theContainerList = (JahiaContainerList) engineMap.get ("theContainerList");

        // gets the current screen
        // screen   = edit, rights, logs
        final String theScreen = (String) engineMap.get ("screen");
        logger.debug ("processCurrentScreen: " + theScreen);

        // indicates to sub engines that we are processing last screen
        int mode = JahiaEngine.LOAD_MODE;

        // #ifdef LOCK
        final LockKey lockKey = LockKey.composeLockKey (LockKey.UPDATE_CONTAINERLIST_TYPE, theContainerList.getID ());
        final LockService lockRegistry = ServicesRegistry.getInstance ().getLockService ();
        // #endif

        // dispatches to the appropriate sub engine
        final JahiaUser user = jParams.getUser();
        if (theScreen.equals ("edit")) {
            //loadContainerListData (jParams, engineMap);

        } else if (theScreen.equals ("logs")) {
            if (engineMap.get ("lastscreen") != null &&
                    engineMap.get ("lastscreen").equals ("edit")) {
                // save container's rank
                saveContainersRank (jParams, engineMap);
            }
            toolBox.loadLogData (jParams,
                    LoggingEventListener.CONTAINERLIST_TYPE,
                    engineMap);

        } else if (theScreen.equals ("categories")) {
            ManageCategories.getInstance ().handleActions (jParams, mode,
                    engineMap, new ContentContainerListKey (theContainerList.getID ()),
                    theContainerList.getDefinition (), false);

        } else {
            ContentContainerList contentContainerList = ContentContainerList.getContainerList(theContainerList.getID());
            boolean sameAcl = contentContainerList.isAclSameAsParent();
            if (theScreen.equals ("rightsMgmt") ||
                    theScreen.equals ("ctneditview_rights")) {

                if (engineMap.get ("lastscreen") != null &&
                        engineMap.get ("lastscreen").equals ("edit")) {
                    // save container's rank
                    saveContainersRank (jParams, engineMap);
                }
                if (engineMap.get ("adminAccess") != null) {
                    if (theScreen.equals ("rightsMgmt")) {
                        ManageRights.getInstance ().handleActions (jParams, mode,
                                engineMap,
                                contentContainerList.getAclID (), null, null, sameAcl,contentContainerList.getObjectKey().toString());
                    } else {
                        logger.debug ("Step1");

                        Map<String, Integer> acls = (Map<String, Integer>) engineMap.get ("fieldAcls");
                        if (acls == null)
                            return;
                        String val = jParams.getParameter ("aclfieldname");
                        logger.debug ("aclfieldname=" + val);

                        if (val == null) {
                            // use the first
                            val = (String) engineMap.get ("fieldAclDefaultField");
                        }
                        if (val != null) {
                            // store for JSP
                            engineMap.put ("aclfieldname", val);
                            engineMap.put ("fieldAclDefaultField", val);
                            Integer I = acls.get (val);
                            if (I != null) {
                                logger.debug ("Step3");
                                ManageRights.getInstance ().handleActions (jParams,
                                        mode, engineMap,
                                        I, null, null, I == contentContainerList.getAclID(), val);
                            }
                        } else {
                            engineMap.put ("aclfieldname", EMPTY_STRING);
                        }
                    }
                } else {
                    throw new JahiaForbiddenAccessException ();
                }
            } else if (theScreen.equals("workflow")) {
                if (engineMap.get("adminAccess") != null) {
                    ManageWorkflow.getInstance().handleActions(jParams, mode,
                            engineMap,  theContainerList.getContentContainerList());
                } else {
                    throw new JahiaForbiddenAccessException();
                }
            } else if (theScreen.equals ("metadata")) {
                final ObjectKey objectKey;
                if ( theContainerList.getID()>0 ){
                    ContentContainerList contentContainer = ContentContainerList.
                            getContainerList(theContainerList.getID());
                    objectKey = contentContainer.getObjectKey();
                } else {
                    JahiaContainerDefinition def = theContainerList.getDefinition();
                    objectKey = def.getObjectKey();
                }
                Metadata_Engine.getInstance().handleActions(jParams,mode,objectKey);
            } else if (theScreen.equals("versioning")) {

                String goTo = jParams.getParameter("method");
                if (goTo == null || goTo.length() == 0) {
                    goTo = "showContainersList";
                }
                logger.debug("Going to: " + goTo);

                final Properties params = new Properties();
                params.put("method", goTo);
                params.put("objectKey", new ContentContainerListKey(theContainerList.getID()).toString());
                final String versioningURL = jParams.composeStrutsUrl(
                        "ContainerListVersioning", params, null);
                engineMap.put(RENDER_TYPE_PARAM, JahiaEngine.RENDERTYPE_FORWARD);
                engineMap.put(JahiaEngine.ENGINE_REDIRECT_URL,versioningURL);
                engineMap.put(ENGINE_OUTPUT_FILE_PARAM, JahiaEngine.REDIRECT_JSP);

            } else if (theScreen.equals("save") || theScreen.equals("apply")) {
                final String lastScreen = jParams.getParameter("lastscreen");
                final LockKey futureStolenkey = (LockKey) engineMap.get("LockKey");
                if (LockPrerequisites.getInstance().getLockPrerequisitesResult(futureStolenkey) != null) {
                    final String param = jParams.getParameter("whichKeyToSteal");
                    if (param != null && param.length() > 0) {
                        if (lockRegistry.isAlreadyAcquired(futureStolenkey) &&
                                !lockRegistry.isAlreadyAcquiredInContext(lockKey, user, user.getUserKey())) {
                            logger.debug("steal: " + user.getUsername());
                            lockRegistry.steal(futureStolenkey, user, user.getUserKey());
                        } else {
                            logger.debug("nuke: " + user.getUsername());
                            lockRegistry.nuke(futureStolenkey, user, user.getUserKey());
                        }
                        if (lockRegistry.acquire(lockKey, user, user.getUserKey(),
                                jParams.getSessionState().getMaxInactiveInterval())) {
                            engineMap.remove("LockKey");
                            jParams.getSessionState().setAttribute("jahia_session_engineMap", engineMap);
                            logger.debug("We were able to acquire the lock after stealing");
                        } else {
                            logger.debug("We were unable to acquire the lock after stealing");
                        }
                        jParams.getSessionState().removeAttribute("showNavigationInLockEngine");
                    }
                    engineMap.put("screen", lastScreen);
                    engineMap.put("jspSource", TEMPLATE_JSP);
                    processCurrentScreen(jParams, engineMap);
                    return;
                }

                // #ifdef LOCK
                // Did somebody steal the lock ? Panpan cucul !
                if (jParams.settings ().areLocksActivated () &&
                        lockRegistry.isStealedInContext (lockKey, user,user.getUserKey())) {
                    engineMap.put ("screen",jParams.getParameter ("lastscreen"));
                    engineMap.put ("jspSource", "apply");
                    return;
                }
                // #endif

                mode = JahiaEngine.SAVE_MODE;
                /*
                if (transactionTemplate == null) {
                    SpringContextSingleton instance = SpringContextSingleton.getInstance();
                    if (instance.isInitialized()) {
                        PlatformTransactionManager manager = (PlatformTransactionManager) instance.getContext().getBean("transactionManager");
                        transactionTemplate = new TransactionTemplate(manager);
                    }
                }*/
                try {
    //                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
    //                    protected void doInTransactionWithoutResult(TransactionStatus status) {
    //                        try {
                    // save workflow
                    if(!ManageWorkflow.getInstance().handleActions(jParams, mode,
                            engineMap, theContainerList.getContentContainerList())) {
                        engineMap.put("screen", "workflow");
                        engineMap.put("jspSource", TEMPLATE_JSP);
                        return;
                    }
                    if (engineMap.get("lastscreen") != null &&
                            engineMap.get("lastscreen").equals("edit")) {
                        // save container's rank
                        saveContainersRank(jParams, engineMap);
                    }

                    Set<ContentObject> pickers = contentContainerList.getPickerObjects();

                    // save the page info
                    final Properties p = theContainerList.getProperties();
                    if (!contentContainerList.getProperties().equals(p)) {
                        final Set<ContentObject> objects = new HashSet<ContentObject>(pickers);
                        objects.add(contentContainerList);
                        for (ContentObject object : objects) {
                            ContentContainerList l = (ContentContainerList) object;
                            for (Enumeration<?> iterator1 = p.propertyNames(); iterator1.hasMoreElements();) {
                                String key = (String) iterator1.nextElement();
                                if (!p.get(key).equals(l.getProperty(key))) {
                                    l.setProperty(key, p.getProperty(key));
                                }
                            }
                            JahiaEvent theEvent = new JahiaEvent(this, jParams, l.getJahiaContainerList(jParams, jParams.getEntryLoadRequest()));
                            ServicesRegistry.getInstance().getJahiaEventService().fireSetContainerListProperties(theEvent);
                            l.setUnversionedChanged();
                        }
                    }
                    // save rights
                    Integer aclID = null;
                    if (engineMap.get("adminAccess") != null) {
                        // save container list ACLs
                        if ("ctneditview_rights".equals(engineMap.get("lastscreen"))) {
                            String aclfieldname = jParams.getParameter(
                                    "aclfieldname");
                            logger.debug("Save container list field ACL : " +
                                    aclfieldname);
                            // save container list field acl
                            Map<String, Integer> acls = (Map<String, Integer>) engineMap.get("fieldAcls");
                            if (acls != null) {
                                aclID = (Integer) acls.get(aclfieldname);
                                boolean sameAcl2 = aclID == contentContainerList.getAclID();
                                ManageRights.getInstance().handleActions(jParams, mode,
                                        engineMap, aclID, null, null, sameAcl2, aclfieldname);
                                if (sameAcl2) {
                                    JahiaBaseACL acl = (JahiaBaseACL) engineMap.get(ManageRights.NEW_ACL+"_"+aclfieldname);
                                    if (acl != null) {
                                        contentContainerList.setProperty ("view_field_acl_" + aclfieldname, String.valueOf (acl.getID()));
                                        for (ContentObject picker : pickers) {
                                            ContentContainerList l = (ContentContainerList) picker;
                                            JahiaBaseACL jAcl = new JahiaBaseACL();
                                            jAcl.create(l.getAclID(), acl.getID());
                                            l.setProperty("view_field_acl_" + aclfieldname, Integer.toString(jAcl.getID()));
                                        }
                                    }
                                }
                                if (Boolean.TRUE.equals(engineMap.get("rightsUpdated"))) {
                                    theContainerList.getContentContainerList().setUnversionedChanged();
                                }

                            }
                        } else {
                            //ViewRights.getInstance().handleActions( jParams, mode, engineMap,
                            ManageRights.getInstance().handleActions(jParams, mode,
                                    engineMap,
                                    contentContainerList.getAclID(), null, null, sameAcl,contentContainerList.getObjectKey().toString());
                            if (sameAcl) {
                                JahiaBaseACL acl = (JahiaBaseACL) engineMap.get(ManageRights.NEW_ACL+"_"+theContainerList.getContentContainerList().getObjectKey());
                                if (acl != null) {
                                    theContainerList.getContentContainerList().updateAclForChildren(acl.getID());
                                }
                                contentContainerList = ContentContainerList.getContainerList(theContainerList.getID());
                            }
                            if (Boolean.TRUE.equals(engineMap.get("rightsUpdated"))) {
                                theContainerList.getContentContainerList().setUnversionedChanged();
                            }
                        }
                    }

                    // save categories
                    ManageCategories.getInstance().handleActions(jParams, mode,
                            engineMap,
                            new ContentContainerListKey(theContainerList.getID()),
                            theContainerList.getDefinition(), false);

                    // save metadata
                    final ObjectKey objectKey;
                    if (theContainerList.getID() > 0) {
                        objectKey = contentContainerList.getObjectKey();
                    } else {
                        JahiaContainerDefinition def = theContainerList.getDefinition();
                        objectKey = def.getObjectKey();
                    }
                    Metadata_Engine.getInstance().handleActions(jParams, mode, objectKey);

                    // fire event
                    JahiaEvent theEvent = new JahiaEvent(this, jParams,
                            theContainerList);
                    ServicesRegistry.getInstance().getJahiaEventService().
                            fireSetContainerListProperties(theEvent);
                    logger.debug("Container list saved");
                    if (theScreen.equals("apply")) {
                        engineMap.put("screen", lastScreen);
                        fillEngineMap(theContainerList.getID(), jParams, engineMap,(String) engineMap.get("cursorField"));
                        if ("ctneditview_rights".equals(lastScreen)) {
                            ManageRights.getInstance().handleActions(jParams,
                                    JahiaEngine.LOAD_MODE,
                                    engineMap, aclID, null, null, aclID == contentContainerList.getAclID(), jParams.getParameter("aclfieldname"));
                        } else if ("rightsMgmt".equals(lastScreen)) {
                            ManageRights.getInstance().handleActions(jParams,
                                    JahiaEngine.LOAD_MODE,
                                    engineMap, contentContainerList.getAclID(), null, null, contentContainerList.isAclSameAsParent(),theContainerList.getContentContainerList().getObjectKey().toString());
                        }
                    }
    //                        } catch (Exception e) {
    //                            throw new RuntimeException(e);
    //                        }
    //                    }
    //                });
                } catch (JahiaException e) {
                    logger.error("Error during update operation of an element we must flush all caches to ensure integrity between database and viewing");
                    ServicesRegistry.getInstance().getCacheService().flushAllCaches();
                    throw new JahiaException(e.getMessage(), e.getMessage(),
                            JahiaException.DATABASE_ERROR, JahiaException.CRITICAL_SEVERITY, e);
                } finally{
                    // #ifdef LOCK
                    if (theScreen.equals("save")) {
                        if (jParams.settings().areLocksActivated()) {
                            lockRegistry.release(lockKey, user,user.getUserKey());
                        }
                    }
                    // #endif
                }
            } else if (theScreen.equals("cancel")) {
                if (jParams.settings ().areLocksActivated ()) {
                    lockRegistry.release (lockKey, user,user.getUserKey());
                    ManageWorkflow.getInstance().handleActions(jParams, CANCEL_MODE, engineMap, theContainerList.getContentContainerList());
                }
            }
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
    private Map<String, Object> initEngineMap (ProcessingContext jParams)
            throws JahiaException {

        Map<String, Object> engineMap = new HashMap<String, Object>();
        JahiaContainerList theContainerList = null;

        String theScreen = jParams.getParameter ("screen");
        String cListIDStr = jParams.getParameter ("clid");
        int cListID = 0;
        try {
            cListID = Integer.parseInt (cListIDStr);
        } catch (Exception e) {
            throw new JahiaException ("Error in parameters",
                    "ContainerListProperties_Engine : error in parameters",
                    JahiaException.PARAMETER_ERROR,
                    JahiaException.CRITICAL_SEVERITY, e);
        }

        // gets session values
        //HttpSession theSession = jParams.getRequest().getSession (true);
        SessionState theSession = jParams.getSessionState ();
        boolean startNewSession = false;
        if (theScreen != null) {
            // if no, load the container value from the session
            engineMap = (Map<String, Object>) theSession.getAttribute ("jahia_session_engineMap");

            ///////////////////////////////////////////////////////////////////////////////////////
            // FIXME -Fulco-
            //
            //      This is a quick hack, engineMap should not be null if the session didn't
            //      expired. Maybe there are other cases where the engineMap can be null, I didn't
            //      checked them at all.
            ///////////////////////////////////////////////////////////////////////////////////////
            if (engineMap == null) {
                throw new JahiaSessionExpirationException ();
            }
            String previousScreen = (String)engineMap.get("screen");
            if ( "versioning".equals(previousScreen) ){
                startNewSession = true;
            } else {
                theContainerList = (JahiaContainerList) engineMap.get ("theContainerList");
            }
        } else {
            startNewSession = true;
        }
        // Init Engine Language Helper
        EngineLanguageHelper elh = (EngineLanguageHelper)engineMap
                .get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        if ( elh == null ){
            elh = new EngineLanguageHelper();
            engineMap.put(JahiaEngine.ENGINE_LANGUAGE_HELPER,elh);
        }
        elh.update(jParams);
        if ( startNewSession ){
            if ( theScreen == null || "".equals(theScreen) ){
                theScreen = "edit";
            }
            // init engine map
            theContainerList = fillEngineMap(cListID, jParams, engineMap,"");
            if (!engineMap.containsKey("cursorField")) {
                engineMap.put ("cursorField", "");
            }
            engineMap.put ("containerSelected", 0);
        }

        if (theContainerList == null) {
            throw new JahiaException ("ContainerList not found",
                    "ContainerListProperties_Engine : ContainerList not found",
                    JahiaException.PARAMETER_ERROR,
                    JahiaException.CRITICAL_SEVERITY);
        }

        if(!elh.getCurrentLanguageCode().equals(elh.getPreviousLanguageCode())) {
            EntryLoadRequest entryLoadRequest = jParams.getEntryLoadRequest();
            jParams.setEntryLoadRequest(elh.getCurrentEntryLoadRequest());
            fillEngineMap(cListID, jParams, engineMap,(String) engineMap.get("cursorField"));
            jParams.setEntryLoadRequest(entryLoadRequest);
        }

        if (theScreen.equals("edit")) {
            engineMap.put ("fields", engineMap.get("fields_metadata"));
        } else if (theScreen.equals("ctneditview_rights")) {
            engineMap.put ("fields", engineMap.get("fields_nometadata"));
        }

        Properties extraParams = new Properties ();
        String containerListName = theContainerList.getDefinition ().getName ();
        String scrollStr = jParams.getParameter (ProcessingContext.CONTAINER_SCROLL_PREFIX_PARAMETER +
                containerListName);
        if (scrollStr != null) {
            if (scrollStr.length () > 0) {
                extraParams.setProperty (ProcessingContext.CONTAINER_SCROLL_PREFIX_PARAMETER + containerListName,
                        scrollStr);
            }
        }
        engineMap.put (RENDER_TYPE_PARAM, JahiaEngine.RENDERTYPE_FORWARD);
        engineMap.put (ENGINE_NAME_PARAM, ENGINE_NAME);
        engineMap.put (ENGINE_URL_PARAM,
                jParams.composeEngineUrl (ENGINE_NAME, extraParams,
                        "&clid=" +
                                theContainerList.getID ()));
        theSession.setAttribute ("jahia_session_engineMap", engineMap);

        // sets screen
        engineMap.put ("screen", theScreen);
        if (theScreen.equals ("save")) {
            engineMap.put ("jspSource", "close");
        } else if (theScreen.equals ("apply")) {
            engineMap.put ("jspSource", "apply");
        } else if (theScreen.equals ("cancel")) {
            engineMap.put ("jspSource", "close");
        } else {
            engineMap.put ("jspSource", TEMPLATE_JSP);
        }

        if (theContainerList.getID () != 0) {
            engineMap.put ("enableCategories", Boolean.TRUE);
        }

        engineMap.put ("doAddEditViewRight", Boolean.TRUE);

        // sets engineMap for JSPs
        jParams.setAttribute ("engineTitle", "Container List Settings");
        jParams.setAttribute ("org.jahia.engines.EngineHashMap", engineMap);
        return engineMap;
    }

    private JahiaContainerList fillEngineMap(int cListID,
                                             ProcessingContext jParams,
                                             Map<String, Object> engineMap,
                                             String cursorField) throws JahiaException {
        JahiaContainerList theContainerList = ServicesRegistry.getInstance().getJahiaContainersService().
                loadContainerList(cListID, LoadFlags.NOTHING, jParams);
        List<Integer> allIds = ServicesRegistry.getInstance().getJahiaContainersService()
                .getctnidsInList(cListID,jParams.getEntryLoadRequest());
        int maxContainersToDisplay = 100;
        String paramValue = jParams.getParameter("maxContainersToDisplay");
        if (paramValue!=null && !"".equals(paramValue)){
            try {
                maxContainersToDisplay = Integer.parseInt(paramValue);
            } catch ( NumberFormatException t ){
                maxContainersToDisplay = 100;
            }
        } else {
            Integer sessionValue = (Integer)engineMap.get("maxContainersToDisplay");
            if (sessionValue != null){
                maxContainersToDisplay = sessionValue;
            }
        }
        engineMap.put("maxContainersToDisplay", maxContainersToDisplay);
        if (allIds !=null && !allIds.isEmpty()){
            engineMap.put("nbTotalOfContainers",allIds.size());
            if (allIds.size()>maxContainersToDisplay){
                theContainerList.setMaxSize(maxContainersToDisplay);
            }
        } else {
            engineMap.put("nbTotalOfContainers",0);
        }
        engineMap.put("theContainerList", theContainerList);

        boolean useOptimizedMode = true;

        // print out container list properties
        Properties props = theContainerList.getProperties();
        Enumeration<?> propNameEnum = props.propertyNames();
        while (propNameEnum.hasMoreElements()) {
            final String p = (String) propNameEnum.nextElement();
            if ("automatic_sort_useOptimizedMode".equals(p)){
                useOptimizedMode = !("false".equals(props.getProperty("automatic_sort_useOptimizedMode")));
            }
            logger.debug("Properties : " + props.getProperty(p));
        }
        if (jParams.getParameter("updMode") != null){
            paramValue = jParams.getParameter("useOptimizedMode");
            useOptimizedMode = "true".equals(paramValue);
        }

        engineMap.put("automatic_sort_useOptimizedMode", useOptimizedMode);

        // the ordered list of containers
        List<JahiaContainer> containers = new ArrayList<JahiaContainer>();
        // the info displayed to make the difference between containers
        List<String> fieldInfoToDisplay = new ArrayList<String>();
        int cnt = 0;

        List<JahiaFieldDefinition> fields = null;
        List<ContentDefinition> fieldsAndMetadata = null;

        // no containers in the container list
        // create an empty container facade
        ContentPage contentPage = ContentPage.getPage(theContainerList.getPageID(), false);
        JahiaContentContainerFacade contentContainerFacade =
                new JahiaContentContainerFacade(0,
                                                contentPage.getJahiaID(),
                                                theContainerList.getPageID(),
                                                theContainerList.getID(),
                                                theContainerList.getctndefid(),
                                                //contentPage.getAclID(),
                                                0,
                                                jParams,
                                                jParams.getSite().getLanguageSettingsAsLocales(false));
        if (contentContainerFacade.getContainers().hasNext()) {
            JahiaContainer theContainer = contentContainerFacade.getContainers().next();
            if (theContainer != null) {
                Iterator<JahiaField> fieldsList = theContainer.getFields();
                fields = new ArrayList<JahiaFieldDefinition>();

                while (fieldsList.hasNext()) {
                    JahiaField theField = fieldsList.next();
                    fields.add(theField.getDefinition());
                }
                fieldsAndMetadata = new ArrayList<ContentDefinition>(fields);
                if (theContainerList.getContainers().hasNext()) {
                    theContainer = theContainerList.getContainers().next();
                    if (theContainer.getID() > 0) {
                        for (ContentField contentField : theContainer.getContentContainer().getMetadatas()) {
                            try {
                                fieldsAndMetadata.add(ContentDefinition.getContentDefinitionInstance(
                                        contentField.getDefinitionKey(null)));
                            } catch (ClassNotFoundException e) {
                                logger.warn(e, e);
                            }
                        }
                    }
                }
            }
        }

        String auto = props.getProperty("automatic_sort_handler");

        if (auto != null && !auto.equals("")) {
            String[] autos = auto.split(";");
            if (!engineMap.containsKey("automatic")) {
                engineMap.put("automatic", "desc".equals(autos[1]) ? "alphDesc" : "alphAsc");
            }
            if ("".equals(cursorField) && fieldsAndMetadata != null) {
                for (ContentDefinition aFieldsAndMetadata : fieldsAndMetadata) {
                    JahiaFieldDefinition field = (JahiaFieldDefinition) aFieldsAndMetadata;
                    if (field.getName().equals(autos[0])) {
                        cursorField = getDefinitionName(field);
                        engineMap.put("cursorField", cursorField);
                        break;
                    }
                }
            }
        }

        final Iterator<JahiaContainer> containerList = theContainerList.getContainers();
        while (containerList.hasNext()) {
            cnt++;
            JahiaContainer theContainer = containerList.next();
            // let's order the container fields in the exact order in which they are decrared in the template
            // ( not as in the order in which they are stored in db )
            theContainer.setLanguageCode(jParams.getEntryLoadRequest().getFirstLocale(true).toString());
            theContainer.fieldsStructureCheck(jParams);
            containers.add(theContainer);

            String fieldInfo = getFieldInfoToDisplay(cursorField, theContainer, jParams, engineMap);
            if (fieldInfo != null) {
                if (fieldInfo.equals(EMPTY_STRING)) {
                    fieldInfo = "-- Item " + cnt + " --";
                }
            } else {
                fieldInfo = "-- Item " + cnt + " --";
            }
            fieldInfoToDisplay.add(fieldInfo);
            getSortInfo4Date("", theContainer, engineMap);
        }

        engineMap.put("containers", containers);
        engineMap.put("fields_metadata", fieldsAndMetadata);
        engineMap.put("fields_nometadata", fields);
        engineMap.put("fieldInfoToDisplay", fieldInfoToDisplay);

        // build container list field acl
        Map<String, Integer> fieldAcls = buildFieldDefAcls(theContainerList, jParams,
                                              engineMap);
        engineMap.put("fieldAcls", fieldAcls);
        return theContainerList;
    }

    private String getDefinitionName(JahiaFieldDefinition field) {
        if( field.getIsMetadata()) return "metadata_"+field.getName();
        else return field.getName();
    }

    /**
     * loads container list data for the JSP file
     *
     * @param jParams   a ProcessingContext object (with request and response)
     * @param engineMap then engine map, to be forwarded to the JSP file
     */

//    private void loadContainerListData (ProcessingContext jParams, Map engineMap) {
//        // do nothin'... for now
//    }

    /**
     * gets container list POST form data from the JSP file
     * <p/>
     * The List "containers" contains all the containers of the container list. The List
     * "fieldInfoToDisplay" contains the fields values on which the sort is done. a value in
     * "fieldInfoToDisplay" correspond to the container at the same index into "containers".
     *
     * @param jParams   a ProcessingContext object (with request and response)
     * @param engineMap then engine map, to be forwarded to the JSP file
     * @return true if everything went okay, false if not
     */
    private boolean updateContainerListData (ProcessingContext jParams,
                                             Map<String, Object> engineMap) throws JahiaException {
        boolean okay = true;
        //FIXME: set JahiaExceptions

        //JahiaContainerList theContainerList = (JahiaContainerList)engineMap.get( "theContainerList" );

        String updMode = jParams.getParameter ("updMode");
        List<JahiaContainer> containers = (List<JahiaContainer>) engineMap.get ("containers");
        if (updMode != null) {
            // **** change cursor field ****
            if (jParams.getParameter ("cursorField")!= null) {
                String curFieldStr = jParams.getParameter ("cursorField");

                List<String> fieldInfoToDisplay = new ArrayList<String>();
                List<String> sortInfo4Date = new ArrayList<String>();
                int i = 0;
                for (final JahiaContainer theContainer : containers) {
                    String fieldInfo = getFieldInfoToDisplay (curFieldStr, theContainer, jParams, engineMap);
                    if (fieldInfo.equals (EMPTY_STRING)) {
                        fieldInfo = "-- Item " + (i + 1) + " --";
                    }
                    fieldInfoToDisplay.add (fieldInfo);

                    final String sortInfo = getSortInfo4Date (curFieldStr, theContainer, engineMap);
                    if (sortInfo != null) {
                        sortInfo4Date.add (sortInfo);
                    }
                    i++;
                }

                engineMap.put ("fieldInfoToDisplay", fieldInfoToDisplay);
                engineMap.put ("sortInfo4Date", sortInfo4Date);
                engineMap.put ("cursorField", curFieldStr);

            }
            if (jParams.getParameter ("useOptimizedMode")!= null){
                engineMap.put("automatic_sort_useOptimizedMode",Boolean.TRUE);
            } else {
                engineMap.put("automatic_sort_useOptimizedMode",Boolean.FALSE);
            }

            if (jParams.getParameter ("maxContainersToDisplay")!= null){
                JahiaContainerList cList = (JahiaContainerList)engineMap.get("theContainerList");
                fillEngineMap(cList.getID(), jParams, engineMap,(String) engineMap.get("cursorField"));
            }

            // ++++ manual ranking ++++
            /*
                         else if (updMode.equals("manual"))
                         {
                 String containerNumberStr = jParams.getRequest().getParameter("manRank");
                int containerNumber = 0;
                if (containerNumberStr != null)
                {
                    try {
                        containerNumber = Integer.parseInt(containerNumberStr);
                    } catch (NumberFormatException nfe) {
                        ;
                    }
                }
                String move = jParams.getRequest().getParameter("move");
                JahiaContainer c = null;
                String str = EMPTY_STRING;
                if (move.equals("top"))
                {
                    c = (JahiaContainer)containers.remove(containerNumber);
                    containers.add(0,c);
                    str = (String)fieldInfoToDisplay.remove(containerNumber);
                    fieldInfoToDisplay.add(0,str);
                    engineMap.put("containerSelected", new Integer(0));
                }
                else if (move.equals("up"))
                {
                    if (containerNumber >0)
                    {
                        c = (JahiaContainer)containers.get(containerNumber);
                 containers.set(containerNumber,containers.get(containerNumber-1));
                        containers.set(containerNumber-1,c);
                        str = (String)fieldInfoToDisplay.get(containerNumber);
                        fieldInfoToDisplay.set(containerNumber,fieldInfoToDisplay.get(containerNumber-1));
                        fieldInfoToDisplay.set(containerNumber-1,str);
                 engineMap.put("containerSelected", new Integer(containerNumber-1));
                    }
                }
                else if (move.equals("down"))
                {
                    if (containerNumber < containers.size()-1)
                    {
                        c = (JahiaContainer)containers.get(containerNumber);
                 containers.set(containerNumber,containers.get(containerNumber+1));
                        containers.set(containerNumber+1,c);
                        str = (String)fieldInfoToDisplay.get(containerNumber);
                        fieldInfoToDisplay.set(containerNumber,fieldInfoToDisplay.get(containerNumber+1));
                        fieldInfoToDisplay.set(containerNumber+1,str);
                 engineMap.put("containerSelected", new Integer(containerNumber+1));
                    }
                }
                else // bottom
                {
                    c = (JahiaContainer)containers.remove(containerNumber);
                    containers.add(c);
                    str = (String)fieldInfoToDisplay.remove(containerNumber);
                    fieldInfoToDisplay.add(str);
                 engineMap.put("containerSelected", new Integer(containers.size()-1));
                }
                engineMap.put("fieldInfoToDisplay", fieldInfoToDisplay);
                engineMap.put("containers", containers);
                         }
                         // ++++ end manual ranking ++++
             */

            // **** automatic ranking ****
            if (updMode.equals ("automatic")) {
                engineMap.put ("containerSelected", 0);
                String containerSort = jParams.getParameter ("autRank");
                engineMap.put ("automatic", containerSort);
                shell_sort (engineMap, containerSort);
            }
            if(engineMap.get("automatic")!=null){
                shell_sort (engineMap, (String) engineMap.get("automatic"));
            }
            // ****end automatic ranking ****
        }

        return okay;
    }

    /**
     * save the rank of each container of the container list
     *
     * @param jParams   a ProcessingContext object (with request and response)
     * @param engineMap then engine map, to be forwarded to the JSP file
     */
    private void saveContainersRank (ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {
        String containerSort = jParams.getParameter("autRank");
        String curFieldStr = jParams.getParameter("cursorField");
        Boolean useOptimizedMode = (Boolean)engineMap.get("automatic_sort_useOptimizedMode");

        if (containerSort != null && !"none".equalsIgnoreCase(containerSort)) {
            JahiaContainerList theContainerList = (JahiaContainerList) engineMap.get("theContainerList");
            Iterator<JahiaContainer> containers = theContainerList.getContainers();
            String fieldName = "";
            int fieldType = 0;
            boolean isMetaDataField = false;
            JahiaContainer jahiaContainer = containers.next();
            Iterator<JahiaField> fList = jahiaContainer.getFields();
            boolean ignoreOptimizedMode = false;
            while (fList.hasNext()) {
                JahiaField f = fList.next();
                if (f != null && curFieldStr.equals(f.getDefinition().getName())) {
                    JahiaFieldDefinition definition = f.getDefinition();
                    ignoreOptimizedMode = (definition.getType() == FieldTypes.PAGE ||
                        definition.getType() == FieldTypes.BIGTEXT ||
                        definition.getType() == FieldTypes.FILE);
                    fieldName = definition.getName();
                    fieldType = definition.getType();
                    break;
                }
            }
            if (null == fieldName || "".equals(fieldName)) {
                isMetaDataField = true;
                for (ContentField contentField : jahiaContainer.getContentContainer().getMetadatas()) {
                    if (contentField != null) {
                        JahiaFieldDefinition definition = contentField.getJahiaField(jParams.getEntryLoadRequest()).getDefinition();
                        if(curFieldStr.equals("metadata_"+definition.getName())) {
                            fieldName = definition.getName();
                            fieldType = definition.getType();
                            break;
                        }
                    }
                }
            }
            StringBuffer buff = new StringBuffer(fieldName);
            buff.append(";").append((containerSort.indexOf("Desc") > 0) ? "desc" : "asc");
            switch (fieldType) {
                case ContentFieldTypes.DATE :
                case ContentFieldTypes.INTEGER :
                case ContentFieldTypes.FLOAT :
                    buff.append(";true");
                    break;
                default:
                    buff.append(";false");
            }
            buff.append(";").append(isMetaDataField);
            theContainerList.setProperty("automatic_sort_ignoreOptimizedMode", String.valueOf(ignoreOptimizedMode));
            theContainerList.setProperty("automatic_sort_handler", buff.toString());
            theContainerList.setProperty("automatic_sort_useOptimizedMode", useOptimizedMode.toString());

        } else {
            List<JahiaContainer> containers_beforeSort = (List<JahiaContainer>) engineMap.get("containers");
            List<JahiaContainer> containersV = new ArrayList<JahiaContainer>(containers_beforeSort.size());

            String[] manRank = jParams.getParameterValues("manRank");
            if (manRank != null) {
                for (String aManRank : manRank) {
                    final int num = Integer.parseInt(aManRank);
                    //System.out.println("####### Debug save CList: "+num);
                    containersV.add(containers_beforeSort.get(num));
                }
                int counter = containersV.size();
                JahiaContainer c;
                JahiaContainersService jahiaContainersService = ServicesRegistry.getInstance().getJahiaContainersService();
                // rank are save with negative number, because new containers have a rank equal to 0.
                // then new containers will appear at the end of the container list.
                for (JahiaContainer aContainersV : containersV) {
                    c = (JahiaContainer) aContainersV;
                    c.setRank(counter * (-1));
                    jahiaContainersService.saveContainerRankInfo(c, jParams);
                    counter--;
                }
                JahiaContainerList theContainerList = (JahiaContainerList) engineMap.get("theContainerList");
                theContainerList.setProperty("automatic_sort_handler", "");
                theContainerList.setProperty("automatic_sort_useOptimizedMode", useOptimizedMode.toString());
                fillEngineMap(theContainerList.getID(), jParams, engineMap, curFieldStr);
            }
        }
    }

    /**
     * get the fieldInfo corresponding to the field "cursorField" of the container
     *
     * @param cursorField  the number of the field into the container
     * @param theContainer the container in which get the fieldInfo
     * @return The field value in display format
     * @param jParams PrcoessingContext
     * @param engineMap Map
     * @throws org.jahia.exceptions.JahiaException When an exception occured
     */
    private String getFieldInfoToDisplay (String cursorField,
                                          JahiaContainer theContainer,
                                          ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {
        String fieldInfo = null;
        logger.debug(cursorField);
        EngineLanguageHelper elh = (EngineLanguageHelper)engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);

        Iterator<JahiaField> fList = theContainer.getFields();
        while (fList.hasNext ()) {
            JahiaField f = fList.next ();
            JahiaField aField =ServicesRegistry.getInstance().
                    getJahiaFieldService ().loadField (f.getID (), LoadFlags.ALL,
                    jParams, elh.getCurrentEntryLoadRequest(), theContainer.getListID());
            if(aField != null) {
                f = aField;
            }
            if ("".equals(cursorField) || (f.getDefinition() != null && f.getDefinition().getName().equals(cursorField))) {
                if (f.getType() == ContentFieldTypes.DATE)
                    fieldInfo = "isDate;" + f.getFieldContent4Ranking();
                else
                    fieldInfo = f.getFieldContent4Ranking();
                break;
            }
        }

        if (null == fieldInfo) {
            for (ContentField contentField : theContainer.getContentContainer().getMetadatas()) {
                logger.debug("definition = " + contentField.getDefinitionID(elh.getCurrentEntryLoadRequest()));
                JahiaField jahiaField = contentField.getJahiaField(elh.getCurrentEntryLoadRequest());
                if (jahiaField != null && cursorField.equals("metadata_" + jahiaField.getDefinition().getName())) {
                    if (jahiaField.getType() == ContentFieldTypes.DATE)
                        fieldInfo = "isDate;" + jahiaField.getFieldContent4Ranking();
                    else if (jahiaField.getType() == ContentFieldTypes.BIGTEXT) {
                        jahiaField.load(0, jParams, elh.getCurrentEntryLoadRequest());
                        fieldInfo = jahiaField.getFieldContent4Ranking();
                    } else
                        fieldInfo = jahiaField.getFieldContent4Ranking();
                    break;
                }
            }
        }
        logger.debug("return "+fieldInfo);
        return fieldInfo!=null?fieldInfo:EMPTY_STRING;
    }

    /**
     * get the date in millisecond
     *
     * @param cursorField  the number of the field into the container
     * @param theContainer the container in which get the fieldInfo
     */
    private String getSortInfo4Date (String cursorField,
                                     JahiaContainer theContainer,
                                     Map<String, Object> engineMap)
            throws JahiaException {
        String sortInfo = null;

        EngineLanguageHelper elh = (EngineLanguageHelper)engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);

        Iterator<JahiaField> fList = theContainer.getFields ();
        while (fList.hasNext ()) {
            JahiaField f = fList.next ();
            //f = ServicesRegistry.getInstance().
            //getJahiaFieldService().loadField( f.getID(), LoadFlags.ALL, jParams );

            if ("".equals(cursorField) || cursorField.equals(f.getDefinition().getName())) {
                if (f.getType () == 4) {
                    sortInfo = (String) f.getObject ();
                }
            }
        }
        if (null == sortInfo) {
            for (ContentField contentField : theContainer.getContentContainer().getMetadatas()) {
                if (contentField != null) {
                    JahiaField jahiaField = contentField.getJahiaField(elh.getCurrentEntryLoadRequest());
                    if (jahiaField != null && cursorField.equals("metadata_"+jahiaField.getDefinition().getName())) {
                        if(jahiaField.getType()==ContentFieldTypes.DATE) {
                            sortInfo = (String) jahiaField.getObject();
                            break;
                        }
                    }
                }
            }
        }
        return sortInfo;
    }

    /**
     * sort the containers and the fieldInfoToDisplay List
     * <p/>
     * The List "containers" contains all the containers of the container list. The List
     * "fieldInfoToDisplay" contains the fields values on which the sort is done. a value in
     * "fieldInfoToDisplay" correspond to the container at the same index into "containers".
     *
     * @param engineMap     then engine map, to be forwarded to the JSP file
     * @param containerSort How to sort the containers (alphabetically or order of insertion,
     *                      ascending or descending)
     */
    private void shell_sort (Map<String, Object> engineMap, String containerSort) {
        List<JahiaContainer> containers = (List<JahiaContainer>) engineMap.get ("containers");
        List<String> fieldInfoToDisplay = (List<String>) engineMap.get ("fieldInfoToDisplay");
        List<String> sortInfo4Date = (List<String>) engineMap.get ("sortInfo4Date");

        int n = containers.size ();
        int incr;
        int j;
        boolean flag = false;
        boolean isDate = sortInfo4Date != null && !sortInfo4Date.isEmpty();
        final Comparator<String> comparator = new NumericStringComparator<String>();
        for (incr = n / 2; incr > 0; incr = incr / 2) {
            for (int i = incr; i < n; i++) {
                final JahiaContainer c = containers.get(i);
                final String str = fieldInfoToDisplay.get(i);
                String str2 = EMPTY_STRING;
                String str4Test = str;
                String str4Test2;
                if (isDate) {
                    str2 = sortInfo4Date.get(i);
                    str4Test = str2;
                }

                for (j = i; j >= incr; j = j - incr) {
                    if (containerSort.equals("insertAsc")) {
                        flag = c.getID() < containers.get(j - incr).getID();
                    } else if (containerSort.equals("insertDesc")) {
                        flag = c.getID() >= containers.get(j - incr).getID();
                    } else if (containerSort.equals("alphAsc")) {
                        str4Test2 = fieldInfoToDisplay.get(j - incr);
                        if (isDate) {
                            str4Test2 = sortInfo4Date.get(j - incr);
                        }
                        try {
                            str4Test = (new RE("<(.*?)>")).subst(str4Test, EMPTY_STRING);
                            str4Test2 = (new RE("<(.*?)>")).subst(str4Test2, EMPTY_STRING);
                        } catch (RESyntaxException re) {
                            logger.debug(re);
                        } catch (Exception t) {
                            logger.debug(t);
                        }
                        int comp = comparator.compare(str4Test,str4Test2);
                        flag = comp < 0;
                    } else if (containerSort.equals("alphDesc")) {
                        str4Test2 = fieldInfoToDisplay.get(j - incr);
                        if (isDate) {
                            str4Test2 = sortInfo4Date.get(j - incr);
                        }
                        try {
                            str4Test = (new RE("<(.*?)>")).subst(str4Test, EMPTY_STRING);
                            str4Test2 = (new RE("<(.*?)>")).subst(str4Test2, EMPTY_STRING);
                        } catch (RESyntaxException re) {
                            logger.debug(re);
                        } catch (Exception t) {
                            logger.debug(t);
                        }
                        int comp = comparator.compare(str4Test,str4Test2);
                        flag = comp >= 0;
                    }

                    if (flag) {

                        containers.set(j, containers.get(j - incr));
                        fieldInfoToDisplay.set(j,
                                fieldInfoToDisplay.get(j - incr));
                        if (isDate) {
                            sortInfo4Date.set(j, sortInfo4Date.get(j - incr));
                        }
                    } else {
                        break;
                    }
                }
                containers.set(j, c);
                fieldInfoToDisplay.set(j, str);
                if (isDate) {
                    sortInfo4Date.set(j, str2);
                }
            }
        }
        engineMap.put("fieldInfoToDisplay", fieldInfoToDisplay);
        engineMap.put("sortInfo4Date", sortInfo4Date);
        engineMap.put("containers", containers);

    }

    // Khue Nguyen
    /**
     * composes the hashmap that contains the pair (String fieldDefName, Integer aclID)
     *
     * @param cList     the container list
     * @param jParams   the parambean that contains reference to the page
     * @param engineMap needed to store the first field def name as "fieldAclDefaultField"
     *                  attribute
     *
     * @return HashMap
     */
    private Map<String, Integer> buildFieldDefAcls (JahiaContainerList cList,
                                       ProcessingContext jParams,
                                       Map<String, Object> engineMap) throws JahiaException {
        Map<String, Integer> hash = new HashMap<String, Integer>();

        if (cList == null || jParams.getPage () == null)
            return hash;

        boolean done = false;

        final Iterator<JahiaContainerStructure> fList = cList.getDefinition().getStructure(JahiaContainerStructure.JAHIA_FIELD);
        while (fList.hasNext ()) {
            final JahiaContainerStructure aStructure = fList.next ();
            final JahiaFieldDefinition theDef = (JahiaFieldDefinition)aStructure.getObjectDef();
            if (theDef != null) {
                final String prop = cList.getContentContainerList().getProperty ("view_field_acl_" + theDef.getName ());
                if (prop != null) {
                    try {
                        int aclID = Integer.parseInt (prop);
                        hash.put (theDef.getName (), aclID);
                        if (!done) {
                            engineMap.put ("fieldAclDefaultField",
                                    theDef.getName ());
                            done = true;
                        }
                    } catch (Exception t) {
                        logger.debug (" requested acl ( view_field_acl_" +
                                theDef.getName () + "), fail ");
                    }
                } else {
                    ContentContainerList contentContainerList = ContentContainerList.getContainerList(cList.getID());
                    // create the acl, because field declaration can change in template
                    cList.getContentContainerList().setProperty ("view_field_acl_" + theDef.getName (), String.valueOf (contentContainerList.getAclID()));

                    hash.put (theDef.getName (), contentContainerList.getAclID());
                    if (!done) {
                        engineMap.put ("fieldAclDefaultField", theDef.getName ());
                        done = true;
                    }
                }
            }
        }

        logger.debug ("Returned hashMap");
        return hash;
    }
}
