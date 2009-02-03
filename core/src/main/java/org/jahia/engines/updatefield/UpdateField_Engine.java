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

//  EV  10.01.20001

package org.jahia.engines.updatefield;

import org.jahia.content.ObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.JahiaContentFieldFacade;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.metadata.Metadata_Engine;
import org.jahia.engines.rights.ManageRights;
import org.jahia.engines.shared.Page_Field;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.engines.workflow.ManageWorkflow;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.fields.ContentField;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockService;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntrySaveRequest;
import org.jahia.utils.JahiaObjectTool;
import org.jahia.utils.LanguageCodeConverters;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

public class UpdateField_Engine implements JahiaEngine {

    /** logging */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (UpdateField_Engine.class);

    private static final String TEMPLATE_JSP = "update_field";
    public static final String ENGINE_NAME = "updatefield";
    private EngineToolBox toolBox;
    private TransactionTemplate transactionTemplate = null;

    /**
     * Default constructor, creates a new <code>UpdateField_Engine</code> instance.
     */
    public UpdateField_Engine () {
        toolBox = EngineToolBox.getInstance ();
    }

    /**
     * authoriseRender AK    19.12.2000
     */
    public boolean authoriseRender (ProcessingContext jParams) {
        return toolBox.authoriseRender (jParams);
    } // end authoriseRender

    /**
     * renderLink AK    19.12.2000 AK    04.01.2001  add the select parameter MJ    21.03.2001
     * mode is now the first URL parameter
     */
    public String renderLink (ProcessingContext jParams, Object theObj)
            throws JahiaException {
        ContentField contentField = (ContentField) theObj;
        String params = "?mode=display&fid=" + contentField.getID ();
        return jParams.composeEngineUrl (ENGINE_NAME, params);
    } // end renderLink

    /**
     * needsJahiaData AK    19.12.2000
     */
    public boolean needsJahiaData (ProcessingContext jParams) {
        return false;
    } // end needsJahiaData

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
        JahiaField theField = (JahiaField) engineMap.get (ENGINE_NAME + ".theField");
        final JahiaUser user = jParams.getUser();

        // does the current user have permission for the current engine ?
        if (ServicesRegistry.getInstance().getJahiaACLManagerService().
                getSiteActionPermission("engines.actions.update",
                        jParams.getUser(), JahiaBaseACL.READ_RIGHTS,
                        jParams.getSiteID()) <=0 ) {
            throw new JahiaForbiddenAccessException();
        }

        if (theField.checkAdminAccess (user)) {
            engineMap.put("enableAuthoring", Boolean.TRUE);
            engineMap.put ("adminAccess", Boolean.TRUE);
            engineMap.put ("enableRightView", Boolean.TRUE);
            engineMap.put ("enableAdvancedWorkflow", Boolean.TRUE);
            engineMap.put ("writeAccess", Boolean.TRUE);
        } else if (theField.checkWriteAccess (user)) {
            engineMap.put("enableAuthoring", Boolean.TRUE);
            engineMap.put ("writeAccess", Boolean.TRUE);
        }

        if (engineMap.get ("writeAccess") != null) {

            // #ifdef LOCK
            final LockService lockRegistry = ServicesRegistry.getInstance ().getLockService ();
            if (jParams.settings ().areLocksActivated ()) {
                final LockKey lockKey = LockKey.composeLockKey (LockKey.UPDATE_FIELD_TYPE, theField.getID ());
                if (lockRegistry.acquire (lockKey, user,
                        user.getUserKey(),
                        jParams.getSessionState ().
                        getMaxInactiveInterval ())) {

                    processLastScreen (jParams, engineMap);
                    processCurrentScreen (jParams, engineMap);

                    // #ifdef LOCK
                } else {
                    final Map m = lockRegistry.getLocksOnObject(lockKey);
                    if (! m.isEmpty()) {
                        final String action = (String) m.keySet().iterator().next();
                        engineMap.put("LockKey", LockKey.composeLockKey(lockKey.getObjectKey(), action));
                    } else {
                        final LockPrerequisitesResult results = LockPrerequisites.getInstance().
                                getLockPrerequisitesResult(lockKey);
                        engineMap.put("LockKey", results.getFirstLockKey());
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

    } // end handleActions

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
    public void processLastScreen (ProcessingContext jParams, Map engineMap)
            throws JahiaException,
            JahiaForbiddenAccessException {
        logger.debug ("started");
        EngineLanguageHelper elh = (EngineLanguageHelper)engineMap
                                   .get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        JahiaContentFieldFacade jahiaContentFieldFacade
                = (JahiaContentFieldFacade) engineMap.get ("jahiaContentFieldFacade");

        JahiaField theField = jahiaContentFieldFacade.getField (
                elh.getPreviousEntryLoadRequest(), true);

        // handle undefined field
        int initialFieldType = theField.getType ();

        engineMap.put (JahiaEngine.PROCESSING_LOCALE,
                elh.getPreviousLocale());
        engineMap.put (ENGINE_NAME +"."+"theField", theField);

        // gets the last screen
        // lastscreen   = edit, rights, logs
        String lastScreen = jParams.getParameter ("lastscreen");
        if (lastScreen == null) {
            //lastScreen = "edit";
            lastScreen = EMPTY_STRING;
        }

        logger.debug ("lastscreen=" + lastScreen);

        // indicates to sub engines that we are processing last screen
        int mode = JahiaEngine.UPDATE_MODE;

        // dispatches to the appropriate sub engine
        if (lastScreen.equals ("edit")) {
            if (!toolBox.processFieldTypes (theField, null, ENGINE_NAME, jParams, mode, engineMap)) {
                // if there was an error, come back to last screen
                engineMap.put ("screen", lastScreen);
                engineMap.put ("jspSource", TEMPLATE_JSP);
            } else {
                if (jParams.getParameter (
                        "apply_change_to_all_lang_" + theField.getID ()) != null) {
                    applyChangeToAllLang (theField, jahiaContentFieldFacade,
                            engineMap, jParams);
                }
            }
        } else if (lastScreen.equals ("rightsMgmt")) {
            if (engineMap.get ("adminAccess") != null) {
                ManageRights.getInstance ().handleActions (jParams, mode,
                        engineMap, theField.getAclID (), null, null);
            } else {
                throw new JahiaForbiddenAccessException ();
            }
        } else if (lastScreen.equals ("logs")) {
            if (engineMap.get ("adminAccess") != null) {
                // ManageLogs_Engine.getInstance().handleActions( jParams, null );
            } else {
                throw new JahiaForbiddenAccessException ();
            }
        } else if (lastScreen.equals("workflow")) {
            final boolean isReadOnly = LockPrerequisites.getInstance().
                    getLockPrerequisitesResult((LockKey) engineMap.get("LockKey")) != null;
            if (engineMap.get("adminAccess") != null || isReadOnly) {
                ManageWorkflow.getInstance().handleActions(jParams, mode,
                        engineMap, theField.getContentField());
            } else {
                throw new JahiaForbiddenAccessException();
            }
        } else if (lastScreen.equals("metadata")) {
            ObjectKey objectKey = ContentField.getField(theField.getID()).getObjectKey();
            Metadata_Engine.getInstance().handleActions(jParams, mode, objectKey);
        } else if (lastScreen.equals ("versioning")) {
            if (engineMap.get ("adminAccess") != null) {

                /**
                 * todo forward to versioning URL
                 *
                 */

                /**
                 VersioningEngine.getInstance()
                 .handleAction( jParams, mode, engineMap, JahiaObjectTool.FIELD_TYPE, theField );
                 */
            } else {
                throw new JahiaForbiddenAccessException ();
            }
        }
        theField = (JahiaField) engineMap.get (ENGINE_NAME + "." + "theField");
        if (initialFieldType != theField.getType ()) {
            // init the JahiaContentFieldFacade
            List localeList = new ArrayList();
            List siteLanguageSettings = jParams.getSite ().getLanguageSettings ();
            if (siteLanguageSettings != null) {
                for (int i = 0; i < siteLanguageSettings.size (); i++) {
                    SiteLanguageSettings curSetting = (SiteLanguageSettings)
                            siteLanguageSettings.get(i);
                    if (curSetting.isActivated ()) {
                        Locale tempLocale = LanguageCodeConverters.
                                languageCodeToLocale (curSetting.
                                getCode ());
                        localeList.add (tempLocale);
                    }
                }
            }

            jahiaContentFieldFacade =
                    new JahiaContentFieldFacade (theField.getID (),
                            theField.getJahiaID (),
                            theField.getPageID (),
                            theField.getctnid (),
                            theField.getFieldDefID (),
                            theField.getType (),
                            theField.getConnectType (),
                            theField.getValue (),
                            theField.getAclID (),
                            jParams,
                            localeList);

            engineMap.put ("jahiaContentFieldFacade", jahiaContentFieldFacade);

        }
    } // end processLastScreen

    /**
     * prepares the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public void processCurrentScreen (final ProcessingContext jParams, final Map engineMap)
            throws JahiaException,
            JahiaForbiddenAccessException {
        logger.debug ("started");
        EngineLanguageHelper elh = (EngineLanguageHelper)engineMap
                                   .get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        final JahiaContentFieldFacade jahiaContentFieldFacade
                = (JahiaContentFieldFacade) engineMap.get ("jahiaContentFieldFacade");

        engineMap.put (JahiaEngine.PROCESSING_LOCALE,
                elh.getCurrentLocale());

        final JahiaField theField = jahiaContentFieldFacade.getField (elh.getCurrentEntryLoadRequest(), true);
        engineMap.put (ENGINE_NAME + "." +"theField", theField);

        // gets the current screen
        // screen   = edit, rights, logs
        final String theScreen = (String) engineMap.get ("screen");
        //JahiaField theField = (JahiaField)  engineMap.get( "theField" );

        logger.debug ("screen=" + theScreen);

        final Set updatedLanguageEntries = (Set) engineMap.get (
                ENGINE_NAME + "." +"updatedLanguageEntries");

        final Set updatedFields = (Set) engineMap.get (ENGINE_NAME + "." +"updated.fields");

        // indicates to sub engines that we are processing last screen
        int mode = JahiaEngine.LOAD_MODE;

        // #ifdef LOCK
        final LockKey lockKey = LockKey.composeLockKey (LockKey.UPDATE_FIELD_TYPE, theField.getID ());
        final LockService lockRegistry = ServicesRegistry.getInstance ().
                getLockService ();
        // #endif

        // dispatches to the appropriate sub engine
        final JahiaUser user = jParams.getUser();
        if (theScreen.equals ("edit")) {
            if (toolBox.processFieldTypes (theField, null, ENGINE_NAME, jParams, mode, engineMap)) {
                if (jParams.getParameter (
                        "apply_change_to_all_lang_" + theField.getID ()) != null) {
                    applyChangeToAllLang (theField, jahiaContentFieldFacade,
                                          engineMap, jParams);
                }
            }
        } else if (theScreen.equals ("logs")) {
            toolBox.loadLogData (jParams, JahiaObjectTool.FIELD_TYPE, engineMap);
        } else if (theScreen.equals ("rightsMgmt")) {
            if (engineMap.get ("adminAccess") != null) {
                ManageRights.getInstance ().handleActions (jParams, mode,
                                                           engineMap, theField.getAclID (), null, null);
            } else {
                throw new JahiaForbiddenAccessException ();
            }
        } else if (theScreen.equals ("versioning")) {
            if (engineMap.get ("adminAccess") != null) {
                /**
                 VersioningEngine.getInstance()
                 .handleAction( jParams, mode,engineMap, JahiaObjectTool.FIELD_TYPE, theField );
                 */
                /**
                 * Todo forward to Versioning engine
                 */
            } else {
                throw new JahiaForbiddenAccessException ();
            }
        } else if (theScreen.equals("workflow")) {
            if (engineMap.get("adminAccess") != null) {
                ManageWorkflow.getInstance().handleActions(jParams, mode,
                                                           engineMap, theField.getContentField());
            } else {
                throw new JahiaForbiddenAccessException();
            }
        } else if (theScreen.equals("metadata")) {
            ObjectKey objectKey = ContentField.getField(theField.getID()).getObjectKey();
            Metadata_Engine.getInstance().handleActions(jParams, mode, objectKey);
        } else if (theScreen.equals ("save") || theScreen.equals ("apply")) {

            // #ifdef LOCK
            // Did somebody steal the lock ? Panpan cucul !
            if (jParams.settings ().areLocksActivated () &&
                lockRegistry.isStealedInContext (lockKey, user,user.getUserKey())) {
                engineMap.put ("screen",
                               jParams.getParameter ("lastscreen"));
                engineMap.put ("jspSource", "apply");
                return;
            }
            // #endif

            mode = JahiaEngine.SAVE_MODE;
            if (transactionTemplate == null) {
                SpringContextSingleton instance = SpringContextSingleton.getInstance();
                if (instance.isInitialized()) {
                    PlatformTransactionManager manager = (PlatformTransactionManager) instance.getContext().getBean("transactionManager");
                    transactionTemplate = new TransactionTemplate(manager);
                }
            }
            try {
                final int mode1 = mode;
//                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//                    protected void doInTransactionWithoutResult(TransactionStatus status) {
//                        try {
                            // save workflow
                            if(!ManageWorkflow.getInstance().handleActions(jParams, mode1,
                                                                       engineMap, theField.getContentField())) {
                                engineMap.put("screen", "workflow");
                                engineMap.put("jspSource", TEMPLATE_JSP);
                                return;
                            }
                            // handle field type change
                            if (theField.getID() > 0) {
                                ContentField contentField = ContentField.getField(theField.
                                        getID());
                                if (theField.getType() != contentField.getType()) {
                                    EntrySaveRequest entrySaveRequest = new EntrySaveRequest(
                                            user, theField.getLanguageCode());
                                    contentField = contentField.changeType(theField.getType(),
                                                                           theField.getValue(), jParams, entrySaveRequest);
                                }
                            }

                            Iterator fields = jahiaContentFieldFacade.getFields();
                            while (fields.hasNext()) {
                                JahiaField field = (JahiaField) fields.next();
                                if (field.hasChanged() &&
                                    (field.getLanguageCode().equals(ContentField.
                                            SHARED_LANGUAGE)
                                     || updatedLanguageEntries.contains(field.getLanguageCode()))
                                    || updatedFields.contains(new Integer(field.getID()))) {
                                    // save the active entry only if the staging doesn't exists.
                                    boolean processField = true;
                                    if (field.getWorkflowState() ==
                                        ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
                                        List entryLocales = new ArrayList();
                                        entryLocales.add(LanguageCodeConverters
                                                .languageCodeToLocale(field.
                                                getLanguageCode()));
                                        EntryLoadRequest stagingLoadRequest =
                                                new EntryLoadRequest(ContentObjectEntryState.
                                                        WORKFLOW_STATE_START_STAGING,
                                                                     0, entryLocales);

                                        processField = (jahiaContentFieldFacade.getField(
                                                stagingLoadRequest, false) == null);
                                    }
                                    if (processField) {
                                        engineMap.put(ENGINE_NAME + "." + "theField", field);
                                        toolBox.processFieldTypes(theField, null, ENGINE_NAME, jParams, mode1, engineMap);
                                    }
                                }
                            }
                            if (engineMap.get("adminAccess") != null) {
                                engineMap.put("logObjectType",
                                              Integer.toString(JahiaObjectTool.FIELD_TYPE));
                                engineMap.put("logObject", theField);
                                //ViewRights.getInstance().handleActions( jParams, mode, engineMap, theField.getAclID() );
                                ManageRights.getInstance().handleActions(jParams, mode1,
                                                                         engineMap, theField.getAclID(), null, null);
                            }

                            // save metadata
                            ObjectKey objectKey = ContentField.getField(theField.getID()).getObjectKey();
                            Metadata_Engine.getInstance().handleActions(jParams, mode1, objectKey);

                            JahiaEvent theEvent = new JahiaEvent(this, jParams, theField);
                            ServicesRegistry.getInstance().getJahiaEventService().
                                    fireUpdateField(theEvent);

                            logger.debug("Changes applied and saved !");
//                        } catch (Exception e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                });
            } catch (Exception e) {
                logger.error("Error during update operation of an element we must flush all caches to ensure integrity between database and viewing");
                ServicesRegistry.getInstance().getCacheService().flushAllCaches();
                throw new JahiaException(e.getMessage(), e.getMessage(),
                                         JahiaException.DATABASE_ERROR, JahiaException.CRITICAL_SEVERITY, e);
            } finally{
                if (theScreen.equals("apply")) {
                    engineMap.put("prevScreenIsApply", Boolean.TRUE);
                    String lastScreen =
                            jParams.getParameter("lastscreen");
                    engineMap.put("screen", lastScreen);
                }
                // #ifdef LOCK
                else {
                    if (jParams.settings().areLocksActivated()) {
                        lockRegistry.release(lockKey, user,user.getUserKey());
                    }
                }
                // #endif
            }
        }
        else if (theScreen.equals ("cancel")) {
            if (jParams.settings ().areLocksActivated ()) {
                lockRegistry.release (lockKey, user,user.getUserKey());
                ManageWorkflow.getInstance().handleActions(jParams, CANCEL_MODE, engineMap, theField.getContentField());
            }
        }
    }

    /**
     * inits the engine map
     *
     * @param jParams a ProcessingContext object (with request and response)
     *
     * @return a Map object containing all the basic values needed by an engine
     */
    private Map initEngineMap (ProcessingContext jParams)
            throws JahiaException,
            JahiaSessionExpirationException {
        logger.debug ("started");

        Map engineMap;
        JahiaField theField;
        Locale previousLocale = null;
        EngineLanguageHelper elh;

        // gets session values
        SessionState theSession = jParams.getSessionState ();

        engineMap = (Map) theSession.getAttribute (
                    "jahia_session_engineMap");

        // tries to find if this is the first screen generated by the engine
        String theScreen = jParams.getParameter ("screen");
        logger.debug ("theScreen=" + theScreen);

        boolean prevScreenIsApply = false;

        if (engineMap != null && theScreen != null) {
            Boolean prevScreenIsApplyBool = (Boolean) engineMap.get (
                    "prevScreenIsApply");
            prevScreenIsApply = (prevScreenIsApplyBool != null &&
                    prevScreenIsApplyBool.booleanValue ());

            if ( prevScreenIsApply ){
                elh = (EngineLanguageHelper)engineMap
                      .get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
                if (elh != null) {
                    previousLocale = elh.getPreviousLocale();
                }
            }
        }

        JahiaContentFieldFacade jahiaContentFieldFacade;

        if (theScreen != null) {
            // if no, load the field value from the session
            engineMap = (Map) theSession.getAttribute (
                    "jahia_session_engineMap");

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

            if (prevScreenIsApply) {
                engineMap.remove ("prevScreenIsApply");
                int fieldID = jParams.getFieldID ();

                List<Locale> localeList = jParams.getSite ().
                        getLanguageSettingsAsLocales (false);
                jahiaContentFieldFacade
                        = new JahiaContentFieldFacade (fieldID,
                                LoadFlags.ALL,
                                jParams,
                                localeList,
                                true);

                engineMap.put ("jahiaContentFieldFacade",
                        jahiaContentFieldFacade);
                Page_Field.resetPageBeanSession (jParams);

            } else {
                jahiaContentFieldFacade
                        = (JahiaContentFieldFacade) engineMap.get (
                                "jahiaContentFieldFacade");
            }
        } else {
            engineMap = new HashMap();

            final String gotoscreen = jParams.getParameter("gotoscreen");
            if (gotoscreen != null && gotoscreen.length() > 0) {
                theScreen = gotoscreen;
            } else {
                theScreen = "edit";
            }

            int fieldID = jParams.getFieldID();
            Page_Field.resetPageBeanSession (jParams);
            List<Locale> localeList = new ArrayList();
            List siteLanguageSettings = jParams.getSite ().getLanguageSettings ();
            if (siteLanguageSettings != null) {
                for (int i = 0; i < siteLanguageSettings.size (); i++) {
                    SiteLanguageSettings curSetting = (SiteLanguageSettings)
                            siteLanguageSettings.get(i);
                    if (curSetting.isActivated ()) {
                        Locale tempLocale = LanguageCodeConverters.
                                languageCodeToLocale (curSetting.
                                getCode ());
                        localeList.add (tempLocale);
                    }
                }
            }

            jahiaContentFieldFacade
                    = new JahiaContentFieldFacade (fieldID,
                            LoadFlags.ALL,
                            jParams,
                            localeList,
                            true);

            engineMap.put ("jahiaContentFieldFacade", jahiaContentFieldFacade);

            // init session
            engineMap.put (ENGINE_NAME + ".isSelectedField", Boolean.TRUE);

        }

        elh = (EngineLanguageHelper)engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        if ( elh == null ){
            elh = new EngineLanguageHelper();
            engineMap.put(JahiaEngine.ENGINE_LANGUAGE_HELPER,elh);
        }
        elh.update(jParams);
        if ( previousLocale != null ){
            elh = new EngineLanguageHelper(previousLocale);
        }
        Set updatedLanguageEntries = (Set) engineMap.get (
                ENGINE_NAME + "." +"updatedLanguageEntries");
        if ( updatedLanguageEntries == null ){
            updatedLanguageEntries = new HashSet();
        }
        if ( !updatedLanguageEntries.contains(elh.getCurrentLanguageCode()) ){
            updatedLanguageEntries.add(elh.getCurrentLanguageCode());
        }
        engineMap.put (ENGINE_NAME + "." +"updatedLanguageEntries", updatedLanguageEntries);

        // remember the updated fields ( Apply Change to all lang options )
        Set updatedFields = (Set) engineMap.get (ENGINE_NAME + "." +"updated.fields");
        if (updatedFields == null) {
            updatedFields = new HashSet();
        }
        engineMap.put (ENGINE_NAME + "." +"updated.fields", updatedFields);

        // MultiLanguage Issue -------------------------------------------------

        theField = jahiaContentFieldFacade.getField (elh.getCurrentEntryLoadRequest(), true);
        engineMap.put (ENGINE_NAME + "." +"theField", theField);

        engineMap.put (RENDER_TYPE_PARAM, new Integer (JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put (ENGINE_NAME_PARAM, ENGINE_NAME);
        engineMap.put (ENGINE_URL_PARAM,
                jParams.composeEngineUrl ("updatefield",
                        "?fid=" + theField.getID ()));
        engineMap.put ("updateField", "updateField");
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

        // sets engineMap for JSPs
        jParams.setAttribute ("engineTitle", "Update Field");
        jParams.setAttribute ("org.jahia.engines.EngineHashMap",
                engineMap);
        return engineMap;
    }

    /**
     * @param theField
     * @param jahiaContentFieldFacade
     * @param engineMap
     *
     * @throws JahiaException
     */
    private void applyChangeToAllLang (JahiaField theField,
                                       JahiaContentFieldFacade
            jahiaContentFieldFacade,
                                       Map engineMap, ProcessingContext jParams)
            throws JahiaException {

        Set updatedFields = (Set) engineMap.get (ENGINE_NAME + "." +"updated.fields");
        Iterator fieldEnum = jahiaContentFieldFacade.getFields ();
        JahiaField field;
        while (fieldEnum.hasNext ()) {
            field = (JahiaField) fieldEnum.next ();
            theField.copyValueInAnotherLanguage (field, jParams);

            // remember change
            if (!updatedFields.contains (new Integer (field.getID ()))) {
                updatedFields.add (new Integer (field.getID ()));
            }
        }
    }

}