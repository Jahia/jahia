/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.params.valves;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;
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

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        if (!isEnabled()) {
            valveContext.invokeNext(context);
            return;
        }
        
        AuthValveContext authContext = (AuthValveContext) context;
        JahiaUser jahiaUser = null;
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
        for (int i = 0; i < cookies.length; i++) {
            Cookie curCookie = cookies[i];
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
            searchCriterias.setProperty(userPropertyName, authCookie.getValue());
            Set<Principal> foundUsers = ServicesRegistry.getInstance().
                    getJahiaUserManagerService().searchUsers(searchCriterias);
            if (foundUsers.size() == 1) {
                jahiaUser = (JahiaUser) foundUsers.iterator().next();
                if (jahiaUser.isAccountLocked()) {
                    jahiaUser = null;
                } else {
                    HttpSession session = authContext.getRequest().getSession(false);
                    if (session !=null) {
                        session.setAttribute(Constants.SESSION_USER, jahiaUser);
                    }
    
                    if (cookieAuthConfig.isRenewalActivated()) {
                        // we can now renew the cookie.
                        String cookieUserKey = null;
                        // now let's look for a free random cookie value key.
                        while (cookieUserKey == null) {
                            cookieUserKey = CookieAuthValveImpl.generateRandomString(cookieAuthConfig.getIdLength());
                            searchCriterias = new Properties();
                            searchCriterias.setProperty(userPropertyName, cookieUserKey);
                            Set<Principal> usersWithKey = ServicesRegistry.getInstance().
                                    getJahiaUserManagerService().
                                    searchUsers(searchCriterias);
                            if (usersWithKey.size() > 0) {
                                cookieUserKey = null;
                            }
                        }
                        // let's save the identifier for the user in the database
                        jahiaUser.setProperty(userPropertyName, cookieUserKey);
                        // now let's save the same identifier in the cookie.
                        authCookie.setValue(cookieUserKey);
                        authCookie.setPath(StringUtils.isNotEmpty(authContext.getRequest().getContextPath()) ?
                                authContext.getRequest().getContextPath() : "/");
                        authCookie.setMaxAge(cookieAuthConfig.getMaxAgeInSeconds());
                        authCookie.setHttpOnly(cookieAuthConfig.isHttpOnly());
                        authCookie.setSecure(cookieAuthConfig.isSecure());
                        HttpServletResponse realResponse = authContext.getResponse();
                        realResponse.addCookie(authCookie);
                    }
                }
            }
        }
        if (jahiaUser == null) {
            valveContext.invokeNext(context);
        } else {
            if (authContext.getRequest().getSession(false) != null) {
                authContext.getRequest().getSession().invalidate();
            }
            authContext.getSessionFactory().setCurrentUser(jahiaUser);

            // do a switch to the user's preferred language
//            if (SettingsBean.getInstance().isConsiderPreferredLanguageAfterLogin()) {
//                Locale preferredUserLocale = UserPreferencesHelper.getPreferredLocale(jahiaUser);
//            }

            enforcePasswordPolicy(jahiaUser, authContext);
            jahiaUser.setProperty(Constants.JCR_LASTLOGINDATE,
                    String.valueOf(System.currentTimeMillis()));
        }
    }

    private void enforcePasswordPolicy(JahiaUser theUser, AuthValveContext authContext) {
//        PolicyEnforcementResult evalResult =
//                ServicesRegistry.getInstance().getJahiaPasswordPolicyService().enforcePolicyOnLogin(theUser);
//        if (!evalResult.isSuccess()) {
//            EngineMessages policyMsgs = evalResult.getEngineMessages();
//            EngineMessages resultMessages = new EngineMessages();
//            for (Iterator<EngineMessage> iterator = policyMsgs.getMessages().iterator(); iterator.hasNext();) {
//                resultMessages.add((EngineMessage) iterator.next());
//            }
//            if (authContext != null) {
//                authContext.getRequest().getSession().setAttribute(EngineMessages.CONTEXT_KEY, resultMessages);
//                try {
//                    String redirectUrl = null;
//                    redirectUrl = new StringBuffer(64).append(authContext.getRequest().getContextPath())
//                            .append(Render.getRenderServletPath()).append("/default/en/users").append(theUser.getName())
//                            .append(".html").toString();
//                    authContext.getResponse().sendRedirect(redirectUrl);
//                } catch (Exception ex) {
//                    logger.error("Unable to forward to the mysettings engine page", ex);
//                }
//            }
//        }

    }

    public void setCookieAuthConfig(CookieAuthConfig config) {
        this.cookieAuthConfig = config;
    }

    public static String generateRandomString(int length) {
        SecureRandom randomGen = new SecureRandom();
        StringBuffer result = new StringBuffer();
        int count = 0;
        while (count < length) {
            int randomSel = randomGen.nextInt(3);
            int randomInt = randomGen.nextInt(26);
            char randomChar = '0';
            switch (randomSel) {
                case 0: randomChar = (char) (((int)'A') + randomInt); break;
                case 1: randomChar = (char) (((int)'a') + randomInt); break;
                case 2: randomChar = (char) (((int)'0') + (randomInt % 10)); break;
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
