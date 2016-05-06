/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.usermanager.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * User: toto
 * Date: 11/20/12
 * Time: 12:24 PM
 */
public class AclCacheKeyPartGenerator implements CacheKeyPartGenerator, InitializingBean {

    public static final String PER_USER = "_perUser_";
    public static final String MR_ACL = "_mraclmr_";
    public static final String PER_USER_MR_ACL = PER_USER + "," + MR_ACL;
    public static final String LOGGED_USER = "_logged_";
    public static final Pattern P_PATTERN = Pattern.compile("_p_");
    public static final Pattern DEP_ACLS_PATTERN = Pattern.compile("_depacl_");
    public static final Pattern ACLS_PATH_PATTERN = Pattern.compile("_p_");
    private static final String[] SUBSTITUTION_STR = new String[]{"%0", "%1", "%2"};
    private static final String[] SPECIFIC_STR = new String[]{"@@", ",", "%"};

    private static final Logger logger = LoggerFactory.getLogger(AclCacheKeyPartGenerator.class);

    private static final String CACHE_NAME = "HTMLNodeUsersACLs";
    private static final String PROPERTY_CACHE_NAME = "HTMLRequiredPermissionsCache";

    private final Object objForSync = new Object();
    private EhCacheProvider cacheProvider;
    private Cache cache;
    private JahiaGroupManagerService groupManagerService;
    private Cache permissionCache;


    private JCRTemplate template;

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public void setCacheProvider(EhCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public void setTemplate(JCRTemplate template) {
        this.template = template;
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

            final Set<String> aclsKeys = new TreeSet<String>();

            if ("true".equals(properties.get(CacheUtils.FRAGMNENT_PROPERTY_CACHE_PER_USER))) {
                aclsKeys.add(PER_USER);
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
                                        try {
                                            Node refNode = systemSession.getNodeByIdentifier(value.getString());
                                            aclsKeys.add(encodeSpecificChars(refNode.getPath()));
                                        } catch (ItemNotFoundException e) {
                                            logger.debug("Trying to add cache dependency for reference but reference node '{}' not found", refProperty.getString());
                                        }
                                    }
                                } else {
                                    try {
                                        Node refNode = systemSession.getNodeByIdentifier(refProperty.getString());
                                        aclsKeys.add(encodeSpecificChars(refNode.getPath()));
                                    } catch (ItemNotFoundException e) {
                                        logger.debug("Trying to add cache dependency for reference but reference node '{}' not found", refProperty.getString());
                                    }
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

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        String[] paths = keyPart.split(",");
        Map<String, Set<String>> rolesForKey = new TreeMap<String, Set<String>>();
        StringBuilder r = new StringBuilder();
        try {
            List<Map<String, Set<String>>> principalAcl = null;

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
                } else {
                    if (principalAcl == null) {
                        principalAcl = getUserAcl(renderContext.getUser());
                    }
                    if (s.equals(MR_ACL)) {
                        populateRolesForKey(renderContext.getMainResource().getNode().getPath(), principalAcl, rolesForKey, null);
                    } else if (s.startsWith("*")) {
                        String decodedNodePath = decodeSpecificChars(s.substring(1));
                        populateRolesForKey(decodedNodePath, principalAcl, rolesForKey, Pattern.compile(decodedNodePath));
                    } else {
                        populateRolesForKey(decodeSpecificChars(s), principalAcl, rolesForKey, null);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);  //To change body of catch statement use File | Settings | File Templates.
        }

        for (Map.Entry<String, Set<String>> entry : rolesForKey.entrySet()) {
            if (r.length() > 0) {
                r.append("|");
            }
            r.append((StringUtils.join(entry.getValue(), ","))).append(":").append(entry.getKey());
        }

        keyPart = StringUtils.replace(r.toString(), "@@", "%0");

        return keyPart;
    }

    private void populateRolesForKey(String nodePath, List<Map<String, Set<String>>> principalAcl, Map<String, Set<String>> rolesForKey, Pattern pattern) {
        if (pattern == null) {
            nodePath += "/";
            for (Map<String, Set<String>> map : principalAcl) {
                for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
                    String grantPath = entry.getKey() + "/";
                    if ((nodePath.startsWith(grantPath) || grantPath.startsWith(nodePath))) {
                        Set<String> roles = entry.getValue();
                        if (!rolesForKey.containsKey(entry.getKey())) {
                            rolesForKey.put(entry.getKey(), new TreeSet<String>(roles));
                        } else {
                            rolesForKey.get(entry.getKey()).addAll(roles);
                        }
                    }
                }
            }
        } else {
            for (Map<String, Set<String>> map : principalAcl) {
                for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
                    if (pattern.matcher(entry.getKey()).matches()) {
                        Set<String> roles = entry.getValue();
                        if (!rolesForKey.containsKey(entry.getKey())) {
                            rolesForKey.put(entry.getKey(), new TreeSet<String>(roles));
                        } else {
                            rolesForKey.get(entry.getKey()).addAll(roles);
                        }
                    }
                }
            }
        }
    }

    private List<Map<String, Set<String>>> getUserAcl(JahiaUser principal) throws RepositoryException {
        List<Map<String, Set<String>>> principalAcl = new ArrayList<>();
        principalAcl.add(getPrincipalAcl("u:" + principal.getName(), principal.getRealm()));

        List<String> groups = groupManagerService.getMembershipByPath(principal.getLocalPath());
        for (String group : groups) {
            principalAcl.add(getPrincipalAcl("g:" + StringUtils.substringAfterLast(group, "/"), JCRContentUtils.getSiteKey(group)));
        }

        return principalAcl;
    }

    private final ConcurrentMap<String, Semaphore> processings = new ConcurrentHashMap<String, Semaphore>();

    @SuppressWarnings("unchecked")
    private Map<String, Set<String>> getPrincipalAcl(final String aclKey, final String siteKey) throws RepositoryException {

        final String cacheKey = siteKey != null ? aclKey + ":" + siteKey : aclKey;
        Element element = cache.get(cacheKey);
        if (element == null) {
            Semaphore semaphore = processings.get(cacheKey);
            if (semaphore == null) {
                semaphore = new Semaphore(1);
                processings.putIfAbsent(cacheKey, semaphore);
            }
            try {
                semaphore.tryAcquire(500, TimeUnit.MILLISECONDS);
                element = cache.get(cacheKey);
                if (element != null) {
                    return (Map<String, Set<String>>) element.getObjectValue();
                }

                logger.debug("Getting ACL for {}", cacheKey);
                long l = System.currentTimeMillis();

                Map<String, Set<String>> map = template.doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, new JCRCallback<Map<String, Set<String>>>() {

                    @Override
                    public Map<String, Set<String>> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        Query query = session.getWorkspace().getQueryManager().createQuery(
                                "select * from [jnt:ace] as ace where ace.[j:principal] = '" + JCRContentUtils.sqlEncode(aclKey) + "'",
                                Query.JCR_SQL2);
                        QueryResult queryResult = query.execute();
                        NodeIterator rowIterator = queryResult.getNodes();

                        Map<String, Set<String>> mapGranted = new ConcurrentHashMap<String, Set<String>>();
                        Map<String, Set<String>> mapDenied = new LinkedHashMap<String, Set<String>>();

                        while (rowIterator.hasNext()) {
                            JCRNodeWrapper node = (JCRNodeWrapper) rowIterator.next();
                            if (siteKey != null && !node.getResolveSite().getName().equals(siteKey)) {
                                continue;
                            }
                            String path = node.getParent().getParent().getPath();
                            Set<String> foundRoles = new HashSet<String>();
                            boolean granted = node.getProperty("j:aceType").getString().equals("GRANT");
                            Value[] roles = node.getProperty(Constants.J_ROLES).getValues();
                            for (Value r : roles) {
                                String role = r.getString();
                                if (!foundRoles.contains(role)) {
                                    foundRoles.add(role);
                                }
                            }
                            if (path.equals("/")) {
                                path = "";
                            }
                            if (granted) {
                                mapGranted.put(path, foundRoles);
                            } else {
                                mapDenied.put(path, foundRoles);
                            }
                        }
                        for (String deniedPath : mapDenied.keySet()) {
                            String grantedPath = deniedPath;
                            while (grantedPath.length() > 0) {
                                grantedPath = StringUtils.substringBeforeLast(grantedPath, "/");
                                if (mapGranted.containsKey(grantedPath)) {
                                    Collection<String> intersection = CollectionUtils.intersection(mapGranted.get(grantedPath), mapDenied.get(deniedPath));
                                    for (String s : intersection) {
                                        mapGranted.get(grantedPath).add(s + " -> " + deniedPath);
                                    }
                                }
                            }
                        }
                        return mapGranted;
                    }
                });
                element = new Element(cacheKey, map);
                element.setEternal(true);
                cache.put(element);
                logger.debug("Getting ACL for {} took {} ms", cacheKey, System.currentTimeMillis() - l);
            } catch (InterruptedException e) {
                logger.debug(e.getMessage(), e);
            } finally {
                semaphore.release();
            }
        }
        return (Map<String, Set<String>>) element.getObjectValue();
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

    public void flushUsersGroupsKey(String key, boolean propageToOtherClusterNodes) {
        synchronized (objForSync) {
            cache.remove(key, !propageToOtherClusterNodes);
        }
    }

    public void flushPermissionCacheEntry(String path, boolean propageToOtherClusterNodes) {
        synchronized (objForSync) {
            permissionCache.remove(path);
        }
    }

    private String encodeSpecificChars(String toEncode) {
        return StringUtils.replaceEach(toEncode, SPECIFIC_STR, SUBSTITUTION_STR);
    }

    private String decodeSpecificChars(String toDecode) {
        return StringUtils.replaceEach(toDecode, SUBSTITUTION_STR, SPECIFIC_STR);
    }
}