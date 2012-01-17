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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializer;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializerService;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static org.jahia.api.Constants.LIVE_WORKSPACE;

/**
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 8 mars 2010
 */
public class Initializers extends JahiaController {
    
    private static final String CONTROLLER_MAPPING = "/initializers";
    
    private transient static Logger logger = LoggerFactory.getLogger(Initializers.class);

    public static String getInitializersServletPath() {
        // TODO move this into configuration
        return "/cms" + CONTROLLER_MAPPING;
    }

    private String defaultLocale = "en";

    private String defaultWorkspace = LIVE_WORKSPACE;

    private void handle(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, IllegalStateException, JSONException, IOException {
        String name = getParameter(request, "name");

        String[] params = parseParameters(request);
        String workspace = params[0];
        Locale locale = LanguageCodeConverters.languageCodeToLocale(params[1]);

        if (StringUtils.isEmpty(request.getParameter("path"))
                && StringUtils.isEmpty(request.getParameter("nodeuuid"))
                && StringUtils.isEmpty(request.getParameter("type"))) {
            throw new JahiaBadRequestException("One of therequired parameters is missing");
        }

        ExtendedNodeType type = null;
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(
                workspace, locale);
        JCRNodeWrapper node = null;
        try {
            if (request.getParameter("path") != null) {
                node = session.getNode(request.getParameter("path"));
                type = node.getPrimaryNodeType();
            } else if (request.getParameter("nodeuuid") != null) {
                node = session.getNodeByUUID(request.getParameter("nodeuuid"));
                type = node.getPrimaryNodeType();
            } else {
                node = null;
                type = NodeTypeRegistry.getInstance().getNodeType(request.getParameter("type"));
            }
        } catch (PathNotFoundException e) {
            throw new JahiaBadRequestException(e);
        } catch (ItemNotFoundException e) {
            throw new JahiaBadRequestException(e);
        } catch (NoSuchNodeTypeException e) {
            throw new JahiaBadRequestException(e);
        }

        if (type == null) {
            throw new JahiaBadRequestException("Cannot determine node type");
        }

        ExtendedPropertyDefinition definition = type.getPropertyDefinition(name);
        if (definition == null) {
            throw new JahiaBadRequestException("Unable to find property defintion with the name '"
                    + name + "'");
        }

        JSONArray results = new JSONArray();

        Map<String, String> map = new LinkedHashMap<String, String>();
        String initializersString = request.getParameter("initializers");
        if (initializersString != null) {
            String[] strings = initializersString.split(",");
            for (String string : strings) {
                map.put(string, "");
            }
        } else {
            map = definition.getSelectorOptions();
        }

        if (map.size() > 0) {
            final Map<String, ChoiceListInitializer> initializers = ChoiceListInitializerService
                    .getInstance().getInitializers();
            List<ChoiceListValue> listValues = null;
            final HashMap<String, Object> context = new HashMap<String, Object>();
            context.put("contextNode", node);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (initializers.containsKey(entry.getKey())) {
                    listValues = initializers.get(entry.getKey()).getChoiceListValues(definition,
                            entry.getValue(), listValues, locale, context);
                }
            }
            if (listValues != null) {
                String s = request.getParameter("q");
                for (ChoiceListValue listValue : listValues) {
                    String displayName = listValue.getDisplayName();
                    if (s == null || displayName.toLowerCase().startsWith(s.toLowerCase())) {
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
            }
        }

        response.setContentType("application/json; charset="
                + SettingsBean.getInstance().getCharacterEncoding());
        if (request.getParameter("gxt") != null) {
            JSONObject object = new JSONObject();
            object.put("choicelist", results);
            if (logger.isDebugEnabled()) {
                StringWriter out = new StringWriter();
                object.write(out).flush();
                logger.debug(out.toString());
            }
            object.write(response.getWriter()).flush();
        } else {
            if (logger.isDebugEnabled()) {
                StringWriter out = new StringWriter();
                results.write(out).flush();
                logger.debug(out.toString());
            }
            results.write(response.getWriter()).flush();
        }
    }

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
                sb.append("] ip=[").append(request.getRemoteAddr()).append("] sessionID=[")
                        .append(sessionId).append("] in [")
                        .append(System.currentTimeMillis() - startTime).append("ms]");
                logger.info(sb.toString());
            }
        }
        return null;
    }

    private String[] parseParameters(HttpServletRequest request) {
        String workspace = null;
        String lang = null;
        String path = request.getPathInfo();
        if (path != null && path.startsWith(CONTROLLER_MAPPING + "/")) {
            path = path.substring(CONTROLLER_MAPPING.length() + 1);
            if (path.contains("/")) {
                workspace = StringUtils.substringBefore(path, "/");
                lang = StringUtils.substringAfter(path, "/");
                if (lang.contains("/")) {
                    lang = StringUtils.substringBefore(lang, "/");
                }
            }
        }
        if (!JCRContentUtils.isValidWorkspace(workspace, true)) {
            // unknown workspace
            throw new JahiaBadRequestException("Unknown workspace '" + workspace + "'");
        }

        return new String[] { StringUtils.defaultIfEmpty(workspace, defaultWorkspace),
                StringUtils.defaultIfEmpty(lang, defaultLocale) };
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public void setDefaultWorkspace(String defaultWorkspace) {
        this.defaultWorkspace = defaultWorkspace;
    }
}
