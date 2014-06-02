/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.*;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.security.Principal;
import java.util.*;

/**
 * User and group related functions.
 *
 * @author Quentin Lamerand
 */
public class User {

    public static Boolean memberOf(String groups, RenderContext renderContext) {
        final JahiaUser currentUser = JCRSessionFactory.getInstance().getCurrentUser();
        if (currentUser != null) {
            final int siteID = retrieveSiteId(renderContext);
            final String[] groupArray = StringUtils.split(groups, ',');
            for (String aGroupArray : groupArray) {
                final String groupName = aGroupArray.trim();
                if (currentUser.isMemberOfGroup(siteID, groupName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Boolean notMemberOf(String groups, RenderContext renderContext) {
        final JahiaUser currentUser = JCRSessionFactory.getInstance().getCurrentUser();
        if (currentUser != null) {
            final int siteID = retrieveSiteId(renderContext);
            final String[] groupArray = StringUtils.split(groups, ',');
            for (String aGroupArray : groupArray) {
                String groupName = aGroupArray.trim();
                if (currentUser.isMemberOfGroup(siteID, groupName)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static Collection<Principal> getMembers(String group, RenderContext renderContext) {
        return ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(group).getMembers();
    }

    private static int retrieveSiteId(RenderContext renderContext) {
        int siteId = 0;
        if (renderContext != null && renderContext.getSite() != null) {
            siteId = renderContext.getSite().getID();
        }
        return siteId;
    }

    /**
     * Looks up the user by the specified user key (with provider prefix) or username.
     *
     * @param user the key or the name of the user to perform lookup for
     * @return the user for the specified user key or name or <code>null</code> if the corresponding user cannot be found
     * @throws IllegalArgumentException in case the specified user key is <code>null</code>
     */
    public static JahiaUser lookupUser(String user) throws IllegalArgumentException {
        if (user == null) {
            throw new IllegalArgumentException("Specified user key is null");
        }
        return user.startsWith("{") ? ServicesRegistry.getInstance().getJahiaUserManagerService()
                .lookupUserByKey(user) : ServicesRegistry.getInstance()
                .getJahiaUserManagerService().lookupUser(user);
    }

    public static Map<String, JahiaGroup> getUserMembership(String username) {
        Map<String, JahiaGroup> map = new LinkedHashMap<String, JahiaGroup>();
        final JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(username);
        final JahiaGroupManagerService managerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        final List<String> userMembership = managerService.getUserMembership(
                jahiaUser);
        for (String groupName : userMembership) {
            if(!groupName.equals(JahiaGroupManagerService.GUEST_GROUPNAME) &&
                    !groupName.equals(JahiaGroupManagerService.USERS_GROUPNAME)) {
                final JahiaGroup group = managerService.lookupGroup(groupName);
                map.put(groupName,group);
            }
        }
        return map;
    }

    public static Map<String, JahiaGroup> getUserMembership(JCRNodeWrapper user) {
        return getUserMembership(user.getName());
    }

    /**
     * Returns whether the current user can be assigned to the specified task (as represented by the specified JCRNodeWrapper).
     *
     * @param task a JCRNodeWrapper representing a WorkflowTask
     * @return <code>true</code> if the user can be assigned to the task, <code>false</code> otherwise
     * @throws RepositoryException
     */
    public static Boolean isAssignable(JCRNodeWrapper task) throws RepositoryException {
        final JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        if(user == null || task == null) {
            return false;
        } else {
           if(task.hasProperty("candidates")) {
               final JahiaGroupManagerService managerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
               Set<String> userMembership = null;

               // candidates are using the u:userName or g:groupName format
               final String formattedUserName = "u:" + user.getName();

               // look at all the candidates for assignment
               final Value[] candidatesValues = task.getProperty("candidates").getValues();
               for (Value value : candidatesValues) {
                   final String candidate = value.getString();

                   // first check if the current candidate is the user name
                   if(candidate.equals(formattedUserName)) {
                       // if it is, we're done
                       return true;
                   } else {
                       // otherwise, check if we're looking at a group, extract the group name and check whether the user is a member of that group
                       if(candidate.startsWith("g:")) {
                           final String groupName = candidate.substring(2);

                           if(userMembership == null) {
                               // only init userMembership if we need it
                               userMembership = new HashSet<String>(managerService.getUserMembership(user));
                           }

                           for (String membership : userMembership) {
                               // memberships are groupName:siteId so check if we have a membership that starts with our group name first
                               if(membership.startsWith(groupName)) {
                                   // we need to make sure that we have a full match instead of a partial only
                                   final String group = membership.substring(0, membership.indexOf(':'));
                                   if(groupName.equals(group)) {
                                       // we have a match!
                                       return true;
                                   }
                               }
                           }
                       }
                   }
               }
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

    public static Set<JahiaUser> searchUsers(Map<String, String> criterias) {
        Properties searchCriterias = new Properties();
        if (criterias == null || criterias.isEmpty()) {
            searchCriterias.setProperty("*", "*");
        } else {
            for (String key : criterias.keySet()) {
                searchCriterias.setProperty(key, criterias.get(key));
            }
        }

        JahiaUserManagerService userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        List<? extends JahiaUserManagerProvider> providerList = userManagerService.getProviderList();
        Set<JahiaUser> searchResults = new HashSet<JahiaUser>();
        for (JahiaUserManagerProvider provider : providerList) {
            searchResults.addAll(provider.searchUsers(searchCriterias));
        }
        return searchResults;
    }

    public static Boolean isPropertyEditable(JCRUserNode userNode, String name) {
        return userNode.isPropertyEditable(name);
    }

    public static String formatUserValueOption(Principal principal) {
        return new PrincipalViewHelper(new String[]{"Name,30","Properties,30"}).getPrincipalValueOption(principal);
    }

    public static String formatUserTextOption(Principal principal,String fieldsToDisplay) {
        return new PrincipalViewHelper(fieldsToDisplay.split(";")).getPrincipalTextOption(principal);
    }
    
    public static Boolean isReadOnlyProvider(Principal p) {
        boolean readOnly = false;
        if (p instanceof JahiaGroup) {
            readOnly = JahiaGroupManagerRoutingService.getInstance().getProvider(((JahiaGroup) p).getProviderName())
                    .isReadOnly();
        } else if (p instanceof JahiaUser) {
            readOnly = JahiaUserManagerRoutingService.getInstance().getProvider(((JahiaUser) p).getProviderName())
                    .isReadOnly();
        }

        return readOnly;
    }
}