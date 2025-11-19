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
package org.jahia.params.valves;

import org.apache.commons.lang.StringUtils;
import org.jahia.osgi.BundleUtils;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.security.*;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        JCRUserNode jcrUserNode;

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
            Set<JCRUserNode> foundUsers;
            try {
                logger.debug("Auth cookie found. Searching users...");
                foundUsers = ServicesRegistry.getInstance().getJahiaUserManagerService().searchUsers(searchCriterias, realm, null,
                        JCRSessionFactory.getInstance().getCurrentSystemSession("live", null, null));
                if (foundUsers.size() == 1) {

                    jcrUserNode = foundUsers.iterator().next();
                    AuthenticationService authenticationService = BundleUtils.getOsgiService(AuthenticationService.class, null);

                    try {
                        // first validate the user node
                        authenticationService.validateUserNode(jcrUserNode.getUserKey());

                        // then authenticate the user
                        logger.debug("User found");
                        boolean rememberMe = false;
                        if (cookieAuthConfig.isRenewalActivated()) {
                            logger.debug("Renewal activated. Recreating cookie...");
                            rememberMe = true;
                        }
                        AuthenticationOptions authOptions = AuthenticationOptions.Builder.withDefaults().shouldRememberMe(rememberMe)
                                .build();
                        authenticationService.authenticate(jcrUserNode.getUserKey(), authOptions, authContext.getRequest(),
                                authContext.getResponse());
                        return;
                    } catch (AccountLockedException e) {
                        logger.debug("Account locked");
                    } catch (ConcurrentLoggedInUsersLimitExceededLoginException e) {
                        logger.debug("Login failed. Maximum number of logged in users reached for {}", jcrUserNode.getName());
                    } catch (InvalidSessionLoginException e) {
                        logger.debug("Login failed. Session expired for user {}", jcrUserNode.getName());
                    } catch (AccountNotFoundException e) {
                        logger.debug("Account not found");
                    }
                } else {
                    logger.debug("Creating deleted cookie...");
                    authCookie = new Cookie(cookieAuthConfig.getCookieName(), "deleted");
                    authCookie.setPath(StringUtils.isNotEmpty(authContext.getRequest().getContextPath()) ?
                            authContext.getRequest().getContextPath() :
                            "/");
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

        // at this point, the user is not authenticated. We continue to the next valve
        valveContext.invokeNext(context);

        logger.debug("Cookie auth invoked");
    }

    /**
     * @see CookieUtils#createRememberMeCookieForUser(org.jahia.services.usermanager.JahiaUser, HttpServletRequest, HttpServletResponse)
     * @deprecated use {@link CookieUtils#createRememberMeCookieForUser(org.jahia.services.usermanager.JahiaUser, HttpServletRequest, HttpServletResponse)} instead.
     */
    @Deprecated(since = "8.2.3.0", forRemoval = true)
    public static void createAndSendCookie(AuthValveContext authContext, JCRUserNode theUser, CookieAuthConfig cookieAuthConfig) {
        CookieUtils.createRememberMeCookieForUser(theUser.getJahiaUser(), authContext.getRequest(), authContext.getResponse());
    }

    /**
     * @deprecated no longer used, use {@link UUID#randomUUID()} directly instead if a unique cookie key is needed.
     */
    @Deprecated(since = "8.2.3.0", forRemoval = true)
    public static String getAvailableCookieKey(CookieAuthConfig cookieAuthConfig) {
        return UUID.randomUUID().toString();
    }

    public void setCookieAuthConfig(CookieAuthConfig config) {
        this.cookieAuthConfig = config;
    }

    /**
     * @deprecated the mechanism to generate a random string is now using the {@link UUID} class, so this method is of no use anymore
     */
    @Deprecated(since = "7.2.2.0")
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
