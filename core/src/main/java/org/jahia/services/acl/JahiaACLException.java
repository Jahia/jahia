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
