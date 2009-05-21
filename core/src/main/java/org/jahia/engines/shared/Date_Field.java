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
//
//  Date_Field
//  EV  14.01.20001
//
//  handleField( mode, jParams )
//

package org.jahia.engines.shared;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.ConnectionTypes;
import org.jahia.data.FormDataManager;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.*;
import org.jahia.engines.EngineParams;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.JahiaEngineTools;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.utils.JahiaTools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Date_Field implements FieldSubEngine
{

    private static org.apache.log4j.Logger logger =
              org.apache.log4j.Logger.getLogger(Date_Field.class);

    private static          Date_Field theObject   = null;
    public  static final    String          JSP_FILE    = "/engines/shared/date_field.jsp";
    public  static final    String  READONLY_JSP  		= "/engines/shared/readonly_date_field.jsp";


    /***
        * getInstance
        * AK    19.12.2000
        *
        */
    public static synchronized Date_Field getInstance()
    {
        if (theObject == null) {
            theObject = new Date_Field();
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
        String fieldsEditCallingEngineName = (String) engineMap.get( "fieldsEditCallingEngineName" );
        JahiaField  theField    = (JahiaField) engineMap.get( fieldsEditCallingEngineName + "." + "theField" );
        //JahiaField theField = (JahiaField) engineMap.get( "theField" );
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
    throws JahiaException {
        String fieldValueStr    = jParams.getParameter( "_Str" + theField.getID());
        String engineParams     = jParams.getParameter( "engine_params" );
        if (StringUtils.isNotEmpty(fieldValueStr)) {
            fieldValueStr = JahiaTools.replacePattern(fieldValueStr,"|","&#124;");
        }

        EngineParams eParams    = new EngineParams( engineParams );
        String localSwitch      = eParams.getParameter( "localswitch" );
        String dataSourceUrl    = eParams.getParameter( "dsurl" );

        if (dataSourceUrl != null) {
            theField.setValue( dataSourceUrl );
            theField.setConnectType( ConnectionTypes.DATASOURCE );
        } else if (localSwitch != null) {
            theField.setValue( theField.getDefinition().getDefaultValue() );
            theField.setConnectType( ConnectionTypes.LOCAL );
        } else if (fieldValueStr != null) {
            Date parsedValue = null;
            SimpleDateFormat fmt = null;
            if (fieldValueStr.length() > 0) {
                try {
                    fmt = JahiaDateFieldUtil.getDateFormatForParsing(theField
                            .getDefinition().getDefaultValue(),
                            jParams.getLocale());
                    parsedValue = fmt.parse(fieldValueStr);
                } catch (ParseException ex) {
                    logger.debug("Unable to parse date value '" + fieldValueStr
                            + "' using pattern '"
                            + (fmt != null ? fmt.toPattern() : "null"), ex);
                }
            }
            theField.setObject(parsedValue != null ? String.valueOf(parsedValue
                    .getTime()) : null);
            theField.setValue( fieldValueStr );
        }

        return true;
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
        if (logger.isDebugEnabled()) {
            logger.debug("Saving Field " + theField.getDefinition().getName() + " : " + theField.getValue() );
        }
        boolean result = theField.save(jParams);
        if ( result == true ){
            theField.load(LoadFlags.ALL,jParams);
        }
        return result;
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
        //boolean editable = false;
        //JahiaContainer theContainer = (JahiaContainer)engineMap.get("theContainer");
        String fieldsEditCallingEngineName = (String) engineMap.get( "fieldsEditCallingEngineName" );
        
        boolean editable = false;
        JahiaContainer theContainer = (JahiaContainer)engineMap.get(fieldsEditCallingEngineName + "." +"theContainer");
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

        String forward = (String)theField.getDefinition().getProperty(JahiaFieldDefinitionProperties.FIELD_UPDATE_JSP_FILE_PROP);
        if (forward == null) {
            forward = JSP_FILE;
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
                forward = READONLY_JSP;
            }
        }

        if ( editable ){

            String localSwitchUrl = "ReloadEngine('localswitch" + EngineParams.VALUE_TOKEN + "yes')";
            engineMap.put( "localSwitchUrl", localSwitchUrl );

            theField.setValue( FormDataManager.formEncode(theField.getValue()) );
            output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet( (ParamBean) jParams, forward );
            //System.out.println("output: "+output);
        } else {
            output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet( (ParamBean) jParams, forward );
        }

        engineMap.put( fieldsEditCallingEngineName + "." +"fieldForm", output );

        return true;
    } // end composeEngineMap


} // end SmallText_Field
