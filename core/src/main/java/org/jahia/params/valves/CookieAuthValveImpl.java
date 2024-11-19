/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.params.valves;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author not attributable
 * @version 1.0
 */

public class CookieAuthValveImpl extends BaseAuthValve {

    private CookieAuthConfig cookieAuthConfig;
    private static final Logger logger = LoggerFactory.getLogger(CookieAuthValveImpl.class);

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        if (!isEnabled() || SettingsBean.getInstance().isFullReadOnlyMode()) {
            valveContext.invokeNext(context);
            return;
        }

        logger.debug("Invoking cookie auth...");
        AuthValveContext authContext = (AuthValveContext) context;
        JCRUserNode jahiaUser = null;

        // now lets look for a cookie in case we are using cookie-based
        // authentication.
        Cookie[] cookies = cookieAuthConfig.isActivated() ? authContext.getRequest().getCookies() : null;
        if (!cookieAuthConfig.isActivated()) {
            logger.debug("Cookie auth not activated");
        }

        if (cookies == null) {
            logger.debug("No cookies found");
            // no cookies at all sent by the client, let's go to the next
            // valve.
            valveContext.invokeNext(context);
            return;
        }

        // we first need to find the authentication cookie in the list.
        Cookie authCookie = null;
        for (Cookie curCookie : cookies) {
            if (cookieAuthConfig.getCookieName().equals(curCookie.getName())) {
                // found it.
                authCookie = curCookie;
                break;
            }
        }
        if (authCookie != null) {
            // now we need to look in the database to see if we have a
            // user that has the corresponding key.
            Properties searchCriterias = new Properties();
            String userPropertyName = cookieAuthConfig.getUserPropertyName();
            String value = authCookie.getValue();
            String realm = null;
            if (value.contains(":")) {
                realm = StringUtils.substringAfter(value, ":");
                value = StringUtils.substringBefore(value, ":");
            }
            if (value.equals("deleted")) {
                logger.debug("Deleted cookie");
                valveContext.invokeNext(context);
                return;
            }
            searchCriterias.setProperty(userPropertyName, value);
            Set<JCRUserNode> foundUsers = null;
            try {
                logger.debug("Auth cookie found. Searching users...");
                foundUsers = ServicesRegistry.getInstance().getJahiaUserManagerService().searchUsers(searchCriterias, realm, null, JCRSessionFactory.getInstance().getCurrentSystemSession("live", null, null));
                if (foundUsers.size() == 1) {
                    jahiaUser = foundUsers.iterator().next();
                    if (jahiaUser.isAccountLocked()) {
                        logger.debug("Account locked");
                        jahiaUser = null;
                    } else {
                        HttpSession session = authContext.getRequest().getSession(false);
                        if (session != null) {
                            logger.debug("Attaching user to session");
                            session.setAttribute(Constants.SESSION_USER, jahiaUser.getJahiaUser());
                        }

                        if (cookieAuthConfig.isRenewalActivated()) {
                            logger.debug("Renewal activated. Recreating cookie..");
                            sendCookie(value, authContext, jahiaUser, cookieAuthConfig);
                        }
                    }
                } else {
                    logger.debug("Creating deleted cookie...");
                    authCookie = new Cookie(cookieAuthConfig.getCookieName(), "deleted");
                    authCookie.setPath(StringUtils.isNotEmpty(authContext.getRequest().getContextPath()) ?
                            authContext.getRequest().getContextPath() : "/");
                    authCookie.setMaxAge(0);
                    authCookie.setHttpOnly(cookieAuthConfig.isHttpOnly());
                    authCookie.setSecure(cookieAuthConfig.isSecure());
                    authContext.getResponse().addCookie(authCookie);
                }
            } catch (RepositoryException e) {
                logger.error("Error while searching for users", e);
            }
        } else {
            logger.debug("Cookie auth not found");
        }

        if (jahiaUser == null) {
            logger.debug("No user found");
            valveContext.invokeNext(context);
        } else {
            logger.debug("User found");
            JahiaUser user = jahiaUser.getJahiaUser();
            if (authContext.getRequest().getSession(false) != null) {
                logger.debug("Invalidated session");
                authContext.getRequest().getSession().invalidate();
            }
            authContext.getSessionFactory().setCurrentUser(user);
        }
        logger.debug("Cookie auth invoked");
    }

    public static void createAndSendCookie(AuthValveContext authContext, JCRUserNode theUser, CookieAuthConfig cookieAuthConfig) {
        // now let's look for a free random cookie value key.
        String cookieUserKey = CookieAuthValveImpl.getAvailableCookieKey(cookieAuthConfig);
        // let's save the identifier for the user in the database
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, session -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Saving cookie auth for user: {}...", theUser.getPath());
                }
                JCRUserNode innerUserNode = (JCRUserNode) session.getNode(theUser.getPath());
                innerUserNode.setProperty(cookieAuthConfig.getUserPropertyName(), cookieUserKey);
                session.save();
                return null;
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        sendCookie(cookieUserKey, authContext, theUser, cookieAuthConfig);
    }

    protected static void sendCookie(String cookieUserKey, AuthValveContext authContext, JCRUserNode theUser, CookieAuthConfig cookieAuthConfig) {
        // now let's save the same identifier in the cookie.
        String realm = theUser.getRealm();
        if (realm != null && logger.isDebugEnabled()) {
            logger.debug("Found realm: {}", realm);
        }
        Cookie authCookie = new Cookie(cookieAuthConfig.getCookieName(), cookieUserKey + (realm != null ? (":"+realm) : ""));
        authCookie.setPath(StringUtils.isNotEmpty(authContext.getRequest().getContextPath()) ?
                authContext.getRequest().getContextPath() : "/");
        authCookie.setMaxAge(cookieAuthConfig.getMaxAgeInSeconds());
        authCookie.setHttpOnly(cookieAuthConfig.isHttpOnly());
        authCookie.setSecure(cookieAuthConfig.isSecure());
        authContext.getResponse().addCookie(authCookie);
    }

    public static String getAvailableCookieKey(CookieAuthConfig cookieAuthConfig) {
        return UUID.randomUUID().toString();
    }

    public void setCookieAuthConfig(CookieAuthConfig config) {
        this.cookieAuthConfig = config;
    }

    /**
     * @deprecated the mechanism to generate a random string is now using the {@link UUID} class, so this method is of no use any more
     */
    @Deprecated
    public static String generateRandomString(int length) {
        SecureRandom randomGen = new SecureRandom();
        StringBuilder result = new StringBuilder();
        int count = 0;
        while (count < length) {
            int randomSel = randomGen.nextInt(3);
            int randomInt = randomGen.nextInt(26);
            char randomChar = '0';
            switch (randomSel) {
                case 0:
                    randomChar = (char) (((int) 'A') + randomInt);
                    break;
                case 1:
                    randomChar = (char) (((int) 'a') + randomInt);
                    break;
                case 2:
                    randomChar = (char) (((int) '0') + (randomInt % 10));
                    break;
            }
            result.append(randomChar);
            count++;
        }
        return result.toString();
    }

    @Override
    public void initialize() {
        super.initialize();
        setEnabled(cookieAuthConfig.isActivated());
    }
}
