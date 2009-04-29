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
package org.jahia.services.workflow;

import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jahia.content.ContentObjectKey;
import org.jahia.services.usermanager.JahiaGroup;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 5, 2008
 * Time: 6:57:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowRole {
    private String role;
    private JahiaGroup group;
    private Map<ContentObjectKey, JahiaGroup> inheritedGroups = new HashMap<ContentObjectKey, JahiaGroup>();
    private ContentObjectKey objectKey;

    public WorkflowRole(String role, JahiaGroup group, ContentObjectKey objectKey) {
        this.role = role;
        this.group = group;
        this.objectKey = objectKey;
    }

    public void addPrincipal(WorkflowRole p) {
        if (p.getGroup() != null) {
            inheritedGroups.put(p.getObjectKey(), p.getGroup());
        }
        inheritedGroups.putAll(p.getInheritedGroups());
    }

    public JahiaGroup getGroup() {
        return group;
    }

    public Map<ContentObjectKey, JahiaGroup> getInheritedGroups() {
        return inheritedGroups;
    }

    public ContentObjectKey getObjectKey() {
        return objectKey;
    }

    public boolean isMember(Principal p) {
        if (group.isMember(p))  {
            return true;
        }
        for (JahiaGroup group : inheritedGroups.values()) {
            if (group.isMember(p)) {
                return true;
            }
        }
        return false;
    }

    public Set<Principal> getAllMembers() {
        Set<Principal> all = getMembers();
        all.addAll(getInheritedMembers().keySet());
        return all;
    }

    public Set<Principal> getMembers() {
        final Set<Principal> membersSet = new HashSet<Principal>();
        if (group != null) {
            final Enumeration<Principal> groupEnum = group.members();
            while (groupEnum.hasMoreElements()) {
                membersSet.add(groupEnum.nextElement());
            }
        }
        return membersSet;
    }

    public Map<Principal, ContentObjectKey> getInheritedMembers() {
        final Map<Principal, ContentObjectKey> membersSet = new HashMap<Principal, ContentObjectKey>();
        for (ContentObjectKey contentObjectKey : inheritedGroups.keySet()) {
            JahiaGroup inheritedGroup = inheritedGroups.get(contentObjectKey);
            if (inheritedGroup != null) {
                final Enumeration<Principal> groupEnum = inheritedGroup.members();
                while (groupEnum.hasMoreElements()) {
                    membersSet.put(groupEnum.nextElement(), contentObjectKey);
                }
            }
        }
        return membersSet;

    }

}
