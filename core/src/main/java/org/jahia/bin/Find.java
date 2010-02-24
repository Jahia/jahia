package org.jahia.bin;

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static org.jahia.api.Constants.LIVE_WORKSPACE;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.jcr.*;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.QueryParser;
import org.jahia.api.Constants;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderException;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * A small servlet to allow us to perform queries on the JCR.
 * User: loom
 * Date: Jan 26, 2010
 * Time: 5:55:17 PM
 */
public class Find extends HttpServlet implements Controller {

    private static Logger logger = Logger.getLogger(Find.class);

    private int defaultDepthLimit = 1;

    private boolean defaultEscapeColon = true;

    private int defaultLimit = 0;

    private String defaultLocale = "en";

    private String defaultWorkspace = LIVE_WORKSPACE;

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

        int refMarkerPos = query.indexOf("{$");
        while (refMarkerPos >= 0) {
            int endRefMarkerPos = query.indexOf("}", refMarkerPos);
            if (endRefMarkerPos > 0) {
                String refName = query.substring(refMarkerPos + 2, endRefMarkerPos);
                String refValue = request.getParameter(refName);
                if (refValue != null) {
                     // now it's very important that we escape it properly to avoid injection security holes
                     refValue = refValue.replaceAll("'", "\\'");
                     refValue = refValue.replaceAll("%", "\\%");
                     // todo we might also have to escape the Lucene reserved characters : + - && || ! ( ) { } [ ] ^ " ~ * ? : \
                     refValue = QueryParser.escape(refValue);
                     query = query.replaceAll("\\{\\$" + refName + "\\}", refValue);
                } else {
                    // the request parameter wasn't found, so we leave the marker as it is, simply ignoring it.
                }
            }
            refMarkerPos = query.indexOf("{$", refMarkerPos + 2);
        }

        Query q = qm.createQuery(query, StringUtils.defaultIfEmpty(request.getParameter("language"), Query.JCR_SQL2));

        int limit = getInt("limit", defaultLimit, request);
        int offset = getInt("offset", 0, request);

        if (limit > 0) {
            q.setLimit(limit);
        }
        if (offset > 0) {
            q.setOffset(offset);
        }

        return q;
    }

    protected void handle(HttpServletRequest request, HttpServletResponse response) throws RenderException,
            IOException, RepositoryException {
        String path = StringUtils.substringAfter(request.getPathInfo().substring(1), "/");
        String workspace = StringUtils.defaultIfEmpty(StringUtils.substringBefore(path, "/"), defaultWorkspace);
        Locale locale = LanguageCodeConverters.languageCodeToLocale(StringUtils.defaultIfEmpty(StringUtils
                .substringBefore(StringUtils.substringAfter(path, "/"), "/"), defaultLocale));
        try {
            Query query = getQuery(request, response, workspace, locale);
            if (query == null) {
                return;
            }
            if (logger.isInfoEnabled()) {
                logger.info("Executing " + query.getLanguage() + " for workspace '" + workspace + "' and locale '"
                        + locale + "'. Statement: " + query.getStatement());
            }
            writeResults(query.execute(), request, response);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (InvalidQueryException e) {
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
                sb.append("Rendered [").append(request.getRequestURI()).append(request.getQueryString());
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

    private JSONObject serializeNode(Node currentNode, int depthLimit, boolean escapeColon, String searchTerm) throws RepositoryException,
            JSONException {
        final PropertyIterator stringMap = currentNode.getProperties();
        JSONObject jsonObject = new JSONObject();
        // Map<String,Object> map = new HashMap<String, Object>();
        Set<String> matchingProperties = new HashSet<String>();
        while (stringMap.hasNext()) {
            JCRPropertyWrapper propertyWrapper = (JCRPropertyWrapper) stringMap.next();
            final int type = propertyWrapper.getType();
            final String name = escapeColon ? propertyWrapper.getName().replace(":", "_") : propertyWrapper.getName();
            if (type == PropertyType.WEAKREFERENCE || type == PropertyType.REFERENCE) {
                if (!propertyWrapper.isMultiple()) {
                    jsonObject.put(name, ((JCRNodeWrapper) propertyWrapper.getNode()).getWebdavUrl());
                }
            } else {
                if (!propertyWrapper.isMultiple()) {
                    jsonObject.put(name, propertyWrapper.getValue().getString());
                    if ((searchTerm != null) && (propertyWrapper.getValue().getString().startsWith(searchTerm))) {
                        // property starts with the searchTerm, let's add it to the list of matching properties.
                        matchingProperties.add(name);
                    }
                } else {
                    JSONArray jsonArray = new JSONArray();
                    Value[] propValues = propertyWrapper.getValues();
                    for (Value propValue : propValues) {
                        jsonArray.put(propValue.getString());
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
        jsonObject.put("primaryNodeType", currentNode.getPrimaryNodeType().getName());
        if (searchTerm != null) {
            jsonObject.put("matchingProperties", new JSONArray(matchingProperties));
        }

        // now let's output the children until we reach the depth limit.
        if ((depthLimit - 1) > 0) {
            final NodeIterator childNodeIterator = currentNode.getNodes();
            JSONArray childMapList = new JSONArray();
            while (childNodeIterator.hasNext()) {
                Node currentChildNode = childNodeIterator.nextNode();
                JSONObject childSerializedMap = serializeNode(currentChildNode, depthLimit - 1, escapeColon, searchTerm);
                childMapList.put(childSerializedMap);
            }
            jsonObject.put("childNodes", childMapList);
        }
        return jsonObject;
    }

    private JSONObject serializeRow(Row row, String[] columns, int depthLimit, boolean escapeColon, Set alreadyIncludedIdentifiers, String searchTerm) throws RepositoryException,
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
                    jsonObject.put("node", serializeNode(currentNode, depthLimit, escapeColon, searchTerm));
                    alreadyIncludedIdentifiers.add(currentNode.getIdentifier());
                } catch (ItemNotFoundException e) {
                    currentNode = null;
                }
            } else {
                if (alreadyIncludedIdentifiers.contains(currentNode.getIdentifier())) {
                    // avoid duplicates due to j:translation nodes.
                    return null;
                }
                jsonObject.put("node", serializeNode(currentNode, depthLimit, escapeColon, searchTerm));
                alreadyIncludedIdentifiers.add(currentNode.getIdentifier());
            }

        }

        for (String column : columns) {
            Value value = row.getValue(column);
            jsonObject.put(escapeColon ? column.replace(":", "_") : column, value != null ? value.getString() : value);
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

    /**
     * @param defaultLimit the defaultLimit to set
     */
    public void setDefaultLimit(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    /**
     * @param defaultLocale the defaultLocale to set
     */
    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * @param defaultWorkspace the defaultWorkspace to set
     */
    public void setDefaultWorkspace(String defaultWorkspace) {
        this.defaultWorkspace = defaultWorkspace;
    }

    private void writeResults(QueryResult result, HttpServletRequest request, HttpServletResponse response)
            throws RepositoryException, IllegalArgumentException, IOException, RenderException {
        response.setContentType("application/json; charset=UTF-8");
        int depth = getInt("depthLimit", defaultDepthLimit, request);
        boolean escape = Boolean.valueOf(StringUtils.defaultIfEmpty(request.getParameter("escapeColon"), String
                .valueOf(defaultEscapeColon)));

        String searchTermName = request.getParameter("searchTermName");
        String searchTerm = null;
        if (searchTermName != null) {
            searchTerm = request.getParameter(searchTermName);
        }

        JSONArray results = new JSONArray();
        
        try {
            String[] columns = result.getColumnNames();
            boolean serializeRows = columns.length > 0 && !columns[0].contains(".");

            Set alreadyIncludedIdentifiers = new HashSet();
            if (serializeRows) {
                RowIterator rows = result.getRows();
                while (rows.hasNext()) {
                    JSONObject serializedRow = serializeRow(rows.nextRow(), columns, depth, escape, alreadyIncludedIdentifiers, searchTerm);
                    if (serializedRow != null) {
                        results.put(serializedRow);
                    }
                }
            } else {
                NodeIterator nodes = result.getNodes();
                while (nodes.hasNext()) {
                    results.put(serializeNode(nodes.nextNode(), depth, escape, searchTerm));
                }
            }
            results.write(response.getWriter());
        } catch (JSONException e) {
            throw new RenderException(e);
        }
    }
}
