/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.user;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import static org.jahia.api.Constants.UI_THEME;

import java.util.*;

/**
 * User and group related functions.
 *
 * @author Quentin Lamerand
 */
public class User {

    private static final Logger logger = LoggerFactory.getLogger(User.class);

    public static Boolean memberOf(String groups, RenderContext renderContext) {
        final JahiaUser currentUser = JCRSessionFactory.getInstance().getCurrentUser();
        if (currentUser != null) {
            JCRUserNode userNode = JahiaUserManagerService.getInstance().lookupUserByPath(currentUser.getLocalPath());
            if (userNode != null) {
                final String siteKey = retrieveSiteKey(renderContext);
                final String[] groupArray = StringUtils.split(groups, ',');
                for (String aGroupArray : groupArray) {
                    final String groupName = aGroupArray.trim();
                    if (userNode.isMemberOfGroup(siteKey, groupName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Boolean notMemberOf(String groups, RenderContext renderContext) {
        final JahiaUser currentUser = JCRSessionFactory.getInstance().getCurrentUser();
        if (currentUser != null) {
            JCRUserNode userNode = JahiaUserManagerService.getInstance().lookupUserByPath(currentUser.getLocalPath());
            if (userNode != null) {
                final String siteKey = retrieveSiteKey(renderContext);
                final String[] groupArray = StringUtils.split(groups, ',');
                for (String aGroupArray : groupArray) {
                    String groupName = aGroupArray.trim();
                    if (userNode.isMemberOfGroup(siteKey, groupName)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static Collection<JCRNodeWrapper> getMembers(String group, RenderContext renderContext) {
        return JahiaGroupManagerService.getInstance().lookupGroupByPath(group).getMembers();
    }

    private static String retrieveSiteKey(RenderContext renderContext) {
        String siteId = null;
        if (renderContext != null && renderContext.getSite() != null) {
            siteId = renderContext.getSite().getSiteKey();
        }
        return siteId;
    }

    /**
     * Looks up the user by the specified user key (user node path) or username.
     *
     * @param user the key or the name of the user to perform lookup for
     * @return the user for the specified user key or name or <code>null</code> if the corresponding user cannot be found
     * @throws IllegalArgumentException in case the specified user key is <code>null</code>
     */
    public static JCRUserNode lookupUser(String user) throws IllegalArgumentException {
        if (user == null) {
            throw new IllegalArgumentException("Specified user key is null");
        }
        return ServicesRegistry.getInstance().getJahiaUserManagerService().lookup(user);
    }

    public static JCRUserNode lookupUser(String user, String site) throws IllegalArgumentException {
        if (user == null) {
            throw new IllegalArgumentException("Specified user key is null");
        }
        return ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(user, site);
    }

    public static Map<String, JCRGroupNode> getUserMembership(String user) {
        return getUserMembership(lookupUser(user));
    }

    public static Map<String, JCRGroupNode> getUserMembership(JCRNodeWrapper user) {
        Map<String, JCRGroupNode> map = new LinkedHashMap<String, JCRGroupNode>();
        final JahiaGroupManagerService managerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        final List<String> userMembership = managerService.getMembershipByPath(user.getPath());
        for (String groupPath : userMembership) {
            if (!groupPath.endsWith("/" + JahiaGroupManagerService.GUEST_GROUPNAME) &&
                    !groupPath.endsWith("/" + JahiaGroupManagerService.USERS_GROUPNAME) &&
                    !groupPath.endsWith("/" + JahiaGroupManagerService.SITE_USERS_GROUPNAME)) {
                final JCRGroupNode group = managerService.lookupGroupByPath(groupPath);
                map.put(groupPath, group);
            }
        }
        return map;
    }

    /**
     * Returns whether the current user can be assigned to the specified task (as represented by the specified JCRNodeWrapper).
     *
     * @param task a JCRNodeWrapper representing a WorkflowTask
     * @return <code>true</code> if the user can be assigned to the task, <code>false</code> otherwise
     * @throws RepositoryException in case of JCR-related errors
     */
    public static Boolean isAssignable(JCRNodeWrapper task) throws RepositoryException {
        final JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        if (user == null || task == null) {
            return false;
        } else {
            if (task.hasProperty("candidates")) {
                final JahiaGroupManagerService managerService = ServicesRegistry.getInstance()
                        .getJahiaGroupManagerService();

                // candidates are using the path
                final String formattedUserName = user.getUserKey();

                // look at all the candidates for assignment
                final Value[] candidatesValues = task.getProperty("candidates").getValues();
                for (Value value : candidatesValues) {
                    final String candidate = value.getString();

                    // first check if the current candidate is the user name
                    if (candidate.equals(formattedUserName)) {
                        // if it is, we're done
                        return true;
                    } else {
                        // otherwise, check if we're looking at a group, extract the group name and check whether the user is a member of
                        // that group
                        if (candidate.contains("/groups/")) {
                            JCRGroupNode candidateGroup = managerService.lookupGroupByPath(candidate);
                            if (candidateGroup != null) {
                                if (candidateGroup.isMember(user.getLocalPath())) {
                                    return true;
                                }
                            } else {
                                logger.info("Unable to lookup group for key {}."
                                        + " Skipping it when checking task assignee candidates.", candidate);
                            }
                        }
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the full user name, including first and last name. If those are
     * not available, returns the username.
     *
     * @param userNode the user JCR node
     * @return the full user name, including first and last name. If those are
     *         not available, returns the username
     */
    public static String userFullName(JCRNodeWrapper userNode) {
        StringBuilder name = new StringBuilder();
        String value = userNode.getPropertyAsString("j:firstName");
        if (StringUtils.isNotEmpty(value)) {
            name.append(value);
        }
        value = userNode.getPropertyAsString("j:lastName");
        if (StringUtils.isNotEmpty(value)) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(value);
        }

        if (name.length() == 0) {
            name.append(PrincipalViewHelper.getUserDisplayName(userNode.getName()));
        }

        return name.toString();
    }

    public static Set<JCRUserNode> searchUsers(Map<String, String> criterias) {
        Properties searchCriterias = new Properties();
        if (criterias == null || criterias.isEmpty()) {
            searchCriterias.setProperty("*", "*");
        } else {
            for (String key : criterias.keySet()) {
                searchCriterias.setProperty(key, criterias.get(key));
            }
        }

        JahiaUserManagerService userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();

        Set<JCRUserNode> searchResults = new HashSet<JCRUserNode>();
        searchResults.addAll(userManagerService.searchUsers(searchCriterias));
        return searchResults;
    }

    public static Boolean isPropertyEditable(JCRUserNode userNode, String name) {
        return userNode.isPropertyEditable(name);
    }

    public static String formatUserValueOption(Object principal) {
        return new PrincipalViewHelper(new String[] {"Name,30", "Properties,30"}).getPrincipalValueOption(principal);
    }

    public static String formatUserTextOption(JCRNodeWrapper principal, String fieldsToDisplay) {
        return new PrincipalViewHelper(fieldsToDisplay.split(";")).getPrincipalTextOption(principal);
    }

    public static Boolean isReadOnlyProvider(JCRNodeWrapper principal) {
        return principal.getProvider().isReadOnly();
    }

    /**
     * Returns the value of the preferred UI theme for the specified user. If the preferred theme property is not set for the user, we
     * use the globally configured theme. Null user parameter passed means global setting retrieval.
     *
     * @param user the user JCR node
     * @return the value for of the preferred UI theme for the currently logged in user
     *
     * @since 7.2.3.1
     */
    public static String getUITheme(JCRUserNode user) {
        String theme = user != null ? user.getPropertyAsString(UI_THEME) : null;
        return theme != null ? theme : SettingsBean.getInstance().getPropertiesFile().getProperty(UI_THEME);
    }
}
