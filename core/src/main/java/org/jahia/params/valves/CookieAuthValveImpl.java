/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 package org.jahia.params.valves;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.usermanager.JahiaDBUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.JahiaString;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.engines.mysettings.MySettingsEngine;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author not attributable
 * @version 1.0
 */

public class CookieAuthValveImpl implements Valve {
    private static final transient Logger logger = Logger
            .getLogger(CookieAuthValveImpl.class);
    
    private CookieAuthConfig cookieAuthConfig;
    
    public void invoke (Object context, ValveContext valveContext)
        throws PipelineException {
        ProcessingContext processingContext = (ProcessingContext) context;
        JahiaUser jahiaUser = null;
        // now lets look for a cookie in case we are using cookie-based
        // authentication.
        Cookie[] cookies = ((ParamBean)processingContext).getRequest().getCookies();
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
            searchCriterias.setProperty(userPropertyName,
                                        authCookie.getValue());
            Set<Principal> foundUsers = ServicesRegistry.getInstance().
                             getJahiaUserManagerService().searchUsers(processingContext.
                getSiteID(), searchCriterias);
            if (foundUsers.size() == 1) {
                jahiaUser = (JahiaUser) foundUsers.iterator().next();
                processingContext.getSessionState().setAttribute(ProcessingContext.
                    SESSION_USER, jahiaUser);

                if (cookieAuthConfig.isRenewalActivated()) {
                    // we can now renew the cookie.
                    String cookieUserKey = null;
                    // now let's look for a free random cookie value key.
                    while (cookieUserKey == null) {
                        cookieUserKey = JahiaString.generateRandomString(cookieAuthConfig.getIdLength());
                        searchCriterias = new Properties();
                        searchCriterias.setProperty(userPropertyName, cookieUserKey);
                        Set<Principal> usersWithKey = ServicesRegistry.getInstance().
                                           getJahiaUserManagerService().
                                           searchUsers(
                            processingContext.getSiteID(), searchCriterias);
                        if (usersWithKey.size() > 0) {
                            cookieUserKey = null;
                        }
                    }
                    // let's save the identifier for the user in the database
                    jahiaUser.setProperty(userPropertyName,
                                          cookieUserKey);
                    // now let's save the same identifier in the cookie.
                    authCookie.setValue(cookieUserKey);
                    authCookie.setPath(StringUtils.isNotEmpty(processingContext.getContextPath()) ? processingContext.getContextPath() : "/");
                    authCookie.setMaxAge(cookieAuthConfig.getMaxAgeInSeconds());
                    HttpServletResponse realResponse = ((ParamBean)processingContext).getRealResponse();
                    realResponse.addCookie(authCookie);
                }
            }
        }
        if (jahiaUser == null) {
            valveContext.invokeNext(context);
        } else {
            try {
                if (processingContext instanceof ParamBean) {
                    ParamBean paramBean = (ParamBean) processingContext;
                    paramBean.invalidateSession();
                }
            } catch (JahiaSessionExpirationException e) {
                logger.error(e.getMessage(), e);
            }
            processingContext.setTheUser(jahiaUser);

            // do a switch to the user's preferred language
            if (SettingsBean.getInstance().isConsiderPreferredLanguageAfterLogin()) {
                Locale preferredUserLocale = UserPreferencesHelper
                        .getPreferredLocale(jahiaUser, processingContext
                                .getSite());
                if (processingContext.getSite() != null) {
                    List<Locale> siteLocales;
                    siteLocales = processingContext.getSite().getLanguagesAsLocales();
                    if (siteLocales.contains(preferredUserLocale)) {
                        processingContext.getSessionState().setAttribute(ProcessingContext.SESSION_LOCALE,
                                                                         preferredUserLocale);
                        processingContext.setCurrentLocale(preferredUserLocale);
                        processingContext.setLocaleList(null);
                    }

                }
            }
            
            enforcePasswordPolicy(jahiaUser, (ParamBean)processingContext);
            jahiaUser.setProperty(JahiaDBUser.PROP_LAST_LOGIN_DATE, String
                    .valueOf(System.currentTimeMillis()));
        }
    }

    public void initialize () {
    }

    private void enforcePasswordPolicy(JahiaUser theUser, ParamBean paramBean) {
        PolicyEnforcementResult evalResult = ServicesRegistry.getInstance()
                .getJahiaPasswordPolicyService().enforcePolicyOnLogin(theUser);
        if (!evalResult.isSuccess()) {
            EngineMessages policyMsgs = evalResult.getEngineMessages();
            EngineMessages resultMessages = new EngineMessages();
            for (Iterator<EngineMessage> iterator = policyMsgs.getMessages().iterator(); iterator
                    .hasNext();) {
                resultMessages.add((EngineMessage) iterator.next());
            }
            if (paramBean != null) {
                paramBean.getRequest().getSession().setAttribute(
                        EngineMessages.CONTEXT_KEY, resultMessages);
                try {
                    String redirectUrl = null;
                    if (paramBean.getPageID() != -1) {
                        redirectUrl = paramBean
                                .composeEngineUrl(MySettingsEngine.ENGINE_NAME)
                                + "?screen=" + MySettingsEngine.EDIT_TOKEN;
                    } else {
                        redirectUrl = new StringBuffer(64).append(
                                paramBean.getRequest().getContextPath())
                                .append(Jahia.getServletPath()).append(
                                        "/engineName/"
                                                + MySettingsEngine.ENGINE_NAME)
                                .append("/pid/").append(
                                        paramBean.getSite().getHomePageID())
                                .append(
                                        "?screen="
                                                + MySettingsEngine.EDIT_TOKEN)
                                .toString();
                    }
                    paramBean.getResponse().sendRedirect(redirectUrl);
                } catch (Exception ex) {
                    logger.error(
                            "Unable to forward to the mysettings engine page",
                            ex);
                }
            }
        }
    }

	public void setCookieAuthConfig(CookieAuthConfig config) {
    	this.cookieAuthConfig = config;
    }
}
