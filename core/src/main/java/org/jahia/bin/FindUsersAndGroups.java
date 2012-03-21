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

package org.jahia.bin;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
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

    private JahiaSitesService siteService;

    protected int getSiteId(HttpServletRequest request) {
        int siteId = getIntParameter(request, "siteId", -1);
        if (siteId <= 0) {
            String siteKey = getParameter(request, "siteKey");

            try {
                JahiaSite site = siteService.getSiteByKey(siteKey);
                if (site == null) {
                    throw new JahiaBadRequestException("Site with key " + siteKey
                            + " cannot be found");
                }
                siteId = site.getID();
            } catch (JahiaException e) {
                throw new JahiaBadRequestException(e);
            }
        }

        return siteId;
    }

    @Override
    protected Set<Principal> search(String queryTerm, HttpServletRequest request) {

        int limit = Math.min(getIntParameter(request, "limit", defaultLimit), hardLimit);
        
        if (!queryTerm.contains("*")) {
            // append wildcard to the search term
            queryTerm += "*"; 
        }

        Set<Principal> users = searchUsers(queryTerm);

        Set<JahiaGroup> groups = searchGroups(queryTerm, request);

        Set<Principal> result = new HashSet<Principal>();

        if (users.size() + groups.size() <= limit) {
            result.addAll(users);
            result.addAll(groups);
        } else {
            if (users.size() <= (limit / 2)) {
                result.addAll(users);
            } else {
                result.addAll(new LinkedList<Principal>(users).subList(0,
                        Math.max(limit / 2, limit - groups.size())));
            }
            for (JahiaGroup g : groups) {
                if (result.size() >= limit) {
                    break;
                }
                result.add(g);
            }
        }

        return result;
    }

    protected Set<JahiaGroup> searchGroups(String queryTerm, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();

        int siteId = getSiteId(request);

        Properties searchCriterias = new Properties();
        for (String key : groupSearchProperties) {
            searchCriterias.put(key, queryTerm);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Performing group search using criteria: {}", searchCriterias);
        }

        Set<JahiaGroup> result = groupService.searchGroups(siteId, searchCriterias);

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

    public void setJahiaSitesService(JahiaSitesService siteService) {
        this.siteService = siteService;
    }

    protected JSONObject toJSON(JahiaGroup group) throws JSONException {
        JSONObject json = new JSONObject();

        json.put("key", "g:" + group.getGroupname());
        json.put("groupKey", group.getGroupKey());
        json.put("groupname", group.getGroupname());
        json.put("displayName", PrincipalViewHelper.getFullName(group));
        json.put("type", "g");

        for (String key : groupDisplayProperties) {
            String value = group.getProperty(key);
            if (value != null) {
                json.put(key, value);
            }
        }

        return json;
    }

    protected JSONObject toJSON(JahiaUser user) throws JSONException {
        JSONObject json = super.toJSON(user);

        json.put("key", "u:" + user.getUsername());
        String fullName = PrincipalViewHelper.getFullName(user);
        if (!fullName.equals(user.getUsername())) {
            fullName = fullName + " (" + user.getUsername() + ")";
        }
        json.put("displayName", fullName);
        json.put("type", "u");

        return json;
    }

    @Override
    protected JSONObject toJSON(Principal principal) throws JSONException {
        return (principal instanceof JahiaGroup) ? toJSON((JahiaGroup) principal)
                : toJSON((JahiaUser) principal);
    }

}
