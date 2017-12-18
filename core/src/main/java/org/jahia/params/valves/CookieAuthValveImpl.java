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
package org.jahia.params.valves;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Set;

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
        if (!isEnabled()) {
            valveContext.invokeNext(context);
            return;
        }

        AuthValveContext authContext = (AuthValveContext) context;
        JCRUserNode jahiaUser = null;
        // now lets look for a cookie in case we are using cookie-based
        // authentication.
        Cookie[] cookies = cookieAuthConfig.isActivated() ? authContext.getRequest().getCookies() : null;
        if (cookies == null) {
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
                valveContext.invokeNext(context);
                return;
            }
            searchCriterias.setProperty(userPropertyName, value);
            Set<JCRUserNode> foundUsers = null;
            try {
                foundUsers = ServicesRegistry.getInstance().getJahiaUserManagerService().searchUsers(searchCriterias, realm, null, JCRSessionFactory.getInstance().getCurrentSystemSession("live", null, null));
                if (foundUsers.size() == 1) {
                    jahiaUser = foundUsers.iterator().next();
                    if (jahiaUser.isAccountLocked()) {
                        jahiaUser = null;
                    } else {
                        HttpSession session = authContext.getRequest().getSession(false);
                        if (session != null) {
                            session.setAttribute(Constants.SESSION_USER, jahiaUser.getJahiaUser());
                        }

                        if (cookieAuthConfig.isRenewalActivated()) {
                            createAndSendCookie(authContext, jahiaUser, cookieAuthConfig);
                        }
                    }
                } else {
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
        }
        if (jahiaUser == null) {
            valveContext.invokeNext(context);
        } else {
            JahiaUser user = jahiaUser.getJahiaUser();
            if (authContext.getRequest().getSession(false) != null) {
                authContext.getRequest().getSession().invalidate();
            }
            authContext.getSessionFactory().setCurrentUser(user);
        }
    }

    public static void createAndSendCookie(AuthValveContext authContext, JCRUserNode theUser, CookieAuthConfig cookieAuthConfig) {
        // now let's look for a free random cookie value key.
        String cookieUserKey = CookieAuthValveImpl.getAvailableCookieKey(cookieAuthConfig);
        // let's save the identifier for the user in the database
        try {
            theUser.setProperty(cookieAuthConfig.getUserPropertyName(), cookieUserKey);
            theUser.getSession().save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        // now let's save the same identifier in the cookie.
        String realm = theUser.getRealm();
        Cookie authCookie = new Cookie(cookieAuthConfig.getCookieName(), cookieUserKey + (realm != null ? (":"+realm) : ""));
        authCookie.setPath(StringUtils.isNotEmpty(authContext.getRequest().getContextPath()) ?
                authContext.getRequest().getContextPath() : "/");
        authCookie.setMaxAge(cookieAuthConfig.getMaxAgeInSeconds());
        authCookie.setHttpOnly(cookieAuthConfig.isHttpOnly());
        authCookie.setSecure(cookieAuthConfig.isSecure());
        authContext.getResponse().addCookie(authCookie);
    }

    public static String getAvailableCookieKey(CookieAuthConfig cookieAuthConfig) {
        String cookieUserKey = null;
        while (cookieUserKey == null) {
            cookieUserKey = generateRandomString(cookieAuthConfig.getIdLength());
            Properties searchCriterias = new Properties();
            searchCriterias.setProperty(cookieAuthConfig.getUserPropertyName(), cookieUserKey);
            Set<JCRUserNode> foundUsers = ServicesRegistry.getInstance().getJahiaUserManagerService().searchUsers(searchCriterias);
            if (foundUsers.size() > 0) {
                cookieUserKey = null;
            }
        }
        return cookieUserKey;
    }

    public void setCookieAuthConfig(CookieAuthConfig config) {
        this.cookieAuthConfig = config;
    }

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
