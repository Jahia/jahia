/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.api.Constants;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * User: toto
 * Date: 11/20/12
 * Time: 12:24 PM
 */
public class AclCacheKeyPartGenerator implements CacheKeyPartGenerator, InitializingBean {
    public static final String PER_USER = "_perUser_";
    public static final String MR_ACL = "_mraclmr_";
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
    //    private Map<String, Set<JahiaGroup>> aclGroups = new LinkedHashMap<String, Set<JahiaGroup>>();
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
            final Set<String> aclsKeys = new LinkedHashSet<String>();

            final Set<String> aclPathChecked = new HashSet<String>();

            aclsKeys.add(getAclKeyPartForNode(renderContext, nodePath, renderContext.getUser(), aclPathChecked));
            final Set<String> dependencies = resource.getDependencies();

            for (final String dependency : dependencies) {
                if (!dependency.equals(nodePath)) {
                    try {
                        if (!dependency.contains("/")) {
                            JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, new JCRCallback<Object>() {
                                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                    final JCRNodeWrapper nodeByIdentifier = session.getNodeByIdentifier(dependency);
                                    aclsKeys.add(getAclKeyPartForNode(renderContext,
                                            nodeByIdentifier.getPath(), renderContext.getUser(), aclPathChecked));
                                    return null;
                                }
                            });
                        } else {
                            aclsKeys.add(getAclKeyPartForNode(renderContext, dependency, renderContext.getUser(), aclPathChecked));
                        }
                    } catch (ItemNotFoundException ex) {
                        logger.warn("ItemNotFound: " + dependency + "  it could be an invalid reference, check jcr integrity");
                    } catch (PathNotFoundException ex) {
                        logger.warn("PathNotFound: "
                                + dependency
                                + "  it could be an invalid reference, check jcr integrity");
                    }
                }
            }

            if ("true".equals(properties.get("cache.mainResource"))) {
                aclsKeys.add(MR_ACL);
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (String aclsKey : aclsKeys) {
                stringBuilder.append(aclsKey);
            }
            return stringBuilder.toString();

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return "";

    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        if (keyPart.equals(PER_USER)) {
            keyPart = renderContext.getUser().getUsername();
        } else if (keyPart.contains(MR_ACL)) {
            try {
                HashSet<String> aclPathChecked = new HashSet<String>();
                String[] p = keyPart.split("\\|");
                for (String s : p) {
                    if (s.contains(":")) {
                        try {
                            aclPathChecked.add(URLDecoder.decode(s.split(":")[1], "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                        }
                    }
                }
                keyPart = keyPart.replace(MR_ACL, getAclKeyPartForNode(renderContext, renderContext.getMainResource().getNode().getPath(), renderContext.getUser(), aclPathChecked));
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
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

    @SuppressWarnings("unchecked")
    public String getAclKeyPartForNode(RenderContext renderContext, String nodePath,
                                                          JahiaUser principal, Set<String> aclPathChecked)
            throws RepositoryException {
        List<Map<String, Set<String>>> l = new ArrayList<Map<String, Set<String>>>();
        l.add(getUserAcl(principal));

        List<String> groups = groupManagerService.getUserMembership(principal);

        for (String group : groups) {
            JahiaGroup g = groupManagerService.lookupGroup(group);
            l.add(getUserAcl(g));
        }

        Map<String, Set<String>> rolesForKey = new TreeMap<String, Set<String>>();
        for (Map<String, Set<String>> map : l) {
            for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
                if (nodePath.startsWith(entry.getKey()) && !aclPathChecked.contains(entry.getKey())) {
                    if (!rolesForKey.containsKey(entry.getKey())) {
                        rolesForKey.put(entry.getKey(), new TreeSet<String>(entry.getValue()));
                    } else {
                        rolesForKey.get(entry.getKey()).addAll(entry.getValue());
                    }
                }
            }
        }

        aclPathChecked.addAll(rolesForKey.keySet());
        String r = "";
        for (Map.Entry<String, Set<String>> entry : rolesForKey.entrySet()) {
            try {
                r += StringUtils.join(entry.getValue(),",") + ":" + URLEncoder.encode(entry.getKey(), "UTF-8") + "|";
            } catch (UnsupportedEncodingException e) {
            }
        }
        return r;
    }

    private Map<String, Set<String>> getUserAcl(final Principal principal) throws RepositoryException {
        final String key;
        if (principal instanceof JahiaUser) {
            key = "u:" + principal.getName();
        } else {
            key = "g:" + principal.getName();
        }
        Element element = cache.get(key);
        if (element == null) {
            Map<String, Set<String>> map = template.doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, new JCRCallback<Map<String, Set<String>>>() {
                @SuppressWarnings("unchecked")
                public Map<String, Set<String>> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Query query = session.getWorkspace().getQueryManager().createQuery(
                            "select * from [jnt:ace] as ace where ace.[j:principal] = '" + key + "'",
                            Query.JCR_SQL2);
                    QueryResult queryResult = query.execute();
                    NodeIterator rowIterator = queryResult.getNodes();

                    Map<String, Set<String>> map1 = new LinkedHashMap<String, Set<String>>();

                    while (rowIterator.hasNext()) {
                        Node node = (Node) rowIterator.next();
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
                            map1.put(path, foundRoles);
                        }
                    }
                    return map1;
                }
            });

            element = new Element(key, map);
            element.setEternal(true);
            cache.put(element);

        }
        return (Map<String, Set<String>>) element.getValue();
    }

    public void flushUsersGroupsKey() {
        flushUsersGroupsKey(true);
    }

    public void flushUsersGroupsKey(boolean propageToOtherClusterNodes) {
        synchronized (objForSync) {
//            aclGroups = new LinkedHashMap<String, Set<JahiaGroup>>();
            cache.removeAll(!propageToOtherClusterNodes);
            cache.flush();
        }
    }

}
