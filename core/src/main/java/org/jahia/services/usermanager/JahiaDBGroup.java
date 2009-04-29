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
///////////////////////////////////////////////////////////////////////////////
// FH - 18 Jan. 2001 :
//   1. Made this class compatible with the Group Java interface
// NK - 02 Avr. 2001 :
//   1. Added member type ( USERTYPE or GROUPTYPE ) to support group in group
// NK - 18 Dec. 2001 :
//   1. Added properties to group
//
///////////////////////////////////////////////////////////////////////////////

package org.jahia.services.usermanager;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaGroupManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;

/**
 * This class represents the Jahia native database based user group.
 *
 * @author Fulco Houkes
 * @version 1.0
 */
public class JahiaDBGroup extends JahiaGroup {
    
    private static final transient Logger logger = Logger
            .getLogger(JahiaDBGroup.class);
    
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

    public JahiaDBGroup (int id, String groupname, String groupKey, int siteID,
            Map<String, Principal> members, Properties properties) {
        this(id, groupname, groupKey, siteID, members, properties,
                members != null && !members.isEmpty() ? true : false);
    }    
    
    //-------------------------------------------------------------------------
    /**
     * Instanciate a new JahiaDBGroup object.
     *
     * @param name       Group's identification name.
     * @param attributes Group additional attributes. If this parameter is null then the
     *                   group will have no additional parameters.
     *
     * @throws JahiaException This class need to access the Services Registry and the DB Pool
     *                        Service. If any of this services can't be accessed, a
     *                        JahiaException is thrown.
     * @param	siteID The site id
     */
    public JahiaDBGroup (int id, String groupname, String groupKey, int siteID,
                            Map<String, Principal> members, Properties properties, boolean preloadedGroups) {

        mID = id;
        mGroupname = groupname;
        mGroupKey = groupKey;
        mSiteID = siteID;

        if (preloadedGroups || members != null && members.size() > 0) {
            mMembers = members != null ? (members instanceof ConcurrentHashMap ? members
                    : new ConcurrentHashMap<String, Principal>(members))
                    : new ConcurrentHashMap<String, Principal>();
        }

        if (properties != null) {
            mProperties = properties;
        }
        this.preloadedGroups = preloadedGroups;
    }


    //-------------------------------------------------------------------------
    public boolean addMember (Principal principal) {

        boolean result = false;

        if (principal != null) {
            if (!isMember (principal)) {
                    JahiaGroupManager groupManager = (JahiaGroupManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaGroupManager.class.getName());
                String key = JahiaUserManagerService.getKey(principal);
                groupManager.addMemberToGroup(key,this.getGroupKey(),principal instanceof Group ? mGROUPTYPE : mUSERTYPE);

                    membership.put(key, Boolean.TRUE);
                    if (isPreloadedGroups()) {
                        mMembers.put(key, principal);
                    }
                    result = true;
            }
        }
        return result;
    }


    //-------------------------------------------------------------------------
    /**
     * Retrieve the group's database unique identification number.
     *
     * @return The unique group ID.
     */
    public int getGroupID () {
        return mID;
    }

    //-------------------------------------------------------------------------
    /**
     * Returns the group's home page id.
     * -1 : undefined
     *
     * @return int The group homepage id.
     */
    public int getHomepageID () {

        if (mProperties != null) {

            try {
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

    //-------------------------------------------------------------------------
    /**
     * Set the home page id.
     *
     * @param int The group homepage id.
     *
     * @return false on error
     */
    public boolean setHomepageID (int id) {

        /*
        if ( !removeProperty(mHOMEPAGE_PROP) )
            return false;
        */
        return id != -1 ? setProperty(mHOMEPAGE_PROP, String.valueOf(id))
                : removeProperty(mHOMEPAGE_PROP);
    }

    //--------------------------------------------------------------------------
    public Properties getProperties () {
        return mProperties;
    }


    //--------------------------------------------------------------------------
    public String getProperty (String key) {

        if ((mProperties != null) && (key != null)) {
            return mProperties.getProperty (key);
        }
        return null;
    }

    //--------------------------------------------------------------------------
    /**
     * Remove the specified property from the properties list.
     *
     * @param key Property's name.
     *
     * @return Return true on success or false on any failure.
     */
    public synchronized boolean removeProperty (String key) {
        boolean result = false;

        if ((key != null) && (key.length () > 0)) {
            JahiaGroupManager groupManager = (JahiaGroupManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaGroupManager.class.getName());
            result = groupManager.removeProperty (key, mID, getProviderName (), getGroupKey ());
        }

        if (result) {
            mProperties.remove (key);
        }

        return result;
    }

    //--------------------------------------------------------------------------
    /**
     * Add (or update if not already in the property list) a property key-value
     * pair in the grp's properties list.
     *
     * @param key   Property's name.
     * @param value Property's value.
     *
     * @return Return true on success or false on any failure.
     */
    public synchronized boolean setProperty (String key, String value) {
        boolean result = false;

        if ((key != null) && (value != null)) {

            JahiaGroupManager groupManager = (JahiaGroupManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaGroupManager.class.getName());

            if (getProperty(key) == null) {
                result = groupManager.addProperty(key, value, mID, getProviderName(), getGroupKey());
            } else {
                result = groupManager.updateProperty(key, value, mID, getProviderName(), getGroupKey());
            }

            if (result) {
                mProperties.setProperty(key, value);
                ServicesRegistry.getInstance().getJahiaGroupManagerService().updateCache(this);
            }
        }
        return result;
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
        return ("group" + mID).hashCode ();
    }


    //-------------------------------------------------------------------------
    public boolean removeMember (Principal principal) {

        boolean result = false;
        if (principal != null) {
            JahiaGroupManager groupManager = (JahiaGroupManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaGroupManager.class.getName());
            String memberName = JahiaUserManagerService.getKey(principal);
            if (principal instanceof Group) {
                groupManager.removeMember(memberName, this.getGroupKey(), mGROUPTYPE);
            } else {

                if ((((JahiaUser) principal).isRoot()) &&
                    (getGroupname().equals(JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME))) {
                    return false;
                }
                groupManager.removeMember(memberName, this.getGroupKey(), mUSERTYPE);
            }

            membership.put(memberName, Boolean.FALSE);
            if (mMembers != null) {
                mMembers.remove(memberName);
            }
            result = true;
        }
        return result;
    }


    //--------------------------------------------------------------------------
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

        // Add the user members usernames detail
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

    /**
     * Get the name of the provider of this group.
     *
     * @return String representation of the name of the provider of this group
     */
    public String getProviderName () {
        return JahiaGroupManagerDBProvider.PROVIDER_NAME;
    }

    public boolean isMember(Principal principal) {
        if (super.isMember(principal)) {
            return true;
        }
        if (!preloadedGroups && principal instanceof JahiaUser) {
            boolean result = JahiaGroupManagerDBProvider.getInstance().getUserMembership((JahiaUser)principal).contains(getGroupKey());
            membership.put(JahiaUserManagerService.getKey(principal), Boolean.valueOf(result));
            return result;
        }
        return false;
    }    
    
    @Override
    protected Map<String, Principal> getMembersMap() {
        if (mMembers == null) {
            mMembers = new ConcurrentHashMap<String, Principal>(((JahiaGroupManager) SpringContextSingleton
                    .getInstance().getContext().getBean(
                            JahiaGroupManager.class.getName()))
                    .getGroupMembers(getGroupKey()));
            preloadedGroups = true;
        }
        return mMembers;
    }    
}
