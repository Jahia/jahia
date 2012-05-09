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
import org.jahia.services.content.*;
import org.jahia.services.render.URLResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.queryParser.QueryParser;
import org.jahia.api.Constants;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.URLResolver;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A small servlet to allow us to perform queries on the JCR.
 *
 * @author loom
 *         Date: Jan 26, 2010
 *         Time: 5:55:17 PM
 */
public class FindHistory extends BaseFindController {

    private static Logger logger = LoggerFactory.getLogger(FindHistory.class);

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

    @Override
    protected void handle(HttpServletRequest request, HttpServletResponse response) throws RenderException,
            IOException, RepositoryException {
        URLResolver urlResolver = urlResolverFactory.createURLResolver(request.getPathInfo(), request.getServerName(), request);
        try {
            if (request.getSession() == null) {
                return;
            }

            List<String> l = (List<String>) request.getSession().getAttribute("historyTracker");

            if (l == null) {
                return;
            }

            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(urlResolver.getWorkspace(), urlResolver.getLocale());

            writeResults(l, session, request, response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (InvalidQueryException e) {
            logger.error("Invalid query", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
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

    private void writeResults(List<String> result, JCRSessionWrapper session, HttpServletRequest request, HttpServletResponse response)
            throws RepositoryException, IllegalArgumentException, IOException, RenderException {
        response.setContentType("application/json; charset=UTF-8");
        int depth = getInt("depthLimit", defaultDepthLimit, request);
        boolean escape = Boolean.valueOf(StringUtils.defaultIfEmpty(request.getParameter("escapeColon"), String
                .valueOf(defaultEscapeColon)));
        boolean removeDuplicatePropertyValues = Boolean.valueOf(StringUtils.defaultIfEmpty(request.getParameter("removeDuplicatePropValues"), String
                .valueOf(defaultRemoveDuplicatePropertyValues)));

        JSONArray results = new JSONArray();

        try {

            Set<String> alreadyIncludedIdentifiers = new HashSet<String>();
            Map<String, String> alreadyIncludedPropertyValues = null;
            if (removeDuplicatePropertyValues) {
                alreadyIncludedPropertyValues = new HashMap<String, String>();
            }

            logger.debug("Serializing nodes into JSON result structure...");
            int resultCount = 0;
            for (String uuid : result) {
                try {
                    JCRNodeWrapper w = session.getNodeByIdentifier(uuid);
                    JSONObject serializedNode = serializeNode(w, depth, escape, null, alreadyIncludedPropertyValues);
                    if (serializedNode != null) {
                        results.put(serializedNode);
                        resultCount++;
                    }
                } catch (ItemNotFoundException e) {
                }
            }
            logger.debug("Found " + resultCount + " results.");

            results.write(response.getWriter());
        } catch (JSONException e) {
            throw new RenderException(e);
        }
    }

    public static String getFindServletPath() {
        // TODO move this into configuration
        return "/cms/findHistory";
    }

}
