/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
    public static final String LOGGED_USER = "_logged_";
    public static final Pattern P_PATTERN = Pattern.compile("_p_");
    public static final Pattern DEP_ACLS_PATTERN = Pattern.compile("_depacl_");
    public static final Pattern ACLS_PATH_PATTERN = Pattern.compile("_p_");
    private static final String[] SUBSTITUTION_STR = new String[]{"%0","%1","%2"};
    private static final String[] SPECIFIC_STR = new String[]{"@@",",","%"};

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
            if ("true".equals(properties.get("cache.perUser"))) {
                return PER_USER;
            }

            JCRNodeWrapper node = resource.getNode();
            String nodePath = node.getPath();
            final Set<String> aclsKeys = new TreeSet<String>();

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

            Element element = permissionCache.get(node.getPath());
            Boolean[] values;
            if (element != null &&  element.getObjectValue() != null) {
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
        if (keyPart.equals(PER_USER)) {
            keyPart = renderContext.getUser().getUserKey();
        } else {
            String[] paths = keyPart.split(",");
            SortedSet<String> aclKeys = new TreeSet<String>();
            for (String s : paths) {
                try {
                    if (s.equals(MR_ACL)) {
                        aclKeys.add(getAclKeyPartForNode(renderContext, renderContext.getMainResource().getNode().getPath(), renderContext.getUser(), new HashSet<String>(), false));
                    } else if (s.equals(LOGGED_USER)) {
                        aclKeys.add(Boolean.toString(renderContext.getUser().getName().equals(JahiaUserManagerService.GUEST_USERNAME)));
                    } else if (s.startsWith("*")) {
                        aclKeys.add(getAclKeyPartForNode(renderContext, decodeSpecificChars(s.substring(1)), renderContext.getUser(), new HashSet<String>(), true));
                    } else {
                        aclKeys.add(getAclKeyPartForNode(renderContext, decodeSpecificChars(s), renderContext.getUser(), new HashSet<String>(), false));
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(),e);  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            keyPart = StringUtils.join(aclKeys, "|");
        }
//        try {
//            byte[] b = DigestUtils.getSha256Digest().digest(keyPart.getBytes("UTF-8"));
//            StringWriter sw = new StringWriter();
//            Base64.encode(b, 0, b.length, sw);
//            return sw.toString();
//        } catch (Exception e) {
//            logger.warn("Issue while digesting key", e);
//        }
//
        return keyPart;
    }

    private void populateRolesForKey(Pattern regexp, Map<String, Set<String>> principalAcl, String nodePath, Map<String, Set<String>> rolesForKey , Set<String> aclPathChecked) {
        if (regexp == null) {
            for (Map.Entry<String, Set<String>> entry : principalAcl.entrySet()) {
                String grantPath = entry.getKey() + "/";
                if ((nodePath.startsWith(grantPath) || grantPath.startsWith(nodePath)) && !aclPathChecked.contains(entry.getKey())) {
                    Set<String> roles = entry.getValue();
                    if (!rolesForKey.containsKey(entry.getKey())) {
                        rolesForKey.put(entry.getKey(), new TreeSet<String>(roles));
                    } else {
                        rolesForKey.get(entry.getKey()).addAll(roles);
                    }
                }
            }
        } else {
            for (Map.Entry<String, Set<String>> entry : principalAcl.entrySet()) {
                if (regexp.matcher(entry.getKey()).matches()) {
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

    public String getAclKeyPartForNode(RenderContext renderContext, String nodePath,
                                       JahiaUser principal, Set<String> aclPathChecked, boolean regexp)
            throws RepositoryException {
        Pattern p =  null;

        if(!regexp) {
            nodePath += "/";
        } else {
            p = Pattern.compile(nodePath);
        }

        Map<String, Set<String>> rolesForKey = new TreeMap<String, Set<String>>();
        populateRolesForKey(p, getPrincipalAcl("u:" + principal.getName(), principal.getRealm()), nodePath, rolesForKey, aclPathChecked);

        List<String> groups = groupManagerService.getMembershipByPath(principal.getLocalPath());

        for (String group : groups) {
            populateRolesForKey(p, getPrincipalAcl("g:" + StringUtils.substringAfterLast(group, "/"),
                    group.startsWith("/sites/") ? StringUtils.substringBetween(group, "/sites/", "/") : null), nodePath, rolesForKey, aclPathChecked);
        }

        aclPathChecked.addAll(rolesForKey.keySet());
        StringBuilder r = new StringBuilder();
        for (Map.Entry<String, Set<String>> entry : rolesForKey.entrySet()) {
            r.append((StringUtils.join(entry.getValue(), ","))).append(":").append(entry.getKey()).append("|");
        }
        return StringUtils.replace(r.toString(), "@@", "%0");
    }

    private final ConcurrentMap<String, Semaphore> processings = new ConcurrentHashMap<String, Semaphore>();

    private Map<String, Set<String>> getPrincipalAcl(final String aclKey, final String siteKey) throws RepositoryException {
        final String cacheKey = aclKey + ":" + siteKey;
        Element element = cache.get(cacheKey);
        if (element == null) {
            Semaphore semaphore = processings.get(cacheKey);
            if(semaphore==null) {
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
                    @SuppressWarnings("unchecked")
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
                                    mapGranted.get(grantedPath).removeAll(intersection);
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