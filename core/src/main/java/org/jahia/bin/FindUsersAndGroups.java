/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.bin;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for performing user and group mixed search.
 * 
 * @author Sergiy Shyrkov
 * @since 6.6.1.0
 */
public class FindUsersAndGroups extends FindUser {

    private static final Logger logger = LoggerFactory.getLogger(FindUsersAndGroups.class);

    public static String getFindUsersAndGroupsServletPath() {
        // TODO move this into configuration
        return "/cms/findUsersAndGroups";
    }

    protected Set<String> groupDisplayProperties = Collections.emptySet();

    protected Set<String> groupSearchProperties = new HashSet<String>(Arrays.asList("groupname"));

    private JahiaGroupManagerService groupService;

    protected String getSiteKey(HttpServletRequest request) {
        String siteKey = getParameter(request, "siteKey", null);
        if (siteKey == null) {
                throw new JahiaBadRequestException("siteKey parameter must be present");
        }

        return siteKey;
    }

    @Override
    protected Set<JCRNodeWrapper> search(String queryTerm, HttpServletRequest request) {

        int limit = Math.min(getIntParameter(request, "limit", defaultLimit), hardLimit);
        
        if (!queryTerm.contains("*")) {
            // append wildcard to the search term
            queryTerm += "*"; 
        }

        Set<JCRUserNode> users = searchUsers(queryTerm);

        Set<JCRGroupNode> groups = searchGroups(queryTerm, request);

        Set<JCRNodeWrapper> result = new HashSet<JCRNodeWrapper>();

        if (users.size() + groups.size() <= limit) {
            result.addAll(users);
            result.addAll(groups);
        } else {
            if (users.size() <= (limit / 2)) {
                result.addAll(users);
            } else {
                result.addAll(new LinkedList<JCRUserNode>(users).subList(0,
                        Math.max(limit / 2, limit - groups.size())));
            }
            for (JCRGroupNode g : groups) {
                if (result.size() >= limit) {
                    break;
                }
                result.add(g);
            }
        }

        return result;
    }

    protected Set<JCRGroupNode> searchGroups(String queryTerm, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();

        String siteKey = getSiteKey(request);

        Properties searchCriterias = new Properties();
        for (String key : groupSearchProperties) {
            searchCriterias.put(key, queryTerm);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Performing group search using criteria: {}", searchCriterias);
        }

        Set<JCRGroupNode> result = groupService.searchGroups(siteKey, searchCriterias);

        if (logger.isDebugEnabled()) {
            logger.debug("Found {} matching groups in {} ms", result.size(),
                    (System.currentTimeMillis() - startTime));
        }

        return result;
    }

    public void setGroupDisplayProperties(Set<String> groupDisplayProperties) {
        this.groupDisplayProperties = groupDisplayProperties;
    }

    public void setGroupSearchProperties(Set<String> groupSearchProperties) {
        this.groupSearchProperties = groupSearchProperties;
    }

    public void setJahiaGroupManagerService(JahiaGroupManagerService groupService) {
        this.groupService = groupService;
    }

    protected JSONObject toJSON(JCRGroupNode group) throws JSONException {
        JSONObject json = new JSONObject();

        json.put("key", "g:" + group.getPath());
        json.put("groupKey", group.getPath());
        json.put("groupname", group.getName());
        json.put("displayName", PrincipalViewHelper.getFullName(group));
        json.put("type", "g");

        for (String key : groupDisplayProperties) {
            try {
                if (group.hasProperty(key)) {
                    String value = group.getProperty(key).getString();
                    if (value != null) {
                        json.put(key, value);
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return json;
    }

    protected JSONObject toJSON(JCRUserNode user) throws JSONException {
        JSONObject json = super.toJSON(user);

        json.put("key", "u:" + user.getName());
        String fullName = PrincipalViewHelper.getFullName(user);
        if (!fullName.equals(user.getName())) {
            fullName = fullName + " (" + user.getName() + ")";
        }
        json.put("displayName", fullName);
        json.put("type", "u");

        return json;
    }

    @Override
    protected JSONObject toJSON(JCRNodeWrapper principal) throws JSONException {
        return (principal instanceof JCRGroupNode) ? toJSON((JCRGroupNode) principal)
                : toJSON((JCRUserNode) principal);
    }

}
