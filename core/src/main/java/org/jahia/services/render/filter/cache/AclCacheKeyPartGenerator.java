/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.render.filter.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.*;
import org.jahia.services.query.QueryWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

/**
 * Generates an ACL cache key, containing the roles of the current user on the node paths that are used to display
 * this fragment. Multiple users having different roles on the current node path will have different keys.
 *
 * In addition to the current node path, the generator will also add the roles for the
 * main resource if cache.mainResource is set to true. The cache.dependsOnReference can contains a list of reference
 * property names, on which we should also get the list of roles. The cache.dependsOnVisibilityOf can be used to add
 * the roles of any other node. It contains a specific path or a regexp on a path.
 *
 * A specific key per user can be generated if cache.perUser is set to true. In that case, no roles at all are
 * calculated - the fragment will only be served for the current user.
 *
 * The non transformed keypart is a comma separated list of node paths - all paths on which
 * we need to get the roles. At least the current node path, flags indicating the main resource, and other paths
 * that have been added by dependsOnReference or dependsOnVisibilityOf.
 *
 * The transformed keypart contains for each path the list of roles the current user has.
 */
public class AclCacheKeyPartGenerator implements CacheKeyPartGenerator, InitializingBean {

    public static final String PER_USER = "_perUser_";
    public static final String MR_ACL = "_mraclmr_";
    public static final String PER_USER_MR_ACL = PER_USER + "," + MR_ACL;
    public static final String LOGGED_USER = "_logged_";
    public static final String GROUPS_SIGNATURE = "_groupsSignature_";
    public static final Pattern P_PATTERN = Pattern.compile("_p_");
    public static final Pattern DEP_ACLS_PATTERN = Pattern.compile("_depacl_");
    public static final Pattern ACLS_PATH_PATTERN = Pattern.compile("_p_");
    private static final String[] SUBSTITUTION_STR = new String[]{"%0", "%1", "%2"};
    private static final String[] SPECIFIC_STR = new String[]{"@@", ",", "%"};

    private static final Logger logger = LoggerFactory.getLogger(AclCacheKeyPartGenerator.class);

    private static final String CACHE_NAME = "HTMLNodeUsersACLs";
    private static final String CACHE_ALL_PRINCIPALS_ENTRY_KEY = "all principals";
    private static final String PROPERTY_CACHE_NAME = "HTMLRequiredPermissionsCache";

    private final Object objForSync = new Object();
    private final ConcurrentMap<String, Semaphore> processings = new ConcurrentHashMap<>();
    private EhCacheProvider cacheProvider;
    private Cache cache;
    private JahiaGroupManagerService groupManagerService;
    private Cache permissionCache;


    private JCRTemplate template;

    private boolean usePerUser = false;
    private boolean useGroupsSignature = false;
    
    private Set<String> groupsSignatureAclPathsToQuery;

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public void setCacheProvider(EhCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public void setTemplate(JCRTemplate template) {
        this.template = template;
    }

    public void setUsePerUser(boolean usePerUser) {
        this.usePerUser = usePerUser;
    }

    public void setUseGroupsSignature(boolean useGroupsSignature) {
        this.useGroupsSignature = useGroupsSignature;
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        CacheManager cacheManager = cacheProvider.getCacheManager();
        cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            cacheManager.addCache(CACHE_NAME);
            cache = cacheManager.getCache(CACHE_NAME);
        }

        permissionCache = cacheManager.getCache(PROPERTY_CACHE_NAME);
        if (permissionCache == null) {
            cacheManager.addCache(PROPERTY_CACHE_NAME);
            permissionCache = cacheManager.getCache(PROPERTY_CACHE_NAME);
        }
    }


    @Override
    public String getKey() {
        return "acls";
    }

    @Override
    public String getValue(Resource resource, final RenderContext renderContext, Properties properties) {
        try {
            final Set<String> aclsKeys = new TreeSet<>();

            if (usePerUser || "true".equals(properties.get(CacheUtils.FRAGMNENT_PROPERTY_CACHE_PER_USER))) {
                return PER_USER;
            }

            if (useGroupsSignature || "true".equals(properties.get(CacheUtils.FRAGMENT_PROPERTY_CACHE_GROUPS_SIGNATURE))) {
                return GROUPS_SIGNATURE;
            }

            JCRNodeWrapper node = resource.getNode();
            final String nodePath = node.getPath();

            aclsKeys.add(encodeSpecificChars(nodePath));

            String s = (String) properties.get("cache.dependsOnVisibilityOf");
            if (s != null) {
                String[] dependencies = s.split(",");
                for (int i = 0; i < dependencies.length; i++) {
                    String dep = dependencies[i];
                    dep = dep.replace("$currentNode", nodePath);
                    dep = dep.replace("$currentSite", renderContext.getSite().getPath());
                    dep = dep.replace("$mainResource", renderContext.getMainResource().getNode().getPath());
                    aclsKeys.add("*" + encodeSpecificChars(dep));
                }
            }

            String ref = (String) properties.get("cache.dependsOnReference");
            if (ref != null && ref.length() > 0) {
                String[] refProperties = ref.split(",");
                for (int i = 0; i < refProperties.length; i++) {
                    String refPropertyName = refProperties[i];
                    if (node.hasProperty(refPropertyName)) {
                        JCRPropertyWrapper refProperty = node.getProperty(refPropertyName);
                        JCRSessionWrapper systemSession = JCRSessionFactory.getInstance().getCurrentSystemSession(node.getSession().getWorkspace().getName(), node.getSession().getLocale(), null);
                        if (refProperty != null) {
                            int propertyRequiredType = refProperty.getDefinition().getRequiredType();
                            if (propertyRequiredType == PropertyType.REFERENCE || propertyRequiredType == PropertyType.WEAKREFERENCE) {
                                if (refProperty.isMultiple() && refProperty.getValues().length > 0) {
                                    for (JCRValueWrapper value : refProperty.getValues()) {
                                        addReferenceDependency(value.getString(), aclsKeys, systemSession);
                                    }
                                } else {
                                    addReferenceDependency(refProperty.getString(), aclsKeys, systemSession);
                                }
                            }
                        }
                    } else {
                        logger.debug("Trying to add cache dependency for reference but property '{}' not found on node '{}'", refPropertyName, nodePath);
                    }
                }
            }

            Element element = permissionCache.get(node.getPath());
            Boolean[] values;
            if (element != null && element.getObjectValue() != null) {
                values = (Boolean[]) element.getObjectValue();
            } else {
                values = new Boolean[3];
                values[0] = node.hasProperty("j:requiredPermissionNames") || node.hasProperty("j:requiredPermissions");
                values[1] = node.hasProperty("j:requirePrivilegedUser") && node.getProperty("j:requirePrivilegedUser").getBoolean();
                values[2] = node.hasProperty("j:requireLoggedUser") && node.getProperty("j:requireLoggedUser").getBoolean();
                permissionCache.put(new Element(node.getPath(), values));
            }

            if ("true".equals(properties.get("cache.mainResource"))) {
                aclsKeys.add(MR_ACL);
            } else {
                if (values[0]) {
                    aclsKeys.add(MR_ACL);
                }
            }
            if (values[1]) {
                aclsKeys.add(renderContext.getSite().getPath());
            }
            if (values[2] || "true".equals(properties.get("cache.useLoggedInState"))) {
                aclsKeys.add(LOGGED_USER);
            }

            return StringUtils.join(aclsKeys, ",");

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return "";

    }
    
    private void addReferenceDependency(String identifier, Set<String> aclsKeys, JCRSessionWrapper systemSession) throws RepositoryException {
        try {
            Node refNode = systemSession.getNodeByIdentifier(identifier);
            aclsKeys.add(encodeSpecificChars(refNode.getPath()));
        } catch (ItemNotFoundException e) {
            logger.debug("Trying to add cache dependency for reference but reference node '{}' not found", identifier);
        }
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        String[] paths = keyPart.split(",");
        Map<String, Set<String>> rolesForPath = new TreeMap<>();
        StringBuilder r = new StringBuilder();
        try {
            List<PrincipalAcl> principalAclList = null;

            for (String s : paths) {
                if (s.equals(PER_USER)) {
                    if (r.length() > 0) {
                        r.append("|");
                    }
                    r.append(renderContext.getUser().getUserKey());
                } else if (s.equals(LOGGED_USER)) {
                    if (r.length() > 0) {
                        r.append("|");
                    }
                    r.append(Boolean.toString(renderContext.getUser().getName().equals(JahiaUserManagerService.GUEST_USERNAME)));
                } else if (s.equals(GROUPS_SIGNATURE)) {
                    r.append(getGroupsSignature(renderContext.getUser()));
                } else {
                    if (principalAclList == null) {
                        principalAclList = getUserAcl(renderContext.getUser());
                    }
                    if (s.equals(MR_ACL)) {
                        // Calculate roles for the main resource
                        populateRolesForPath(renderContext.getMainResource().getNode().getPath(), principalAclList, rolesForPath);
                    } else if (s.startsWith("*")) {
                        // Path starting by * are regular expressions
                        String decodedNodePath = decodeSpecificChars(s.substring(1));
                        populateRolesForPathPattern(Pattern.compile(decodedNodePath), principalAclList, rolesForPath);
                    } else {
                        populateRolesForPath(decodeSpecificChars(s), principalAclList, rolesForPath);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);  //To change body of catch statement use File | Settings | File Templates.
        }

        for (Map.Entry<String, Set<String>> entry : rolesForPath.entrySet()) {
            if (r.length() > 0) {
                r.append("|");
            }
            r.append(StringUtils.join(entry.getValue(), ",")).append(":").append(entry.getKey());
        }

        String replacedKeyPart = StringUtils.replace(r.toString(), "@@", "%0");
        logger.debug("ACL keypart : {} = {}", keyPart, replacedKeyPart);
        return replacedKeyPart;
    }

    /**
     * Fill the map passed as parameter with the roles for the corresponding node path, defined for this user.
     * @param nodePath The path to look for in the Principal ACLs
     * @param principalAcls List of all PrincipalAcl that apply to the current user
     * @param rolesForPath The final map where to put the results, with all roles per paths
     */
    private void populateRolesForPath(String nodePath, List<PrincipalAcl> principalAcls, Map<String, Set<String>> rolesForPath) {
        for (PrincipalAcl principalAcl : principalAcls) {
            principalAcl.fillRolesForPath(nodePath, rolesForPath);
        }
    }

    /**
     * Check all known path (from all Principal ACL) that could match the pattern, and populates roles for these path.
     * @param pattern Pattern to check on all known paths
     * @param principalAcls List of all PrincipalAcl that apply to the current user
     * @param rolesForPath The final map where to put the results, with all roles per paths
     */
    private void populateRolesForPathPattern(Pattern pattern, List<PrincipalAcl> principalAcls, Map<String, Set<String>> rolesForPath) {
        Set<String> paths = new HashSet<>();
        for (PrincipalAcl principalAcl : principalAcls) {
            principalAcl.fillMatchingPaths(pattern, paths);
        }
        for (String path : paths) {
            populateRolesForPath(path, principalAcls, rolesForPath);
        }
    }

    /**
     * Get all PrincipalAcl that applies to the current user. The first one of the list is the ACL that applies
     * directly on the current user, the following ones are the ACLs that applies on his groups.
     * @param principal The user
     * @return List of PrincipalAcl
     * @throws RepositoryException
     */
    private String getGroupsSignature(JahiaUser principal) throws RepositoryException {
        if (principal.isRoot()) {
            return "u:" + principal.getName();
        }

        SortedSet<String> principals = new TreeSet<>();
        Set<String> all = getAllPrincipalsWithAcl();
        addPrincipalIfAcl(principals, all, "u:" + principal.getName());

        List<String> groups = groupManagerService.getMembershipByPath(principal.getLocalPath());
        for (String group : groups) {
            addPrincipalIfAcl(principals, all, "g:" + StringUtils.substringAfterLast(group, "/"));
        }

        // Adds the hashcode of all list to ensure the key is generated with same set of principals having ACE
        return Integer.toString(all.hashCode()) + principals;
    }

    private void addPrincipalIfAcl(Collection<String> principalAcl, Collection<String> all, String principal) {
        if (all.contains(principal)) {
            principalAcl.add(principal);
        }
    }

    /**
     * Get all PrincipalAcl that applies to the current user. The first one of the list is the ACL that applies
     * directly on the current user, the following ones are the ACLs that applies on his groups.
     * @param principal The user
     * @return List of PrincipalAcl
     * @throws RepositoryException
     */
    private List<PrincipalAcl> getUserAcl(JahiaUser principal) throws RepositoryException {
        List<PrincipalAcl> principalAcl = new ArrayList<>();
        principalAcl.add(getPrincipalAcl("u:" + principal.getName(), principal.getRealm()));

        List<String> groups = groupManagerService.getMembershipByPath(principal.getLocalPath());
        for (String group : groups) {
            principalAcl.add(getPrincipalAcl("g:" + StringUtils.substringAfterLast(group, "/"), JCRContentUtils.getSiteKey(group)));
        }

        return principalAcl;
    }

    private PrincipalAcl getPrincipalAcl(final String principallKey, final String siteKey) throws RepositoryException {
        String cacheKey = siteKey != null ? principallKey + ":" + siteKey : principallKey;
        Element element = cache.get(cacheKey);
        if (element == null) {
            element = generateCacheEntry(cacheKey, new CacheEntryNotFoundCallback() {
            @Override
            public Object generateCacheEntry() throws RepositoryException {
                return  template.doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, new JCRCallback<PrincipalAcl>() {

                    @Override
                    public PrincipalAcl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        Query query = session.getWorkspace().getQueryManager().createQuery(
                                "select * from [jnt:ace] as ace where ace.[j:principal] = '" + JCRContentUtils.sqlEncode(principallKey) + "'",
                                Query.JCR_SQL2);
                        QueryResult queryResult = query.execute();
                        NodeIterator rowIterator = queryResult.getNodes();

                        Map<String, Set<String>> mapGranted = new LinkedHashMap<>();
                        Map<String, Set<String>> mapDenied = new LinkedHashMap<>();

                        while (rowIterator.hasNext()) {
                            JCRNodeWrapper node = (JCRNodeWrapper) rowIterator.next();
                            if (siteKey != null && !node.getResolveSite().getName().equals(siteKey)) {
                                continue;
                            }
                            String path = node.getParent().getParent().getPath();
                            Set<String> foundRoles = new HashSet<>();
                            boolean granted = "GRANT".equals(node.getProperty("j:aceType").getString());
                            Value[] roles = node.getProperty(Constants.J_ROLES).getValues();
                            for (Value r : roles) {
                                String role = r.getString();
                                if (!foundRoles.contains(role)) {
                                    foundRoles.add(role);
                                }
                            }
                            if ("/".equals(path)) {
                                path = "";
                            }
                            if (granted) {
                                mapGranted.put(path, foundRoles);
                            } else {
                                mapDenied.put(path, foundRoles);
                            }
                        }

                        return new PrincipalAcl(mapGranted, mapDenied);
                    }
                });
            }
          });
          }
        
        return (PrincipalAcl) element.getObjectValue();
    }

    @SuppressWarnings("unchecked")
    public Set<String> getAllPrincipalsWithAcl() throws RepositoryException {
      Element element = cache.get(CACHE_ALL_PRINCIPALS_ENTRY_KEY);
      if (element == null) {
          // generate it
          element = generateCacheEntry(CACHE_ALL_PRINCIPALS_ENTRY_KEY, new CacheEntryNotFoundCallback() {
            @Override
            public Object generateCacheEntry() throws RepositoryException {
                return JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, new JCRCallback<Set<String>>() {
                    @Override
                    public Set<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        long startTime = System.currentTimeMillis();
                        Set<String> results = new HashSet<>();
                        String queryStatement = getAllPrincipalsWithAclQuery();
                        QueryWrapper query = session.getWorkspace().getQueryManager().createQuery(
                        queryStatement, Query.JCR_SQL2);
                        RowIterator ri = query.execute().getRows();
                        while (ri.hasNext()) {
                            Row row = ri.nextRow();
                            Value v = row.getValue("j:principal");
                            String principal = v !=  null ? v.getString() : null;
                            if (StringUtils.isEmpty(principal)) {
                                continue;
                            }
                            String path = row.getValue("jcr:path").getString();
                            if (path.startsWith("/users/") || path.startsWith("/sites/") && path.contains("/users/") && path.startsWith(session.getNode(path).getResolveSite().getPath() + "/users/")) {
                                continue;
                            }
                            results.add(principal);
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "getAllPrincipalsWithAcl query ({}) brought {} nodes back; collected {} principals; finished in {} ms",
                                    new Object[] { queryStatement, ri.getSize(), results.size(), System.currentTimeMillis() - startTime });
                        }
                        return results;
                    }
                });
            }
          });
      }
      
      return (Set<String>) element.getObjectValue();
    }

    private String getAllPrincipalsWithAclQuery() {
        final String queryBase = "SELECT [jcr:path],[j:principal] FROM [jnt:ace]";
        if (groupsSignatureAclPathsToQuery == null) {
            return queryBase;
        }
        StringBuilder q = new StringBuilder(128);
        q.append(queryBase).append(" WHERE ");
        boolean first = true;
        for (String path : groupsSignatureAclPathsToQuery) {
            if (!first) {
                q.append(" OR");
            } else {
                first = false;
            }

            q.append(" ISDESCENDANTNODE('").append(JCRContentUtils.sqlEncode(path)).append("')");
        }

        return q.toString();
    }

    private Element generateCacheEntry(final String cacheKey, CacheEntryNotFoundCallback callback) throws RepositoryException {
        Element element = null;
        Semaphore semaphore = processings.get(cacheKey);
        if (semaphore == null) {
            semaphore = new Semaphore(1);
            processings.putIfAbsent(cacheKey, semaphore);
        }
        try {
            semaphore.acquire();

            element = cache.get(cacheKey);
            if (element != null) {
                return element;
            }

            logger.debug("Getting ACL for {}", cacheKey);
            long l = System.currentTimeMillis();

            element = new Element(cacheKey, callback.generateCacheEntry());
            element.setEternal(true);
            cache.put(element);
            logger.debug("Getting ACL for {} took {} ms", cacheKey, System.currentTimeMillis() - l);
        } catch (InterruptedException e) {
            logger.debug(e.getMessage(), e);
            Thread.currentThread().interrupt();
        } finally {
            semaphore.release();
        }
        return element;
    }

    private interface CacheEntryNotFoundCallback {
        Object generateCacheEntry() throws RepositoryException;
    }

    public void flushUsersGroupsKey() {
        flushUsersGroupsKey(true);
    }

    public void flushUsersGroupsKey(boolean propageToOtherClusterNodes) {
        synchronized (objForSync) {
            cache.removeAll(!propageToOtherClusterNodes);
            cache.flush();
            logger.debug("Flushed HTMLNodeUsersACLs cache");
        }
    }

    public void flushUsersGroupsKey(String key, boolean propagateToOtherClusterNodes) {
        synchronized (objForSync) {
            cache.remove(key, !propagateToOtherClusterNodes);
            Element element = cache.get(CACHE_ALL_PRINCIPALS_ENTRY_KEY);
            if (element != null) {
                @SuppressWarnings("unchecked")
                Set<String> allPrincipalsWithAcl = (Set<String>) element.getObjectValue();
                if (key.lastIndexOf(':') == 1 && !allPrincipalsWithAcl.contains(key)) {
                    // The principal is not present in the list, flush the list
                    // Don't need to flush it if a principal has been removed from all ACEs
                    cache.remove(CACHE_ALL_PRINCIPALS_ENTRY_KEY, !propagateToOtherClusterNodes);
                }
            }
        }
    }

    public void flushPermissionCacheEntry(String path, boolean propagateToOtherClusterNodes) {
        synchronized (objForSync) {
            permissionCache.remove(path, !propagateToOtherClusterNodes);
        }
    }

    private String encodeSpecificChars(String toEncode) {
        return StringUtils.replaceEach(toEncode, SPECIFIC_STR, SUBSTITUTION_STR);
    }

    private String decodeSpecificChars(String toDecode) {
        return StringUtils.replaceEach(toDecode, SUBSTITUTION_STR, SPECIFIC_STR);
    }

    public void setGroupsSignatureAclPathsToQuery(String paths) {
        groupsSignatureAclPathsToQuery = null;
        if (StringUtils.isNotBlank(paths)) {
            groupsSignatureAclPathsToQuery = new LinkedHashSet<>(Arrays.asList(StringUtils.split(paths, ", ")));
        }
    }

}