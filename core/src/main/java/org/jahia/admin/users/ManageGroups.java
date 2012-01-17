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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.*;
import org.jahia.admin.AbstractAdministrationModule;
import org.jahia.services.usermanager.jcr.JCRGroup;

/**
 * This class is used by the administration to manage groups
 * (add a user to a group, and for adding, editing and deleting groups) in
 * Jahia portal.
 * <p/>
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author AK
 * @author MJ
 * @author MAP
 * @version 2.0
 */

public class ManageGroups extends AbstractAdministrationModule {

    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;
    private static final String GROUP_MEMBERS = "org.jahia.admin.manage.groups.group.members";
    private static JahiaUserManagerService uMgr;
    private static JahiaGroupManagerService gMgr;
    private JahiaSite jahiaSite;
    private String groupMessage = "";
    private boolean isError = true;

    ProcessingContext jParams;

    /**
     * Default constructor.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @throws Exception
     */
    public void service(HttpServletRequest request,
                        HttpServletResponse response)
            throws Exception {
        // get services...
        ServicesRegistry sReg = ServicesRegistry.getInstance();
        if (sReg != null) {
            uMgr = sReg.getJahiaUserManagerService();
            gMgr = sReg.getJahiaGroupManagerService();
        }
        // get the current website. get the jahiaserver if it's null...
        jahiaSite = (JahiaSite) request.getSession().getAttribute(ProcessingContext.SESSION_SITE);
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        this.jParams = jData.getProcessingContext();

        if (jahiaSite == null) {
            JahiaSitesService sitesService = sReg.getJahiaSitesService();
            jahiaSite = sitesService.getSite(0);
            request.getSession().setAttribute(ProcessingContext.SESSION_SITE, jahiaSite);
        }
        userRequestDispatcher(request, response, request.getSession());
    } // end constructor


    /**
     * This method is used like a dispatcher for user requests.
     *
     * @param request
     * @param response
     * @param session
     * @throws Exception
     */
    private void userRequestDispatcher(HttpServletRequest request,
                                       HttpServletResponse response,
                                       HttpSession session)
            throws Exception {
        String operation = request.getParameter("sub");

        if (operation.equals("display")) {
            displayGroupList(request, response, session);
        } else if (operation.equals("search")) {
            displayGroupList(request, response, session);
        } else if (operation.equals("create")) {
            displayGroupCreate(request, response, session);
        } else if (operation.equals("edit")) {
            displayGroupEdit(request, response, session);
        } else if (operation.equals("membership")) {
            displayGroupMembership(request, response, session);
        } else if (operation.equals("copy")) {
            displayGroupCopy(request, response, session);
        } else if (operation.equals("remove")) {
            displayGroupRemove(request, response, session);
        } else if (operation.equals("processCreate")) {
            if (processGroupCreate(request, response, session)) {
                displayGroupList(request, response, session);
            } else {
                displayGroupCreate(request, response, session);
            }
        } else if (operation.equals("processEdit")) {
            if (processGroupEdit(request, response, session)) {
                displayGroupList(request, response, session);
            } else {
                displayGroupEdit(request, response, session);
            }
        } else if (operation.equals("processRemove")) {
            processGroupRemove(request, response, session);
        } else if (operation.equals("processCopy")) {
            if (processGroupCopy(request, response, session)) {
                displayGroupList(request, response, session);
            } else {
                displayGroupCopy(request, response, session);
            }
        }
    }

    /**
     * Forward the servlet request and servlet response objects, using the request
     * dispatcher (from the ServletContext). Note: please be careful, use only
     * context relative path.
     *
     * @param request
     * @param response
     * @param session
     * @param target   context-relative path.
     * @throws IOException
     * @throws ServletException
     */
    private void doRedirect(HttpServletRequest request,
                            HttpServletResponse response,
                            HttpSession session,
                            String target)
            throws IOException, ServletException {
        try {
            request.setAttribute("currentSiteBean", jahiaSite);

            // check null warning msg
            if (request.getAttribute("warningMsg") == null) {
                request.setAttribute("warningMsg", "");
            }

            // check null jsp bottom message, and fill in if necessary...
            if (request.getAttribute("msg") == null) {
                request.setAttribute("msg", Jahia.COPYRIGHT);
            }

            if (request.getAttribute("focus") == null) {
                request.setAttribute("focus", "-none-");
            }

            // check null configuration step title, and fill in if necessary...
            if (request.getAttribute("title") == null) {
                request.setAttribute("title", "Manage Groups");
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
     * Display the group list.
     *
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     */
    private void displayGroupList(HttpServletRequest request,
                                  HttpServletResponse response,
                                  HttpSession session)
            throws IOException, ServletException {
        // get list of groups...
        request.setAttribute("providerList", gMgr.getProviderList());
        request.setAttribute("resultList", PrincipalViewHelper.getGroupSearchResult(request, jahiaSite.getID()));
        request.setAttribute("currentSite", jahiaSite.getSiteKey());
        request.setAttribute("jspSource", JSP_PATH + "group_management/group_management.jsp");
        request.setAttribute("directMenu", JSP_PATH + "direct_menu.jsp");
        request.setAttribute("groupSearch", JSP_PATH + "group_management/group_search.jsp");
        session.setAttribute("jahiaDisplayMessage", Jahia.COPYRIGHT);
        session.setAttribute("groupMessage", groupMessage);
        session.setAttribute("isError", isError);
        session.removeAttribute(GROUP_MEMBERS);
        doRedirect(request, response, session, JSP_PATH + "admin.jsp");
        groupMessage = "";
        isError = true;
    }

    /**
     * Display a form for creating a group.
     *
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     * @throws JahiaException
     */
    private void displayGroupCreate(HttpServletRequest request,
                                    HttpServletResponse response,
                                    HttpSession session)
            throws IOException, ServletException, JahiaException {
        logger.debug("Started");
        request.setAttribute("groupName", StringUtils.defaultString(request.getParameter("groupName")));

        request.setAttribute("jspSource", JSP_PATH + "group_management/group_create.jsp");
        request.setAttribute("directMenu", JSP_PATH + "direct_menu.jsp");
        session.setAttribute("groupMessage", groupMessage);
        session.setAttribute("isError", isError);
        session.setAttribute("jahiaDisplayMessage", Jahia.COPYRIGHT);
        doRedirect(request, response, session, JSP_PATH + "admin.jsp");
        groupMessage = "";
        isError = true;
    }

    /**
     * Create the new group in the jahia DB.
     *
     * @param request
     * @param response
     * @param session
     * @return true if group successfully created.
     * @throws IOException
     * @throws ServletException
     * @throws JahiaException
     */
    private boolean processGroupCreate(HttpServletRequest request,
                                       HttpServletResponse response,
                                       HttpSession session)
            throws IOException, ServletException, JahiaException {
        String groupName = (String) request.getParameter("groupName").trim();
        if (groupName.length() == 0) {
            groupMessage = getMessage("org.jahia.admin.groupMessage.specifyGroupName.label");
            return false;
        }
        // Does the introduced groupName contain some errors ?
        if (groupName.length() == 0) {
            groupMessage = getMessage("org.jahia.admin.groupMessage.specifyGroupName.label");
            return false;
        } else if (!ServicesRegistry.getInstance().getJahiaGroupManagerService()
                .isGroupNameSyntaxCorrect(groupName)) {
            groupMessage = getMessage(
                    "org.jahia.admin.users.ManageGroups.groupName.label")
                    + ": "
                    + getMessage("org.jahia.admin.users.ManageGroups.onlyCharacters.label");
            return false;
        } else if (gMgr.groupExists(jahiaSite.getID(), groupName)) {
            groupMessage = getMessage("label.group");
            groupMessage += " [" + groupName + "] ";
            groupMessage += getMessage("org.jahia.admin.groupMessage.alreadyExist.label");
            return false;
        }
        // try to create the new group...
        JahiaGroup grp = createGroup(session, groupName);
        if (grp == null) {
            groupMessage = getMessage("org.jahia.admin.groupMessage.unableCreateGroup.label");
            groupMessage += " " + groupName;
            return false;
        } else {
            groupMessage = getMessage("label.group");
            groupMessage += " [" + groupName + "] ";
            groupMessage += getMessage("message.successfully.created");
            isError = false;
        }
        return true;
    }

    /**
     * Display group members and home page
     *
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     * @throws JahiaException
     */
    private void displayGroupEdit(HttpServletRequest request,
                                  HttpServletResponse response,
                                  HttpSession session)
            throws IOException, ServletException, JahiaException {
        logger.debug("Started");
        String groupToEdit = request.getParameter("selectedGroup");
        if (groupToEdit == null) { // Get the last group if none was selected.
            groupToEdit = (String) session.getAttribute("selectedGroup");
        }
        if (groupToEdit == null || "null".equals(groupToEdit)) {
            groupMessage = getMessage("org.jahia.admin.groupMessage.selectGroup.label");
            displayGroupList(request, response, session);
            return;
        }
        // Consider actual selected group as the last one and store it in session.
        session.setAttribute("selectedGroup", groupToEdit);
        JahiaGroup theGroup = (JahiaGroup) gMgr.lookupGroup(jahiaSite.getID(), groupToEdit);
        //predrag
        session.setAttribute("providerName", theGroup.getProviderName());
        //end predrag

//        if (JahiaPasswordPolicyService.getInstance().isPolicyEnforcementEnabled()) {
//        	request.setAttribute("enforcePasswordPolicyForSite", Boolean.TRUE);
//            boolean disablePasswordPolicyForGroup = StringUtils
//                    .equals(
//                            "false",
//                            theGroup
//                                    .getProperty(JahiaGroup.PROPERTY_ENFORCE_PASSWORD_POLICY));
//            request.setAttribute(JahiaGroup.PROPERTY_ENFORCE_PASSWORD_POLICY,
//                    disablePasswordPolicyForGroup ? "false" : "true");
//        }

        if ("update".equals(request.getParameter("actionType"))) {
            // let's recuperate the members of the group from the selection box
            // if we are re-displaying the same form.
            String[] newMembersList = (String[]) request.getParameterValues("selectMember");
            // convert to HashSet
            if (newMembersList != null) {
                Set<JahiaUser> updatedGroupSet = new HashSet<JahiaUser>();
                for (int i = 0; i < newMembersList.length; i++) {
                    // remove identifier type ("u " or "g " and provider) for future use.
                    JahiaUser usr = uMgr.lookupUserByKey(newMembersList[
                            i].substring(1));
                    updatedGroupSet.add(usr);
                }
                // display the edit form with initial values
                request.setAttribute("groupMembers", updatedGroupSet); //usersViewHelper.getUserListForDisplay(groupMembers));
            } else {
                request.setAttribute("groupMembers", new HashSet<JahiaUser>()); //usersViewHelper.getUserListForDisplay(groupMembers));
            }
        } else {
            Set<Principal> groupMembers = getGroupMembers(groupToEdit, jahiaSite.getID());
            session.setAttribute(GROUP_MEMBERS,groupMembers);
            // display the edit form with initial values
            request.setAttribute("groupMembers", groupMembers); //usersViewHelper.getUserListForDisplay(groupMembers));
        }

        request.setAttribute("jspSource", JSP_PATH + "group_management/group_edit.jsp");
        request.setAttribute("directMenu", JSP_PATH + "direct_menu.jsp");
        session.setAttribute("jahiaDisplayMessage", Jahia.COPYRIGHT);
        session.setAttribute("groupMessage", groupMessage);
        session.setAttribute("isError", isError);
        doRedirect(request, response, session, JSP_PATH + "admin.jsp");
        groupMessage = "";
        isError = true;
    }

    /**
     * Process modifications to the group previously edited.
     *
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     * @throws JahiaException
     */
    private boolean processGroupEdit(HttpServletRequest request,
                                     HttpServletResponse response,
                                     HttpSession session)
            throws IOException, ServletException, JahiaException {
        logger.debug("Started");
        String groupName = request.getParameter("groupName");
        JahiaGroup grp = gMgr.lookupGroup(jahiaSite.getID(), groupName);

        if ("update".equals(request.getParameter("actionType"))) {
            return false;
        }

//        boolean enforcePasswordPolicyForSite = true;
//        if (enforcePasswordPolicyForSite) {
//            boolean enforcePasswordPolicyForGroup = StringUtils
//                    .equals(
//                            "true",
//                            (String) request
//                                    .getParameter(JahiaGroup.PROPERTY_ENFORCE_PASSWORD_POLICY));
//
//            boolean enforePasswordPolicyOld = !StringUtils.equals("false", grp
//                    .getProperty(JahiaGroup.PROPERTY_ENFORCE_PASSWORD_POLICY));
//
//            if (enforcePasswordPolicyForGroup != enforePasswordPolicyOld) {
//                if (enforcePasswordPolicyForGroup) {
//                    grp
//                            .removeProperty(JahiaGroup.PROPERTY_ENFORCE_PASSWORD_POLICY);
//                } else {
//                    grp
//                            .setProperty(
//                                    JahiaGroup.PROPERTY_ENFORCE_PASSWORD_POLICY,
//                                    "false");
//                }
//            }
//        }

        // let's recuperate the members of the group from the selection box
        String[] newMembersList = (String[]) request.getParameterValues("selectMember");
        // convert to HashSet
        Set<Principal> candidateMembers = new HashSet<Principal>();
        if (newMembersList != null) {
            for (String aNewMembersList : newMembersList) {
                // remove identifier type ("u " or "g " and provider) for future use.
                JahiaPrincipal usr;
                String key = aNewMembersList.substring(1);
                if(aNewMembersList.charAt(0)=='u') {
                    usr = uMgr.lookupUserByKey(key);
                } else {
                    usr = gMgr.lookupGroup(key);
                }
                candidateMembers.add(usr);
            }
        }
        // Update group members
        if (candidateMembers.size() > 0) {
            try { // FIXME : Is here a way to optmize these pointer to method ?
                Set<Principal> groupMembers = (Set<Principal>) session.getAttribute(GROUP_MEMBERS);
                // Is there any new members to the original groupMembers
                addRemoveGroupMembers(groupMembers, candidateMembers,
                        JahiaGroup.class.getMethod("addMember", new Class[]{Principal.class}), grp, jParams);
                // Is there any removed members from the original groupMembers
                addRemoveGroupMembers(candidateMembers, groupMembers,
                        JahiaGroup.class.getMethod("removeMember", new Class[]{Principal.class}), grp, jParams);
            } catch (NoSuchMethodException nsme) {
                logger.debug("Error ", nsme);
            }
        } else {
            Set<Principal> groupMembers = (Set<Principal>) session.getAttribute(GROUP_MEMBERS);
            // No member in the select box, all members have to be removed
            for (Iterator<Principal> it = groupMembers.iterator(); it.hasNext();) {
                Principal jahiaUser = it.next();
                grp.removeMember(jahiaUser);
            }
        }
        session.removeAttribute(GROUP_MEMBERS);
        groupMessage = getMessage("label.group");
        groupMessage += " [" + groupName + "] ";
        groupMessage += getMessage("message.successfully.updated");
        isError = false;

        return true;
    }

    /**
     * Display group members defining from other sites
     *
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     */
    private void displayGroupMembership(HttpServletRequest request,
                                        HttpServletResponse response,
                                        HttpSession session)
            throws IOException, ServletException {
        String selectedGroup = request.getParameter("selectedGroup");
        if (selectedGroup == null) {
            groupMessage = getMessage("org.jahia.admin.groupMessage.selectGroup.label");
            displayGroupList(request, response, session);
        } else {
            request.setAttribute("groupName", selectedGroup);
            List<String> groupMembership = getGroupMembership(selectedGroup, jahiaSite.getID());
            request.setAttribute("groupMembership", groupMembership);

            request.setAttribute("jspSource", JSP_PATH + "group_management/group_view.jsp");
            request.setAttribute("directMenu", JSP_PATH + "direct_menu.jsp");
            session.setAttribute("jahiaDisplayMessage", Jahia.COPYRIGHT);
            doRedirect(request, response, session, JSP_PATH + "admin.jsp");
            groupMessage = "";
            isError = true;
        }
    }

    /**
     * Display the form permitting to copy the selected group.
     *
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     */
    private void displayGroupCopy(HttpServletRequest request,
                                  HttpServletResponse response,
                                  HttpSession session)
            throws IOException, ServletException {
        logger.debug("Started");
        request.setAttribute("newGroup", StringUtils.defaultString(request.getParameter("newGroup")));
        String selectedGroup = request.getParameter("selectedGroup");
        if (selectedGroup == null) { // Get the last group if none was selected.
            selectedGroup = (String) session.getAttribute("selectedGroup");
        }
        if (selectedGroup == null || "null".equals(selectedGroup)) {
            groupMessage = "Please select a group in the select box";
            displayGroupList(request, response, session);
            return;
        }
        // Consider actual selected group as the last one and store it in session.
        session.setAttribute("selectedGroup", selectedGroup);
        session.setAttribute("groupMessage", groupMessage);
        session.setAttribute("isError", isError);

        request.setAttribute("jspSource", JSP_PATH + "group_management/group_copy.jsp");
        request.setAttribute("directMenu", JSP_PATH + "direct_menu.jsp");
        session.setAttribute("jahiaDisplayMessage", Jahia.COPYRIGHT);
        doRedirect(request, response, session, JSP_PATH + "admin.jsp");
        groupMessage = "";
        isError = true;
    }

    /**
     * Make a copy of a group.
     *
     * @param request
     * @param response
     * @param session
     * @return true if group successfully copied.
     * @throws IOException
     * @throws ServletException
     */
    private boolean processGroupCopy(HttpServletRequest request,
                                     HttpServletResponse response,
                                     HttpSession session)
            throws IOException, ServletException {
        String groupName = (String) request.getParameter("newGroup");
        String sourceGroupName = (String) session.getAttribute("selectedGroup");
        // Does the introduced groupName contain some errors ?
        if (groupName.length() == 0) {
            groupMessage = getMessage("org.jahia.admin.groupMessage.specifyGroupName.label");
            return false;
        } else if (!ServicesRegistry.getInstance().getJahiaGroupManagerService()
                .isGroupNameSyntaxCorrect(groupName)) {
            groupMessage = getMessage("org.jahia.admin.users.ManageGroups.groupName.label")
                    + ": "
                    + getMessage("org.jahia.admin.users.ManageGroups.onlyCharacters.label");
            return false;
        } else if (gMgr.groupExists(jahiaSite.getID(), groupName)) {
            groupMessage = getMessage("label.group");
            groupMessage += " [" + groupName + "] ";
            groupMessage += getMessage("org.jahia.admin.groupMessage.alreadyExist.label");
            return false;
        }

        // Try to create the new group
        if (createGroup(session, groupName) == null) {
            groupMessage = getMessage("org.jahia.admin.groupMessage.unableCreateGroup.label");
            return false;
        } else {
            JahiaGroup theNewGroup = gMgr.lookupGroup(jahiaSite.getID(), groupName);
            groupMessage = getMessage("label.group");
            groupMessage += " [" + groupName + "] ";
            groupMessage += getMessage("message.successfully.created");
            isError = false;
            for (Principal member : getGroupMembers(sourceGroupName, jahiaSite.getID())) {
                theNewGroup.addMember(member);
            }
        }
        return true;
    }

    /**
     * Display a confirmation form to remove a group
     *
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     */
    private void displayGroupRemove(HttpServletRequest request,
                                    HttpServletResponse response,
                                    HttpSession session)
            throws IOException, ServletException {
        String selectedGroup = request.getParameter("selectedGroup");
        if (selectedGroup == null) {
            groupMessage = getMessage("org.jahia.admin.groupMessage.selectGroup.label");
            displayGroupList(request, response, session);
        } else {
            request.setAttribute("groupName", selectedGroup);
            JahiaGroup group = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(jahiaSite.getID(), selectedGroup);
            try {
                if (!(group instanceof JCRGroup) || ((JCRGroup)group).getNode(JCRSessionFactory.getInstance().getCurrentUserSession()).isNodeType("jmix:systemNode")) {
                    request.setAttribute("groupReadOnly", Boolean.TRUE);
                } else {
                    request.setAttribute("groupReadOnly", Boolean.FALSE);
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
            request.setAttribute("jspSource", JSP_PATH + "group_management/group_remove.jsp");
            request.setAttribute("directMenu", JSP_PATH + "direct_menu.jsp");
            session.setAttribute("jahiaDisplayMessage", Jahia.COPYRIGHT);
            session.setAttribute("groupMessage", groupMessage);
            session.setAttribute("isError", isError);
            doRedirect(request, response, session, JSP_PATH + "admin.jsp");
        }
    }

    /**
     * Remove a group from the jahia DB
     *
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     */
    private void processGroupRemove(HttpServletRequest request,
                                    HttpServletResponse response,
                                    HttpSession session)
            throws IOException, ServletException {
        groupMessage = "";
        isError = true;

        String groupName = (String) request.getParameter("groupName");
        // first let's do a quick sanity check on the group name.
        if ((groupName == null) || ("".equals(groupName))) {
            groupMessage = getMessage("org.jahia.admin.groupMessage.cannotRemoved.label");
            groupMessage += " [" + groupName + "] ";
            groupMessage += getMessage("label.group");
        }

        // for the moment we forbid the deletion of the following groups,
        // because code in Jahia assumes they are always there :
        // - administrators
        // - users
        // - guest
        if (JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME.equals(groupName) ||
                JahiaGroupManagerService.USERS_GROUPNAME.equals(groupName) ||
                JahiaGroupManagerService.GUEST_GROUPNAME.equals(groupName)) {
            groupMessage = getMessage("org.jahia.admin.groupMessage.cannotRemoved.label");
            groupMessage += " [" + groupName + "] ";
            groupMessage += getMessage("label.group");
        }

        if (groupMessage.equals("")) {
            JahiaGroup theGroup = (JahiaGroup) gMgr.lookupGroup(jahiaSite.getID(), groupName);

            // delete group...
            if (!gMgr.deleteGroup(theGroup)) {
                groupMessage = getMessage("org.jahia.admin.groupMessage.cannotRemoved.label");
                groupMessage += " [" + groupName + "] ";
                groupMessage += getMessage("label.group");
            } else {
                groupMessage = getMessage("label.group");
                groupMessage += " [" + groupName + "] ";
                groupMessage += getMessage("message.successfully.removed");
                isError = false;

            }
        }
        displayGroupList(request, response, session);
        groupMessage = "";
        isError = true;
    }

    /**
     * Create a group
     * FIXME : This method has to be rechecked.
     *
     * @param session ??? FIXME : Has to be removed ?
     * @param name    The group name
     * @return A reference to the newly created group.
     */
    private JahiaGroup createGroup(HttpSession session, String name) {
        // create the group...
        JahiaGroup theGroup = null;
        theGroup = gMgr.createGroup(jahiaSite.getID(), name, null, false);
        return theGroup;
    }

    // FIXME : Has this method really something to do here ?

    /**
     * Return the member List of a given site and a given group.
     *
     * @param groupName
     * @param jahiaSite
     * @return the member Set of a given site and a given group.
     */
    private Set<Principal> getGroupMembers(String groupName, int jahiaSite) {

        JahiaGroup theGroup = (JahiaGroup) gMgr.lookupGroup(jahiaSite, groupName);
        Set<Principal> groupMembers = new HashSet<Principal>();
        if (theGroup != null) {
            Enumeration<Principal> groupMembersEnum = theGroup.members();

            while (groupMembersEnum.hasMoreElements()) {
                Object member = (Object) groupMembersEnum.nextElement();
                // keep only members from this jahiaSite...
                if (member instanceof JahiaUser) {
                    groupMembers.add((JahiaUser) member);
                } else {
                    if (((JahiaGroup) member).getSiteID() == jahiaSite) {
                        groupMembers.add((JahiaGroup) member);
                    }
                }
            }
        }
        return groupMembers;
    }

    /**
     * Recover membership and its site of a group.
     * FIXME : Has to be recheked.
     * (Indeed. When we edit the group, this method doesn't provide the mebership anymore -PredragV-)
     *
     * @param groupName the group name
     * @param jahiaSite a jahia site ID
     * @return the group member ship
     */
    private List<String> getGroupMembership(String groupName, int jahiaSite) {
        JahiaGroup theGroup = (JahiaGroup) gMgr.lookupGroup(jahiaSite, groupName);
        List<String> groupMembership = new ArrayList<String>();
        if (theGroup != null) {
            Enumeration<Principal> groupMembersEnum = theGroup.members();
            while (groupMembersEnum.hasMoreElements()) {
                Principal obj = groupMembersEnum.nextElement();
                // keep only out member of this jahiaSite...
                if (obj instanceof JahiaUser) {
                    JahiaUser grpMember = (JahiaUser) obj;
                    groupMembership.add(PrincipalViewHelper.getUserDisplayName(grpMember.getUsername())); // Member name
                    // Look for site name from membership
                    groupMembership.add("jahia server");
                }
            }
        }
        return groupMembership;
    }

    /**
     * Add or remove members to a group.
     * FIXME : This method has to be rechecked !
     *
     * @param left    a set of members to check
     * @param right   a set of members to check
     * @param addRem  the addMembers or removeMembers method to apply.
     * @param grp     the object defining the previous method
     * @param jParams
     */
    private void addRemoveGroupMembers(Set<Principal> left,
                                       Set<Principal> right,
                                       Method addRem,
                                       JahiaGroup grp,
                                       ProcessingContext jParams) {
        for (Principal elementRight : right) {
            for (Principal elementLeft : left) {
                if (elementLeft.getName().equals(elementRight.getName())) {
                    elementRight = null;
                    break;
                }
            }
            if (elementRight != null) {
                try {
                    Object[] args = {elementRight};
                    addRem.invoke(grp, args);
                } catch (IllegalAccessException iae) {
                    logger.error("Error ", iae);
                } catch (InvocationTargetException ite) {
                    logger.error("Error ", ite);
                }
            }
        }
    }

//    /**
//     * Get the groups from a site a prepare them for display.
//     *
//     * @param searchResult
//     * @return a formated array list of string.
//     */

//    private List getGroupListForDisplay(List searchResult) {
//
//        List resultList = new ArrayList();
//        Iterator groupListEnum = searchResult.iterator();
//        while (groupListEnum.hasNext()) {
//            String groupKey = (String)groupListEnum.next();
//            JahiaGroup group = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(groupKey);
//            String provider = JahiaString.adjustStringSize(group.getProviderName(), 6) + " ";
//            // Construct a displayable groupname
//            String grpname = JahiaString.adjustStringSize(group.getGroupname(), 15);
//            // Find some group members for properties
//            Iterator grpMembers = group.members();
//            String members = "(";
//            while (grpMembers.hasNext()) {
//                Object obj = (Object)grpMembers.next();
//                if (obj instanceof JahiaUser) {
//                    JahiaUser tmpUser = (JahiaUser)obj;
//                    members += tmpUser.getUsername();
//                } else {
//                    JahiaGroup tmpGroup = (JahiaGroup)obj;
//                    members += tmpGroup.getGroupname();
//                }
//                if (members.length() > 30) break;
//                if (grpMembers.hasNext()) members += ", ";
//            }
//            members += ")";
//            members = JahiaString.adjustStringSize(members, 30);
//            String result = provider + " " + grpname + " " + members;
//            resultList.add(group.getGroupname());
//            resultList.add(JahiaTools.replacePattern(result, " ", "&nbsp;"));
//        }
//        return resultList;
//    }

    private static org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(ManageGroups.class);

}