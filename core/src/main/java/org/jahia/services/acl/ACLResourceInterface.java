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
//
// NK 24.01.2002 - added in Jahia
//

package org.jahia.services.acl;


/**
 * This interface defines resources that have an ACL.
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public interface ACLResourceInterface {

    //-------------------------------------------------------------------------
    /**
     * Returns the ACL
     *
     * @return Return the ACL.
     */
    public abstract JahiaBaseACL getACL ();

    //-------------------------------------------------------------------------
    /**
     * Returns the acl id
     *
     * @return int the acl id. Return -1 if not found
     */
    public abstract int getAclID ();

}

