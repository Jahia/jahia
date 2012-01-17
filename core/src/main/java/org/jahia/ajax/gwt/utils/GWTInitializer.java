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

package org.jahia.ajax.gwt.utils;

import org.apache.commons.lang.WordUtils;
import org.drools.util.StringUtils;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.bin.Render;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.*;

/**
 * @author rfelden
 * @version 10 mars 2008 - 10:46:13
 */
public class GWTInitializer {
    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(GWTInitializer.class);
    private static List<String> gwtCssStyles;

    public static String generateInitializerStructure(HttpServletRequest request, HttpSession session) {
        return generateInitializerStructure(request, session, null, null);
    }

    public static String generateInitializerStructure(HttpServletRequest request, HttpSession session, Locale locale, Locale uilocale) {
        StringBuilder buf = new StringBuilder();

        if (uilocale == null) {
            Locale sessionLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_UI_LOCALE);
            uilocale = sessionLocale != null ?
                    UserPreferencesHelper.getPreferredLocale((JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER), sessionLocale) :
                    UserPreferencesHelper.getPreferredLocale((JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER), LanguageCodeConverters.resolveLocaleForGuest(request));
        }
        if (locale == null) {
            String language = request.getParameter("lang");
            if (!StringUtils.isEmpty(language)) {
                locale = LanguageCodeConverters.getLocaleFromCode(language);
            }
            if (locale == null) {
                locale = (Locale) session.getAttribute(ParamBean.SESSION_LOCALE);
            }
            if (locale == null) {
                locale = Locale.ENGLISH;
            }
        }

        buf.append("<meta name=\"gwt:property\" content=\"locale=").append(uilocale.toString()).append("\"/>");
        addCss(buf, request);

        // creat parameters map
        Map<String, String> params = new HashMap<String, String>();

        RenderContext renderContext = (RenderContext) request.getAttribute("renderContext");

        String serviceEntrypoint = buildServiceBaseEntrypointUrl(request);
        params.put(JahiaGWTParameters.SERVICE_ENTRY_POINT, serviceEntrypoint);
        params.put(JahiaGWTParameters.CONTEXT_PATH, request.getContextPath().equals("/")?"":request.getContextPath());
        params.put(JahiaGWTParameters.SERVLET_PATH, request.getServletPath());
        params.put(JahiaGWTParameters.PATH_INFO, request.getPathInfo());
        params.put(JahiaGWTParameters.QUERY_STRING, request.getQueryString());

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

        params.put(JahiaGWTParameters.LANGUAGE, locale.toString());
        params.put(JahiaGWTParameters.LANGUAGE_DISPLAY_NAME, WordUtils.capitalizeFully(locale.getDisplayName(locale)));
        params.put(JahiaGWTParameters.UI_LANGUAGE, uilocale.toString());
        params.put(JahiaGWTParameters.UI_LANGUAGE_DISPLAY_NAME, WordUtils.capitalizeFully(uilocale.getDisplayName(uilocale)));
        try {
            if (renderContext != null) {
                params.put(JahiaGWTParameters.WORKSPACE, renderContext
                        .getMainResource().getWorkspace());

                if (renderContext.getSite() != null) {
                    params.put(JahiaGWTParameters.SITE_UUID, renderContext.getSite().getIdentifier());
                    params.put(JahiaGWTParameters.SITE_KEY, renderContext.getSite().getSiteKey());
                }
            } else {
                if (request.getParameter("site") != null) {
                    params.put(JahiaGWTParameters.SITE_UUID, request.getParameter("site"));
                } else {
                    final JahiaSite attribute = (JahiaSite) request.getSession().getAttribute(ProcessingContext.SESSION_SITE);
                    if (attribute != null && !"".equals(attribute.getSiteKey())) {
                        try {
                            params.put(JahiaGWTParameters.SITE_UUID, ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(attribute.getSiteKey()).getUuid());
                            params.put(JahiaGWTParameters.SITE_KEY, attribute.getSiteKey());
                        } catch (JahiaException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                if (request.getParameter("workspace") != null) {
                    params.put(JahiaGWTParameters.WORKSPACE, request.getParameter("workspace"));
                } else {
                    params.put(JahiaGWTParameters.WORKSPACE, "default");
                }
            }
        } catch (RepositoryException e) {
            logger.error("Error when getting site id", e);
        }

        // put live workspace url
        if (request.getAttribute("url") != null) {
            URLGenerator url = (URLGenerator) request.getAttribute("url");
            params.put(JahiaGWTParameters.BASE_URL, url.getContext() + url.getBase());
            params.put(JahiaGWTParameters.STUDIO_URL, url.getContext() + url.getStudio());
            addLanguageSwitcherLinks(renderContext, params, url);
        } else {
            params.put(JahiaGWTParameters.BASE_URL, request.getContextPath().equals("/") ? "" : request.getContextPath() + Render.getRenderServletPath() + "/" + params.get("workspace")  + "/" + locale.toString());
        }

        // add jahia parameter dictionary
        buf.append("<script type=\"text/javascript\">\n");
        buf.append(getJahiaGWTConfig(params));
        buf.append("\n</script>\n");

        return buf.toString();
    }

    private static void addCss(StringBuilder buf, HttpServletRequest request) {
        String context = request.getContextPath();
        for (String css : gwtCssStyles()) {
            buf.append("<link type=\"text/css\" href=\"").append(context).append(css).append("\" rel=\"stylesheet\"/>\n");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> gwtCssStyles() {
        if (gwtCssStyles == null) {
            gwtCssStyles = (List<String>) SpringContextSingleton.getBean("gwtCssStyles");
        }
        return gwtCssStyles;
    }

    /**
     * Add language switcher link into page
     *
     * @param renderContext
     * @param params
     * @param urlGenerator
     */
    public static void addLanguageSwitcherLinks(RenderContext renderContext, Map<String, String> params, URLGenerator urlGenerator) {
        try {
            final JCRSiteNode currentSite = renderContext.getSite();
            if (currentSite != null) {
                final Set<String> languageSettings = currentSite.getLanguages();
                if (languageSettings != null && languageSettings.size() > 0) {
                    for (String lang : languageSettings) {
                        params.put(lang, urlGenerator.getLanguages().get(lang));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error while creating change site link", e);
        }
    }


    /**
     * Get jahiaGWTConfig as JSON string
     *
     * @param params
     * @return
     */
    public static String getJahiaGWTConfig(Map<String, String> params) {
        StringBuilder s = new StringBuilder();
        s.append("var " + JahiaGWTParameters.JAHIA_GWT_PARAMETERS + "={");
        if (params != null) {
            boolean b = false;
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (param.getValue() != null) {
                    if (b) {
                        s.append(",");
                    } else {
                        b = true;
                    }
                    //s.append("\n");
                    s.append(param.getKey()).append(":\"").append(String.valueOf(param.getValue())).append("\"");
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
