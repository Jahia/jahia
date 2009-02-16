/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

// $Id$
//

package org.jahia.admin.users;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.compass.core.util.StringUtils;

import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.engines.EngineMessages;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.jahia.security.license.License;
import org.jahia.services.events.JahiaEventGeneratorBaseService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPageBaseService;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.*;
import org.jahia.utils.JahiaTools;
import org.jahia.admin.AbstractAdministrationModule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * This class is used by the administration to manage users
 * (add a user, edit and delete) from the Jahia software.
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Alexandre Kraft
 * @author Philippe Martin
 *
 * @version 2.0
 */
public class ManageUsers extends AbstractAdministrationModule {

    public static final String REQUEST_KEY_PREFIX = "manage-user-";
    public static final String REQUEST_PASSWORD_KEY = REQUEST_KEY_PREFIX + "password";
    public static final String REQUEST_PASSWORD_CONFIRMATION_KEY =
            REQUEST_KEY_PREFIX + "passwordConfirmation";

    public static final String SEPARATOR = "#";
    public static final String USER_PROPERTY_PREFIX = REQUEST_KEY_PREFIX + "property" + SEPARATOR;

    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

    private static JahiaUserManagerService uMgr;

    private JahiaSite jahiaSite;

    ProcessingContext jParams;

    private String userMessage = "";
    private boolean isError = true;

    private License coreLicense;

    // This attribute will be set to true if the super admin change its
    // properties, false other wise.
    private static boolean isSuperAdminProp;

    /**
     * Constructor called from JahiaAdministration
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     *
     * @throws Exception
     */
    public void service( HttpServletRequest request,
                        HttpServletResponse response )
    throws Exception
    {
        // get services...
        ServicesRegistry sReg = ServicesRegistry.getInstance();
        if (sReg != null) {
            uMgr = sReg.getJahiaUserManagerService();
        }

        // get the current website. get the jahiaserver if it's null...
        jahiaSite =  (JahiaSite) request.getSession().getAttribute( ProcessingContext.SESSION_SITE );

        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        this.jParams = jData.getProcessingContext();

        coreLicense = Jahia.getCoreLicense();
        if ( coreLicense == null ){
            // set request attributes...
            String dspMsg = JahiaResourceBundle.getAdminResource("org.jahia.admin.JahiaDisplayMessage.invalidLicenseKey.label",
                                               jParams, jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            // redirect...
            doRedirect( request, response, request.getSession(), JSP_PATH + "menu.jsp" );
            return;
        }

        // continue the execution of user request...
        userRequestDispatcher( request, response, request.getSession() );
    }

    /**
     * Default constructor.
     *
     * @throws Exception
     */
    public ManageUsers()
    throws Exception
    {
        ServicesRegistry sReg =  ServicesRegistry.getInstance();
        if (sReg != null) {
            uMgr =  sReg.getJahiaUserManagerService();
        }
    }

    /**
     * This method is used like a dispatcher for user requests.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     *
     * @throws Exception
     */
    private void userRequestDispatcher( HttpServletRequest    request,
                                        HttpServletResponse   response,
                                        HttpSession           session )
    throws Exception
    {
        String operation = request.getParameter("sub");

        if (operation.equals("display")) {
                displayUsers(request, response, session);
        } else if (operation.equals("search")) {
            displayUsers(request, response, session);
        } else if (operation.equals("create")) {
            displayUserCreate(request, response, session);
        } else if (operation.equals("register")) {
            displayRegisterUser(request, response, session);
        } else if (operation.equals("edit")) {
            isSuperAdminProp = false;
            displayUserEdit(request, response, session);
        } else if (operation.equals("superAdminProps")) {
            isSuperAdminProp = true;
            session.setAttribute(JahiaAdministration.CLASS_NAME + "configJahia", Boolean.TRUE);            
            displayUserEdit(request, response, session);
        } else if (operation.equals("remove")) {
            displayUserRemove(request, response, session);
        } else if (operation.equals("processCreate")) {
            if (processUserCreate(request, response, session)) {
                displayUsers(request, response, session);
            } else {
                displayUserCreate(request, response, session);
            }
        } else if (operation.equals("processRegister")) {
            processRegisterUser(request, response, session);
        } else if (operation.equals("processEdit")) {
            if (processUserEdit(request, response, session)) {
                if (isSuperAdminProp) {
                    JahiaAdministration.displayMenu(request, response, session);
                } else {
                    displayUsers(request, response, session);
                }
            } else {
                displayUserEdit(request, response, session);
            }
        } else if (operation.equals("processRemove")) {
            processUserRemove(request, response, session);
        }
    }


    /**
     * Forward the servlet request and servlet response objects, using the request
     * dispatcher (from the ServletContext). Note: please be careful, use only
     * context relative path.
     *
     * @param       request             servlet request.
     * @param       response            servlet response.
     * @param       session             Servlet session for the current user.
     * @param       target              target, context-relative path.
     * @exception   IOException         an I/O exception occured during the process.
     * @exception   ServletException    a servlet exception occured during the process.
     */
    private void doRedirect( HttpServletRequest   request,
                             HttpServletResponse  response,
                             HttpSession          session,
                             String               target )
    throws IOException, ServletException
    {
        try {
            request.setAttribute("jahiaSite",jahiaSite);

            // check null warning msg
            if ( request.getAttribute("warningMsg") == null ) {
                request.setAttribute("warningMsg", "");
            }

            // check null jsp bottom message, and fill in if necessary...
            if ( request.getAttribute("msg") == null ) {
                request.setAttribute("msg", Jahia.COPYRIGHT);
            }

            // check null configuration step title, and fill in if necessary...
            if ( request.getAttribute("title") == null ) {
                request.setAttribute("title", "Manage Users");
            }

            // redirect!
            JahiaAdministration.doRedirect(request, response, session, target);

        } catch (IOException ie) {
            logger.error("Error ", ie);
        } catch (ServletException se) {
            logger.error("Error ", se);
            if (se.getRootCause() != null) {
                logger.error("Error root cause", se.getRootCause());
            }
        }
    }

    /**
     * Display the Jahia users and external (if possible) users management menus
     * including the user search engine.
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     * @throws JahiaException
     */
    private void displayUsers(HttpServletRequest request,
                              HttpServletResponse response,
                              HttpSession session)
    throws IOException, ServletException, JahiaException
    {
        request.setAttribute("providerList", uMgr.getProviderList());
        Set userSet = PrincipalViewHelper.getSearchResult(request, jahiaSite.getID());
        userSet = PrincipalViewHelper.removeJahiaAdministrators(userSet);
        request.setAttribute("resultList", userSet);
        request.setAttribute("currentSite", jahiaSite.getServerName());
        request.setAttribute("jspSource", JSP_PATH + "user_management/user_management.jsp");
        request.setAttribute("directMenu", JSP_PATH + "direct_menu.jsp");
        request.setAttribute("userSearch", JSP_PATH + "user_management/user_search.jsp");
        session.setAttribute("jahiaDisplayMessage", Jahia.COPYRIGHT);
        session.setAttribute("userMessage", userMessage);
        session.setAttribute("isError", isError);
        // Remove any home page definition from the session.
        session.setAttribute("homePageID", null);
        session.setAttribute("homePageLabel", null);
        session.removeAttribute("selectedUsers");
        doRedirect(request, response, session, JSP_PATH + "admin.jsp");
        userMessage = "";
        isError = true;
    }

    /**
     * Display the user creation interface.
     *
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     * @throws JahiaException
     */
    private void displayUserCreate(HttpServletRequest request,
                                   HttpServletResponse response,
                                   HttpSession session)
    throws IOException, ServletException, JahiaException
    {
        logger.debug("Started");
        // Check user limitation according to license.
        int nbUserSite = ServicesRegistry.getInstance().getJahiaUserManagerService().getNbUsers();
        int nbUserLic = Jahia.getUserLimit();
        /** @todo  >= because default user 'root' is not considerated as a real user. */
        if (!(nbUserLic == -1 || nbUserLic >= nbUserSite)) {
          userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.licenseLimited.label",
              jParams, jParams.getLocale());
          userMessage += " " + nbUserLic + " ";
          userMessage += JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.users.label",
             jParams, jParams.getLocale());
            displayUsers(request, response, session);
            return;
        }
        Map userProperties = new HashMap();
        userProperties.put("username", JahiaTools.nnString(request.getParameter("username")));
        userProperties.put("passwd", JahiaTools.nnString(request.getParameter("passwd")));
        userProperties.put("passwdconfirm", JahiaTools.nnString(request.getParameter("passwdconfirm")));

        //userProperties.put("firstname", JahiaTools.nnString(request.getParameter("firstname")));
        //userProperties.put("lastname", JahiaTools.nnString(request.getParameter("lastname")));
        //userProperties.put("email", JahiaTools.nnString(request.getParameter("email")));
        //userProperties.put("organization", JahiaTools.nnString(request.getParameter("organization")));
        Iterator names = new EnumerationIterator(request.getParameterNames ());
        if (names != null) {
            while (names.hasNext ()) {
                String name = (String) names.next ();
                if (name != null && name.startsWith (USER_PROPERTY_PREFIX)) {
                    String newValue = request.getParameter (name);
                    int index = name.indexOf (SEPARATOR);
                    String key = name.substring (index + 1);
                    String currentValue = (String) userProperties.get(key);
                    if (newValue == null) {
                        continue;
                    }
                    if (currentValue == null) {
                        userProperties.put (key, newValue);
                    } else if (!currentValue.equals (newValue)) {
                        userProperties.put (key, currentValue);
                    }
                }
            }
        }

        // Is here any default home page?
        String homePageParam = request.getParameter("homePageID");
        String homePageTitle = null;
        int homePageId = -1;
        if (homePageParam != null && homePageParam.length() > 0) {
            homePageId = Integer.parseInt(homePageParam);
        } else {
            homePageId = jahiaSite.getUserDefaultHomepageDef();
        }
        if (homePageId != -1) {
            try {
                ContentPage contentPage = JahiaPageBaseService
                        .getInstance().lookupContentPage(homePageId, false);
                homePageParam = String.valueOf(homePageId);
                homePageTitle = contentPage.getTitle(jParams
                        .getEntryLoadRequest());
            } catch (JahiaPageNotFoundException e) {
                logger.warn(
                        "Unable to find default home page for new user using page ID '"
                                + homePageId + "'", e);
            }
        }
        request.setAttribute("homePageID", homePageParam);
        request.setAttribute("homePageLabel", homePageTitle);

        // set request attributes...
        request.setAttribute("userProperties", userProperties);
        session.setAttribute("userMessage", userMessage);
        session.setAttribute("isError", isError);
        request.setAttribute("jspSource", JSP_PATH + "user_management/user_create.jsp");
        request.setAttribute("directMenu", JSP_PATH + "direct_menu.jsp");
        session.setAttribute("jahiaDisplayMessage", Jahia.COPYRIGHT);
        doRedirect(request, response, session, JSP_PATH + "admin.jsp");
        userMessage = "";
        isError = true;
    }

    /**
     * Process the user creation interface. If any problem, the "userRequestDispatcher"
     * method is in charge to redisplay the user creation interface.
     * 1) Check for a valid user name.
     * 2) Check for a valid password.
     * 3) Create a new user and put it into the Jahia DB.
     *
     * @param request
     * @param response
     * @param session
     * @return true if operation successed, false otherwise.
     * @throws IOException
     * @throws ServletException
     * @throws JahiaException
     */
    private boolean processUserCreate(HttpServletRequest request,
                                      HttpServletResponse response,
                                      HttpSession session)
            throws IOException, ServletException, JahiaException {
        JahiaSiteUserManagerService siteUser = ServicesRegistry.getInstance().getJahiaSiteUserManagerService();
        // get form values...
        if (!"save".equals(request.getParameter("actionType"))) {
            // in this case we are just refreshing the form, we do not perform
            // any saving or checks.
            return false;
        }
        String username = request.getParameter("username").trim();
        if (username.length() == 0) {
            userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.specifyUserName.label",
                    jParams, jParams.getLocale());
            return false;
        }
        // The following test is really disputable because we should can enter
        // as well accentueted char and any internationalized char.
        else if (!ServicesRegistry.getInstance().getJahiaUserManagerService()
                .isUsernameSyntaxCorrect(username)) {
            userMessage = StringUtils.capitalize(JahiaResourceBundle.getAdminResource("org.jahia.admin.users.ManageUsers.onlyCharacters.label",
                    jParams, jParams.getLocale()));
            return false;
        } else if (siteUser.getMember(jahiaSite.getID(), username) != null) {
            userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.user.label",
                    jParams, jParams.getLocale());
            userMessage += " [" + username + "] ";
            userMessage += JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.alreadyExist.label",
                    jParams, jParams.getLocale());
            return false;
        } else if (uMgr.userExists(username)) {
            JahiaUser user = uMgr.lookupUser(username);
            String url = JahiaAdministration.composeActionURL(request,response,"users","&sub=processRegister&userSelected=" + user.getUserKey());

            userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.user.label",
                    jParams, jParams.getLocale());
            userMessage += " [" + username + "] ";
            userMessage += JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.alreadyExist.label",
                    jParams, jParams.getLocale()) + " ";
            userMessage += JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.onAnotherSite.label", jParams, jParams.getLocale()) + ".";
            userMessage += "&nbsp;<a href=\""+url+"\">" + JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.clickHereToRegister.label", jParams, jParams.getLocale()) + "</a>";
            return false;
        }
        JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry
                .getInstance().getJahiaPasswordPolicyService();
        boolean pwdPolicyEnabled = pwdPolicyService.isPolicyEnabled(jahiaSite
                .getID());
        String passwd = request.getParameter("passwd").trim();
        if ("".equals(passwd)) {
            userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.specifyPassword.label",
                    jParams, jParams.getLocale());
            return false;
        } else {
            String passwdConfirm = request.getParameter("passwdconfirm").trim();
            if (!passwdConfirm.equals(passwd)) {
                userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.passwdNotMatch.label",
                        jParams, jParams.getLocale());
                return false;
            }
            if (pwdPolicyEnabled) {
                PolicyEnforcementResult evalResult = pwdPolicyService
                        .enforcePolicyOnUserCreate(
                                new JahiaDBUser(-1, username, passwd, null,
                                        null), passwd, jahiaSite.getID());
                if (!evalResult.isSuccess()) {
                    EngineMessages policyMsgs = evalResult.getEngineMessages();
                    policyMsgs.saveMessages(((ParamBean) jParams).getRequest());
                    return false;
                }
            }
        }
        Properties userProps = new Properties();

        Iterator names = new EnumerationIterator(request.getParameterNames ());
        if (names != null) {
            while (names.hasNext ()) {
                String name = (String) names.next ();
                if (name != null && name.startsWith (USER_PROPERTY_PREFIX)) {
                    String newValue = request.getParameter (name);
                    int index = name.indexOf (SEPARATOR);
                    String key = name.substring (index + 1);
                    String currentValue = (String) userProps.get(key);
                    if (newValue == null) {
                        continue;
                    }
                    if (currentValue == null) {
                        userProps.put (key, newValue);
                    } else if (!currentValue.equals (newValue)) {
                        userProps.put (key, currentValue);
                    }
                }
            }
        }

        JahiaUser usr = null;
        usr = uMgr.createUser(username, passwd, userProps);
        if (usr == null) {
            userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.unableCreateUser.label",
                    jParams, jParams.getLocale());
            userMessage += " " + username;
            return false;
        } else {
            usr = uMgr.lookupUser(username);
            JahiaSiteUserManagerService siteUserManager =
                    ServicesRegistry.getInstance().getJahiaSiteUserManagerService();
            siteUserManager.addMember(jahiaSite.getID(), usr);
            userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.user.label",
                    jParams, jParams.getLocale());
            userMessage += " [" + username + "] ";
            userMessage += JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.created.label",
                    jParams, jParams.getLocale());
            isError = false;
        }
        // Lookup for home page settings and set it.
        String homePageParam = request.getParameter("homePageID");
        if (homePageParam != null && homePageParam.length() > 0) {
            usr.setHomepageID(Integer.parseInt(homePageParam));
        }

        return true;
    }

    /**
     * Display all user properties including home page redirection if exists.
     *
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     * @throws JahiaException
     */
    private void displayUserEdit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 HttpSession session)
    throws IOException, ServletException, JahiaException
    {
        logger.debug("Started");
        String userToEdit;
        JahiaUser theUser;
        if (isSuperAdminProp) {
            userToEdit = (String)session.getAttribute(JahiaAdministration.CLASS_NAME + "jahiaLoginUsername");
            theUser = (JahiaUser)uMgr.lookupUser(userToEdit);
            // Spaces ensure the compatibility format from the user_management.jsp select box
            request.setAttribute("isSuperAdminProp", "");
        } else {
            userToEdit = (String)request.getParameter("selectedUsers");
            if (userToEdit == null) { // Get the last user if none was selected.
                userToEdit = (String)session.getAttribute("selectedUsers");
            }
            if (userToEdit == null || "null".equals(userToEdit)) {
              userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.selectUser.label",
                  jParams, jParams.getLocale());
                displayUsers(request, response, session);
                return;
            }
            // Consider actual selected user as the last one and store it in session.
            session.setAttribute("selectedUsers", userToEdit);
            userToEdit = JahiaTools.replacePattern(userToEdit, "&nbsp;", " ");
            theUser = (JahiaUser)uMgr.lookupUserByKey(userToEdit.substring(1));
        }
        request.setAttribute("theUser", theUser);


        Set groups = (Set)getGroups(theUser, jahiaSite.getID());
        // display the edit form with initial values
        request.setAttribute("groups", groups);

        UserProperties userProperties = (UserProperties) theUser.getUserProperties().clone();
        // pick out all the user properties parameters, and set it into the
        // user properties
        Iterator names = new EnumerationIterator(request.getParameterNames ());
        if (names != null) {
            while (names.hasNext ()) {
                String name = (String) names.next ();
                if (name != null && name.startsWith (USER_PROPERTY_PREFIX)) {
                    String newValue = request.getParameter (name);
                    int index = name.indexOf (SEPARATOR);
                    String key = name.substring (index + 1);
                    UserProperty currentProp = userProperties.getUserProperty (key);
                    if (newValue == null) {
                        continue;
                    }
                    if (currentProp == null) {
                        currentProp = new UserProperty(name, newValue, false);
                        userProperties.setUserProperty (key, currentProp);
                    } else if (!currentProp.getValue().equals (newValue)) {
                        //TODO: The new data should be validated here!!
                        if (!currentProp.isReadOnly()) {
                            currentProp.setValue(newValue);
                            userProperties.setUserProperty(key, currentProp);
                        }
                    }
                }
            }
        }

        request.setAttribute("passwd", "");
        request.setAttribute("passwdconfirm", "");
        request.setAttribute("userProperties", userProperties);

        // Get the home page
        String homePageParam = request.getParameter("homePageID");
        String homePageTitle = null;
        int homePageId = -1;
        if (homePageParam != null && homePageParam.length() > 0) {
            homePageId = Integer.parseInt(homePageParam);
        } else {
            homePageId = theUser.getHomepageID();
        }
        if (homePageId != -1) {
            try {
                ContentPage contentPage = JahiaPageBaseService
                        .getInstance().lookupContentPage(homePageId, false);
                homePageParam = String.valueOf(homePageId);
                homePageTitle = contentPage.getTitle(jParams
                        .getEntryLoadRequest());
            } catch (JahiaPageNotFoundException e) {
                logger.warn(
                        "Unable to find default home page for new user using page ID '"
                                + homePageId + "'", e);
            }
        }
        request.setAttribute("homePageID", homePageParam);
        request.setAttribute("homePageLabel", homePageTitle);

        request.setAttribute("jspSource", JSP_PATH + "user_management/user_edit.jsp");
        request.setAttribute("directMenu", JSP_PATH + "direct_menu.jsp");
        session.setAttribute("jahiaDisplayMessage", Jahia.COPYRIGHT);
        session.setAttribute("userMessage", userMessage);
        session.setAttribute("isError", isError);
        doRedirect(request, response, session, JSP_PATH + "admin.jsp");
        userMessage = "";
        isError = true;
    }

    /**
     * Process the user edition formular.
     * 1) Check if the user is coming out from Jahia DB
     * 2) Check password validity
     * 3) Set the user properties in DB
     * 4) Set the home page redirection if one is set.
     *
     * @param request
     * @param response
     * @param session
     * @return true if operation successed, false otherwise.
     * @throws IOException
     * @throws ServletException
     * @throws JahiaException
     */
    private boolean processUserEdit(HttpServletRequest request,
                                    HttpServletResponse response,
                                    HttpSession session)
    throws IOException, ServletException, JahiaException
    {
        logger.debug("Started");
        // get form values...
        String username = request.getParameter("username");
        JahiaUser usr;
        if (isSuperAdminProp) {
            usr = (JahiaUser)uMgr.lookupUser(username);
        } else {
            usr = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getMember(jahiaSite.getID(), username);
        }
        logger.debug("Update user : " + usr.getUserKey());

        // jahia_db usr processing
        if ("update".equals(request.getParameter("actionType").trim())) {
            return false;
        }
        String passwd = request.getParameter("passwd");
        // passwd may be null in case of an LDAP user.
        if (passwd != null) {
            passwd = passwd.trim();
            JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry
                    .getInstance().getJahiaPasswordPolicyService();
            if (!"".equals(passwd)) {
                String passwdConfirm = request.getParameter("passwdconfirm").
                                       trim();
                if (!passwdConfirm.equals(passwd)) {
                    userMessage = JahiaResourceBundle.getAdminResource(
                        "org.jahia.admin.userMessage.passwdNotMatch.label",
                        jParams, jParams.getLocale());
                    return false;
                }
            }
            if (!"".equals(passwd) && !usr.setPassword(passwd)) {
                userMessage = JahiaResourceBundle.getAdminResource(
                    "org.jahia.admin.userMessage.cannotChangePasswd.label",
                    jParams, jParams.getLocale());
                userMessage += " [" + username + "] ";
                return false;
            }
            if (pwdPolicyService.isPolicyEnabled(usr)) {
                PolicyEnforcementResult evalResult = pwdPolicyService.enforcePolicyOnPasswordChange(usr, passwd, false);
                if (!evalResult.isSuccess()) {
                    EngineMessages policyMsgs = evalResult.getEngineMessages();
                    policyMsgs.saveMessages(((ParamBean) jParams).getRequest());
                    return false;
                }
            }
        }
        // pick out all the user properties parameters, and set it into the
        // user properties
        Iterator names = new EnumerationIterator(request.getParameterNames());
        if (names != null) {
            while (names.hasNext()) {
                String name = (String) names.next();
                if (name != null && name.startsWith(USER_PROPERTY_PREFIX)) {
                    String newValue = request.getParameter(name);
                    int index = name.indexOf(SEPARATOR);
                    String key = name.substring(index + 1);
                    UserProperty currentProp = usr.getUserProperty(key);
                    if (newValue == null) {
                        continue;
                    }
                    if (currentProp == null) {
                        usr.setProperty(key, newValue);
                    } else if (!currentProp.getValue().equals(newValue)) {
                        //TODO: The new data should be validated here!!
                        if (!currentProp.isReadOnly()) {
                            usr.setProperty(key, newValue);
                        }
                    }
                }
            }
        }

        // Lookup for home page settings and set it.
        String homePageParam = request.getParameter("homePageID");
        usr
                .setHomepageID(homePageParam != null
                        && homePageParam.length() > 0 ? Integer
                        .parseInt(homePageParam) : -1);

        userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.user.label",
            jParams, jParams.getLocale());
        userMessage += " [" + username + "] ";
        userMessage += JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.updated.label",
            jParams, jParams.getLocale());
        isError = false;

        // Nicol�s Charczewski - Neoris Argentina - added 28/03/2006 - Begin
        JahiaEvent je = new JahiaEvent(this, jParams, usr);
        JahiaEventGeneratorBaseService.getInstance().fireUpdateUser(je);
        // Nicol�s Charczewski - Neoris Argentina - added 28/03/2006 - End
        
        return true;
    }

    private Set getGroups(JahiaUser usr, int site) {
        Set groups = new HashSet();
        JahiaGroupManagerService jahiaGroupManagerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        List v = jahiaGroupManagerService.getUserMembership(usr);
        Iterator en = v.iterator();
        while (en.hasNext()) {
            String gname = (String) en.next();
            JahiaGroup g = jahiaGroupManagerService.lookupGroup(gname);
            if (g != null && (usr.isRoot() || g.getSiteID() == site)) {
                groups.add(g);
            }
        }
        return groups;
    }

    /**
     * Display confirmation message for removing or not a Jahia user.
     * External user cannot be removed actualy.
     *
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     * @throws JahiaException
     */
    private void displayUserRemove(HttpServletRequest    request,
                                   HttpServletResponse   response,
                                   HttpSession           session)
        throws IOException, ServletException, JahiaException {

        logger.debug("Started");
        String selectedUsers = request.getParameter("selectedUsers");
        selectedUsers = JahiaTools.replacePattern(selectedUsers, "&nbsp;", " ");
        if (selectedUsers == null || "null".equals(selectedUsers)) {
          userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.selectUser.label",
              jParams, jParams.getLocale());
            displayUsers(request, response, session);
        } else {
            // set request attributes...
            request.setAttribute("selectedUsers", selectedUsers);
            session.setAttribute("userMessage", userMessage);
            session.setAttribute("isError", isError);
            request.setAttribute("jspSource", JSP_PATH + "user_management/user_remove.jsp");
            request.setAttribute("directMenu", JSP_PATH + "direct_menu.jsp");
            session.setAttribute("jahiaDisplayMessage", Jahia.COPYRIGHT);
            doRedirect(request, response, session, JSP_PATH + "admin.jsp");
            userMessage = "";
            isError = true;
        }
    }

    private void displayRegisterUser(HttpServletRequest request,
                                     HttpServletResponse response,
                                     HttpSession session)
            throws IOException, ServletException, JahiaException {
        //logger.debug("display select existant administrator started ");

        request.getSession().setAttribute("admin","create");

        // retrieve previous form values...
        String selectedSite = (String) request.getAttribute("selectedSite");

        String jahiaDisplayMessage = Jahia.COPYRIGHT;

        if (selectedSite == null) {
            if (request.getParameter("site") == null) {
                selectedSite = "0";
            } else {
                selectedSite = request.getParameter("site");
            }
        }

        try {
            JahiaSitesService jahiaSitesService = ServicesRegistry.getInstance().getJahiaSitesService();
            JahiaSiteUserManagerService siteUser = ServicesRegistry.getInstance().getJahiaSiteUserManagerService();

            // get admins list...
            Iterator allSites = jahiaSitesService.getSites();
            List sitesList = new ArrayList();
            Integer siteIDInteger = new Integer(selectedSite);

            // clean sites...
            while (allSites.hasNext()) {
                JahiaSite site = (JahiaSite) allSites.next();
                if (site.getID() > 0 && site.getID() != jParams.getSiteID()) {
                    sitesList.add(site);
                }
            }

            // set the Iterator to null if the List is empty...
            if (sitesList.size() == 0) {
                allSites = null;
            } else {
                Locale defaultLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_LOCALE);
                if (defaultLocale != null) {
                    Collections.sort(sitesList, JahiaSite.getTitleComparator(defaultLocale));
                } else {
                    Collections.sort(sitesList, JahiaSite.getTitleComparator());
                }
                allSites = sitesList.iterator();
            }

            // get users... only if allSites is not null...
            if ((allSites != null) && (!selectedSite.equals("0"))) {
                List allUser = new ArrayList();

                List users = siteUser.getMembers(siteIDInteger);
                for (Iterator iterator = users.iterator(); iterator.hasNext();) {
                    JahiaUser user = (JahiaUser) iterator.next();
                    if (siteUser.getMember(jParams.getSiteID(),user.getUsername()) == null) {
                        Map adminHash = new HashMap();
                        adminHash.put("key", user.getUserKey());
                        adminHash.put("username", user.getUsername());
                        allUser.add(adminHash);
                    }
                }

                request.setAttribute("usersList", allUser.iterator());
            }

            // set attributes...
            session.setAttribute("jahiaDisplayMessage", jahiaDisplayMessage);
            request.setAttribute("allSites", allSites);
            request.setAttribute("allSitesJS", sitesList.iterator());
            request.setAttribute("selectedSite", new Integer(selectedSite));

            // redirect...
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "user_management/site_existant_user.jsp");
        } catch (JahiaException je) {
            displayUsers(request, response, session);
        }

        // set default values...
        session.setAttribute("jahiaDisplayMessage", Jahia.COPYRIGHT);
    } // end displaySelectExistantAdmin

    private void processRegisterUser(HttpServletRequest request,
                                                HttpServletResponse response,
                                                HttpSession session)
            throws IOException, ServletException, JahiaException {

        String userkey = request.getParameter("userSelected");
        if (userkey != null) {
            JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userkey);
            JahiaSiteUserManagerService siteUser = ServicesRegistry.getInstance().getJahiaSiteUserManagerService();
            siteUser.addMember(jParams.getSiteID(), user);
        }
        
        displayUsers(request, response, session);
    } // end processExistantAdminSelectSite


    /**
     * Process the Jahia user DB removing.
     * CAUTION ! The user "guest" cannot be removed.
     *
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     * @throws JahiaException
     */
    private void processUserRemove(HttpServletRequest    request,
                                   HttpServletResponse   response,
                                   HttpSession           session)
    throws IOException, ServletException, JahiaException
    {
        logger.debug("Started");

        session.setAttribute("selectedUsers", null);
        String userName = request.getParameter("username");
        if (!userName.equals("guest")) {
            // try to delete the user and memberships...
            try {
                JahiaUser user = uMgr.lookupUser(userName);
                JahiaUser currentUser = (JahiaUser)session.getAttribute(ProcessingContext.SESSION_USER);
                if (!user.getUserKey().equals(currentUser.getUserKey())) {
                    JahiaSiteUserManagerService siteUserManager =
                        ServicesRegistry.getInstance().getJahiaSiteUserManagerService();
                    siteUserManager.removeMember(jParams.getSiteID(),user);
//                    uMgr.deleteUser(user);
                    userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.user.label",
                        jParams, jParams.getLocale());
                    userMessage += " [" + userName + "] ";
                    userMessage += JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.removed.label",
                        jParams, jParams.getLocale());
                    isError = false;

                    // Nicol�s Charczewski - Neoris Argentina - added 28/03/2006 - Begin
                    JahiaEvent je = new JahiaEvent(this, jParams, user);
                    JahiaEventGeneratorBaseService.getInstance().fireDeleteUser(je);
                    // Nicol�s Charczewski - Neoris Argentina - added 28/03/2006 - End

                  } else {
                  userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.cannotRemoveYourUser.label",
                      jParams, jParams.getLocale());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
              userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.cannotRemoveUser.label",
                  jParams, jParams.getLocale());
              userMessage += " " + userName + ".";
            }
        } else {
          userMessage = JahiaResourceBundle.getAdminResource("org.jahia.admin.userMessage.cannotRemoveGuest.label",
              jParams, jParams.getLocale());
        }
        displayUsers( request, response, session);
    }

    private static org.apache.log4j.Logger logger =
             org.apache.log4j.Logger.getLogger(ManageUsers.class);

}
