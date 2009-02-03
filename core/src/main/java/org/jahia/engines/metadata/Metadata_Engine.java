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

//
package org.jahia.engines.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentMetadataFacade;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.data.containers.ContainerEditView;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.ContainerFieldsEditHelper;
import org.jahia.data.fields.FieldsEditHelperAbstract;
import org.jahia.data.fields.JahiaContentFieldFacade;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.LoadFlags;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.addcontainer.AddContainer_Engine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.engines.validation.ValidationError;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.LanguageCodeConverters;

/**
 * Display the popup that let the user add a new container.
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author NK
 * @version 1.0
 */
public class Metadata_Engine implements JahiaEngine {

    /** The engine name */
    public static final String ENGINE_NAME = "metadata_engine";

    public static final String CONTENT_METADATA_FACADE_ATTRIBUTE_NAME =
        "Metadata_Engine.ContentMetadataFacade";

    private static final String TEMPLATE_JSP = "add_container";

    /** Unique class instance */
    private static Metadata_Engine instance;

    /** logging */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (Metadata_Engine.class);

    /**
     * Default constructor, creates a new <code>Metadata_Engine</code> instance.
     */
    private Metadata_Engine () {
    }

    /**
     * Returns the unique instance of this class
     *
     * @return the unique instance of this class
     */
    public static synchronized Metadata_Engine getInstance () {

        if (instance == null) {
            instance = new Metadata_Engine ();
        }
        return instance;
    }

    /**
     * authoriseRender
     */
    public boolean authoriseRender (ProcessingContext jParams) {
        return EngineToolBox.getInstance().authoriseRender (jParams);
    }

    /**
     * @param jParams
     * @param theObj
     *
     * @return
     *
     * @throws JahiaException
     * todo add some javadoc description here
     */
    public String renderLink (ProcessingContext jParams, Object theObj)
            throws JahiaException {
        ObjectKey objectKey = (ObjectKey) theObj;
        StringBuffer params = new StringBuffer ("?mode=display&objectkey=");
        params.append (objectKey.toString());

        String opEditMode = jParams.getOperationMode();
        jParams.setOperationMode(ProcessingContext.EDIT);
        String result = jParams.composeEngineUrl (ENGINE_NAME, params.toString ());
        jParams.setOperationMode(opEditMode);
        return result;
    }

    public boolean needsJahiaData (ProcessingContext jParams) {
        return true;
    } // end needsJahiaData

    /**
     * Handle action when this engine is invoked as sub engine
     *
     * @param jParams ProcessingContext
     * @param mode int
     * @param objectKey ObjectKey
     * @throws JahiaException
     * @return boolean
     */
    public EngineValidationHelper handleActions (ProcessingContext jParams, int mode,
                                  ObjectKey objectKey)
    throws JahiaException {

        logger.debug ("Processing engine action...");
        jParams.setAttribute("subEngineMode",new Integer(mode));
        jParams.setAttribute("Metadata.objectKey",objectKey);
        return handleActions(jParams, null);
    }

    /**
     * Handle action when this engine is invoked as main engine
     *
     * @param jParams a ProcessingContext object
     * @param jData   a JahiaData object (not mandatory)
     */
    public EngineValidationHelper handleActions (ProcessingContext jParams, JahiaData jData)
            throws JahiaException, JahiaForbiddenAccessException {

        logger.debug ("Processing engine action...");

        Integer subEngineMode = (Integer) jParams.
                                getAttribute("subEngineMode");
        int mode = -1;
        if ( subEngineMode != null ){
            mode = subEngineMode.intValue();
        }
        boolean isInSubEngineMode = (mode != -1);

        // initalizes the hashmap
        Map engineMap = initEngineMap (jParams);

        // checks if the user has the right to display the engine
        JahiaUser theUser = jParams.getUser ();

        ContentMetadataFacade cmf = (ContentMetadataFacade)
                                    engineMap.get(CONTENT_METADATA_FACADE_ATTRIBUTE_NAME);

        ContentObject contentObject = cmf.getContentObject();
        if ( contentObject == null ) {
            JahiaPage thePage = jParams.getPage ();
            if (thePage.checkAdminAccess (theUser)) {
                engineMap.put ("adminAccess", Boolean.TRUE);
                //engineMap.put( "enableRightView", Boolean.TRUE );
                engineMap.put ("writeAccess", Boolean.TRUE);

            } else if (thePage.checkWriteAccess (theUser)) {
                engineMap.put ("writeAccess", Boolean.TRUE);
            }
        } else {
            if (contentObject.checkAdminAccess(theUser)) {
                engineMap.put("adminAccess", Boolean.TRUE);
                //engineMap.put( "enableRightView", Boolean.TRUE );
                engineMap.put("writeAccess", Boolean.TRUE);
//                engineMap.put ("enableAdvancedWorkflow", Boolean.TRUE);

            } else if (contentObject.checkWriteAccess(theUser)) {
                engineMap.put("writeAccess", Boolean.TRUE);
            }
        }

        EngineValidationHelper evh = null;

        if (engineMap.get ("writeAccess") != null) {
               // fire metadata engine after init event
               JahiaEvent theEvent = new JahiaEvent (this, jParams,
                       cmf);
               ServicesRegistry.getInstance ().getJahiaEventService ()
                   .fireMetadataEngineAfterInit(theEvent);

               if ( mode == JahiaEngine.VALIDATE_MODE ){
                   return validate(jParams,engineMap);
               }
               processLastScreen(jParams, engineMap);

               ContainerFieldsEditHelper feh = (ContainerFieldsEditHelper)engineMap.get(ENGINE_NAME+"."
                   +FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);

               if ( feh.getStayOnSameField() ){
                   feh.setSelectedFieldId(feh.getLastFieldId());
                   evh = new EngineValidationHelper();
                   evh.setNextScreen("metadata");
                   engineMap.put("screen","metadata");
                   evh.addError(new ValidationError(feh.getField(feh.getSelectedFieldId()),"Error processing last field"));
               }
               processCurrentScreen(jParams, engineMap);

        } else {
            throw new JahiaForbiddenAccessException ();
        }

        if ( !isInSubEngineMode ){
            // displays the screen
            EngineToolBox.getInstance().displayScreen(jParams, engineMap);
        }

        return evh;
    } // end handleActions


    private EngineValidationHelper validate(ProcessingContext jParams,
                             Map engineMap) throws JahiaException {
        EngineValidationHelper evh = null;

        ContentMetadataFacade contentMetadataFacade
                = (ContentMetadataFacade) engineMap.get (
                        CONTENT_METADATA_FACADE_ATTRIBUTE_NAME);

        ContainerFieldsEditHelper feh = (ContainerFieldsEditHelper)engineMap.get(ENGINE_NAME+"."
            +FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);

        EngineLanguageHelper elh = (EngineLanguageHelper)
                                   engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);

        evh = feh.validate(ENGINE_NAME, contentMetadataFacade, elh, jParams, engineMap);
        if ( evh != null && evh.hasErrors() ){
            evh.setNextScreen("metadata");
            JahiaField field = (JahiaField)evh.getFirstError().getSource();
            feh.setSelectedFieldId(field.getID());
            engineMap.put("screen","metadata");
            // prepare view
            this.processCurrentScreen(jParams,engineMap);

        }
        return evh;
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
    public EngineValidationHelper processLastScreen (ProcessingContext jParams, Map engineMap)
            throws JahiaException,
            JahiaForbiddenAccessException {
        logger.debug("Processing last screen...");

        Integer subEngineMode = (Integer) jParams.getAttribute("subEngineMode");
        int mode = -1;
        if (subEngineMode != null) {
            mode = subEngineMode.intValue();
        }
        boolean isInSubEngineMode = (mode != -1);

        if ( isInSubEngineMode ){
            Boolean alreadyProcessedLastScreen =
                (Boolean) jParams.getAttribute(ENGINE_NAME +
                "."
                + "alreadyProcessedLastScreen");
            if (alreadyProcessedLastScreen == null ||
                !alreadyProcessedLastScreen.booleanValue()) {
                jParams.setAttribute(ENGINE_NAME + "."
                                                  +
                                                  "alreadyProcessedLastScreen",
                                                  Boolean.TRUE);
            } else {
                return null;
            }
        }

        // gets the last screen
        // lastscreen   = edit, rights, logs
        String lastScreen = jParams.getParameter ("lastscreen");
        if (lastScreen == null) {
            lastScreen = "metadata";
            // reset session var
            jParams.getSessionState ().removeAttribute ("UpdateMetadata");
        }

        if ( !isInSubEngineMode ){
            // indicates to sub engines that we are processing last screen
            mode = JahiaEngine.UPDATE_MODE;
        }

        if (lastScreen.equals ("metadata")) {

            ContentMetadataFacade contentMetadataFacade
                    = (ContentMetadataFacade) engineMap.get (
                            CONTENT_METADATA_FACADE_ATTRIBUTE_NAME);

            ContainerFieldsEditHelper feh = (ContainerFieldsEditHelper)engineMap.get(ENGINE_NAME+"."
                +FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);

            /*
            // In case where the objectKey has changed ( the content object has been created in persistence )
            ObjectKey objectKey = (ObjectKey)jParams.getRequest().getAttribute("Metadata.objectKey");
            contentMetadataFacade.setObjectKey(objectKey);*/

            EngineLanguageHelper elh = (EngineLanguageHelper)
                                       engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
            if ( feh.getLastFieldId() != 0 ){
                feh.processLastFields(ENGINE_NAME, contentMetadataFacade, elh, jParams, engineMap, mode);
            }
        }

        return null;
    } // end processLastScreen

    /**
     * prepares the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public EngineValidationHelper processCurrentScreen (ProcessingContext jParams, Map engineMap)
            throws JahiaException,
            JahiaForbiddenAccessException {

        logger.debug ("Processing current screen...");
        EngineLanguageHelper elh = (EngineLanguageHelper)
                                   engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        ContainerFieldsEditHelper feh = (ContainerFieldsEditHelper)engineMap.get(ENGINE_NAME+"."
            +FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);

        Integer subEngineMode = (Integer) jParams.getAttribute("subEngineMode");
        int mode = -1;
        if ( subEngineMode != null ){
            mode = subEngineMode.intValue();
        }
        boolean isInSubEngineMode = (mode != -1);

        ContentMetadataFacade contentMetadataFacade
                = (ContentMetadataFacade) engineMap.get (
                        CONTENT_METADATA_FACADE_ATTRIBUTE_NAME);

        /*
        // In case where the objectKey has changed ( the content object has been created in persistence )
        ObjectKey objectKey = (ObjectKey)jParams.getRequest().getAttribute("Metadata.objectKey");
        contentMetadataFacade.setObjectKey(objectKey);*/

        JahiaContainer theContainer = contentMetadataFacade.getContainer (
                elh.getCurrentEntryLoadRequest(), true);

        //engineMap.put (this.ENGINE_NAME + "." +"theContainer", theContainer);

        // gets the current screen
        // screen   = edit, rights, logs
        String theScreen = (String) engineMap.get ("screen");

        if ( !isInSubEngineMode ){
            // indicates to sub engines that we are processing last screen
            mode = JahiaEngine.LOAD_MODE;
        }

        if ( isInSubEngineMode ){
            String alreadyProcessedCurrentScreen =
                (String) jParams.getAttribute(ENGINE_NAME +
                "."
                + "alreadyProcessedCurrentScreen");
            if (!theScreen.equals(alreadyProcessedCurrentScreen)){
                jParams.setAttribute(ENGINE_NAME +
                "." + "alreadyProcessedCurrentScreen", new String(theScreen));
            } else {
                return null;
            }
            // reset fieldform
            engineMap.put (ENGINE_NAME+"."+"fieldForms", new HashMap());
        }

        if (theScreen.equals ("metadata")) {
            // switch mode to upload instead of Update, because, the update is already done in processLastScreen
            mode = JahiaEngine.LOAD_MODE;
            feh.processCurrentFields(ENGINE_NAME, contentMetadataFacade, elh, jParams, engineMap, mode);

            // This test should replace the above
        } else if (theScreen.equals ("save") || theScreen.equals("apply")) {

            /*
            if ( feh.getStayOnSameField() ){
                // there are some error
                EngineValidationHelper evh = new EngineValidationHelper();
                evh.setNextScreen("metadata");
                engineMap.put("screen","metadata");
                return evh;
            }*/

            logger.debug ("processCurrentScreen > we are in save Mode");
            // Save only if the objectKey is a ContentObject, not a ContentDefinition
            try {
                ContentDefinition.
                    getContentDefinitionInstance(contentMetadataFacade.getObjectKey());
                if ( isInSubEngineMode ){
                        jParams.removeAttribute(ENGINE_NAME +
                            "." + "alreadyProcessedCurrentScreen");
                }
                return null; // Not a permanent ContentObject so abort save.
            } catch ( Exception t ){
            }
            if ( !contentMetadataFacade.isContentObject() ){
                throw new JahiaException("Invalid contentObject","Invalid contentObject",
                                         JahiaException.DATA_ERROR,JahiaException.ERROR_SEVERITY);
            }
            ContentObject contentObject = contentMetadataFacade.getContentObject();

            // fire event
            JahiaEvent theEvent = new JahiaEvent (this, jParams, contentMetadataFacade);
            ServicesRegistry.getInstance ().getJahiaEventService ()
                    .fireMetadataEngineBeforeSave (theEvent);

            mode = JahiaEngine.SAVE_MODE;

            Map existingMetadatas = new HashMap();
            Iterator iterator = ContentObject.getMetadatas(contentObject.getObjectKey()).iterator();
            ContentField contentField = null;
            while ( iterator.hasNext() ){
                contentField = (ContentField)iterator.next();
                try {
                    ContentDefinition def = JahiaFieldDefinition
                            .getContentDefinitionInstance(contentField.getDefinitionKey(null));
                    existingMetadatas.put(def.getName(),contentField);
                } catch ( Exception t ){
                    logger.debug(t);
                }
            }

            Iterator fieldFacadeEnum = contentMetadataFacade.getFields ();
            while (fieldFacadeEnum.hasNext ()) {
                JahiaContentFieldFacade contentFieldFacade = (
                        JahiaContentFieldFacade) fieldFacadeEnum.next ();
                Iterator fields = contentFieldFacade.getFields ();
                EntryLoadRequest processingEntryLoadRequest = null;
                while (fields.hasNext ()) {
                    JahiaField field = (JahiaField) fields.next ();
                    String fieldName = field.getDefinition().getName();
                    if (feh.containsUpdatedField(field.getID(),field.getLanguageCode ())) {
                        if ( field.getID()<0 ) {
                            // check if the metadata is really new ( doesn't exist in db )
                            ContentField cf = (ContentField)existingMetadatas.get(fieldName);
                            if ( cf != null ){
                                field.setID(cf.getID());
                                field.setAclID(cf.getAclID());
                            } else {
                                field.setID(0);
                            }
                        }

                        // save the active entry only if the staging doesn't exists.
                        processingEntryLoadRequest =
                                new EntryLoadRequest (field.getWorkflowState (),
                                        field.getVersionID (),
                                        new ArrayList());
                        processingEntryLoadRequest.getLocales ()
                                .add (LanguageCodeConverters.languageCodeToLocale (
                                        field.getLanguageCode ()));
                        EntryLoadRequest savedEntryLoadRequest =
                            jParams.getSubstituteEntryLoadRequest();
                        jParams.setSubstituteEntryLoadRequest (
                                processingEntryLoadRequest);
                        if (field.getID () == 0) {
                            field.setIsMetadata(true);
                            field.setMetadataOwnerObjectKey(contentObject.getObjectKey());
                            // create the field only once
                            field.setAclID(contentObject.getAclID ());
                            EngineToolBox.getInstance().processFieldTypes (field, theContainer, ENGINE_NAME, jParams, mode, engineMap);
                        } else {
                            EngineToolBox.getInstance().processFieldTypes (field, theContainer, ENGINE_NAME, jParams, mode, engineMap);
                        }
                        if (field.getID()>0 && !existingMetadatas.containsKey(fieldName)){
                            ContentField cf = ContentField.getField(field.getID());
                            if (cf != null){
                                existingMetadatas.put(fieldName,cf);
                            }
                        }
                        engineMap.put (ENGINE_NAME + "." + "theField", field);
                        jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);
                    }
                }
            }

            // fire event
            theEvent = new JahiaEvent (this, jParams, contentObject.getObjectKey());
            ServicesRegistry.getInstance ().getJahiaEventService ().
                    fireMetadataEngineAfterSave (theEvent);

            // flag for subEngine: means that is a call from  AddContainer, reset the flag
            jParams.getSessionState ().setAttribute ("MetadataEngine", "false");
            // #endif
            logger.debug ("Saving container !!");
        } else if (theScreen.equals ("cancel")) {
            // #endif
            // flag for subEngine: means that is a call from  AddContainer, reset the flag
            jParams.getSessionState ().setAttribute ("MetadataEngine", "false");
        }
        return null;

    } // end processCurrentScreen

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

        Map engineMap = null;
        JahiaContainer theContainer = null;
        EngineLanguageHelper elh = null;
        ContainerFieldsEditHelper feh = null;
        int lastFieldId = 0;

        Integer subEngineMode = (Integer) jParams.
                                getAttribute("subEngineMode");
        int mode = -1;
        if ( subEngineMode != null ){
            mode = subEngineMode.intValue();
        }
        boolean isInSubEngineMode = (mode != -1);

        // gets session values
        //HttpSession theSession = jParams.getRequest().getSession( true );
        SessionState theSession = jParams.getSessionState ();

        // flag for subEngine: means that is a call from  Metadata engine
        theSession.setAttribute (ENGINE_NAME, "true");

        String theScreen = jParams.getParameter ("screen");
        logger.debug ("Initializing engine map for screen [" + theScreen + "]");

        ContentMetadataFacade contentMetadataFacade = null;
        ObjectKey objectKey = null;

        if ( !isInSubEngineMode ){
            if (theScreen != null) {
                // if no, load data value from the session
                engineMap = (Map) theSession.getAttribute(
                    "jahia_session_engineMap");

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
                contentMetadataFacade = (ContentMetadataFacade)
                                        engineMap.get(
                    CONTENT_METADATA_FACADE_ATTRIBUTE_NAME);
                //objectKey = (ObjectKey)engineMap.get("Metadata.objectKey");
            }
        } else {
            // try to get from session
            engineMap = (Map) theSession.getAttribute(
                "jahia_session_engineMap");
            if (engineMap == null) {
                throw new JahiaSessionExpirationException();
            }
            contentMetadataFacade = (ContentMetadataFacade)
                                    engineMap.get(
                CONTENT_METADATA_FACADE_ATTRIBUTE_NAME);
            feh = (ContainerFieldsEditHelper)engineMap.get(ENGINE_NAME+"."
                                                      +FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);
        }
        if ( engineMap == null ){

            // init engine map
            engineMap = new HashMap();
            org.jahia.engines.shared.Page_Field.resetPageBeanSession (jParams);
            // retrieve the choice ID if the screen is reloaded. Otherwise it should be set
            // to a default value, to avoid having it set to 3 (tree operation) if no source
            // page has been selected
            String reload = (String) jParams.getParameter ("reload");
            logger.debug (" reload = " + reload);
            if (reload != null) {
                if (reload.equals ("true")) {
                    String engineMapSessionAttributeName = "jahia_session_engineMap";
                    Map previousEngineMap = (Map) theSession.
                            getAttribute (engineMapSessionAttributeName);
                    if (previousEngineMap != null) {
                        logger.debug (" previous session is not null");
                        feh = (ContainerFieldsEditHelper)engineMap.get(ENGINE_NAME+"."
                                                              +FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);
                        if ( feh != null ){
                            lastFieldId = feh.getSelectedFieldId();
                        }
                        theSession.removeAttribute (engineMapSessionAttributeName);
                        feh = null;
                    }
                }
            }
        }

        if ( !isInSubEngineMode ){
            String objectKeyStr = (String) jParams.getParameter("objectkey");
            try {
                objectKey = (ObjectKey) ObjectKey.getInstance(objectKeyStr);
                //engineMap.put("Metadata.objectKey",objectKey);
            } catch (Exception t) {
                throw new JahiaException("Error resolving objectkey",
                                         "Error resolving objectkey",
                                         JahiaException.ERROR_SEVERITY,
                                         JahiaException.ERROR_SEVERITY, t);
            }
        } else {
            objectKey = (ObjectKey)jParams.getAttribute("Metadata.objectKey");
            //engineMap.put("Metadata.objectKey",objectKey);
        }

        if ( contentMetadataFacade == null ){
            contentMetadataFacade = createContentMetadataFacade(jParams,objectKey);
            engineMap.put (CONTENT_METADATA_FACADE_ATTRIBUTE_NAME, contentMetadataFacade);

            theScreen = "metadata";
        } else {
            // In case the content object is now persisted in db, the objectkey is
            // no more a ContentDefinition but a ContentObject
            contentMetadataFacade.setObjectKey(objectKey);
        }

        // Init Engine Language Helper
        elh = (EngineLanguageHelper)engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        if ( elh == null ){
            elh = new EngineLanguageHelper();
            engineMap.put(JahiaEngine.ENGINE_LANGUAGE_HELPER,elh);
        }
        elh.update(jParams);

        theContainer = contentMetadataFacade.getContainer (elh.getCurrentEntryLoadRequest(), true);

        if ( feh == null ){
            /*
            // ensure to create metadata association for this object's content definition
            ObjectKey object = null;
            ContentDefinition contentDefinition = null;
            try {
                if (contentMetadataFacade.isContentObject()) {
                    object = contentMetadataFacade.getContentObject()//
                             .getDefinitionKey(null);
                    contentDefinition = ContentDefinition
                                        .getContentDefinitionInstance(objectKey);
                } else {
                    object = contentMetadataFacade.getObjectKey();
                    contentDefinition = ContentDefinition
                                        .getContentDefinitionInstance(objectKey);
                }
                ServicesRegistry.getInstance().getMetadataService()
                    .assignMetadataToContentDefinition(contentDefinition);
            } catch ( ClassNotFoundException cnfe ){
                throw new JahiaException("Error initializing metadata engine",
                                         "Error initializing metadata engine",
                                         JahiaException.ERROR_SEVERITY,
                                         JahiaException.ERROR_SEVERITY, cnfe);
            }*/

            feh = new ContainerFieldsEditHelper(theContainer);
            // create the edit view
            ContainerEditView editView = ContainerEditView.getDefaultInstance(theContainer,jParams,null);
            feh.setContainerEditView(editView);
            feh.setSelectedFieldId(0);
            engineMap.put(ENGINE_NAME+"."+FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID,feh);
        }

        // Update FieldsEditHelper
        feh.setFieldForms(new HashMap());
        feh.setStayOnSameField(false);

        String lastScreen = jParams.getParameter ("lastscreen");
        if ( "metadata".equals(lastScreen) ){
            // come from same engine
            feh.processRequest(jParams,lastFieldId);
        } else {
            //feh.setSelectedFieldId(0);
            //feh.setLastFieldId(0);
        }

        engineMap.put ( AddContainer_Engine.ENGINE_NAME+"."+"fieldForms", new HashMap());

        /*
        theContainer = contentMetadataFacade.getContainer (
                elh.getCurrentEntryLoadRequest(), true);
        engineMap.put (this.ENGINE_NAME+"."+"theContainer", theContainer);*/

        engineMap.put ("objectKey", objectKey);

        if ( !isInSubEngineMode ){
            engineMap.put("noApply", EMPTY_STRING);
            engineMap.put(RENDER_TYPE_PARAM,
                          new Integer(JahiaEngine.RENDERTYPE_FORWARD));
            engineMap.put(ENGINE_NAME_PARAM, ENGINE_NAME);
            engineMap.put(ENGINE_URL_PARAM,
                          jParams.composeEngineUrl(ENGINE_NAME,
                "?objectkey=" + objectKey.toString()));
        }
        theSession.setAttribute ("jahia_session_engineMap", engineMap);

        if ( !isInSubEngineMode ){
            // init map
            engineMap.put("screen", theScreen);
            if (!theScreen.equals("save") && !theScreen.equals("cancel")) {
                engineMap.put("jspSource", TEMPLATE_JSP);

                //} else if (theScreen.equals ("apply")) {
                //    engineMap.put( "jspSource", "apply" );

            } else {
                engineMap.put("jspSource", "close");
            }
        }

        // sets engineMap for JSPs
        if ( !isInSubEngineMode ){
            engineMap.put(ENGINE_NAME + "." + "fieldForms", new HashMap());
        } else {

        }
        jParams.setAttribute ("engineTitle", "Metadata Engine");
        jParams.setAttribute ("org.jahia.engines.EngineHashMap",
                engineMap);

        return engineMap;
    } // end initEngineMap

    /**
     * @param jParams   ProcessingContext
     * @param objectKey ObjectKey
     * @return ContentMetadataFacade
     * @throws JahiaException
     */
    public synchronized static ContentMetadataFacade createContentMetadataFacade (final ProcessingContext jParams,
                                                                                  final ObjectKey objectKey)
            throws JahiaException {
        if (objectKey instanceof ContentObjectKey && objectKey.getIdInType() > 0) {
            try {
                final ContentObject contentObject = ContentObject.getContentObjectInstance(objectKey);
                if (contentObject != null) {
                    return new ContentMetadataFacade(objectKey,
                            LoadFlags.ALL,
                            jParams,
                            jParams.getSite().getLanguageSettingsAsLocales(false),
                            true, true);
                }
            } catch (final Throwable t) {
                logger.error(t, t);
                return null;
            }
        }
        return new ContentMetadataFacade(objectKey, jParams, jParams.getSite().getLanguageSettingsAsLocales(false));
    }

}
