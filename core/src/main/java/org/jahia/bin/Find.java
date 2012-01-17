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

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import javax.jcr.*;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.URLResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.queryParser.QueryParser;
import org.jahia.api.Constants;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.URLResolver;
import org.jahia.services.usermanager.JahiaUser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;

/**
 * A small servlet to allow us to perform queries on the JCR.
 * @author loom
 * Date: Jan 26, 2010
 * Time: 5:55:17 PM
 */
public class Find extends BaseFindController {

    private static Logger logger = LoggerFactory.getLogger(Find.class);

    private int defaultDepthLimit = 1;

    private boolean defaultEscapeColon = false;

    private boolean defaultRemoveDuplicatePropertyValues = false;

    private URLResolverFactory urlResolverFactory;

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
    }

    private int getInt(String paramName, int defaultValue, HttpServletRequest req) throws IllegalArgumentException {
        int param = defaultValue;
        String valueStr = req.getParameter(paramName);
        if (StringUtils.isNotEmpty(valueStr)) {
            try {
                param = Integer.parseInt(valueStr);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Invalid integer value '" + valueStr + "' for request parameter '"
                        + paramName + "'", nfe);
            }
        }

        return param;
    }

    private Query getQuery(HttpServletRequest request, HttpServletResponse response, String workspace, Locale locale)
            throws IOException, RepositoryException {

        QueryManager qm = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale).getWorkspace()
                .getQueryManager();
        if (qm == null) {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            return null;
        }

        String query = request.getParameter("query");
        if (StringUtils.isEmpty(query)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Mandatory parameter 'query' is not found in the request");
            return null;
        }

        // now let's parse the query to see if it references any other request parameters, and replace the reference with
        // the actual value.

        query = expandRequestMarkers(request, query, true, StringUtils.defaultIfEmpty(request.getParameter("language"), Query.JCR_SQL2), false);
        logger.debug("Using expanded query=[{}]", query);

        Query q = qm.createQuery(query, StringUtils.defaultIfEmpty(request.getParameter("language"), Query.JCR_SQL2));

        int limit = getInt("limit", defaultLimit, request);
        if (limit <= 0 || limit > hardLimit) {
            limit = hardLimit;
        }
        
        int offset = getInt("offset", 0, request);

        if (limit > 0) {
            q.setLimit(limit);
        }
        if (offset > 0) {
            q.setOffset(offset);
        }

        return q;
    }

    protected String expandRequestMarkers(HttpServletRequest request, String sourceString, boolean escapeValue, String queryLanguage, boolean escapeForRegexp) {
        String result = new String(sourceString);
        int refMarkerPos = result.indexOf("{$");
        while (refMarkerPos >= 0) {
            int endRefMarkerPos = result.indexOf("}", refMarkerPos);
            if (endRefMarkerPos > 0) {
                String refName = result.substring(refMarkerPos + 2, endRefMarkerPos);
                String refValue = request.getParameter(refName);
                if (refValue != null) {
                     // now it's very important that we escape it properly to avoid injection security holes
                    if (escapeValue) {
                        refValue = QueryParser.escape(refValue);
                        if (Query.XPATH.equals(queryLanguage)) {
                            // found this here : http://markmail.org/thread/pd7myawyv2dadmdh
                            refValue = StringUtils.replace(refValue,"'", "\\'");
                        } else {
                        }
                        refValue = StringUtils.replace(refValue, "'", "''");
                    }
                    if (escapeForRegexp) {
                        refValue = Pattern.quote(refValue);
                    }
                     result = StringUtils.replace(result, "{$" + refName + "}", refValue);
                } else {
                    // the request parameter wasn't found, so we leave the marker as it is, simply ignoring it.
                }
            }
            refMarkerPos = result.indexOf("{$", refMarkerPos + 2);
        }
        return result;
    }

    protected void handle(HttpServletRequest request, HttpServletResponse response) throws RenderException,
            IOException, RepositoryException {
        URLResolver urlResolver = urlResolverFactory.createURLResolver(request.getPathInfo(), request.getServerName(), request);
        try {
            Query query = getQuery(request, response, urlResolver.getWorkspace(), urlResolver.getLocale());
            if (query == null) {
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Executing " + query.getLanguage() + " for workspace '" + urlResolver.getWorkspace() + "' and locale '"
                        + urlResolver.getLocale() + "'. Statement: " + query.getStatement());
            }
            writeResults(query.execute(), request, response, query.getLanguage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (InvalidQueryException e) {
            logger.error("Invalid query", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long startTime = System.currentTimeMillis();
        String sessionId = null;
        try {
            if (logger.isInfoEnabled()) {
                sessionId = request.getSession().getId();
            }
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
                        sessionId).append("] in [").append(
                        System.currentTimeMillis() - startTime).append("ms]");
                logger.info(sb.toString());
            }
        }
        return null;
    }

    private JSONObject serializeNode(Node currentNode, int depthLimit, boolean escapeColon, Pattern propertyMatchRegexp, Map<String, String> alreadyIncludedPropertyValues) throws RepositoryException,
            JSONException {
        final PropertyIterator stringMap = currentNode.getProperties();
        JSONObject jsonObject = new JSONObject();
        // Map<String,Object> map = new HashMap<String, Object>();
        Set<String> matchingProperties = new HashSet<String>();
        while (stringMap.hasNext()) {
            JCRPropertyWrapper propertyWrapper = (JCRPropertyWrapper) stringMap.next();
            final int type = propertyWrapper.getType();
            final String name = escapeColon ? JCRContentUtils.replaceColon(propertyWrapper.getName()) : propertyWrapper.getName();
            if (type == PropertyType.BINARY) {
                continue;
            }
            if (type == PropertyType.WEAKREFERENCE || type == PropertyType.REFERENCE) {
                if (!propertyWrapper.isMultiple()) {
                    jsonObject.put(name, ((JCRNodeWrapper) propertyWrapper.getNode()).getUrl());
                }
            } else {
                if (!propertyWrapper.isMultiple()) {
                    jsonObject.put(name, propertyWrapper.getValue().getString());
                    // @todo this code is duplicated for multiple values, we need to clean this up.
                    if (propertyMatchRegexp != null && propertyMatchRegexp.matcher(propertyWrapper.getValue().getString()).matches()) {
                        if (alreadyIncludedPropertyValues != null) {
                            String nodeIdentifier = alreadyIncludedPropertyValues.get(propertyWrapper.getValue().getString());
                            if (nodeIdentifier != null) {
                                if (!nodeIdentifier.equals(currentNode.getIdentifier())) {
                                    // This property value already exists and comes from another node.
                                    return null;
                                }
                            } else {
                                alreadyIncludedPropertyValues.put(propertyWrapper.getValue().getString(), currentNode.getIdentifier());
                            }
                        }
                        // property starts with the propertyMatchRegexp, let's add it to the list of matching properties.
                        matchingProperties.add(name);
                    }
                } else {
                    JSONArray jsonArray = new JSONArray();
                    Value[] propValues = propertyWrapper.getValues();
                    for (Value propValue : propValues) {
                        jsonArray.put(propValue.getString());
                        if (propertyMatchRegexp != null && propertyMatchRegexp.matcher(propValue.getString()).matches()) {
                            if (alreadyIncludedPropertyValues != null) {
                                String nodeIdentifier = alreadyIncludedPropertyValues.get(propValue.getString());
                                if (nodeIdentifier != null) {
                                    if (!nodeIdentifier.equals(currentNode.getIdentifier())) {
                                        // This property value already exists and comes from another node.
                                        return null;
                                    }
                                } else {
                                    alreadyIncludedPropertyValues.put(propValue.getString(), currentNode.getIdentifier());
                                }
                            }
                            // property starts with the propertyMatchRegexp, let's add it to the list of matching properties.
                            matchingProperties.add(name);
                        }
                    }
                    jsonObject.put(name, jsonArray);
                }
            }
        }
        // now let's output some node information.
        jsonObject.put("path", currentNode.getPath());
        jsonObject.put("identifier", currentNode.getIdentifier());
        jsonObject.put("index", currentNode.getIndex());
        jsonObject.put("depth", currentNode.getDepth());
        jsonObject.put("nodename", currentNode.getName());
        jsonObject.put("primaryNodeType", currentNode.getPrimaryNodeType().getName());
        if (propertyMatchRegexp != null) {
            jsonObject.put("matchingProperties", new JSONArray(matchingProperties));
        }

        // now let's output the children until we reach the depth limit.
        if ((depthLimit - 1) > 0) {
            final NodeIterator childNodeIterator = currentNode.getNodes();
            JSONArray childMapList = new JSONArray();
            while (childNodeIterator.hasNext()) {
                Node currentChildNode = childNodeIterator.nextNode();
                JSONObject childSerializedMap = serializeNode(currentChildNode, depthLimit - 1, escapeColon, propertyMatchRegexp, alreadyIncludedPropertyValues);
                childMapList.put(childSerializedMap);
            }
            jsonObject.put("childNodes", childMapList);
        }
        return jsonObject;
    }

    private JSONObject serializeRow(Row row, String[] columns, int depthLimit, boolean escapeColon, Set<String> alreadyIncludedIdentifiers, Pattern propertyMatchRegexp, Map<String, String> alreadyIncludedPropertyValues) throws RepositoryException,
            JSONException {

        JSONObject jsonObject = new JSONObject();

        Node currentNode = row.getNode();
        if (currentNode != null) {
            if (currentNode.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                try {
                    currentNode = currentNode.getParent();
                    if (alreadyIncludedIdentifiers.contains(currentNode.getIdentifier())) {
                        // avoid duplicates due to j:translation nodes.
                        return null;
                    }
                    JSONObject serializedNode = serializeNode(currentNode, depthLimit, escapeColon, propertyMatchRegexp, alreadyIncludedPropertyValues);
                    if (serializedNode == null) {
                        return null;
                    }
                    jsonObject.put("node", serializedNode);
                    alreadyIncludedIdentifiers.add(currentNode.getIdentifier());
                } catch (ItemNotFoundException e) {
                    currentNode = null;
                }
            } else {
                if (alreadyIncludedIdentifiers.contains(currentNode.getIdentifier())) {
                    // avoid duplicates due to j:translation nodes.
                    return null;
                }
                JSONObject serializedNode = serializeNode(currentNode, depthLimit, escapeColon, propertyMatchRegexp, alreadyIncludedPropertyValues);
                if (serializedNode == null) {
                    return null;
                }
                jsonObject.put("node", serializedNode);
                alreadyIncludedIdentifiers.add(currentNode.getIdentifier());
            }

        }

        for (String column : columns) {
            try {
                Value value = row.getValue(column);
                jsonObject.put(escapeColon ? JCRContentUtils.replaceColon(column) : column, value != null ? value.getString() : value);
            } catch (ItemNotFoundException infe) {
                logger.warn("No value found for column " + column);
            } catch (PathNotFoundException pnfe) {
                logger.warn("No value found for column " + column);                
            }
        }


        return jsonObject;
    }

    /**
     * @param defaultDepthLimit the defaultDepthLimit to set
     */
    public void setDefaultDepthLimit(int defaultDepthLimit) {
        this.defaultDepthLimit = defaultDepthLimit;
    }

    /**
     * @param defaultEscapeColon the defaultEscapeColon to set
     */
    public void setDefaultEscapeColon(boolean defaultEscapeColon) {
        this.defaultEscapeColon = defaultEscapeColon;
    }

    public boolean isDefaultRemoveDuplicatePropertyValues() {
        return defaultRemoveDuplicatePropertyValues;
    }

    public void setDefaultRemoveDuplicatePropertyValues(boolean defaultRemoveDuplicatePropertyValues) {
        this.defaultRemoveDuplicatePropertyValues = defaultRemoveDuplicatePropertyValues;
    }

    private void writeResults(QueryResult result, HttpServletRequest request, HttpServletResponse response, String queryLanguage)
            throws RepositoryException, IllegalArgumentException, IOException, RenderException {
        response.setContentType("application/json; charset=UTF-8");
        int depth = getInt("depthLimit", defaultDepthLimit, request);
        boolean escape = Boolean.valueOf(StringUtils.defaultIfEmpty(request.getParameter("escapeColon"), String
                .valueOf(defaultEscapeColon)));
        boolean removeDuplicatePropertyValues = Boolean.valueOf(StringUtils.defaultIfEmpty(request.getParameter("removeDuplicatePropValues"), String
                .valueOf(defaultRemoveDuplicatePropertyValues)));

        Pattern propertyMatchRegexp = null;
        String propertyMatchRegexpString = request.getParameter("propertyMatchRegexp");
        if (propertyMatchRegexpString != null) {
            String expandedPattern = expandRequestMarkers(request, propertyMatchRegexpString, false, queryLanguage, true);
            propertyMatchRegexp = Pattern.compile(expandedPattern, Pattern.CASE_INSENSITIVE);
        }

        JSONArray results = new JSONArray();
        
        try {
            String[] columns = result.getColumnNames();
            boolean serializeRows = !Boolean.parseBoolean(request.getParameter("getNodes")) && columns.length > 0 && !columns[0].contains("*");

            Set<String> alreadyIncludedIdentifiers = new HashSet<String>();
            Map<String, String> alreadyIncludedPropertyValues = null;
            if (removeDuplicatePropertyValues) {
                alreadyIncludedPropertyValues = new HashMap<String, String>();
            }
            if (serializeRows) {
                logger.debug("Serializing rows into JSON result structure...");
                RowIterator rows = result.getRows();
                int resultCount = 0;
                while (rows.hasNext()) {
                    Row row = rows.nextRow();
                    JSONObject serializedRow = serializeRow(row, columns, depth, escape, alreadyIncludedIdentifiers, propertyMatchRegexp, alreadyIncludedPropertyValues);
                    if (serializedRow != null) {
                        results.put(serializedRow);
                        resultCount++;
                    }
                }
                logger.debug("Found " + resultCount + " results.");
            } else {
                logger.debug("Serializing nodes into JSON result structure...");
                NodeIterator nodes = result.getNodes();
                int resultCount = 0;
                while (nodes.hasNext()) {
                    Node nextNode = nodes.nextNode();
                    JSONObject serializedNode = serializeNode(nextNode, depth, escape, propertyMatchRegexp, alreadyIncludedPropertyValues);
                    if (serializedNode != null) {
                        results.put(serializedNode);
                        resultCount++;
                    }
                }
                logger.debug("Found " + resultCount + " results.");
            }
            results.write(response.getWriter());
        } catch (JSONException e) {
            throw new RenderException(e);
        }
    }

    public static String getFindServletPath() {
        // TODO move this into configuration
        return "/cms/find";
    }

}
