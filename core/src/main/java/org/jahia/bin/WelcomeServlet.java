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

package org.jahia.bin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Url;
import org.jahia.utils.WebUtils;
import org.slf4j.Logger;

/**
 * Servlet for the first entry point in Jahia portal that performs a client-side redirect
 * to the home page of the appropriate site.
 * User: toto
 * Date: Apr 26, 2010
 * Time: 5:49:14 PM
 */
public class WelcomeServlet extends HttpServlet {

    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = -2055161334153523152L;
    
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(WelcomeServlet.class);
    
    private static final String DEFAULT_LOCALE = Locale.ENGLISH.toString();

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
        JahiaUser user = (JahiaUser) request.getSession().getAttribute(ProcessingContext.SESSION_USER);
        if (!JahiaUserManagerService.isGuest(user)) {
            JCRSiteNode site = (JCRSiteNode) JCRStoreService.getInstance().getSessionFactory().getCurrentUserSession("live").getNode(JCRContentUtils.getSystemSitePath());
            String language = resolveLanguage(request, site, user);
            redirect(request.getContextPath() + "/cms/render/live/"+language + user.getLocalPath() + ".user-home.html", response);
        } else {
            throw new AccessDeniedException();
        }
    }

    protected void redirect(String url, HttpServletResponse response) throws IOException {
        String targetUrl = response.encodeRedirectURL(url);
        if (targetUrl.contains(";jsessionid")) {
            if (targetUrl.contains("?")) {
                targetUrl = StringUtils.substringBefore(targetUrl, ";jsessionid=") + "?"
                        + StringUtils.substringAfter(targetUrl, "?");
            } else {
                targetUrl = StringUtils.substringBefore(targetUrl, ";jsessionid=");
            }
        }
        WebUtils.setNoCacheHeaders(response);
        response.sendRedirect(targetUrl);
    }

    protected void defaultRedirect(HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws Exception {
        request.getSession(true);
        final JCRSiteNode site = resolveSite(request, Constants.LIVE_WORKSPACE);
        String redirect = null;
        if (site == null) {
            userRedirect(request, response, context);
            return;
        } else {
            JahiaUser user = (JahiaUser) request.getSession().getAttribute(ProcessingContext.SESSION_USER);
            String language = resolveLanguage(request, site, user);
            String base;

            String pathInfo = request.getPathInfo();
            JCRNodeWrapper home = site.getHome();
            if (pathInfo != null && "/edit".equals(pathInfo) && !SettingsBean.getInstance().isDistantPublicationServerMode()) {
                // edit mode was requested
                if(home!=null) {
                    base = request.getContextPath() + Edit.getEditServletPath() + "/"
                        + Constants.EDIT_WORKSPACE + "/" + language + home.getPath();
                } else {
                    base = request.getContextPath() + Edit.getEditServletPath() + "/"
                        + Constants.EDIT_WORKSPACE + "/" + language + resolveSite(request, Constants.EDIT_WORKSPACE).getHome().getPath();
                }
            } else {
                if (home != null) {
                    base = request.getContextPath() + Render.getRenderServletPath() + "/"
                            + Constants.LIVE_WORKSPACE + "/" + language + home.getPath();
                } else if (!SettingsBean.getInstance().isDistantPublicationServerMode()) {
                    JCRSiteNode defSite = null;
                    try {
                        defSite = (JCRSiteNode) JCRStoreService.getInstance().getSessionFactory()
                                .getCurrentUserSession().getNode(site.getPath());
                    } catch (PathNotFoundException e) {
                        redirect(request.getContextPath()+"/start", response);
                        return;
                    }
                    if (defSite.getHome() != null) {
                        base = request.getContextPath() + Edit.getEditServletPath() + "/"
                                + Constants.EDIT_WORKSPACE + "/" + language
                                + defSite.getHome().getPath();
                    } else {
                        redirect(request.getContextPath()+"/start", response);
                        return;
                    }
                } else {
                    redirect(request.getContextPath()+"/start", response);
                    return;
                }
            }

            redirect = base + ".html";
        }

        redirect(redirect, response);
    }

    protected JCRSiteNode resolveSite(HttpServletRequest request, String workspace) throws JahiaException, RepositoryException {
        JahiaSitesService siteService = JahiaSitesBaseService.getInstance();
        JahiaSite resolvedSite = !Url.isLocalhost(request.getServerName()) ? siteService.getSiteByServerName(request.getServerName()) : null;
        if (resolvedSite == null) {
            resolvedSite = siteService.getDefaultSite();
        }
        return resolvedSite != null ? (JCRSiteNode) JCRStoreService.getInstance().getSessionFactory()
                .getCurrentUserSession(workspace).getNode(resolvedSite.getJCRLocalPath()) : null;
    }
    
    protected String resolveLanguage(HttpServletRequest request, final JCRSiteNode site, JahiaUser user)
            throws JahiaException {
        final List<Locale> newLocaleList = new ArrayList<Locale>();
        List<Locale> siteLanguages = Collections.emptyList();
        try {
            if (site != null) {
                siteLanguages = site.getLanguagesAsLocales();
            }
        } catch (Exception t) {
            logger.debug("Exception while getting language settings as locales", t);
        }

        Locale preferredLocale = UserPreferencesHelper.getPreferredLocale(user);
        if (preferredLocale != null) {
            addLocale(site, newLocaleList, preferredLocale);
        }

        // retrieve the browser locales
        for (@SuppressWarnings("unchecked")
        Iterator<Locale> browserLocales = new EnumerationIterator(request.getLocales()); browserLocales
                .hasNext();) {
            final Locale curLocale = browserLocales.next();
            if (siteLanguages.contains(curLocale)) {
                addLocale(site, newLocaleList, curLocale);
            } else if (!StringUtils.isEmpty(curLocale.getCountry())) {
                final Locale langOnlyLocale = new Locale(curLocale.getLanguage());
                if (siteLanguages.contains(langOnlyLocale)) {
                    addLocale(site, newLocaleList, langOnlyLocale);
                }
            }
        }

        String language = DEFAULT_LOCALE;
        if (!newLocaleList.isEmpty()) {
            language = newLocaleList.get(0).toString();
        } else if (site!=null){
            language = site.getDefaultLanguage();
        } else if (!StringUtils.isEmpty(SettingsBean.getInstance().getDefaultLanguageCode())) {
            language = SettingsBean.getInstance().getDefaultLanguageCode();
        }
        return language;
    }

    private void addLocale(final JCRSiteNode site, final List<Locale> newLocaleList, final Locale curLocale) {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(null,
                    Constants.LIVE_WORKSPACE,curLocale,new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        if(site!=null) {
                            JCRSiteNode nodeByIdentifier = (JCRSiteNode) session.getNodeByIdentifier(site.getIdentifier());
                            JCRNodeWrapper home = nodeByIdentifier.getHome();
                            if (home!=null && !newLocaleList.contains(curLocale)) {
                                newLocaleList.add(curLocale);
                            }
                        }
                    } catch (RepositoryException e) {
                        logger.debug("This site does not have a published home in language "+curLocale,e);
                    }
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }
}