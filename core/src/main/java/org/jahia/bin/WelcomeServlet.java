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
package org.jahia.bin;

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
import org.jahia.services.seo.urlrewrite.SessionidRemovalResponseWrapper;
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

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

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

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(WelcomeServlet.class);

    private static final String DEFAULT_LOCALE = Locale.ENGLISH.toString();
    private static final String DASHBOARD_HOME = "/jahia/dashboard";
    private static final String HTML_EXTENSION = ".html";
    private static final String DEFAULT_SITE_HOME = "$defaultSiteHome";

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
            redirect(request.getContextPath() + DASHBOARD_HOME, response);
        } else {
            throw new AccessDeniedException();
        }
    }

    protected void redirect(String url, HttpServletResponse response) throws IOException {
        String targetUrl = response.encodeRedirectURL(url);
        if (SettingsBean.getInstance().isDisableJsessionIdParameter()) {
            targetUrl = SessionidRemovalResponseWrapper.removeJsessionId(targetUrl);
        }
        WebUtils.setNoCacheHeaders(response);
        response.sendRedirect(targetUrl);
    }

    protected void defaultRedirect(HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws Exception {
        request.getSession(true);

        final JahiaSitesService siteService = JahiaSitesService.getInstance();
        String siteKey = !Url.isLocalhost(request.getServerName()) ? siteService.getSitenameByServerName(request.getServerName()) : null;
        JahiaSite defaultSite = siteKey == null ? siteService.getDefaultSite() : null;
        final JCRSiteNode site = siteKey != null ? (JCRSiteNode) siteService.getSiteByKey(siteKey) : (JCRSiteNode) defaultSite;

        String redirect = null;
        String pathInfo = request.getPathInfo();

        String defaultLocation = getDefaultLocationFromPathInfo(pathInfo);
        String mapping = getDefaultUrlMappingFromPathInfo(pathInfo);

        if (site == null && (defaultLocation == null || defaultLocation.contains(DEFAULT_SITE_HOME))) {
            userRedirect(request, response, context);
        } else {
            redirect = getRedirectPath(request, siteService, defaultSite, site, redirect, defaultLocation, mapping);
            // Add query string to the redirect URL
            if (request.getQueryString() != null) {
                redirect += "?" + request.getQueryString();
            }
            if (redirect == null) {
                redirect(request.getContextPath() + DASHBOARD_HOME, response);
                return;
            }
            redirect(redirect, response);
        }
    }

    private String getRedirectPath(HttpServletRequest request, JahiaSitesService siteService, JahiaSite defaultSite, JCRSiteNode site,
                                   String redirect, String defaultLocation, String mapping) throws JahiaException, RepositoryException {
        defaultSite = defaultSite == null ? siteService.getDefaultSite() : defaultSite;
        String defaultSitePath = defaultSite != null ? defaultSite.getJCRLocalPath() : null;

        JahiaUser user = (JahiaUser) request.getSession().getAttribute(Constants.SESSION_USER);
        JCRUserNode userNode = user != null ? JahiaUserManagerService.getInstance().lookupUserByPath(user.getLocalPath()) : null;
        String language = resolveLanguage(request, site, userNode, false);
        if (defaultLocation != null) {
            defaultLocation = getDefaultLocation(request, site, defaultLocation, defaultSitePath);
            redirect = request.getContextPath() + mapping + "/" + language + defaultLocation;
        } else {
            redirect = getRedirectUrlForHomePage(request, defaultSite, site, redirect, defaultSitePath, language);
        }
        return redirect;
    }

    private String getDefaultLocation(HttpServletRequest request, JCRSiteNode site, String defaultLocation, String defaultSitePath)
            throws RepositoryException, JahiaException {
        if (site != null && defaultLocation.contains(DEFAULT_SITE_HOME)) {
            JCRNodeWrapper home = site.getHome() == null
                    ? resolveSite(request, Constants.EDIT_WORKSPACE, defaultSitePath).getHome()
                    : site.getHome();
            defaultLocation = defaultLocation.replace(DEFAULT_SITE_HOME,home.getPath());
        }
        return defaultLocation;
    }

    private String getRedirectUrlForHomePage(HttpServletRequest request, JahiaSite defaultSite, JCRSiteNode site, String redirect,
            String defaultSitePath, String language) throws RepositoryException {
        JCRNodeWrapper home = site.getHome();
        if (home != null) {
            redirect = request.getContextPath() + "/cms/render/"
                    + Constants.LIVE_WORKSPACE + "/" + language + home.getPath() + HTML_EXTENSION;
        } else if (!SettingsBean.getInstance().isDistantPublicationServerMode()) {
            JCRSiteNode defSite = null;
            try {
                defSite = (JCRSiteNode) JCRStoreService.getInstance().getSessionFactory()
                        .getCurrentUserSession().getNode(site.getPath());
            } catch (PathNotFoundException e) {
                if (isValidDefaultSite(request, defaultSite, site)) {
                    redirect = getRedirectedDefaultSite(request, defaultSitePath, redirect, language);
                }
            }
            redirect = getHomePageRedirect(request, redirect, language, defSite);
        }
        return redirect;
    }

    private String getRedirectedDefaultSite(HttpServletRequest request, String defaultSitePath, String redirect, String language)
            throws RepositoryException {
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
                    + defaultSiteNode.getHome().getPath() + HTML_EXTENSION;
        }
        return redirect;
    }

    private boolean isValidDefaultSite(HttpServletRequest request, JahiaSite defaultSite, JCRSiteNode site) {
        return !Url.isLocalhost(request.getServerName()) && defaultSite != null && !site.getSiteKey().equals(defaultSite.getSiteKey()) && (
                !SettingsBean
                        .getInstance()  // the check in this parenthesis is added to prevent immediate servername change in the url, which leads to the side effect with an automatic login on default site after logout on other site
                        .isUrlRewriteUseAbsoluteUrls() || site.getServerName().equals(defaultSite.getServerName()) || Url
                        .isLocalhost(defaultSite.getServerName()));
    }

    private String getModeFromPathInfo(String pathInfo) {
        if (pathInfo != null && (pathInfo.endsWith("mode") || pathInfo.endsWith("mode/"))) {
            return pathInfo.endsWith("/") ? StringUtils.substringBetween(pathInfo, "/", "/") : StringUtils.substringAfter(pathInfo, "/");
        }
        return null;
    }

    private String getDefaultUrlMappingFromPathInfo(String pathInfo) {
        String mode = getModeFromPathInfo(pathInfo);
        if (mode != null && SpringContextSingleton.getInstance().getContext().containsBean(mode)) {
            return ((EditConfiguration) SpringContextSingleton.getInstance().getContext().getBean(mode)).getDefaultUrlMapping();
        }
        return null;
    }

    private String getDefaultLocationFromPathInfo(String pathInfo) {
        String mode = getModeFromPathInfo(pathInfo);
        if (mode != null && SpringContextSingleton.getInstance().getContext().containsBean(mode)) {
            return ((EditConfiguration) SpringContextSingleton.getInstance().getContext().getBean(mode)).getDefaultLocation();
        }
        return null;
    }

    /**
     * Redirect to the default home page
     * @param request
     * @param redirect
     * @param language
     * @param defSite
     * @return the home page base on the permission.
     * @throws RepositoryException
     */
    private String getHomePageRedirect(HttpServletRequest request, String redirect, String language, JCRSiteNode defSite)
            throws RepositoryException {
        if (redirect == null && defSite != null && defSite.getHome() != null) {
            if (defSite.getHome().hasPermission("jContentAccess")) {
                redirect = String.format("%s/jahia/jcontent/%s/%s/pages", request.getContextPath(), defSite.getSiteKey(),
                        defSite.getDefaultLanguage());
            }
            if (defSite.getHome().hasPermission("pageComposerAccess")) {
                redirect = request.getContextPath() + "/jahia/page-composer/"
                        + Constants.EDIT_WORKSPACE + "/" + language
                        + defSite.getHome().getPath() + HTML_EXTENSION;
            }
        }
        return redirect;
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
                    // check if site have one locale (language) matching only language part of browser locale
                    // example:
                    // - browser language: 'en_KY'
                    // - site languages: 'fr', 'fr_FR', 'en'
                    // return: 'en'
                    final Locale langOnlyLocale = LanguageCodeConverters.languageCodeToLocale(curLocale.getLanguage());
                    if (isLocaleSupported(site, siteLanguages, langOnlyLocale)) {
                        return langOnlyLocale.toString();
                    }
                } else {
                    // check if site have one locale (language+country) matching the browser locale (that doesn't have country)
                    // example:
                    // - browser language: 'en'
                    // - site languages: 'fr', 'fr_FR', 'en_KY'
                    // return: 'en_KY'
                    if (siteLanguages != null && !siteLanguages.isEmpty()) {
                        for (Locale siteLanguage : siteLanguages) {
                            if (siteLanguage.getLanguage().equals(curLocale.getLanguage()) && ensureHomePageExists(site, siteLanguage)) {
                                return siteLanguage.toString();
                            }
                        }
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
        return (site != null && site.isAllowsUnlistedLanguages()) || siteLanguages == null || siteLanguages.contains(locale) && ensureHomePageExists(site, locale);
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
