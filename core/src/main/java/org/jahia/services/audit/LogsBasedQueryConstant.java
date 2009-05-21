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
package org.jahia.services.audit;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 22 nov. 2007
 * Time: 15:09:19
 * To change this template use File | Settings | File Templates.
 */
public class LogsBasedQueryConstant {

    // modification, creation and deletion operations
    public static final String OPERATION_ADDED_FIELD = "added field";
    public static final String OPERATION_UPDATED_FIELD = "modified field";
    public static final String OPERATION_DELETED_FIELD = "deleted field";
    public static final String OPERATION_ADDED_CONTAINER = "added container";
    public static final String OPERATION_UPDATED_CONTAINER = "updated container";
    public static final String OPERATION_DELETED_CONTAINER = "deleted container";
    public static final String OPERATION_ADDED_PAGE = "added page";
    public static final String OPERATION_MODIFIED_PAGE = "modified page";
    public static final String OPERATION_DELETED_PAGE = "deleted page";
    public static final String OPERATION_SET_PROPERTY_FOR_PAGE = "set properties for page";
    public static final String OPERATION_SET_PROPERTY_FOR_CONTAINER = "set properties for container";
    public static final String OPERATION_SET_PROPERTY_FOR_CONTAINER_LIST = "set properties for containerList";
    public static final String OPERATION_SET_RIGHTS = "set rights";
    public static final String OPERATION_UPDATED_TEMPLATE = "template updated";

    // activation and validation of deleted content operations
    public static final String OPERATION_FIELD_ACTIVATION = "activation of field";
    public static final String OPERATION_CONTAINER_ACTIVATION = "activation of container";
    public static final String OPERATION_PAGE_ACTIVATION = "activation of page";
    public static final String OPERATION_FIRST_FIELD_ACTIVATION = "first activation of field";
    public static final String OPERATION_FIRST_CONTAINER_ACTIVATION = "first activation of container";
    public static final String OPERATION_FIRST_CONTAINER_LIST_ACTIVATION = "first activation of container list";
    public static final String OPERATION_FIRST_PAGE_ACTIVATION = "first activation of page";
    public static final String OPERATION_CONTAINER_LIST_ACTIVATION = "activation of container list";
    public static final String OPERATION_FIELD_DELETION_AND_ACTIVATION = "deletion of field";
    public static final String OPERATION_CONTAINER_DELETION_AND_ACTIVATION = "deletion of container";
    public static final String OPERATION_CONTAINER_LIST_DELETION_AND_ACTIVATION = "deletion of container list";
    public static final String OPERATION_PAGE_DELETION_AND_ACTIVATION = "deletion of page";

    public static final int CONTAINER_TYPE = LoggingEventListener.CONTAINER_TYPE;
    public static final int CONTAINER_LIST_TYPE = LoggingEventListener.CONTAINERLIST_TYPE;
    public static final int PAGE_TYPE = LoggingEventListener.PAGE_TYPE;
    public static final int ACL_TYPE = LoggingEventListener.ACL_TYPE;
    public static final int FIELD_TYPE = LoggingEventListener.FIELD_TYPE;

    public static final int AND_LOGIC = 1;
    public static final int OR_LOGIC = 2;

    public static final String LOG_ALIAS = "log";
    public static final String PROPERTY_ID = LOG_ALIAS + ".id";
    public static final String PROPERTY_OBJECT_ID = LOG_ALIAS + ".objectid";
    public static final String PROPERTY_TIME = LOG_ALIAS + ".time";
    public static final String PROPERTY_USERNAME = LOG_ALIAS + ".username";
    public static final String PROPERTY_OBJECT_TYPE = LOG_ALIAS + ".objecttype";
    public static final String PROPERTY_PARENT_TYPE = LOG_ALIAS + ".parenttype";
    public static final String PROPERTY_PARENT_ID = LOG_ALIAS + ".parentid";
    public static final String PROPERTY_OPERATION = LOG_ALIAS + ".operation";
    public static final String PROPERTY_SITE = LOG_ALIAS + ".site";
    public static final String PROPERTY_CONTENT = LOG_ALIAS + ".content";

}
