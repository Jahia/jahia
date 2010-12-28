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
    private static final Set<PrivilegeImpl> REGISTERED_PRIVILEGES = new HashSet<PrivilegeImpl>(20);

    private static Map<Integer, String> STANDARD_PRIVILEGES = new HashMap<Integer, String>();

    static {
        STANDARD_PRIVILEGES.put(Permission.READ, "jcr:read");
        STANDARD_PRIVILEGES.put(Permission.SET_PROPERTY, "jcr:modifyProperties");
        STANDARD_PRIVILEGES.put(Permission.ADD_NODE, "jcr:addChildNodes");
        STANDARD_PRIVILEGES.put(Permission.REMOVE_NODE, "jcr:removeNode");
        STANDARD_PRIVILEGES.put(Permission.REMOVE_PROPERTY, "jcr:removeChildNodes");
        STANDARD_PRIVILEGES.put(Permission.READ_AC, "jcr:readAccessControl");
        STANDARD_PRIVILEGES.put(Permission.MODIFY_AC, "jcr:modifyAccessControl");
        STANDARD_PRIVILEGES.put(Permission.NODE_TYPE_MNGMT, "jcr:nodeTypeManagement");
        STANDARD_PRIVILEGES.put(Permission.VERSION_MNGMT, "jcr:versionManagement");
        STANDARD_PRIVILEGES.put(Permission.LOCK_MNGMT, "jcr:lockManagement");
        STANDARD_PRIVILEGES.put(Permission.LIFECYCLE_MNGMT, "jcr:lifecycleManagement");
        STANDARD_PRIVILEGES.put(Permission.RETENTION_MNGMT, "jcr:retentionManagement");
    }

    public static void init(Session session) throws RepositoryException {
        Node perms = session.getNode("/permissions");

        registerPrivileges(perms);
        return;
    }

    private static Set<Privilege> registerPrivileges(Node perms) throws RepositoryException {
        Set<Privilege> privileges = new HashSet<Privilege>();

        NodeIterator ni = perms.getNodes();
        while (ni.hasNext()) {
            Node node = (Node) ni.next();

            Set<Privilege> sub = registerPrivileges(node);

            if (node.isNodeType("jnt:permission")) {
                privileges.add(new PrivilegeImpl(node.getName(), node.hasProperty("j:isAbstract") && node.getProperty("j:isAbstract").getBoolean(), sub));
            }
        }
        return privileges;
    }

    /**
     * Per instance map containing the instance specific representation of
     * the registered privileges.
     */
    private final Map<String, Privilege> localCache;

    /**
     * Create a new <code>PrivilegeRegistry</code> instance.
     *
     * privileges.
     */
    public JahiaPrivilegeRegistry() {
        localCache = new HashMap<String, Privilege>(REGISTERED_PRIVILEGES.size());
        for (Privilege p : REGISTERED_PRIVILEGES) {
            localCache.put(p.getName(), p);
        }
    }

    public Set<Privilege> getPrivileges(int permissions, String workspace) throws AccessControlException, RepositoryException {
        Set<Privilege> r = new HashSet<Privilege>();

        for (Map.Entry<Integer, String> entry : STANDARD_PRIVILEGES.entrySet()) {
            if ((permissions & entry.getKey()) == entry.getKey()) {
                r.add(getPrivilege(entry.getValue() + "_"+workspace));
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
        return localCache.values().toArray(new Privilege[localCache.size()]);
    }

    /**
     * Returns the privilege with the specified <code>privilegeName</code>.
     *
     * @param privilegeName Name of the principal.
     * @return the privilege with the specified <code>privilegeName</code>.
     * @throws AccessControlException If no privilege with the given name exists.
     * @throws RepositoryException If another error occurs.
     */
    public Privilege getPrivilege(String privilegeName) throws AccessControlException, RepositoryException {
        if (localCache.containsKey(privilegeName)) {
            return localCache.get(privilegeName);
        } else {
            // todo fix initialization
            return new PrivilegeImpl(privilegeName, false, new HashSet<Privilege>());
//            throw new AccessControlException("Unknown privilege " + privilegeName);
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
            REGISTERED_PRIVILEGES.add(this);
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
