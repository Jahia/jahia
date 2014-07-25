/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
// NK - 18 Dec. 2001 :
//   1. Added properties to group

package org.jahia.services.usermanager;

import java.util.*;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.usermanager.jcr.JCRGroupManagerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;


public class JahiaGroupManagerService extends JahiaService {

    private static Logger logger = LoggerFactory.getLogger(JahiaGroupManagerService.class);

    public static final String USERS_GROUPNAME = "users";
    public static final String ADMINISTRATORS_GROUPNAME = "administrators";
    public static final String PRIVILEGED_GROUPNAME = "privileged";
    public static final String SITE_PRIVILEGED_GROUPNAME = "site-privileged";
    public static final String SITE_ADMINISTRATORS_GROUPNAME = "site-administrators";
    public static final String GUEST_GROUPNAME = "guest";
    public static final String GUEST_GROUPPATH = "/groups/"+GUEST_GROUPNAME;
    public static final String USERS_GROUPPATH = "/groups/"+USERS_GROUPNAME;
    
    public static final Set<String> POWERFUL_GROUPS = new HashSet<String>(Arrays.asList(
            ADMINISTRATORS_GROUPNAME, SITE_ADMINISTRATORS_GROUPNAME, PRIVILEGED_GROUPNAME,
            SITE_PRIVILEGED_GROUPNAME));
    private JCRGroupManagerProvider defaultProvider;
    private List<String> jahiaJcrEnforcedGroups;

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final JahiaGroupManagerService INSTANCE = new JahiaGroupManagerService();
    }

    public static JahiaGroupManagerService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Create a new group in the system.
     *
     * @param hidden
     * @return a reference on a group object on success, or if the groupname
     *         already exists or another error occured, null is returned.
     */
    public JCRGroupNode createGroup(String siteKey, String name, Properties properties, boolean hidden){
        return defaultProvider.createGroup(siteKey, name, properties, hidden);
    }

    //-------------------------------------------------------------------------
    /**
     * Delete a group from the system. Updates the database automatically, and
     * signal the ACL Manager that the group no longer exists.
     *
     * @param group Reference to a JahiaGroup object.
     *
     * @return Return true on success, or false on any failure.
     */
    public boolean deleteGroup (String groupPath){
        return defaultProvider.deleteGroup(groupPath);
    }
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    /**
     *
     */
    public JCRGroupNode getAdministratorGroup (String siteKey) throws RepositoryException {
        return defaultProvider.getAmdinistratorGroup(siteKey);
    }

    //-------------------------------------------------------------------------
    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group keys of a site.
     *
     * @param int the site id
     *
     * @return Return a List of identifier of all groups of this site.
     *
     * @auhtor NK
     */
    public List<String> getGroupList (){
        return defaultProvider.getGroupList();
    }

    public List<String> getGroupList (String siteKey){
        return defaultProvider.getGroupList(siteKey);
    }

    //-------------------------------------------------------------------------
    /**
     * Return a <code>List</code) of <code>String</code> representing all the
     * group names.
     *
     * @return Return a List of strings containing all the group names.
     */
    public List<String> getGroupnameList (){
        return defaultProvider.getGroupnameList();
    }

    //-------------------------------------------------------------------------
    /**
     * Return the list of groups to shich the specified user has access.
     *
     * @param user Valid reference on an existing group.
     *
     * @return Return a List of strings holding all the group names to
     *         which the user as access.
     */
    public List<String> getUserMembership (String userPath) {
        try {
            return defaultProvider.getUserMembership(userPath);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    //-------------------------------------------------------------------------
    /**
     * This function checks on a gived site if the groupname has already been
     * assigned to another group.
     *
     * @param int       siteKey the site id
     * @param groupname String representing the unique group name.
     *
     * @return Return true if the specified username has not been assigned yet,
     *         return false on any failure.
     */
    public boolean groupExists (String siteKey, String name){
        try {
            return defaultProvider.lookupGroup(getGroupPath(siteKey, name))!=null;
        } catch (RepositoryException e) {
            return false;
        }
    }

    public boolean groupExists (String groupPath){
        try {
            return defaultProvider.lookupGroup(groupPath)!=null;
        } catch (RepositoryException e) {
            return false;
        }
    }

    private String getGroupPath(String siteKey, String name) {
        return (siteKey == null ? "/groups" : "/sites/" + siteKey + "/groups") + "/" + name;
    }

    //-------------------------------------------------------------------------
    /**
     * Lookup the group information from the underlaying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cahce from the database.
     *
     * @param String groupID Group's unique identification id.
     *
     * @return Return a reference on a the specified group name. Return null
     *         if the group doesn't exist or when any error occurred.
     */
    public JCRGroupNode lookupGroup (String groupPath) {
        try {
            return defaultProvider.lookupGroup(groupPath);
        } catch (RepositoryException e) {
            return null;
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Lookup the group information from the underlaying system (DB, LDAP, ... )
     * Try to lookup the group into the cache, if it's not in the cache, then
     * load it into the cahce from the database.
     *
     * @param int       siteKey the site id
     * @param groupname Group's unique identification name.
     *
     * @return Return a reference on a the specified group name. Return null
     *         if the group doesn't exist or when any error occured.
     */
    public JCRGroupNode lookupGroup (String siteKey, String name){
        try {
            return defaultProvider.lookupGroup(getGroupPath(siteKey, name));
        } catch (RepositoryException e) {
            return null;
        }
    }

    /**
     * Find groups according to a table of name=value properties. If the left
     * side value is "*" for a property then it will be tested against all the
     * properties. ie *=test* will match every property that starts with "test"
     *
     * @param siteKey         site identifier
     * @param searchCriterias a Properties object that contains search criterias
     *                        in the format name,value (for example "*"="*" or "groupname"="*test*") or
     *                        null to search without criterias
     *
     * @return List a List of JahiaGroup elements that correspond to those
     *         search criterias
     */
    public Set<JCRGroupNode> searchGroups (String siteKey, Properties searchCriterias){
        return defaultProvider.searchGroups(siteKey, searchCriterias);
    }
    
    /**
     * Validates provided group name against a regular expression pattern, specified in the Jahia configuration.
     * 
     * @param name
     *            the group name to be validated
     * @return <code>true</code> if the specified group name matches the validation pattern
     */
    public boolean isGroupNameSyntaxCorrect(String name){
        return defaultProvider.isGroupNameSyntaxCorrect(name);
    }

    public void setDefaultProvider(JCRGroupManagerProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public void setJahiaJcrEnforcedGroups(List<String> jahiaJcrEnforcedGroups) {
        this.jahiaJcrEnforcedGroups = jahiaJcrEnforcedGroups;
    }

    public List getJahiaJcrEnforcedGroups() {
        return jahiaJcrEnforcedGroups;
    }

    @Override
    public void start() throws JahiaInitializationException {

    }

    @Override
    public void stop() throws JahiaException {

    }
}
