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
package org.jahia.bin;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.api.Constants;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.uicomponents.bean.editmode.EditConfiguration;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Url;
import org.jahia.utils.WebUtils;
import org.slf4j.Logger;

/**
 * Servlet for the first entry point in Jahia portal that performs a client-side redirect
 * to the home page of the appropriate site.
 * 
 * @author toto
 */
public class WelcomeServlet extends HttpServlet {

    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = -2055161334153523152L;
    
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(WelcomeServlet.class);
    
    private static final String DEFAULT_LOCALE = Locale.ENGLISH.toString();
    private static final String DASHBOARD_HOME = ".projects.html";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            if (request.getRequestURI().endsWith("/start")) {
                userRedirect(request, response, getServletContext());
            } else {
                defaultRedirect(request, response, getServletContext());
            }
        } catch (Exception e) {
            List<ErrorHandler> handlers = ServicesRegistry.getInstance()
                    .getJahiaTemplateManagerService().getErrorHandler();
            for (ErrorHandler handler : handlers) {
                if (handler.handle(e, request, response)) {
                    return;
                }
            }
            DefaultErrorHandler.getInstance().handle(e, request, response);
        }
    }

    protected void userRedirect(HttpServletRequest request, HttpServletResponse response, ServletContext context) throws Exception {
        JahiaUser user = (JahiaUser) request.getSession().getAttribute(Constants.SESSION_USER);
        JCRUserNode userNode = user != null ? JahiaUserManagerService.getInstance().lookupUserByPath(user.getLocalPath()) : null;
        if (!JahiaUserManagerService.isGuest(user) && userNode.isMemberOfGroup(null, JahiaGroupManagerService.PRIVILEGED_GROUPNAME)) {
            JCRSiteNode site = resolveSite(request, Constants.LIVE_WORKSPACE,
                    JCRContentUtils.getSystemSitePath());
            String language = resolveLanguage(request, site, userNode, true);
            redirect(request.getContextPath() + "/cms/dashboard/default/"+ language + user.getLocalPath() +
                     DASHBOARD_HOME, response);
        } else {
            throw new AccessDeniedException();
        }
    }

    protected void redirect(String url, HttpServletResponse response) throws IOException {
        String targetUrl = response.encodeRedirectURL(url);
        String jsessionIdParameterName = SettingsBean.getInstance().getJsessionIdParameterName();
        if (targetUrl.contains(";" + jsessionIdParameterName)) {
            if (targetUrl.contains("?")) {
                targetUrl = StringUtils.substringBefore(targetUrl, ";" + jsessionIdParameterName + "=") + "?"
                        + StringUtils.substringAfter(targetUrl, "?");
            } else {
                targetUrl = StringUtils.substringBefore(targetUrl, ";" + jsessionIdParameterName + "=");
            }
        }
        WebUtils.setNoCacheHeaders(response);
        response.sendRedirect(targetUrl);
    }

    protected void defaultRedirect(HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws Exception {
        request.getSession(true);

        final JahiaSitesService siteService = JahiaSitesService.getInstance();
        JahiaSite defaultSite = null;
        String defaultSitePath = null;
        final JCRSiteNode site;
        String siteKey = !Url.isLocalhost(request.getServerName()) ? siteService.getSitenameByServerName(request.getServerName()) : null;
        if (siteKey != null) {
            // site resolved by the hostname -> read it with user session to check the access rights
            site = (JCRSiteNode) siteService.getSiteByKey(siteKey);
        } else {
            // use the default site
            defaultSite = siteService.getDefaultSite();
            defaultSitePath = defaultSite != null ? defaultSite.getJCRLocalPath() : null;
            site = (JCRSiteNode) defaultSite;
        }
        
        String redirect = null;
        String pathInfo = request.getPathInfo();

        String defaultLocation = null;
        String mapping = null;

        if (pathInfo != null && (pathInfo.endsWith("mode") || pathInfo.endsWith("mode/"))) {
            String mode = pathInfo.endsWith("/") ? StringUtils.substringBetween(pathInfo, "/", "/") : StringUtils.substringAfter(pathInfo, "/");
            if (SpringContextSingleton.getInstance().getContext().containsBean(mode)) {
                EditConfiguration editConfiguration =  (EditConfiguration) SpringContextSingleton.getInstance().getContext().getBean(mode);
                defaultLocation = editConfiguration.getDefaultLocation();
                mapping = editConfiguration.getDefaultUrlMapping();
            }
        }

        if (site == null && (defaultLocation == null || defaultLocation.contains("$defaultSiteHome"))) {
            userRedirect(request, response, context);
        } else {
            if (defaultSite == null) {
                defaultSite = siteService.getDefaultSite();
                defaultSitePath = defaultSite != null ? defaultSite.getJCRLocalPath() : null;
            }
            
            JahiaUser user = (JahiaUser) request.getSession().getAttribute(Constants.SESSION_USER);
            JCRUserNode userNode = user != null ? JahiaUserManagerService.getInstance().lookupUserByPath(user.getLocalPath()) : null;
            String language = resolveLanguage(request, site, userNode, false);
            if (defaultLocation != null) {
                if (site != null && defaultLocation.contains("$defaultSiteHome")) {
                    JCRNodeWrapper home = site.getHome();
                    if (home == null) {
                        home = resolveSite(request, Constants.EDIT_WORKSPACE, defaultSitePath).getHome();
                    }
                    defaultLocation = defaultLocation.replace("$defaultSiteHome",home.getPath());
                }

                redirect = request.getContextPath() + mapping + "/" + language +defaultLocation;
            } else {
                JCRNodeWrapper home = site.getHome();
                if (home != null) {
                    redirect = request.getContextPath() + "/cms/render/"
                            + Constants.LIVE_WORKSPACE + "/" + language + home.getPath() + ".html";
                } else if (!SettingsBean.getInstance().isDistantPublicationServerMode()) {
                    JCRSiteNode defSite = null;
                    try {
                        defSite = (JCRSiteNode) JCRStoreService.getInstance().getSessionFactory()
                                .getCurrentUserSession().getNode(site.getPath());
                    } catch (PathNotFoundException e) {
                        if (!Url.isLocalhost(request.getServerName())
                                && defaultSite != null
                                && !site.getSiteKey().equals(
                                        defaultSite.getSiteKey())
                                && (!SettingsBean.getInstance()  // the check in this parenthesis is added to prevent immediate servername change in the url, which leads to the side effect with an automatic login on default site after logout on other site 
                                        .isUrlRewriteUseAbsoluteUrls()
                                        || site.getServerName().equals(
                                                defaultSite.getServerName()) || Url
                                            .isLocalhost(defaultSite
                                                    .getServerName()))) {
                            JCRSiteNode defaultSiteNode = (JCRSiteNode) JCRStoreService
                                    .getInstance()
                                    .getSessionFactory()
                                    .getCurrentUserSession(
                                            Constants.LIVE_WORKSPACE)
                                    .getNode(defaultSitePath);
                            if (defaultSiteNode.getHome() != null) {
                                redirect = request.getContextPath()
                                        + "/cms/render/"
                                        + Constants.LIVE_WORKSPACE + "/"
                                        + language
                                        + defaultSiteNode.getHome().getPath() + ".html";
                            }
                        }
                    }
                    if (redirect == null && defSite != null && defSite.getHome() != null) {
                        if (defSite.getHome().hasPermission("editModeAccess")) {
                            redirect = request.getContextPath() + "/cms/edit/"
                                    + Constants.EDIT_WORKSPACE + "/" + language
                                    + defSite.getHome().getPath() + ".html";
                        } else if (defSite.getHome().hasPermission("contributeModeAccess")) {
                            redirect = request.getContextPath() + "/cms/contribute/"
                                    + Constants.EDIT_WORKSPACE + "/" + language
                                    + defSite.getHome().getPath() + ".html";
                        } 
                    } 
                } 
            }
            if (redirect == null) {
                redirect(request.getContextPath() + "/start", response);
                return;
            }
            redirect(redirect, response);
        }
    }

    protected JCRSiteNode resolveSite(HttpServletRequest request, String workspace, String fallbackSitePath) throws JahiaException, RepositoryException {
        JahiaSitesService siteService = JahiaSitesService.getInstance();
        JahiaSite resolvedSite = !Url.isLocalhost(request.getServerName()) ? siteService.getSiteByServerName(request.getServerName()) : null;
        String sitePath = resolvedSite == null ? fallbackSitePath : resolvedSite.getJCRLocalPath(); 

        return sitePath != null ? (JCRSiteNode) JCRStoreService.getInstance().getSessionFactory()
                .getCurrentUserSession(workspace).getNode(sitePath) : null;
    }
    
    protected String resolveLanguage(HttpServletRequest request, final JCRSiteNode site, JCRUserNode user, boolean userRedirect)
            throws JahiaException {
        List<Locale> siteLanguages = null;
        if (!userRedirect && site != null && !JahiaSitesService.SYSTEM_SITE_KEY.equals(site.getSiteKey())) {
            try {
                siteLanguages = site.getActiveLiveLanguagesAsLocales();
            } catch (Exception t) {
                logger.debug("Exception while getting language settings as locales", t);
                siteLanguages = Collections.emptyList();
            }
        }

        // first we will check the preferred user locale (if it is among the
        Locale preferredLocale = UserPreferencesHelper.getPreferredLocale(user);
        if (preferredLocale != null && isLocaleSupported(site, siteLanguages, preferredLocale)) {
            return preferredLocale.toString();
        }

        // retrieve the browser locales, but if Accept-Language header is missing we won't fallback to the default system locale
        for (Enumeration<?> requestLocales = Util.getRequestLocales(request); requestLocales.hasMoreElements();) {
            final Locale curLocale = (Locale) requestLocales.nextElement();
            if (curLocale != null) { 
                // check that the site contains the language and the home page exists in live for that language
                if (isLocaleSupported(site, siteLanguages, curLocale)) {
                    return curLocale.toString();
                }
                if (!StringUtils.isEmpty(curLocale.getCountry())) {
                    // check the same but for language only
                    final Locale langOnlyLocale = LanguageCodeConverters.languageCodeToLocale(curLocale.getLanguage());
                    if (isLocaleSupported(site, siteLanguages, langOnlyLocale)) {
                        return langOnlyLocale.toString();
                    }
                }
            }
        }

        String lang = site.getDefaultLanguage();
        if (lang != null) {
            // use site's default language
            return lang;
        }

        // nothing matches -> fallback to default
        return StringUtils.defaultIfEmpty(SettingsBean.getInstance().getDefaultLanguageCode(), DEFAULT_LOCALE);
    }

    private boolean isLocaleSupported(JCRSiteNode site, List<Locale> siteLanguages, Locale locale) {
        return siteLanguages == null || siteLanguages.contains(locale) && ensureHomePageExists(site, locale);
    }

    private boolean ensureHomePageExists(final JCRSiteNode site, final Locale curLocale) {
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, curLocale,
                    new JCRCallback<Boolean>() {
                        public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            try {
                                JCRSiteNode nodeByIdentifier = (JCRSiteNode) session.getNodeByIdentifier(site
                                        .getIdentifier());
                                return nodeByIdentifier.getHome() != null;
                            } catch (RepositoryException e) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("This site does not have a published home in language " + curLocale, e);
                                }
                            }
                            return Boolean.FALSE;
                        }
                    });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }
}