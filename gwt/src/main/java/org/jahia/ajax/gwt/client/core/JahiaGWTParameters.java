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
package org.jahia.ajax.gwt.client.core;

import com.google.gwt.i18n.client.Dictionary;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.util.URL;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 31 oct. 2007
 * Time: 11:01:16
 * To change this template use File | Settings | File Templates.
 */
public class JahiaGWTParameters {
    public static final String SERVICE_ENTRY_POINT = "serviceEntryPoint";
    public static final String JAHIA_GWT_PARAMETERS = "jahiaGWTParameters";
    public static final String CURRENT_USER_NAME = "currentUser";
    public static final String COPYRIGHT = "copyright";
    public static final String UI_LANGUAGE = "uilang";
    public static final String LANGUAGE = "lang";
    public static final String SITE_KEY = "siteKey";
    public static final String WORKSPACE = "workspace";
    public static final String PATH_INFO = "pathInfo";
    public static final String CONTEXT_PATH = "contextPath";
    public static final String SERVLET_PATH = "servletPath";
    public static final String QUERY_STRING = "queryString";
    public static final String STUDIO_URL = "studioUrl";
    public static final String BASE_URL = "baseUrl";
    public static final String USER_URL = "userUrl";

    public static final String SYSTEM_USER = " system "; // org.jahia.jaas.JahiaLoginModule.SYSTEM

    private static Dictionary jahiaParamDictionary = Dictionary.getDictionary(JAHIA_GWT_PARAMETERS);
    private static String language;
    private static String siteKey;


    /**
     * Retrieve GWTJahiaPage object
     *
     * @return a new instance of a GWTJahiaPage.
     */
    public static GWTJahiaPageContext getGWTJahiaPageContext() {
        // init panel
        GWTJahiaPageContext page = new GWTJahiaPageContext(URL.getRelativeURL());
        page.setPid(JahiaGWTParameters.getPID());
        page.setMode(JahiaGWTParameters.getOperationMode());
        return page;
    }

    public static int getPID() {
        return -1;
    }

    public static String getServiceEntryPoint() {
        return jahiaParamDictionary.get(SERVICE_ENTRY_POINT);
    }

    public static String getOperationMode() {
        return "normal";
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
    
    public static String getLanguage() {
        if (language == null) {
            language = jahiaParamDictionary.get(LANGUAGE);
        }
        return language;
    }

    public static void setLanguage(String newLanguage) {
        language = newLanguage;
        for (UrlUpdater urlUpdater : updaters) {
            urlUpdater.updateEntryPointUrl();
        }
    }

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

    public static void setSiteKey(String newSiteKey) {
        siteKey = newSiteKey;
        for (UrlUpdater urlUpdater : updaters) {
            urlUpdater.updateEntryPointUrl();
        }
    }

    public static String getContextPath() {
        return jahiaParamDictionary.get(CONTEXT_PATH);
    }

    public static String getServletPath() {
        return jahiaParamDictionary.get(SERVLET_PATH);
    }

    public static String getParameter(String name) {
        return jahiaParamDictionary.get(name);
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
}
