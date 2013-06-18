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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Base64;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Default implementation of the module output cache key generator.
 *
 * @author rincevent
 * @author Sergiy Shyrkov
 */
public class DefaultCacheKeyGenerator implements CacheKeyGenerator {
    private static Logger logger = LoggerFactory.getLogger(DefaultCacheKeyGenerator.class);
    private List<CacheKeyPartGenerator> partGenerators;
    private List<String> fields;

<<<<<<< .working
    public List<CacheKeyPartGenerator> getPartGenerators() {
        return partGenerators;
=======
    private static transient Logger logger = org.slf4j.LoggerFactory.getLogger(DefaultCacheKeyGenerator.class);

    private static final Set<String> KNOWN_FIELDS = new LinkedHashSet<String>(Arrays.asList("workspace", "language",
            "path", "template", "templateType", "acls", "context", "wrapped", "custom", "queryString",
            "templateNodes", "resourceID", "inArea", "site"));
    private static final String CACHE_NAME = "HTMLNodeUsersACLs";
    private static final String PROPERTY_CACHE_NAME = "HTMLRequiredPermissionsCache";
    public static final String PER_USER = "_perUser_";
    private static final String MAIN_RESOURCE_KEY = "_mr_";
    private List<String> fields = new ArrayList<String>(KNOWN_FIELDS);

    private MessageFormat format = new MessageFormat("#{0}#{1}#{2}#{3}#{4}#{5}#{6}#{7}#{8}#{9}#{10}#{11}#{12}#{13}");

    private JahiaGroupManagerService groupManagerService;
    private JahiaUserManagerService userManagerService;
    private Map<String, Set<JahiaGroup>> aclGroups = new LinkedHashMap<String, Set<JahiaGroup>>();
    private final Object objForSync = new Object();
    private EhCacheProvider cacheProvider;
    private Cache cache;
    private JCRTemplate template;
    private Cache permissionCache;
    public static Pattern mainResourcePattern = Pattern.compile(MAIN_RESOURCE_KEY);
    public static Pattern perUserPattern = Pattern.compile(PER_USER);
    private static final Pattern depAclsPattern = Pattern.compile("_depacl_");
    private static final Pattern aclsPathPattern = Pattern.compile("_p_");
    public static Pattern mainResourceAclPattern = Pattern.compile("mraclmr");

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
>>>>>>> .merge-right.r46381
    }

    public void setPartGenerators(List<CacheKeyPartGenerator> partGenerators) {
        this.partGenerators = partGenerators;
        this.fields = new ArrayList<String>();
        for (CacheKeyPartGenerator generator : partGenerators) {
            fields.add(generator.getKey());
        }
    }

    public String generate(Resource resource, RenderContext renderContext, Properties properties) {
        return StringUtils.join(getArguments(resource, renderContext, properties), "@@");
    }

    private Object[] getArguments(Resource resource, RenderContext renderContext, Properties properties) {
        List<String> args = new LinkedList<String>();
<<<<<<< .working
        for (CacheKeyPartGenerator generator : partGenerators) {
            args.add(generator.getValue(resource, renderContext, properties));
=======
        for (String field : fields) {
            if ("workspace".equals(field)) {
                args.add(encodeString(resource.getWorkspace()));
            } else if ("language".equals(field)) {
                args.add(encodeString(resource.getLocale().toString()));
            } else {
                if ("path".equals(field)) {
                    StringBuilder s = new StringBuilder(resource.getNode().getPath());
                    if (Boolean.TRUE.equals(request.getAttribute("cache.mainResource"))) {
                        s.append(MAIN_RESOURCE_KEY);
                    }
                    args.add(encodeString(s.toString()));
                } else if ("template".equals(field)) {
                    if (resource.getContextConfiguration().equals("page") && resource.getNode().getPath().equals(
                            renderContext.getMainResource().getNode().getPath())) {
                        args.add(encodeString(renderContext.getMainResource().getResolvedTemplate()));
                    } else {
                        args.add(encodeString(resource.getResolvedTemplate()));
                    }
                } else if ("templateType".equals(field)) {
                    String templateType = resource.getTemplateType();
                    if (renderContext.isAjaxRequest()) {
                        templateType += ".ajax";
                    }
                    args.add(encodeString(templateType));
                } else if ("queryString".equals(field)) {
                    String[] params = (String[]) request.getAttribute("cache.requestParameters");
                    if (params != null && params.length > 0) {
                        args.add(encodeString("_qs" + Arrays.toString(params) + "_"));
                    } else {
                        args.add("");
                    }
                } else if ("acls".equals(field)) {
                    args.add(encodeString(appendAcls(resource, renderContext, true)));
                } else if ("wrapped".equals(field)) {
                    args.add(encodeString(String.valueOf(resource.hasWrapper())));
                } else if ("context".equals(field)) {
                    args.add(encodeString(String.valueOf(resource.getContextConfiguration())));
                } else if ("custom".equals(field)) {
                    args.add(encodeString((String) resource.getModuleParams().get("module.cache.additional.key")) +
                            encodeString((String) request.getAttribute("module.cache.additional.key")));
                } else if ("templateNodes".equals(field)) {
                    final Template t = (Template) request.getAttribute("previousTemplate");
                    args.add(encodeString(t != null ? t.serialize() : ""));
                } else if ("resourceID".equals(field)) {
                    try {
                        args.add(encodeString(resource.getNode().getIdentifier()));
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                } else if ("inArea".equals(field)) {
                    Object inArea = request.getAttribute("inArea");
                    args.add(encodeString(inArea != null ? inArea.toString() : ""));
                } else if ("site".equals(field)) {
                    // Todo : Do we need to find another way of getting the urlresolver ?
                    URLResolver urlResolver = (URLResolver) renderContext.getRequest().getAttribute("urlResolver");
                    args.add(encodeString(urlResolver == null ||
                            urlResolver.getSiteKeyByServerName() == null ? new StringBuilder().append(
                            renderContext.getSite().getSiteKey()).append(":").append("virtualhost").append(":").append(
                            request.getParameter("jsite")).toString() : new StringBuilder().append(
                            renderContext.getSite().getSiteKey()).append(":").append(request.getParameter(
                            "jsite")).toString()));
                }
            }
>>>>>>> .merge-right.r46381
        }
        return args.toArray(new String[args.size()]);
    }

<<<<<<< .working
    public Map<String, String> parse(String key) {
        String[] values = key.split("@@");
=======
    public String appendAcls(final Resource resource, final RenderContext renderContext, boolean appendNodePath) {
        try {
            if (renderContext.getRequest() != null && Boolean.TRUE.equals(renderContext.getRequest().getAttribute("cache.perUser"))) {
                return PER_USER;
            }

            JCRNodeWrapper node = resource.getNode();
            boolean checkRootPath = true;
            Element element = permissionCache.get(node.getPath());
            if (element != null && Boolean.TRUE == ((Boolean) element.getValue())) {
                node = renderContext.getMainResource().getNode();
                checkRootPath = false;
            } else if (element == null) {
                if (node.hasProperty("j:requiredPermissions")) {
                    permissionCache.put(new Element(node.getPath(), Boolean.TRUE));
                    node = renderContext.getMainResource().getNode();
                    checkRootPath = false;
                } else {
                    permissionCache.put(new Element(node.getPath(), Boolean.FALSE));
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
                        try {
                            if (!JCRContentUtils.isNotJcrUuid(dependency)) {
                                final boolean finalCheckRootPath = checkRootPath;
                                JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, new JCRCallback<Object>() {
                                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                        final JCRNodeWrapper nodeByIdentifier = session.getNodeByIdentifier(dependency);
                                        aclsKeys.add(getAclsKeyPart(renderContext, finalCheckRootPath, nodeByIdentifier.getPath(),
                                                true, null));
                                        return null;
                                    }
                                });
                            } else if (dependency.contains("/")) {
                                aclsKeys.add(getAclsKeyPart(renderContext,
                                        checkRootPath, dependency, true, null));
                            }
                        } catch (ItemNotFoundException ex) {
                            logger.warn("ItemNotFound: "
                                    + dependency
                                    + "  it could be an invalid reference, check jcr integrity");
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
                if (stringBuilder.length() > 0) {
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
            String[] aclKeys = depAclsPattern.split(acls);
            StringBuilder stringBuilder = new StringBuilder();
            for (String aclKey : aclKeys) {
                if (aclKey.contains("/")) {
                    String path = "";
                    final String[] aclRolePaths = aclsPathPattern.split(aclKey);
                    if (aclRolePaths.length == 2) {
                        path = "/" + StringUtils.substringAfter(aclRolePaths[1], "/");
                        if (stringBuilder.length() > 0) {
                            stringBuilder.append("_depacl_");
                        }
                        stringBuilder.append(getAclKeyPartForNode(renderContext, checkRootPath, path, true, principal,
                                userName));
                    }
                } else if ("mraclmr".equals(aclKey)) {
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
            if (jahiaGroups == null) {
                // Should never be null here so relaunch the call.
                return getAclKeyPartForNode(renderContext, checkRootPath, nodePath, appendNodePath, principal, userName);
            }
            aclGroups.addAll(jahiaGroups);
            fakePath = StringUtils.substringBeforeLast(fakePath, "/");
            if (fakePath.equals("")) {
                final Set<JahiaGroup> jahiaGroups1 = allAclsGroups.get("/");
                if (jahiaGroups1 == null) {
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
            roles = ((JahiaAccessManager) ((JCRNodeWrapper) mainResource.getNode()).getAccessControlManager()).getRoles(
                    path);
        } else {
            JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(path);
            roles = ((JahiaAccessManager) ((JCRNodeWrapper) node).getAccessControlManager()).getRoles(path);
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
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                Query query = session.getWorkspace().getQueryManager().createQuery(
                        "select * from [jnt:ace] as ace where ace.[j:principal] = 'u:" + userName + "'",
                        Query.JCR_SQL2);
                QueryResult queryResult = query.execute();
                NodeIterator rowIterator = queryResult.getNodes();

                Map<String, String> map = new LinkedHashMap<String, String>();

                while (rowIterator.hasNext()) {
                    Node node = (Node) rowIterator.next();
                    String userPath = userManagerService.getUserSplittingRule().getPathForUsername(userName);
                    if (!node.getPath().startsWith(userPath + "/j:acl")) {
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
                        }
                    }
                }

                final Element element = new Element(userName, map);
                element.setEternal(true);
                cache.put(element);

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
                                groupManagerService.ADMINISTRATORS_GROUPNAME));
                        Query groupQuery = session.getWorkspace().getQueryManager().createQuery(
                                "select * from [jnt:ace] as u where u.[j:principal] like 'g%'", Query.JCR_SQL2);
                        QueryResult groupQueryResult = groupQuery.execute();
                        final NodeIterator nodeIterator = groupQueryResult.getNodes();
                        while (nodeIterator.hasNext()) {
                            JCRNodeWrapper node = (JCRNodeWrapper) nodeIterator.next();
                            String s = StringUtils.substringAfter(node.getProperty("j:principal").getString(), ":");
                            JahiaGroup group = groupManagerService.lookupGroup(node.getResolveSite().getID(), s);
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

    public String getPath(String key) throws ParseException {
        return mainResourcePattern.matcher(parse(key).get("path")).replaceAll("");
    }

    public Map<String, String> parse(String key) throws ParseException {
        Object[] values = format.parse(key);
>>>>>>> .merge-right.r46381
        Map<String, String> result = new LinkedHashMap<String, String>(fields.size());
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            result.put(fields.get(i), value == null || value.equals("null") ? null : value);
        }
        return result;
    }

    public String replaceField(String key, String fieldName, String newValue) {
        String[] args = key.split("@@");
        args[fields.indexOf(fieldName)] = newValue;
        return StringUtils.join(args, "@@");
    }

    public CacheKeyPartGenerator getPartGenerator(String field) {
        return partGenerators.get(fields.indexOf(field));
    }

    public String replacePlaceholdersInCacheKey(RenderContext renderContext, String key) {
        String[] args = key.split("@@");
        String[] newArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            String value = args[i];
            newArgs[i] = partGenerators.get(i).replacePlaceholders(renderContext,value);
        }
        String s = StringUtils.join(newArgs,"@@");
//        if (SettingsBean.getInstance().isProductionMode()) {
//            try {
//                byte[] b = DigestUtils.getSha512Digest().digest(s.getBytes("UTF-8"));
//                StringWriter sw = new StringWriter();
//                Base64.encode(b, 0, b.length, sw);
//                return sw.toString();
//            } catch (Exception e) {
//                logger.warn("Issue while digesting key",e);
//            }
//        }
        return s;
    }

<<<<<<< .working
=======
    public void setCacheProvider(EhCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
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

    public void setTemplate(JCRTemplate template) {
        this.template = template;
    }

    private String encodeString(String toBeEncoded) {
        return toBeEncoded != null ? toBeEncoded.replaceAll("#", "@@") : toBeEncoded;
    }

    private String decodeString(String toBeDecoded) {
        return toBeDecoded != null ? toBeDecoded.replaceAll("@@", "#") : toBeDecoded;
    }
>>>>>>> .merge-right.r46381
}
