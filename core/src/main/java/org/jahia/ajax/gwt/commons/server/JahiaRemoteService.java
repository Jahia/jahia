/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.commons.server;

import com.google.gwt.user.client.rpc.RemoteService;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.decorator.JCRUserNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.ajax.gwt.client.core.SessionExpirationException;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.Messages;
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

    private static final transient Logger logger = LoggerFactory.getLogger(JahiaRemoteService.class);

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
            throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.cannot.open.user.session", getUILocale()));
        }
    }

    /**
     * Get current locale
     *
     * @return
     */
    protected Locale getLocale() {
        return LanguageCodeConverters.languageCodeToLocale(request.getParameter("lang"));
    }

    private Locale getFallbackLocale() throws GWTJahiaServiceException {
        Locale fallback = null;

        try {
            final String siteParam = request.getParameter("site");
            if (siteParam == null) {
                return null;
            }
            JCRSiteNode site = (JCRSiteNode) JCRSessionFactory.getInstance().getCurrentUserSession(getWorkspace()).getNodeByUUID(siteParam);

            if (site.isMixLanguagesActive()) {
                fallback = LanguageCodeConverters.getLocaleFromCode(site.getDefaultLanguage());
            }
            return fallback;
        } catch (ItemNotFoundException e) {
            return null;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.cannot.open.user.session", getUILocale()));
        }
    }

    /**
     * Get site
     *
     * @return
     */
    protected JCRSiteNode getSite() {
        try {
            final String site = request.getParameter("site");
            if (!StringUtils.isEmpty(site)) {
                return (JCRSiteNode) retrieveCurrentSession().getNodeByUUID(site);
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
        final String workspace = request.getParameter("workspace");
        if (!StringUtils.isEmpty(workspace)) {
            return workspace;
        }
        return "default";
    }

    /**
     * Get current UI locale
     *
     * @return
     */
    protected Locale getUILocale() throws GWTJahiaServiceException {
        Locale sessionLocale = (Locale) getSession().getAttribute(Constants.SESSION_UI_LOCALE);
        final JCRUserNode remoteJahiaUserNode = getRemoteJahiaUserNode();
        Locale locale = sessionLocale != null ? UserPreferencesHelper.getPreferredLocale(remoteJahiaUserNode, sessionLocale) : UserPreferencesHelper.getPreferredLocale(remoteJahiaUserNode, LanguageCodeConverters.resolveLocaleForGuest(request));
        if (locale == null) {
            if(!JahiaUserManagerService.isGuest(getRemoteJahiaUser())) {
                locale = UserPreferencesHelper.getPreferredLocale(remoteJahiaUserNode);
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

    protected JCRUserNode getRemoteJahiaUserNode() {
        JahiaUser jUser = getRemoteJahiaUser();
        if (jUser == null) {
            return null;
        }
        return JahiaUserManagerService.getInstance().lookupUserByPath(jUser.getLocalPath());
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
