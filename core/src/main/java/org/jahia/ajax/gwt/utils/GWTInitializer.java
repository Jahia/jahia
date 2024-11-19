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
package org.jahia.ajax.gwt.utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.api.Constants;
import org.jahia.bin.Edit;
import org.jahia.bin.Jahia;
import org.jahia.bin.Render;
import org.jahia.bin.filters.ContentManagerAccessCheckFilter;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author rfelden
 * @version 10 mars 2008 - 10:46:13
 */
public class GWTInitializer {

    private static volatile GWTResourceConfig config;
    private static final Logger logger = LoggerFactory.getLogger(GWTInitializer.class);

    public static String generateInitializerStructureForFrame(RenderContext ctx) {
        StringBuilder buf = new StringBuilder();

        addCss(buf, ctx.getRequest(), ctx.getResponse(), true);

        buf.append("<script type=\"text/javascript\">\n" + "var onGWTFrameLoaded = []; "
                + "function onGWTFrameLoad(fun) { onGWTFrameLoaded[onGWTFrameLoaded.length] = fun; }; ");
        String customCkeditorConfig = getCustomCKEditorConfig(ctx);
        buf.append("if (typeof parent.contextJsParameters != 'undefined') { parent.contextJsParameters.ckeCfg='");
        if (customCkeditorConfig != null) {
            buf.append(customCkeditorConfig);
        }
        buf.append("' };\n</script>\n");

        return buf.toString();
    }

    /**
     * @deprecated since 7.2.3.3 / 7.3.0.1; use {@link #generateInitializerStructure(HttpServletRequest, HttpServletResponse, HttpSession, Locale, Locale)} instead
     */
    @SuppressWarnings("javadoc")
    @Deprecated
    public static String generateInitializerStructure(HttpServletRequest request, HttpSession session) {
        return generateInitializerStructure(request, session, null, null);
    }

    /**
     * @deprecated since 7.2.3.3 / 7.3.0.1; use {@link #generateInitializerStructure(HttpServletRequest, HttpServletResponse, HttpSession, Locale, Locale)} instead
     */
    @SuppressWarnings("javadoc")
    @Deprecated
    public static String generateInitializerStructure(HttpServletRequest request, HttpSession session, Locale locale, Locale uilocale) {
        return generateInitializerStructure(request, null, session, locale, uilocale);
    }

    public static String generateInitializerStructure(HttpServletRequest request, HttpServletResponse response, HttpSession session, Locale locale, Locale uilocale) {
        StringBuilder buf = new StringBuilder();
        JahiaUser user = (JahiaUser) session.getAttribute(Constants.SESSION_USER);
        if (uilocale == null) {
            Locale sessionLocale = (Locale) session.getAttribute(Constants.SESSION_UI_LOCALE);
            JCRUserNode userNode = null;
            if (user != null) {
                userNode = JahiaUserManagerService.getInstance().lookupUserByPath(user.getLocalPath());
            }
            uilocale = sessionLocale != null ?
                    UserPreferencesHelper.getPreferredLocale(userNode, sessionLocale) :
                    UserPreferencesHelper.getPreferredLocale(userNode, LanguageCodeConverters.resolveLocaleForGuest(request));
        }
        if (locale == null) {
            String language = request.getParameter("lang");
            if (!StringUtils.isEmpty(language)) {
                locale = LanguageCodeConverters.getLocaleFromCode(language);
            }
            if (locale == null) {
                locale = (Locale) session.getAttribute(Constants.SESSION_LOCALE);
            }
            if (locale == null) {
                locale = Locale.ENGLISH;
            }
        }

        buf.append("<meta name=\"gwt:property\" content=\"locale=").append(StringEscapeUtils.escapeXml(uilocale.toString())).append("\"/>");

        RenderContext renderContext = (RenderContext) request.getAttribute("renderContext");

        addCss(buf, request, response != null ? response : (renderContext != null ? renderContext.getResponse() : null), false);

        // creat parameters map
        Map<String, String> params = new HashMap<String, String>();

        String serviceEntrypoint = buildServiceBaseEntrypointUrl(request);
        params.put(JahiaGWTParameters.SERVICE_ENTRY_POINT, serviceEntrypoint);
        String contextPath = request.getContextPath();
        params.put(JahiaGWTParameters.CONTEXT_PATH, contextPath);
        params.put(JahiaGWTParameters.DX_VERSION, Jahia.VERSION);
        params.put(JahiaGWTParameters.SERVLET_PATH, (request.getAttribute("servletPath") == null) ? request.getServletPath() : (String) request.getAttribute("servletPath"));
        params.put(JahiaGWTParameters.PATH_INFO, request.getPathInfo());
        params.put(JahiaGWTParameters.QUERY_STRING, request.getQueryString());
        boolean devMode = SettingsBean.getInstance().isDevelopmentMode();
        params.put(JahiaGWTParameters.DEVELOPMENT_MODE, devMode ? "true" : "false");
        boolean areaAutoActivated = SettingsBean.getInstance().isAreaAutoActivated();
        params.put(JahiaGWTParameters.AREA_AUTO_ACTIVATED, areaAutoActivated ? "true" : "false");

        if (devMode) {
            params.put(JahiaGWTParameters.MODULES_SOURCES_DISK_PATH, SettingsBean.getInstance().getModulesSourcesDiskPath());
        }
        if (user != null) {
            String name = user.getUsername();
            int index = name.indexOf(":");
            if (index > 0) {
                String displayname = name.substring(0, index);
                params.put(JahiaGWTParameters.CURRENT_USER_NAME, displayname);
            } else {
                params.put(JahiaGWTParameters.CURRENT_USER_NAME, name);
            }
            params.put(JahiaGWTParameters.CURRENT_USER_PATH, user.getLocalPath());
        } else {
            params.put(JahiaGWTParameters.CURRENT_USER_NAME, "guest");
            params.put(JahiaGWTParameters.CURRENT_USER_PATH, "/users/guest");
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
                    params.put(JahiaGWTParameters.SITE_UUID, StringEscapeUtils.escapeXml(request.getParameter("site")));
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
            params.put(JahiaGWTParameters.EDIT_URL, url.getContext() + url.getBaseEdit());
            params.put(JahiaGWTParameters.STUDIO_URL, url.getContext() + url.getStudio());
            params.put(JahiaGWTParameters.STUDIO_VISUAL_URL, url.getContext() + url.getStudioVisual());
            addLanguageSwitcherLinks(renderContext, params, url);
        } else {
            params.put(JahiaGWTParameters.BASE_URL, contextPath + Render.getRenderServletPath() + "/" + params.get("workspace") + "/" + locale.toString());
            params.put(JahiaGWTParameters.EDIT_URL, contextPath + "/cms/edit/" + params.get("workspace") + "/" + locale.toString());
        }

        if (SettingsBean.getInstance().isUseWebsockets()) {
            params.put(JahiaGWTParameters.USE_WEBSOCKETS, "true");
        }

        String customCkeditorConfig = getCustomCKEditorConfig(request, renderContext);
        if (customCkeditorConfig != null) {
            params.put("ckeCfg", customCkeditorConfig);
        }

        params.put(JahiaGWTParameters.STUDIO_MAX_DISPLAYABLE_FILE_SIZE, String.valueOf(SettingsBean.getInstance().getStudioMaxDisplayableFileSize()));

        params.put(JahiaGWTParameters.SERVER_DISPLAYABLE_TIME_ZONE, getServerDisplayableTimezone(Calendar.getInstance().getTimeZone(), uilocale));

        // add jahia parameter dictionary
        buf.append("<script type=\"text/javascript\">\n");
        buf.append(getJahiaGWTConfig(params));
        buf.append("\n</script>\n");

        addJavaScript(buf, request, response, renderContext);

        return buf.toString();
    }

    private static String getServerDisplayableTimezone(TimeZone timeZone, Locale locale) {
        long hours = TimeUnit.MILLISECONDS.toHours(timeZone.getRawOffset());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeZone.getRawOffset())
                - TimeUnit.HOURS.toMinutes(hours);
        // avoid -4:-30 issue
        minutes = Math.abs(minutes);

        if (hours > 0) {
            return String.format("%s (GMT+%d:%02d)", timeZone.getDisplayName(locale), hours, minutes);
        } else {
            return String.format("%s (GMT%d:%02d)", timeZone.getDisplayName(locale), hours, minutes);
        }
    }

    public static String getCustomCKEditorConfig(RenderContext ctx) {
        return ctx == null ? null : getCustomCKEditorConfig(ctx.getRequest(), ctx);
    }

    public static String getCustomCKEditorConfig(HttpServletRequest request, RenderContext ctx) {
        String cfgPath = null;

        if (getConfig().isDetectCustomCKEditorConfig()) {
            JahiaTemplatesPackage pkg = getCurrentSiteTemplatePackage(request, ctx);
            if (pkg != null) {
                Bundle bundle = pkg.getBundle();
                if (bundle != null && bundle.getEntry("/javascript/ckeditor_config.js") != null) {
                    cfgPath = (ctx != null ? ctx.getRequest() : request).getContextPath() + pkg.getRootFolderPath()
                            + "/javascript/ckeditor_config.js";
                }

            }
        }
        if (null == cfgPath) {
            JahiaTemplatesPackage ckeditorModule = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                    .getTemplatePackageById("ckeditor");
            if (ckeditorModule != null) {
                Bundle ckeditorBundle = ckeditorModule.getBundle();
                if (ckeditorBundle != null && ckeditorBundle.getResource("javascript/config.js") != null) {
                    cfgPath = request.getContextPath() + ckeditorModule.getRootFolderPath()
                            + "/javascript/config.js";
                }
            }
        }

        return cfgPath;
    }

    private static JahiaTemplatesPackage getCurrentSiteTemplatePackage(HttpServletRequest request, RenderContext ctx) {
        JahiaTemplatesPackage pkg = null;
        if (ctx != null) {
            JCRSiteNode site = ctx.getSite();
            if (site != null) {
                pkg = site.getTemplatePackage();
            }
        } else if (request != null) {
            pkg = ContentManagerAccessCheckFilter.getCurrentSiteTemplatePackage(request);
        }

        return pkg;
    }

    private static void addCss(StringBuilder buf, HttpServletRequest request, HttpServletResponse response, boolean frame) {
        String context = request.getContextPath();

        String theme = (String) request.getSession().getAttribute(Constants.UI_THEME);

        List<String> cssStyles = frame ? getConfig().getCssStylesForFrame() : getConfig().getCssStyles();

        for (String css : cssStyles) {
            if (frame && css.startsWith("/gwt/resources/css") && theme != null) {
                try {
                    String themedCss = css.replaceFirst("/gwt/resources/css", "/gwt/resources/css/" + theme);
                    if (request.getServletContext().getResource(themedCss) != null) {
                        css = themedCss;
                    }
                } catch (MalformedURLException e) {
                    logger.error("Invalid path", e);
                }
            }
            buf.append("<link type=\"text/css\" href=\"");
            if (response != null) {
                buf.append(context.isEmpty() ? response.encodeURL(css) : response.encodeURL(context + css));
            } else {
                buf.append(context).append(css);
            }
            buf.append("\" rel=\"stylesheet\"/>\n");
        }
    }

    private static void addJavaScript(StringBuilder buf, HttpServletRequest request, HttpServletResponse response, RenderContext ctx) {
        String context = request.getContextPath();
        for (String js : getConfig().getJavaScripts()) {
            buf.append("<script type=\"text/javascript\" src=\"");
            if (response != null) {
                buf.append(context.isEmpty() ? response.encodeURL(js) : response.encodeURL(context + js));
            } else {
                buf.append(context).append(js);
            }
            buf.append("\"></script>\n");
        }
    }

    private static GWTResourceConfig getConfig() {
        if (config == null) {
            synchronized (GWTInitializer.class) {
                if (config == null) {
                    config = (GWTResourceConfig) SpringContextSingleton.getBean("GWTResourceConfig");
                }
            }
        }
        return config;
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
                    s.append("\"").append(StringEscapeUtils.escapeJavaScript(param.getKey())).append("\":\"")
                            .append(StringEscapeUtils.escapeJavaScript(String.valueOf(param.getValue()))).append("\"");
                }
            }
        }

        s.append("}; contextJsParameters=" + JahiaGWTParameters.JAHIA_GWT_PARAMETERS + ";");

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
