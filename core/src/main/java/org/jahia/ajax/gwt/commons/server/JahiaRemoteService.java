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

package org.jahia.ajax.gwt.commons.server;

import com.google.gwt.user.client.rpc.RemoteService;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRContentUtils;
import org.slf4j.Logger;
import org.jahia.ajax.gwt.client.core.SessionExpirationException;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.api.Constants;
import org.jahia.params.ParamBean;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.springframework.web.context.ServletContextAware;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Locale;

/**
 * Base class for Jahia GWT services.
 *
 * @author Sergiy Shyrkov
 */
public abstract class JahiaRemoteService implements RemoteService, ServletContextAware, RequestResponseAware {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaRemoteService.class);

    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;

    /**
     * Retrieve current session
     *
     * @return
     * @throws GWTJahiaServiceException
     */
    protected JCRSessionWrapper retrieveCurrentSession() throws GWTJahiaServiceException {
        return retrieveCurrentSession(getLocale());
    }

    /**
     * Retrieve current session by locale
     *
     * @param locale
     * @return
     * @throws GWTJahiaServiceException
     */
    protected JCRSessionWrapper retrieveCurrentSession(Locale locale) throws GWTJahiaServiceException {
        return retrieveCurrentSession(getWorkspace(), locale, false);
    }

    /**
     * Retrieve current session by workspace
     *
     * @return
     * @throws GWTJahiaServiceException
     */
    protected JCRSessionWrapper retrieveCurrentSession(String workspace, Locale locale, boolean useSiteFallbackLanguage) throws GWTJahiaServiceException {
        checkSession();
        try {
            return JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale, useSiteFallbackLanguage ? getFallbackLocale() : null);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(JahiaResourceBundle.getJahiaInternalResource("label.gwt.error.cannot.open.user.session",getUILocale()));
        }
    }

    /**
     * Get current locale
     *
     * @return
     */
    protected Locale getLocale() {
        Locale locale = LanguageCodeConverters.languageCodeToLocale(request.getParameter("lang"));
        return locale;
    }

    private Locale getFallbackLocale() throws GWTJahiaServiceException {
        Locale fallback = null;

        try {
            if (request.getParameter("site") == null) {
                return null;
            }
            JCRSiteNode site = (JCRSiteNode) JCRSessionFactory.getInstance().getCurrentUserSession(getWorkspace()).getNodeByUUID(request.getParameter("site"));

            if (site.isMixLanguagesActive()) {
                fallback = LanguageCodeConverters.getLocaleFromCode(site.getDefaultLanguage());
            }
            return fallback;
        } catch (ItemNotFoundException e) {
            return null;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(JahiaResourceBundle.getJahiaInternalResource("label.gwt.error.cannot.open.user.session", getUILocale()));
        }
    }

    /**
     * Get site
     *
     * @return
     */
    protected JCRSiteNode getSite() {
        try {
            if (!StringUtils.isEmpty(request.getParameter("site"))) {
                return (JCRSiteNode) retrieveCurrentSession().getNodeByUUID(request.getParameter("site"));
            } else {
                return (JCRSiteNode) retrieveCurrentSession().getNode(JCRContentUtils.getSystemSitePath());
            }
        } catch (Exception e) {
            logger.error("Cannot get site",e);
        }
        return null;
    }

    /**
     * Get workspace
     *
     * @return
     */
    protected String getWorkspace() {
        if (!StringUtils.isEmpty(request.getParameter("workspace"))) {
            return request.getParameter("workspace");
        }
        return "default";
    }

    /**
     * Get current UI locale
     *
     * @return
     */
    protected Locale getUILocale() throws GWTJahiaServiceException {
        Locale sessionLocale = (Locale) getSession().getAttribute(ParamBean.SESSION_UI_LOCALE);
        Locale locale = sessionLocale != null ? UserPreferencesHelper.getPreferredLocale(getRemoteJahiaUser(), sessionLocale) : UserPreferencesHelper.getPreferredLocale(getRemoteJahiaUser(), LanguageCodeConverters.resolveLocaleForGuest(request));
        if (locale == null) {
            if(JahiaUserManagerService.isNotGuest(getRemoteJahiaUser())) {
                locale = UserPreferencesHelper.getPreferredLocale(getRemoteJahiaUser());
            }
            if (locale == null) {
                locale = getLocale();
            }
            request.getSession(false).setAttribute(Constants.SESSION_UI_LOCALE, locale);
        }
        return locale;
    }

    /**
     * Get remote jahiaUser
     *
     * @return
     */
    protected JahiaUser getRemoteJahiaUser() {
        return JCRSessionFactory.getInstance().getCurrentUser();
    }

    /**
     * Get remote user
     *
     * @return
     */
    protected String getRemoteUser() {
        //retrieve user
        JahiaUser jUser = getRemoteJahiaUser();
        if (jUser != null) {
            return jUser.getUserKey();
        }
        return null;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Get resources
     *
     * @param key
     * @param locale
     * @param site
     * @return
     */
    public String getResources(String key, Locale locale, JCRSiteNode site) {
        if (logger.isDebugEnabled()) {
            logger.debug("Resources key: " + key);
        }
        if (key == null || key.length() == 0) {
            return key;
        }
        String value = new JahiaResourceBundle(locale, site != null ? site.getTemplatePackageName() : null).get(key, null);
        if (value == null || value.length() == 0) {
            value = JahiaResourceBundle.getJahiaInternalResource(key, locale);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Resources value: " + value);
        }

        return value;
    }


    public HttpServletResponse getResponse() {
        return response;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    protected HttpSession getSession() throws SessionExpirationException {
        checkSession();
        return getRequest().getSession();
    }

    private void checkSession() throws SessionExpirationException {
        if (request.getSession(false) == null) {
            throw new SessionExpirationException();
        }
    }


}
