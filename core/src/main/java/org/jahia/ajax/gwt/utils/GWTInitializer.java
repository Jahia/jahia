/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.utils;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.util.*;

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
            processingContext = Jahia.getThreadParamBean();
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
        // creat parameters map
        Map<String, String> params = new HashMap<String, String>();

        String serviceEntrypoint = buildServiceBaseEntrypointUrl(request);
        params.put(JahiaGWTParameters.SERVICE_ENTRY_POINT, serviceEntrypoint);
        if (processingContext != null) {
            params.put(JahiaGWTParameters.PID, String.valueOf(processingContext.getPageID()));
            params.put(JahiaGWTParameters.OPERATION_MODE, processingContext.getOperationMode());
            params.put(JahiaGWTParameters.PATH_INFO, processingContext.getPathInfo());
            params.put(JahiaGWTParameters.QUERY_STRING, processingContext.getQueryString());
        } else {
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

        AdvPreviewSettings advPreviewSettings = (AdvPreviewSettings) session.getAttribute(ParamBean.SESSION_ADV_PREVIEW_SETTINGS);
        if (advPreviewSettings != null) {
            params.put(JahiaGWTParameters.ENABLE_ADV_PREVIEW_SETTINGS, String.valueOf(advPreviewSettings.isEnabled()));
            if (advPreviewSettings.getPreviewDate() != 0) {
                params.put(JahiaGWTParameters.PREVIEW_DATE, String.valueOf(advPreviewSettings.getPreviewDate()));
            }
            user = advPreviewSettings.getAliasedUser();
            if (user != null) {
                String name = user.getUsername();
                int index = name.indexOf(":");
                if (index > 0) {
                    String displayname = name.substring(0, index);
                    params.put(JahiaGWTParameters.ALIASED_USER_NAME, displayname);
                } else {
                    params.put(JahiaGWTParameters.ALIASED_USER_NAME, name);
                }
            }
        }

        params.put(JahiaGWTParameters.LANGUAGE, locale.getLanguage());

        Locale enginelocale = (Locale) session.getAttribute(ParamBean.SESSION_LOCALE_ENGINE);
        if (enginelocale != null) {
            params.put(JahiaGWTParameters.ENGINE_LANGUAGE, enginelocale.toString());
        }

        // add jahia parameter dictionary
        buf.append("<script type='text/javascript'>\n");
        buf.append(getJahiaGWTConfig(params));
        buf.append("\n</script>\n");
        String context = request.getContextPath();
        buf.append("<link type=\"text/css\" href=\"").append(context).append("/jsp/jahia/gwt/resources/css/jahia-ext-all.css\" rel=\"stylesheet\"/>\n");
        buf.append("<link type=\"text/css\" href=\"").append(context).append("/jsp/jahia/gwt/resources/css/xtheme-jahia.css\" rel=\"stylesheet\"/>\n");
        buf.append("<link type=\"text/css\" href=\"").append(context).append("/jsp/jahia/gwt/resources/css/jahia-gwt-engines.css\" rel=\"stylesheet\"/>\n");
        buf.append("<link type=\"text/css\" href=\"").append(context).append("/jsp/jahia/gwt/resources/css/jahia-gwt-templates.css\" rel=\"stylesheet\"/>\n");

        return buf.toString();
    }


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

    private static String buildServiceBaseEntrypointUrl(HttpServletRequest request) {
        return new StringBuilder(request.getContextPath()).append("/gwt/").toString();
    }

}
