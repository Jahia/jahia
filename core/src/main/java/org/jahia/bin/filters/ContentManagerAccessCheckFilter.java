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

    private static final Logger logger = LoggerFactory
            .getLogger(ContentManagerAccessCheckFilter.class);

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
        if (cfg.equals("repositoryexplorer")) {
            try {
                site = JCRSessionFactory.getInstance().getCurrentUserSession().getRootNode();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } else if (cfg.equals("categorymanager")) {
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

    protected JCRSiteNode getSite(HttpServletRequest request) {
        String siteId = request.getParameter("site");

        try {
            JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession();
            if (StringUtils.isNotEmpty(siteId)) {
                return currentUserSession.getNodeByUUID(siteId).getResolveSite();
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
