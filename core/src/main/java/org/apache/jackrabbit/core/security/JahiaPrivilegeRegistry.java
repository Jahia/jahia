/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.apache.jackrabbit.core.security;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.jahia.services.content.JCRContentUtils;

import javax.jcr.*;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.Privilege;
import java.util.*;

/**
 * The <code>PrivilegeRegistry</code> defines the set of <code>Privilege</code>s
 * known to the repository.
 */
public final class JahiaPrivilegeRegistry {

    private static Map<Integer, String> STANDARD_PRIVILEGES = new HashMap<Integer, String>();

    static {
        STANDARD_PRIVILEGES.put(Permission.READ, Privilege.JCR_READ);
        STANDARD_PRIVILEGES.put(Permission.SET_PROPERTY, Privilege.JCR_MODIFY_PROPERTIES);
        STANDARD_PRIVILEGES.put(Permission.ADD_NODE, Privilege.JCR_ADD_CHILD_NODES);
        STANDARD_PRIVILEGES.put(Permission.REMOVE_NODE, Privilege.JCR_REMOVE_CHILD_NODES);
        STANDARD_PRIVILEGES.put(Permission.REMOVE_PROPERTY, Privilege.JCR_REMOVE_NODE);
        STANDARD_PRIVILEGES.put(Permission.READ_AC, Privilege.JCR_READ_ACCESS_CONTROL);
        STANDARD_PRIVILEGES.put(Permission.MODIFY_AC, Privilege.JCR_MODIFY_ACCESS_CONTROL);
        STANDARD_PRIVILEGES.put(Permission.NODE_TYPE_MNGMT, Privilege.JCR_NODE_TYPE_MANAGEMENT);
        STANDARD_PRIVILEGES.put(Permission.VERSION_MNGMT, Privilege.JCR_VERSION_MANAGEMENT);
        STANDARD_PRIVILEGES.put(Permission.LOCK_MNGMT, Privilege.JCR_LOCK_MANAGEMENT);
        STANDARD_PRIVILEGES.put(Permission.LIFECYCLE_MNGMT, Privilege.JCR_LOCK_MANAGEMENT);
        STANDARD_PRIVILEGES.put(Permission.RETENTION_MNGMT, Privilege.JCR_RETENTION_MANAGEMENT);
    }

    /**
     * Per instance map containing the instance specific representation of
     * the registered privileges.
     */
    private static final Map<String, Privilege> map = new HashMap<String, Privilege>();
    private static final List<Privilege> allPrivileges = new ArrayList<Privilege>();

    private NamespaceRegistry ns;

    public static void init(Session session) throws RepositoryException {
        Node perms = session.getNode("/permissions");

        Set<Privilege> privileges = new HashSet<Privilege>(20);

        registerPrivileges(perms, privileges);

        for (Privilege p : privileges) {
            if (!map.containsKey(p.getName())) {
                map.put(p.getName(), p);
            }
            if (!allPrivileges.contains(p)) {
                allPrivileges.add(p);
            }
        }
    }

    public JahiaPrivilegeRegistry(NamespaceRegistry ns) {
        this.ns = ns;
    }

    private static Privilege registerPrivileges(Node node, Set<Privilege> privileges) throws RepositoryException {
        Set<Privilege> subPrivileges = new HashSet<Privilege>();

        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            Node subNode = (Node) ni.next();
            Privilege subPriv = registerPrivileges(subNode, privileges);
            if (subPriv != null) {
                subPrivileges.add(subPriv);
            }
        }

        try {
            String expandedName = JCRContentUtils.getExpandedName(node.getName(), node.getSession().getWorkspace().getNamespaceRegistry());
            boolean isAbstract = node.hasProperty("j:isAbstract") && node.getProperty("j:isAbstract").getBoolean();
            Privilege priv = new PrivilegeImpl(node.getName(), expandedName, isAbstract, subPrivileges);
            privileges.add(priv);
            return priv;
        } catch (NamespaceException ne) {
            // this can happen if we are trying to register a privilege who's namespace is not yet registered, as this
            // can be the case for portlet privileges. In this case we will simply ignore it for now and register it
            // at portlet registration time.
        }
        return null;
    }

    public Set<Privilege> getPrivileges(int permissions, String workspace) throws AccessControlException, RepositoryException {
        Set<Privilege> r = new HashSet<Privilege>();

        for (Map.Entry<Integer, String> entry : STANDARD_PRIVILEGES.entrySet()) {
            if ((permissions & entry.getKey()) == entry.getKey()) {
                r.add(getPrivilege(entry.getValue(),workspace));
            }
        }

        return r;
    }

    /**
     * Returns all registered privileges.
     *
     * @return all registered privileges.
     */
    public Privilege[] getRegisteredPrivileges() {
        return allPrivileges.toArray(new Privilege[allPrivileges.size()]);
    }

    /**
     * Returns the privilege with the specified <code>privilegeName</code>.
     *
     * @param privilegeName Name of the principal.
     * @return the privilege with the specified <code>privilegeName</code>.
     * @throws AccessControlException If no privilege with the given name exists.
     * @throws RepositoryException If another error occurs.
     */
    public Privilege getPrivilege(String privilegeName, String workspaceName) throws AccessControlException, RepositoryException {
        if (!privilegeName.contains("{") && privilegeName.contains("/")) {
            privilegeName = StringUtils.substringAfterLast(privilegeName, "/");
        }

        privilegeName = JCRContentUtils.getExpandedName(privilegeName, ns);

        String s = privilegeName + "_" + workspaceName;
        if (map.containsKey(s)) {
            return map.get(s);
        } else if (map.containsKey(privilegeName)) {
            return map.get(privilegeName);
        } else {
            throw new AccessControlException("Unknown privilege " + privilegeName);
        }
    }

    public Privilege getPrivilege(Node node) throws AccessControlException, RepositoryException {
        String privilegeName = JCRContentUtils.getExpandedName(node.getName(), ns);
        if (map.containsKey(privilegeName)) {
            return map.get(privilegeName);
        } else {
            throw new AccessControlException("Unknown privilege " + privilegeName);
        }
    }


}
