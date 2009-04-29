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
 package org.jahia.engines.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.JahiaString;
import org.jahia.utils.JahiaTools;


/**
 * <p>Title: An engine to do user selection.</p> <p>Description: This engine is sort of like a
 * "File Open" window under Windows, where you can select multiple users. This should be generic
 * enough to be used both in ACL management and in the administration component for example for
 * group management.</p> <p>Copyright: Copyright (c) 2002</p> <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 3.0
 */

public class SelectUG_Engine implements JahiaEngine {
    private static final String TEMPLATE_JSP = "selectUG";
    private static final String CLOSE_JSP = "selectusers_close";

    public static final String SELECT_SITE = "selectSite";

    public static final String ENGINE_NAME = "selectUG";
    private EngineToolBox toolBox;

    public SelectUG_Engine () {
        toolBox = EngineToolBox.getInstance ();
    }

    /**
     * Check if we have the rights to view this engine
     *
     * @param jParams ProcessingContext object
     *
     * @return boolean if we are allowed to render this engine, false otherwise
     */
    public boolean authoriseRender (ProcessingContext jParams) {
        return toolBox.authoriseRender (jParams);
    } // end authoriseRender


    /**
     * Renders a link to this engine.
     *
     * @param jParams ProcessingContext object to be used to generate URL.
     * @param theObj
     *
     * @return
     *
     * @throws JahiaException
     */
    public String renderLink (ProcessingContext jParams, Object theObj)
            throws JahiaException {
        String rightParams = (String) theObj;
        StringBuffer params = new StringBuffer();
        params.append("?mode=display");
        if (theObj != null) {
            params.append(rightParams);
        }
        return jParams.composeEngineUrl (ENGINE_NAME, params.toString());
    } // end renderLink


    /**
     * needsJahiaData
     */
    public boolean needsJahiaData (ProcessingContext jParams) {
        return false;
    } // end needsJahiaData


    /**
     * handles the engine actions
     *
     * @param jParams a ProcessingContext object
     * @param jData   a JahiaData object (not mandatory)
     */
    public EngineValidationHelper handleActions (ProcessingContext jParams, JahiaData jData)
            throws JahiaException,
            JahiaSessionExpirationException {
        // initalizes the hashmap
        Map engineMap = initEngineMap (jParams);

        processLastScreen (jParams, engineMap);
        processCurrentScreen (jParams, engineMap);

        // displays the screen
        toolBox.displayScreen (jParams, engineMap);

        return null;

    } // end handleActions

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName () {
        return ENGINE_NAME;
    }


    /**
     * processes the last screen sent by the user
     *
     * @param jParams a ProcessingContext object
     */
    public void processLastScreen (ProcessingContext jParams, Map engineMap)
            throws JahiaException {
        // gets engineMap values
        String theScreen = (String) engineMap.get ("screen");
        if (theScreen == null) {
            throw new JahiaException ("SelectUG_Engine.processLastScreen",
                    "Error in parameters",
                    JahiaException.PARAMETER_ERROR,
                    JahiaException.CRITICAL_SEVERITY);
        }

        // read search parameters.
        String searchString = jParams.getParameter("searchString");
        if(searchString == null){
            // first access: done in order to search in all provider and not only in jahia_db
            searchString = "";
        }
        jParams.setAttribute("searchString", searchString);
        String searchIn = jParams.getParameter("searchIn");
        if(searchIn == null){
            // first access: done in order to search in all provider and dot only in jahia_db
            searchIn = "";
        }
        String[] searchInProps = jParams.getParameterValues("properties");
        String storedOn = jParams.getParameter("storedOn");
        String[] providers = jParams.getParameterValues("providers");

        // let's do storage stuff here... Most notably insert into engine
        // map the result of the user selection.
        String[] selectedUsers = jParams.getParameterValues("selectedUG");
        Set userKeySet = new HashSet();
        if (selectedUsers != null) {
            for (int i=0; i < selectedUsers.length; i++) {
                if (!"null".equals(selectedUsers[i]) &&
                        !"".equals(selectedUsers[i])) {
                    userKeySet.add(selectedUsers[i]);
                }
            }
        }
        engineMap.put("alreadySelectedUsers", userKeySet);

        Integer userNameWidth = (Integer) jParams.getSessionState ().getAttribute (
                "userNameWidth");

        int siteID = jParams.getSiteID();
        // first, if present, we handle the site change.
        if (jParams.getParameter("selectedSite") != null) {
            String siteIDStr = (String) jParams.getParameter("selectedSite");
            int newSiteID = Integer.parseInt(siteIDStr);
            JahiaSite newSite = ServicesRegistry.getInstance().getJahiaSitesService().getSite(newSiteID);
            if (newSite != null) {
                engineMap.put("currentSite", newSite);
                siteID = newSite.getID();
            }
        }

        PrincipalViewHelper pvh = new PrincipalViewHelper(new String[] {"Provider, 6", "Name, " + userNameWidth, "Properties, 20"} );

        if ("searchUsers".equals (theScreen)) {

            // first let's detect if we are coming from the group view, with a double-click
            // on a user, in which case we must select the appropriate entry by performing
            // a search for the selected user.
            String selectedGroupMember = (String) jParams.getParameter("memberList");
            if ("null".equals(selectedGroupMember)) {
                selectedGroupMember = null;
            }
            if ((selectedGroupMember != null) &&
                (!"".equals(selectedGroupMember)) ) {
                userKeySet.clear();
                userKeySet.add(selectedGroupMember);
                String userName = extractPrincipalName(selectedGroupMember, userNameWidth);
                JahiaUser selectedUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userName);
                searchString = selectedUser.getUsername();
                jParams.setAttribute("searchString", searchString);
            }

            // now let's build the view of group membership for the selected users
            String subAction = (String) jParams.getParameter("subaction");
            if (("viewGroupMembership".equals(subAction)) || (selectedGroupMember != null)) {
                // we will display the group membership of the selected user
                List groupList = new ArrayList();
                Iterator userKeySetIter = userKeySet.iterator();
                while (userKeySetIter.hasNext()) {
                    String curUserKey = (String) userKeySetIter.next();
                    String userName = extractPrincipalName(curUserKey, userNameWidth);
                    JahiaUser selectedUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userName);
                    Set groupMembership = PrincipalViewHelper.getUserGroupMembership(selectedUser, jParams.getSiteID());
                    Iterator groupMembershipIter = groupMembership.iterator();
                    while (groupMembershipIter.hasNext()) {
                        JahiaGroup group = (JahiaGroup) groupMembershipIter.next();
                        buildGroupListEntry(group, userNameWidth, groupList);
                    }
                }
                engineMap.put("groupList", groupList);
            }

            //if (searchString != null && searchString.length() > 0) {
                Set searchResults = PrincipalViewHelper.getSearchResult(searchIn, siteID, searchString, searchInProps, storedOn, providers);
                if (searchResults != null) {
                    List resultList = new ArrayList();
                    Iterator resultListEnum = searchResults.iterator();
                    while (resultListEnum.hasNext()) {
                        JahiaUser user = (JahiaUser) resultListEnum.next();
                        if(user !=  null){
                            String usrname = JahiaString.adjustStringSize(user.getUsername(), userNameWidth.intValue());
                            String resultValue = "10000000r-- " + usrname + "u" + user.getUserKey();
                            String result = " " + pvh.getPrincipalTextOption(user);
                            resultList.add(JahiaTools.replacePattern(resultValue, " ", "&nbsp;"));
                            resultList.add(JahiaTools.replacePattern(result, " ", "&nbsp;"));
                        }
                    }
                    engineMap.put("resultList", resultList);
                }
            /*} else {
                engineMap.put("resultList", new ArrayList());
            }*/
            engineMap.put ("screen", "edit");
            engineMap.put ("selectUGEngine", "selectUsers");
        } else if (theScreen.equals ("searchGroups")) {

            // first let's detect if we are coming from the user view, with a double-click
            // on a group, in which case we must select the appropriate entry, by performing a
            // search for the selected group
            String selectedMembershipGroup = (String) jParams.getParameter("groupMembership");
            if ("null".equals(selectedMembershipGroup)) {
                selectedMembershipGroup = null;
            }
            if ( (selectedMembershipGroup != null) &&
                (!"".equals(selectedMembershipGroup)) ) {
                userKeySet.clear();
                userKeySet.add(selectedMembershipGroup);
                String groupName = extractPrincipalName(selectedMembershipGroup, userNameWidth);
                JahiaGroup selectedGroup = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(groupName);
                searchString = selectedGroup.getGroupname();
                jParams.setAttribute("searchString", searchString);
            }

            // now let's build the view of the members of the selected group.
            String subAction = (String) jParams.getParameter("subaction");
            if (("viewMembers".equals(subAction)) || (selectedMembershipGroup != null)) {
                // we will display the members of the selected group
                List memberList = new ArrayList();
                Iterator userKeySetIter = userKeySet.iterator();
                while (userKeySetIter.hasNext()) {
                    String curUserKey = (String) userKeySetIter.next();
                    String groupName = extractPrincipalName(curUserKey, userNameWidth);
                    JahiaGroup selectedGroup = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(groupName);
                    Iterator membersEnum = new EnumerationIterator(selectedGroup.members());
                    while (membersEnum.hasNext()) {
                        Object curMember = membersEnum.next();
                        // @todo for the moment we don't process embedded groups !!!!
                        if (curMember instanceof JahiaUser) {
                            JahiaUser user = (JahiaUser) curMember;
                            buildUserListEntry(user, userNameWidth, memberList);
                        }
                    }
                }
                engineMap.put("memberList", memberList);
            }

            // now let's perform the search for the groups
            //if (searchString != null && searchString.length() > 0) {  
                Set searchResults = PrincipalViewHelper.getGroupSearchResult(searchIn, siteID, searchString, searchInProps, storedOn, providers);
                if (searchResults != null) {
                    List resultList = new ArrayList();
                    Iterator resultListEnum = searchResults.iterator();
                    while (resultListEnum.hasNext()) {
                        JahiaGroup group = (JahiaGroup) resultListEnum.next();
                        if(group != null){
                            // Construct a displayable groupname
                             String grpname = JahiaString.adjustStringSize(group.getGroupname(), userNameWidth.intValue());
                            String resultValue = "10000000r-- " + grpname + "g" + group.getGroupKey();
                            String result = " " + pvh.getPrincipalTextOption(group);
                            resultList.add(JahiaTools.replacePattern(resultValue, " ", "&nbsp;"));
                            resultList.add(JahiaTools.replacePattern(result, " ", "&nbsp;"));
                        }
                    }
                    engineMap.put("resultList", resultList);
                }
            /*} else {
                engineMap.put("resultList", new ArrayList());
            } */
            engineMap.put("screen", "searchGroups");
            engineMap.put("selectUGEngine", "selectGroups");

        } else if (theScreen.equals ("save")) {

            // Another way to paste selection to the ACL entries is used

            // let's do storage stuff here... Most notably insert into engine
            // map the result of the user selection.
            /*String[] selectedUsers = jParams.getRequest().getParameterValues("selectedUsers");
            List userKeyList = new ArrayList();
            for (int i=0; i < selectedUsers.length; i++) {
                userKeyList.add(selectedUsers[i]);
            }
            jParams.getSession().setAttribute("org.jahia.engines.selectusers.selectedUsers", userKeyList);*/
        }
    } // end processLastScreen

    private String extractPrincipalName(String curPrincipalKey, Integer userNameWidth) {
        return curPrincipalKey.substring("10000000r-- ".length()  + userNameWidth.intValue() + 1);
    }

    private void buildUserListEntry(JahiaUser user, Integer userNameWidth, List memberList) {
        String provider = JahiaString.adjustStringSize (user.getProviderName (), 6) +
                " ";
        String usrname = JahiaString.adjustStringSize (user.getUsername (),
                userNameWidth.intValue ());
        // Find a displayable user property
        String properties = EMPTY_STRING;
        String firstname = user.getProperty ("firstname");
        String lastname = user.getProperty ("lastname");
        if (firstname != null) {
            properties += firstname;
            if (firstname.length () < 20) properties += " ";
        }
        if (lastname != null) properties += lastname;
        if (EMPTY_STRING.equals (properties)) {
            String email = user.getProperty ("email");
            if (email != null) properties += email;
        }
        properties = JahiaString.adjustStringSize (properties, 20);
        String resultValue = "10000000r-- " + usrname + "u" + user.getUserKey ();
        String result = " " + provider + usrname + " " + properties;
        memberList.add (JahiaTools.replacePattern (resultValue, " ", "&nbsp;"));
        memberList.add (JahiaTools.replacePattern (result, " ", "&nbsp;"));
    }

    private void buildGroupListEntry(JahiaGroup group, Integer userNameWidth, List resultList) {
        String provider = JahiaString.adjustStringSize (group.getProviderName (), 6) +
                " ";
        // Construct a displayable groupname
        String grpname = JahiaString.adjustStringSize (group.getGroupname (),
                userNameWidth.intValue ());
        // Find some group members for properties
        Iterator grpMembers = new EnumerationIterator(group.members ());
        String members = "(";
        while (grpMembers.hasNext ()) {
            Object obj = grpMembers.next ();
            if (obj instanceof JahiaUser) {
                JahiaUser tmpUser = (JahiaUser) obj;
                members += tmpUser.getUsername ();
            } else {
                JahiaGroup tmpGroup = (JahiaGroup) obj;
                members += tmpGroup.getGroupname ();
            }
            if (members.length () > 20) break;
            if (grpMembers.hasNext ()) members += ", ";
        }
        members += ")";
        members = JahiaString.adjustStringSize (members, 20);
        String resultValue = "10000000r-- " + grpname + "g" + group.getGroupKey ();
        String result = " " + provider + grpname + " " + members;
        resultList.add (JahiaTools.replacePattern (resultValue, " ", "&nbsp;"));
        resultList.add (JahiaTools.replacePattern (result, " ", "&nbsp;"));
    }

    /**
     * prepares the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public void processCurrentScreen (ProcessingContext jParams, Map engineMap)
            throws JahiaException {
        //String  theScreen   = (String) engineMap.get( "screen" );

        JahiaUserManagerService uMgr = ServicesRegistry.getInstance ()
                .getJahiaUserManagerService ();
        JahiaGroupManagerService gMgr = ServicesRegistry.getInstance ()
                .getJahiaGroupManagerService ();
        List providerList = uMgr.getProviderList ();
        engineMap.put ("providerList", providerList);
        List group_providerList = gMgr.getProviderList ();
        engineMap.put ("groupProviderList", group_providerList);
        engineMap.put ("siteList", JahiaAdministration.getAdminGrantedSites(jParams.getUser()));

        jParams.setAttribute ("jahia_session_engineMap", engineMap);

    } // end processCurrentScreen


    /**
     * inits the engine map
     *
     * @param jParams a ProcessingContext object (with request and response)
     *
     * @return a Map object containing all the basic values needed by an engine
     */
    private Map initEngineMap (ProcessingContext jParams)
            throws JahiaException,
            JahiaSessionExpirationException {
        String theScreen = jParams.getParameter ("screen");
        if (theScreen == null) {
            theScreen = "searchGroups";
        }

        // gets session values
        //HttpSession theSession = jParams.getRequest().getSession (true);
        SessionState theSession = jParams.getSessionState ();

        Map engineMap = (Map) theSession.getAttribute ("jahia_session_engineMap");

        if (engineMap == null) {
            theScreen = "searchGroups";
            // init engine map
            engineMap = new HashMap();
        }
        engineMap.put (RENDER_TYPE_PARAM, new Integer (JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put (ENGINE_NAME_PARAM, ENGINE_NAME);
        engineMap.put ("selectUGEngine", "selectGroups");
        if (engineMap.get("currentSite") == null) {
            engineMap.put ("currentSite", jParams.getSite());
        }
        if ("true".equals(jParams.getParameter("selectSite"))) {
            engineMap.put("selectSite", Boolean.TRUE);
            // we now keep generating URLs with the attribute.
            engineMap.put (ENGINE_URL_PARAM, renderLink(jParams, "&selectSite=true"));
        } else {
            engineMap.put("selectSite", Boolean.FALSE);
            engineMap.put (ENGINE_URL_PARAM, renderLink(jParams, null));
        }

        theSession.setAttribute ("jahia_session_engineMap", engineMap);

        if (theScreen == null) {
            theScreen = "searchGroups";
        }

        // sets screen
        engineMap.put ("screen", theScreen);
        if (theScreen.equals ("cancel")) {
            engineMap.put ("jspSource", CLOSE_JSP);
        } else if (theScreen.equals ("save")) {
            engineMap.put ("jspSource", CLOSE_JSP);
        } else {
            engineMap.put ("jspSource", TEMPLATE_JSP);
        }

        // sets engineMap for JSPs
        jParams.setAttribute ("engineTitle", "Select Users");
        jParams.setAttribute ("org.jahia.engines.EngineHashMap", engineMap);

        return engineMap;
    } // end initEngineMap

}
