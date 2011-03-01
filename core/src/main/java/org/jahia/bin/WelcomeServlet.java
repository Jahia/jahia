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
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
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
    
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(ProcessingContext.class);
    
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
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/cms/render/live/en/users/" + user.getUsername() + ".html"));
        } else {
            throw new AccessDeniedException();
        }
    }

    protected void defaultRedirect(HttpServletRequest request, HttpServletResponse response, ServletContext context) throws Exception {
            request.getSession(true);
            JahiaSite site = resolveSite(request);
            String redirect = null;
            if (site == null) {
                redirect = request.getContextPath() + "/administration";
            } else {
                String language = resolveLanguage(request, site);
                String base;

                final String jcrPath = "/sites/" + site.getSiteKey() + "/home";

                String pathInfo = request.getPathInfo();
                if (pathInfo != null && "/edit".equals(pathInfo)) {
                	// edit mode was requested
					base = request.getContextPath() + Edit.getEditServletPath() + "/"
					        + Constants.EDIT_WORKSPACE + "/" + language;
                } else {
	                try {
	                    JCRStoreService.getInstance().getSessionFactory()
	                            .getCurrentUserSession(Constants.LIVE_WORKSPACE).getNode(jcrPath);
	                    base = request.getContextPath() + Render.getRenderServletPath() + "/"
	                            + Constants.LIVE_WORKSPACE + "/" + language;
	                } catch (PathNotFoundException e) {
	                    try {
	                        JCRStoreService.getInstance().getSessionFactory().getCurrentUserSession()
	                                .getNode(jcrPath);
	                        base = request.getContextPath() + Edit.getEditServletPath() + "/"
	                                + Constants.EDIT_WORKSPACE + "/" + language;
	                    } catch (PathNotFoundException e2) {
	                        JCRTemplate.getInstance().doExecuteWithSystemSession(
	                                new JCRCallback<Object>() {
	                                    public Object doInJCR(JCRSessionWrapper session)
	                                            throws RepositoryException {
	                                        session.getNode(jcrPath);
	                                        throw new AccessDeniedException();
	                                    }
	                                });
	                        throw new AccessDeniedException();
	                    }
	                }
                }

                redirect = base + jcrPath + ".html";
            }

            response.sendRedirect(response.encodeRedirectURL(redirect));
    }

    protected JahiaSite resolveSite(HttpServletRequest request) throws JahiaException {
        JahiaSitesService siteService = JahiaSitesBaseService.getInstance();
        JahiaSite resolvedSite = !URLGenerator.isLocalhost(request.getServerName()) ? siteService.getSiteByServerName(request.getServerName()) : null;
        if (resolvedSite == null) {
            resolvedSite = siteService.getDefaultSite();
        }
        return resolvedSite;
    }
    
    protected String resolveLanguage(HttpServletRequest request, JahiaSite site)
            throws JahiaException {
        List<Locale> newLocaleList = new ArrayList<Locale>();
        List<Locale> siteLanguages = Collections.emptyList();
        try {
            if (site != null) {
                siteLanguages = site.getLanguagesAsLocales();
            }
        } catch (Exception t) {
            logger.debug("Exception while getting language settings as locales", t);
        }

        // retrieve the browser locales
        for (@SuppressWarnings("unchecked")
        Iterator<Locale> browserLocales = new EnumerationIterator(request.getLocales()); browserLocales
                .hasNext();) {
            final Locale curLocale = browserLocales.next();
            if (siteLanguages.contains(curLocale)) {
                if (!newLocaleList.contains(curLocale)) {
                    newLocaleList.add(curLocale);
                }
            } else if (!StringUtils.isEmpty(curLocale.getCountry())) {
                final Locale langOnlyLocale = new Locale(curLocale.getLanguage());
                if (siteLanguages.contains(langOnlyLocale)) {
                    if (!newLocaleList.contains(langOnlyLocale)) {
                        newLocaleList.add(langOnlyLocale);
                    }
                }
            }
        }

        String language = DEFAULT_LOCALE;
        if (!newLocaleList.isEmpty()) {
            language = newLocaleList.get(0).toString();
        } else if (!siteLanguages.isEmpty()){
            language = siteLanguages.get(0).toString();            
        } else if (!StringUtils.isEmpty(SettingsBean.getInstance().getDefaultLanguageCode())) {
            language = SettingsBean.getInstance().getDefaultLanguageCode();
        }
        return language;
    }
}