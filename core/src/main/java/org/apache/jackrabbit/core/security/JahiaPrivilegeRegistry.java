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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
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

    public static void init(Session session) throws RepositoryException {
        Node perms = session.getNode("/permissions");

        Set<Privilege> privileges = new HashSet<Privilege>(20);

        registerPrivileges(perms, privileges);

        for (Privilege p : privileges) {
            if (!map.containsKey(p.getName())) {
                map.put(p.getName(), p);
            }
        }
    }

    private static Privilege registerPrivileges(Node node, Set<Privilege> privileges) throws RepositoryException {
        Set<Privilege> subPrivileges = new HashSet<Privilege>();

        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            Node subNode = (Node) ni.next();
            subPrivileges.add(registerPrivileges(subNode, privileges));
        }

        String name = getExpandedName(node);
        boolean isAbstract = node.hasProperty("j:isAbstract") && node.getProperty("j:isAbstract").getBoolean();
        Privilege priv = new PrivilegeImpl(name, isAbstract, subPrivileges);
        privileges.add(priv);
        return priv;
    }

    private static String getExpandedName(Node node) throws RepositoryException {
        String name = node.getName();
        if (name.contains(":")) {
            name = "{" + node.getSession().getNamespaceURI(StringUtils.substringBefore(name, ":")) + "}" +
                    StringUtils.substringAfter(name, ":");
        } else {
            name = "{}" + name;
        }
        return name;
    }

    /**
     * Per instance map containing the instance specific representation of
     * the registered privileges.
     */
    private static final Map<String, Privilege> map = new HashMap<String, Privilege>();

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
        return map.values().toArray(new Privilege[map.size()]);
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
        if (!privilegeName.startsWith("{")) {
            privilegeName = "{}" + privilegeName;
        }
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
        String privilegeName = getExpandedName(node);
        if (map.containsKey(privilegeName)) {
            return map.get(privilegeName);
        } else {
            throw new AccessControlException("Unknown privilege " + privilegeName);
        }
    }


    static class PrivilegeImpl implements Privilege {
        private String name;
        private boolean isAbstract;
        private Set<Privilege> declaredAggregates;
        private Set<Privilege> aggregates;

        PrivilegeImpl(String name, boolean anAbstract, Set<Privilege> declaredAggregates) {
            this.name = name;
            isAbstract = anAbstract;
            this.declaredAggregates = declaredAggregates;
            this.aggregates = new HashSet<Privilege>(declaredAggregates);
            for (Privilege priv : declaredAggregates) {
                for (Privilege privilege : priv.getAggregatePrivileges()) {
                    aggregates.add(privilege);
                }
            }
        }

        public String getName() {
            return name;
        }

        public boolean isAbstract() {
            return isAbstract;
        }

        public boolean isAggregate() {
            return !declaredAggregates.isEmpty();
        }

        public Privilege[] getDeclaredAggregatePrivileges() {
            return declaredAggregates.toArray(new Privilege[declaredAggregates.size()]);
        }

        public Privilege[] getAggregatePrivileges() {
            return aggregates.toArray(new Privilege[aggregates.size()]);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PrivilegeImpl that = (PrivilegeImpl) o;

            if (name != null ? !name.equals(that.name) : that.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

}
