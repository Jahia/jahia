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

package org.jahia.ajax.gwt.client.core;

import com.google.gwt.i18n.client.Dictionary;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.action.DeployTemplatesActionItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 31 oct. 2007
 * Time: 11:01:16
 * 
 */
public class JahiaGWTParameters {
    public static final String SERVICE_ENTRY_POINT = "serviceEntryPoint";
    public static final String JAHIA_GWT_PARAMETERS = "jahiaGWTParameters";
    public static final String CURRENT_USER_NAME = "currentUser";
    public static final String COPYRIGHT = "copyright";
    public static final String UI_LANGUAGE = "uilang";
    public static final String UI_LANGUAGE_DISPLAY_NAME = "uilangdisplayname";
    public static final String LANGUAGE = "lang";
    public static final String LANGUAGE_DISPLAY_NAME = "langdisplayname";
    public static final String SITE_UUID = "siteUuid";
    public static final String SITE_KEY = "siteKey";
    public static final String WORKSPACE = "workspace";
    public static final String PATH_INFO = "pathInfo";
    public static final String CONTEXT_PATH = "contextPath";
    public static final String SERVLET_PATH = "servletPath";
    public static final String QUERY_STRING = "queryString";
    public static final String STUDIO_URL = "studioUrl";
    public static final String BASE_URL = "baseUrl";

    public static final String SYSTEM_USER = " system "; // org.jahia.jaas.JahiaLoginModule.SYSTEM

    private static Dictionary jahiaParamDictionary = Dictionary.getDictionary(JAHIA_GWT_PARAMETERS);
    private static String baseUrl;
    private static String language;
    private static String languageDisplayName;
    private static String siteUUID;
    private static String siteKey;
    private static String workspace;

    private static String sitesLocation;
    private static Map<String, GWTJahiaNode> sitesMap;
    private static GWTJahiaNode siteNode;

    public static String getServiceEntryPoint() {
        return jahiaParamDictionary.get(SERVICE_ENTRY_POINT);
    }

    public static String getCurrentUser() {
        return jahiaParamDictionary.get(CURRENT_USER_NAME);
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

    public static String getUILanguage() {
        return jahiaParamDictionary.get(UI_LANGUAGE);
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
        baseUrl = getBaseUrl();
        baseUrl = baseUrl.substring(0,baseUrl.lastIndexOf('/')+1) + language;
        setNativeLanguage(newLanguage.getLanguage());
    }

    private static native void setNativeLanguage(String newLanguage) /*-{
        $wnd.jahiaGWTParameters.lang  = newLanguage;
        if ($wnd.contextJsParameters) {
            $wnd.contextJsParameters.lang = newLanguage;
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
    }

    private static native void setNativeSiteUUID(String newSiteUUID) /*-{
        $wnd.jahiaGWTParameters.siteUuid  = newSiteUUID;
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
    }

    private static native void setNativeSiteKey(String newSiteKey) /*-{
        $wnd.jahiaGWTParameters.siteKey  = newSiteKey;
    }-*/;

    public static GWTJahiaNode getSiteNode() {
        return siteNode;
    }

    public static void setSiteNode(GWTJahiaNode siteNode) {
        JahiaGWTParameters.siteNode = siteNode;

        setSiteUUID(siteNode.getSiteUUID());
        setSiteKey(siteNode.getSiteKey());
    }

    public static List<GWTJahiaLanguage> getSiteLanguages() {
        return (List<GWTJahiaLanguage>) getSiteNode().get(GWTJahiaNode.SITE_LANGUAGES);
    }


    public static String getSitesLocation() {
        return sitesLocation;
    }

    public static void setSitesLocation(String sitesLocation) {
        JahiaGWTParameters.sitesLocation = sitesLocation;
    }

    public static Map<String, GWTJahiaNode> getSitesMap() {
        return sitesMap;
    }

    public static void setSitesMap(Map<String, GWTJahiaNode> sitesMap) {
        JahiaGWTParameters.sitesMap = sitesMap;
    }

    public static void setSite(GWTJahiaNode node, Linker linker) {
        setSiteNode(sitesMap.get(node.getSiteUUID()));
        if (linker != null) {
            DeployTemplatesActionItem.refreshAllMenus(linker);
        }
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
        workspace = newWorkspace;
        for (UrlUpdater urlUpdater : updaters) {
            urlUpdater.updateEntryPointUrl();
        }
        setNativeWorkspace(newWorkspace);
    }

    private static native void setNativeWorkspace(String newWorkspace) /*-{
        $wnd.jahiaGWTParameters.workspace  = newWorkspace;
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

    /**
     * @param name the parameter to get
     * @return return null if the param is not present in the dictionary instead of throwing
     *         a missing resource exception
     */
    public static String getParam(String name) {
        if (!jahiaParamDictionary.keySet().contains(name)) {
            return null;
        }
        return jahiaParamDictionary.get(name);
    }

    public static void addUpdater(UrlUpdater updater ) {
        updaters.add(updater);
    }

    static List<UrlUpdater> updaters = new ArrayList<UrlUpdater>();

    public static interface UrlUpdater {
        void updateEntryPointUrl();
    }

    public static void refreshSitesList() {
        JahiaContentManagementService
                .App.getInstance().getRoot(Arrays.asList(sitesLocation), Arrays.asList("jnt:virtualsite"), null, null,GWTJahiaNode.DEFAULT_SITE_FIELDS,null,null, false, false, null, null, new BaseAsyncCallback<List<GWTJahiaNode>>() {
            public void onSuccess(List<GWTJahiaNode> sites) {
                sitesMap.clear();
                for (GWTJahiaNode site : sites) {
                    sitesMap.put(site.getUUID(), site);
                }
            }
        });
    }

}
