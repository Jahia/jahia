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
    public abstract Map<String, String> getGroups (int siteID) throws JahiaException;


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
