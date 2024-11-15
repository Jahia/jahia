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
package org.jahia.bin;

import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.Patterns;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Controller for performing user search.
 *
 * @author Sergiy Shyrkov
 */
public class FindUser extends BaseFindController {

    private static final Logger logger = LoggerFactory.getLogger(FindUser.class);

    public static String getFindUserServletPath() {
        // TODO move this into configuration
        return "/cms/findUser";
    }

    protected Set<String> displayProperties = new HashSet<String>(Arrays.asList("j:lastName", "j:firstName"));

    protected Set<String> searchProperties = new HashSet<String>(Arrays.asList("j:lastName", "j:firstName", "username"));

    protected JahiaUserManagerService userService;

    protected Properties buildSearchCriteria(String queryTerm) {
        Properties props = new Properties();
        for (String key : searchProperties) {
            props.put(key, queryTerm.contains("*") ? queryTerm : queryTerm + "*");
        }

        return props;
    }

    @Override
    protected void handle(HttpServletRequest request, HttpServletResponse response)
            throws JahiaForbiddenAccessException, IOException, JSONException {
        checkUserLoggedIn();
        checkUserAuthorized();

        boolean badQuery = false;
        String queryTerm = getParameter(request, "q");
        if (queryTerm.length() < 1 || Patterns.STAR.matcher(queryTerm).replaceAll("").trim().length() < 1) {
            badQuery = true;
            if (!Boolean.valueOf(getParameter(request, "emptyResultForBadQuery", "false"))) {
                throw new JahiaBadRequestException("Please specify more exact term for user search");
            }
        }

        Set<JCRNodeWrapper> result = !badQuery ? search(queryTerm, request) : Collections.<JCRNodeWrapper>emptySet();
        JCRNodeWrapper jcrNodeWrapper = null;
        String roles = null;
        if (!result.isEmpty()) {
            if (request.getParameter("node") != null) {
                try {
                    jcrNodeWrapper = JCRSessionFactory
                            .getInstance().getCurrentUserSession().getNode(request.getParameter("node"));
                } catch (RepositoryException e) {
                    jcrNodeWrapper = null;
                }
            }
            roles = request.getParameter("roles");
        }
        writeResults(result, request, response, jcrNodeWrapper, roles);
    }

    protected Set<JCRNodeWrapper> search(String queryTerm, HttpServletRequest request) {
        return new HashSet<JCRNodeWrapper>(searchUsers(queryTerm));
    }

    protected Set<JCRUserNode> searchUsers(String queryTerm) {
        Properties searchCriterias = buildSearchCriteria(queryTerm);

        if (logger.isDebugEnabled()) {
            logger.debug("Performing user search using criteria: {}", searchCriterias);
        }

        Set<JCRUserNode> result = userService.searchUsers(searchCriterias);

        if (logger.isDebugEnabled()) {
            logger.debug("Found {} matching users", result.size());
        }

        return result;
    }

    public void setDisplayProperties(Set<String> displayProperties) {
        this.displayProperties = displayProperties;
    }

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.userService = jahiaUserManagerService;
    }

    public void setSearchProperties(Set<String> searchFields) {
        this.searchProperties = searchFields;
    }

    protected void sortResults(List<JSONObject> jsonResults) {
        // no sorting here
    }

    protected JSONObject toJSON(JCRUserNode user) throws JSONException {
        JSONObject json = new JSONObject();

        if (user != null) {
            json.put("userKey", user.getUserKey());
            json.put("username", user.getName());

            for (String key : displayProperties) {
                String value = user.getPropertyAsString(key);
                if (value != null) {
                    json.put(key, value);
                }
            }
        }
        return json;
    }

    protected JSONObject toJSON(JCRNodeWrapper principal) throws JSONException {
        return toJSON((JCRUserNode) principal);
    }

    protected void writeResults(Set<JCRNodeWrapper> users, HttpServletRequest request, HttpServletResponse response,
                                JCRNodeWrapper jcrNodeWrapper, String roles) throws IOException, JSONException {

        response.setContentType("application/json; charset=UTF-8");

        List<JSONObject> jsonResults = new LinkedList<JSONObject>();
        int count = 0;

        List<Map<String, Object>> rolesForNode = Collections.emptyList();
        if (jcrNodeWrapper != null && roles != null) {
            rolesForNode = JCRContentUtils.getRolesForNode(jcrNodeWrapper, true, true, roles, 0, false);
        }

        for (JCRNodeWrapper user : users) {
            if (jcrNodeWrapper != null && roles != null) {
                for (Map<String, Object> stringObjectMap : rolesForNode) {
                    if (stringObjectMap.get("principalType").equals("user") && stringObjectMap.get("principal").equals(user)) {
                        jsonResults.add(toJSON(user));
                        count++;
                    }
                }
            } else {
                jsonResults.add(toJSON(user));
                count++;
            }
            if (count >= hardLimit) {
                logger.info("{} principals were found, limiting results to {}", users.size(), hardLimit);
                break;
            }
        }

        sortResults(jsonResults);

        JSONArray results = new JSONArray(jsonResults);

        results.write(response.getWriter());
    }

}
