/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.bin;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.nodetypes.ExtendedItemDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializer;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializerService;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static org.jahia.api.Constants.LIVE_WORKSPACE;

/**
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 8 mars 2010
 */
public class Initializers extends JahiaController {
    
    private transient static Logger logger = LoggerFactory.getLogger(Initializers.class);
    
    private String defaultLocale = "en";

    private String defaultWorkspace = LIVE_WORKSPACE;

    /**
     * Process the request and return a ModelAndView object which the DispatcherServlet
     * will render. A <code>null</code> return value is not an error: It indicates that
     * this object completed request processing itself, thus there is no ModelAndView
     * to render.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render, or <code>null</code> if handled directly
     * @throws Exception in case of errors
     */
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
                sb.append("] ip=[").append(request.getRemoteAddr()).append("] sessionID=[")
                        .append(request.getSession(true).getId()).append("] in [")
                        .append(System.currentTimeMillis() - startTime).append("ms]");
                logger.info(sb.toString());
            }
        }
        return null;
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) {
        ExtendedNodeType type = null;
        String path = StringUtils.substringAfter(request.getPathInfo().substring(1), "/");
        String workspace = StringUtils.defaultIfEmpty(StringUtils.substringBefore(path, "/"), defaultWorkspace);
        Locale locale = LanguageCodeConverters.languageCodeToLocale(
                StringUtils.defaultIfEmpty(StringUtils.substringBefore(StringUtils.substringAfter(path, "/"), "/"),
                        defaultLocale));
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale);
            String nodePath = request.getParameter("path");
            JCRNodeWrapper node;
            if (nodePath != null) {
                node = session.getNode(nodePath);
                type = node.getPrimaryNodeType();
            } else if (request.getParameter("nodeuuid") != null) {
                node = session.getNodeByUUID(request.getParameter("nodeuuid"));
                type = node.getPrimaryNodeType();
            } else {
                node = null;
                type = NodeTypeRegistry.getInstance().getNodeType(request.getParameter("type"));
            }

            String initializersString = request.getParameter("initializers");
            String name = request.getParameter("name");
            if (type != null) {
                final List<ExtendedItemDefinition> extendedItemDefinitionList = type.getItems();
                for (ExtendedItemDefinition definition : extendedItemDefinitionList) {
                    if (definition.getName().equals(name)) {
                        Map<String, String> map = new LinkedHashMap<String, String>();
                        if (initializersString != null) {
                            String[] strings = initializersString.split(",");
                            for (String string : strings) {
                                map.put(string, "");
                            }
                        } else {
                            map = definition.getSelectorOptions();
                        }

                        if (map.size() > 0) {
                            final Map<String, ChoiceListInitializer> initializers =
                                    ChoiceListInitializerService.getInstance().getInitializers();
                            List<ChoiceListValue> listValues = null;
                            final HashMap<String, Object> context = new HashMap<String, Object>();
                            context.put("contextNode", node);
                            for (Map.Entry<String, String> entry : map.entrySet()) {
                                if (initializers.containsKey(entry.getKey())) {
                                    listValues = initializers.get(entry.getKey())
                                            .getChoiceListValues((ExtendedPropertyDefinition) definition, entry.getValue(), listValues, locale, context);
                                }
                            }
                            if (listValues != null) {
                                JSONArray results = new JSONArray();
                                String s = request.getParameter("q");
                                for (ChoiceListValue listValue : listValues) {
                                    String displayName = listValue.getDisplayName();
                                    if (s == null || displayName.toLowerCase().startsWith(s)) {
                                        JSONObject value = new JSONObject();
                                        if (request.getParameter("gxt") != null) {
                                            value.put("value", listValue.getValue().getString());
                                            value.put("name", displayName);
                                        } else {
                                            value.append("value", listValue.getValue().getString());
                                            value.append("name", displayName);
                                        }
                                        results.put(value);
                                    }
                                }
                                if (request.getParameter("gxt") != null) {
                                    JSONObject object = new JSONObject();
                                    object.put("choicelist", results);
                                    object.write(response.getWriter());
                                } else {
                                    results.write(new PrintWriter(System.out));
                                    results.write(response.getWriter());
                                }
                            }
                        }
                    }
                }

            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (JSONException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static String getInitializersServletPath() {
        // TODO move this into configuration
        return "/cms/initializers";
    }
}
