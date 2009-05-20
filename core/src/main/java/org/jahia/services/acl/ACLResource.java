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

import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;

import java.security.Principal;
import java.security.acl.Group;


/**
 * Holds static methods to manipulate acl rights on any resource
 * that implements the ACLResourceInterface.
 * They apply to a Principal that should be a JahiaUser or JahiaGroup
 *
 * @author Khue Nguyen
 * @version 1.0
 * @see ACLResourceInterface
 */
public final class ACLResource {


    //--------------------------------------------------------------------------
    /**
     * Check if the Principal has a given permission access on the specified resource.
     *
     * @param res  a resource that implements the ACLResourceInterface.
     * @param p    reference to a JahiaUser or JahiaGroup.
     * @param perm the permission.
     *
     * @return Return true if the Principal has a given permission for the specified res,
     *         or false in any other case.
     */
    public static boolean checkPermission (ParentACLFinder parentACLFinder,
                                           ACLResourceInterface res,
                                           Principal p,
                                           int perm) {
        return checkAccess (parentACLFinder, res, p, perm);
    }



    //--------------------------------------------------------------------------
    /**
     * Check if the Principal has administration access on the specified resource.
     *
     * @param res a resource that implements the ACLResourceInterface.
     * @param p   reference to a JahiaUser or JahiaGroup.
     *
     * @return Return true if the Principal has admin access for the specified res,
     *         or false in any other case.
     */
    public static boolean checkAdminAccess (
            ParentACLFinder parentACLFinder,
            ACLResourceInterface res, Principal p) {
        return checkAccess (parentACLFinder, res, p, JahiaBaseACL.ADMIN_RIGHTS);
    }


    //--------------------------------------------------------------------------
    /**
     * Check if the Principal has read access on the specified res.
     *
     * @param res a resource that implements the ACLResourceInterface.
     * @param p   reference to a JahiaUser or JahiaGroup.
     *
     * @return Return true if the Principal has read access for the specified res,
     *         or false in any other case.
     */
    public static boolean checkReadAccess (ParentACLFinder parentACLFinder, ACLResourceInterface res, Principal p) {
        return checkAccess (parentACLFinder, res, p, JahiaBaseACL.READ_RIGHTS);
    }


    //--------------------------------------------------------------------------
    /**
     * Check if the Principal has Write access on the specified res.
     *
     * @param res a resource that implements the ACLResourceInterface.
     * @param p   reference to a JahiaUser or JahiaGroup.
     *
     * @return Return true if the Principal has write access for the specified res,
     *         or false in any other case.
     */
    public static boolean checkWriteAccess (ParentACLFinder parentACLFinder, ACLResourceInterface res, Principal p) {
        return checkAccess (parentACLFinder, res, p, JahiaBaseACL.WRITE_RIGHTS);
    }

    //--------------------------------------------------------------------------
    /**
     * check ACL permissions
     *
     * @param res        the resource
     * @param p          reference to a JahiaUser or JahiaGroup.
     * @param permission the permission
     *
     * @return boolean true if the user has the given permission on this resource
     */
    private static boolean checkAccess (ParentACLFinder parentACLFinder,
                                        ACLResourceInterface res,
                                        Principal p,
                                        int permission) {

        JahiaBaseACL acl;
        if ((p == null) || (res == null) || ((acl = res.getACL ()) == null))
            return false;

        // Test the access rights
        boolean result = false;
        try {
            if (isGroup (p)) {
                result = acl.getPermission (parentACLFinder, res, (JahiaGroup) p, permission);
            } else {
                result = acl.getPermission (parentACLFinder, res, (JahiaUser) p, permission);
            }
        } catch (JahiaACLException ex) {
            // if an error occured, just return false;
        }
        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * returns true if the principal is a Group
     *
     * @param p reference to a JahiaUser or JahiaGroup.
     *
     * @return boolean true if the Principal is a Group
     */
    private static boolean isGroup (Principal p) {

        return (p instanceof Group);
    }

}

