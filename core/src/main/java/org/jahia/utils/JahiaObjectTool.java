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
//  JahiaObjectUtils
//  MJ      12.03.2001
//
//

package org.jahia.utils;


import java.util.Map;

import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.fields.ContentField;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.version.EntryLoadRequest;


/**
 * Utility class to return characteristic strings with an input of (int objectType, int objectID)
 * intended for use by the audit log management system
 *
 * @author MJ
 */


public class JahiaObjectTool
{

    private static JahiaObjectTool theObject           = null;

    private static final String    MSG_INTERNAL_ERROR  = "Object Tool internal error";

    public static final int        FIELD_TYPE          = 1;
    public static final int        CONTAINER_TYPE      = 2;
    public static final int        CONTAINERLIST_TYPE  = 3;
    public static final int        PAGE_TYPE           = 4;
    public static final int        ACL_TYPE            = 5;
    public static final int        APPLICATION_TYPE    = 6;
    public static final int        SERVER_TYPE         = 7;
    public static final int        TEMPLATE_TYPE       = 8;
    public static final int        CATEGORY_TYPE       = 9;

    /***
        * returns the single instance of the object
        *
        */
    public static synchronized JahiaObjectTool getInstance()
    {
        if (theObject == null) {
            theObject = new JahiaObjectTool();
        }
        return theObject;
    } // end getInstance



    /***
        * get the object name according to the object type and object ID (without engineMap)
        * @param    objectType
        * @return   the object name, as an <code>String</code>
        */
    public String getObjectName( int objectType, int objectID, ProcessingContext jParams )
    throws JahiaException
    {
        ServicesRegistry sReg = ServicesRegistry.getInstance();
        EntryLoadRequest loadRequest = new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, jParams.getEntryLoadRequest().getLocales());

        switch(objectType) {
            case PAGE_TYPE:
                JahiaPage thePage = sReg.getJahiaPageService().lookupPage(objectID,
                    loadRequest, jParams.getOperationMode(), jParams.getUser(), true);
                if (thePage != null) {
                    String title = thePage.getTitle();
                    if (title != null) {
                        return title;
                    }
                }
                return "n/a";
            case FIELD_TYPE:
                ContentField f = ContentField.getField(objectID);

                if ( f!= null ){
                    return sReg.getJahiaFieldService().loadFieldDefinition(f.getDefinitionID(null)).getName();
                } else {
                    return ("field [" + objectID + "]");
                }
            case TEMPLATE_TYPE:
                return sReg.getJahiaPageTemplateService().lookupPageTemplate(objectID).getName();
            case SERVER_TYPE:
                return "Jahia Server";
//            case CATEGORY_TYPE:
//                return sReg.getCategoryService().getCategoryByUUID(objectID).getKey();
            default:
                throw new JahiaException (MSG_INTERNAL_ERROR,
                                          "Incompatible Object Type passed to JahiaObjectTool.getObjectName(objectType, objectID)",
                                          JahiaException.SERVICE_ERROR,
                                          JahiaException.CRITICAL_SEVERITY);
        }
    } // end getObjectName( int objectType, int objectID )



    /***
        * get the object name according to the object type
        * @param    objectType
        * @param    engineMap
        * @return   the object name, as an <code>String</code>
        */
    public String getObjectName( int objectType, Map engineMap )
    throws JahiaException
    {
        switch(objectType) {
            case PAGE_TYPE:          return ((JahiaPage) engineMap.get( "thePage" )).getTitle();
            case FIELD_TYPE:         return ((JahiaField) engineMap.get( "theField" )).getDefinition().getName();
            case TEMPLATE_TYPE:      return ((JahiaPageDefinition) engineMap.get( "theTemplate" )).getName();
            case CATEGORY_TYPE:      return ((Category) engineMap.get("categoryKey")).getKey();

            default:                 throw new JahiaException (MSG_INTERNAL_ERROR,
                                                               "Incompatible Object Type passed to JahiaObjectTool.getObjectName(objectType, engineMap)",
                                                               JahiaException.SERVICE_ERROR,
                                                               JahiaException.CRITICAL_SEVERITY);
        }
    } // end getObjectName (int objectType, Map engineMap)


    /***
        * get an object type name from the objectType
        * @param    objectType     the object type, as an <code>int</code>
        * @return   a <code>String</code> containing the name of the object type
        *
        */
    public String getObjectTypeName( int objectType )
    throws JahiaException
    {
        switch(objectType) {

            case CONTAINERLIST_TYPE: return "containerlist";
            case CONTAINER_TYPE:     return "container";
            case PAGE_TYPE:          return "page";
            case FIELD_TYPE:         return "field";
            case TEMPLATE_TYPE:      return "template";
            case CATEGORY_TYPE:      return "category";
            case APPLICATION_TYPE:   return "application";
            default:                 throw new JahiaException (MSG_INTERNAL_ERROR,
                                                               "Incompatible Object Type passed to JahiaObjectTool.getObjectName(objectType)",
                                                               JahiaException.SERVICE_ERROR,
                                                               JahiaException.CRITICAL_SEVERITY);
        }
    }


    /***
        * get the object ID according to the object type
        * @param    objectType
        * @param    engineMap
        * @return   the object ID, as an <code>int</code>
        */
    public int getObjectID( int objectType, Map<String, Object> engineMap )
    throws JahiaException
    {
        switch(objectType) {

            case PAGE_TYPE:          return ((JahiaPage) engineMap.get( "thePage" )).getID();
            case FIELD_TYPE:         return ((JahiaField) engineMap.get( "theField" )).getID();
            case TEMPLATE_TYPE:      return ((JahiaPageDefinition) engineMap.get( "theTemplate" )).getID();
            case CATEGORY_TYPE:      return ((Category) engineMap.get("categoryKey")).getObjectKey().getIdInType();
            default:                 throw new JahiaException (MSG_INTERNAL_ERROR,
                                                               "Incompatible Object Type passed to JahiaObjectTool",
                                                               JahiaException.SERVICE_ERROR,
                                                               JahiaException.CRITICAL_SEVERITY);
        }
    }



} // end class JahiaObjectTool
