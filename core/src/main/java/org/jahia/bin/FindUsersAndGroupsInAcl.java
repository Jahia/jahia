/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin;

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
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
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

    protected Set<JCRNodeWrapper> findMatchingPrincipals(List<Map<String, Object>> rolesForNode,
            String queryTerm) {
        if (rolesForNode.isEmpty()) {
            return Collections.emptySet();
        }
        Pattern pattern = Pattern.compile(queryTerm, Pattern.CASE_INSENSITIVE);
        Set<JCRNodeWrapper> matching = new HashSet<JCRNodeWrapper>();

        for (Map<String, Object> principalEntry : rolesForNode) {
            JCRNodeWrapper principal = (JCRNodeWrapper) principalEntry.get("principal");
            if ("user".equals(principalEntry.get("principalType"))) {
                if (userMatches((JCRUserNode) principal, pattern)) {
                    matching.add(principal);
                }
            } else if ("group".equals(principalEntry.get("principalType"))) {
                if (groupMatches((JCRGroupNode) principal, pattern)) {
                    matching.add(principal);
                }
            } else {
                logger.warn("Unknown principal type for entry {}. Skipping.", principalEntry);
            }
        }

        return matching;
    }

    protected boolean groupMatches(JCRGroupNode group, Pattern pattern) {
        boolean matches = false;

        for (String prop : groupSearchProperties) {
            try {
                String value = "groupname".equals(prop) ? group.getName() :
                        (group.hasProperty(prop) ? group.getProperty(prop).getString() : null);
                if (StringUtils.isNotEmpty(value) && pattern.matcher(value).matches()) {
                    matches = true;
                    break;
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return matches;
    }

    @Override
    protected Set<JCRNodeWrapper> search(String queryTerm, HttpServletRequest request) {
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
                true,
                getParameter(request, "roles", null), -1, false);
        rolesForNode.addAll(JCRContentUtils.getRolesForNode(sourceNode,
                Boolean.valueOf(getParameter(request, "includeInherited", "false")),
                false,
                getParameter(request, "roles", null), -1, false));

        Set<JCRNodeWrapper> result = findMatchingPrincipals(rolesForNode, queryTerm);

        if (logger.isDebugEnabled()) {
            logger.debug("Found {} matching principals in ACL of the node {}", result.size(),
                    sourceNodePath);
        }

        return result;
    }

    protected boolean userMatches(JCRUserNode user, Pattern pattern) {
        boolean matches = false;

        for (String prop : searchProperties) {
            try {
                String value = "username".equals(prop) ? user.getName() :
                        (user.hasProperty(prop) ? user.getProperty(prop).getString() : null);
                if (StringUtils.isNotEmpty(value) && pattern.matcher(value).matches()) {
                    matches = true;
                    break;
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return matches;
    }

}
