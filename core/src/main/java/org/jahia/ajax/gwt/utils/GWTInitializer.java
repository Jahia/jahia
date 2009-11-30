/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.utils;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 10 mars 2008 - 10:46:13
 */
public class GWTInitializer {
    private final static Logger logger = Logger.getLogger(GWTInitializer.class);

    public static String getInitString(PageContext pageContext) {
        return getInitString(pageContext, false);
    }

    public static String getInitString(PageContext pageContext, boolean standalone) {
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        final HttpSession session = request.getSession();
        ProcessingContext processingContext;
        if (jData != null) {
            processingContext = jData.getProcessingContext();
        } else {
            processingContext = null;
        }
        return generateInitializerStructure(request, session, processingContext);
    }

    public static String getInitString(ParamBean paramBean) {
        try {
            return generateInitializerStructure(paramBean.getRealRequest(), paramBean.getSession(), paramBean);
        } catch (JahiaSessionExpirationException e) {
            try {
                return generateInitializerStructure(paramBean.getRealRequest(), paramBean.getSession(true), paramBean);
            } catch (JahiaSessionExpirationException e1) {
                logger.error("Could not create a new session");
            }
        }
        return "";
    }

    private static String generateInitializerStructure(HttpServletRequest request, HttpSession session, ProcessingContext processingContext) {
        StringBuilder buf = new StringBuilder();
        Locale locale = (Locale) session.getAttribute(ParamBean.SESSION_LOCALE);
        if (locale == null) {
            locale = Locale.ENGLISH;
        }

        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        String context = request.getContextPath();
        buf.append("<meta name=\"gwt:property\" content=\"locale=").append(locale.toString()).append("\"/>");
        buf.append("<link type=\"text/css\" href=\"").append(context).append("/gwt/resources/ckeditor/contents.css\" rel=\"stylesheet\"/>\n");
        buf.append("<link type=\"text/css\" href=\"").append(context).append("/gwt/resources/ckeditor/contents.css\" rel=\"stylesheet\"/>\n");                
        buf.append("<link type=\"text/css\" href=\"").append(context).append("/gwt/resources/css/jahia-ext-all.css\" rel=\"stylesheet\"/>\n");
        buf.append("<link type=\"text/css\" href=\"").append(context).append("/gwt/resources/css/xtheme-jahia.css\" rel=\"stylesheet\"/>\n");
        buf.append("<link type=\"text/css\" href=\"").append(context).append("/gwt/resources/css/jahia-gwt-engines.css\" rel=\"stylesheet\"/>\n");
        buf.append("<link type=\"text/css\" href=\"").append(context).append("/gwt/resources/css/jahia-gwt-templates.css\" rel=\"stylesheet\"/>\n");

        // creat parameters map
        Map<String, String> params = new HashMap<String, String>();

        String serviceEntrypoint = buildServiceBaseEntrypointUrl(request);
        params.put(JahiaGWTParameters.SERVICE_ENTRY_POINT, serviceEntrypoint);
        if (processingContext != null) {
            params.put(JahiaGWTParameters.PID, String.valueOf(processingContext.getPageID()));
            params.put(JahiaGWTParameters.OPERATION_MODE, processingContext.getOperationMode());
            params.put(JahiaGWTParameters.PATH_INFO, processingContext.getPathInfo());
            params.put(JahiaGWTParameters.QUERY_STRING, processingContext.getQueryString());
            params.put(JahiaGWTParameters.CONTEXT_PATH, processingContext.getContextPath());
            params.put(JahiaGWTParameters.SERVLET_PATH, processingContext.getServletPath());

            try {
                JahiaPage page = processingContext.getPage();
                if (page != null) {
                    JahiaBaseACL jahiaBaseACL = page.getACL();
                    if (jahiaBaseACL != null && jahiaBaseACL.getPermission(processingContext.getUser(), JahiaBaseACL.WRITE_RIGHTS)) {
                        params.put(JahiaGWTParameters.PAGE_WRITE, "true");
                    }
                }
            } catch (Exception e) {
                logger.error(e, e);
            }
        } else {
            params.put(JahiaGWTParameters.CONTEXT_PATH, Jahia.getContextPath());
            params.put(JahiaGWTParameters.SERVLET_PATH, Jahia.getServletPath());
            params.put(JahiaGWTParameters.PATH_INFO, request.getPathInfo());
            params.put(JahiaGWTParameters.QUERY_STRING, request.getQueryString());            
            params.put(JahiaGWTParameters.PID, String.valueOf(session.getAttribute(ParamBean.SESSION_LAST_REQUESTED_PAGE_ID)));
            params.put(JahiaGWTParameters.OPERATION_MODE, String.valueOf(session.getAttribute(ParamBean.SESSION_JAHIA_RUNNING_MODE)));
        }

        if (processingContext != null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(processingContext.getScheme());
            buffer.append("://");
            buffer.append(processingContext.getServerName());
            buffer.append(":");
            buffer.append(processingContext.getServerPort());
            buffer.append(Jahia.getContextPath());
            buffer.append(Jahia.getServletPath());
            params.put(JahiaGWTParameters.JAHIA_SERVER_PATH, buffer.toString());
        }

        JahiaUser user = (JahiaUser) session.getAttribute(ParamBean.SESSION_USER);
        if (user != null) {
            String name = user.getUsername();
            int index = name.indexOf(":");
            if (index > 0) {
                String displayname = name.substring(0, index);
                params.put(JahiaGWTParameters.CURRENT_USER_NAME, displayname);
            } else {
                params.put(JahiaGWTParameters.CURRENT_USER_NAME, name);
            }
        } else {
            params.put(JahiaGWTParameters.CURRENT_USER_NAME, "guest");
        }
        params.put(JahiaGWTParameters.USER_ALLOWED_TO_UNLOCK_FILES, Boolean
                .toString(user != null && user.isRoot()));

        params.put(JahiaGWTParameters.LANGUAGE, locale.toString());

        Locale enginelocale = (Locale) session.getAttribute(ParamBean.SESSION_LOCALE_ENGINE);
        if (enginelocale != null) {
            params.put(JahiaGWTParameters.ENGINE_LANGUAGE, enginelocale.toString());
        }

            // put live workspace url
            if (request.getAttribute("renderContext") != null) {
                URLGenerator url = (URLGenerator) request.getAttribute("url");
                params.put(JahiaGWTParameters.LIVE_URL, url.getLive());
                params.put(JahiaGWTParameters.EDIT_URL, url.getEdit());
                params.put(JahiaGWTParameters.PREVIEW_URL, url.getPreview());
                params.put(JahiaGWTParameters.COMPARE_URL, null);
            } else {
                if (jData != null && jData.gui() != null) {
                }
            }

        // add jahia parameter dictionary
        buf.append("<script type='text/javascript'>\n");
        buf.append(getJahiaGWTConfig(params));
        buf.append("\n</script>\n");

        // add custom ck config
        buf.append("<script type='text/javascript' src='/gwt/resources/ckeditor/ckeditor_custom_config.js'></script>\n");  


        return buf.toString();
    }


    /**
     * Get jahiaGWTConfig as JSON string
     *
     * @param params
     * @return
     */
    private static String getJahiaGWTConfig(Map params) {
        StringBuilder s = new StringBuilder();
        s.append("var " + JahiaGWTParameters.JAHIA_GWT_PARAMETERS + " = {\n");
        if (params != null) {
            Iterator keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String name = keys.next().toString();
                Object value = params.get(name);
                if (value != null) {
                    s.append(name).append(":\"").append(value.toString()).append("\"");
                    if (keys.hasNext()) {
                        s.append(",");
                    }
                    s.append("\n");
                }
            }
        }

        s.append("};");

        return s.toString();
    }

    /**
     * Build service base entry point url
     *
     * @param request
     * @return
     */
    private static String buildServiceBaseEntrypointUrl(HttpServletRequest request) {
        return new StringBuilder(request.getContextPath()).append("/gwt/").toString();
    }

}
