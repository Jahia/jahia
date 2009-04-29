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
package org.jahia.engines.logout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.http.Cookie;

import org.jahia.data.JahiaData;
import org.jahia.data.events.JahiaEvent;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.core.Core_Engine;
import org.jahia.engines.login.Login_Engine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.EnginesRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageBaseService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;


/**
 * @author Eric Vassalli
 * @author Khue Nguyen
 * @author Fulco Houkes
 *         todo add some nice javadoc comment here
 *         todo (Fulco) : the toolBox attribute is initializedin the constructor, but never used.
 */
public class Logout_Engine implements JahiaEngine {

    /**
     * Engine's name.
     */
    public static final String ENGINE_NAME = "logout";

    /**
     * logging
     */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(Logout_Engine.class);


    /**
     * Default constructor, creates a new <code>Logout_Engin</code> instance.
     */
    public Logout_Engine() {
    }

    /**
     * Return true if the engine is authorise to render the logout.
     *
     * @param jParams the parameter bean
     * @return <code>true</code> when the render is authorised, otherwise <code>false</code>.
     */
    public boolean authoriseRender(ProcessingContext jParams) {
        return true;                    // always allowed to render logout!
    }


    /**
     * Renders link to pop-up window
     *
     * @param jParams
     * @param theObj
     * @return
     * @throws JahiaException
     */
    public String renderLink(ProcessingContext jParams, Object theObj)
            throws JahiaException {
        /**
         * todo This is very ugly... what are we doing here exactly ?
         * Seems like we are replacing the page ID, but it usually is the
         * same. What is the case where we generate a logout URL for a
         * different page ?
         */
        Properties extraProps = new Properties();
        if (theObj != null) {
            Integer pageID = (Integer) theObj;
            extraProps.setProperty("pid", pageID.toString());
        }

        String theUrl = jParams.composeEngineUrl(ENGINE_NAME, extraProps,"?engine_params=logout");

        return jParams.encodeURL(theUrl);
    }


    /**
     * Returns <code>true</code> if the engine needs a <code>JahiaData</code> instance.
     *
     * @param jParams the parameter bean instance
     * @return <code>true</code> if the engine needs a <code>JahiaData</code>, otherwise
     *         <code>false</code>.
     */
    public boolean needsJahiaData(ProcessingContext jParams) {
        return false;        // we need the jData because we are forwarding the request
        // to the "core" engine!
    }


    /**
     * handles the engine actions
     *
     * @param jParams a ParamBean object
     * @param jData   a JahiaData object (not mandatory)
     */
    public EngineValidationHelper handleActions(ProcessingContext jParams, JahiaData jData)
            throws JahiaException {
        String engineParams = jParams.getParameter("engine_params");

        // change mode to normal
        jParams.setOperationMode(ProcessingContext.NORMAL);

        JahiaPage logoutPage = getLogoutPage(jData);

        // if there is no engineparams, it just means that the engine is still set to
        // logout, but in fact the user wants to call "core". This is because a user
        // logs outs, the page is reloaded but still with engine "logout".
        final ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
        final ParamBean bean = ((ParamBean) jParams);
        if ((engineParams != null) && (engineParams.equals("logout"))) {
            if (logger.isDebugEnabled())
                logger.debug("User [" + jParams.getUser().getName() + "] logged out.");

            // send a new logout event !
            JahiaEvent theEvent = new JahiaEvent(this, jParams, jParams.getUser());
            servicesRegistry.getJahiaEventService().fireLogout(theEvent);

            // now let's destroy the cookie authentification if there was one
            // set for this user.
            JahiaUser curUser = jParams.getUser();
            SettingsBean settingsBean = org.jahia.settings.SettingsBean.getInstance();
            String cookieAuthKey = curUser.getProperty(settingsBean.getCookieAuthUserPropertyName());
            Cookie authCookie = new Cookie(settingsBean.getCookieAuthCookieName(), cookieAuthKey);
            authCookie.setPath(jParams.getContextPath());
            authCookie.setMaxAge(0); // means we want it deleted now !
            bean.getRealResponse().addCookie(authCookie);
            curUser.removeProperty(settingsBean.getCookieAuthUserPropertyName());

            jParams.setUserGuest();
        }

        String redirectUrl = jParams.settings().getLogoutRedirectUrl();
        if (redirectUrl!=null) {
            try {
                bean.getRealResponse().sendRedirect(redirectUrl);
                if (logger.isDebugEnabled())
                    logger.debug("Logout triggered Http redirection to [" + redirectUrl + "]");
            } catch (IOException e) {
                throw new JahiaException("Logout redirection failed. Page: " + jParams.getPageID(),
                        "Error redirecting to ["+ redirectUrl + "] from ["+
                        bean.getRealRequest().getRequestURL().toString()+"]",
                        JahiaException.APPLICATION_ERROR,
                        JahiaException.ERROR_SEVERITY, e);
            }
            return null;
        } else if (jParams.settings().getLogoutForwardUrl() != null) {
            try {
                bean.getRequest().getRequestDispatcher(
                        jParams.settings().getLogoutForwardUrl()).forward(
                        bean.getRequest(), bean.getResponse());
                if (logger.isDebugEnabled())
                    logger.debug("Logout triggered forward to ["
                            + jParams.settings().getLogoutForwardUrl() + "]");
            } catch (Exception e) {
                throw new JahiaException("Logout forward failed. Page: "
                        + jParams.getPageID(), "Error forwarding to ["
                        + jParams.settings().getLogoutForwardUrl() + "] from ["
                        + bean.getRequest().getRequestURL().toString() + "]",
                        JahiaException.APPLICATION_ERROR,
                        JahiaException.ERROR_SEVERITY, e);
            }
            return null;
        }

        if (logoutPage == null) {
            // current page do not have read
            bean.invalidateSession();
            throw new JahiaUnauthorizedException();
//            throw new JahiaException("403 Forbidden - Page:" + jParams.getPageID(),
//                    "No read access for page " + jParams.getPageID(),
//                    JahiaException.SECURITY_ERROR,
//                    JahiaException.ERROR_SEVERITY);
        } else {
            Locale localeToUse = null;
            if (!logoutPage.hasEntry(ContentPage.ACTIVE_PAGE_INFOS,
                    jParams.getEntryLoadRequest()
                            .getFirstLocale(true).toString())) {
                List<Locale> siteLanguages =
                        jParams.getSite().getLanguageSettingsAsLocales(true);
                for (Locale locale : siteLanguages) {
                    if (logoutPage.hasEntry(ContentPage.ACTIVE_PAGE_INFOS,
                            locale.toString())) {
                        localeToUse = locale;
                        break;
                    }
                }
            }
            
            // we now restore the session timeout to it's initial value.
            SessionState sessionState = jParams.getSessionState();
            if (sessionState.getAttribute("previousInactiveInterval") != null) {
                int previousInactiveInterval = ((Integer) sessionState.getAttribute("previousInactiveInterval")).intValue();
                sessionState.setMaxInactiveInterval(previousInactiveInterval);
                sessionState.removeAttribute("previousInactiveInterval");
            }
            
            if (jParams.settings().isDoRedirectOnLogout()) {
                String logoutPageUrl = null;
                try {
                    logoutPageUrl = jParams.composePageUrl(logoutPage);
                    bean.invalidateSession();
                    bean.getRealResponse().sendRedirect(logoutPageUrl);
                } catch (IOException e) {
                    throw new JahiaException("Logout redirection failed. Page: " + jParams.getPageID(),
                            "Error redirecting to ["+ logoutPageUrl + "] from ["+
                            bean.getRealRequest().getRequestURL().toString()+"]",
                            JahiaException.APPLICATION_ERROR,
                            JahiaException.ERROR_SEVERITY);
                }
                return null;
            }            

            // get the logout page with template
            try {
                if (localeToUse != null) {
                    jParams.changeLanguage(localeToUse);
                }
                
                if (logoutPage.hasActiveEntries()) {
                    JahiaPageBaseService pageService = JahiaPageBaseService.getInstance();
                    logoutPage = pageService.lookupPage(logoutPage.getID(), jParams);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            if (logoutPage == null) {
                throw new JahiaException("404 Page not found ",
                        "Logout page not found ",
                        JahiaException.SECURITY_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }

            // change the page
            ContentPage contentPage = servicesRegistry
                    .getJahiaPageService().lookupContentPage(logoutPage.getID(),
                    jParams.getEntryLoadRequest(), true);
            jParams.changePage(contentPage);
        }

        if (logoutPage.hasActiveEntries()
                && logoutPage.checkGuestAccess(jParams.getSiteID())) {
            bean.invalidateSession();
            JahiaData jData2 = new JahiaData(jParams, true);
            ((JahiaEngine) EnginesRegistry.getInstance().getEngine(Core_Engine.ENGINE_NAME)).handleActions(
                    jParams, jData2);

        } else {
            bean.invalidateSession();
            jParams.setUserGuest();
            JahiaData jData2 = new JahiaData(jParams, false);
            ((JahiaEngine) EnginesRegistry.getInstance().getEngine(Login_Engine.ENGINE_NAME)).handleActions(
                    jParams, jData2);
        }
        return null;
    }

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName() {
        return ENGINE_NAME;
    }

    // NK

    /**
     * Check for a "friendly" logout page instead of a "403 Forbidden " page, when the user
     * loggout.
     *
     * @param jData a page, can be null if no page available.
     */
    private JahiaPage getLogoutPage(final JahiaData jData) {

        final JahiaUser user = jData.getProcessingContext().getUser();
        JahiaPage page = jData.getProcessingContext().getPage();
        final JahiaSite site = jData.getProcessingContext().getSite();

        if (page == null || user == null || site == null)
            return null;

        if (page.hasActiveEntries() && page.checkGuestAccess(site.getID())
                && jData.getProcessingContext().getContentPage().isAvailable())
            // he can stay at current page.
            return page;

        // look at the user homepages
        try {

            final JahiaPageBaseService pageService = JahiaPageBaseService.getInstance();

            // get user home page
            if (user.getHomepageID() >= 0) {

                try {
                    page = pageService.lookupPage(user.getHomepageID(),
                            jData.getProcessingContext().getEntryLoadRequest(), user,
                            false);
                    if ((page != null) && page.hasActiveEntries()
                            && page.checkGuestAccess(site.getID())){
                        ContentPage contentPage = ContentPage.getPage(page.getID());
                        if ( contentPage.isAvailable() ){
                            return page; // return the user home page
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            // get group homepages
            JahiaGroupManagerService grpServ =
                    ServicesRegistry.getInstance().getJahiaGroupManagerService();

            List v = grpServ.getUserMembership(user);
            int size = v.size();
            String grpKey;
            JahiaGroup grp;
            for (int i = 0; i < size; i++) {
                grpKey = (String) v.get(i);
                grp = grpServ.lookupGroup(grpKey);
                if (grp != null
                        && grp.getSiteID() == site.getID()
                        && grp.getHomepageID() >= 0) {

                    try {
                        page = pageService.lookupPage(grp.getHomepageID(),
                                jData.getProcessingContext().getEntryLoadRequest(), user, false);
                        if ((page != null) && page.hasActiveEntries()
                                && page.checkGuestAccess(site.getID())){
                            ContentPage contentPage = ContentPage.getPage(page.getID());
                            if ( contentPage.isAvailable() ){
                                return page;// return the user first available group home page
                            }
                            // TODO -> have a default homepage per user !!! Pol is doing it
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            // if none available check if user has guest access on the site's homepage
            page = pageService.lookupPage(site.getHomePageID(),
                    jData.getProcessingContext().getEntryLoadRequest(), user, false);
            if ((page != null) && page.hasActiveEntries()
                    && page.checkGuestAccess(site.getID())) {
                ContentPage contentPage = ContentPage.getPage(page.getID());
                if ( contentPage.isAvailable() ){
                    return page;
                }
            } else if ((page != null) && !page.hasActiveEntries()) {
                return page;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null; // no page available...
    }
}
