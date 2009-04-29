/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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

        params.put(JahiaGWTParameters.LANGUAGE, locale.toString());

        Locale enginelocale = (Locale) session.getAttribute(ParamBean.SESSION_LOCALE_ENGINE);
        if (enginelocale != null) {
            params.put(JahiaGWTParameters.ENGINE_LANGUAGE, enginelocale.toString());
        }

        // add jahia parameter dictionary
        buf.append("<script type='text/javascript'>\n");
        buf.append(getJahiaGWTConfig(params));
        buf.append("\n</script>\n");
        String context = request.getContextPath();
        buf.append("<link type=\"text/css\" href=\"").append(context).append("/gwt/resources/css/jahia-ext-all.css\" rel=\"stylesheet\"/>\n");
        buf.append("<link type=\"text/css\" href=\"").append(context).append("/gwt/resources/css/xtheme-jahia.css\" rel=\"stylesheet\"/>\n");
        buf.append("<link type=\"text/css\" href=\"").append(context).append("/gwt/resources/css/jahia-gwt-engines.css\" rel=\"stylesheet\"/>\n");
        buf.append("<link type=\"text/css\" href=\"").append(context).append("/gwt/resources/css/jahia-gwt-templates.css\" rel=\"stylesheet\"/>\n");

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
