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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for performing user and group mixed search in the list of principles of a specified node's ACL.
 * 
 * @author Sergiy Shyrkov
 * @since 6.6.1.0
 */
public class FindUsersAndGroupsInAcl extends FindUsersAndGroups {

    private static final Logger logger = LoggerFactory.getLogger(FindUsersAndGroupsInAcl.class);

    public static String getFindUsersAndGroupsInAclServletPath() {
        // TODO move this into configuration
        return "/cms/findUsersAndGroupsInAcl";
    }

    protected Set<Principal> findMatchingPrincipals(List<Map<String, Object>> rolesForNode,
            String queryTerm) {
        if (rolesForNode.isEmpty()) {
            return Collections.emptySet();
        }
        Pattern pattern = Pattern.compile(queryTerm, Pattern.CASE_INSENSITIVE);
        Set<Principal> matching = new HashSet<Principal>();

        for (Map<String, Object> principalEntry : rolesForNode) {
            Principal principal = (Principal) principalEntry.get("principal");
            if ("user".equals(principalEntry.get("principalType"))) {
                if (userMatches((JahiaUser) principal, pattern)) {
                    matching.add(principal);
                }
            } else if ("group".equals(principalEntry.get("principalType"))) {
                if (groupMatches((JahiaGroup) principal, pattern)) {
                    matching.add(principal);
                }
            } else {
                logger.warn("Unknown principal type for entry {}. Skipping.", principalEntry);
            }
        }

        return matching;
    }

    protected boolean groupMatches(JahiaGroup group, Pattern pattern) {
        boolean matches = false;

        for (String prop : groupSearchProperties) {
            String value = "groupname".equals(prop) ? group.getGroupname() : group
                    .getProperty(prop);
            if (StringUtils.isNotEmpty(value) && pattern.matcher(value).matches()) {
                matches = true;
                break;
            }
        }

        return matches;
    }

    @Override
    protected Set<Principal> search(String queryTerm, HttpServletRequest request) {
        String sourceNodePath = getParameter(request, "sourceNode");

        // append wildcard to the search term and convert wildcards to regexp
        queryTerm = queryTerm.contains("*") ? StringUtils.replace(queryTerm, "*", ".*") : queryTerm
                + ".*";

        JCRNodeWrapper sourceNode = null;
        try {
            sourceNode = JCRSessionFactory.getInstance().getCurrentUserSession()
                    .getNode(sourceNodePath);
        } catch (PathNotFoundException e) {
            throw new JahiaBadRequestException(
                    "Source node " + sourceNodePath + " cannot be found", e);
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }

        List<Map<String, Object>> rolesForNode = JCRContentUtils.getRolesForNode(sourceNode,
                Boolean.valueOf(getParameter(request, "includeInherited", "false")),
                Boolean.valueOf(getParameter(request, "expandGroups", "false")),
                getParameter(request, "roles", null), -1, false);

        Set<Principal> result = findMatchingPrincipals(rolesForNode, queryTerm);

        if (logger.isDebugEnabled()) {
            logger.debug("Found {} matching principals in ACL of the node {}", result.size(),
                    sourceNodePath);
        }

        return result;
    }

    protected boolean userMatches(JahiaUser user, Pattern pattern) {
        boolean matches = false;

        for (String prop : searchProperties) {
            String value = "username".equals(prop) ? user.getUsername() : user.getProperty(prop);
            if (StringUtils.isNotEmpty(value) && pattern.matcher(value).matches()) {
                matches = true;
                break;
            }
        }

        return matches;
    }

}
