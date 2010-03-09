/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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

import java.util.List;
import java.util.Locale;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Edit;
import org.jahia.bin.Jahia;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.utils.LanguageCodeConverters;

public class URLResolver {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    private static final String DEFAULT_WORKSPACE = LIVE_WORKSPACE;

    private static Logger logger = Logger.getLogger(URLResolver.class);

    private String urlPathInfo = null;
    private String workspace;
    private Locale locale;
    private String path;
    private String siteKey;
    private boolean mapable = false;

    public URLResolver(String urlPathInfo, String siteKey) {
        super();
        this.urlPathInfo = urlPathInfo;
        this.siteKey = siteKey;
        path = StringUtils.substringAfter(getUrlPathInfo().substring(1), "/");
        if (!resolveUrlMapping()) {
            init();
        }
    }

    public URLResolver(String url, HttpServletRequest request) {
        String contextPath = request.getContextPath();
        if (url.startsWith(contextPath + Edit.getRenderServletPath())) {
            this.urlPathInfo = StringUtils.substringAfter(url, Edit
                    .getRenderServletPath());
        } else if (url.startsWith(contextPath + Edit.getEditServletPath())) {
            this.urlPathInfo = StringUtils.substringAfter(url, Edit
                    .getEditServletPath());
        }
        if (this.urlPathInfo != null) {
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
        
        // TODO: this is perhaps a temporary limitation as URL points to special templates
        String lastPart = StringUtils.substringAfterLast(path, "/");
        if (!StringUtils.substringBefore(lastPart, ".html").contains(".")) {
            mapable = true;
        }
    }

    private boolean resolveUrlMapping() {
        boolean mappingResolved = false;
        String tempPath = null; 
        try {
            String tempWorkspace = StringUtils.defaultIfEmpty(StringUtils.substringBefore(
                    getPath(), "/"), DEFAULT_WORKSPACE);            
            tempPath = StringUtils.substringAfter(getPath(), "/");            
            if (getSiteKey() == null) {
                setSiteKey(StringUtils.substringBetween(getPath(), "/sites/",
                        "/"));
            }
            List<VanityUrl> vanityUrls = getVanityUrlService()
                    .findExistingVanityUrls("/" + tempPath, StringUtils.EMPTY);
            VanityUrl resolvedVanityUrl = null;
            if (!vanityUrls.isEmpty() && !StringUtils.isEmpty(getSiteKey())) {
                for (VanityUrl vanityUrl : vanityUrls) {
                    if (getSiteKey().equals(vanityUrl.getSite())) {
                        resolvedVanityUrl = vanityUrl;
                    }
                }
            }
            if (resolvedVanityUrl == null && StringUtils.isEmpty(getSiteKey())
                    && !vanityUrls.isEmpty()) {
                resolvedVanityUrl = vanityUrls.get(0);
            }
            if (resolvedVanityUrl != null) {
                workspace = tempWorkspace;
                locale = StringUtils.isEmpty(resolvedVanityUrl.getLanguage()) ? DEFAULT_LOCALE
                        : LanguageCodeConverters
                                .languageCodeToLocale(resolvedVanityUrl
                                        .getLanguage());
                path = StringUtils.substringBeforeLast(resolvedVanityUrl.getPath(), "/") + ".html";
                mappingResolved = true;
            }
        } catch (RepositoryException e) {
            logger.warn("Error when trying to resolve URL mapping: "
                    + tempPath, e);
        }
        return mappingResolved;
    }

    public String getUrlPathInfo() {
        return urlPathInfo;
    }

    public String getWorkspace() {
        return workspace;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getPath() {
        return path;
    }

    public JCRNodeWrapper getNode() throws RepositoryException {
        return resolveNode(getWorkspace(), getLocale(), getPath());
    }

    public Resource getResource() throws RepositoryException {
        return resolveResource(getWorkspace(), getLocale(), getPath(), null);
    }

    public Resource getResource(String versionNumber)
            throws RepositoryException {
        return resolveResource(getWorkspace(), getLocale(), getPath(),
                versionNumber);
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
                        String nodePath = path;
                        JCRNodeWrapper node;
                        while (true) {
                            int i = nodePath.lastIndexOf('.');
                            if (i > nodePath.lastIndexOf('/')) {
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
     * @return The resource, if found
     * @throws PathNotFoundException
     *             if the resource cannot be resolved
     * @throws RepositoryException
     */
    protected Resource resolveResource(final String workspace,
            final Locale locale, final String path, final String versionNumber)
            throws RepositoryException {
        if (logger.isDebugEnabled()) {
            logger.debug("Resolving resource for workspace '" + workspace
                    + "' locale '" + locale + "' and path '" + path + "'");
        }

        return JCRTemplate.getInstance().doExecuteWithSystemSession(null,
                workspace, new JCRCallback<Resource>() {
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

                        // handle version number
                        if (versionNumber != null) {
                            JCRNodeWrapper versionNode = node
                                    .getFrozenVersion(versionNumber);
                            if (versionNode != null) {
                                node = versionNode;
                            } else {
                                logger
                                        .error("Error while retrieving node with path "
                                                + nodePath
                                                + " and version "
                                                + versionNumber);
                            }
                        }

                        JahiaSite site = node.resolveSite();
                        JCRSessionWrapper userSession;

                        if (site != null) {
                            ProcessingContext ctx = Jahia.getThreadParamBean();
                            ctx.setSite(site);
                            ctx.setContentPage(site.getHomeContentPage());
                            ctx.setThePage(site.getHomePage());
                            ctx.getSessionState().setAttribute(
                                    ProcessingContext.SESSION_SITE, site);
                            ctx
                                    .getSessionState()
                                    .setAttribute(
                                            ProcessingContext.SESSION_LAST_REQUESTED_PAGE_ID,
                                            site.getHomePageID());

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
                        try {
                            node = userSession.getNode(nodePath);
                        } catch (PathNotFoundException e) {
                            throw new AccessDeniedException(path);
                        }

                        Resource r = new Resource(node, ext, null, tpl);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Resolved resource: " + r);
                        }
                        return r;
                    }
                });
    }

    public boolean isMapable() {
        return mapable;
    }

    private VanityUrlService getVanityUrlService() {
        return (VanityUrlService) SpringContextSingleton
                .getBean(VanityUrlService.class.getName());
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }
}
