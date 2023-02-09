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
package org.jahia.bin.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaControllerUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.services.uicomponents.bean.contentmanager.ManagerConfiguration;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationListener;

/**
 * Protects access to the content manager and content picker by enforcing permission checks and validating parameters.
 *
 * @author Sergiy Shyrkov
 */
public class ContentManagerAccessCheckFilter implements Filter,
        ApplicationListener<TemplatePackageRedeployedEvent> {

    private static final String SITE_TEMPLATE_PACKAGE_ID = "org.jahia.contentManager.site.templatePackageId";

    private static final Logger logger = LoggerFactory
            .getLogger(ContentManagerAccessCheckFilter.class);

    /**
     * Returns the template package of the current site, based on the information provided when the Content Manager window was called. If no
     * site information was provided, the template package of the default site it returned. If there is no default site, <code>null</code>
     * is returned.
     *
     * @param request
     *            current HTTP request object
     * @return the template package of the current site, based on the information provided when the Content Manager window was called. If no
     *         site information was provided, the template package of the default site it returned. If there is no default site,
     *         <code>null</code> is returned
     */
    public static JahiaTemplatesPackage getCurrentSiteTemplatePackage(HttpServletRequest request) {
        JahiaTemplatesPackage pkg = null;
        String pkgId = (String) request.getAttribute(SITE_TEMPLATE_PACKAGE_ID);
        if (pkgId != null) {
            pkg = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(pkgId);
        } else {
            JCRSiteNode site = getSite(request);
            if (site != null) {
                pkg = site.getTemplatePackage();
            }
        }

        return pkg;
    }

    private String defaultContentManager = "repositoryexplorer";

    private String defaultContentPicker = "filepicker";

    private Map<String, String> mapping;

    private boolean requireAuthenticatedUser = true;

    private SettingsBean settingsBean;

    protected boolean checkConfig(HttpServletRequest request) {
        String cfg = StringUtils.defaultIfEmpty(request.getParameter("conf"),
                isContentPicker(request) ? defaultContentPicker : defaultContentManager);

        if (!getMapping().containsKey(cfg)) {
            return false;
        }

        return true;
    }

    protected boolean checkWorkspace(HttpServletRequest request) {
        return JCRContentUtils.isValidWorkspace(request.getParameter("workspace"), true);
    }

    public void destroy() {
        // do nothing
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (requireAuthenticatedUser && JahiaUserManagerService.isGuest(getCurrentUser()) || settingsBean.isReadOnlyMode()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        if (!checkWorkspace(request)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String cfg = StringUtils.defaultIfEmpty(request.getParameter("conf"),
                isContentPicker(request) ? defaultContentPicker : defaultContentManager);

        if (!getMapping().containsKey(cfg)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        JCRNodeWrapper site;
        if (cfg.equals("repositoryexplorer") || cfg.startsWith("repositoryexplorer-")) {
            try {
                site = JCRSessionFactory.getInstance().getCurrentUserSession().getRootNode();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } else if (cfg.equals("categorymanager") || cfg.startsWith("categorymanager-")) {
            try {
                site = JCRSessionFactory.getInstance().getCurrentUserSession().getNode("/sites/systemsite");
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } else {
            site = getSite(request);
            if (site == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            JahiaTemplatesPackage tmplPack = ((JCRSiteNode) site).getTemplatePackage();
            if (tmplPack != null) {
                // remember the ID of the template package for the current site
                request.setAttribute(SITE_TEMPLATE_PACKAGE_ID, tmplPack.getId());
            }
        }

        if (requireAuthenticatedUser) {
            String permission = getMapping().get(cfg);
            if (permission == null && isContentPicker(request)) {
                permission = "jcr:read_default";
            }

            if (permission != null && !isAllowed(permission, site)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        chain.doFilter(servletRequest, servletResponse);
    }

    protected JahiaUser getCurrentUser() {
        return JCRSessionFactory.getInstance().getCurrentUser();
    }

    protected Map<String, String> getMapping() {
        if (mapping == null) {
            mapping = new HashMap<String, String>();
            for (JahiaTemplatesPackage aPackage : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackages()) {
                if (aPackage.getContext() != null) {
                    for (Map.Entry<String, ManagerConfiguration> cfg : BeanFactoryUtils.beansOfTypeIncludingAncestors(
                                    aPackage.getContext(),
                                    ManagerConfiguration.class).entrySet()) {
                        mapping.put(cfg.getKey(), cfg.getValue().getRequiredPermission());
                    }
                }
            }
        }

        return mapping;
    }

    protected static JCRSiteNode getSite(HttpServletRequest request) {
        String siteId = request.getParameter("site");

        try {
            JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession();
            if (StringUtils.isNotEmpty(siteId)) {
                if (siteId.startsWith("/")) {
                    return currentUserSession.getNode(siteId).getResolveSite();
                } else {
                    return currentUserSession.getNodeByUUID(siteId).getResolveSite();
                }
            } else {
                JahiaSitesService siteService = JahiaSitesService.getInstance();
                JahiaSite defaultSite = siteService.getDefaultSite();
                if (defaultSite != null) {
                    return (JCRSiteNode) currentUserSession.getNodeByUUID(((JCRSiteNode) defaultSite).getIdentifier());
                }
            }
        } catch (RepositoryException e) {
            logger.warn("Unble to lookup site for UUID '{}'", siteId);
        }

        return null;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    protected boolean isAllowed(String permission, JCRNodeWrapper site) {
        boolean allowed = false;
        try {
            allowed = JahiaControllerUtils
                    .hasRequiredPermission(site, getCurrentUser(), permission);
        } catch (ItemNotFoundException e) {
            // ignore
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
        return allowed;
    }

    protected boolean isContentPicker(HttpServletRequest request) {
        return request.getRequestURI().equals(Jahia.getContextPath() + "/engines/contentpicker.jsp");
    }

    public void onApplicationEvent(TemplatePackageRedeployedEvent event) {
        mapping = null;
    }

    public void setDefaultContentManager(String defaultContentManager) {
        this.defaultContentManager = defaultContentManager;
    }

    public void setDefaultContentPicker(String defaultContentPicker) {
        this.defaultContentPicker = defaultContentPicker;
    }

    public void setRequireAuthenticatedUser(boolean requireAuthenticatedUser) {
        this.requireAuthenticatedUser = requireAuthenticatedUser;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }
}
