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
package org.jahia.ajax.gwt.aclmanagement.server;

import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.acl.JahiaACLEntry;
import org.jahia.services.acl.ParentACLFinder;
import org.jahia.services.acl.ACLResourceInterface;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.pages.JahiaPageContentRights;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.params.ProcessingContext;
import org.jahia.content.ContentObject;
import org.jahia.bin.Jahia;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * User: toto
 * Date: Nov 26, 2008 - 7:40:56 PM
 */
public class ACLHelper {

    private final static Logger logger = Logger.getLogger(ACLHelper.class);

    public static GWTJahiaNodeACL getGWTJahiaNodeACL(JahiaBaseACL baseACL, ProcessingContext jParams) {
        return getGWTJahiaNodeACL(baseACL, false, jParams);
    }

    public static GWTJahiaNodeACL getGWTJahiaNodeACL(JahiaBaseACL baseACL, boolean newAcl, ProcessingContext jParams) {
        return getGWTJahiaNodeACL(baseACL, null,null, newAcl, jParams);
    }

    public static GWTJahiaNodeACL getGWTJahiaNodeACL(JahiaBaseACL baseACL, ACLResourceInterface resource, ParentACLFinder finder, boolean newAcl, ProcessingContext jParams) {
        GWTJahiaNodeACL gwtacl = new GWTJahiaNodeACL();
        List<GWTJahiaNodeACE> aces = new ArrayList<GWTJahiaNodeACE>();

        gwtacl.setAvailablePermissions(new HashMap<String, List<String>>(Collections.singletonMap("default", Arrays.asList("read", "write", "admin"))));

        Map<String, String> labels = new HashMap<String, String>();
        for (List<String> list : gwtacl.getAvailablePermissions().values()) {
            for (String s : list) {
                labels.put(s, JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.rights.ManageRights."+s+".label", jParams.getLocale(), s));
            }
        }
        gwtacl.setPermissionLabels(labels);

        gwtacl.setAclDependencies(new HashMap<String, List<String>>());
        gwtacl.getAclDependencies().put("write", Arrays.asList("read"));
        gwtacl.getAclDependencies().put("admin", Arrays.asList("write"));
        Map<String, JahiaAclEntry> inheritedGroups = new HashMap<String, JahiaAclEntry>();
        Map<String, JahiaAclEntry> inheritedUsers = new HashMap<String, JahiaAclEntry>();
        Map<String, JahiaAclEntry> localGroups = baseACL.getACL().getGroupEntries();
        Map<String, JahiaAclEntry> localUsers = baseACL.getACL().getUserEntries();

        JahiaAcl parentAcl = null;
        ACLResourceInterface parentResource = null;
        if (finder == null) {
            parentAcl =  baseACL.getACL().getParent();
        } else {
            parentResource = finder.getParent(resource);
            if (parentResource != null) {
                parentAcl = parentResource.getACL().getACL();
            }
        }

        if (newAcl) {
            inheritedGroups = new HashMap<String, JahiaAclEntry>(localGroups);
            inheritedUsers = new HashMap<String, JahiaAclEntry>(localUsers);
            inheritedGroups.putAll(parentAcl.getRecursedPermissions(finder, parentResource)[1]);
            inheritedUsers.putAll(parentAcl.getRecursedPermissions(finder, parentResource)[0]);
            localGroups = new HashMap<String, JahiaAclEntry>();
            localUsers = new HashMap<String, JahiaAclEntry>();
        } else {
            if (parentAcl != null) {
                inheritedGroups = parentAcl.getRecursedPermissions(finder, parentResource)[1];
                inheritedUsers = parentAcl.getRecursedPermissions(finder, parentResource)[0];
            }
        }
        Set<String> groupsKeys = new HashSet<String>(inheritedGroups.keySet());
        groupsKeys.addAll(localGroups.keySet());
        Set<String> usersKeys = new HashSet<String>(inheritedUsers.keySet());
        usersKeys.addAll(localUsers.keySet());

        for (String group : groupsKeys) {
            JahiaGroup jahiaGroup = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(group);
            String name = jahiaGroup.getGroupname();
            if (name.equals(JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME)) {
                continue;
            }
            GWTJahiaNodeACE ace = new GWTJahiaNodeACE();
            ace.setPrincipalType('g');
            ace.setPrincipal(name);
            ace.setPrincipalKey(jahiaGroup.getGroupKey());
            setGwtAce(ace, localGroups.get(group), inheritedGroups.get(group));
            aces.add(ace);
        }

        for (String user : usersKeys) {
            JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(user);
            GWTJahiaNodeACE ace = new GWTJahiaNodeACE();
            ace.setPrincipalType('u');
            ace.setPrincipal(jahiaUser.getUsername());
            ace.setPrincipalKey(jahiaUser.getUserKey());
            setGwtAce(ace, localUsers.get(user), inheritedUsers.get(user));
            aces.add(ace);
        }

        gwtacl.setAce(aces);
        gwtacl.setBreakAllInheritance(!newAcl && baseACL.getACL().getInheritance() != 0);
        return gwtacl;
    }

    private static void setGwtAce(GWTJahiaNodeACE ace, JahiaAclEntry e, JahiaAclEntry inherited) {
        ace.setInherited(e == null);
        ace.setPermissions(getPermissions(e));
        ace.setInheritedPermissions(getPermissions(inherited));
        if (inherited != null) {
            ContentObject contentObject = getInheritedFrom(inherited);
            if (contentObject != null) {
                String name = contentObject.getDisplayName(Jahia.getThreadParamBean());
                ace.setInheritedFrom(name);
            }
        }
    }

    public static GWTJahiaNodeACE createUsersGroupACE(List<String> permissions, boolean grand,ProcessingContext jParams) {
        JahiaGroup usersGroup = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(jParams.getSiteID(), JahiaGroupManagerService.USERS_GROUPNAME);
        GWTJahiaNodeACE ace = new GWTJahiaNodeACE();
        ace.setPrincipalType('g');
        ace.setPrincipal(usersGroup.getGroupname());
        ace.setPrincipalKey(usersGroup.getGroupKey());
        Map<String, String> permissionsMap = new HashMap<String, String>();
        for (String perm : permissions) {
            if (grand) {
                permissionsMap.put(perm, "GRANT");
            } else {
                permissionsMap.put(perm, "DENY");
            }
        }
        ace.setPermissions(permissionsMap);
        ace.setInheritedPermissions(new HashMap<String,String>());
        ace.setInherited(false);
        return ace;
    }

    private static ContentObject getInheritedFrom(JahiaAclEntry inherited) {
        try {
            ContentObject inheritedObject;
            Set<Integer> acls = Collections.singleton(inherited.getComp_id().getId().getId());
            List<JahiaPageContentRights> pageContentRightsList = ServicesRegistry.getInstance().getJahiaPageService().getPageIDsWithAclIDs(acls);
            for (JahiaPageContentRights jahiaPageContentRights : pageContentRightsList) {
                inheritedObject = ContentPage.getPage(jahiaPageContentRights.getPageID());
                if (inheritedObject != null && !inheritedObject.isAclSameAsParent()) {
                    return inheritedObject;
                }
            }
            List<Integer> containerIds = ServicesRegistry.getInstance().getJahiaContainersService().getContainerListIDsHavingAcls(acls);
            for (Integer integer : containerIds) {
                inheritedObject = ContentContainer.getContainer(integer);
                if (inheritedObject != null && !inheritedObject.isAclSameAsParent()) {
                    return inheritedObject;
                }
            }
            List<Integer> containerListIds = ServicesRegistry.getInstance().getJahiaContainersService().getContainerIDsHavingAcls(acls);
            for (Integer integer : containerListIds) {
                inheritedObject = ContentContainerList.getContainerList(integer);
                if (inheritedObject != null && !inheritedObject.isAclSameAsParent()) {
                    return inheritedObject;
                }
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private static Map<String, String> getPermissions(JahiaAclEntry e) {
        Map<String, String> perms = new HashMap<String, String>();
        if (e != null) {
            if (e.getPermission(JahiaBaseACL.READ_RIGHTS) == JahiaAclEntry.ACL_YES) perms.put("read", "GRANT");
            if (e.getPermission(JahiaBaseACL.READ_RIGHTS) == JahiaAclEntry.ACL_NO) perms.put("read", "DENY");
            if (e.getPermission(JahiaBaseACL.WRITE_RIGHTS) == JahiaAclEntry.ACL_YES) perms.put("write", "GRANT");
            if (e.getPermission(JahiaBaseACL.WRITE_RIGHTS) == JahiaAclEntry.ACL_NO) perms.put("write", "DENY");
            if (e.getPermission(JahiaBaseACL.ADMIN_RIGHTS) == JahiaAclEntry.ACL_YES) perms.put("admin", "GRANT");
            if (e.getPermission(JahiaBaseACL.ADMIN_RIGHTS) == JahiaAclEntry.ACL_NO) perms.put("admin", "DENY");
        }
        return perms;
    }

    public static JahiaBaseACL saveACL(GWTJahiaNodeACL acl, JahiaBaseACL baseACL, boolean newAcl) throws JahiaException {
        if (newAcl) {
            JahiaBaseACL jAcl = new JahiaBaseACL();
            jAcl.create(baseACL.getID());
            baseACL = jAcl;
        } else {
            baseACL.removeAllUserEntries();
            baseACL.removeAllGroupEntries();
        }
        List<GWTJahiaNodeACE> aces = acl.getAce();
        for (GWTJahiaNodeACE ace : aces) {
            if (!ace.isInherited()) {
                switch (ace.getPrincipalType()) {
                    case 'u': {
                        JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(ace.getPrincipalKey());
                        JahiaAclEntry permissions = createAclEntry(ace);
                        baseACL.setUserEntry(user, permissions);
                        break;
                    }
                    case 'g': {
                        JahiaGroup group = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(ace.getPrincipalKey());
                        JahiaAclEntry permissions = createAclEntry(ace);
                        baseACL.setGroupEntry(group, permissions);
                        break;
                    }
                }
            }
        }
        baseACL.setInheritance(acl.isBreakAllInheritance() ? 1 : 0);
        return baseACL;
    }

    private static JahiaAclEntry createAclEntry(GWTJahiaNodeACE ace) {
        JahiaAclEntry permissions = new JahiaAclEntry();
        permissions.setPermission(JahiaBaseACL.READ_RIGHTS, "GRANT".equals(ace.getPermissions().get("read")) ? JahiaACLEntry.ACL_YES : JahiaACLEntry.ACL_NO);
        permissions.setPermission(JahiaBaseACL.WRITE_RIGHTS, "GRANT".equals(ace.getPermissions().get("write")) ? JahiaACLEntry.ACL_YES : JahiaACLEntry.ACL_NO);
        permissions.setPermission(JahiaBaseACL.ADMIN_RIGHTS, "GRANT".equals(ace.getPermissions().get("admin")) ? JahiaACLEntry.ACL_YES : JahiaACLEntry.ACL_NO);
        return permissions;
    }


}
