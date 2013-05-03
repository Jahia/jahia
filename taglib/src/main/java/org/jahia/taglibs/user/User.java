/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.taglibs.user;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.*;

import java.security.Principal;
import java.util.*;

/**
 * User and group related functions.
 *
 * @author Quentin Lamerand
 */
public class User {

    public static Boolean memberOf(String groups, RenderContext renderContext) {
        boolean result = false;
        if (JCRSessionFactory.getInstance().getCurrentUser() != null) {
            final String[] groupArray = StringUtils.split(groups, ',');
            for (String aGroupArray : groupArray) {
                final String groupName = aGroupArray.trim();
                if (JCRSessionFactory
                        .getInstance()
                        .getCurrentUser()
                        .isMemberOfGroup(retrieveSiteId(renderContext),
                                groupName)) {
                    return true;
                }
            }
        }
        return result;
    }

    public static Boolean notMemberOf(String groups, RenderContext renderContext) {
        boolean result = true;
        if (JCRSessionFactory.getInstance().getCurrentUser() != null) {
            final String[] groupArray = StringUtils.split(groups, ',');
            for (String aGroupArray : groupArray) {
                String groupName = aGroupArray.trim();
                if (JCRSessionFactory
                        .getInstance()
                        .getCurrentUser()
                        .isMemberOfGroup(retrieveSiteId(renderContext),
                                groupName)) {
                    return false;
                }
            }
        }
        return result;
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
}