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
