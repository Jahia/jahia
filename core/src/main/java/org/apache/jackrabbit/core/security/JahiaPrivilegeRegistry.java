/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.apache.jackrabbit.core.security;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.jahia.services.content.JCRContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.Privilege;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The <code>PrivilegeRegistry</code> defines the set of <code>Privilege</code>s
 * known to the repository.
 */
public final class JahiaPrivilegeRegistry {

    private static final Logger logger = LoggerFactory.getLogger(JahiaPrivilegeRegistry.class);
    private static Map<Integer, String> STANDARD_PRIVILEGES = new HashMap<Integer, String>();

    static {
        STANDARD_PRIVILEGES.put(Permission.READ, Privilege.JCR_READ);
        STANDARD_PRIVILEGES.put(Permission.SET_PROPERTY, Privilege.JCR_MODIFY_PROPERTIES);
        STANDARD_PRIVILEGES.put(Permission.ADD_NODE, Privilege.JCR_ADD_CHILD_NODES);
        STANDARD_PRIVILEGES.put(Permission.REMOVE_NODE, Privilege.JCR_REMOVE_CHILD_NODES);
        STANDARD_PRIVILEGES.put(Permission.REMOVE_PROPERTY, Privilege.JCR_MODIFY_PROPERTIES);
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
    private static final Map<String, Privilege> privilegesByName = new LinkedHashMap<String, Privilege>();
    private static Privilege[] registeredPrivileges;
    private static List<String> registeredPrivilegeNames;

    private static ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static Lock readLock = readWriteLock.readLock();
    private static Lock writeLock = readWriteLock.writeLock();

    private NamespaceRegistry ns;

    public static void init(Session session) throws RepositoryException {
        init(session, null);
    }

    public static void addModulePrivileges(Session session, String path) throws RepositoryException {
        init(session, path);
    }

    private static void init(Session session, String path) throws RepositoryException {
        Node perms = session.getNode(path != null ? (path + "/permissions") : "/permissions");

        Set<Privilege> privileges = new LinkedHashSet<Privilege>(20);

        registerPrivileges(perms, privileges);
        if (!privileges.isEmpty()) {
            writeLock.lock();
            try {
                for (Privilege p : privileges) {
                    privilegesByName.put(p.getName(), p);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding privilege {} under path {}{}", p.getName(), path, ((PrivilegeImpl) p).getNodePath());
                    }
                }
                registeredPrivileges = privilegesByName.values().toArray(new Privilege[privilegesByName.size()]);
            } finally {
                writeLock.unlock();
            }
            List<String> privilegeNames = new ArrayList<String>();
            for (Privilege privilege : registeredPrivileges) {
                privilegeNames.add(((PrivilegeImpl) privilege).getPrefixedName());
            }
            registeredPrivilegeNames = privilegeNames;
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

        if (registeredPrivileges != null) {
            for (Privilege privilege : registeredPrivileges) {
                if (privilege instanceof PrivilegeImpl) {
                    ((PrivilegeImpl) privilege).removePrivileges(subPrivileges);
                }
            }
        }

        try {
            String expandedName = JCRContentUtils.getExpandedName(node.getName(), node.getSession().getWorkspace().getNamespaceRegistry());
            boolean isAbstract = node.hasProperty("j:isAbstract") && node.getProperty("j:isAbstract").getBoolean();
            PrivilegeImpl priv = (PrivilegeImpl) getPrivilegeByName(expandedName);
            if (priv == null) {
                priv = new PrivilegeImpl(node.getName(), expandedName, isAbstract, subPrivileges, node.getPath());
            } else {
                priv.addPrivileges(subPrivileges);
            }
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

        // special case for MODIFY_CHILD_NODE_COLLECTION: in this case we enforce both ADD_NODE and REMOVE_NODE permissions
        if ((permissions & Permission.MODIFY_CHILD_NODE_COLLECTION) == Permission.MODIFY_CHILD_NODE_COLLECTION) {
            r.add(getPrivilege(STANDARD_PRIVILEGES.get(Permission.ADD_NODE), workspace));
            r.add(getPrivilege(STANDARD_PRIVILEGES.get(Permission.REMOVE_NODE), workspace));
        }

        return r;
    }

    /**
     * Returns all registered privileges.
     *
     * @return all registered privileges.
     */
    public static Privilege[] getRegisteredPrivileges() {
        return registeredPrivileges;
    }

    /**
     * Returns all registered privileges.
     *
     * @return all registered privileges.
     */
    public static List<String> getRegisteredPrivilegeNames() {
        return registeredPrivilegeNames;
    }

    private static Privilege getPrivilegeByName(String... privilegeNames) {
        Privilege privilege = null;
        readLock.lock();
        try {
            for (String name : privilegeNames) {
                privilege = privilegesByName.get(name);
                if (privilege != null) {
                    break;
                }
            }
        } finally {
            readLock.unlock();
        }

        return privilege;
    }

    /**
     * Returns the privilege with the specified <code>privilegeName</code>.
     *
     * @param privilegeName Name of the principal.
     * @return the privilege with the specified <code>privilegeName</code>.
     * @throws AccessControlException If no privilege with the given name exists.
     * @throws RepositoryException If another error occurs.
     */
    public Privilege getPrivilege(String privilegeName, String workspaceName) throws AccessControlException,
            RepositoryException {
        if (!privilegeName.contains("{") && privilegeName.contains("/")) {
            privilegeName = StringUtils.substringAfterLast(privilegeName, "/");
        }

        privilegeName = JCRContentUtils.getExpandedName(privilegeName, ns);

        String s = JahiaAccessManager.getPrivilegeName(privilegeName, workspaceName);
        Privilege privilege = getPrivilegeByName(s, privilegeName);
        if (privilege != null) {
            return privilege;
        }
        throw new AccessControlException("Unknown privilege " + privilegeName);
    }

    public Privilege getPrivilege(Node node) throws AccessControlException, RepositoryException {
        String privilegeName = JCRContentUtils.getExpandedName(node.getName(), ns);
        Privilege privilege = getPrivilegeByName(privilegeName);
        if (privilege != null) {
            return privilege;
        }
        throw new AccessControlException("Unknown privilege " + privilegeName);
    }

}
