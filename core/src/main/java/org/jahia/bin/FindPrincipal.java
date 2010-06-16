package org.jahia.bin;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.URLResolver;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
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

    private static final Set<String> RESERVED_PARAMETERNAMES = new HashSet<String>();

    static {
        RESERVED_PARAMETERNAMES.add(PRINCIPALTYPE_PARAMNAME);
        RESERVED_PARAMETERNAMES.add(WILDCARDTERM_PARAMNAME);
        RESERVED_PARAMETERNAMES.add(ESCAPECOLON_PARAMNAME);
        RESERVED_PARAMETERNAMES.add(SITEKEY_PARAMNAME);
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

    protected String retrieveParameter(HttpServletRequest request, HttpServletResponse response, String parameterName) throws IOException {
        String parameterValue = request.getParameter(PRINCIPALTYPE_PARAMNAME);
        if (StringUtils.isEmpty(parameterValue)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Mandatory parameter '"+PRINCIPALTYPE_PARAMNAME+"' is not found in the request");
            return null;
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

    protected Properties buildSearchCriterias(String wildcardTerm, Map<String, String[]> otherRequestParameters) {
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
            criterias.setProperty(curEntry.getKey(), paramValues[0]);
        }
        return criterias;
    }

    private void writeUserResults(Set<Principal> users, HttpServletRequest request, HttpServletResponse response)
            throws RepositoryException, IllegalArgumentException, IOException, RenderException {
        response.setContentType("application/json; charset=UTF-8");
        boolean escape = Boolean.valueOf(StringUtils.defaultIfEmpty(request.getParameter("escapeColon"), String
                .valueOf(defaultEscapeColon)));

        JSONArray results = new JSONArray();

        for (Principal principal : users) {
            JSONObject jsonPrincipal = new JSONObject(principal);
            results.put(jsonPrincipal);
        }

        try {
            results.write(response.getWriter());
        } catch (JSONException e) {
            throw new RenderException(e);
        }
    }

    private void writeGroupResults(Set<JahiaGroup> groups, HttpServletRequest request, HttpServletResponse response)
            throws RepositoryException, IllegalArgumentException, IOException, RenderException {
        response.setContentType("application/json; charset=UTF-8");
        boolean escape = Boolean.valueOf(StringUtils.defaultIfEmpty(request.getParameter("escapeColon"), String
                .valueOf(defaultEscapeColon)));

        JSONArray results = new JSONArray();

        for (JahiaGroup jahiaGroup : groups) {
            JSONObject jsonGroup = new JSONObject(jahiaGroup);
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
            String principalType = retrieveParameter(request, response, PRINCIPALTYPE_PARAMNAME);
            if (principalType == null) {
                return;
            }
            String wildcardTerm = retrieveParameter(request, response, WILDCARDTERM_PARAMNAME);
            String escapeColonStr = retrieveParameter(request, response, ESCAPECOLON_PARAMNAME);
            String siteKey = retrieveParameter(request, response, SITEKEY_PARAMNAME);
            Map<String, String[]> otherRequestParameters = retrieveOtherParameters(request);
            Properties searchCriterias = buildSearchCriterias(wildcardTerm, otherRequestParameters);
            if (logger.isDebugEnabled()) {
                logger.debug("Search for principal type " + principalType + " with criterias " + searchCriterias);
            }

            if ("users".equals(principalType)) {
                Set<Principal> result = jahiaUserManagerService.searchUsers(searchCriterias);
                writeUserResults(result, request, response);
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
                writeGroupResults(result, request, response);
            } else {
                logger.error("Principal type value " + principalType + " is invalid, aborting searching...");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

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

}
