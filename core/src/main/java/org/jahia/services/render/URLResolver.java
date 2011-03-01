/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render;

import static org.jahia.api.Constants.LIVE_WORKSPACE;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jahia.bin.Render;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;

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

    private static final String VANITY_URL_NODE_PATH_SEGMENT = "/" + VanityUrlManager.VANITYURLMAPPINGS_NODE + "/";

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(URLResolver.class);

    private static String[] servletsAllowingUrlMapping = new String[] {
            StringUtils.substringAfterLast(Render.getRenderServletPath(), "/")
    };

    private String urlPathInfo = null;
    private String servletPart;
    private String workspace;
    private Locale locale;
    private String path;
    private String siteKey;
    private boolean mappable = false;

    private String redirectUrl;
    private String vanityUrl;
    private String method;

    public void setRenderContext(RenderContext renderContext) {
        this.renderContext = renderContext;
    }

    private RenderContext renderContext;

    /**
     * Initializes an instance of this class. This constructor is mainly used when
     * resolving URLs of incoming requests.
     *
     * @param urlPathInfo  the path info (usually obtained with @link javax.servlet.http.HttpServletRequest.getPathInfo())
     * @param serverName  the server name (usually obtained with @link javax.servlet.http.HttpServletRequest.getServerName())
     * @param request  the current HTTP servlet request object 
     */
    public URLResolver(String urlPathInfo, String serverName, HttpServletRequest request) {
        super();
        this.method = request.getMethod();
        this.urlPathInfo = urlPathInfo;
        servletPart = StringUtils.substring(getUrlPathInfo(), 1,
                StringUtils.indexOf(getUrlPathInfo(), "/", 1));
        path = StringUtils.substring(getUrlPathInfo(),
                servletPart.length() + 2, getUrlPathInfo().length());
        if (!resolveUrlMapping(serverName)) {
            init();
            if (!URLGenerator.isLocalhost(serverName) && isMappable()
                    && SettingsBean.getInstance().isPermanentMoveForVanityURL()) {
                try {
                    VanityUrl defaultVanityUrl = getVanityUrlService()
                            .getVanityUrlForWorkspaceAndLocale(getNode(),
                                    workspace, locale);
                    if (defaultVanityUrl != null && defaultVanityUrl.isActive()) {
                        if (request == null || StringUtils.isEmpty(request.getQueryString())) {
                            setRedirectUrl(defaultVanityUrl.getUrl());
                        } else {
                            setRedirectUrl(defaultVanityUrl.getUrl() + "?" + request.getQueryString());
                        }
                    }
                } catch (PathNotFoundException e) {
                    logger.debug("Path not found : " + urlPathInfo);
                } catch (AccessDeniedException e) {
                    logger.debug("User has no access to the resource, so there will not be a redirection");
                } catch (RepositoryException e) {
                    logger.warn("Error when trying to check whether there is a vanity URL mapping", e);
                }
            }
        }
    }

    /**
     * Initializes an instance of this class. This constructor is mainly used when
     * trying to find mapping for URLs in outgoing requests.
     *
     * @param url   URL in HTML links of outgoing requests
     * @param context  The current request in order to obtain the context path
     */
    public URLResolver(String url, RenderContext context) {
        method = context.getRequest().getMethod();
        renderContext = context;
        String contextPath = context.getRequest().getContextPath();

        this.urlPathInfo = StringUtils.substringAfter(url, contextPath + context.getServletPath());
        this.servletPart = StringUtils.substringAfterLast(context.getServletPath(), "/");

        if (!StringUtils.isEmpty(urlPathInfo)) {
            path = getUrlPathInfo().substring(1);
            init();
        }
    }

    private void init() {
        workspace = StringUtils.defaultIfEmpty(StringUtils.substringBefore(
                path, "/"), DEFAULT_WORKSPACE);
        path = StringUtils.substringAfter(path, "/");
        String langCode = StringUtils.substringBefore(path, "/");

        locale = StringUtils.isEmpty(langCode) ? DEFAULT_LOCALE
                : LanguageCodeConverters.languageCodeToLocale(langCode);
        path = "/" + StringUtils.substringAfter(path, "/");

        // TODO: this is perhaps a temporary limitation as URL points to special templates, when 
        // there are more than one dots - and the path needs to end with .html
        String lastPart = StringUtils.substringAfterLast(path, "/");
        int indexOfHTMLSuffix = lastPart.indexOf(".html");
        if (isServletAllowingUrlMapping() && indexOfHTMLSuffix > 0
                && lastPart.indexOf(".") == indexOfHTMLSuffix && lastPart.endsWith(".html")) {
            mappable = true;
        }
    }

    private boolean isServletAllowingUrlMapping() {
        boolean isServletAllowingUrlMapping = false;
        for (String servletAllowingUrlMapping : servletsAllowingUrlMapping) {
            if (servletAllowingUrlMapping.equals(servletPart)) {
                isServletAllowingUrlMapping = true;
                break;
            }
        }
        return isServletAllowingUrlMapping;
    }

    protected boolean resolveUrlMapping(String serverName) {
        boolean mappingResolved = false;
        if (getSiteKey() == null) {
            String siteKeyInPath = StringUtils.substringBetween(getPath(), "/sites/", "/");
            if (!StringUtils.isEmpty(siteKeyInPath)) {
                setSiteKey(siteKeyInPath);
            } else if (!URLGenerator.isLocalhost(serverName)) {
                try {
                    JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByServerName(
                            serverName);
                    if (site != null) {
                        setSiteKey(site.getSiteKey());
                    }
                } catch (JahiaException e) {
                    logger.warn("Error finding site via servername: " + serverName, e);
                }
            }
        }

        if (isServletAllowingUrlMapping() && !URLGenerator.isLocalhost(serverName)) {
            String tempPath = null;
            try {
                String tempWorkspace = StringUtils.defaultIfEmpty(StringUtils
                        .substringBefore(getPath(), "/"), DEFAULT_WORKSPACE);
                tempPath = StringUtils.substringAfter(getPath(), "/");
                List<VanityUrl> vanityUrls = getVanityUrlService()
                        .findExistingVanityUrls("/" + tempPath,
                                StringUtils.EMPTY, tempWorkspace);
                VanityUrl resolvedVanityUrl = null;
                for (VanityUrl vanityUrl : vanityUrls) {
                    if (vanityUrl.isActive()
                            && (StringUtils.isEmpty(getSiteKey()) || getSiteKey().equals(
                            vanityUrl.getSite()))) {
                        resolvedVanityUrl = vanityUrl;
                        break;
                    }
                }
                if (resolvedVanityUrl != null) {
                    workspace = tempWorkspace;
                    locale = StringUtils.isEmpty(resolvedVanityUrl
                            .getLanguage()) ? DEFAULT_LOCALE
                            : LanguageCodeConverters
                            .languageCodeToLocale(resolvedVanityUrl
                                    .getLanguage());
                    path = StringUtils.substringBefore(resolvedVanityUrl
                            .getPath(), VANITY_URL_NODE_PATH_SEGMENT)
                            + ".html";
                    setVanityUrl(resolvedVanityUrl.getUrl());
                    if (SettingsBean.getInstance()
                            .isPermanentMoveForVanityURL()
                            && !resolvedVanityUrl.isDefaultMapping()) {
                        VanityUrl defaultVanityUrl = getVanityUrlService()
                                .getVanityUrlForWorkspaceAndLocale(getNode(),
                                        workspace, locale);
                        if (defaultVanityUrl != null
                                && defaultVanityUrl.isActive() && !resolvedVanityUrl.equals(defaultVanityUrl)) {
                            setRedirectUrl(defaultVanityUrl.getUrl());
                        }
                    }
                    mappingResolved = true;
                }
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
        return locale;
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
     * @throws RepositoryException
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
     * @throws RepositoryException
     */
    public Resource getResource() throws RepositoryException {
        return resolveResource(getWorkspace(), getLocale(), getPath(), null, null);
    }

    /**
     * Creates a versioned resource from the path in the URL.
     * <p/>
     * The path should looks like : [nodepath][.templatename].[templatetype] or [nodepath].[templatetype]
     *
     * Workspace, locale and path are taken from the given resolved URL.
     *
     * @param versionDate
     *            The version date to get the resource of a versioned node, or the closest before this date
     * @param versionLabel
     * @return The resource, if found
     * @throws PathNotFoundException
     *             if the resource cannot be resolved
     * @throws RepositoryException
     */
    public Resource getResource(Date versionDate, String versionLabel)
            throws RepositoryException {
        return resolveResource(getWorkspace(), getLocale(), getPath(),
                versionDate,versionLabel);
    }

    public Resource getResource(String path) throws RepositoryException {
        return resolveResource(getWorkspace(), getLocale(), path, null, null);
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
     * @throws RepositoryException
     */
    protected JCRNodeWrapper resolveNode(final String workspace,
                                         final Locale locale, final String path) throws RepositoryException {
        if (logger.isDebugEnabled()) {
            logger.debug("Resolving node for workspace '" + workspace
                    + "' locale '" + locale + "' and path '" + path + "'");
        }
        return JCRTemplate.getInstance().doExecuteWithSystemSession(null,
                workspace, new JCRCallback<JCRNodeWrapper>() {
                    public JCRNodeWrapper doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        String nodePath = path.endsWith("/*")?path.substring(0,path.lastIndexOf("/*")):path;
                        JCRNodeWrapper node = null;
                        while (true) {
                            try {
                                node = session.getNode(nodePath);
                                break;
                            } catch (PathNotFoundException ex) {
                                if (nodePath.lastIndexOf("/") < nodePath.lastIndexOf(".")) {
                                    nodePath = nodePath.substring(0,nodePath.lastIndexOf("."));
                                } else {
                                    throw new PathNotFoundException("'" + nodePath + "'not found");
                                }
                            }
                        }

                        JCRSiteNode site = node.getResolveSite();

                        JCRSessionWrapper userSession = site != null
                                && site.getDefaultLanguage() != null
                                && site.isMixLanguagesActive() ? JCRSessionFactory
                                .getInstance().getCurrentUserSession(workspace,
                                        locale)
                                : JCRSessionFactory
                                .getInstance()
                                .getCurrentUserSession(
                                        workspace,
                                        locale,
                                        null);

                        try {
                            node = userSession.getNode(nodePath);
                        } catch (PathNotFoundException e) {
                            throw new AccessDeniedException(path);
                        }

                        return node;
                    }
                });
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
     * @param versionDate
     *            The version date to get the resource of a versioned node, or the closest before this date
     * @param versionLabel
     * @return The resource, if found
     * @throws PathNotFoundException
     *             if the resource cannot be resolved
     * @throws RepositoryException
     */
    protected Resource resolveResource(final String workspace, final Locale locale, final String path,
                                       final Date versionDate, final String versionLabel)
            throws RepositoryException {
        if (logger.isDebugEnabled()) {
            logger.debug("Resolving resource for workspace '" + workspace
                    + "' locale '" + locale + "' and path '" + path + "'");
        }
        final URLResolver urlResolver = this;
        return JCRTemplate.getInstance().doExecuteWithSystemSession(null,
                workspace, locale, new JCRCallback<Resource>() {
                    public Resource doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        String ext = null;
                        String tpl = null;
                        String nodePath = path;
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
                                            if (req.getParameter("mainResource") != null) {
                                                Resource resource = urlResolver.getResource(req.getParameter(
                                                        "mainResource"));
                                                renderContext.setAjaxResource(resource);
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

                        JCRSiteNode site = node.getResolveSite();
                        JCRSessionWrapper userSession;

                        if (site != null) {
                            String defaultLanguage = site.getDefaultLanguage();
                            boolean mixLanguagesActive = site
                                    .isMixLanguagesActive();

                            if (defaultLanguage != null && mixLanguagesActive) {
                                userSession = JCRSessionFactory
                                        .getInstance()
                                        .getCurrentUserSession(
                                                workspace,
                                                locale,
                                                LanguageCodeConverters
                                                        .languageCodeToLocale(defaultLanguage));
                            } else {
                                userSession = JCRSessionFactory.getInstance()
                                        .getCurrentUserSession(workspace,
                                                locale);
                            }
                        } else {
                            userSession = JCRSessionFactory.getInstance()
                                    .getCurrentUserSession(workspace, locale);
                        }
                        userSession.setVersionDate(versionDate);
                        userSession.setVersionLabel(versionLabel);

                        try {
                            node = userSession.getNode(nodePath);
                        } catch (PathNotFoundException e) {
                            throw new AccessDeniedException(path);
                        }

                        Resource r = new Resource(node, ext, tpl, Resource.CONFIGURATION_PAGE);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Resolved resource: " + r);
                        }
                        return r;
                    }
                });
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
                JCRNodeWrapper node = getNode();
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
}
