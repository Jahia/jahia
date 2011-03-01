/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.admin.AdministrationModule;
import org.jahia.admin.AdministrationModulesRegistry;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.MenuItem;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.AdminParamBean;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.applications.ServletIncludeRequestWrapper;
import org.jahia.services.applications.ServletIncludeResponseWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSiteTools;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;


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
public class JahiaAdministration extends HttpServlet {

    private static final long serialVersionUID = 332486402871252316L;

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaAdministration.class);
    
    private static ServletConfig config;
    private static ServletContext context;

    public static final String CLASS_NAME = "org.jahia.bin.JahiaAdministration";
    public static final String JSP_PATH = "/admin/";
    private static final int SUPERADMIN_SITE_ID = 0;

    private static ServicesRegistry sReg;
    private static JahiaUserManagerService uMgr;
    private static JahiaGroupManagerService gMgr;
    static private String servletPath = null;

    private String servletURI = null;
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            process(req, resp);
        } finally {
            Jahia.setThreadParamBean(null);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            process(req, resp);
        } finally {
            Jahia.setThreadParamBean(null);
        }
    }
    
    /**
     * Default service inherited from HttpServlet.
     *
     * @param request  Servlet request (inherited).
     * @param response Servlet response (inherited).
     */
    private void process(HttpServletRequest request,
                        HttpServletResponse response) throws IOException,
            ServletException {
        if (logger.isDebugEnabled()) {
            logger.debug("--[ " + request.getMethod() + " Request Start URI='" +
                    request.getRequestURI() + "' query='" +
                    request.getQueryString() + "'] --");
        }

        // get the current user session...
        HttpSession session = request.getSession(true);

        if (JahiaAdministration.servletPath == null) {
            servletPath = request.getServletPath();
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
                // set Jahia running mode to Admin
                session.setAttribute(ProcessingContext.SESSION_JAHIA_RUNNING_MODE,
                                     Jahia.ADMIN_MODE);
                logger.debug("Running mode : " + Jahia.ADMIN_MODE);

                    try {
                        userRequestDispatcher(request, response, session); // ok continue admin...
                    } catch (JahiaException je) {
                        DefaultErrorHandler.getInstance().handle(je, request, response);
                    } catch (Exception t) {
                        DefaultErrorHandler.getInstance().handle(t, request, response);
                    } finally {
                        Jahia.setThreadParamBean(null);
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


    private static boolean hasServerPermission(String permissionName) {
        try {
            return JCRSessionFactory.getInstance().getCurrentUserSession().getRootNode().hasPermission(permissionName);
        } catch (RepositoryException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean hasSitePermission(String permissionName, final String siteKey) {
        try {
            return JCRSessionFactory.getInstance().getCurrentUserSession().getNode("/sites/"+siteKey).hasPermission(permissionName);
        } catch (RepositoryException e) {
            e.printStackTrace();
            return false;
        }
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
        final JahiaUser user = jParams.getUser();
        final JahiaSite site = jParams.getSite();

        try {
            if (operation.equals("processlogin")) {                     // operation : process login...
                processLogin(request, response, session, null, null);
            } else {
                Boolean accessGranted = (Boolean) session.getAttribute(CLASS_NAME + "accessGranted");
                if (accessGranted == null) {
                    accessGranted = Boolean.FALSE;
                }

                if (accessGranted) {
                    Locale uiLocale = (Locale) request.getSession().getAttribute(ProcessingContext.SESSION_UI_LOCALE);
                    if(request.getParameter("switchUiLocale") != null) {
                        uiLocale = LanguageCodeConverters.languageCodeToLocale(request.getParameter("switchUiLocale"));
                        request.getSession().setAttribute(ProcessingContext.SESSION_UI_LOCALE, uiLocale);
                        user.setProperty("preferredLanguage", uiLocale.toString());
                    }

                    Config.set(request, Config.FMT_LOCALIZATION_CONTEXT,
                            new LocalizationContext(new JahiaResourceBundle(uiLocale, site.getTemplatePackageName()), uiLocale));

                    AdministrationModulesRegistry modulesRegistry = (AdministrationModulesRegistry) SpringContextSingleton.getInstance().getContext().getBean("administrationModulesRegistry");
                    AdministrationModule currentModule = modulesRegistry.getServerAdministrationModule(operation);
                    if (currentModule != null) {
                        if (hasServerPermission(currentModule.getPermissionName())) {
                            session.setAttribute(CLASS_NAME + "configJahia", Boolean.TRUE);
                            /** todo clean up this hardcoded mess. Is it even used anymore ? */
                            if ("sharecomponents".equals(operation) && user.isRoot()) {
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
                            if (hasSitePermission(currentModule.getPermissionName(), site.getSiteKey())) {
                                session.setAttribute(CLASS_NAME + "configJahia", Boolean.FALSE);
                                if ("search".equals(operation)) {
                                    // Use response response wrapper to ensure correct handling of Application fields output to the response
                                    // @todo is this still necessary with Pluto wrapper in effect ?
                                    currentModule.service(new ServletIncludeRequestWrapper(request), new ServletIncludeResponseWrapper(response, true, SettingsBean.getInstance().getCharacterEncoding()));
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

        request.setAttribute("URL", null);
        request.setAttribute("requestURI", request.getRequestURI());

        if (request.getAttribute("title") == null) {
            request.setAttribute("title", "no title");
        }

        // init locale
        Locale defaultLocale = (Locale) session
                .getAttribute(ProcessingContext.SESSION_LOCALE);
//        if (defaultLocale == null) {
//            defaultLocale = request.getLocale() != null ? request.getLocale() : Locale.ENGLISH;
//        }
        session.setAttribute(ProcessingContext.SESSION_LOCALE, defaultLocale);

        String contentTypeStr = "text/html;charset=";
        String charEncoding = org.jahia.settings.SettingsBean.getInstance() != null ? org.jahia.settings.SettingsBean.getInstance()
                .getCharacterEncoding() : "UTF-8";
        contentTypeStr = contentTypeStr + charEncoding;

        request.setAttribute("content-type", contentTypeStr);

        // response no-cache headers...
        response.setHeader("Pragma", "no-cache");
        //response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        if (jData != null) {
            final ParamBean jParams = (ParamBean) jData.getProcessingContext();
            
            if (isValidLoginSession(session)) {
            	initMenu(request,(JahiaUser) session.getAttribute("org.jahia.usermanager.jahiauser"));
            }

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


    public static void initMenu(HttpServletRequest request, JahiaUser user) {
        JahiaData jData = (JahiaData) request
                .getAttribute("org.jahia.data.JahiaData");
        if (jData != null) {
            ParamBean ctx = (ParamBean) jData.getProcessingContext();

            AdministrationModulesRegistry administrationModulesRegistry = (AdministrationModulesRegistry) SpringContextSingleton
                    .getInstance().getContext().getBean(
                            "administrationModulesRegistry");
            ctx.setAttribute("administrationServerModules", getMenuItems(
                    administrationModulesRegistry.getServerModules(), ctx, user));
            ctx.setAttribute("administrationSiteModules", getMenuItems(
                    administrationModulesRegistry.getSiteModules(), ctx, user));
        }
    }

    private static List<MenuItem> getMenuItems(
            List<AdministrationModule> modules, ParamBean ctx, JahiaUser user) {
        List<MenuItem> menuItems = new LinkedList<MenuItem>();
        for (AdministrationModule module : modules) {
            String actionUrl = null;
            try {
                actionUrl = module.getActionURL(ctx);
            } catch (Exception e) {
                logger.error(
                        "Error computing an URL for the administration module '"
                                + module.getName() + "'", e);
            }
            menuItems.add(new MenuItem(module.getName(), actionUrl != null
                    && module.isEnabled(user, ctx.getSiteKey()), module
                    .getLabel(), actionUrl, module.getIcon(), module.getIconSmall(), module
                    .getTooltip(), module.isSelected(ctx),module.getLocalizationContext()));
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
        if (!response.isCommitted()) {
            boolean isNutchCrawler = false;
            for (Enumeration<?> agentEnum = request.getHeaders("user-agent"); agentEnum.hasMoreElements();) {
                String reqUserAgent = (String)agentEnum.nextElement();
                if (reqUserAgent.contains("nutch")) {
                    isNutchCrawler = true;
                    break;
                }
            }
            if (isNutchCrawler) {    
                response.setHeader("WWW-Authenticate", "BASIC realm=\"Secured Jahia tools\"");
                response.sendError(SC_UNAUTHORIZED);
            }    
        }
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
        String jahiaLoginUsername = null;
        String jahiaLoginPassword = null;

        JahiaUser theUser = null;
        JahiaGroup theGroup;
        boolean superAdmin = false;
        int entrySiteID = 0;

        // get form values...
        if (rootName == null && rootPass == null) {
            if (request.getParameter("login_username") != null) {
                jahiaLoginUsername = request.getParameter("login_username").trim();
            }
            if (request.getParameter("login_password") != null) {
                jahiaLoginPassword = request.getParameter("login_password").trim();                
            }
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
                String s = JahiaResourceBundle.getJahiaInternalResource("message.invalidUsernamePassword",
                        request.getLocale());
                logger.warn(s);
                request.setAttribute(JahiaAdministration.CLASS_NAME + "jahiaDisplayMessage", s);
            }

            theGroup = gMgr.getAdministratorGroup(SUPERADMIN_SITE_ID);

            if (theUser != null) {
                if (theUser.verifyPassword(jahiaLoginPassword)) {
                    if (!theUser.isRoot() && Boolean.valueOf(theUser.getProperty("j:accountLocked"))) {
                        logger.debug("Login failed. Account is locked for user " + jahiaLoginUsername);
                        String dspMsg = JahiaResourceBundle.getJahiaInternalResource("message.accountLocked", request.getLocale());
                        request.setAttribute(JahiaAdministration.CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                    } else {
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
                                String dspMsg = JahiaResourceBundle.getJahiaInternalResource("label.isntadministrator1",
                                        request.getLocale());
                                dspMsg += " ";
                                dspMsg += jahiaLoginUsername;
                                dspMsg += " ";
                                dspMsg += JahiaResourceBundle.getJahiaInternalResource("label.isntadministrator2",
                                        request.getLocale());
                                request.setAttribute(JahiaAdministration.CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                                logger.error("Login Error: User " + jahiaLoginUsername + " is not a system administrator.");
                            }
                        }
                    }
                } else {
                    String dspMsg = JahiaResourceBundle.getJahiaInternalResource("message.invalidUsernamePassword",
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
            JCRSessionFactory.getInstance().setCurrentUser(theUser);

            Locale sessionLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_UI_LOCALE);
            session.setAttribute(ProcessingContext.SESSION_UI_LOCALE, sessionLocale != null ? UserPreferencesHelper.getPreferredLocale(theUser, sessionLocale) : UserPreferencesHelper.getPreferredLocale(theUser));
            
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
    public void displayMenu(HttpServletRequest request,
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

        request.setAttribute("site", theSite);
        request.setAttribute("sitesList", grantedSites);
        request.setAttribute("siteID", session.getAttribute(CLASS_NAME + "manageSiteID"));
        request.setAttribute("isSuperAdmin", session.getAttribute(CLASS_NAME + "isSuperAdmin"));
        Object attribute = session.getAttribute(CLASS_NAME + "configJahia");
        request.setAttribute("configJahia", attribute!=null?attribute:Boolean.TRUE);

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
    public void changeSite(HttpServletRequest request,
                                  HttpServletResponse response,
                                  HttpSession session)
            throws IOException, ServletException {
        String newSiteID = request.getParameter("changesite").trim();
        int siteID = Integer.parseInt(newSiteID);

        // check if the user has really admin access to this site...
        JahiaUser theUser = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
        JahiaGroup group = ServicesRegistry.getInstance().getJahiaGroupManagerService().getAdministratorGroup(siteID);
        JahiaSite currentSite = null;
        try {
            currentSite = ServicesRegistry.getInstance()
                    .getJahiaSitesService().getSite(siteID);

            if ((group != null && group.isMember(theUser))
                    || (hasServerPermission("administrationAccess"))) {
                List<Locale> languageSettingsAsLocales = currentSite
                        .getLanguagesAsLocales();
                final Locale localeSession = (Locale) session
                        .getAttribute(ProcessingContext.SESSION_LOCALE);
                if (languageSettingsAsLocales != null
                        && languageSettingsAsLocales.size() > 0
                        && !languageSettingsAsLocales.contains(localeSession)) {
                    Locale locale = languageSettingsAsLocales.get(0);
                    session.setAttribute("org.apache.struts.action.LOCALE", locale);
                    session.setAttribute(ProcessingContext.SESSION_LOCALE,
                            locale);
                }
                session.setAttribute(ProcessingContext.SESSION_SITE,
                        currentSite);
            } else {
                // System.out.println(" --> no admin access on this site <--");
                currentSite = (JahiaSite) session
                        .getAttribute(ProcessingContext.SESSION_SITE);
                siteID = currentSite.getID();
            }
        } catch (JahiaException je) {
            String dspMsg = JahiaResourceBundle
                    .getJahiaInternalResource(
                            "message.invalidUsernamePassword",
                            request.getLocale());
            request.setAttribute(JahiaAdministration.CLASS_NAME
                    + "jahiaDisplayMessage", dspMsg);
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
    public void switchModeAction(HttpServletRequest request,
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
    private static boolean isValidLoginSession(HttpSession session) {

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
                        (hasServerPermission("administrationAccess"))) {
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
        
        Locale forcedLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_UI_LOCALE);
        if (forcedLocale == null && JahiaUserManagerService.isGuest(user)) { 
	        // resolve locale
        	forcedLocale = resolveLocaleForGuest(request);
        }
        
        if (site == null) {
            site = ServicesRegistry.getInstance().getJahiaSitesService().getDefaultSite();
            if (site == null) {
	            // This can occurs when all sites has been deleted ...
	            // create a fake site for JSP using taglibs
	            site = new JahiaSite(-1,
	                    "",
	                    "",
	                    "",
	                    "",
	                    new Properties(), null);
            }
            session.setAttribute(ProcessingContext.SESSION_SITE, site);
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
                context, startTime,
                intRequestMethod,
                site,
                user);

        JahiaData jData = new JahiaData(jParams, false);

        request.setAttribute("org.jahia.data.JahiaData", jData);
        request.setAttribute("org.jahia.params.ParamBean", jData.getProcessingContext());

        // Seem's that the Thread Param Bean is null when were in Admin ???? So we set it here
        Jahia.setThreadParamBean(jParams);
        
        if (forcedLocale != null) {
        	session.setAttribute(ProcessingContext.SESSION_UI_LOCALE, forcedLocale);
        	jParams.setUILocale(forcedLocale);
        }
        
        return jParams;
    }

    private static Locale resolveLocaleForGuest(HttpServletRequest request) {
        List<Locale> availableBundleLocales = LanguageCodeConverters.getAvailableBundleLocales();
        @SuppressWarnings("unchecked")
        Enumeration<Locale> browserLocales = request.getLocales();
        Locale resolvedLocale = availableBundleLocales != null && !availableBundleLocales.isEmpty() ? availableBundleLocales.get(0) : Locale.ENGLISH;
        while (browserLocales != null && browserLocales.hasMoreElements()) {
        	Locale candidate = browserLocales.nextElement();
        	if (candidate != null) {
        		if (availableBundleLocales.contains(candidate)) {
        			resolvedLocale = candidate;
        			break;
        		} else if (StringUtils.isNotEmpty(candidate.getCountry()) && availableBundleLocales.contains(new Locale(candidate.getLanguage()))) {
        			resolvedLocale = new Locale(candidate.getLanguage());
        			break;
        		} 
        	}
        }
	    
	    return resolvedLocale;
    }

	/**
     * Get all JahiaSite objects where the user has an access.
     *
     * @param user the user you want to get his access grantes sites list.
     * @return Return a List containing all JahiaSite objects where the user has an access.
     */
    public List<JahiaSite> getAdminGrantedSites(JahiaUser user)
            throws JahiaException {

        List<JahiaSite> grantedSites = new ArrayList<JahiaSite>();
        Iterator<JahiaSite> sitesList = ServicesRegistry.getInstance().
                getJahiaSitesService().getSites();

        while (sitesList.hasNext()) {
            JahiaSite jahiaSite = sitesList.next();
            logger.debug("check granted site " + jahiaSite.getSiteKey());

            if ((JahiaSiteTools.getAdminGroup(jahiaSite) != null) &&
                    JahiaSiteTools.getAdminGroup(jahiaSite).isMember(user)) {
                logger.debug("granted site for " + jahiaSite.getSiteKey());
                grantedSites.add(jahiaSite);
            } else if (hasSitePermission("administrationAccess", jahiaSite.getSiteKey())) {
                logger.debug("granted site for " + jahiaSite.getSiteKey());
                grantedSites.add(jahiaSite);
            }
        }

        return grantedSites;
    }

}