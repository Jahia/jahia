/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.core;

import com.google.gwt.dom.client.Document;
import com.google.gwt.i18n.client.Dictionary;
import org.jahia.ajax.gwt.client.data.GWTJahiaChannel;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 31 oct. 2007
 * Time: 11:01:16
 */
public class JahiaGWTParameters {

    public static final String SERVICE_ENTRY_POINT = "serviceEntryPoint";
    public static final String JAHIA_GWT_PARAMETERS = "jahiaGWTParameters";
    public static final String CURRENT_USER_NAME = "currentUser";
    public static final String CURRENT_USER_PATH = "currentUserPath";
    public static final String COPYRIGHT = "copyright";
    public static final String UI_LANGUAGE = "uilang";
    public static final String UI_LANGUAGE_DISPLAY_NAME = "uilangdisplayname";
    public static final String ENGINE_LANGUAGE = "enginelang";
    public static final String LANGUAGE = "lang";
    public static final String LANGUAGE_DISPLAY_NAME = "langdisplayname";
    public static final String CHANNEL_IDENTIFIER = "channelIdentifier";
    public static final String CHANNEL_ORIENTATION = "channelOrientation";
    public static final String SITE_UUID = "siteUuid";
    public static final String SITE_KEY = "siteKey";
    public static final String WORKSPACE = "workspace";
    public static final String PATH_INFO = "pathInfo";
    public static final String CONTEXT_PATH = "contextPath";
    public static final String SERVLET_PATH = "servletPath";
    public static final String QUERY_STRING = "queryString";
    public static final String STUDIO_URL = "studioUrl";
    public static final String STUDIO_VISUAL_URL = "studioVisualUrl";
    public static final String EDIT_URL = "editUrl";
    public static final String BASE_URL = "baseUrl";
    public static final String DEVELOPMENT_MODE = "developmentMode";
    public static final String MODULES_SOURCES_DISK_PATH = "modulesSourcesDiskPath";
    public static final String TOOLBAR_MESSAGES = "toolbarMessages";
    public static final String USE_WEBSOCKETS = "useWebsockets";
    public static final String AREA_AUTO_ACTIVATED = "areaAutoActivated";
    public static final String DX_VERSION = "dxVersion";
    public static final String STUDIO_MAX_DISPLAYABLE_FILE_SIZE = "studioMaxDisplayableFileSize";
    public static final String SERVER_DISPLAYABLE_TIME_ZONE = "serverDisplayableTimeZone";

    public static final String SYSTEM_USER = " system "; // org.jahia.jaas.JahiaLoginModule.SYSTEM

    private static Dictionary jahiaParamDictionary = Dictionary.getDictionary(JAHIA_GWT_PARAMETERS);
    private static String baseUrl;
    private static String baseEditUrl;
    private static String language;
    private static String languageDisplayName;
    private static String siteUUID;
    private static String siteKey;
    private static String workspace;
    private static String channelIdentifier;
    private static String channelOrientation;
    private static List<GWTJahiaChannel> channels;
    private static String reactSiteKey;
    private static String reactLanguage;

    private static Map<String, GWTJahiaNode> sitesMap;
    private static GWTJahiaNode siteNode;

    public static String getServiceEntryPoint() {
        return jahiaParamDictionary.get(SERVICE_ENTRY_POINT);
    }

    public static String getCurrentUser() {
        return jahiaParamDictionary.get(CURRENT_USER_NAME);
    }

    public static String getCurrentUserPath() {
        return jahiaParamDictionary.get(CURRENT_USER_PATH);
    }

    public static boolean isDevelopmentMode() {
        return "true".equals(jahiaParamDictionary.get(DEVELOPMENT_MODE));
    }

    public static String getModulesSourcesDiskPath() {
        return jahiaParamDictionary.get(MODULES_SOURCES_DISK_PATH);
    }

    public static String getPathInfo() {
        return jahiaParamDictionary.get(PATH_INFO);
    }

    public static String getQueryString() {
        return jahiaParamDictionary.get(QUERY_STRING);
    }

    public static String getCopyright() {
        return jahiaParamDictionary.get(COPYRIGHT);
    }

    static {
        for (String s : jahiaParamDictionary.keySet()) {
            String param = jahiaParamDictionary.get(s);
            Document.get().getBody().setAttribute("data-" + s, param);
        }
    }

    public static Boolean isWebSockets() {
        try {
            return Boolean.parseBoolean(jahiaParamDictionary.get(USE_WEBSOCKETS));
        } catch (MissingResourceException e) {
            return false;
        }
    }

    public static boolean isAreaAutoActivated() {
        return "true".equals(jahiaParamDictionary.get(AREA_AUTO_ACTIVATED));
    }

    public static String getUILanguage() {
        return jahiaParamDictionary.get(UI_LANGUAGE);
    }

    public static String getDxVersion() {
        return jahiaParamDictionary.get(DX_VERSION);
    }

    public static String getUILanguageDisplayName() {
        return jahiaParamDictionary.get(UI_LANGUAGE_DISPLAY_NAME);
    }

    public static String getLanguageDisplayName() {
        if (languageDisplayName == null) {
            languageDisplayName = jahiaParamDictionary.get(LANGUAGE_DISPLAY_NAME);
        }
        return languageDisplayName;
    }

    public static String getLanguage() {
        if (language == null) {
            language = jahiaParamDictionary.get(LANGUAGE);
        }
        return language;
    }

    public static void setLanguage(GWTJahiaLanguage newLanguage) {
        language = newLanguage.getLanguage();
        languageDisplayName = newLanguage.getDisplayName();

        for (UrlUpdater urlUpdater : updaters) {
            urlUpdater.updateEntryPointUrl();
        }
        baseUrl = newBaseUrl(getBaseUrl(), language);
        String oldBaseEditUrl = getBaseEditUrl();
        if (oldBaseEditUrl != null) {
            baseEditUrl = newBaseUrl(oldBaseEditUrl, language);
        }
        setNativeLanguage(newLanguage.getLanguage());
        Document.get().getBody().setAttribute("data-lang", language);
        Document.get().getBody().setAttribute("data-langdisplayname", languageDisplayName);
    }

    private static String newBaseUrl(String baseUrl, String language) {
        return baseUrl.substring(0, baseUrl.lastIndexOf('/') + 1) + language;
    }

    public static void changeServletMapping(String oldMapping, String newMapping) {
        baseUrl = baseUrl.replaceFirst(oldMapping, newMapping);
        baseEditUrl = baseEditUrl.replaceFirst(oldMapping, newMapping);
    }

    private static native void setNativeLanguage(String newLanguage) /*-{
        $wnd.jahiaGWTParameters.lang = newLanguage;
        if ($wnd.contextJsParameters) {
            $wnd.contextJsParameters.lang = newLanguage;
        }
    }-*/;

    public static native void setEngineLanguage(String engineLanguage) /*-{
        if ($wnd.contextJsParameters) {
            $wnd.contextJsParameters.engineLang = engineLanguage;
        }
    }-*/;

    public static native void removeEngineLanguage() /*-{
        if ($wnd.contextJsParameters) {
            delete $wnd.contextJsParameters.engineLang;
        }
    }-*/;

    public static String getSiteUUID() {
        if (siteUUID == null) {
            if (jahiaParamDictionary.keySet().contains(SITE_UUID)) {
                siteUUID = jahiaParamDictionary.get(SITE_UUID);
            } else {
                siteUUID = "";
            }
        }
        return siteUUID;
    }

    private static void setSiteUUID(String newSiteUUID) {
        siteUUID = newSiteUUID;
        for (UrlUpdater urlUpdater : updaters) {
            urlUpdater.updateEntryPointUrl();
        }
        setNativeSiteUUID(newSiteUUID);
        Document.get().getBody().setAttribute("data-siteUuid", newSiteUUID);
    }

    private static native void setNativeSiteUUID(String newSiteUUID) /*-{
        $wnd.jahiaGWTParameters.siteUuid = newSiteUUID;
        if ($wnd.contextJsParameters) {
            $wnd.contextJsParameters.siteUuid = newSiteUUID;
        }
    }-*/;

    public static String getSiteKey() {
        if (siteKey == null) {
            if (jahiaParamDictionary.keySet().contains(SITE_KEY)) {
                siteKey = jahiaParamDictionary.get(SITE_KEY);
            } else {
                siteKey = "";
            }
        }
        return siteKey;
    }

    private static void setSiteKey(String newSiteKey) {
        siteKey = newSiteKey;
        setNativeSiteKey(newSiteKey);
        Document.get().getBody().setAttribute("data-siteKey", newSiteKey);
    }

    private static native void setNativeSiteKey(String newSiteKey) /*-{
        $wnd.jahiaGWTParameters.siteKey = newSiteKey;
    }-*/;

    public static GWTJahiaNode getSiteNode() {
        return siteNode;
    }

    public static void setSiteNode(GWTJahiaNode siteNode) {
        JahiaGWTParameters.siteNode = siteNode;

        setSiteUUID(siteNode.getSiteUUID());
        setSiteKey(siteNode.getSiteKey());
    }

    /**
     * Getter for the list of GWTJahiaLanguage available on this site
     *
     * @return List of GWTJahiaLanguage available on the current site.
     */
    @SuppressWarnings("unchecked")
    public static List<GWTJahiaLanguage> getSiteLanguages() {
        return (List<GWTJahiaLanguage>) getSiteNode().get(GWTJahiaNode.SITE_LANGUAGES);
    }

    /**
     * Get/Check if a particular language exist on the current site.
     *
     * @param language the language as a string that we want to get as a GWTJahiaLanguage
     * @return the GWTJahiaLanguage associated to this language if available on the current site or null otherwise
     */
    public static GWTJahiaLanguage getLanguage(String language) {
        if (language != null) {
            for (GWTJahiaLanguage jahiaLanguage : JahiaGWTParameters.getSiteLanguages()) {
                if (jahiaLanguage.getLanguage().equals(language)) {
                    return jahiaLanguage;
                }
            }
        }
        return null;
    }


    /**
     * Return a list of mandatory languages defined on current site.
     *
     * @return a list of mandatory languages defined on current site.
     */
    @SuppressWarnings("unchecked")
    public static List<String> getSiteMandatoryLanguages() {
        return (List<String>) getSiteNode().get(GWTJahiaNode.SITE_MANDATORY_LANGUAGES);
    }

    public static Map<String, GWTJahiaNode> getSitesMap() {
        return sitesMap;
    }

    public static void setSitesMap(Map<String, GWTJahiaNode> sitesMap) {
        JahiaGWTParameters.sitesMap = sitesMap;
    }

    public static String getWorkspace() {
        if (workspace == null) {
            if (jahiaParamDictionary.keySet().contains(WORKSPACE)) {
                workspace = jahiaParamDictionary.get(WORKSPACE);
            } else {
                workspace = "";
            }
        }
        return workspace;
    }

    public static void setWorkspace(String newWorkspace) {
        baseUrl = baseUrl.replaceFirst(workspace, newWorkspace);
        baseEditUrl = baseEditUrl.replaceFirst(workspace, newWorkspace);
        workspace = newWorkspace;
        for (UrlUpdater urlUpdater : updaters) {
            urlUpdater.updateEntryPointUrl();
        }
        setNativeWorkspace(newWorkspace);
        Document.get().getBody().setAttribute("data-workspace", newWorkspace);
    }

    private static native void setNativeWorkspace(String newWorkspace) /*-{
        $wnd.jahiaGWTParameters.workspace = newWorkspace;
    }-*/;

    public static String getContextPath() {
        return jahiaParamDictionary.get(CONTEXT_PATH);
    }

    public static String getServletPath() {
        return jahiaParamDictionary.get(SERVLET_PATH);
    }

    public static String getParameter(String name) {
        return jahiaParamDictionary.get(name);
    }

    public static String getBaseUrl() {
        if (baseUrl == null) {
            baseUrl = jahiaParamDictionary.get(BASE_URL);
        }
        return baseUrl;
    }

    public static String getBaseEditUrl() {
        if (baseEditUrl == null) {
            baseEditUrl = getParam(EDIT_URL);
        }
        return baseEditUrl;
    }

    /**
     * @param name the parameter to get
     * @return return null if the param is not present in the dictionary instead of throwing
     * a missing resource exception
     */
    public static String getParam(String name) {
        if (!jahiaParamDictionary.keySet().contains(name)) {
            return null;
        }
        return jahiaParamDictionary.get(name);
    }

    public static void addUpdater(UrlUpdater updater) {
        updaters.add(updater);
    }

    static List<UrlUpdater> updaters = new ArrayList<UrlUpdater>();

    public static void setChannels(List<GWTJahiaChannel> channels) {
        JahiaGWTParameters.channels = channels;
    }

    public static List<GWTJahiaChannel> getChannels() {
        return channels;
    }

    public static interface UrlUpdater {
        void updateEntryPointUrl();
    }

    public static Long getStudioMaxDisplayableFileSize() {
        return Long.parseLong(jahiaParamDictionary.get(STUDIO_MAX_DISPLAYABLE_FILE_SIZE));
    }

    public static Dictionary getJahiaParamDictionary() {
        return jahiaParamDictionary;
    }

    public static String getServerDisplayableTimeZone() {
        return jahiaParamDictionary.get(SERVER_DISPLAYABLE_TIME_ZONE);
    }

    public static String getReactSiteKey() {
        return reactSiteKey;
    }

    public static void setReactSiteKey(String reactSiteKey) {
        JahiaGWTParameters.reactSiteKey = reactSiteKey;
        for (UrlUpdater urlUpdater : updaters) {
            urlUpdater.updateEntryPointUrl();
        }
    }

    public static String getReactLanguage() {
        return reactLanguage;
    }

    public static void setReactLanguage(String reactLanguage) {
        JahiaGWTParameters.reactLanguage = reactLanguage;
        for (UrlUpdater urlUpdater : updaters) {
            urlUpdater.updateEntryPointUrl();
        }
    }

}
