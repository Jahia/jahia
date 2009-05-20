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

import org.jahia.services.usermanager.JahiaUser;


/**
 * This is a wrapper to manipulate any resource that implements the ACLResourceInterface.
 *
 * @author Khue Nguyen
 * @version 1.0
 * @see ACLResourceInterface
 */
public final class ACLResourceWrapper {

    // the resource
    private ACLResourceInterface res;


    //--------------------------------------------------------------------------
    public ACLResourceWrapper (ACLResourceInterface res) {
        this.res = res;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the res' ACL
     *
     * @return Return the ACL.
     */
    public JahiaBaseACL getACL () {
        return res.getACL ();
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the res' ACL id
     *
     * @return Return the res' ACL id.
     */
    public int getAclID () {
        int id = -1;
        try {
            id = getACL ().getID ();
        } catch (JahiaACLException ex) {
            // This exception should not happen ... :)
        }
        return id;
    }

    //--------------------------------------------------------------------------
    /**
     * Check if the user has administration access on the specified resource.
     *
     * @param user Reference to the user.
     *
     * @return Return true if the user has admin access for the specified res,
     *         or false in any other case.
     */
    public final boolean checkAdminAccess (JahiaUser user) {
        return ACLResource.checkAdminAccess (null, res, user);
    }


    //--------------------------------------------------------------------------
    /**
     * Check if the user has read access on the specified res.
     *
     * @param user Reference to the user.
     *
     * @return Return true if the user has read access for the specified res,
     *         or false in any other case.
     */
    public final boolean checkReadAccess (JahiaUser user) {
        return ACLResource.checkReadAccess (null, res, user);
    }


    //--------------------------------------------------------------------------
    /**
     * Check if the user has Write access on the specified res.
     *
     * @param user Reference to the user.
     *
     * @return Return true if the user has read access for the specified res,
     *         or false in any other case.
     */
    public final boolean checkWriteAccess (JahiaUser user) {
        return ACLResource.checkWriteAccess (null, res, user);
    }


}

