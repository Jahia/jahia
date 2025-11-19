package org.jahia.services.security;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.params.valves.CookieAuthConfig;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

public class CookieUtils {
    private static final Logger logger = LoggerFactory.getLogger(CookieUtils.class);

    /**
     * Creates and sends a "remember me" authentication cookie for the specified user.
     * Stores a new property (defined by {@link CookieAuthConfig#getUserPropertyName()}) in the user's JCR node to associate the cookie value.
     *
     * @param jahiaUser         the user for which to create the cookie and JCR property
     * @param httpServletRequest  the current HTTP servlet request
     * @param httpServletResponse the current HTTP servlet response
     */
    public static void createRememberMeCookieForUser(JahiaUser jahiaUser, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        // use a random UUID to get a random cookie value
        String cookieUserKey = UUID.randomUUID().toString();
        // let's save the identifier for the user in the database
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, session -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Saving cookie auth for user: {}...", jahiaUser.getUserKey());
                }
                JCRUserNode jcrUserNode = (JCRUserNode) session.getNode(jahiaUser.getUserKey());
                jcrUserNode.setProperty(getCookieAuthConfig().getUserPropertyName(), cookieUserKey);
                session.save();
                return null;
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        sendCookie(cookieUserKey, httpServletRequest, httpServletResponse, jahiaUser);
    }

    /**
     * Removes the "remember me" authentication cookie for the specified user.
     * Also deletes the corresponding property (defined by {@link CookieAuthConfig#getUserPropertyName()}) from the user's JCR node if
     * present.
     *
     * @param jahiaUser the user whose "remember me" cookie and JCR property should be cleared
     * @param request   the current HTTP servlet request
     * @param response  the current HTTP servlet response
     */
    public static void clearRememberMeCookieForUser(JahiaUser jahiaUser, HttpServletRequest request, HttpServletResponse response) {
        // now let's destroy the cookie authentication if there was one
        // set for this user.
        JCRPropertyWrapper cookieAuthKey = null;
        try {
            CookieAuthConfig cookieAuthConfig = getCookieAuthConfig();
            if (!JahiaUserManagerService.isGuest(jahiaUser)) {
                JCRUserNode userNode = ServicesRegistry.getInstance().getJahiaUserManagerService()
                        .lookupUserByPath(jahiaUser.getLocalPath());
                String userPropertyName = cookieAuthConfig.getUserPropertyName();
                if (userNode != null && userNode.hasProperty(userPropertyName)) {
                    cookieAuthKey = userNode.getProperty(userPropertyName);
                }
            }
            if (cookieAuthKey != null) {
                Cookie authCookie = new Cookie(cookieAuthConfig.getCookieName(), cookieAuthKey.getString());
                authCookie.setPath(StringUtils.isNotEmpty(request.getContextPath()) ? request.getContextPath() : "/");
                authCookie.setMaxAge(0); // 0 to delete the cookie
                authCookie.setHttpOnly(cookieAuthConfig.isHttpOnly());
                authCookie.setSecure(cookieAuthConfig.isSecure());
                response.addCookie(authCookie);
                if (!SettingsBean.getInstance().isFullReadOnlyMode()) {
                    cookieAuthKey.remove();
                    cookieAuthKey.getSession().save();
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void sendCookie(String cookieUserKey, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            JahiaUser jahiaUser) {
        // now let's save the same identifier in the cookie.
        String realm = jahiaUser.getRealm();
        if (realm != null && logger.isDebugEnabled()) {
            logger.debug("Found realm: {}", realm);
        }
        CookieAuthConfig cookieAuthConfig = getCookieAuthConfig();
        Cookie authCookie = new Cookie(cookieAuthConfig.getCookieName(), cookieUserKey + (realm != null ? (":" + realm) : ""));
        authCookie.setPath(org.apache.commons.lang3.StringUtils.isNotEmpty(httpServletRequest.getContextPath()) ?
                httpServletRequest.getContextPath() :
                "/");
        authCookie.setMaxAge(cookieAuthConfig.getMaxAgeInSeconds());
        authCookie.setHttpOnly(cookieAuthConfig.isHttpOnly());
        authCookie.setSecure(cookieAuthConfig.isSecure());
        httpServletResponse.addCookie(authCookie);
    }

    private static CookieAuthConfig getCookieAuthConfig() {
        return SettingsBean.getInstance().getCookieAuthConfig();
    }
}
