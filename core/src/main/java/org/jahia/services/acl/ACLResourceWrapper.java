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

