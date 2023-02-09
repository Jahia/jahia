/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.collections.map.LRUMap;
import org.apache.jackrabbit.api.security.authorization.PrivilegeManager;
import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.RepositoryContext;
import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.security.authorization.PrivilegeRegistry;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.conversion.DefaultNamePathResolver;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
import org.apache.jackrabbit.spi.commons.name.PathFactoryImpl;
import org.apache.jackrabbit.spi.commons.namespace.NamespaceResolver;
import org.apache.jackrabbit.spi.commons.namespace.SessionNamespaceResolver;
import org.jahia.jaas.JahiaPrincipal;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.security.AccessManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;
import javax.security.auth.Subject;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Current ACL policy :
 * <p/>
 * - If there is a grant ACE defined for the user matching the permission, grant access
 * - If there is a deny ACE defined for the user matching the permission, deny access
 * - Go to parent node, repeat
 * - Then, start again from the leaf
 * - If there are at least one grant ACEs defined for groups the user belongs to, grant access
 * - Go to the parent node, repeat
 * - Deny access
 * <p/>
 *
 * @author toto
 */
@SuppressWarnings("squid:RedundantThrowsDeclarationCheck")
public class JahiaAccessManager extends AbstractAccessControlManager implements AccessManager, AccessControlManager {
    private static final Logger logger = LoggerFactory.getLogger(JahiaAccessManager.class);

    private static final Subject SYSTEM_SUBJECT = new Subject(true, new HashSet<>(
            Arrays.asList(new SystemPrincipal())), Collections.emptySet(), Collections.emptySet()
    );

    /**
     * Subject whose access rights this AccessManager should reflect
     */
    protected Subject subject;

    /**
     * hierarchy manager used for ACL-based access control model
     */
    protected HierarchyManager hierMgr;
    protected NamePathResolver resolver;
    private JahiaPrivilegeRegistry privilegeRegistry;
    private boolean initialized;
    protected String workspaceName;

    protected JahiaPrincipal jahiaPrincipal;

    private JahiaSystemSession securitySession;

    private RepositoryContext repositoryContext;
    private WorkspaceConfig workspaceConfig;

    private Map<String, Boolean> pathPermissionCache = null;
    private Map<Object, AccessManagerUtils.CompiledAcl> compiledAcls = new HashMap<>();

    private boolean isAliased = false;
    private DefaultNamePathResolver pr;

    public static String getPrivilegeName(String privilegeName, String workspace) {
        return AccessManagerUtils.getPrivilegeName(privilegeName, workspace);
    }

    public static void setDeniedPaths(Collection<String> denied) {
        AccessManagerUtils.setDeniedPaths(denied);
    }

    /**
     * Empty constructor
     */
    public JahiaAccessManager() {
        initialized = false;
        jahiaPrincipal = null;
        AccessManagerUtils.initCaches();
    }

    @Override
    public void init(AMContext amContext) throws AccessDeniedException, Exception {
        init(amContext, (RepositoryContext) null, (WorkspaceConfig) null);
    }

    @Override
    public void init(AMContext amContext, AccessControlProvider acProvider, WorkspaceAccessManager wspAccessManager) throws AccessDeniedException, Exception {
        init(amContext);
    }

    public JahiaSystemSession getSecuritySession() throws RepositoryException {
        if (securitySession != null) {
            return securitySession;
        }

        securitySession = new JahiaSystemSession(repositoryContext, SYSTEM_SUBJECT, workspaceConfig);

        return securitySession;
    }

    public boolean isSystemPrincipal() {
        return AccessManagerUtils.isSystemPrincipal(jahiaPrincipal);
    }

    /**
     * @deprecated Use {@link #init(AMContext, RepositoryContext, WorkspaceConfig)} instead
     */
    @Deprecated
    public void init(AMContext context, AccessControlProvider acProvider, WorkspaceAccessManager wspAccessManager, RepositoryContext repositoryContext, WorkspaceConfig workspaceConfig) throws AccessDeniedException, Exception {
        init(context, repositoryContext, workspaceConfig);
    }

    @SuppressWarnings("squid:S00112")
    public void init(AMContext context, RepositoryContext repositoryContext, WorkspaceConfig workspaceConfig) throws AccessDeniedException, Exception {
        if (initialized) {
            throw new IllegalStateException("already initialized");
        }

        pathPermissionCache = Collections.synchronizedMap(new LRUMap(SettingsBean.getInstance().getAccessManagerPathPermissionCacheMaxSize()));
        subject = context.getSubject();
        resolver = context.getNamePathResolver();
        hierMgr = context.getHierarchyManager();
        workspaceName = context.getWorkspaceName();
        this.repositoryContext = repositoryContext;
        this.workspaceConfig = workspaceConfig;
        privilegeRegistry = new JahiaPrivilegeRegistry(context.getSession().getWorkspace().getNamespaceRegistry());

        Set<JahiaPrincipal> principals = subject.getPrincipals(JahiaPrincipal.class);
        if (!principals.isEmpty()) {
            jahiaPrincipal = principals.iterator().next();
        }

        NamespaceResolver nr = new SessionNamespaceResolver(getSecuritySession());

        pr = new DefaultNamePathResolver(nr,true);
        initialized = true;
    }

    @Override
    public void close() throws Exception {
        if (securitySession != null) {
            securitySession.logout();
        }
    }

    @Override
    public void checkPermission(ItemId id, int actions) throws AccessDeniedException, ItemNotFoundException, RepositoryException {
        if (!isGranted(id, actions)) {
            throw new AccessDeniedException("Not sufficient privileges for permissions : " + actions + " on " + id);
        }
    }

    @Override
    public void checkPermission(Path path, int permissions) throws AccessDeniedException, RepositoryException {
        if (!isGranted(path, permissions)) {
            throw new AccessDeniedException("Not sufficient privileges for permissions : " + permissions + " on " + path + " [" + AccessManagerUtils.deniedPathes.get() + "]");
        }
    }

    @Override
    protected void checkPermission(String absPath, int permission)
            throws AccessDeniedException, PathNotFoundException, RepositoryException {
        checkValidNodePath(absPath);
        checkPermission(resolver.getQPath(absPath), permission);
    }

    @Override
    public boolean hasPrivileges(String absPath, Set<Principal> principals, Privilege[] privileges)
            throws PathNotFoundException, AccessDeniedException, RepositoryException {
        logCheckedPrivileges(absPath, privileges);
        checkInitialized();
        checkValidNodePath(absPath);
        checkPermission(absPath, Permission.READ_AC);

        if (privileges == null || privileges.length == 0) {
            // null or empty privilege array -> return true
            if (logger.isDebugEnabled()) {
                logger.debug("No privileges passed -> allowed.");
            }
            return true;
        } else {
            int privs = PrivilegeRegistry.getBits(privileges);
            Path p = resolver.getQPath(absPath);
            return isGranted(p, privs);
        }
    }

    @Override
    protected void checkInitialized() throws IllegalStateException {
        if (!initialized) {
            throw new IllegalStateException("not initialized");
        }
    }

    @Override
    protected PrivilegeManager getPrivilegeManager() throws RepositoryException {
        return new PrivilegeManager() {
            public Privilege[] getRegisteredPrivileges() throws RepositoryException {
                return JahiaPrivilegeRegistry.getRegisteredPrivileges();
            }

            public Privilege getPrivilege(String privilegeName) throws AccessControlException, RepositoryException {
                return privilegeRegistry.getPrivilege(privilegeName, workspaceName);
            }

            public Privilege registerPrivilege(String privilegeName, boolean isAbstract, String[] declaredAggregateNames) throws AccessDeniedException, NamespaceException, RepositoryException {
                return null;
            }
        };
    }

    @Override
    public void checkRepositoryPermission(int permissions) throws AccessDeniedException, RepositoryException {
        if (!isGranted(PathFactoryImpl.getInstance().getRootPath(), permissions)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    /**
     * @see AbstractAccessControlManager#checkValidNodePath(String)
     */
    @Override
    protected void checkValidNodePath(String absPath) throws PathNotFoundException, RepositoryException {
        checkValidNodePath(resolver.getQPath(absPath));
    }

    protected void checkValidNodePath(Path p) throws RepositoryException {
        if (!p.isAbsolute()) {
            throw new RepositoryException("Absolute path expected.");
        }
        if (hierMgr.resolveNodePath(p) == null) {
            throw new PathNotFoundException("No such node " + p);
        }
    }

    @Override
    public AccessControlPolicy[] getEffectivePolicies(Set<Principal> principals)
            throws AccessDeniedException, AccessControlException, UnsupportedRepositoryOperationException,
            RepositoryException {
        return new AccessControlPolicy[0];
    }

    @Override
    public Privilege[] getPrivileges(String absPath, Set<Principal> principals)
            throws PathNotFoundException, AccessDeniedException, RepositoryException {
        return new Privilege[0];
    }

    @Override
    public boolean isGranted(ItemId id, int actions) throws ItemNotFoundException, RepositoryException {
        if (isSystemPrincipal() && AccessManagerUtils.deniedPathes.get() == null) {
            return true;
        }
        Set<String> perm = new HashSet<>();
        if ((actions & READ) == READ) {
            perm.add(getPrivilegeName(Privilege.JCR_READ, workspaceName));
        }
        if ((actions & WRITE) == WRITE) {
            if (id.denotesNode()) {
                perm.add(getPrivilegeName(Privilege.JCR_ADD_CHILD_NODES, workspaceName));
            } else {
                perm.add(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName));
            }
        }
        if ((actions & REMOVE) == REMOVE) {
            perm.add((id.denotesNode()) ? getPrivilegeName(Privilege.JCR_REMOVE_CHILD_NODES, workspaceName) : getPrivilegeName(Privilege.JCR_REMOVE_NODE, workspaceName));
        }
        Path path = hierMgr.getPath(id);
        return isGranted(path, perm);
    }

    @Override
    public boolean isGranted(Path absPath, int permissions) throws RepositoryException {
        if (isSystemPrincipal() && AccessManagerUtils.deniedPathes.get() == null) {
            return true;
        }

        Set<String> privs = new HashSet<>();

        if (permissions == Permission.ADD_NODE || permissions == Permission.SET_PROPERTY || permissions == Permission.REMOVE_PROPERTY) {
            String fullPath=pr.getJCRPath(absPath);
            if (permissions == Permission.ADD_NODE && (fullPath.contains("j:translation_") || fullPath.contains("j:referenceInField_"))) {
                permissions = Permission.SET_PROPERTY;
            } else {
                absPath = absPath.getAncestor(1);
            }
        }

        for (Privilege privilege : privilegeRegistry.getPrivileges(permissions, workspaceName)) {
            privs.add(privilege.getName());
        }

        return isGranted(absPath, privs);
    }

    public boolean isGranted(Path absPath, Set<String> permissions) throws RepositoryException {
        JahiaJCRPathWrapperImpl pathImpl = new JahiaJCRPathWrapperImpl(absPath.getCanonicalPath(), pr, getSecuritySession());
        return AccessManagerUtils.isGranted(pathImpl, permissions, getSecuritySession(), jahiaPrincipal,
                workspaceName, isAliased, pathPermissionCache, compiledAcls, privilegeRegistry);
    }

    @Override
    public boolean isGranted(Path parentPath, Name childName, int permissions) throws RepositoryException {
        Path p = PathFactoryImpl.getInstance().create(parentPath, childName, true);
        return isGranted(p, permissions);
    }

    @Override
    public boolean canRead(Path path, ItemId itemId) throws RepositoryException {
        if (path != null) {
            return isGranted(path, Permission.READ);
        } else if (itemId != null) {
            return isGranted(itemId, READ);
        }
        return false;
    }

    @Override
    public boolean canAccess(String workspaceName) throws RepositoryException {
        return true;
    }

    public Set<Privilege> getPermissionsInRole(String role) throws RepositoryException {
        return AccessManagerUtils.getPermissionsInRole(role, privilegeRegistry);
    }

    public boolean matchPermission(Set<String> permissions, String role) throws RepositoryException {
        return AccessManagerUtils.matchPermission(permissions, role, isAliased, privilegeRegistry, workspaceName);
    }

    @Override
    public boolean hasPrivileges(String absPath, Privilege[] privileges) throws PathNotFoundException, RepositoryException {
        return hasPrivileges(resolver.getQPath(absPath), privileges);
    }

    public boolean hasPrivileges(Path absPath, Privilege[] privileges) throws PathNotFoundException, RepositoryException {
        logCheckedPrivileges(absPath.getString(), privileges);
        checkInitialized();
        checkValidNodePath(absPath);
        if (privileges == null || privileges.length == 0) {
            // null or empty privilege array -> return true
            if (logger.isDebugEnabled()) {
                logger.debug("No privileges passed -> allowed.");
            }
            return true;
        } else {
            Set<String> privs = new HashSet<>();

            for (Privilege privilege : privileges) {
                privs.add(privilege.getName());
            }

            return isGranted(absPath, privs);
        }
    }

    @Override
    public Privilege[] getPrivileges(String absPath) throws PathNotFoundException, RepositoryException {
        if (isAdmin(null)) {
            return getSupportedPrivileges(absPath);
        }

        return AccessManagerUtils.getPrivileges(absPath, workspaceName, jahiaPrincipal, privilegeRegistry);
    }

    @Override
    public AccessControlPolicy[] getEffectivePolicies(String absPath) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        return new AccessControlPolicy[0];
    }

    public void setAliased(boolean aliased) {
        isAliased = aliased;
    }

    public boolean isAdmin(String siteKey) {
        return AccessManagerUtils.isAdmin(siteKey, jahiaPrincipal);
    }

    public Set<String> getRoles(String absPath)  throws PathNotFoundException, RepositoryException {
        return AccessManagerUtils.getRoles(absPath, workspaceName, jahiaPrincipal);
    }

    /**
     * Flush the cache of privileges set by role
     */
    public static void flushPrivilegesInRoles() {
        AccessManagerUtils.flushPrivilegesInRoles();
    }

    public static void flushMatchingPermissions() {
        AccessManagerUtils.flushMatchingPermissions();
    }

    private void logCheckedPrivileges(String path, Privilege[] privileges) {
        if (logger.isDebugEnabled()) {
            String privs = Arrays.stream(privileges).map(Privilege::getName).collect(Collectors.joining(", "));
            logger.debug("Checking privileges for path {}: {} ", path.replaceAll("\t\\{\\}|\t", "/"), privs);
        }
    }

}
