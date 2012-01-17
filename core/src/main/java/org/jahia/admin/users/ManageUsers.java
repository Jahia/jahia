/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

// $Id$
//

package org.jahia.admin.users;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang.StringUtils;
import org.jahia.admin.AbstractAdministrationModule;
import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.engines.EngineMessages;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.usermanager.*;
import org.jahia.tools.files.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public static final String SEPARATOR = "#";
    public static final String USER_PROPERTY_PREFIX = REQUEST_KEY_PREFIX + "property" + SEPARATOR;

    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

	private static Logger logger = LoggerFactory.getLogger(ManageUsers.class);

    private JahiaUserManagerService userManager;

    ProcessingContext jParams;

    private String userMessage = "";
    private boolean isError = true;

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
            userManager = sReg.getJahiaUserManagerService();
        }

        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        this.jParams = jData.getProcessingContext();

        // continue the execution of user request...
        userRequestDispatcher( request, response, request.getSession() );
    }


    public JahiaUserManagerService getUserManager() {
        return userManager;
    }

    public void setUserManager(JahiaUserManagerService uMgr) {
        this.userManager = uMgr;
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
        } else if (operation.equals("edit")) {
            isSuperAdminProp = false;
            displayUserEdit(request, response, session);
        } else if (operation.equals("superAdminProps")) {
            isSuperAdminProp = true;
            displayUserEdit(request, response, session);
        } else if (operation.equals("remove")) {
            displayUserRemove(request, response, session);
        } else if (operation.equals("processCreate")) {
            if (processUserCreate(request, response)) {
                displayUsers(request, response, session);
            } else {
                displayUserCreate(request, response, session);
            }
        } else if (operation.equals("processEdit")) {
            if (processUserEdit(request)) {
                if (isSuperAdminProp) {
                    displayUserEdit(request, response, session);
                } else {
                    displayUsers(request, response, session);
                }
            } else {
                displayUserEdit(request, response, session);
            }
        } else if (operation.equals("processRemove")) {
            processUserRemove(request, response, session);
        } else if (operation.equals("batchCreate")) {
            displayBatchCreate(request, response, session);
        } else if (operation.equals("processBatchCreate")) {
            processBatchCreate(request, response, session);
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
        request.setAttribute("providerList", userManager.getProviderList());
        Set userSet = PrincipalViewHelper.getSearchResult(request);
        userSet = PrincipalViewHelper.removeJahiaAdministrators(userSet);
        request.setAttribute("resultList", userSet);
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
        Map<String,String> userProperties = new HashMap<String, String>();
        userProperties.put("username", StringUtils.defaultString(request.getParameter("username")));
        userProperties.put("passwd", StringUtils.defaultString(request.getParameter("passwd")));
        userProperties.put("passwdconfirm", StringUtils.defaultString(request.getParameter("passwdconfirm")));

        //userProperties.put("firstname", StringUtils.defaultString(request.getParameter("firstname")));
        //userProperties.put("lastname", StringUtils.defaultString(request.getParameter("lastname")));
        //userProperties.put("email", StringUtils.defaultString(request.getParameter("email")));
        //userProperties.put("organization", StringUtils.defaultString(request.getParameter("organization")));
        Iterator names = new EnumerationIterator(request.getParameterNames ());
        while (names.hasNext()) {
            String name = (String) names.next();
            if (name != null && name.startsWith(USER_PROPERTY_PREFIX)) {
                String newValue = request.getParameter(name);
                int index = name.indexOf(SEPARATOR);
                String key = name.substring(index + 1);
                String currentValue = userProperties.get(key);
                if (newValue == null) {
                    continue;
                }
                if (currentValue == null) {
                    userProperties.put(key, newValue);
                } else if (!currentValue.equals(newValue)) {
                    userProperties.put(key, currentValue);
                }
            }
        }

        // Is here any default home page?
        String homePageParam = request.getParameter("homePageID");
        String homePageTitle = null;

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
     * @return true if operation successed, false otherwise.
     * @throws IOException
     * @throws ServletException
     * @throws JahiaException
     */
    private boolean processUserCreate(HttpServletRequest request,
                                      HttpServletResponse response)
            throws IOException, ServletException, JahiaException {
        // get form values...
        if (!"save".equals(request.getParameter("actionType"))) {
            // in this case we are just refreshing the form, we do not perform
            // any saving or checks.
            return false;
        }
        String username = request.getParameter("username").trim();
        if (username.length() == 0) {
            userMessage = getMessage("org.jahia.admin.userMessage.specifyUserName.label");
            return false;
        }
        String email = request.getParameter("manage-user-property#j:email").trim();
        if(!"".equals(email) && !email.matches("^$|^[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@([A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*|\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])$")){
            userMessage = getMessage("org.jahia.admin.userMessage.emailFormatIsIncorrect.label");
            return false;
        }
        // The following test is really disputable because we should can enter
        // as well accentueted char and any internationalized char.
        else if (!ServicesRegistry.getInstance().getJahiaUserManagerService()
                .isUsernameSyntaxCorrect(username)) {
            userMessage = StringUtils.capitalize(getMessage("org.jahia.admin.users.ManageUsers.onlyCharacters.label"));
            return false;
        } else if (userManager.userExists(username)) {
			userMessage = getMessage("label.user") + " [" + username + "] "
			        + getMessage("org.jahia.admin.userMessage.alreadyExist.label");
            return false;
        }
        JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry.getInstance().getJahiaPasswordPolicyService();
        String passwd = request.getParameter("passwd").trim();
        if ("".equals(passwd)) {
            userMessage = getMessage(
                    "org.jahia.admin.userMessage.specifyPassword.label");
            return false;
        } else {
            String passwdConfirm = request.getParameter("passwdconfirm").trim();
            if (!passwdConfirm.equals(passwd)) {
                userMessage = getMessage(
                        "org.jahia.admin.userMessage.passwdNotMatch.label");
                return false;
            }
            PolicyEnforcementResult evalResult = pwdPolicyService.enforcePolicyOnUserCreate(username, passwd);
            if (!evalResult.isSuccess()) {
                EngineMessages policyMsgs = evalResult.getEngineMessages();
                policyMsgs.saveMessages(((ParamBean) jParams).getRequest());
                return false;
            }
        }
        Properties userProps = new Properties();

        Iterator names = new EnumerationIterator(request.getParameterNames());
        while (names.hasNext()) {
            String name = (String) names.next();
            if (name != null && name.startsWith(USER_PROPERTY_PREFIX)) {
                String newValue = request.getParameter(name);
                int index = name.indexOf(SEPARATOR);
                String key = name.substring(index + 1);
                String currentValue = (String) userProps.get(key);
                if (newValue == null) {
                    continue;
                }
                if (currentValue == null) {
                    userProps.put(key, newValue);
                } else if (!currentValue.equals(newValue)) {
                    userProps.put(key, currentValue);
                }
            }
        }

        JahiaUser usr = userManager.createUser(username, passwd, userProps);
        if (usr == null) {
            userMessage = getMessage("org.jahia.admin.userMessage.unableCreateUser.label") + " " + username;
            return false;
        } else {
            usr = userManager.lookupUser(username);
            userMessage = getMessage("label.user") + " [" + username + "] " + getMessage("message.successfully.created");
            isError = false;
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
            theUser = userManager.lookupUser(userToEdit);
            // Spaces ensure the compatibility format from the user_management.jsp select box
            request.setAttribute("isSuperAdminProp", "");
        } else {
            userToEdit = request.getParameter("selectedUsers");
            if (userToEdit == null) { // Get the last user if none was selected.
                userToEdit = (String)session.getAttribute("selectedUsers");
            }
            if (userToEdit == null || "null".equals(userToEdit)) {
              userMessage = getMessage("org.jahia.admin.userMessage.selectUser.label");
                displayUsers(request, response, session);
                return;
            }
            // Consider actual selected user as the last one and store it in session.
            session.setAttribute("selectedUsers", userToEdit);
            userToEdit = StringUtils.replace(userToEdit, "&nbsp;", " ");
            theUser = userManager.lookupUserByKey(userToEdit.substring(1));
        }
        request.setAttribute("theUser", theUser);


        Set<JahiaGroup> groups = getGroups(theUser);
        // display the edit form with initial values
        request.setAttribute("groups", groups);

        UserProperties userProperties = (UserProperties) theUser.getUserProperties().clone();
        // pick out all the user properties parameters, and set it into the
        // user properties
        Iterator names = new EnumerationIterator(request.getParameterNames ());
        while (names.hasNext()) {
            String name = (String) names.next();
            if (name != null && name.startsWith(USER_PROPERTY_PREFIX)) {
                String newValue = request.getParameter(name);
                int index = name.indexOf(SEPARATOR);
                String key = name.substring(index + 1);
                UserProperty currentProp = userProperties.getUserProperty(key);
                if (newValue == null) {
                    continue;
                }
                if (currentProp == null) {
                    currentProp = new UserProperty(name, newValue, false);
                    userProperties.setUserProperty(key, currentProp);
                } else if (!currentProp.getValue().equals(newValue)) {
                    //TODO: The new data should be validated here!!
                    if (!currentProp.isReadOnly()) {
                        currentProp.setValue(newValue);
                        userProperties.setUserProperty(key, currentProp);
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
     * @return true if operation successed, false otherwise.
     * @throws IOException
     * @throws ServletException
     * @throws JahiaException
     */
    private boolean processUserEdit(HttpServletRequest request)
    throws IOException, ServletException, JahiaException
    {
        logger.debug("Started");
        // get form values...
        String username = request.getParameter("username");
        JahiaUser usr = userManager.lookupUser(username);

        logger.debug("Update user : " + usr.getUserKey());

        // jahia_db usr processing
        if ("update".equals(request.getParameter("actionType").trim())) {
            return false;
        }
        String email = request.getParameter("manage-user-property#j:email");
        email = email != null ? email.trim() : null;
        if(StringUtils.isNotEmpty(email) && !email.matches("^$|^[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@([A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+(\\.[A-Za-z0-9!#$%&\'*+/=?^_`{|}~-]+)*|\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])$")){
            userMessage = getMessage("org.jahia.admin.userMessage.emailFormatIsIncorrect.label");
            return false;
        }
        String passwd = request.getParameter("passwd");
        // passwd may be null in case of an LDAP user.
        if (StringUtils.isNotBlank(passwd)) {
            passwd = passwd.trim();
            JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry
                    .getInstance().getJahiaPasswordPolicyService();
            String passwdConfirm = request.getParameter("passwdconfirm").
                                       trim();
            if (!passwdConfirm.equals(passwd)) {
                userMessage = getMessage("org.jahia.admin.userMessage.passwdNotMatch.label");
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
            if (!usr.setPassword(passwd)) {
                userMessage = getMessage("org.jahia.admin.userMessage.cannotChangePasswd.label");
                userMessage += " [" + username + "] ";
                return false;
            }
        }
        // pick out all the user properties parameters, and set it into the
        // user properties
        Iterator names = new EnumerationIterator(request.getParameterNames());
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

        userMessage = getMessage("label.user");
        userMessage += " [" + username + "] ";
        userMessage += getMessage("message.successfully.updated");
        isError = false;

        return true;
    }

    private Set<JahiaGroup> getGroups(JahiaUser usr) {
        Set<JahiaGroup> groups = new HashSet<JahiaGroup>();
        JahiaGroupManagerService jahiaGroupManagerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        List<String> v = jahiaGroupManagerService.getUserMembership(usr);
        for (String aV : v) {
            JahiaGroup g = jahiaGroupManagerService.lookupGroup(aV);
            if (g != null || usr.isRoot()) {
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
        String[] selectedUsers = request.getParameterValues("selectedUsers");
        if (selectedUsers == null || selectedUsers.length == 0) {
          userMessage = getMessage("org.jahia.admin.userMessage.selectUser.label");
            displayUsers(request, response, session);
        } else {
            // set request attributes...
        	List<String> users = new LinkedList<String>();
        	for (String theUser : selectedUsers) {
	            users.add(theUser.substring(1));
            }
//            request.setAttribute("userReadOnly", JahiaUserManagerService.isGuest(user) || user.isRoot() || Boolean.valueOf(userService.getProvider(user.getProviderName()).isReadOnly()));
            request.setAttribute("selectedUsers", users);
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
        session.setAttribute("selectedUsers", null);
        String[] userNames = request.getParameterValues("username");
        Set<String> deleted = new HashSet<String>();
       	for (String userName : userNames) {
            // try to delete the user and memberships...
            try {
                JahiaUser user = userManager.lookupUser(userName);
                JahiaUser currentUser = (JahiaUser)session.getAttribute(ProcessingContext.SESSION_USER);
                if (!user.getUserKey().equals(currentUser.getUserKey())) {
                    userManager.deleteUser(user);
                    deleted.add(userName);
                  } else {
                      userMessage = getMessage("org.jahia.admin.userMessage.cannotRemoveYourUser.label");
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                userMessage = getMessage("org.jahia.admin.userMessage.cannotRemoveUser.label") + " " + userName + ".";
            }
        }
       	if (userNames.length > 1) {
       		if (!deleted.isEmpty()) {
	            userMessage = getMessage("label.user");
	            userMessage += " [" + StringUtils.join(deleted, ", ") + "] ";
	            userMessage += getMessage("org.jahia.admin.userMessage.removed.label");
	            isError = false;
       		} else {
       			userMessage = getMessage("org.jahia.admin.userMessage.cannotRemoveUser.label") + " " + StringUtils.join(userNames, ", ") + ".";       			
       		}
       	} else {
       		if (userMessage == null && deleted.size() == 1) {
	            userMessage = getMessage("label.user");
	            userMessage += " [" + deleted.iterator().next() + "] ";
	            userMessage += getMessage("org.jahia.admin.userMessage.removed.label");
	            isError = false;
       		}
       	}
        displayUsers( request, response, session);
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
    private void displayBatchCreate(HttpServletRequest    request,
                                   HttpServletResponse   response,
                                   HttpSession           session)
        throws IOException, ServletException, JahiaException {

        logger.debug("Started");
            // set request attributes...
        session.setAttribute("userMessage", userMessage);
        session.setAttribute("isError", isError);
        request.setAttribute("jspSource", JSP_PATH + "user_management/user_batchcreate.jsp");
        request.setAttribute("directMenu", JSP_PATH + "direct_menu.jsp");
        session.setAttribute("jahiaDisplayMessage", Jahia.COPYRIGHT);
        doRedirect(request, response, session, JSP_PATH + "admin.jsp");
        userMessage = "";
        isError = true;
    }

    private Properties buildProperties(List<String> headerElementList, List<String> lineElementList) {
        Properties result = new Properties();
        for (int i=0; i < headerElementList.size(); i++) {
            String currentHeader = headerElementList.get(i);
            String currentValue = lineElementList.get(i);
            if (!"j:nodename".equals(currentHeader) &&
                !"j:password".equals(currentHeader)) {
                result.setProperty(currentHeader.trim(), currentValue);
            }
        }
        return result;
    }

    private void processBatchCreate(HttpServletRequest    request,
                                   HttpServletResponse   response,
                                   HttpSession           session)
    throws IOException, ServletException, JahiaException
    {
        logger.debug("Started");
        long timer = System.currentTimeMillis();

        FileUpload fileUpload = ((ParamBean) jParams).getFileUpload();
        if (fileUpload != null) {
            DiskFileItem fileItem = fileUpload.getFileItems().get("csvFile");
            if (fileItem == null) {
                logger.error("Couldn't find CSV file in form parameter name 'csvFile' aborting batch creation !");
                displayUsers( request, response, session);
                return;
            }
            String csvSeparator = jParams.getParameter("csvSeparator");
            CSVReader csvReader = new CSVReader(new InputStreamReader(fileItem.getInputStream(), "UTF-8"), csvSeparator.charAt(0));
            // the first line contains the column names;
            String[] headerElements = csvReader.readNext();
            List<String> headerElementList = Arrays.asList(headerElements);
            int userNamePos = headerElementList.indexOf("j:nodename");
            int passwordPos = headerElementList.indexOf("j:password");
            if ((userNamePos < 0) || (passwordPos < 0)) {
                logger.error("Couldn't find user name or password column in CSV file, aborting batch creation !");
                displayUsers( request, response, session);
                return;
            }
            String[] lineElements = null;
            int errorsCreatingUsers = 0;
            int usersCreatedSuccessfully = 0;
            JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry.getInstance().getJahiaPasswordPolicyService();
            JahiaUserManagerService userService = ServicesRegistry.getInstance().getJahiaUserManagerService();
            
            while ((lineElements = csvReader.readNext()) != null) {
                List<String> lineElementList = Arrays.asList(lineElements);
                Properties properties = buildProperties(headerElementList, lineElementList);
                String userName = lineElementList.get(userNamePos);
                String password = lineElementList.get(passwordPos);
                if (userService.isUsernameSyntaxCorrect(userName)) {
                    PolicyEnforcementResult evalResult = pwdPolicyService.enforcePolicyOnUserCreate(userName, password);
                    if (evalResult.isSuccess()) {
                    	JahiaUser jahiaUser = userManager.createUser(userName, password, properties);
                    	if (jahiaUser != null) {
                            usersCreatedSuccessfully++;
                            logger.info("Successfully created user {}", userName);
                    	} else {
                            errorsCreatingUsers++;
                            logger.warn("Error creating user {}", userName);
                    	}
                    } else {
                        errorsCreatingUsers++;
                        StringBuilder result = new StringBuilder();
                        for (String msg : evalResult.getTextMessages()) {
                            result.append(msg).append("\n");
                        }
                    	logger.warn("Skipping user {}. Following password policy rules are violated\n{}", userName, result.toString());
                    }
                } else {
                    errorsCreatingUsers++;
                    logger.warn("Username {} is not valid. Skipping user.", userName);
                } 
            }
			userMessage = Integer.toString(usersCreatedSuccessfully)
			        + " "
			        + getMessage("org.jahia.admin.userMessage.usersCreatedSuccessfully.label")
			        + ", "
			        + Integer.toString(errorsCreatingUsers)
			        + " "
			        + getMessage("org.jahia.admin.userMessage.usersNotCreatedBecauseOfErrors.label");
            isError = false;

        }

        logger.info("Batch user create took " + (System.currentTimeMillis() - timer) + " ms");

        displayUsers( request, response, session);
    }

}
