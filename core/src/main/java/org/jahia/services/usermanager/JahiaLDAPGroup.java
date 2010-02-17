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

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaGroupManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author Viceic Predrag <Predrag.Viceic@ci.unil.ch>
 * @version 1.0
 */

public class JahiaLDAPGroup extends JahiaGroup {
    
    private static final transient Logger logger = Logger
            .getLogger(JahiaLDAPGroup.class);

    /** Group's unique identification number */
    protected int mID;

    /** User Member type designation * */
    protected static int mUSERTYPE = 1;

    /** Group Member type designation * */
    protected static int mGROUPTYPE = 2;

    /** Group home page property * */
    private static final String mHOMEPAGE_PROP = "group_homepage";

    /** Group additional parameters. */
    private Properties mProperties = new Properties ();

    // LDAP dynamic group (groupOfURLs)
    private boolean dynamic;

    private JahiaGroupManagerLDAPProvider myProvider;

    /**
     * Instanciate a new JahiaDBGroup object.
     *
     * @throws JahiaException This class need to access the Services Registry and the DB Pool
     *                        Service. If any of this services can't be accessed, a
     *                        JahiaException is thrown.
     * @param	siteID The site id
     * @param dynamic
     */
    protected JahiaLDAPGroup (int id, String groupname, String groupKey, int siteID,
                              Map<String, Principal> members, Properties properties, boolean dynamic, boolean preloadedGroups, JahiaGroupManagerLDAPProvider provider)
            throws JahiaException {
        ServicesRegistry registry = ServicesRegistry.getInstance ();
        if (registry == null) {
            throw new JahiaException ("Jahia Internal Error",
                    "JahiaLDAPGroup Could not get the Service Registry instance",
                    JahiaException.SERVICE_ERROR, JahiaException.CRITICAL_SEVERITY);
        }

        mID = id;
        mGroupname = groupname;
        mGroupKey ="{"+provider.getKey()+"}"+ groupKey;
        mSiteID = siteID;

        if (preloadedGroups || members != null && members.size() >  0) {
            mMembers = members != null ? (members instanceof ConcurrentHashMap ? members
                    : new ConcurrentHashMap<String, Principal>(members))
                    : new ConcurrentHashMap<String, Principal>();
        }

        if (properties != null) {
            mProperties = properties;
        }
        this.dynamic = dynamic;
        this.preloadedGroups = preloadedGroups;
        this.myProvider = provider;        
    }


    /**
     * Returns the group's home page id.
     * -1 : undefined
     *
     * @return int The group homepage id.
     */

    public int getHomepageID () {
        if (mProperties != null) {

            try {
                // Get the home page from the Jahia DB.
                // By default an external group is represented with a -1 group ID.
                String value = mProperties.getProperty (mHOMEPAGE_PROP);

                if (value == null)
                    return -1;
                return Integer.parseInt (value);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return -1;

    }

    public boolean setHomepageID (int id) {
        if (!removeProperty (mHOMEPAGE_PROP))
            return false;
        // Set the home page into the Jahia DB.
        // By default an external group is represented with a -1 group ID.
        return setProperty (mHOMEPAGE_PROP, String.valueOf (id));

    }

    public synchronized boolean removeProperty (String key) {
        boolean result = false;

        if ((key != null) && (key.length () > 0)) {
            // Remove these lines if LDAP problem --------------------
            JahiaGroupManager groupManager = (JahiaGroupManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaGroupManager.class.getName());
            result = groupManager.removeProperty (key, -1, getProviderName (), getGroupKey ());
        }

        if (result) {
            mProperties.remove (key);
        }
        // End remove --------------------
        return result;

    }

    public Properties getProperties () {
        return mProperties;
    }

    public boolean removeMember (Principal user) {
        /**@todo Must check this*/
        return false;
    }

    public String getProperty (String key) {
        if ((mProperties != null) && (key != null)) {
            return mProperties.getProperty (key);
        }
        return null;

    }

    public boolean addMember (Principal user) {
        /**@todo Must check this*/
        return false;
    }

    public synchronized boolean setProperty (String key, String value) {
        boolean result = false;

        if ((key != null) && (value != null)) {
            // Remove these lines if LDAP problem --------------------
            JahiaGroupManager groupManager = (JahiaGroupManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaGroupManager.class.getName());
            if (getProperty(key) == null) {
                result = groupManager.addProperty(key, value, -1, getProviderName(), getGroupKey());
            } else {
                result = groupManager.updateProperty(key, value, -1, getProviderName(), getGroupKey());
            }

            // End remove --------------------
            if (result) {
                mProperties.setProperty(key, value);
            }
        }
        return result;

    }

    public String getProviderName () {
        return JahiaGroupManagerLDAPProvider.PROVIDER_NAME;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public String toString () {

        StringBuffer output = new StringBuffer ("Details of group [" + mGroupname + "] :\n");

        output.append ("  - ID : " + Integer.toString (mID) + "\n");

        output.append ("  - properties :");

        Iterator names = new EnumerationIterator(mProperties.propertyNames ());
        String name;
        if (names.hasNext ()) {
            output.append ("\n");
            while (names.hasNext ()) {
                name = (String) names.next ();
                output.append (
                        "       " + name + " -> [" + (String) mProperties.getProperty (name) + "]\n");
            }
        } else {
            output.append (" -no properties-\n");
        }

        // Add the user members useranames detail
        output.append ("  - members : ");
        
        if (mMembers != null) {
            if (mMembers.size() > 0) {
                for (String member : mMembers.keySet()) {
                    output.append (member + "/");
                }
            } else {
                output.append (" -no members-\n");
            }
        } else {
            output.append (" -preloading of members disabled-\n");
        }

        return output.toString ();
    }

    public boolean equals (Object another) {
        if (this == another) return true;

        if (another != null && this.getClass() == another.getClass()) {
            return (getGroupKey().equals(((JahiaGroup) another).getGroupKey()));
        }
        return false;
    }

    //-------------------------------------------------------------------------
    public int hashCode () {
        return mID;
    }


    public void setSiteID (int id) {
        mSiteID = id;
    }

   public boolean isMember(Principal principal) {
       Principal user = principal;
       if (!(user instanceof JahiaLDAPUser) && !(user instanceof JahiaLDAPGroup)) {
           return false;
       }
        if (super.isMember(user)) {
            return true;
        }
        if (!preloadedGroups && user instanceof JahiaUser) {
            boolean result = myProvider.getUserMembership((JahiaUser)principal).contains(getGroupKey());
            membership.put(JahiaUserManagerService.getKey(principal), Boolean.valueOf(result));
            return result;
        }
        return false;
    }

   @Override
    protected Map<String, Principal> getMembersMap() {
        if (mMembers == null) {
            mMembers = new ConcurrentHashMap<String, Principal>(myProvider.getGroupMembers(getGroupname(), isDynamic()));
            preloadedGroups = true;
        }
        return mMembers;
    }
}
