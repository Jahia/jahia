/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.bin;

import java.io.IOException;
import java.util.*;

import javax.jcr.RepositoryException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.valves.CookieAuthConfig;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.*;
import org.jahia.services.seo.urlrewrite.UrlRewriteService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Logout controller.
 * User: toto
 * Date: Nov 17, 2009
 * Time: 1:47:45 PM
 */
public class Logout implements Controller {
    private static final String DEFAULT_LOCALE = Locale.ENGLISH.toString();
    private static final transient Logger logger = LoggerFactory.getLogger(Logout.class);
    
    public static String getLogoutServletPath() {
        // TODO move this into configuration
        return "/cms/logout";
    }
    
    protected CookieAuthConfig cookieAuthConfig;
    
    protected URLResolverFactory urlResolverFactory;

    protected UrlRewriteService urlRewriteService;

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
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected void doRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String redirect = request.getParameter("redirect");
        if (redirect == null) {
            redirect = request.getHeader("referer");
            if (StringUtils.isNotEmpty(redirect) && (redirect.startsWith("http://") || redirect.startsWith("https://") && redirect.length() > 8 )) {
                redirect = redirect.startsWith("http://") ? StringUtils.substringAfter(redirect, "http://") : StringUtils.substringAfter(redirect, "https://");
                redirect = redirect.contains("/") ? "/" + StringUtils.substringAfter(redirect, "/") : null; 
            } else {
                redirect = null;
            }
        }
        if (StringUtils.isNotEmpty(redirect)) {
            String prefix = request.getContextPath() + "/cms/";
            if (redirect.startsWith(prefix)) {
                if (!urlRewriteService.isSeoRulesEnabled()) {
                    String url = "/" + StringUtils.substringAfter(redirect, prefix);
                    String hash = StringUtils.substringAfterLast(url, "#");
                    url = StringUtils.substringBefore(url, ";jsessionid");
                    url = StringUtils.substringBefore(url, "?");
                    url = StringUtils.substringBefore(url, "#");
                    if (hash != null && hash.startsWith("/sites/") && url.contains("/sites/")) {
                        url = StringUtils.substringBefore(url, "/sites/") + StringUtils.substringBefore(hash, ":") + ".html";
                    }

                    List<String> urls = new ArrayList<String>();
                    urls.add(url);
                    if (url.startsWith("/edit/")) {
                        url = "/render/" + StringUtils.substringAfter(url,"/edit/");
                        urls.add(url);
                    }
                    if (url.startsWith("/contribute/")) {
                        url = "/render/" + StringUtils.substringAfter(url,"/contribute/");
                        urls.add(url);
                    }
                    if (url.startsWith("/render/default/")) {
                        url = "/render/live/" + StringUtils.substringAfter(url,"/render/default/");
                        urls.add(url);
                    }
                    for (String currentUrl : urls) {
                        try {
                            URLResolver r = urlResolverFactory.createURLResolver(currentUrl, request.getServerName(), request);
                            if (r.getPath().startsWith("/sites/")) {
                                    JCRNodeWrapper n = r.getNode();
                                    redirect = prefix + r.getServletPart() + "/" + r.getWorkspace() + "/"
                                            + resolveLanguage(request, n.getResolveSite()) + n.getPath() + ".html";
                            } else {
                                redirect = request.getContextPath() + "/";
                            }
                            response.sendRedirect(response.encodeRedirectURL(redirect));
                            return;
                        } catch (Exception e) {
                        }
                    }
                    response.sendRedirect(response.encodeRedirectURL(prefix + StringUtils.substringAfter(urls.get(0), "/")));
                    return;
                } else {
                    // TODO handle the case when SEO rules are enabled
                    redirect = null;
                }
            }
        }

        response.sendRedirect(response.encodeRedirectURL(redirect));
    }

    /**
     * Process the request and return a ModelAndView object which the DispatcherServlet
     * will render. A <code>null</code> return value is not an error: It indicates that
     * this object completed request processing itself, thus there is no ModelAndView
     * to render.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render, or <code>null</code> if handled directly
     * @throws Exception in case of errors
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (cookieAuthConfig.isActivated()) {
            removeAuthCookie(request, response);
        }
        Locale uiLocale = (Locale) request.getSession().getAttribute(ProcessingContext.SESSION_UI_LOCALE);
        Locale locale = (Locale) request.getSession().getAttribute(ProcessingContext.SESSION_LOCALE);

        request.getSession().invalidate();

        JCRSessionFactory.getInstance().closeAllSessions();
        JCRSessionFactory.getInstance()
                .setCurrentUser(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(JahiaUserManagerService.GUEST_USERNAME));

        request.getSession().setAttribute(ProcessingContext.SESSION_UI_LOCALE, uiLocale);
        request.getSession().setAttribute(ProcessingContext.SESSION_LOCALE, locale);

        String redirectActiveStr = request.getParameter("redirectActive");
        if (redirectActiveStr == null || Boolean.parseBoolean(redirectActiveStr)) {
            doRedirect(request, response);
        }
        
        return null;
    }

    protected void removeAuthCookie(HttpServletRequest request, HttpServletResponse response) {
        // now let's destroy the cookie authentication if there was one
        // set for this user.
        JahiaUser curUser = JCRSessionFactory.getInstance().getCurrentUser();
        String cookieAuthKey = JahiaUserManagerService.isNotGuest(curUser) ? curUser.getProperty(cookieAuthConfig.getUserPropertyName()) : null;
        if (cookieAuthKey != null) {
            Cookie authCookie = new Cookie(cookieAuthConfig.getCookieName(), cookieAuthKey);
            authCookie.setPath(StringUtils.isNotEmpty(request.getContextPath()) ? request.getContextPath() : "/");
            authCookie.setMaxAge(0); // means we want it deleted now !
            response.addCookie(authCookie);
            curUser.removeProperty(cookieAuthConfig.getUserPropertyName());
        }
    }

    protected String resolveLanguage(HttpServletRequest request, final JCRSiteNode site)
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

        // retrieve the browser locales
        for (@SuppressWarnings("unchecked") Iterator<Locale> browserLocales = new EnumerationIterator(request.getLocales()); browserLocales
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

    public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
        this.cookieAuthConfig = cookieAuthConfig;
    }

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
    }

    public void setUrlRewriteService(UrlRewriteService urlRewriteService) {
        this.urlRewriteService = urlRewriteService;
    }
}
