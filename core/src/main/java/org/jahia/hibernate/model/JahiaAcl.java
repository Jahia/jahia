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
 package org.jahia.hibernate.model;

import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaAclManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLInfo;
import org.jahia.services.acl.ACLResourceInterface;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.acl.ParentACLFinder;
import org.jahia.services.cache.Cache;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.springframework.beans.BeansException;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * @hibernate.class table="jahia_acl" lazy="false"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaAcl implements Serializable,Cloneable, ParentACLFinder, ACLResourceInterface,ACLInfo {

    private static final long serialVersionUID = -1154328896217458051L;

// ------------------------------ FIELDS ------------------------------

    /**
     * identifier field
     */
    private Integer id;

    /**
     * nullable persistent field
     */
    private Integer inheritance;

    private transient static Logger logger = Logger.getLogger (JahiaAcl.class);
    private transient JahiaBaseACL mAcl = null;

    private Collection<JahiaAclEntry> entries;

    private Integer hasEntries;

    private Map<String, JahiaAclEntry> groupEntries;

    /**
     * persistent field
     */
    private Map<String, JahiaAclEntry> userEntries;

    private transient Cache<String, Object> permissions;
    public static final String JAHIA_ACL_PERMISSIONS_CACHE = "JahiaAclPermissionsCache";

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * default constructor
     */
    public JahiaAcl() {
        entries = new HashSet<JahiaAclEntry>(11);
        hasEntries = new Integer(0);
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * @hibernate.set lazy="true"
     * inverse="true"
     * cascade="delete"
     * @hibernate.collection-key column="id_jahia_acl"
     * @hibernate.collection-one-to-many class="org.jahia.hibernate.model.JahiaAclEntry"
     */
    public Collection<JahiaAclEntry> getEntries() {
        if (hasEntries != null && hasEntries.intValue()==0 && pickedAclId == null) {
            this.groupEntries = new HashMap<String, JahiaAclEntry>();
            this.userEntries = new HashMap<String, JahiaAclEntry>();
            return new HashSet<JahiaAclEntry>();
        }
        if (userEntries == null) {
            this.groupEntries = new HashMap<String, JahiaAclEntry>();
            this.userEntries = new HashMap<String, JahiaAclEntry>();
            Collection<JahiaAclEntry> loadedEntries = getEntries();
            for (JahiaAclEntry ace : loadedEntries) {
                if (ace.getComp_id().getType().intValue() == 1) {
                    userEntries.put(ace.getComp_id().getTarget(),ace);
                } else {
                    groupEntries.put(ace.getComp_id().getTarget(),ace);
                }
            }
            return loadedEntries;
        }
        return entries;
    }

    public void setEntries(Collection<JahiaAclEntry> entries) {
        this.entries = entries;
    }

    public Map<String, JahiaAclEntry> getUserEntries() {
        getEntries();
        return userEntries;
    }

    public void setUserEntries(Map<String, JahiaAclEntry> userEntries) {
        getEntries().removeAll(this.userEntries.values());
        if (!userEntries.isEmpty()) {
            setHasEntries(new Integer(1));
        }
        getEntries().addAll(userEntries.values());
        if (entries.isEmpty()) {
            setHasEntries(new Integer(0));
        }
        this.userEntries = userEntries;
    }

    public Map<String, JahiaAclEntry> getGroupEntries() {
        getEntries();
        return groupEntries;
    }

    public void setGroupEntries(Map<String, JahiaAclEntry> groupEntries) {
        getEntries().removeAll(this.groupEntries.values());
        if (!groupEntries.isEmpty()) {
            setHasEntries(new Integer(1));
        }
        getEntries().addAll(groupEntries.values());
        if (entries.isEmpty()) {
            setHasEntries(new Integer(0));
        }
        this.groupEntries = groupEntries;
    }

    /**
     * @hibernate.id generator-class="assigned"
     * type="java.lang.Integer"
     * column="id_jahia_acl"
     *
     */
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @hibernate.property column="inheritance_jahia_acl"
     * length="11"
     */
    public Integer getInheritance() {
        return this.inheritance;
    }

    public boolean setInheritance(Integer inheritance) {
        this.inheritance = inheritance;
        return true;
    }

    /**
     * @hibernate.property column="hasentries_jahia_acl"
     */
    public Integer getHasEntries() {
        return hasEntries;
    }

    public void setHasEntries(Integer hasEntries) {
        this.hasEntries = hasEntries;
    }

    private transient JahiaAcl parent;
    private transient JahiaAcl pickedAcl;

    private Integer parentId;
    private Integer pickedAclId;

    /**
     * @hibernate.property column="parent_id_jahia_acl"
     */
    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    /**
     * @hibernate.property column="picked_id_jahia_acl"
     */
    public Integer getPickedAclId() {
        return pickedAclId;
    }

    public void setPickedAclId(Integer pickedAclId) {
        this.pickedAclId = pickedAclId;
    }

    public JahiaAcl getParent() {
        if (parentId == null) {
            return null;
        }
        if (parent == null) {
            try {
                parent = ((JahiaAclManager)SpringContextSingleton.getInstance().getContext().getBean(JahiaAclManager.class.getName())).findJahiaAclById(""+getParentId());
            } catch (BeansException e) {
                logger.error("Error we cannot find the ACL Manager",e);
            } catch(ObjectRetrievalFailureException e) {
                parent = null;
                logger.warn("We cannot find the parent acl for this acl, current acl id = "+id+" parentid = "+parentId,e);
            }
        }
        return parent;
    }

    public void setParent(JahiaAcl parent) {
        this.parent = parent;
        if (parent == null) {
            setParentId(null);
        } else {
            setParentId(parent.getId());
        }
    }

    public JahiaAcl getPickedAcl() {
        if (pickedAclId == null) {
            return null;
        }
        if (pickedAcl == null) {
            try {
                pickedAcl = ((JahiaAclManager)SpringContextSingleton.getInstance().getContext().getBean(JahiaAclManager.class.getName())).findJahiaAclById(""+getPickedAclId());
            } catch (BeansException e) {
                logger.error("Error we cannot find the ACL Manager",e);
            } catch(ObjectRetrievalFailureException e) {
                pickedAcl = null;
                logger.warn("We cannot find the parent acl for this acl, current acl id = "+id+" parentid = "+pickedAclId,e);
            }
        }
        return pickedAcl;
    }

    public void setPickedAcl(JahiaAcl pickedAcl) {
        this.pickedAcl = pickedAcl;
        if (pickedAcl == null) {
            setPickedAclId(null);
        } else {
            setPickedAclId(pickedAcl.getId());
        }
    }

// ------------------------ CANONICAL METHODS ------------------------

    public Object clone() {
        final JahiaAcl acl = new JahiaAcl();
        acl.setParentId(parentId);
        acl.setPickedAclId(pickedAclId);
        acl.setInheritance(inheritance);
        acl.setId(id);
        acl.setEntries(new HashSet<JahiaAclEntry>(getEntries()));
        acl.setHasEntries(getHasEntries());
        return acl;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaAcl castOther = (JahiaAcl) obj;
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .toHashCode();
    }

    public String toString() {
        return new StringBuffer(getClass().getName())
                .append("id=").append(getId())
                .toString();
    }

// -------------------------- OTHER METHODS --------------------------

    public boolean clearEntries(int userTypeEntry) {
        if (getEntries().isEmpty()) {
            return true;
        }
        if (userTypeEntry == ACLInfo.USER_TYPE_ENTRY) {
            entries.removeAll(getUserEntries().values());
            userEntries.clear();
        } else if (userTypeEntry == ACLInfo.GROUP_TYPE_ENTRY) {
            entries.removeAll(getGroupEntries().values());
            groupEntries.clear();
        }
        if (entries.isEmpty()) {
            setHasEntries(new Integer(0));
        }
        return true;
    }

    /**
     * Return all the group names present in the ACL object having the same
     * rights as specified.
     *
     * @param entry Access rights bits map. Set this parameter to null to get all the
     *              group names regarding their access rights.
     *
     * @return Return a List holding all the String representation of the
     *         groups' usernames. The returned List is never null, but if no
     *         group is present in the ACL, it will be empty.
     */
    public List<String> getGroupnameList (JahiaAclEntry entry) {
        Map<String, JahiaAclEntry> table = getRecursedPermissions (null, null) [1];
        return getNameList (table, entry);
    }

    /**
     * Return all the group names present in the ACL object having the same
     * rights as specified, but not the Administrator group
     *
     * @param entry Access rights bits map. Set this parameter to null to get all the
     *              group names regarding their access rights.
     *
     * @return Return a List holding all the String representation of the
     *         groupnames. The returned List is never null, but if no group is
     *         present in the ACL, it will be empty.
     */
    public List<String> getGroupnameListNoAdmin (JahiaAclEntry entry) {
        List<String> result = getGroupnameList (entry);
        for (int i = 0; i < result.size (); i++) {
            String adminGroup = (String) result.get (i);
            if (adminGroup.indexOf (JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME) == 0)
                result.remove(i--);
        }
        return result;
    }

    /**
     * Check the permission of a given user recursively from the acl tree.
     *
     * @param user       Reference to a non-null user object.
     * @param permission Bit index of the requested access, this index should be defined as
     *                   a constant in the derived classes.
     *
     * @return True if the specified user has the requested rights in the ACL,
     *         or in one of the parent's ACLs.
     *
     * @todo This code has to be reviewed
     */
    public boolean getPermission(JahiaUser user, int permission) {
        int contextSiteID = getContextSiteID(user);
        return getPermission(null, null, user, permission, true, contextSiteID);
    }

    private void checkCache() {
        if(permissions == null)
        permissions = ServicesRegistry.getInstance().getCacheService().getCache(JAHIA_ACL_PERMISSIONS_CACHE);
        if(permissions==null)
            try {
                permissions = ServicesRegistry.getInstance().getCacheService().createCacheInstance(JAHIA_ACL_PERMISSIONS_CACHE);
            } catch (JahiaInitializationException e) {
                logger.error(e.getMessage(), e);
            }
    }

    /**
     * Check the permission of a given user recursively from the acl tree.
     *
     * @param user       Reference to a non-null user object.
     * @param permission Bit index of the requested access, this index should be defined as
     *                   a constant in the derived classes.
     * @param siteAdminsHaveAllRights if true, then site administrators can bypassed all the
     * rights and have access to all objects. For content objects this is the desired behavior,
     * but for UI actions this might not be what is desired.
     *
     * @return True if the specified user has the requested rights in the ACL,
     *         or in one of the parent's ACLs.
     *
     * @todo This code has to be reviewed
     */
    public boolean getPermission (ParentACLFinder parentACLFinder,
                                  ACLResourceInterface aclResource,
                                  JahiaUser user,
                                  int permission,
                                  boolean siteAdminsHaveAllRights,
                                  int objectSiteID) {

        return getUserPermission(parentACLFinder,
                                  aclResource,
                                  user,
                                  permission,
                                  siteAdminsHaveAllRights,
                                  objectSiteID);
    }

    protected boolean getUserPermission (
                                    ParentACLFinder parentACLFinder,
                                    ACLResourceInterface aclResource,
                                    JahiaUser user,
                                    int permission,
                                    boolean siteAdminsHaveAllRights,
                                    int objectSiteID) {


        int result = -1;

        if (user == null) {
            return false;
        }
        checkCache();
//        else return true;
        String key = "" + user.getUserKey() + "_" + permission + getKey() + objectSiteID+siteAdminsHaveAllRights;
        Boolean v = (Boolean) permissions.get(key);
        if (v != null) {
            return v.booleanValue();
        }
        try {

            if (siteAdminsHaveAllRights) {
            // First check if the user is member of the administrators group.
            // if it's the case, then give the user all the rights.

                if (objectSiteID != -1) {

                    JahiaGroup siteAdminGroup = ServicesRegistry.getInstance ()
                            .getJahiaGroupManagerService ().getAdministratorGroup (objectSiteID);
                    // FIXME_MULTISITE Hollis , the administrator grp is deducted from user.getSiteID() problem here ?
                    if (siteAdminGroup != null) {
                        if (siteAdminGroup.isMember (user)) {
                            result = 1;
                        }
                    }
                    // only for GC purposes
                    siteAdminGroup = null;
                } else {
                    logger.warn("User comes from LDAP, so we can test it's apartanence to the administrators group when we have no site ID");
                }
            }

            // now let's check if the user if part of the server administrators group
            JahiaGroup serverAdminGroup = ServicesRegistry.getInstance ()
                    .getJahiaGroupManagerService ().getAdministratorGroup (0);
            if (serverAdminGroup != null) {
                if (serverAdminGroup.isMember (user)) {
                    result = 1;
                }
            }
            // only for GC purposes
            serverAdminGroup = null;

            //logger.debug("step1 result=" + result);

            if (result == -1) {

                // Compute recursively the user and group permission according to
                // the ACL entries flags.
                Map<String, JahiaAclEntry>[] m = getRecursedPermissions (parentACLFinder, aclResource);
                Map<String, JahiaAclEntry> userTable = m[0];
                Map<String, JahiaAclEntry> groupTable = m[1];


                // Check if the user is explicitely mentioned in the ACL, look up
                // recursively the user in the tree.
                result = getUserPermissionInHashMap (user, permission, userTable);

                //logger.debug("step2 result=" + result);

                if (result == -1) {
                    // seems the user has not been found directly in the ACL tree,
                    // check now if the user is member of one of the specified group
                    // in the ACL.
                    result = getUserPermissionInGroupHashMap (user, permission, groupTable);
                }

                //logger.debug("step3 result=" + result);

                userTable = null;
                groupTable = null;
            }
        } catch (NullPointerException ex) {
            result = 0;
        }

        // return true only if one of the groups in the ACL tree has the
        // requested permission and if the user is member of this group.
        boolean b = (result == 1);
        if (b)
            permissions.put(key, Boolean.TRUE);
        else permissions.put(key, Boolean.FALSE);
        return b;
    }

    /**
     * Check the permission of a given group recursively from the acl tree.
     *
     * @param group      Reference to a non-null group object.
     * @param permission Bit index of the requested access, this index should be defined as
     *                   a constant in the derived classes.
     *
     * @return True if the specified group has the requested rights in the ACL,
     *         or in one of the parent's ACLs.
     */
    public boolean getPermission (JahiaGroup group, int permission) {
        return getPermission(null, null, group, permission);
    }

    /**
     * Return all the user names present in the ACL object having the same
     * rights as specified.
     *
     * @param entry Access rights bits map. Set this parameter to null to get
     *              all the user names regarding their access rights.
     *
     * @return Return a List holding all the String representation of the
     *         users' usernames. The returned List is never null, but if no
     *         user is present in the ACL, it will be empty.
     */
    public List<String> getUsernameList(JahiaAclEntry entry) {
        Map<String, JahiaAclEntry> table = getRecursedPermissions(null, null) [0];
        return getNameList(table, entry);
    }

    /**
     * Return all the user names present in the ACL object having the same
     * rights as specified, including users members of groups having the same
     * rights as specified.
     *
     * @param entry Access rights bits map. Set this parameter to null to get all the
     *              user names regarding their access rights. Only one bit of the entry
     *              should be set to ACL_YES!
     *
     * @return Return a List holding all the String representation of the users'
     *         usernames. The returned List is never null, but if no user is
     *         present in the ACL, it will be empty.
     */
    public List<String> getUsernameListAlsoGroupUsers (JahiaAclEntry entry) {
        return getUsernameListAlsoGroupUsers(entry, false);
    }
    
    /**
     * Return all the user names present in the ACL object having the same
     * rights as specified, including users members of groups having the same
     * rights as specified.
     *
     * @param entry Access rights bits map. Set this parameter to null to get all the
     *              user names regarding their access rights. Only one bit of the entry
     *              should be set to ACL_YES!
     * @param ignoreGuestAndUsersGroup set to "true" if user members of the UserGroup and GuestGroup should
     *              be ignored                            
     *
     * @return Return a List holding all the String representation of the users'
     *         usernames. The returned List is never null, but if no user is
     *         present in the ACL, it will be empty.
     */
    public List<String> getUsernameListAlsoGroupUsers (JahiaAclEntry entry, boolean ignoreGuestAndUsersGroup) {        
        Map<String, JahiaAclEntry>[] m = getRecursedPermissions (null, null);
        Map<String, JahiaAclEntry> userTable = m[0];
        Map<String, JahiaAclEntry> groupTable = m[1];

        Set<String> result = new HashSet<String>(getNameList (userTable, entry));
        if (logger.isDebugEnabled()) {
            for (Iterator<String> iterator = result.iterator(); iterator.hasNext();) {
                String name = iterator.next();
                logger.debug("Name=" + name);
                if (getParent() != null) {
                    JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(name);
                    JahiaAclEntry parentEntry = getParent().getUserEntries().get(user.getUserKey());
                    if (parentEntry != null) {
                        logger.debug("parentAclEntry=" + parentEntry.toString());
                    }
                }
            }
        }
        List<String> groupList = getNameList (groupTable, entry);

        // add in the result all users from every group that also have this
        // access except for users that have excplicitely a NO access
        for (int i = 0; i < groupList.size (); i++) {
            String groupname = (String) groupList.get(i);
            JahiaGroup group = ServicesRegistry.getInstance ().
                    getJahiaGroupManagerService ().lookupGroup (groupname);
            if (group == null) {
                logger.warn("Group " + groupname + " not found in manager service but present in ACL " + getId());
                continue;
            }
            
            Enumeration<Principal> membersEnum = ignoreGuestAndUsersGroup
                    && (JahiaGroupManagerService.GUEST_GROUPNAME.equals(group
                            .getGroupname()) || JahiaGroupManagerService.USERS_GROUPNAME
                            .equals(group.getGroupname())) ? null : group.members();

            if (membersEnum != null) {
                Principal p;

                while (membersEnum.hasMoreElements()) {
                    p = membersEnum.nextElement();
                    if (!(p instanceof Group)) {
                        JahiaUser user = (JahiaUser) p;
                        if ((user != null) && (!result.contains (user.getUserKey ()))) {
                            // check if the user has access for this permission!


                            if (entry == null) {
                                result.add (user.getUserKey ());
                            } else {
                                /**
                                 * @todo FIXME
                                 * What the hell are you doing here Dada's ??
                                 * -Fulco-
                                 */

                                // the user has the right for this permission ?
                                int permBit = -1;
                                for (int j = 0; j < 3; j++) {
                                    if (entry.getPermission (j) ==
                                            JahiaAclEntry.ACL_YES) {
                                        permBit = j;
                                    }
                                }
                                if ((permBit != -1) &&
                                        (this.getPermission (user, permBit))) {
                                    result.add (user.getUserKey ());
                                }
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<String>(result);
    }

    /**
     * Return true if the given aclId is in this acl parents path
     *
     * @param aclId
     *
     * @return true if true
     */
    public boolean isAclInParents (int aclId) {
        if (getParent() == null) {
            return false;
        }
        if (getParent().getId().intValue()  == aclId) {
            return true;
        }
        return getParent().isAclInParents (aclId);
    }

    public boolean removeGroupEntry(JahiaGroup group) {
        getGroupEntries().remove(group.getGroupKey());
        JahiaAclEntry entry = null;
        for (Iterator<JahiaAclEntry> iterator = entries.iterator(); iterator.hasNext() && entry == null;) {
            JahiaAclEntry currentEntry = iterator.next();
            if (currentEntry.getComp_id().getType().intValue()==2 && currentEntry.getComp_id().getTarget().equals(group.getGroupKey())) {
                entry = currentEntry;
            }
        }
        if (entry != null) {
            entries.remove(entry);
            if (entries.isEmpty()) {
                setHasEntries(new Integer(0));
            }
        }
        return true;
    }

    public boolean removeUserEntry(JahiaUser user) {
        getUserEntries().remove(user.getUserKey());
        JahiaAclEntry entry = null;
        for (Iterator<JahiaAclEntry> iterator = entries.iterator(); iterator.hasNext() && entry == null;) {
            JahiaAclEntry currentEntry = iterator.next();
            if (currentEntry.getComp_id().getType().intValue()==1 && currentEntry.getComp_id().getTarget().equals(user.getUserKey())) {
                entry = currentEntry;
            }
        }
        if (entry != null) {
            entries.remove(entry);
            if (entries.isEmpty()) {
                setHasEntries(new Integer(0));
            }
        }
        return true;
    }

    public boolean setGroupEntry(JahiaGroup group, JahiaAclEntry entry) {
        if ((group == null) || (entry == null)) {
            return false;
        }
        JahiaAclEntry current = (JahiaAclEntry) getGroupEntries().get(group.getGroupKey());
        if (current != null) {
            current.setEntryState(entry.getEntryState());
            current.setEntryTri(entry.getEntryTri());

        } else {
            //Create new jahiaAclEntry to ensure presence of pk
            setHasEntries(new Integer(1));
            current = new JahiaAclEntry(
                    new JahiaAclEntryPK(this, new Integer(ACLInfo.GROUP_TYPE_ENTRY), group.getGroupKey()),
                    entry.getEntryState(), entry.getEntryTri());
            getGroupEntries().put(group.getGroupKey(), current);
            getEntries().add(current);
        }
        return true;
    }

    public boolean setUserEntry(JahiaUser user, JahiaAclEntry entry) {
        JahiaAclEntry current = getUserEntries().get(user.getUserKey());
        if (current != null) {
            current.setEntryState(entry.getEntryState());
            current.setEntryTri(entry.getEntryTri());
        } else {
            setHasEntries(new Integer(1));
            current = new JahiaAclEntry(
                    new JahiaAclEntryPK(this, new Integer(ACLInfo.USER_TYPE_ENTRY),
                    user.getUserKey()),
                    entry.getEntryState(), entry.getEntryTri());
            getUserEntries().put(user.getUserKey(), current);
            getEntries().add(current);
        }
        return true;
    }

    /**
     * Transpose the HashMap's keys into a List.
     *
     * @param HashMap
     *
     * @return a List of Map keys.
     */
    protected List<String> getKeysFromHashMap(Map<String, JahiaAclEntry> map) {
        final List<String> vect = new ArrayList<String>();

        final Iterator<String> names = map.keySet().iterator();
        while (names.hasNext()) {
            vect.add(names.next());
        }
        return vect;
    }

    private void buildPermissions(Map<String, JahiaAclEntry> localTable, Map<String, JahiaAclEntry> parentTable, boolean readBreak) {
        // for each ACL user/group entry check if it doesn't already exist in the
        // global user entries HashMap. If it does, add only permission that
        // should be herited by the current user entry.
        try {
            for (String name : parentTable.keySet()) {
                if (name != null) {
                    JahiaAclEntry entry = parentTable.get(name);
                    entry = (JahiaAclEntry) entry.clone();
                    if (readBreak) {
                        entry.setPermission(JahiaBaseACL.READ_RIGHTS, JahiaAclEntry.ACL_NEUTRAL);
                    }
                    JahiaAclEntry localEntry = localTable.get(name);

                    // if the user already exist, add some permission if needed,
                    // otherwise add the user entry in the table.
                    if (localEntry != null) {
                        int tristate = entry.getEntryTri();
                        int localTristate = localEntry.getEntryTri();

                        // 1. get the bit mask of the permissions to be inherited
                        int mask = ((~tristate) & localTristate);

                        // if there is some bits to set, go for it, otherwise give up
                        // and go to the next user.
                        if (mask != 0) {
                            int localState = localEntry.getEntryState();

                            // 2. get the permission to set
                            int permissionMask = (entry.getEntryState() & mask);

                            // 3. compute the new entry state
                            localState = (localState | permissionMask);

                            // 4. compute the new entry tristate
                            localTristate = ((~mask) & localTristate);

                            // finally update the state and tristate
                            localEntry.setEntryState(localState);
                            localEntry.setEntryTri(localTristate);
                        }
                    } else {
                        localTable.put(name, entry);
                    }
                }
            } // while
        } catch (CloneNotSupportedException e) {
            logger.warn("Try to clone unclonable object", e);
        }
    }

    private void buildPickerPermissions(Map<String, JahiaAclEntry> localTable, Map<String, JahiaAclEntry> pickedTable) {
        try {
            Iterator<String> keys = pickedTable.keySet().iterator();
            String name;
            while (keys.hasNext()) {
                name = keys.next();
                if (name != null) {
                    JahiaAclEntry entry = pickedTable.get(name);
                    entry = (JahiaAclEntry) entry.clone();
                    entry.setPermission(JahiaBaseACL.WRITE_RIGHTS, JahiaAclEntry.ACL_NEUTRAL);
                    entry.setPermission(JahiaBaseACL.ADMIN_RIGHTS, JahiaAclEntry.ACL_NEUTRAL);
                    JahiaAclEntry localEntry = localTable.get(name);
                    // if the user already exist, add some permission if needed,
                    // otherwise add the user entry in the table.
                    if (localEntry != null) {
                        int tristate = entry.getEntryTri();
                        int localTristate = localEntry.getEntryTri();

                        // 1. get the bit mask of the permissions to be inherited
                        int mask = ((~tristate) & localTristate);

                        // if there is some bits to set, go for it, otherwise give up
                        // and go to the next user.
                        if (mask != 0) {
                            int localState = localEntry.getEntryState();

                            // 2. get the permission to set
                            int permissionMask = (entry.getEntryState() & mask);

                            // 3. compute the new entry state
                            localState = (localState | permissionMask);

                            // 4. compute the new entry tristate
                            localTristate = ((~mask) & localTristate);

                            // finally update the state and tristate
                            localEntry.setEntryState(localState);
                            localEntry.setEntryTri(localTristate);
                        }
                    } else {
                        localTable.put(name, entry);
                    }
                }
            }
        } catch (CloneNotSupportedException e) {
            logger.warn("Try to clone unclonable object", e);
        }
    }

    /**
     * Get the user/group name list from the HashMap.
     *
     * @param entry
     *
     * @return a List of user/group name.
     */
    private List<String> getNameList(Map<String, JahiaAclEntry> map, JahiaAclEntry entry) {
        if (entry == null) 
            return getKeysFromHashMap(map);
        
        List<String> result = new ArrayList<String>();
		Iterator<String> keys = map.keySet().iterator();
		while (keys.hasNext()) {
			String name = keys.next();
			JahiaAclEntry mapEntry = map.get(name);
			if (mapEntry.hasSameBitsActivated(entry)) {
				result.add(name);
			}
		}

        return result;
    }

    /**
	 * Search recursively the user permission in the ACL.
	 * 
	 * @param user
	 * @param permission
	 * @param table
	 * 
	 * @return Return 1 if the user has the requested permission, 0 if the user
	 *         has no permission and -1 if the user was not found.
	 * 
	 * @todo This code has to be reviewed
	 */
    private int getUserPermissionInGroupHashMap (JahiaUser user, int permission, Map<String, JahiaAclEntry> table) {
        int result = -1;

        // Check if the user has the requested permission in one of the group entries.
        for (String groupname : table.keySet()) {
            JahiaGroup group = ServicesRegistry.getInstance ().
                    getJahiaGroupManagerService ().lookupGroup (groupname);

            // Check if the user is member of the group

			if (group != null && group.isMember(user)) {
                // if it's the first time the user is found in a group, set by default
                // the "deny" permission. If the user has the permission, it will be
                // given later.
                if (result < 0) {
                    result = 0;
                }
                
                // Get the permission entries for the group and check it.
                JahiaAclEntry entry = table.get (groupname);
                if (entry.getPermission (permission) ==
                        JahiaAclEntry.ACL_YES) {
                    result = 1;
                }
                
				if (result == 1) {
					if (logger.isDebugEnabled())
						logger.debug("Permission for " + user.getUserKey()
								+ " found in group " + group.getGroupKey());
					break;
				}         
            } // if
        } // for


        return result;
    }

    /**
     * Search recursively the user permission in the ACL.
     *
     * @param user
     * @param permission
     * @param table
     *
     * @return Return 1 if the user has the requested permission, 0 if the user
     *         has no permission and -1 if the user was not found.
     *
     * @todo This code has to be reviewed
     */
    private int getUserPermissionInHashMap (JahiaUser user, int permission, Map<String, JahiaAclEntry> table) {
        int result = -1; // start as if the user was not found.

        //check if the user is in entries
        JahiaAclEntry entry = (JahiaAclEntry) table.get (user.getUserKey ());
        if (entry != null) {
            switch (entry.getPermission (permission)) {
                case JahiaAclEntry.ACL_YES:
                    result = 1;
                    break;

                case JahiaAclEntry.ACL_NO:
                    result = 0;
                    break;
            }
        }

        return result;
    }

    /**
     * Find permissions recursively.
     *
     */
    public Map<String, JahiaAclEntry>[] getRecursedPermissions(ParentACLFinder parentACLFinder, ACLResourceInterface aclResource) {
        return getRecursedPermissions(parentACLFinder, aclResource, new ArrayList<Integer>());
    }

    public Map<String, JahiaAclEntry>[] getRecursedPermissions(ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, List<Integer> list) {
        checkCache();
        if (list.contains(getId())) {
            logger.error("Permission loop detected : "+list);
            return new Map[] {new HashMap<String, JahiaAclEntry>(), new HashMap<String, JahiaAclEntry>()};
        }
        list.add(getId());

        Map<String, JahiaAclEntry>[] recursedMaps = new Map[] { new HashMap<String, JahiaAclEntry>(userEntries), new HashMap<String, JahiaAclEntry>(groupEntries) };

            JahiaAcl parent = null;
            ACLResourceInterface parentAclResource = null;
            if (getPickedAcl() != null) {
                if (getParent().getPickedAcl() != null) {
                    buildPickerPermissions(recursedMaps[0], getPickedAcl().getUserEntries());
                    buildPickerPermissions(recursedMaps[1], getPickedAcl().getGroupEntries());
                } else {
                    buildPickerPermissions(recursedMaps[0], getPickedAcl().getRecursedUserEntries());
                    buildPickerPermissions(recursedMaps[1], getPickedAcl().getRecursedGroupEntries());                    
                }
            }
            if (inheritance.intValue() == ACLInfo.INHERITANCE) {
                if (parentACLFinder == null) {
                    parent = getParent();
                } else {
                    parentAclResource = parentACLFinder.getParent(aclResource);
                    if (parentAclResource != null) {
                        parent = parentAclResource.getACL().getACL();
                    }
                }
            }
            if (parent != null) {
                Map<String, JahiaAclEntry>[] parents = parent.getRecursedPermissions(parentACLFinder, parentAclResource, list);
                for (String groupName : parents[1].keySet()) {
                    if (groupName.startsWith("guest:")) {
                        Set<String> set = new HashSet<String>(recursedMaps[1].keySet());
                        for (String pickedGroupName : set) {
                            if (pickedGroupName.startsWith("guest:")) {
                                recursedMaps[1].put(groupName, recursedMaps[1].remove(pickedGroupName));
                            }
                        }
                    }
                }

                boolean readBreak = getPickedAcl() != null && getPickedAcl().getInheritance().intValue() == ACLInfo.NO_INHERITANCE;

                buildPermissions(recursedMaps[0], parents[0], readBreak);
                buildPermissions(recursedMaps[1], parents[1], readBreak);
            }

            permissions.put("recursedTables"+ getKey() , recursedMaps);
//        }

        return recursedMaps;
    }

    protected String getKey() {
        return id.toString();
    }

    public Map<String, JahiaAclEntry> getRecursedUserEntries() {
        return getRecursedPermissions (null, null) [0];
    }

    public Map<String, JahiaAclEntry> getRecursedGroupEntries() {
        return getRecursedPermissions (null, null) [1];
    }

    public JahiaBaseACL getACL() {
        try {
            if (mAcl == null)
              mAcl = new JahiaBaseACL(getAclID());
            return mAcl;
        } catch (Exception t) {
            // this should never happen
            logger.error("Error while retrieving ACL, returning null !", t);
            return null;
        }
    }

    public int getAclID() {
        return id.intValue();
    }

    public ACLResourceInterface getParent(ACLResourceInterface aclResource) {
        return getParent();
    }

    public List<String> getUsernameList(ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, JahiaAclEntry entry) {
        Map<String, JahiaAclEntry> table = getRecursedPermissions (parentACLFinder, aclResource) [0];
        return getNameList (table, entry);
    }

    public List<String> getGroupnameList(ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, JahiaAclEntry entry) {
        Map<String, JahiaAclEntry> table = getRecursedPermissions (parentACLFinder, aclResource) [1];
        return getNameList (table, entry);
    }

    public List<String> getGroupnameListNoAdmin(ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, JahiaAclEntry entry) {
        List<String> result = getGroupnameList (parentACLFinder, aclResource, entry);
        for (int i = 0; i < result.size (); i++) {
            String adminGroup = (String) result.get (i);
            if (adminGroup.indexOf (JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME) == 0)
                result.remove (i--);
        }
        return result;
    }

    public boolean getPermission(ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, JahiaUser user, int permission) {
        int contextSiteID = getContextSiteID(user);
        return getPermission(parentACLFinder, aclResource, user, permission, true, contextSiteID);
    }

    /**
     * This method should ideally not even exist, it's a hack to retrieve a context
     * site ID. Ideally we should get this from the OBJECT on which we are checking
     * the permission, not the ProcessingContext nor the user.
     * @param user the user on which to retrieve the siteID, if he has one (will not
     * have one if he comes from LDAP provider)
     * @return a site ID extracted from the context, either from the ThreadLocal
     * processing context or the user's siteID.
     */
    private int getContextSiteID(JahiaUser user) {
        /**
         * @todo FIXME_MULTISITE Hollis , the administrator grp is deducted
         * from user.getSiteID() problem here ? This is clearly a serious
         * problem, as it means that administrators from ANY site have full
         * access to all Jahia content ! Actually the siteID should come
         * from the content object. Maybe with the new modifications that
         * pass the ACLResourceInterface we could use reflection to try
         * to retrieve the siteID from the content object.
         **/
        ProcessingContext processingContext = Jahia.getThreadParamBean();
        int contextSiteID = -1;
        if (processingContext != null) {
            contextSiteID = processingContext.getSiteID();
        }
        return contextSiteID;
    }

    public boolean getPermission(ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, JahiaGroup group, int permission) {
        boolean result = false;

        if (group != null) {
            Map<String, JahiaAclEntry> table = getRecursedPermissions(parentACLFinder, aclResource) [1];
            JahiaAclEntry entry = table.get(group.getGroupKey());
            if (entry != null) {
                result = (entry.getPermission(permission) ==
                        JahiaAclEntry.ACL_YES);
            }
        }

        return result;
    }

    public List<String> getUsernameListAlsoGroupUsers(ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, JahiaAclEntry entry) {
        return getUsernameListAlsoGroupUsers(entry);
    }

}

