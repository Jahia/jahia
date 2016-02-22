/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.sites.JahiaSitesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.services.render.RenderException;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.*;
import org.jahia.utils.Patterns;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Separate servlet to search for users and/or groups
 *
 * @author loom
 *         Date: Jun 16, 2010
 *         Time: 10:38:40 AM
 */
public class FindPrincipal extends BaseFindController {

    private static Logger logger = LoggerFactory.getLogger(FindPrincipal.class);

    private static final String PRINCIPALTYPE_PARAMNAME = "principalType";
    private static final String WILDCARDTERM_PARAMNAME = "wildcardTerm";
    private static final String ESCAPECOLON_PARAMNAME = "escapeColon";
    private static final String SITEKEY_PARAMNAME = "siteKey";
    private static final String PROPERTYMATCHREGEXP_PARAMNAME = "propertyMatchRegexp";
    private static final String REMOVEDUPLICATEPROPVALUES_PARAMNAME = "removeDuplicatePropValues";
    private static final String INCLUDECRITERIANAMES_PARAMNAME = "includeCriteriaNames";

    private static final Set<String> RESERVED_PARAMETERNAMES = new HashSet<String>();

    static {
        RESERVED_PARAMETERNAMES.add(PRINCIPALTYPE_PARAMNAME);
        RESERVED_PARAMETERNAMES.add(WILDCARDTERM_PARAMNAME);
        RESERVED_PARAMETERNAMES.add(ESCAPECOLON_PARAMNAME);
        RESERVED_PARAMETERNAMES.add(SITEKEY_PARAMNAME);
        RESERVED_PARAMETERNAMES.add(PROPERTYMATCHREGEXP_PARAMNAME);
        RESERVED_PARAMETERNAMES.add(REMOVEDUPLICATEPROPVALUES_PARAMNAME);
        RESERVED_PARAMETERNAMES.add(INCLUDECRITERIANAMES_PARAMNAME);
    }

    private JahiaUserManagerService jahiaUserManagerService;
    private JahiaGroupManagerService jahiaGroupManagerService;
    private JahiaSitesService jahiaSitesService;

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    public void setJahiaGroupManagerService(JahiaGroupManagerService jahiaGroupManagerService) {
        this.jahiaGroupManagerService = jahiaGroupManagerService;
    }

    public void setJahiaSitesService(JahiaSitesService jahiaSitesService) {
        this.jahiaSitesService = jahiaSitesService;
    }

    protected String expandRequestMarkers(HttpServletRequest request, String sourceString) {
        String result = new String(sourceString);
        int refMarkerPos = result.indexOf("{$");
        while (refMarkerPos >= 0) {
            int endRefMarkerPos = result.indexOf("}", refMarkerPos);
            if (endRefMarkerPos > 0) {
                String refName = result.substring(refMarkerPos + 2, endRefMarkerPos);
                String refValue = request.getParameter(refName);
                if (refValue != null) {
                     result = StringUtils.replace(result, "{$" + refName + "}", refValue);
                } else {
                    // the request parameter wasn't found, so we leave the marker as it is, simply ignoring it.
                }
            }
            refMarkerPos = result.indexOf("{$", refMarkerPos + 2);
        }
        return result;
    }        

    protected String retrieveParameter(HttpServletRequest request, HttpServletResponse response, String parameterName, boolean mandatory) throws IOException {
        String parameterValue = request.getParameter(parameterName);
        if (!StringUtils.isEmpty(parameterValue)) {
            parameterValue = expandRequestMarkers(request, parameterValue);
        }
        if (mandatory) {
            if (StringUtils.isEmpty(parameterValue)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Mandatory parameter '"+parameterName+"' is not found in the request");
                throw new IOException("Mandatory parameter '"+parameterName+"' is not found in the request");
            }
        }
        return parameterValue;
    }

    protected Map<String, String[]> retrieveOtherParameters(HttpServletRequest request) throws IOException {
        Map<String, String[]> parameterMap = new HashMap<String, String[]>(request.getParameterMap());
        for (String reservedParameterName : RESERVED_PARAMETERNAMES) {
            parameterMap.remove(reservedParameterName);
        }
        return parameterMap;
    }

    protected Properties buildSearchCriterias(String wildcardTerm, Map<String, String[]> otherRequestParameters, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Properties criterias = new Properties();
        if (wildcardTerm != null) {
            criterias.setProperty("*", wildcardTerm);
        }
        String includeCriteriaNames = retrieveParameter(request, response, INCLUDECRITERIANAMES_PARAMNAME, false);
        Set<String> criteriasToInclude = new HashSet<String>();
        if (includeCriteriaNames != null) {
            if (includeCriteriaNames.indexOf(",") >= 0) {
                String[] criteriaNamesArray = Patterns.COMMA.split(includeCriteriaNames);
                List<String> criteriaNamesList = Arrays.asList(criteriaNamesArray);
                criteriasToInclude.addAll(criteriaNamesList);
            } else {
                criteriasToInclude.add(includeCriteriaNames);
            }
        }

        for (Map.Entry<String, String[]> curEntry : otherRequestParameters.entrySet()) {
            String[] paramValues = curEntry.getValue();
            if (criteriasToInclude.size() > 0) {
                if (!criteriasToInclude.contains(curEntry.getKey())) {
                    logger.debug("Ignoring parameter with name " + curEntry.getKey() + " since it wasn't specified in the include criteria name list");
                    continue;
                }
            }
            if (paramValues.length < 1) {
                logger.warn("Parameter " + curEntry.getKey() + " has invalid value(s), ignoring it.");
                continue;
            } else if (paramValues.length > 1) {
                logger.warn("Parameter " + curEntry.getKey() + " has more than one value, only the first one will be used.");
                continue;
            }
            criterias.setProperty(curEntry.getKey(), expandRequestMarkers(request, paramValues[0]));
        }
        return criterias;
    }

    private void writeUserResults(Set<JCRUserNode> users, HttpServletRequest request, HttpServletResponse response, String propertyMatchRegexp, boolean removeDuplicatePropValues)
            throws RepositoryException, IllegalArgumentException, IOException, RenderException, JSONException {
        response.setContentType("application/json; charset=UTF-8");

        JSONArray results = new JSONArray();

        Map<String, String> alreadyIncludedPropertyValues = null;
        if (removeDuplicatePropValues) {
            alreadyIncludedPropertyValues = new HashMap<String, String>();
        }

        Pattern pattern = propertyMatchRegexp != null ? Pattern.compile(propertyMatchRegexp, Pattern.CASE_INSENSITIVE) : null;
        for (JCRUserNode user : users) {
            JSONObject jsonPrincipal = new JSONObject(user.getJahiaUser());
            if (pattern != null) {
                PropertyIterator userProperties = user.getProperties();
                Set<String> matchingProperties = new HashSet<String>();
                while (userProperties.hasNext()) {
                    Property curProperty = userProperties.nextProperty();
                    String curPropertyName = curProperty.getName();
                    String curPropertyValue = curProperty.getValue().getString();
                    if (pattern.matcher(curPropertyValue).matches()) {
                        if (alreadyIncludedPropertyValues != null) {
                            String nodeIdentifier = alreadyIncludedPropertyValues.get(curPropertyValue);
                            if (nodeIdentifier != null) {
                                if (!nodeIdentifier.equals(user.getPath())) {
                                    // This property value already exists and comes from another node.
                                    break;
                                }
                            } else {
                                alreadyIncludedPropertyValues.put(curPropertyValue, user.getPath());
                            }
                        }
                        // property starts with the propertyMatchRegexp, let's add it to the list of matching properties.
                        matchingProperties.add(curPropertyName);
                    }

                }
                jsonPrincipal.put("matchingProperties", new JSONArray(matchingProperties));
            }
            results.put(jsonPrincipal);
        }
        try {
            results.write(response.getWriter());
        } catch (JSONException e) {
            throw new RenderException(e);
        }
    }

    private void writeGroupResults(Set<JCRGroupNode> groups, HttpServletRequest request, HttpServletResponse response, String propertyMatchRegexp, boolean removeDuplicatePropValues)
            throws RepositoryException, IllegalArgumentException, IOException, RenderException, JSONException {
        response.setContentType("application/json; charset=UTF-8");

        JSONArray results = new JSONArray();

        Map<String, String> alreadyIncludedPropertyValues = null;
        if (removeDuplicatePropValues) {
            alreadyIncludedPropertyValues = new HashMap<String, String>();
        }

        for (JCRGroupNode jahiaGroup : groups) {
            JSONObject jsonGroup = new JSONObject(jahiaGroup.getJahiaGroup());
            if (propertyMatchRegexp != null) {
                Pattern pattern = Pattern.compile(propertyMatchRegexp, Pattern.CASE_INSENSITIVE);
                PropertyIterator groupProperties = jahiaGroup.getProperties();
                Set<String> matchingProperties = new HashSet<String>();
                while(groupProperties.hasNext()) {
                    Property curProperty = groupProperties.nextProperty();
                    String curPropertyName = curProperty.getName();
                    String curPropertyValue = curProperty.getValue().getString();
                    if (pattern.matcher(curPropertyValue).matches()) {
                        if (alreadyIncludedPropertyValues != null) {
                            String nodeIdentifier = alreadyIncludedPropertyValues.get(curPropertyValue);
                            if (nodeIdentifier != null) {
                                if (!nodeIdentifier.equals(jahiaGroup.getPath())) {
                                    // This property value already exists and comes from another node.
                                    break;
                                }
                            } else {
                                alreadyIncludedPropertyValues.put(curPropertyValue, jahiaGroup.getPath());
                            }
                        }
                        // property starts with the propertyMatchRegexp, let's add it to the list of matching properties.
                        matchingProperties.add(curPropertyName);
                    }
    
                }
                jsonGroup.put("matchingProperties", new JSONArray(matchingProperties));
            }

            results.put(jsonGroup);
        }

        try {
            results.write(response.getWriter());
        } catch (JSONException e) {
            throw new RenderException(e);
        }
    }


    @Override
    protected void handle(HttpServletRequest request, HttpServletResponse response) throws RenderException,
            IOException, RepositoryException, JahiaForbiddenAccessException {
        
        checkUserLoggedIn();
        checkUserAuthorized();
        
        try {
            String principalType = retrieveParameter(request, response, PRINCIPALTYPE_PARAMNAME, true);
            if (principalType == null) {
                return;
            }
            String wildcardTerm = retrieveParameter(request, response, WILDCARDTERM_PARAMNAME, false);
            String propertyMatchRegExp = retrieveParameter(request, response, PROPERTYMATCHREGEXP_PARAMNAME, false);
            String removeDuplicatePropValuesStr = retrieveParameter(request, response, REMOVEDUPLICATEPROPVALUES_PARAMNAME, false);
            boolean removeDuplicatePropValues = false;
            if (removeDuplicatePropValuesStr != null) {
                removeDuplicatePropValues = Boolean.parseBoolean(removeDuplicatePropValuesStr);
            }
            boolean siteKeyMandatory = false;
            if ("groups".equals(principalType)) {
                siteKeyMandatory = true;
            }
            String siteKey = retrieveParameter(request, response, SITEKEY_PARAMNAME, siteKeyMandatory);
            Map<String, String[]> otherRequestParameters = retrieveOtherParameters(request);
            Properties searchCriterias = buildSearchCriterias(wildcardTerm, otherRequestParameters, request, response);
            if (logger.isDebugEnabled()) {
                logger.debug("Searching for principal type " + principalType + " with criterias " + searchCriterias);
            }

            if ("users".equals(principalType)) {
                Set<JCRUserNode> result = jahiaUserManagerService.searchUsers(searchCriterias);
                writeUserResults(result, request, response, propertyMatchRegExp, removeDuplicatePropValues);
            } else if ("groups".equals(principalType)) {
                if (siteKey == null) {
                    logger.error("Site key is mandatory for group searching and is missing in request, aborting searching...");
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                JahiaSite site = null;
                try {
                    site = jahiaSitesService.getSiteByKey(siteKey);
                } catch (JahiaException e) {
                    logger.error("Error while trying to retrieve site with key " + siteKey + ", aborting search... ", e);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                Set<JCRGroupNode> result = jahiaGroupManagerService.searchGroups(site.getSiteKey(), searchCriterias);
                writeGroupResults(result, request, response, propertyMatchRegExp, removeDuplicatePropValues);
            } else {
                logger.error("Principal type value " + principalType + " is invalid, aborting searching...");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

        } catch (JSONException jsone) {
            logger.error("JSON serialization error ", jsone);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, jsone.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    public static String getFindPrincipalServletPath() {
        // TODO move this into configuration
        return "/cms/findPrincipal";
    }


}
