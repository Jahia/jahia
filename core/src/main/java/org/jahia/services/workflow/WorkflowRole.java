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
