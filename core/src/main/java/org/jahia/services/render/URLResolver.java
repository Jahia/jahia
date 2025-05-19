/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.jcr.AccessDeniedException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Render;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jahia.api.Constants.LIVE_WORKSPACE;

/**
 * Class to resolve URLs and URL paths, so that the workspace / locale / node-path information is
 * returned.
 *
 * There are also convenience methods to directly return the Node or Resource where the URL points to.
 *
 * The method also considers vanity URL mappings and resolves them to the mapped nodes, so that
 * the URL resolver will return the info as if no mapping have been used.
 *
 * @author Benjamin Papez
 *
 */
public class URLResolver {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    private static final String DEFAULT_WORKSPACE = LIVE_WORKSPACE;

    private static final Pattern MACRO_URL_PATTERN = Pattern.compile("(.?)*.html##[a-zA-Z]*##$");

    private static final String VANITY_URL_NODE_PATH_SEGMENT = "/" + VanityUrlManager.VANITYURLMAPPINGS_NODE + "/";

    private static final Pattern CROSS_SITES_URL_PATTERN = Pattern.compile("[a-z]{0,3}_?[A-Z]{0,2}/sites/.*");

    private static final Logger logger = LoggerFactory.getLogger(URLResolver.class);

    private static final String[] SERVLETS_ALLOWING_URL_MAPPING = new String[] {
            StringUtils.substringAfterLast(Render.getRenderServletPath(), "/")
    };

    private static JahiaSitesService sitesService = ServicesRegistry.getInstance().getJahiaSitesService();

    private String urlPathInfo = null;
    private String servletPart = "";
    private String workspace;
    private Locale locale;
    private String path = "";
    private String siteKey;
    private String siteKeyByServerName;
    private boolean mappable = false;

    private String redirectUrl;
    private String vanityUrl;

    private Date versionDate;
    private String versionLabel;
    private static final String CACHE_KEY_SEPARATOR = "___";
    private SiteInfo siteInfo;

    public void setRenderContext(RenderContext renderContext) {
        this.renderContext = renderContext;
    }

    private RenderContext renderContext;
    private Map<String, JCRNodeWrapper> resolvedNodes = new ConcurrentHashMap<String, JCRNodeWrapper>();
    private Ehcache nodePathCache;
    private Ehcache siteInfoCache;

    protected URLResolver(String urlPathInfo, String serverName, HttpServletRequest request, Ehcache nodePathCache, Ehcache siteInfoCache) {
        this(urlPathInfo, serverName, null, request, nodePathCache, siteInfoCache);
    }

    /**
     * Initializes an instance of this class. This constructor is mainly used when
     * resolving URLs of incoming requests.
     *
     * @param pathInfo  the path info (usually obtained with @link javax.servlet.http.HttpServletRequest.getPathInfo())
     * @param serverName  the server name (usually obtained with @link javax.servlet.http.HttpServletRequest.getServerName())
     * @param request  the current HTTP servlet request object
     */
    protected URLResolver(String pathInfo, String serverName, String workspace, HttpServletRequest request, Ehcache nodePathCache, Ehcache siteInfoCache) {
        super();
        this.nodePathCache = nodePathCache;
        this.siteInfoCache = siteInfoCache;
        this.workspace = workspace;

        this.urlPathInfo = normalizeUrlPathInfo(pathInfo);

        if (!JahiaUserManagerService.isGuest(JCRSessionFactory.getInstance().getCurrentUser())) {
            Date date = getVersionDate(request);
            String versionLabel = getVersionLabel(request);
            setVersionDate(date);
            setVersionLabel(versionLabel);
        }

        if (urlPathInfo != null) {
            servletPart = StringUtils.substring(getUrlPathInfo(), 1,
                    StringUtils.indexOf(getUrlPathInfo(), "/", 1));
            path = StringUtils.substring(getUrlPathInfo(), servletPart.length() + 2,
                    getUrlPathInfo().length());
        }
        if (!resolveUrlMapping(serverName, request)) {
            init();
            if (!Url.isLocalhost(serverName) && isMappable()
                    && SettingsBean.getInstance().isPermanentMoveForVanityURL()) {
                try {
                    if (siteKeyByServerName != null) {
                        JCRNodeWrapper targetNode = getNode();
                        if (siteKeyByServerName.equals(targetNode.getResolveSite().getSiteKey())) {
                            VanityUrl defaultVanityUrl = getVanityUrlService().getVanityUrlForWorkspaceAndLocale(targetNode, this.workspace,
                                    locale, siteKey);
                            if (defaultVanityUrl != null && defaultVanityUrl.isActive()) {
                                redirect(request, defaultVanityUrl);
                            }
                        }
                    }
                } catch (NoSuchWorkspaceException e) {
                    logger.debug("Workspace not found: {} with resolved workspace {} and path info {}",
                            workspace, this.workspace, pathInfo);
                } catch (PathNotFoundException e) {
                    logger.debug("Path not found: {}", urlPathInfo);
                } catch (AccessDeniedException e) {
                    logger.debug("User has no access to the resource, so there will not be a redirection");
                } catch (RepositoryException e) {
                    logger.warn("Error when trying to check whether there is a vanity URL mapping", e);
                }
            }
        }
    }

    private void redirect(HttpServletRequest request, VanityUrl defaultVanityUrl) {
        if (request == null || StringUtils.isEmpty(request.getQueryString())) {
            setRedirectUrl(defaultVanityUrl.getUrl());
        } else {
            String queryString = "?";
            try {
                queryString += URLDecoder.decode(request.getQueryString(), "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                logger.error("Error decoding query string, will use undecoded query string instead", uee);
                queryString += request.getQueryString();
            }
            setRedirectUrl(defaultVanityUrl.getUrl() + queryString);
        }
    }

    private static String normalizeUrlPathInfo(String urlPathInfo) {
        if (urlPathInfo != null && urlPathInfo.length() > 1 && urlPathInfo.charAt(urlPathInfo.length() - 1) == '/') {
            urlPathInfo = urlPathInfo.substring(0, urlPathInfo.length() - 1);
            return urlPathInfo;
        }
        return urlPathInfo;
    }

    /**
     * Initializes an instance of this class. This constructor is mainly used when
     * trying to find mapping for URLs in outgoing requests.
     *
     * @param url   URL in HTML links of outgoing requests
     * @param context  The current request in order to obtain the context path
     */
    protected URLResolver(String url, RenderContext context, Ehcache nodePathCache, Ehcache siteInfoCache) {
        this.nodePathCache = nodePathCache;
        this.siteInfoCache = siteInfoCache;

        renderContext = context;
        String contextPath = context.getRequest().getContextPath();

        this.urlPathInfo = normalizeUrlPathInfo(StringUtils.substringAfter(url, !StringUtils.isEmpty(contextPath) ? contextPath + context.getServletPath() : context.getServletPath()));
        this.servletPart = StringUtils.substringAfterLast(context.getServletPath(), "/");

        if (!StringUtils.isEmpty(urlPathInfo)) {
            path = getUrlPathInfo().substring(1);
            init();
        }
    }

    private void init() {
        workspace = verifyWorkspace(StringUtils.substringBefore(path, "/"));
        path = StringUtils.substringAfter(path, "/");
        locale = verifyLanguage(StringUtils.substringBefore(path, "/"));
        path = "/" + (locale != null ? StringUtils.substringAfter(path, "/") : path);

        // TODO: this is perhaps a temporary limitation as URL points to special templates, when
        // there are more than one dots - and the path needs to end with .html
        // and in some cases macro extension are added like the ##requestParameters## for languageswitcher
        String lastPart = StringUtils.substringAfterLast(path, "/");
        int indexOfHTMLSuffix = lastPart.indexOf(".html");
        if (isServletAllowingUrlMapping() && indexOfHTMLSuffix > 0
                && (lastPart.endsWith(".html") ||
                        MACRO_URL_PATTERN.matcher(lastPart).matches())) {
            mappable = true;
        }
    }

    private Date getVersionDate(HttpServletRequest req) {
        // we assume here that the date has been passed as milliseconds.
        String msString = req.getParameter("v");
        if (msString == null) {
            return null;
        }
        try {
            long msLong = Long.parseLong(msString);
            if (logger.isDebugEnabled()) {
                logger.debug("Display version of date : " + SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date(msLong)));
            }
            return new Date(msLong);
        } catch (NumberFormatException nfe) {
            logger.warn("Invalid version date found in URL {}", msString);
            return null;
        }
    }

    private String getVersionLabel(HttpServletRequest req) {
        return req.getParameter("l");
    }

    private boolean isServletAllowingUrlMapping() {
        boolean isServletAllowingUrlMapping = false;
        for (String servletAllowingUrlMapping : SERVLETS_ALLOWING_URL_MAPPING) {
            if (servletAllowingUrlMapping.equals(servletPart)) {
                isServletAllowingUrlMapping = true;
                break;
            }
        }
        return isServletAllowingUrlMapping;
    }

    protected boolean resolveUrlMapping(String serverName, HttpServletRequest request) {
        boolean mappingResolved = false;

        try {
            siteKeyByServerName = sitesService.getSitenameByServerName(serverName);
        } catch (JahiaException e) {
            logger.warn("Error finding site via servername: " + serverName, e);
        }

        if (getSiteKey() == null) {
            String siteKeyInPath = resolveSiteKeyFromPath(getPath());
            if (!StringUtils.isEmpty(siteKeyInPath)) {
                setSiteKey(siteKeyInPath);
            } else if (!Url.isLocalhost(serverName)) {
                if (siteKeyByServerName != null) {
                    setSiteKey(siteKeyByServerName);
                }
            }
        }

        if (isServletAllowingUrlMapping() && !Url.isLocalhost(serverName)) {
            String tempPath = null;
            try {
                String tempWorkspace = verifyWorkspace(StringUtils.substringBefore(getPath(), "/"));
                tempPath = StringUtils.substringAfter(getPath(), "/");
                VanityUrl resolvedVanityUrl = null;
                logger.debug("Trying to resolve vanity url for tempPath = {}", tempPath);
                boolean doNotMatchesCrossSitesPattern = !CROSS_SITES_URL_PATTERN.matcher(tempPath).matches();
                if (!StringUtils.isEmpty(getSiteKey()) && doNotMatchesCrossSitesPattern) {
                    List<VanityUrl> vanityUrls = getVanityUrlService()
                            .findExistingVanityUrls("/" + tempPath,
                                    getSiteKey(), tempWorkspace);
                    for (VanityUrl vanityUrl : vanityUrls) {
                        if (vanityUrl.isActive()) {
                            resolvedVanityUrl = vanityUrl;
                            break;
                        }
                    }
                } else if (doNotMatchesCrossSitesPattern) {
                    List<VanityUrl> vanityUrls = getVanityUrlService()
                            .findExistingVanityUrls("/" + tempPath,
                                    StringUtils.EMPTY, tempWorkspace);

                    for (VanityUrl vanityUrl : vanityUrls) {
                        if (vanityUrl.isActive()
                                && (StringUtils.isEmpty(getSiteKey()) || getSiteKey().equals(
                                vanityUrl.getSite()))) {
                            resolvedVanityUrl = vanityUrl;
                            break;
                        }
                    }
                }
                if (resolvedVanityUrl != null) {
                    workspace = tempWorkspace;
                    locale = StringUtils.isEmpty(resolvedVanityUrl
                            .getLanguage()) ? DEFAULT_LOCALE
                            : LanguageCodeConverters
                            .languageCodeToLocale(resolvedVanityUrl
                                    .getLanguage());
                    String nodePath = StringUtils.substringBefore(resolvedVanityUrl
                            .getPath(), VANITY_URL_NODE_PATH_SEGMENT);
                    path = nodePath + ".html";
                    setVanityUrl(resolvedVanityUrl.getUrl());
                    // If siteKey was not resolved so far, we can fairly consider it is now, we have a matching vanity URL
                    if (getSiteKey() == null) {
                        setSiteKey(resolvedVanityUrl.getSite());
                    }
                    if (SettingsBean.getInstance()
                            .isPermanentMoveForVanityURL()
                            && !resolvedVanityUrl.isDefaultMapping()) {
                        VanityUrl defaultVanityUrl = getVanityUrlService()
                                .getVanityUrlForWorkspaceAndLocale(nodePath,
                                        workspace, locale, siteKey);
                        if (defaultVanityUrl != null
                                && defaultVanityUrl.isActive() && !resolvedVanityUrl.equals(defaultVanityUrl)) {
                            redirect(request, defaultVanityUrl);
                        }
                    }
                    mappingResolved = true;
                }
            } catch (PathNotFoundException e) {
                logger.debug(e.getMessage());
            } catch (RepositoryException e) {
                logger.warn("Error when trying to resolve URL mapping: "
                        + tempPath, e);
            }
        }
        return mappingResolved;
    }

    /**
     * Gets the pathInfo of the given URL (@link javax.servlet.http.HttpServletRequest.getPathInfo())
     * @return the pathInfo of the given URL
     */
    public String getUrlPathInfo() {
        return urlPathInfo;
    }

    public String getServletPart() {
        return servletPart;
    }

    /**
     * Gets the workspace of the request resolved by the URL
     * @return the workspace of the given URL
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * Gets the locale of the request resolved by the URL
     * @return the locale of the given URL
     */
    public Locale getLocale() {
        Locale uiLocale = null;
        if (renderContext!=null && renderContext.isForceUILocaleForJCRSession()) {
            uiLocale = renderContext.getUILocale();
        }
        return uiLocale!=null ? uiLocale:locale;
    }

    /**
     * Gets the content node path of the request resolved by the URL
     * @return the content node path of the given URL
     */
    public String getPath() {
        return path;
    }

    /**
     * Creates a node from the path in the URL.
     *
     * @return The node, if found
     * @throws PathNotFoundException
     *             if the resource cannot be resolved
     * @throws RepositoryException in case of JCR-related errors
     */
    public JCRNodeWrapper getNode() throws RepositoryException {
        return resolveNode(getWorkspace(), getLocale(), getPath());
    }

    /**
     * Creates a resource from the path in the URL.
     * <p/>
     * The path should looks like : [nodepath][.templatename].[templatetype] or [nodepath].[templatetype]
     *
     * Workspace, locale and path are taken from the given resolved URL.
     *
     * @return The resource, if found
     * @throws PathNotFoundException
     *             if the resource cannot be resolved
     * @throws RepositoryException in case of JCR-related errors
     */
    public Resource getResource() throws RepositoryException {
        return resolveResource(getWorkspace(), getLocale(), getPath());
    }

    public Resource getResource(String path) throws RepositoryException {
        return resolveResource(getWorkspace(), getLocale(), path);
    }

    /**
     * Creates a node from the specified path.
     * <p/>
     * The path should looks like : [nodepath][.templatename].[templatetype] or [nodepath].[templatetype]
     *
     * @param workspace
     *            The workspace where to get the node
     * @param locale
     *            current locale
     * @param path
     *            The path of the node, in the specified workspace
     * @return The node, if found
     * @throws PathNotFoundException
     *             if the resource cannot be resolved
     * @throws RepositoryException in case of JCR-related errors
     */
    protected JCRNodeWrapper resolveNode(final String workspace,
                                         final Locale locale, final String path) throws RepositoryException {
        logger.debug("Resolving node for workspace '{}' locale '{}' and path '{}'", new Object[] {workspace, locale, path});
        final String cacheKey = getCacheKey(workspace, locale, path);
        if (resolvedNodes.containsKey(cacheKey)) {
            return resolvedNodes.get(cacheKey);
        }
        JCRNodeWrapper node = null;
        Element element = nodePathCache.get(cacheKey);
        String nodePath = null;
        if (element!=null) {
            nodePath = (String) element.getObjectValue();
        }
            element = siteInfoCache.get(cacheKey);
        if (element!=null) {
            siteInfo = (SiteInfo) element.getObjectValue();
        }
        if (nodePath == null || siteInfo == null) {
            nodePath = JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, locale,
                    new JCRCallback<String>() {

                        @Override
                        public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            String nodePath = JCRContentUtils.escapeNodePath(path.endsWith("/*") ? path.substring(0,
                                    path.lastIndexOf("/*")) : path);

                            String siteName = resolveSiteKeyFromPath(nodePath);
                            if (siteName != null && session.itemExists("/sites/" + siteName)) {
                                siteInfo = new SiteInfo((JCRSiteNode) session.getNode("/sites/" + siteName));

                                if (siteInfo.isMixLanguagesActive() && siteInfo.getDefaultLanguage() != null) {
                                    session.setFallbackLocale(LanguageCodeConverters.getLocaleFromCode(siteInfo.getDefaultLanguage()));
                                }
                            }
                            logger.debug("{} has not been found in the cache, still looking for node {}", cacheKey, nodePath);
                            JCRNodeWrapper node = null;
                            while (true) {
                                try {
                                    node = session.getNode(nodePath);
                                    break;
                                } catch (PathNotFoundException ex) {
                                    if (nodePath.lastIndexOf("/") < nodePath.lastIndexOf(".")) {
                                        nodePath = nodePath.substring(0, nodePath.lastIndexOf("."));
                                    } else {
                                        throw new PathNotFoundException("'" + nodePath + "'not found");
                                    }
                                }
                            }
                            nodePathCache.put(new Element(cacheKey, nodePath));
                            // the next condition is false e.g. when nodePath is "/" and session's locale is not in systemsite's locales
                            JCRSiteNode resolveSite = node.getResolveSite();
                            if (resolveSite != null) {
                                siteInfo = new SiteInfo(resolveSite);
                                siteInfoCache.put(new Element(cacheKey, siteInfo));
                            }
                            return nodePath;
                        }
                    });
        }
        if (siteInfo == null) {
            siteInfoCache.remove(cacheKey);
            throw new RepositoryException("could not resolve site for " + path + " in workspace " + workspace + " in language " + locale);
        }
        if (siteInfo.isMixLanguagesActive() && siteInfo.getDefaultLanguage() != null) {
            JCRSessionFactory.getInstance().setFallbackLocale(LanguageCodeConverters.getLocaleFromCode(siteInfo.getDefaultLanguage()));
        }
        JCRSessionWrapper userSession = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale);
        if (userSession.getVersionDate() == null && versionDate != null) {
            userSession.setVersionDate(versionDate);
        }
        if (userSession.getVersionLabel() == null && versionLabel != null) {
            userSession.setVersionLabel(versionLabel);
        }
        try {
            node = userSession.getNode(nodePath);
        } catch (PathNotFoundException e) {
            throw new AccessDeniedException(path);
        }
        resolvedNodes.put(cacheKey, node);
        return node;

    }

    private String getCacheKey(final String workspace, final Locale locale, final String path) {
        StringBuilder builder = new StringBuilder(workspace != null ? workspace : "null");
        builder.append(CACHE_KEY_SEPARATOR);
        builder.append(locale != null ? locale.toString() : "null");
        builder.append(CACHE_KEY_SEPARATOR);
        builder.append(path);
        return builder.toString();
    }

    /**
     * Creates a resource from the specified path.
     * <p/>
     * The path should looks like : [nodepath][.templatename].[templatetype] or [nodepath].[templatetype]
     *
     * @param workspace
     *            The workspace where to get the node
     * @param locale
     *            current locale
     * @param path
     *            The path of the node, in the specified workspace
     * @return The resource, if found
     * @throws PathNotFoundException
     *             if the resource cannot be resolved
     * @throws RepositoryException in case of JCR-related errors
     */
    protected Resource resolveResource(final String workspace, final Locale locale, final String path)
            throws RepositoryException {
        logger.debug("Resolving resource for workspace '{}' locale '{}' and path '{}'", new Object[] {workspace, locale, path});
        if (locale == null) {
            throw new JahiaBadRequestException("Unknown locale");
        }
        final URLResolver urlResolver = this;
        return JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null,
                workspace, locale, new JCRCallback<Resource>() {

                    @Override
                    public Resource doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        String ext = null;
                        String tpl = null;
                        String nodePath = JCRContentUtils.escapeNodePath(path);
                        JCRNodeWrapper node;
                        while (true) {
                            int i = nodePath.lastIndexOf('.');
                            if (i > nodePath.lastIndexOf('/')) {
                                if (ext == null) {
                                    ext = nodePath.substring(i + 1);
                                    if ("ajax".equals(ext)) {
                                        ext = null;
                                        if (renderContext != null) {
                                            renderContext.setAjaxRequest(true);
                                            HttpServletRequest req = renderContext.getRequest();
                                            if (req.getParameter("mainResource") != null && !req.getParameter("mainResource").equals(path)) {
                                                try {
                                                    Resource resource = urlResolver.getResource(req.getParameter(
                                                            "mainResource"));
                                                    renderContext.setAjaxResource(resource);
                                                } catch (PathNotFoundException e) {
                                                }
                                            }
                                        }
                                    }
                                } else if (tpl == null) {
                                    tpl = nodePath.substring(i + 1);
                                } else {
                                    tpl = nodePath.substring(i + 1) + "." + tpl;
                                }
                                nodePath = nodePath.substring(0, i);
                            } else {
                                throw new PathNotFoundException("not found");
                            }
                            try {
                                node = session.getNode(nodePath);
                                break;
                            } catch (PathNotFoundException ex) {
                                // ignore it
                            }
                        }

                        final Element element = siteInfoCache.get(getCacheKey(workspace, locale, path));
                        SiteInfo siteInfo = null;
                        if (element != null) {
                            siteInfo = (SiteInfo) element.getObjectValue();
                        }
                        boolean mixLanguagesActive = false;
                        String defaultLanguage = null;
                        if (siteInfo == null) {
                            JCRSiteNode site = node.getResolveSite();
                            if (site != null) {
                                defaultLanguage = site.getDefaultLanguage();
                                mixLanguagesActive = site.isMixLanguagesActive();
                                siteInfoCache.put(new Element(getCacheKey(workspace, locale, path), new SiteInfo(site)));
                            }
                        } else {
                            defaultLanguage = siteInfo.getDefaultLanguage();
                            mixLanguagesActive = siteInfo.isMixLanguagesActive();
                        }
                        JCRSessionWrapper userSession;

                        if (defaultLanguage != null && mixLanguagesActive) {
                            userSession = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale,
                                    LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
                        } else {
                            userSession = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale);
                        }

                        if (userSession.getVersionDate() == null)
                            userSession.setVersionDate(versionDate);
                        if (userSession.getVersionLabel() == null)
                            userSession.setVersionLabel(versionLabel);

                        try {
                            node = userSession.getNode(nodePath);
                        } catch (PathNotFoundException e) {
                            throw new AccessDeniedException(path);
                        }

                        Resource r = new Resource(node, ext, tpl, Resource.CONFIGURATION_PAGE);
                        logger.debug("Resolved resource: {}", r);
                        return r;
                    }
                });
    }


    private String resolveSiteKeyFromPath(String path) {

        // .../sites/[siteKey]/... case.
        String siteKey = StringUtils.substringBetween(path, "/sites/", "/");
        if (StringUtils.isNotEmpty(siteKey)) {
            return siteKey;
        }

        // .../sites/[siteKey].[templatename].[templatetype] or .../sites/[siteKey].[templatetype] case.
        // Note, siteKey itself may contain a point.
        String pathEnding = StringUtils.substringAfter(path, "/sites/");
        if (StringUtils.isEmpty(pathEnding)) {
            return null;
        }
        List<String> siteNames = sitesService.getSitesNames();
        for (String siteKeyCandidate = pathEnding; true; siteKeyCandidate = StringUtils.substringBeforeLast(siteKeyCandidate, ".")) {
            if (siteNames.contains(siteKeyCandidate)) {
                return siteKeyCandidate;
            }
            if (!StringUtils.contains(siteKeyCandidate, ".")) {
                return null;
            }
        }
    }

    /**
     * Checks whether the URL points to a Jahia content object, which can be mapped to vanity URLs.
     * @return true if current node can be mapped to vanity URLs, otherwise false
     */
    public boolean isMappable() {
        return mappable;
    }


    /**
     * Checks whether the URL points to a Jahia content object, which can be mapped to vanity
     * URLs. If this is the case then also check if there already is a mapping for the current node.
     * @return true if mappings exist(ed) for the current node, otherwise false
     */
    public boolean isMapped() {
        boolean mapped = mappable;
        if (mapped) {
            try {
                Resource resource = getResource();
                if (!resource.getTemplate().equals("default")) {
                    return false;
                }
                JCRNodeWrapper node = resource.getNode();
                if (node != null && !node.isNodeType(VanityUrlManager.JAHIAMIX_VANITYURLMAPPED)) {
                    mapped = false;
                }
            } catch (RepositoryException e) {
                logger.debug("Cannot check if node has the jmix:vanityUrlMapped mixin", e);
            }
        }
        return mapped;
    }

    private VanityUrlService getVanityUrlService() {
        return (VanityUrlService) SpringContextSingleton
                .getBean(VanityUrlService.class.getName());
    }

    /**
     * Gets the site-key of the request resolved by the URL
     * @return the site-key of the given URL
     */
    public String getSiteKey() {
        return siteKey;
    }

    /**
     * Sets the site-key resolved for the current URL
     * @param siteKey the site-key resolved for the current URL
     */
    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public String getSiteKeyByServerName() {
        return siteKeyByServerName;
    }

    /**
     * If value is not null, then URL resolving encountered that the URL has
     * permanently been changed and thus a server-side redirect to the new
     * location should be triggered.
     * @return URL for the new location
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * Set the URL location for a redirection suggestion.
     * @param redirectUrl suggested vanity URL to redirect to
     */
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getVanityUrl() {
        return vanityUrl;
    }

    public void setVanityUrl(String vanityUrl) {
        this.vanityUrl = vanityUrl;
    }

    protected Locale verifyLanguage(String lang) {
        if (StringUtils.isEmpty(lang)) {
            return DEFAULT_LOCALE;
        }

        if (!LanguageCodeConverters.LANGUAGE_PATTERN.matcher(lang).matches()) {
            return null;
        }

        return LanguageCodeConverters.languageCodeToLocale(lang);
    }

    protected String verifyWorkspace(String workspace) {
        if (StringUtils.isEmpty(workspace)) {
            if (workspace == null) {
                workspace = DEFAULT_WORKSPACE;
            }
        } else {
            if (!JCRContentUtils.isValidWorkspace(workspace) && this.workspace == null) {
                throw new JahiaBadRequestException("Unknown workspace '" + workspace + "'");
            }
            if (JCRContentUtils.isValidWorkspace(workspace) && this.workspace != null && !workspace.equals(this.workspace)) {
                throw new JahiaBadRequestException("Invalid workspace '" + workspace + "'");
            }
            if (!JCRContentUtils.isValidWorkspace(workspace) && this.workspace != null) {
                workspace = this.workspace;
                path = this.workspace + "/" + path;
            }
        }

        return workspace;
    }

    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    public Date getVersionDate() {
        return versionDate;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public SiteInfo getSiteInfo() {
        return siteInfo;
    }
}
