/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.params.valves;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.mail.MailHelper;
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

    public CookieAuthValveImpl () {
    }

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
        SettingsBean settingsBean = org.jahia.settings.SettingsBean.getInstance();
        // we first need to find the authentication cookie in the list.
        Cookie authCookie = null;
        for (int i = 0; i < cookies.length; i++) {
            Cookie curCookie = cookies[i];
            if (settingsBean.getCookieAuthCookieName().equals(curCookie.getName())) {
                // found it.
                authCookie = curCookie;
                break;
            }
        }
        if (authCookie != null) {
            // now we need to look in the database to see if we have a
            // user that has the corresponding key.
            Properties searchCriterias = new Properties();
            searchCriterias.setProperty(settingsBean.
                                        getCookieAuthUserPropertyName(),
                                        authCookie.getValue());
            Set foundUsers = ServicesRegistry.getInstance().
                             getJahiaUserManagerService().searchUsers(processingContext.
                getSiteID(), searchCriterias);
            if (foundUsers.size() == 1) {
                jahiaUser = (JahiaUser) foundUsers.iterator().next();
                processingContext.getSessionState().setAttribute(ProcessingContext.
                    SESSION_USER, jahiaUser);

                if (settingsBean.isCookieAuthRenewalActivated()) {
                    // we can now renew the cookie.
                    String cookieUserKey = null;
                    // now let's look for a free random cookie value key.
                    while (cookieUserKey == null) {
                        cookieUserKey = JahiaString.generateRandomString(
                            settingsBean.
                            getCookieAuthIDLength());
                        searchCriterias = new Properties();
                        searchCriterias.setProperty(settingsBean.
                            getCookieAuthUserPropertyName(),
                            cookieUserKey);
                        Set usersWithKey = ServicesRegistry.getInstance().
                                           getJahiaUserManagerService().
                                           searchUsers(
                            processingContext.getSiteID(), searchCriterias);
                        if (usersWithKey.size() > 0) {
                            cookieUserKey = null;
                        }
                    }
                    // let's save the identifier for the user in the database
                    jahiaUser.setProperty(settingsBean.
                                          getCookieAuthUserPropertyName(),
                                          cookieUserKey);
                    // now let's save the same identifier in the cookie.
                    authCookie.setValue(cookieUserKey);
                    authCookie.setPath(processingContext.getContextPath());
                    authCookie.setMaxAge(settingsBean.
                                         getCookieAuthMaxAgeInSeconds());
                    HttpServletResponse realResponse = ((ParamBean)processingContext).
                        getRealResponse();
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
            if (settingsBean.isConsiderPreferredLanguageAfterLogin()) {
                Locale preferredUserLocale = MailHelper
                        .getPreferredLocale(jahiaUser, processingContext
                                .getSite());
                if (processingContext.getSite() != null) {
                    List<Locale> siteLocales;
                    try {
                        siteLocales = processingContext.getSite()
                                .getLanguageSettingsAsLocales(true);
                        if (siteLocales.contains(preferredUserLocale)) {
                            processingContext.getSessionState().setAttribute(
                                    ProcessingContext.SESSION_LOCALE,
                                    preferredUserLocale);
                            processingContext
                                    .setCurrentLocale(preferredUserLocale);
                            processingContext.setLocaleList(null);
                        }
                    } catch (JahiaException e) {
                        logger.warn(
                                "Unable to switch to the user's preferred language. Cause: "
                                        + e.getMessage(), e);
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
            for (Iterator iterator = policyMsgs.getMessages().iterator(); iterator
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
}
