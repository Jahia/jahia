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
package org.jahia.services.usermanager;

import org.jahia.data.JahiaDOMObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;

import java.util.Map;


/**
 * Manage groups memberhip in a multi site context.
 * Doesn't delete or create a group or user, only handle the association
 * between users, groups and sites
 *
 * @author Khue Ng
 */
public abstract class JahiaSiteGroupManagerService extends JahiaService {


    //-------------------------------------------------------------------------
    /**
     * Create a new membership for a group on a gived site
     *
     * @param int        siteID, the site identifier
     * @param JahiaGroup group, the group to add to a site
     *
     * @author NK
     */
    public abstract boolean addGroup (int siteID, JahiaGroup grp) throws JahiaException;


    //-------------------------------------------------------------------------
    /**
     * Remove a group from a site, doesn't delete the group
     *
     * @param int        siteID, the site identifier
     * @param JahiaGroup grp reference on the group to be removed from the site.
     *
     * @author NK
     */
    public abstract boolean removeGroup (int siteID, JahiaGroup grp) throws JahiaException;


    //-------------------------------------------------------------------------
    /**
     * Remove a group membership from all sites
     *
     * @param JahiaGroup grp, the group to be removed from all sites.
     *
     * @author Khue Ng
     */
    public abstract boolean removeGroup (JahiaGroup grp) throws JahiaException;


    //-------------------------------------------------------------------------
    /**
     * Remove all groups of a site ( only the membership, not the groups )
     *
     * @param int siteID, the identifier of the site.
     *
     * @author Khue Ng
     */
    public abstract boolean removeGroups (int siteID) throws JahiaException;


    //-------------------------------------------------------------------------
    /**
     * This method returns an Map of groupname/grp_id couples of members of the requested site
     *
     * @return Return an Map of groupname/grp_id couples members of this site.
     *
     * @author Khue Ng
     */
    public abstract Map getGroups (int siteID) throws JahiaException;


    //--------------------------------------------------------------------------
    /**
     * return a DOM document of all groups membership of a site
     *
     * @param int the site id
     *
     * @return JahiaDOMObject a DOM representation of this object
     *
     * @author NK
     */
    public abstract JahiaDOMObject getGroupMembershipsAsDOM (int siteID)
            throws JahiaException;


    //--------------------------------------------------------------------------
    /**
     * return a DOM document of external groups ( from other sites )
     * that have membership access on this site
     *
     * @param int the site id
     *
     * @return JahiaDOMObject a DOM representation of this object
     *
     * @author NK
     */
    public abstract JahiaDOMObject getAuthExternalGroupsAsDOM (int siteID)
            throws JahiaException;


}
