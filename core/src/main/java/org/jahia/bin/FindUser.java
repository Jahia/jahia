/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
