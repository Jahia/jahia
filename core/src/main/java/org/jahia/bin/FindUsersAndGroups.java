/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
