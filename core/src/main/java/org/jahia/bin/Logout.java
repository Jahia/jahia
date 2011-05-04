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

package org.jahia.bin;

import java.util.*;

import javax.jcr.Node;
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
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Logout controller.
 * User: toto
 * Date: Nov 17, 2009
 * Time: 1:47:45 PM
 */
public class Logout implements Controller {
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(Logout.class);
    private static final String DEFAULT_LOCALE = Locale.ENGLISH.toString();
    private CookieAuthConfig cookieAuthConfig;
    private URLResolverFactory urlResolverFactory;

    public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
        this.cookieAuthConfig = cookieAuthConfig;
    }

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
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

        request.getSession().setAttribute(ProcessingContext.SESSION_UI_LOCALE, uiLocale);
        request.getSession().setAttribute(ProcessingContext.SESSION_LOCALE, locale);

        String redirectActiveStr = request.getParameter("redirectActive");
        boolean redirectActive = true;
        if (redirectActiveStr != null) {
            redirectActive = Boolean.parseBoolean(redirectActiveStr);
        }
        if (redirectActive) {
            String redirect = request.getParameter("redirect");
            if (redirect == null) {
                redirect = request.getHeader("referer");
                if (redirect == null) {
                    redirect = "";
                }
            }
            String url = request.getServerName().startsWith(request.getContextPath()) ? redirect.substring(
                    redirect.indexOf(request.getContextPath())) : redirect;
            // Remove servlet Dispatcher (hardcoded "/cms")
            url = url.substring(url.indexOf(request.getContextPath()) + request.getContextPath().length());
            url = url.startsWith("/cms") ? url.substring(url.indexOf("/cms") + 4) : url;
            url = StringUtils.substringBeforeLast(url,"#");
            URLResolver r = urlResolverFactory.createURLResolver(url, request.getServerName(), request);
            boolean redirectToStart = false;
            if (r.getPath().startsWith("/sites/")) {
                try {
                    RenderContext context = new RenderContext(request, response,
                            ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(
                                    JahiaUserManagerService.GUEST_USERNAME));
                    JCRNodeWrapper n = JCRContentUtils.findDisplayableNode(r.getNode(), context);
                    redirect =
                            request.getContextPath() + Render.getRenderServletPath() + "/" + Constants.LIVE_WORKSPACE +
                            "/" + resolveLanguage(request, n.getResolveSite()) + n.getPath() + ".html";
                } catch (Exception e) {
                    redirectToStart = true;
                }
            } else if (!r.getUrlPathInfo().equals("/administration")) {
                redirectToStart = true;
            }
            if (redirectToStart) {
                redirect = request.getContextPath() + "/start";
            }
            response.sendRedirect(StringUtils.isEmpty(redirect) ? "/" : redirect);
        }
        return null;
    }

    private void removeAuthCookie(HttpServletRequest request, HttpServletResponse response) {
        // now let's destroy the cookie authentication if there was one
        // set for this user.
        JahiaUser curUser = JCRSessionFactory.getInstance().getCurrentUser();
        String cookieAuthKey = curUser.getProperty(cookieAuthConfig.getUserPropertyName());
        if (cookieAuthKey != null) {
            Cookie authCookie = new Cookie(cookieAuthConfig.getCookieName(), cookieAuthKey);
            authCookie.setPath(StringUtils.isNotEmpty(request.getContextPath()) ? request.getContextPath() : "/");
            authCookie.setMaxAge(0); // means we want it deleted now !
            response.addCookie(authCookie);
            curUser.removeProperty(cookieAuthConfig.getUserPropertyName());
        }
    }

    public static String getLogoutServletPath() {
        // TODO move this into configuration
        return "/cms/logout";
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
