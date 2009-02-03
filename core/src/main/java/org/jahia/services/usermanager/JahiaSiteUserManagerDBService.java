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

package org.jahia.services.usermanager;

import org.jahia.data.JahiaDOMObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaUserManager;
import org.jahia.registries.ServicesRegistry;

import java.util.List;
import java.util.Map;


/**
 * DB implementation of the Manage users memberhip Service in a multi site context
 *
 * @author Khue Ng
 */
public class JahiaSiteUserManagerDBService extends JahiaSiteUserManagerService {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaSiteUserManagerDBService.class);

    private static JahiaSiteUserManagerDBService mInstance;

    public static final String SITE_USERS_CACHE = "SiteUsersCache";
    private JahiaUserManager userManager;

    public void setUserManager(JahiaUserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Default constructor.
     *
     * @throws JahiaException Raise a JahiaException when during initialization
     *                        one of the needed services could not be instanciated.
     */
    protected JahiaSiteUserManagerDBService() throws JahiaException {
    }


    /**
     * Create an new instance of the Site User Manager Service if the instance do not
     * exist, or return the existing instance.
     *
     * @return Return the instance of the Site User Manager Service.
     */
    public static synchronized JahiaSiteUserManagerDBService getInstance() {
        if (mInstance == null) {
            try {
                mInstance = new JahiaSiteUserManagerDBService();
            } catch (JahiaException ex) {
                logger.error(
                        "Could not create an instance of the JahiaSiteUserManagerDBService class");

            }
        }
        return mInstance;
    }

    public void start() {}

    public void stop() {}


    /**
     * Create a new membership for a user on a gived site
     *
     * @param siteID the site identifier
     * @param user the user to add as member
     */
    public synchronized boolean addMember(int siteID, JahiaUser user) throws JahiaException {

        if (user == null) {
            return false;
        }
        return userManager.addMemberToSite(siteID, user);
    }


    /**
     * Remove a user's membership from a site, doesn't delete the user
     *
     * @param siteID the site identifier
     * @param user  reference on the user to be removed from the site.
     */
    public synchronized boolean removeMember(int siteID, JahiaUser user)
            throws JahiaException {

        if (user == null) {
            return false;
        }
        userManager.removeMemberFromSite(siteID,user);
        return true;
    }


    /**
     * Remove a user's membership from all sites, doesn't delete the user
     *
     * @param user the user to be removed from all site.
     */
    public synchronized boolean removeMember(JahiaUser user) throws JahiaException {

        if (user == null) {
            return false;
        }
        userManager.removeMemberFromAllSite(user);
        return true;
    }



    /**
     * This method returns the list of all the usernames of members of a site.
     *
     * @param siteID the site identifier
     * @return Return an Map of username/usrid couples members of this site.
     */
    public Map getMembersMap(int siteID) throws JahiaException {
        return userManager.getAllMembersNameOfSite(siteID);
    }


    /**
     * This method returns the list of all members of this site.
     *
     * @return List of JahiaUser members of this site.
     */
    public List getMembers(int siteID) throws JahiaException {
        return userManager.getAllMembersOfSite(siteID);
    }


    /**
     * Check if a user has or not access on a site.
     *
     * @param int    siteID, the site identifier
     * @param String username, the site identifier
     * @return Return the user if not null.
     */
    public JahiaUser getMember(int siteID, String username) {
        // first look it up directly
        JahiaUser user = userManager.getMemberInSite(siteID,username);

        if (user == null) {
            if (!"".equals(username)) {
                user = ServicesRegistry.getInstance().
                        getJahiaUserManagerService().lookupUser(
                        username);
                if (user instanceof JahiaDBUser) {
                    user = null;
                }
            }
        }
        return user;
    }

    public List<Integer> getUserMembership(JahiaUser user) {
        return userManager.getUserSiteMembership(user);
    }

    /**
     * return a DOM document of all users membership of a site
     *
     * @param int the site id
     * @return JahiaDOMObject a DOM representation of this object
     */
    public JahiaDOMObject getUserMembershipsAsDOM(int siteID)
            throws JahiaException {

        return null;
    }


    /**
     * return a DOM document of external users ( from other site except server admin )
     * that have membership access on this site
     *
     * @param int the site id
     * @return JahiaDOMObject a DOM representation of this object
     */
    public JahiaDOMObject getAuthExternalUsersAsDOM(int siteID)
            throws JahiaException {

        return null;
    }
}
