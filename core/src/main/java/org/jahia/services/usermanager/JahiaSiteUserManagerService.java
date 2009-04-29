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
package org.jahia.services.usermanager;

import org.jahia.data.JahiaDOMObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;

import java.security.Principal;
import java.util.List;
import java.util.Map;


/**
 * Manage users memberhip in a multi site context
 *
 * @author Khue Ng
 */
public abstract class JahiaSiteUserManagerService extends JahiaService {


    /**
     * Create a new membership for a user on a gived site
     *
     * @param siteID the site identifier
     * @param user the user to add as member
     */
    public abstract boolean addMember (int siteID, JahiaUser user) throws JahiaException;


    /**
     * Remove a user's membership from a site, doesn't delete the user
     *
     * @param siteID the site identifier
     * @param user  reference on the user to be removed from the site.
     */
    public abstract boolean removeMember (int siteID, JahiaUser user) throws JahiaException;


    /**
     * Remove a user's membership from all sites, doesn't delete the user
     *
     * @param user the user to be removed from the site.
     */
    public abstract boolean removeMember (JahiaUser user) throws JahiaException;


    /**
     * This method returns an Map of username/usr_id couples of members of the requested site
     *
     * @param siteID the site identifier
     *
     * @return Return an Map of username/usrid couples members of this site.
     */
    public abstract Map getMembersMap (int siteID) throws JahiaException;


    /**
     * This method returns the list of all members of this site.
     *
     * @param siteID the site identifier
     *
     * @return List of members of this site.
     */
    public abstract List<Principal> getMembers (int siteID) throws JahiaException;


    /**
     * Check if a user has or not access on a site.
     *
     * @param siteID the site identifier
     * @param username the site identifier
     *
     * @return Return the user if not null.
     */
    public abstract JahiaUser getMember (int siteID, String username);

    public abstract List<Integer> getUserMembership (JahiaUser user);


    /**
     * return a DOM document of all users membership of a site
     *
     * @param siteID    the site id
     *
     * @return JahiaDOMObject a DOM representation of this object
     */
    public abstract JahiaDOMObject getUserMembershipsAsDOM (int siteID)
            throws JahiaException;


    /**
     * return a DOM document of external users ( from other site )
     * that have membership access on this site
     *
     * @param siteID the site id
     *
     * @return JahiaDOMObject a DOM representation of this object
     */
    public abstract JahiaDOMObject getAuthExternalUsersAsDOM (int siteID)
            throws JahiaException;


}
