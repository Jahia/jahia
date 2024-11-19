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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import javax.jcr.*;
import javax.jcr.query.InvalidQueryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.*;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.render.filter.HistoryTrackerBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            String name = request.getParameter("trackerName");
            if (name == null) {
                name = "historyTracker";
            }
            List<String> l = (List<String>) request.getSession().getAttribute(HistoryTrackerBean.class.getName()+"."+name);

            if (l == null) {
                return;
            }

            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(urlResolver.getWorkspace(), urlResolver.getLocale()).disableSessionCache();

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
