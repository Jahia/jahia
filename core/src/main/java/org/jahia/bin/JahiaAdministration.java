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
// $Id$
//
//  JahiaAdministration
//
//  27.01.2001  AK  added in jahia.
//  29.01.2001  AK  fix a bug when check the database.
//  06.02.2001  AK  add server settings methods.
//  07.02.2001  AK  replace context attributes by session and request attributes.
//  10.01.2001  AK  include design, add base for templates and components.
//  13.01.2001  MJ  added user&group management.
//  31.03.2001  AK  completely change the properties system.
//  01.04.2001  AK  cut the servlet into some beans.
//  17.04.2001  AK  change methods processLogin() and isValidLoginSession() to
//                  check if the user is member of the "admin" or "superadmin"
//                  elitists groups.
//  18.04.2001  AK  change method displayMenu() to get all administrator access
//                  granted website(s).
//  20.04.2001  AK  fix mammouth in userRequestDispatcher() method.
//  21.04.2001  AK  set an attribute if the super admin is logged to administrate
//                  the server and not an hosted site.
//  30.04.2001  AK  administrative log settings are now only available on server
//                  mode, because they are not sites-specific.
//  16.05.2001  NK  DisplayMenu handle default site created at install.
//  12.06.2001  NK  added jef files management
//
//

package org.jahia.bin;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.jahia.admin.AdministrationModulesRegistry;
import org.jahia.admin.AdministrationModule;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.MenuItem;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.cache.JahiaBatchingClusterCacheHibernateProvider;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.AdminParamBean;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ProcessingContextFactory;
import org.jahia.params.ServletURLGeneratorImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.security.license.LicenseManager;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSiteTools;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.springframework.beans.factory.BeanFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;


/**
 * desc:  This servlet handles all general administration tasks.
 * it requires authentication as a member of the "admin" group (for the
 * site currently used... or "superadmin" group on the site 0), and provides
 * a management interface for general context property settings, database
 * settings, sites, pages, users, user groups, application roles, components,
 * templates and audit log settings.
 * <p/>
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Alexandre Kraft
 * @author Michael Janson
 * @author Khue NGuyen
 * @author Serge Huber
 * @version 1.0
 */
public class JahiaAdministration extends org.apache.struts.action.ActionServlet implements JahiaInterface {

    private static final long serialVersionUID = 332486402871252316L;

    private static Logger logger = Logger.getLogger(JahiaAdministration.class);

    private static ServletConfig config;
    private static ServletContext context;

    public static final String CLASS_NAME = "org.jahia.bin.JahiaAdministration";
    public static final String JSP_PATH = "/admin/";
    private static final int SUPERADMIN_SITE_ID = 0;

    private static final long DAY_MILLIS = 1000L * 60L * 60L * 24L;

    private static ServicesRegistry sReg;
    private static JahiaUserManagerService uMgr;
    private static JahiaGroupManagerService gMgr;
    static private String servletPath = null;

    private String servletURI = null;
    public static String installerURL = "";
    private static SettingsBean jSettings;
    private static String contentServletPath = null;

    static private final String GET_REQUEST = "GET";
    static private final String POST_REQUEST = "POST";

    //-------------------------------------------------------------------------
    /**
     * Default init inherited from HttpServlet. This method set config and
     * context static variables.
     *
     * @param aConfig Servlet configuration (inherited).
     */
    public void init(ServletConfig aConfig)
            throws ServletException {
        super.init(aConfig);
        // get servlet config and context...
        JahiaAdministration.config = aConfig;
        JahiaAdministration.context = aConfig.getServletContext();

        JahiaAdministration.contentServletPath = Jahia.getDefaultServletPath(aConfig.getServletContext());
    } // end init

    /**
     * Default service inherited from HttpServlet.
     *
     * @param request  Servlet request (inherited).
     * @param response Servlet response (inherited).
     */
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws IOException,
            ServletException {
        logger.debug("-- service --");
        if (logger.isDebugEnabled()) {
            logger.debug("--[ " + request.getMethod() + " Request Start URI='" +
                    request.getRequestURI() + "' query='" +
                    request.getQueryString() + "'] --");
        }

        Jahia.copySessionCookieToRootContext(request, response);

        if (jSettings != null) {
            if (jSettings.isUtf8Encoding()) {
                // bad browser, doesn't send character encoding :(
                // we can force the encoding ONLY if we do this call before any
                // getParameter() call is done !
                request.setCharacterEncoding("UTF-8");
            }
        }

        // get the current user session...
        HttpSession session = request.getSession(true);

        if (JahiaAdministration.servletPath == null) {
            servletPath = request.getServletPath();
        }

        if (Jahia.isInitiated() && !Jahia.checkLockAccess(session)) {
            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage",
                    "Sorry, Jahia is locked by a super admin. No more access allowed.");
            displayLogin(request, response, session);
            return;
        }

        if (Jahia.getJahiaHttpPort() == -1) {
            Jahia.setJahiaHttpPort(request.getServerPort());
        }

        // init host servlet URI
        if (servletURI == null) {
            this.servletURI = getServletURI(request, response);
        }


        // determine installation status...
        if (!Jahia.isInitiated()) { // jahia is not launched...
            request.setAttribute("jahiaLaunch", "administration"); // call jahia to init and re-launch admin...
            logger.debug("Redirecting to " + contentServletPath +
                    " in order to init Jahia first ...");
            doRedirect(request, response, session, contentServletPath);
        } else { // jahia is running...
        	jSettings = SettingsBean.getInstance();
            if (jSettings != null) {
                installerURL = jSettings.getJahiaEnginesHttpPath();

                // set Jahia running mode to Admin
                session.setAttribute(ProcessingContext.SESSION_JAHIA_RUNNING_MODE,
                                     Jahia.ADMIN_MODE);
                logger.debug("Running mode : " + Jahia.ADMIN_MODE);

                if (Jahia.getCoreLicense() == null) {
                    session.setAttribute(CLASS_NAME + "jahiaDisplayMessage",
                            "Invalid License");
                    displayLogin(request, response, session);
                } else {
                    try {
                        if (!handleEngines(request, response))
                            userRequestDispatcher(request, response, session); // ok continue admin...
                        ServicesRegistry.getInstance().getCacheService().syncClusterNow();
                        JahiaBatchingClusterCacheHibernateProvider.syncClusterNow();
                    } catch (JahiaException je) {
                        ErrorHandler.getInstance().handle(je, request, response);
                    } catch (Exception t) {
                        ErrorHandler.getInstance().handle(t, request, response);
                    }
                }
            } else {
                request.setAttribute("jahiaLaunch", "installation"); // call jahia to init and launch install...
                doRedirect(request, response, session, contentServletPath);
            }
        }
        Jahia.setThreadParamBean(null);
        logger.debug("--[ " + request.getMethod() + " Request End ] --");
    } // end service

    public static String getServletPath() {
        return servletPath;
    }

    /**
     * Static method to generate URLs for the administration. This should be
     * used by all JSPs and java code that is called by the administration.
     *
     * @param request          the current request object, used to generate the context
     *                         path part of the URL
     * @param response         the current response object, used to make a call to
     *                         encodeURL to generate session information in the case that cookies cannot
     *                         be used
     * @param doAction         a String representing the action we are linking to. This
     *                         is then encoded as a ?do=doAction string
     * @param extraQueryParams a string including any other parameters that will
     *                         be directly appended after the doAction string. This is done because this
     *                         way we offer the possibility to do an encodeURL over the whole string.
     *                         Note that this string may be null.
     * @return a String containing an URL with jsessionid generated and in the
     *         form : /contextPath/servletPath/?do=doActionextraQueryParams
     */
    public static String composeActionURL(HttpServletRequest request,
                                          HttpServletResponse response,
                                          String doAction,
                                          String extraQueryParams) {
        String internalDoAction = "";
        String internalQueryParams = "";

        if (doAction != null) {
            internalDoAction = "/?do=" + doAction;
        }

        if (extraQueryParams != null) {
            internalQueryParams = extraQueryParams;
        }

        StringBuffer actionURL = new StringBuffer();
        actionURL.append(request.getContextPath()).append(getServletPath()).append(internalDoAction)
                .append(internalQueryParams);
        return response.encodeURL(StringUtils.replace(actionURL.toString(), "//", "/"));
    }


    private boolean hasServerPermission(String permissionName, ProcessingContext jParams) {
        JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        return aclService.getServerActionPermission(permissionName,
                jParams.getUser(),
                JahiaBaseACL.READ_RIGHTS,
                jParams.getSiteID()) > 0;
    }

    private boolean hasSitePermission(String permissionName, ProcessingContext jParams) {
        JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        return aclService.getSiteActionPermission(permissionName,
                jParams.getUser(),
                JahiaBaseACL.READ_RIGHTS,
                jParams.getSiteID()) > 0;
    }

    //-------------------------------------------------------------------------
    /**
     * This method is used like a dispatcher for user requests.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  Servlet session for the current user.
     */
    private void userRequestDispatcher(final HttpServletRequest request,
                                       final HttpServletResponse response,
                                       final HttpSession session)
            throws JahiaException, IOException, ServletException {
        String op = request.getParameter("do");
        if (op == null)
            op = "";
        final String operation = op;

        ParamBean jParams = initAdminJahiaData(request, response, session);

        try {
            if (operation.equals("processlogin")) {                     // operation : process login...
                processLogin(request, response, session, null, null);
            } else {
                Boolean accessGranted = (Boolean) session.getAttribute(CLASS_NAME + "accessGranted");
                if (accessGranted == null) {
                    accessGranted = Boolean.FALSE;
                }

                if (accessGranted) {

                    AdministrationModulesRegistry modulesRegistry = (AdministrationModulesRegistry) SpringContextSingleton.getInstance().getContext().getBean("administrationModulesRegistry");
                    AdministrationModule currentModule = modulesRegistry.getServerAdministrationModule(operation);
                    if (currentModule != null) {
                        if (hasServerPermission(currentModule.getPermissionName(), jParams)) {
                            session.setAttribute(CLASS_NAME + "configJahia", Boolean.TRUE);
                            /** todo clean up this hardcoded mess. Is it even used anymore ? */
                            if ("sharecomponents".equals(operation) && jParams.getUser().isRoot()) {
                                request.setAttribute("showAllComponents", Boolean.TRUE);
                            }

                            currentModule.service(request, response);
                        } else if ("sites".equals(operation) && (session.getAttribute(JahiaAdministration.CLASS_NAME + "redirectToJahia") != null)) {
                            session.setAttribute(CLASS_NAME + "configJahia", Boolean.TRUE);
                            currentModule.service(request, response);
                        }
                    } else {
                        currentModule = modulesRegistry.getSiteAdministrationModule(operation);
                        if (currentModule != null) {
                            if (hasSitePermission(currentModule.getPermissionName(), jParams)) {
                                session.setAttribute(CLASS_NAME + "configJahia", Boolean.FALSE);
                                if ("search".equals(operation)) {
                                    // Use response response wrapper to ensure correct handling of Application fields output to the response
                                    // @todo is this still necessary with Pluto wrapper in effect ?
                                    currentModule.service(jParams.getRequest(), jParams.getResponse());
                                } else {
                                    currentModule.service(request, response);
                                }
                            }
                        }
                    }

                    if ("switch".equals(operation)) {
                        // operation : switch management mode (server, sites)
                        switchModeAction(request, response, session);
                    } else if ("change".equals(operation)) {
                        // operation : change site id to manage
                        session.setAttribute(CLASS_NAME + "configJahia", Boolean.FALSE);
                        changeSite(request, response, session);
                    } else if ("clipbuilder".equals(operation) &&
                            hasServerPermission("admin.clipBuilder.MenuBuilderAction", jParams)) {
                        // clipbuilder is a struts servlet
                        session.setAttribute(CLASS_NAME + "configJahia", Boolean.TRUE);
                        process(request, response);
                    } else {
                        if (currentModule == null) {
                            displayMenu(request, response, session);
                        }
                    }

                    // the user don't currently have a granted access...
                } else if (isValidLoginSession(session)) {
                    displayMenu(request, response, session);
                } else {
                    logger.debug("session login not valid.");
                    displayLogin(request, response, session);
                }
            }
        } catch (Exception e) {
            logger.error("Error during " + operation + " operation of a new element we must flush all caches to ensure integrity between database and viewing", e);
            ServicesRegistry.getInstance().getCacheService().flushAllCaches();
            if (isValidLoginSession(session)) {
                displayMenu(request, response, session);
            } else {
                displayLogin(request, response, session);
            }
        }
    } // end userRequestDispatcher


    //-------------------------------------------------------------------------
    /**
     * Forward servlet request and servlet response objects, using the request
     * dispatcher contained in the context. Please be careful: use only context
     * relative path.
     *
     * @param request     Servlet request.
     * @param response    Servlet response.
     * @param destination Context relative path where you want to go.
     */
    public synchronized static void doRedirect(HttpServletRequest request,
                                               HttpServletResponse response,
                                               HttpSession session,
                                               String destination)
            throws IOException, ServletException {
        if (session.getAttribute(CLASS_NAME + "jahiaDisplayMessage") == null) {
            request.setAttribute("jahiaDisplayMessage", "");
        } else {
            request.setAttribute("jahiaDisplayMessage",
                    session.getAttribute(CLASS_NAME + "jahiaDisplayMessage"));
        }

        session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", "");

        request.setAttribute("URL", installerURL);
        request.setAttribute("requestURI", request.getRequestURI());

        if (request.getAttribute("title") == null) {
            request.setAttribute("title", "no title");
        }

        // init locale
        Locale defaultLocale = (Locale) session
                .getAttribute(ProcessingContext.SESSION_LOCALE);
        if (defaultLocale == null) {
            defaultLocale = request.getLocale() != null ? request.getLocale()
                    : Locale.ENGLISH;
        }
        session.setAttribute(ProcessingContext.SESSION_LOCALE, defaultLocale);

        String contentTypeStr = "text/html;charset=";
        String charEncoding = org.jahia.settings.SettingsBean.getInstance() != null ? org.jahia.settings.SettingsBean.getInstance()
                .getDefaultResponseBodyEncoding() : "UTF-8";
        contentTypeStr = contentTypeStr + charEncoding;

        request.setAttribute("content-type", contentTypeStr);

        // response no-cache headers...
        response.setHeader("Pragma", "no-cache");
        //response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        if (jData != null) {
            final ParamBean jParams = (ParamBean) jData.getProcessingContext();
            
            initMenu(request);

            try {
                String htmlContent = ServicesRegistry.getInstance().
                        getJahiaFetcherService().fetchServlet(jParams, destination);

                if (jParams.getRedirectLocation() != null) {
                    logger.debug(
                            "sendRedirect call detected during output generation, no other output...");
                    if (!response.isCommitted()) {
                        response.sendRedirect(
                                response.encodeRedirectURL(jParams.getRedirectLocation()));
                    }
                } else {
                    /** @todo we should really find a more elegant way to handle this
                     *  case, especially when handling a file manager download that
                     *  has already accessed the RealResponse object.
                     */
                    if (!response.isCommitted()) {
                        logger.debug("Printing content output to real writer");
                        response.setContentType(contentTypeStr);
                        ServletOutputStream outputStream = response.getOutputStream();
                        OutputStreamWriter streamWriter;
                        streamWriter = new OutputStreamWriter(outputStream, charEncoding);
                        streamWriter.write(htmlContent, 0, htmlContent.length());
                        streamWriter.flush();
//                        response.getWriter().append(htmlContent);
                    } else {
                        logger.debug(
                                "Output has already been committed, aborting display...");
                    }
                }
            } catch (JahiaException je) {
                StringWriter strWriter = new StringWriter();
                PrintWriter ptrWriter = new PrintWriter(strWriter);
                logger.debug("Error while redirecting", je);

                ptrWriter.println("Exception in doRedirect");
                je.printStackTrace(ptrWriter);

                Throwable t = je.getRootCause();
                if (t != null) {
                    ptrWriter.println("Root cause Exception");
                    t.printStackTrace(ptrWriter);
                }

                logger.warn(strWriter.toString());
            }
        } else {
            config.getServletContext().getRequestDispatcher(destination).forward(
                    request, response);
        }
    } // end doRedirect


    public static void initMenu(HttpServletRequest request) {
        JahiaData jData = (JahiaData) request
                .getAttribute("org.jahia.data.JahiaData");
        if (jData != null) {
            ParamBean ctx = (ParamBean) jData.getProcessingContext();

            AdministrationModulesRegistry administrationModulesRegistry = (AdministrationModulesRegistry) SpringContextSingleton
                    .getInstance().getContext().getBean(
                            "administrationModulesRegistry");
            ctx.setAttribute("administrationServerModules", getMenuItems(
                    administrationModulesRegistry.getServerModules(), ctx));
            ctx.setAttribute("administrationSiteModules", getMenuItems(
                    administrationModulesRegistry.getSiteModules(), ctx));
        }
    }

    private static List<MenuItem> getMenuItems(
            List<AdministrationModule> modules, ParamBean ctx) {
        List<MenuItem> menuItems = new LinkedList<MenuItem>();
        for (AdministrationModule module : modules) {
            String actionUrl = null;
            try {
                actionUrl = module.getActionURL(ctx);
            } catch (Exception e) {
                logger.error(
                        "Error computing an URL for the administraion module '"
                                + module.getName() + "'", e);
            }
            menuItems.add(new MenuItem(module.getName(), actionUrl != null
                    && module.isEnabled(ctx.getUser(), ctx.getSiteID()), module
                    .getLabel(), actionUrl, module.getIcon(), module.getIconSmall(), module
                    .getTooltip(), module.isSelected(ctx)));
        }

        return menuItems;
    }

    //-------------------------------------------------------------------------
    /**
     * Display the login page, using doRedirect().
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  Servlet session for the current user.
     */
    private void displayLogin(HttpServletRequest request,
                              HttpServletResponse response,
                              HttpSession session)
            throws IOException, ServletException {
        // retrieve previous form values...
        String jahiaLoginUsername = (String) request.getAttribute(CLASS_NAME + "jahiaLoginUsername");

        // set default values (if necessary)...
        if (jahiaLoginUsername == null) {
            jahiaLoginUsername = "";
        }

        // set request attributes...
        request.setAttribute("jahiaLoginUsername", jahiaLoginUsername);
        request.setAttribute("redirectTo", request.getRequestURI() + "?" + request.getQueryString());

        doRedirect(request, response, session, JSP_PATH + "login.jsp");
    } // end displayLogin


    //-------------------------------------------------------------------------
    /**
     * Process and check validity of inputs from the login page.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  Servlet session for the current user.
     * @param rootName String containing root username fo bypass login.
     * @param rootPass String containing root password fo bypass login.
     * @todo FIXME we need to change this so that siteadmins can also login,
     * without accessing server settings. Actually a global implementation
     * would grant permissions to each administration action.
     */
    private void processLogin(HttpServletRequest request,
                              HttpServletResponse response,
                              HttpSession session,
                              String rootName,
                              String rootPass)
            throws IOException, ServletException {
        logger.debug("processLogin started");

        boolean loginError = true;
        String jahiaLoginUsername;
        String jahiaLoginPassword;

        JahiaUser theUser = null;
        JahiaGroup theGroup;
        boolean superAdmin = false;
        int entrySiteID = 0;

        // get form values...
        if (rootName == null && rootPass == null) {
            jahiaLoginUsername = request.getParameter("login_username").trim();
            jahiaLoginPassword = request.getParameter("login_password").trim();
        } else {
            jahiaLoginUsername = rootName;
            jahiaLoginPassword = rootPass;
        }

        String redirectTo = request.getParameter("redirectTo");
        if (redirectTo != null && request.getCharacterEncoding() != null) {
            redirectTo = URLDecoder.decode(redirectTo, request.getCharacterEncoding());
        }

        // get references to user manager and group manager...
        sReg = ServicesRegistry.getInstance();
        if (sReg != null) {
            uMgr = sReg.getJahiaUserManagerService();
            gMgr = sReg.getJahiaGroupManagerService();
        }

        // check form validity...
        if (uMgr != null) {

            theUser = uMgr.lookupUser(jahiaLoginUsername);

            if (theUser == null) {
                String s = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaDisplayMessage.logininvalid.label",
                        request.getLocale());
                logger.warn(s);
                request.setAttribute(JahiaAdministration.CLASS_NAME + "jahiaDisplayMessage", s);
            }

            theGroup = gMgr.getAdministratorGroup(SUPERADMIN_SITE_ID);

            if (theUser != null) {
                if (theUser.verifyPassword(jahiaLoginPassword)) {
                    if (theGroup.isMember(theUser)) {
                        loginError = false;
                        superAdmin = true;
                        logger.debug("Login granted: " + jahiaLoginUsername + " entered correct password.");
                    } else {
                        List<JahiaSite> adminGrantedSites;
                        try {
                            adminGrantedSites = getAdminGrantedSites(theUser);
                        } catch (JahiaException e) {
                            adminGrantedSites = new ArrayList<JahiaSite>();
                        }
                        superAdmin = false;
                        if (adminGrantedSites.size() > 0) {
                            loginError = false;
                            JahiaSite firstAdminSite = adminGrantedSites.get(0);
                            entrySiteID = firstAdminSite.getID();
                        } else {
                            String dspMsg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaDisplayMessage.isntadministrator1.label",
                                    request.getLocale());
                            dspMsg += " ";
                            dspMsg += jahiaLoginUsername;
                            dspMsg += " ";
                            dspMsg += JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaDisplayMessage.isntadministrator2.label",
                                    request.getLocale());
                            request.setAttribute(JahiaAdministration.CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                            logger.error("Login Error: User " + jahiaLoginUsername + " is not an system administrator.");
                        }
                    }
                } else {
                    String dspMsg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaDisplayMessage.logininvalid.label",
                            request.getLocale());
                    request.setAttribute(JahiaAdministration.CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                    logger.error("Login Error: User " + jahiaLoginUsername + " entered bad password.");
                }
            }
        }

        if (!loginError) {                                                           // access granted...
            // i lookup user on the superadmin group. so... only a super admin can arrive on this set attribute :o)
            session.setAttribute(CLASS_NAME + "isSuperAdmin", superAdmin);
            session.setAttribute(CLASS_NAME + "manageSiteID", entrySiteID);
            session.setAttribute(CLASS_NAME + "accessGranted", Boolean.TRUE);
            session.setAttribute(CLASS_NAME + "jahiaLoginUsername", jahiaLoginUsername);
            if (entrySiteID == 0) {
                session.setAttribute(CLASS_NAME + "configJahia", Boolean.TRUE);
            }
            session.setAttribute(ProcessingContext.SESSION_USER, theUser);
            if (redirectTo == null) {
                displayMenu(request, response, session);
            } else {
                logger.debug("Should redirect to : " + redirectTo + " but not yet implemented.");
                /** @todo not implemented fully for the moment, let's just display menu */
                displayMenu(request, response, session);
            }
        } else {                                                                    // access failed...
            session.setAttribute(CLASS_NAME + "isSuperAdmin", Boolean.FALSE);
            session.setAttribute(CLASS_NAME + "accessGranted", Boolean.FALSE);
            session.setAttribute(CLASS_NAME + "configJahia", Boolean.FALSE);
            request.setAttribute(CLASS_NAME + "jahiaLoginUsername", jahiaLoginUsername);
            displayLogin(request, response, session);
        }
    } // end processLogin


    //-------------------------------------------------------------------------
    /**
     * Display the administration menu, using doRedirect().
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  Servlet session for the current user.
     */
    public static void displayMenu(HttpServletRequest request,
                                   HttpServletResponse response,
                                   HttpSession session)
            throws IOException, ServletException {
        List<JahiaSite> grantedSites = new ArrayList<JahiaSite>();
        JahiaUser theUser;

        logger.debug("started ");
        JahiaSite theSite = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);

        if (theSite == null) {
            logger.debug("session site is null ! ");
        } else {
            // try to get site from cache
            try {
                if (sReg == null) {
                    sReg = ServicesRegistry.getInstance();
                }
                theSite = sReg.getJahiaSitesService().getSiteByKey(theSite.getSiteKey());
            } catch (Exception e) {
                logger.debug(e.getMessage(), e);
                theSite = null;
            }
        }

        // get sites where the user has an admin access...
        try {
            theUser = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
            if (theUser != null) {
                grantedSites = getAdminGrantedSites(theUser);
            }
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
        }

        if (grantedSites == null) {
            logger.debug("can not admin any site at all !!! ");
            grantedSites = new ArrayList<JahiaSite>();
        } else {
            Locale defaultLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_LOCALE);
            if (defaultLocale != null) {
                Collections.sort(grantedSites, JahiaSite.getTitleComparator(defaultLocale));
            } else {
                Collections.sort(grantedSites, JahiaSite.getTitleComparator());
            }
        }

        if (theSite == null && grantedSites.size() > 0) {
            theSite = grantedSites.get(0);
        }

        // check if the user is created on this site...
        if (theSite != null) {
            session.setAttribute(CLASS_NAME + "manageSiteID", theSite.getID());
            session.setAttribute(ProcessingContext.SESSION_SITE, theSite);
        }

        try {
            initAdminJahiaData(request, response, session);
        } catch (JahiaException je) {
            ErrorHandler.getInstance().handle(je, request, response);
            return;
        }

        long expirationTime = LicenseManager.getInstance().getJahiaExpirationDate();
        if (expirationTime > 0) {
            long timeLeft = expirationTime - System.currentTimeMillis();
            timeLeft = timeLeft < 0 ? 0 : timeLeft;
            request.setAttribute("daysLeft", (int) (timeLeft / DAY_MILLIS));
        }

        request.setAttribute("site", theSite);
        request.setAttribute("sitesList", grantedSites);
        request.setAttribute("siteID", session.getAttribute(CLASS_NAME + "manageSiteID"));
        request.setAttribute("isSuperAdmin", session.getAttribute(CLASS_NAME + "isSuperAdmin"));
        request.setAttribute("configJahia", session.getAttribute(CLASS_NAME + "configJahia"));

        doRedirect(request, response, session, JSP_PATH + "menu.jsp");
    } // end displayMenu


    //-------------------------------------------------------------------------
    /**
     * Change the site you want to administrate. After, display the menu again.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  Servlet session for the current user.
     */
    public static void changeSite(HttpServletRequest request,
                                  HttpServletResponse response,
                                  HttpSession session)
            throws IOException, ServletException {
        String newSiteID = request.getParameter("changesite").trim();
        int siteID = Integer.parseInt(newSiteID);

        // check if the user has really admin access to this site...
        JahiaUser theUser = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
        JahiaGroup group = ServicesRegistry.getInstance().getJahiaGroupManagerService().getAdministratorGroup(siteID);

        JahiaSite currentSite = null;
        JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();

        if ((group != null && group.isMember(theUser)) ||
                (aclService.getSiteActionPermission("admin.JahiaAdministration", theUser, JahiaBaseACL.READ_RIGHTS, siteID) > 0)) {
            try {
                currentSite = ServicesRegistry.getInstance().getJahiaSitesService().getSite(siteID);
                List<Locale> languageSettingsAsLocales = currentSite.getLanguageSettingsAsLocales(true);
                final Locale localeSession = (Locale) session.getAttribute(ProcessingContext.SESSION_LOCALE);
                if (languageSettingsAsLocales != null && languageSettingsAsLocales.size() > 0 && !languageSettingsAsLocales.contains(localeSession)) {
                    Locale locale = languageSettingsAsLocales.get(0);
                    session.setAttribute(Globals.LOCALE_KEY, locale);
                    session.setAttribute(ProcessingContext.SESSION_LOCALE, locale);
                }
                session.setAttribute(ProcessingContext.SESSION_SITE, currentSite);
            } catch (JahiaException je) {
                String dspMsg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaDisplayMessage.logininvalid.label",
                        request.getLocale());
                request.setAttribute(JahiaAdministration.CLASS_NAME + "jahiaDisplayMessage", dspMsg);
            }
        } else {
            //System.out.println(" --> no admin access on this site <--");
            currentSite = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);
            siteID = currentSite.getID();
        }

        // set the new site id to administrate...
        request.setAttribute("site", currentSite);
        session.setAttribute(CLASS_NAME + "manageSiteID", siteID);

        displayMenu(request, response, session);
    } // end changeSite


    //-------------------------------------------------------------------------
    /**
     * Change the management mode, between sites and server. After, display the menu again.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  Servlet session for the current user.
     */
    public static void switchModeAction(HttpServletRequest request,
                                        HttpServletResponse response,
                                        HttpSession session)
            throws IOException, ServletException {

        logger.debug("switchModeAction started");

        String mode = request.getParameter("mode").trim();

        if (mode.equals("server")) {
            // check if the user has really superadmin access...
            //JahiaUser  theUser  =  (JahiaUser) session.getAttribute( ProcessingContext.SESSION_USER );
            // @todo we will want to check here if the user has ANY permission on the server
            session.setAttribute(CLASS_NAME + "configJahia", Boolean.TRUE);
        } else {
            session.setAttribute(CLASS_NAME + "configJahia", Boolean.FALSE);
        }

        displayMenu(request, response, session);
    } // end switchModeAction


    //-------------------------------------------------------------------------
    /**
     * Checks if a login session passed from Jahia is valid for silent login to JahiaAdministration
     *
     * @param session the HttpSession object
     * @return <code>true</code> if the user can access to administration, <code>false</code> otherwise.
     */
    static boolean isValidLoginSession(HttpSession session) {

        logger.debug("isValidatingLoginSession started");

        boolean isValid = false;
        boolean isSuperAdmin = false;

        try {
            // get references to user manager and group manager...
            sReg = ServicesRegistry.getInstance();
            if (sReg != null) {
                uMgr = sReg.getJahiaUserManagerService();
                gMgr = sReg.getJahiaGroupManagerService();
            }

            JahiaUser theUser = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
            if (theUser == null) {
                return false;
            }
            JahiaSite theSite = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);

            if (theSite != null) {

                JahiaGroup theGroup = gMgr.getAdministratorGroup(theSite.getID());
                if (theGroup == null) {
                    return false; // group might have been deleted
                }
                if (theGroup.isMember(theUser) ||
                        (ServicesRegistry.getInstance().getJahiaACLManagerService().
                                getSiteActionPermission("admin.JahiaAdministration",
                                        theUser,
                                        JahiaBaseACL.READ_RIGHTS,
                                        theSite.getID()) > 0)) {
                    // check if the user is a super admin or not...
                    JahiaGroup superAdminGroup = gMgr.getAdministratorGroup(SUPERADMIN_SITE_ID);
                    if (superAdminGroup.isMember(theUser)) {
                        isSuperAdmin = true;
                    }

                    session.setAttribute(CLASS_NAME + "isSuperAdmin", isSuperAdmin);
                    session.setAttribute(CLASS_NAME + "manageSiteID", theSite.getID());
                    session.setAttribute(CLASS_NAME + "accessGranted", Boolean.TRUE);
                    session.setAttribute(CLASS_NAME + "jahiaLoginUsername", theUser.getUsername());

                    logger.debug("Silent login granted: user " + theUser.getUsername() + " has valid login session.");
                    isValid = true;
                } else {
                    logger.debug("Couldn't validate login session for: " + theUser.getUsername());
                }
            } else if (theUser.hashCode() == 0) {

                session.setAttribute(CLASS_NAME + "isSuperAdmin", Boolean.TRUE);
                session.setAttribute(CLASS_NAME + "manageSiteID", 0);
                session.setAttribute(CLASS_NAME + "accessGranted", Boolean.TRUE);
                session.setAttribute(CLASS_NAME + "jahiaLoginUsername", theUser.getUsername());
            }

        } catch (Exception e) {
            logger.error("Exception in isValidLoginSession", e);
        }
        return isValid;
    } // end isValidLoginSession


    //-------------------------------------------------------------------------
    /**
     * Handles engine calls within JahiaAdministration Servlet
     *
     * @param request
     * @param response
     * @return boolean true if the request is effectively a engine call.
     */
    protected boolean handleEngines(HttpServletRequest request, HttpServletResponse response)
            throws JahiaException, IOException, ServletException {

        String pathInfo = request.getPathInfo();
        logger.debug("Path Info: " + pathInfo);
        if (pathInfo == null)
            return false;
        if (pathInfo.indexOf(ProcessingContext.ENGINE_NAME_PARAMETER) == -1)
            return false;

        // get the main http method...
        String requestMethod = request.getMethod();

        logger.debug("------------------------------------------------------- NEW " + requestMethod + " REQUEST ---");

        // create the parambean (jParams)...
        ParamBean jParams;

        try {

            BeanFactory bf = SpringContextSingleton.getInstance().getContext();
            ProcessingContextFactory pcf = (ProcessingContextFactory) bf.getBean(ProcessingContextFactory.class.getName());
            jParams = pcf.getContext(request, response, context);

            // jParams = new ParamBean( request, response, context, jSettings, startTime, intRequestMethod);

            if (jParams == null) {
                throw new JahiaException(CLASS_NAME + ".handleEngine",
                        "ParamBean is null",
                        JahiaException.ERROR_SEVERITY,
                        JahiaException.CRITICAL_SEVERITY);
            }
            request.setAttribute("org.jahia.params.ParamBean", jParams);
            process(request, response);
            /*
            OperationManager operations = new OperationManager();
            operations.handleOperations (jParams, jSettings);
            */

            // display time
            if (jParams.getUser() != null && logger.isDebugEnabled()) {
                logger.debug("Served " + jParams.getEngine() +
                        " engine for user " +
                        jParams.getUser().getUsername() + " from [" +
                        jParams.getRequest().getRemoteAddr() + "] in [" +
                        (System.currentTimeMillis() - jParams.getStartTime()) +
                        "ms]");
            }
        }
        catch (JahiaException je) {
            ErrorHandler.getInstance().handle(je, request, response);
        }

        return true;
    }

    //-------------------------------------------------------------------------
    /**
     * Returns a relative qualified request URL , i.e /JahiaAdministration.
     *
     * @param request
     */
    private String getServletURI(HttpServletRequest request,
                                 HttpServletResponse response) {

        if (request == null)
            return "";

        String pathInfo = request.getPathInfo();

        String tempServletURI;
        if (pathInfo == null) {
            tempServletURI = response.encodeURL(request.getRequestURI());
        } else {
            tempServletURI = response.encodeURL(request.getRequestURI().substring(0, request.getRequestURI().indexOf(pathInfo)));
        }
        return tempServletURI;
    }

    //-------------------------------------------------------------------------
    /**
     * Init a milimalistic JahiaData that is necessary to work with taglibs and
     * engines within JahiaAdministration servlet.
     * The JahiaData only contains the current site and the user,
     * that is the current identified admin. The page is null and
     * fields set and containers set are not build.
     * The JahiaData is then stored in the request as the attribute :
     * "org.jahia.data.JahiaData"
     * "org.jahia.data.JahiaData"
     *
     * @param request
     * @param response
     * @param session
     */
    static public ParamBean initAdminJahiaData(HttpServletRequest request,
                                               HttpServletResponse response,
                                               HttpSession session)
            throws JahiaException, IOException, ServletException {

        logger.debug("started");
        JahiaSite site = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);
        JahiaUser user = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
        if (site == null) {
            // This can occurs when all sites has been deleted ...
            // create a fake site for JSP using taglibs
            JahiaSite fakeSite = new JahiaSite(-1,
                    "",
                    "",
                    "",
                    false,
                    -1,
                    "",
                    null,
                    new Properties());
            site = fakeSite;
            session.setAttribute(ProcessingContext.SESSION_SITE, fakeSite);
        }
        ContentPage contentPage = null;

        Integer I = (Integer) session.getAttribute(ProcessingContext.SESSION_LAST_REQUESTED_PAGE_ID);
        if (I != null) {
            try {
                contentPage = ServicesRegistry.getInstance().getJahiaPageService()
                        .lookupContentPage(I, false);
                if (contentPage.getJahiaID() != site.getID()) {
                    contentPage = site.getHomeContentPage(); // site has changed , we cannot use the old page
                    session.setAttribute(ProcessingContext.SESSION_LAST_REQUESTED_PAGE_ID, Integer.valueOf(contentPage.getID()));
                }
            } catch (Exception t) {
                logger.debug(t.getMessage(), t);
            }
        } else {
            contentPage = site.getHomeContentPage();
            if (contentPage != null) {
                session.setAttribute(ProcessingContext.SESSION_LAST_REQUESTED_PAGE_ID, Integer.valueOf(contentPage.getID()));
            }
        }

        // start the chrono...
        long startTime = System.currentTimeMillis();

        // get the main http method...
        String requestMethod = request.getMethod();
        int intRequestMethod = 0;

        if (GET_REQUEST.equals(requestMethod)) {
            intRequestMethod = ProcessingContext.GET_METHOD;
        } else if (POST_REQUEST.equals(requestMethod)) {
            intRequestMethod = ProcessingContext.POST_METHOD;
        }

        AdminParamBean jParams = new AdminParamBean(request,
                response,
                context,
                jSettings,
                startTime,
                intRequestMethod,
                site,
                user,
                contentPage);
        jParams.setUrlGenerator(new ServletURLGeneratorImpl(request, response));
        if (contentPage != null) {
            try {
                contentPage = ServicesRegistry.getInstance()
                        .getJahiaPageService().lookupContentPage(contentPage.getID(), jParams.getEntryLoadRequest(), true);
            } catch (Exception t) {
                logger.error(t.getMessage(), t);
            }
        }
        JahiaData jData = new JahiaData(jParams, false);
        jData.getProcessingContext().changePage(contentPage);

        request.setAttribute("org.jahia.data.JahiaData", jData);
        request.setAttribute("org.jahia.params.ParamBean", jData.getProcessingContext());

        // Seem's that the Thread Param Bean is null when were in Admin ???? So we set it here
        Jahia.setThreadParamBean(jParams);
        return jParams;
    }

    /**
     * @param request
     * @param response
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     * @todo should call Struts' ActionServlet.process() method
     */
    public void process(HttpServletRequest request,
                        HttpServletResponse response)
            throws java.io.IOException, javax.servlet.ServletException {
        try {
            ParamBean jParams =
                    (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
            // Create a JahiaData without loaded containers and fields
            JahiaData jData = new JahiaData(jParams, false);
            jParams.getRequest().setAttribute(JahiaData.JAHIA_DATA, jData);
            super.process(jParams.getRequest(), jParams.getResponse());
        } catch (JahiaException je) {
            logger.debug(je.getMessage(), je);
        }
    }

    /**
     * Get all JahiaSite objects where the user has an access.
     *
     * @param user the user you want to get his access grantes sites list.
     * @return Return a List containing all JahiaSite objects where the user has an access.
     */
    public static List<JahiaSite> getAdminGrantedSites(JahiaUser user)
            throws JahiaException {

        List<JahiaSite> grantedSites = new ArrayList<JahiaSite>();
        Iterator<JahiaSite> sitesList = ServicesRegistry.getInstance().
                getJahiaSitesService().getSites();

        JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();

        while (sitesList.hasNext()) {
            JahiaSite jahiaSite = sitesList.next();
            logger.debug("check granted site " + jahiaSite.getServerName());

            if ((JahiaSiteTools.getAdminGroup(jahiaSite) != null) &&
                    JahiaSiteTools.getAdminGroup(jahiaSite).isMember(user)) {
                logger.debug("granted site for " + jahiaSite.getServerName());
                grantedSites.add(jahiaSite);
            } else if (aclService.getSiteActionPermission("admin.JahiaAdministration", user, JahiaBaseACL.READ_RIGHTS, jahiaSite.getID()) > 0) {
                logger.debug("granted site for " + jahiaSite.getServerName());
                grantedSites.add(jahiaSite);
            }
        }

        return grantedSites;
    }

}
