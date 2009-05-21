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
package org.jahia.services.acl;

import org.jahia.exceptions.JahiaException;


public class JahiaACLException extends JahiaException {
    public static final int SERVICE_NULL_INSTANCE = 200;
    public static final int ACL_INVALID_ID = 201;
    public static final int ACL_INVALID_CLASS_NAME = 202;
    public static final int ACL_INVALID_CLASS_ID = 203;
    public static final int ACL_UNKNOWN_ID = 204;
    public static final int ACL_UNKNOWN_CLASS_NAME = 205;
    public static final int ACL_NOT_INITIALIZED = 206;


    //-------------------------------------------------------------------------
    /**
     *
     */
    public JahiaACLException (String message, int code) {
        super ("Internal error in ACL", message, code, JahiaException.ERROR_SEVERITY);
    }

}   // finish class JahiaACLException
