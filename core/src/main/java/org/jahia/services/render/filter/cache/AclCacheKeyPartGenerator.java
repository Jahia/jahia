/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.jahia.api.Constants;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.security.AccessControlManager;
import java.util.*;
import java.util.regex.Pattern;

/**
 * User: toto
 * Date: 11/20/12
 * Time: 12:24 PM
 */
public class AclCacheKeyPartGenerator implements CacheKeyPartGenerator, InitializingBean {
    public static final String PER_USER = "_perUser_";
    public static final Pattern P_PATTERN = Pattern.compile("_p_");
    public static final Pattern DEP_ACLS_PATTERN = Pattern.compile("_depacl_");
    public static final Pattern ACLS_PATH_PATTERN = Pattern.compile("_p_");

    private static final Logger logger = LoggerFactory.getLogger(AclCacheKeyPartGenerator.class);

    private static final String CACHE_NAME = "HTMLNodeUsersACLs";
    private static final String PROPERTY_CACHE_NAME = "HTMLRequiredPermissionsCache";

    private final Object objForSync = new Object();
    private EhCacheProvider cacheProvider;
    private Cache cache;
    private JahiaGroupManagerService groupManagerService;
    private JahiaUserManagerService userManagerService;
    private Map<String, Set<JahiaGroup>> aclGroups = new LinkedHashMap<String, Set<JahiaGroup>>();
    private Cache permissionCache;


    private JCRTemplate template;

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
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
        cache.setStatisticsEnabled(cacheProvider.isStatisticsEnabled());

        permissionCache = cacheManager.getCache(PROPERTY_CACHE_NAME);
        if (permissionCache == null) {
            cacheManager.addCache(PROPERTY_CACHE_NAME);
            permissionCache = cacheManager.getCache(PROPERTY_CACHE_NAME);
        }
        permissionCache.setStatisticsEnabled(cacheProvider.isStatisticsEnabled());
    }


    @Override
    public String getKey() {
        return "acls";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext) {
        return appendAcls(resource, renderContext, true);
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        if (!keyPart.contains(AclCacheKeyPartGenerator.PER_USER)) {
            String[] split = P_PATTERN.split(keyPart);
            String nodePath = "/" + StringUtils.substringAfter(split[1], "/");

            try {
                return getAclsKeyPart(renderContext,
                        Boolean.parseBoolean(StringUtils.substringBefore(split[1], "/")), nodePath, true, keyPart);
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        } else {
            keyPart = StringUtils.replace(keyPart, PER_USER, renderContext.getUser().getUsername());
        }

        return keyPart;
    }

    public String appendAcls(final Resource resource, final RenderContext renderContext, boolean appendNodePath) {
        try {
            if (renderContext.getRequest() != null && Boolean.TRUE.equals(renderContext.getRequest().getAttribute("cache.perUser"))) {
                return PER_USER;
            }

            JCRNodeWrapper node = resource.getNode();
            boolean checkRootPath = true;
            Element element = permissionCache.get(node.getPath());
            if(element!=null && Boolean.TRUE==((Boolean)element.getValue())) {
                node = renderContext.getMainResource().getNode();
                checkRootPath = false;
            } else if(element==null) {
                if (node.hasProperty("j:requiredPermissions")) {
                    permissionCache.put(new Element(node.getPath(),Boolean.TRUE));
                    node = renderContext.getMainResource().getNode();
                    checkRootPath = false;
                } else {
                    permissionCache.put(new Element(node.getPath(),Boolean.FALSE));
                }
            }
            String nodePath = node.getPath();
            final Set<String> aclsKeys = new LinkedHashSet<String>();
            aclsKeys.add(getAclsKeyPart(renderContext, checkRootPath, nodePath, appendNodePath, null));
            final Set<String> dependencies = resource.getDependencies();

            if (renderContext.getRequest() != null && Boolean.TRUE.equals(renderContext.getRequest().getAttribute("cache.mainResource"))) {
                aclsKeys.add("mraclmr");
            } else {
                for (final String dependency : dependencies) {
                    if (!dependency.equals(nodePath)) {
                        try{
                            if (!dependency.contains("/")) {
                                final boolean finalCheckRootPath = checkRootPath;
                                JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, new JCRCallback<Object>() {
                                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                        final JCRNodeWrapper nodeByIdentifier = session.getNodeByIdentifier(dependency);
                                        aclsKeys.add(getAclsKeyPart(renderContext, finalCheckRootPath,
                                                nodeByIdentifier.getPath(), true, null));
                                        return null;
                                    }
                                });
                            } else {
                                aclsKeys.add(getAclsKeyPart(renderContext, checkRootPath, dependency, true, null));
                            }
                        } catch(ItemNotFoundException ex) {
                            logger.warn("ItemNotFound: " + dependency + "  it could be an invalid reference, check jcr integrity");
                        } catch (PathNotFoundException ex) {
                            logger.warn("PathNotFound: "
                                    + dependency
                                    + "  it could be an invalid reference, check jcr integrity");
                        }                                               
                    }
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (String aclsKey : aclsKeys) {
                if(stringBuilder.length()>0) {
                    stringBuilder.append("_depacl_");
                }
                stringBuilder.append(aclsKey);
            }
            return stringBuilder.toString();

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return "";

    }

    public String getAclsKeyPart(RenderContext renderContext, boolean checkRootPath, String nodePath,
                                 boolean appendNodePath, String acls)
            throws RepositoryException {
        // Search for user specific acl
        JahiaUser principal = renderContext.getUser();
        final String userName = principal.getUsername();
        if (nodePath.contains("_depacl_")) {
            String[] aclKeys = DEP_ACLS_PATTERN.split(acls);
            StringBuilder stringBuilder = new StringBuilder();
            for (String aclKey : aclKeys) {
                if (aclKey.contains("/")) {
                    String path = "";
                    final String[] aclRolePaths = ACLS_PATH_PATTERN.split(aclKey);
                    if (aclRolePaths.length == 2) {
                        path = "/" + StringUtils.substringAfter(aclRolePaths[1], "/");
                        if (stringBuilder.length() > 0) {
                            stringBuilder.append("_depacl_");
                        }
                        stringBuilder.append(getAclKeyPartForNode(renderContext, checkRootPath, path, true, principal,
                                userName));
                    }
                }else if ("mraclmr".equals(aclKey)) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append("_depacl_");
                    }
                    stringBuilder.append(getAclKeyPartForNode(renderContext, checkRootPath, renderContext.getMainResource().getNode().getCanonicalPath(), true, principal,
                            userName));
                }
            }
            return stringBuilder.toString();
        } else {
            return getAclKeyPartForNode(renderContext, checkRootPath, nodePath, appendNodePath, principal, userName);
        }
    }

    @SuppressWarnings("unchecked")
    private String getAclKeyPartForNode(RenderContext renderContext, boolean checkRootPath, String nodePath,
                                        boolean appendNodePath, JahiaUser principal, String userName)
            throws RepositoryException {
        Element element = hasUserAcl(userName);
        if (element != null) {
            Map<String, String> map = (Map<String, String>) element.getValue();
            String path = nodePath;
            while ((!path.equals("")) && !map.containsKey(path) && !aclGroups.containsKey(path)) {
                path = StringUtils.substringBeforeLast(path, "/");
            }
            if (checkRootPath) {
                if (path.equals("")) {
                    path = "/";
                }
            }
            if (map.containsKey(path)) {
                return (String) map.get(path) + "_p_" + checkRootPath + (appendNodePath ? nodePath : "");
            }
        }
        StringBuilder b = new StringBuilder();
        String path = nodePath;
        Map<String, Set<JahiaGroup>> allAclsGroups = getAllAclsGroups();
        if (checkRootPath) {
            while (!allAclsGroups.containsKey(path) && !path.equals("")) {
                path = StringUtils.substringBeforeLast(path, "/");
            }
            if (path.equals("")) {
                path = "/";
            }
        }

        Set<JahiaGroup> aclGroups = new HashSet<JahiaGroup>();

        String fakePath = path;
        while (!fakePath.equals("")) {
            while (!allAclsGroups.containsKey(fakePath) && !fakePath.equals("")) {
                fakePath = StringUtils.substringBeforeLast(fakePath, "/");
            }
            if (fakePath.equals("")) {
                fakePath = "/";
            }
            final Set<JahiaGroup> jahiaGroups = allAclsGroups.get(fakePath);
            if(jahiaGroups==null) {
                // Should never be null here so relaunch the call.
                return getAclKeyPartForNode(renderContext, checkRootPath, nodePath, appendNodePath, principal, userName);
            }
            aclGroups.addAll(jahiaGroups);
            fakePath = StringUtils.substringBeforeLast(fakePath, "/");
            if (fakePath.equals("")) {
                final Set<JahiaGroup> jahiaGroups1 = allAclsGroups.get("/");
                if(jahiaGroups1==null){
                    return getAclKeyPartForNode(renderContext, checkRootPath, nodePath, appendNodePath, principal, userName);
                }
                aclGroups.addAll(jahiaGroups1);
            }
        }

        for (JahiaGroup g : aclGroups) {
            if (g != null && g.isMember(principal)) {
                if (b.length() > 0) {
                    b.append("|");
                }
                b.append(g.getGroupname());
            }
        }

        if (b.toString().equals(JahiaGroupManagerService.GUEST_GROUPNAME) && !userName.equals(
                JahiaUserManagerService.GUEST_USERNAME)) {
            b.append("|" + JahiaGroupManagerService.USERS_GROUPNAME);
        }
        String userKey = b.toString();
        if ("".equals(userKey.trim()) && userName.equals(JahiaUserManagerService.GUEST_USERNAME)) {
            userKey = userName;
        }
        if ("".equals(userKey.trim())) {
            throw new RepositoryException(
                    "Userkey is empty while generating cache key for path " + path + " and nodepath = " + nodePath +
                    " checkrootpath = " + checkRootPath);
        }
        Resource mainResource = renderContext.getMainResource();
        Set<String> roles;
        if (mainResource != null) {
            AccessControlManager accessControlManager = ((JCRNodeWrapper) mainResource.getNode()).getAccessControlManager();
            if(accessControlManager instanceof JahiaAccessManager)
                roles = ((JahiaAccessManager) accessControlManager).getRoles(path);
            else
                roles = Collections.emptySet();
        } else {
            JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(path);
            AccessControlManager accessControlManager = ((JCRNodeWrapper) node).getAccessControlManager();
            if(accessControlManager instanceof JahiaAccessManager)
                roles = ((JahiaAccessManager) accessControlManager).getRoles(path);
            else
                roles = Collections.emptySet();
        }

        b = new StringBuilder();
        for (String g : roles) {
            if (b.length() > 0) {
                b.append("|");
            }
            b.append(g);
        }
        Map<String, String> map;
        element = (Element) cache.get(userName);
        if (element == null) {
            map = new LinkedHashMap<String, String>();
        } else {
            map = (Map<String, String>) element.getValue();
        }
        String value = userKey + "_r_" + b.toString();
        map.put(path, value);
        element = new Element(userName, map);
        element.setEternal(true);
        cache.put(element);
        return value + "_p_" + checkRootPath + (appendNodePath ? nodePath : "");
    }

    private Element hasUserAcl(final String userName) throws RepositoryException {
        Element element = cache.get(userName);
        if (element == null) {
            initCache(userName);
            element = cache.get(userName);
            getAllAclsGroups();
        }
        return element;
    }

    private void initCache(final String userName) throws RepositoryException {
        template.doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, new JCRCallback<Object>() {
            @SuppressWarnings("unchecked")
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                Query query = session.getWorkspace().getQueryManager().createQuery(
                        "select * from [jnt:ace] as ace where ace.[j:principal] = 'u:" + userName + "'",
                        Query.JCR_SQL2);
                QueryResult queryResult = query.execute();
                NodeIterator rowIterator = queryResult.getNodes();
                while (rowIterator.hasNext()) {
                    Node node = (Node) rowIterator.next();
                    String userPath = userManagerService.getUserSplittingRule().getPathForUsername(userName);
                    if (!node.getPath().startsWith(userPath + "/j:acl")) {
                        Map<String, String> map;
                        if (!cache.isKeyInCache(userName)) {
                            map = new LinkedHashMap<String, String>();
                        } else {
                            map = (Map<String, String>) ((Element) cache.get(userName)).getValue();
                        }
                        String path = node.getParent().getParent().getPath();
                        StringBuilder b = new StringBuilder();
                        Set<String> foundRoles = new HashSet<String>();
                        boolean granted = node.getProperty("j:aceType").getString().equals("GRANT");
                        if (granted) {
                            Value[] roles = node.getProperty(Constants.J_ROLES).getValues();
                            for (Value r : roles) {
                                String role = r.getString();
                                if (!foundRoles.contains(role)) {
                                    if (b.length() > 0) {
                                        b.append("|");
                                    }
                                    b.append(role);
                                    foundRoles.add(role);
                                }
                            }
                            map.put(path, userName + "_r_" + b.toString());
                            if (!"/".equals(path)) {
                                path = node.getParent().getParent().getParent().getPath();
                            }
                            map.put(path, userName + "_r_" + b.toString());
                            final Element element = new Element(userName, map);
                            element.setEternal(true);
                            cache.put(element);
                        }
                    }
                }
                return null;
            }
        });
    }

    private Map<String, Set<JahiaGroup>> getAllAclsGroups() throws RepositoryException {
        if (aclGroups.isEmpty()) {
            initAclGroups();
        }
        return aclGroups;
    }

    private void initAclGroups() throws RepositoryException {
        if (!aclGroups.isEmpty()) {
            return;
        }
        synchronized (objForSync) {
            if (aclGroups.isEmpty()) {
                template.doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        Map<String, Set<JahiaGroup>> tempAclGroups = new LinkedHashMap<String, Set<JahiaGroup>>();
                        tempAclGroups.put("/", new HashSet<JahiaGroup>());
                        tempAclGroups.get("/").add(groupManagerService.lookupGroup(
                                JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME));
                        Query groupQuery = session.getWorkspace().getQueryManager().createQuery(
                                "select * from [jnt:ace] as u where u.[j:principal] like 'g%'", Query.JCR_SQL2);
                        QueryResult groupQueryResult = groupQuery.execute();
                        final NodeIterator nodeIterator = groupQueryResult.getNodes();
                        while (nodeIterator.hasNext()) {
                            JCRNodeWrapper node = (JCRNodeWrapper) nodeIterator.next();
                            String s = StringUtils.substringAfter(node.getProperty("j:principal").getString(), ":");
                            JahiaGroup group = groupManagerService.lookupGroup(node.getResolveSite().getSiteKey(), s);
                            if (group == null) {
                                group = groupManagerService.lookupGroup(s);
                            }
                            if (group != null) {
                                JCRNodeWrapper parent = node.getParent().getParent();
                                String path = parent.getPath();
                                boolean granted = node.getProperty("j:aceType").getString().equals("GRANT");
                                if (granted) {
                                    Set<JahiaGroup> groups;
                                    if (!tempAclGroups.containsKey(path)) {
                                        groups = new LinkedHashSet<JahiaGroup>();
                                        tempAclGroups.put(path, groups);
                                    } else {
                                        groups = tempAclGroups.get(path);
                                    }
                                    groups.add(group);
                                    try {
                                        path = parent.getParent().getPath();
                                        if (!tempAclGroups.containsKey(path)) {
                                            groups = new LinkedHashSet<JahiaGroup>();
                                            tempAclGroups.put(path, groups);
                                        } else {
                                            groups = tempAclGroups.get(path);
                                        }
                                        groups.add(group);
                                    } catch (RepositoryException e) {
                                    }
                                }
                            }
                        }
                        aclGroups = tempAclGroups;
                        return null;
                    }
                });
            }
        }
    }

    public void flushUsersGroupsKey() {
        flushUsersGroupsKey(true);
    }

    public void flushUsersGroupsKey(boolean propageToOtherClusterNodes) {
        synchronized (objForSync) {
            aclGroups = new LinkedHashMap<String, Set<JahiaGroup>>();
            cache.removeAll(!propageToOtherClusterNodes);
            cache.flush();
        }
    }

}
