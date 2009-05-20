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
//  Integer_Field
//  YG 08.08.2001
//
//  handleField( mode, jParams )
//

package org.jahia.engines.shared;

import java.util.Map;

import org.jahia.data.ConnectionTypes;
import org.jahia.data.FormDataManager;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.FieldsEditHelper;
import org.jahia.data.fields.FieldsEditHelperAbstract;
import org.jahia.data.fields.JahiaField;
import org.jahia.engines.EngineParams;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.JahiaEngineTools;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.EnginesRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.utils.JahiaTools;

public class Integer_Field implements FieldSubEngine
{

    private static org.apache.log4j.Logger logger =
                org.apache.log4j.Logger.getLogger(Integer_Field.class);

    private static          Integer_Field theObject   = null;
    private static final    String          JSP_FILE    = "/engines/shared/integer_field.jsp";
    public  static final    String  READONLY_JSP  		= "/engines/shared/readonly_smalltext_field.jsp";


    /***
        * getInstance
        * AK    19.12.2000
        *
        */
    public static synchronized Integer_Field getInstance()
    {
        if (theObject == null) {
            theObject = new Integer_Field();
        }
        return theObject;
    } // end getInstance



    /***
        * handles the field actions
        *
        * @param        jParams             a ProcessingContext object
        * @param        modeInt             the mode, according to JahiaEngine
        * @return       true if everything went okay, false if not
        * @see          org.jahia.engines.JahiaEngine
        *
        */
    public boolean handleField( ProcessingContext jParams, Integer modeInt, Map engineMap )
    throws JahiaException
    {
        int mode = modeInt.intValue();
        JahiaField theField = (JahiaField) engineMap.get( "theField" );
        switch (mode)
        {
            case (JahiaEngine.LOAD_MODE)            :   return composeEngineMap( jParams, engineMap, theField );
            case (JahiaEngine.UPDATE_MODE)          :   return getFormData( jParams, engineMap, theField );
            case (JahiaEngine.SAVE_MODE)            :   return saveData( jParams, engineMap, theField );
        }
        return false;
    } // end handleField



    /***
        * gets POST data from the form and saves it in session
        *
        * @param        jParams             a ProcessingContext object
        * @param        engineMap           the engine hashmap
        * @param        theField            the field we are working on
        * @return       true if everything went okay, false if not
        *
        */
    private boolean getFormData( ProcessingContext jParams, Map engineMap, JahiaField theField )
    throws JahiaException
    {
        boolean out             = true;

        String fieldValue       = jParams.getParameter( "_" + new Integer(theField.getID()).toString() );
        String engineParams     = jParams.getParameter( "engine_params" );
        fieldValue = JahiaTools.replacePattern(fieldValue,"|","&#124;");


        EngineParams eParams    = new EngineParams( engineParams );
        String localSwitch      = eParams.getParameter( "localswitch" );
        String dataSourceUrl    = eParams.getParameter( "dsurl" );

        if (dataSourceUrl != null) {
            theField.setValue( dataSourceUrl );
            theField.setConnectType( ConnectionTypes.DATASOURCE );
        } else if (localSwitch != null) {
            theField.setValue( theField.getDefinition().getDefaultValue() );
            theField.setConnectType( ConnectionTypes.LOCAL );
        } else if (fieldValue != null) {
            theField.setValue( fieldValue );
            out = true;
        }

        return out;
    } // end getFormData



    /***
        * saves data in datasource
        *
        * @param        jParams             a ProcessingContext object
        * @param        engineMap           the engine hashmap
        * @param        theField            the field we are working on
        * @return       true if everything went okay, false if not
        *
        */
    private boolean saveData( ProcessingContext jParams, Map engineMap, JahiaField theField )
    throws JahiaException
    {
        logger.debug("Saving Field " + theField.getDefinition().getName() + " : " + theField.getValue() );
        return theField.save(jParams);
    } // end saveData



    /***
        * composes engine hash map
        *
        * @param        jParams             a ProcessingContext object
        * @param        engineMap           the engine hashmap
        * @param        theField            the field we are working on
        * @return       true if everything went okay, false if not
        *
        */
    private boolean composeEngineMap( ProcessingContext jParams, Map engineMap, JahiaField theField )
    throws JahiaException
    {

        boolean editable = false;
        String fieldsEditCallingEngineName = (String) engineMap.get( "fieldsEditCallingEngineName" );
        JahiaContainer theContainer = (JahiaContainer)engineMap.get("theContainer");
        if (theContainer == null) {
            // in case of a field , not a field in a container
            editable = true;
        } else {
            FieldsEditHelper feh = (FieldsEditHelper)engineMap.get(fieldsEditCallingEngineName + "."
            + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);
            Map ctnListFieldAcls = feh.getCtnListFieldAcls();
            int fieldId = theField.getID();
            if (theContainer.getListID() != 0 && ctnListFieldAcls != null && ctnListFieldAcls.size()>0) {
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
        if ( editable && ! readOnly){

            String localSwitchUrl = "ReloadEngine('localswitch" + EngineParams.VALUE_TOKEN + "yes')";
            engineMap.put( "localSwitchUrl", localSwitchUrl );

            String dataSourceConnectUrl = "";
            JahiaEngine dsEngine = (JahiaEngine) EnginesRegistry.getInstance().getEngine( "selectdatasource" );
            dataSourceConnectUrl = dsEngine.renderLink( jParams, theField );
            engineMap.put( "dataSourceConnectUrl", dataSourceConnectUrl );

            String dataSourceIDUrl = "";
            dsEngine = (JahiaEngine) EnginesRegistry.getInstance().getEngine( "viewdatasourceid" );
            dataSourceIDUrl = dsEngine.renderLink( jParams, theField );
            engineMap.put( "dataSourceIDUrl", dataSourceIDUrl );

            boolean isIE = false;
            String userAgent = jParams.getUserAgent();
            if (userAgent != null) {
                isIE = (userAgent.indexOf( "IE" ) != -1);
            }
            jParams.setAttribute("isIE" , Boolean.valueOf(isIE));

            theField.setValue( FormDataManager.formEncode(theField.getValue()) );
            output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet( (ParamBean) jParams, JSP_FILE );
        } else {
            output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet( (ParamBean) jParams, READONLY_JSP );
        }

        engineMap.put( fieldsEditCallingEngineName + "." + "fieldForm", output );

        return true;
    } // end composeEngineMap


} // end SmallText_Field
