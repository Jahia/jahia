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
import org.jahia.hibernate.manager.JahiaGroupManager;

import java.util.Map;


/**
 * DB implementation of the Manage groups Grouphip Service in a multi site context
 *
 * @author Khue Ng
 */
public class JahiaSiteGroupManagerDBService extends JahiaSiteGroupManagerService {
    
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaSiteGroupManagerDBService.class);

    private static JahiaSiteGroupManagerDBService mInstance;

    private JahiaGroupManager groupManager;

    public void setGroupManager(JahiaGroupManager groupManager) {
        this.groupManager = groupManager;
    }
    //--------------------------------------------------------------------------
    /**
     * Default constructor.
     *
     * @throws JahiaException Raise a JahiaException when during initialization
     *                        one of the needed services could not be instanciated.
     */
    protected JahiaSiteGroupManagerDBService () throws JahiaException {
    }


    //-------------------------------------------------------------------------
    /**
     * Create an new instance of the Site Group Manager Service if the instance do not
     * exist, or return the existing instance.
     *
     * @return Return the instance of the Site Group Manager Service.
     */
    public static synchronized JahiaSiteGroupManagerDBService getInstance () {
        if (mInstance == null) {
            try {
                mInstance = new JahiaSiteGroupManagerDBService ();
            } catch (JahiaException ex) {
                logger.error (
                        "Could not create an instance of the JahiaSiteGroupManagerDBService class");

            }
        }
        return mInstance;
    }

    public void start() {}

    public void stop() {}

    //-------------------------------------------------------------------------
    /**
     * Create a new association between a group and a site
     *
     * @param siteID the site identifier
     * @param grp the group to add to a site
     *
     */
    public synchronized boolean addGroup (int siteID, JahiaGroup grp) throws JahiaException {

        if (grp == null) {
            return false;
        }
        return groupManager.addGroupToSite(siteID,grp);
    }


    //-------------------------------------------------------------------------
    /**
     * Remove a group's membership from a site
     *
     * @param siteID the site identifier
     * @param grp  reference on the group to be removed from the site.
     *
     */
    public synchronized boolean removeGroup (int siteID, JahiaGroup grp) throws JahiaException {

        if (grp == null) {
            return false;
        }
        groupManager.removeGroupFromSite(siteID,grp);
        return true;
    }


    //-------------------------------------------------------------------------
    /**
     * Remove a group's membership from all sites, doesn't delete the group
     *
     * @param grp the group to be removed from the site.
     *
     */
    public synchronized boolean removeGroup (JahiaGroup grp) throws JahiaException {

        if (grp == null) {
            return false;
        }
        groupManager.removeGroupFromAllSites(grp);
        return true;
    }


    //-------------------------------------------------------------------------
    /**
     * Remove all groups of a site ( only the membership, not the groups )
     *
     * @param siteID the identifier of the site.
     *
     */
    public synchronized boolean removeGroups (int siteID) throws JahiaException {
        groupManager.removeAllGroupsFromSite(siteID);
        return true;
    }


    //-------------------------------------------------------------------------
    /**
     * This method returns the list of all the groupnames of a site.
     *
     * @return Return an Map of groupname/grpid couples members of this site.
     *
     */
    public Map getGroups (int siteID) throws JahiaException {
        return groupManager.getGroupsInSite(siteID);
    }


    //--------------------------------------------------------------------------
    /**
     * return a DOM document of all groups membership of a site
     *
     * @param siteID the site id
     *
     * @return JahiaDOMObject a DOM representation of this object
     *
     */
    public JahiaDOMObject getGroupMembershipsAsDOM (int siteID)
            throws JahiaException {

        return null;
    }


    //--------------------------------------------------------------------------
    /**
     * return a DOM document of external groups ( from other sites )
     * that have membership access on this site
     *
     * @param siteID the site id
     *
     * @return JahiaDOMObject a DOM representation of this object
     *
     */
    public JahiaDOMObject getAuthExternalGroupsAsDOM (int siteID)
            throws JahiaException {

        return null;
    }

}
