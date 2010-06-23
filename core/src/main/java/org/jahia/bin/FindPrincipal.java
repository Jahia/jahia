package org.jahia.bin;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.QueryParser;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.URLResolver;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.*;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.jcr.*;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;

/**
 * Seperate servlet to search for users and/or groups
 *
 * @author loom
 *         Date: Jun 16, 2010
 *         Time: 10:38:40 AM
 */
public class FindPrincipal extends HttpServlet implements Controller {

    private static Logger logger = Logger.getLogger(FindPrincipal.class);

    private static final String PRINCIPALTYPE_PARAMNAME = "principalType";
    private static final String WILDCARDTERM_PARAMNAME = "wildcardTerm";
    private static final String ESCAPECOLON_PARAMNAME = "escapeColon";
    private static final String SITEKEY_PARAMNAME = "siteKey";
    private static final String PROPERTYMATCHREGEXP_PARAMNAME = "propertyMatchRexexp";
    private static final String REMOVEDUPLICATEPROPVALUES_PARAMNAME = "removeDuplicatePropValues";

    private static final Set<String> RESERVED_PARAMETERNAMES = new HashSet<String>();

    static {
        RESERVED_PARAMETERNAMES.add(PRINCIPALTYPE_PARAMNAME);
        RESERVED_PARAMETERNAMES.add(WILDCARDTERM_PARAMNAME);
        RESERVED_PARAMETERNAMES.add(ESCAPECOLON_PARAMNAME);
        RESERVED_PARAMETERNAMES.add(SITEKEY_PARAMNAME);
        RESERVED_PARAMETERNAMES.add(PROPERTYMATCHREGEXP_PARAMNAME);
        RESERVED_PARAMETERNAMES.add(REMOVEDUPLICATEPROPVALUES_PARAMNAME);
    }

    private JahiaUserManagerService jahiaUserManagerService;
    private JahiaGroupManagerService jahiaGroupManagerService;
    private JahiaSitesService jahiaSitesService;

    private boolean defaultEscapeColon = false;

    public void setDefaultEscapeColon(boolean defaultEscapeColon) {
        this.defaultEscapeColon = defaultEscapeColon;
    }

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

    protected Map<String, String[]> retrieveOtherParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = new HashMap<String, String[]>(request.getParameterMap());
        for (String reservedParameterName : RESERVED_PARAMETERNAMES) {
            parameterMap.remove(reservedParameterName);
        }
        return parameterMap;
    }

    protected Properties buildSearchCriterias(String wildcardTerm, Map<String, String[]> otherRequestParameters, HttpServletRequest request) {
        Properties criterias = new Properties();
        if (wildcardTerm != null) {
            criterias.setProperty("*", wildcardTerm);
        }
        for (Map.Entry<String, String[]> curEntry : otherRequestParameters.entrySet()) {
            String[] paramValues = curEntry.getValue();
            if (paramValues.length < 1) {
                logger.warn("Parameter " + curEntry.getKey() + " has invalid value(s), ignoring it.");
            } else if (paramValues.length > 1) {
                logger.warn("Parameter " + curEntry.getKey() + " has more than one value, only the first one will be used.");
            }
            criterias.setProperty(curEntry.getKey(), expandRequestMarkers(request, paramValues[0]));
        }
        return criterias;
    }

    private void writeUserResults(Set<Principal> users, HttpServletRequest request, HttpServletResponse response, String propertyMatchRegexp, boolean removeDuplicatePropValues)
            throws RepositoryException, IllegalArgumentException, IOException, RenderException, JSONException {
        response.setContentType("application/json; charset=UTF-8");
        boolean escape = Boolean.valueOf(StringUtils.defaultIfEmpty(request.getParameter("escapeColon"), String
                .valueOf(defaultEscapeColon)));

        JSONArray results = new JSONArray();

        Map<String, String> alreadyIncludedPropertyValues = null;
        if (removeDuplicatePropValues) {
            alreadyIncludedPropertyValues = new HashMap<String, String>();
        }

        for (Principal principal : users) {
            JSONObject jsonPrincipal = new JSONObject(principal);
            if (principal instanceof JahiaUser) {
                JahiaUser jahiaUser = (JahiaUser) principal;
                Properties userProperties = jahiaUser.getProperties();
                Set<String> matchingProperties = new HashSet<String>();
                for (Map.Entry curPropertyEntry : userProperties.entrySet()) {
                    String curPropertyName = (String) curPropertyEntry.getKey();
                    String curPropertyValue = (String) curPropertyEntry.getValue();
                    if ((propertyMatchRegexp != null) && (curPropertyValue.matches(propertyMatchRegexp))) {
                        if (alreadyIncludedPropertyValues != null) {
                            String nodeIdentifier = alreadyIncludedPropertyValues.get(curPropertyValue);
                            if (nodeIdentifier != null) {
                                if (!nodeIdentifier.equals(jahiaUser.getUserKey())) {
                                    // This property value already exists and comes from another node.
                                    break;
                                }
                            } else {
                                alreadyIncludedPropertyValues.put(curPropertyValue, jahiaUser.getUserKey());
                            }
                        }
                        // property starts with the propertyMatchRegexp, let's add it to the list of matching properties.
                        matchingProperties.add(curPropertyName);
                    }

                }
                if (propertyMatchRegexp != null) {
                    jsonPrincipal.put("matchingProperties", new JSONArray(matchingProperties));
                }
            }
            results.put(jsonPrincipal);
        }

        try {
            results.write(response.getWriter());
        } catch (JSONException e) {
            throw new RenderException(e);
        }
    }

    private void writeGroupResults(Set<JahiaGroup> groups, HttpServletRequest request, HttpServletResponse response, String propertyMatchRegexp, boolean removeDuplicatePropValues)
            throws RepositoryException, IllegalArgumentException, IOException, RenderException, JSONException {
        response.setContentType("application/json; charset=UTF-8");
        boolean escape = Boolean.valueOf(StringUtils.defaultIfEmpty(request.getParameter("escapeColon"), String
                .valueOf(defaultEscapeColon)));

        JSONArray results = new JSONArray();

        Map<String, String> alreadyIncludedPropertyValues = null;
        if (removeDuplicatePropValues) {
            alreadyIncludedPropertyValues = new HashMap<String, String>();
        }

        for (JahiaGroup jahiaGroup : groups) {
            JSONObject jsonGroup = new JSONObject(jahiaGroup);
            Properties userProperties = jahiaGroup.getProperties();
            Set<String> matchingProperties = new HashSet<String>();
            for (Map.Entry curPropertyEntry : userProperties.entrySet()) {
                String curPropertyName = (String) curPropertyEntry.getKey();
                String curPropertyValue = (String) curPropertyEntry.getValue();
                if ((propertyMatchRegexp != null) && (curPropertyValue.matches(propertyMatchRegexp))) {
                    if (alreadyIncludedPropertyValues != null) {
                        String nodeIdentifier = alreadyIncludedPropertyValues.get(curPropertyValue);
                        if (nodeIdentifier != null) {
                            if (!nodeIdentifier.equals(jahiaGroup.getGroupKey())) {
                                // This property value already exists and comes from another node.
                                break;
                            }
                        } else {
                            alreadyIncludedPropertyValues.put(curPropertyValue, jahiaGroup.getGroupKey());
                        }
                    }
                    // property starts with the propertyMatchRegexp, let's add it to the list of matching properties.
                    matchingProperties.add(curPropertyName);
                }

            }
            if (propertyMatchRegexp != null) {
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


    protected void handle(HttpServletRequest request, HttpServletResponse response) throws RenderException,
            IOException, RepositoryException {
        URLResolver urlResolver = new URLResolver(request.getPathInfo());
        try {
            String principalType = retrieveParameter(request, response, PRINCIPALTYPE_PARAMNAME, true);
            if (principalType == null) {
                return;
            }
            String wildcardTerm = retrieveParameter(request, response, WILDCARDTERM_PARAMNAME, false);
            String escapeColonStr = retrieveParameter(request, response, ESCAPECOLON_PARAMNAME, false);
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
            Properties searchCriterias = buildSearchCriterias(wildcardTerm, otherRequestParameters, request);
            if (logger.isDebugEnabled()) {
                logger.debug("Searching for principal type " + principalType + " with criterias " + searchCriterias);
            }

            if ("users".equals(principalType)) {
                Set<Principal> result = jahiaUserManagerService.searchUsers(searchCriterias);
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
                Set<JahiaGroup> result = jahiaGroupManagerService.searchGroups(site.getID(), searchCriterias);
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

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            if (request.getMethod().equals("GET") || request.getMethod().equals("POST")) {
                handle(request, response);
            } else if (request.getMethod().equals("OPTIONS")) {
                response.setHeader("Allow", "GET, OPTIONS, POST");
            } else {
                response.sendError(SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            DefaultErrorHandler.getInstance().handle(e, request, response);
        } finally {
            if (logger.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder(100);
                sb.append("Rendered [").append(request.getRequestURI());
                JahiaUser user = JCRTemplate.getInstance().getSessionFactory().getCurrentUser();
                if (user != null) {
                    sb.append("] user=[").append(user.getUsername());
                }
                sb.append("] ip=[").append(request.getRemoteAddr()).append("] sessionID=[").append(
                        request.getSession(true).getId()).append("] in [").append(
                        System.currentTimeMillis() - startTime).append("ms]");
                logger.info(sb.toString());
            }
        }
        return null;
    }

    public static String getFindPrincipalServletPath() {
        // TODO move this into configuration
        return "/cms/findPrincipal";
    }


}
