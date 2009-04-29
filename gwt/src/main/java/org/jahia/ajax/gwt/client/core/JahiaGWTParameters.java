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
package org.jahia.ajax.gwt.client.core;

import com.google.gwt.i18n.client.Dictionary;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.util.URL;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 31 oct. 2007
 * Time: 11:01:16
 * To change this template use File | Settings | File Templates.
 */
public class JahiaGWTParameters {
    public static final String SERVICE_ENTRY_POINT = "serviceEntryPoint";
    public static final String PID = "pid";
    public static final String JAHIA_GWT_PARAMETERS = "jahiaGWTParameters";
    public static final String OPERATION_MODE = "op";
    public static final String CURRENT_USER_NAME = "currentUser";
    public static final String ALIASED_USER_NAME = "aliasedUser";
    public static final String PREVIEW_DATE = "previewDate";
    public static final String ENABLE_ADV_PREVIEW_SETTINGS = "enableAdvPreviewSettings";
    public static final String COPYRIGHT = "copyright";
    public static final String LANGUAGE = "lang";
    public static final String ENGINE_LANGUAGE = "enginelang";
    public static final String JAHIA_SERVER_PATH = "serverPath";
    public static final String PATH_INFO = "pathInfo";
    public static final String QUERY_STRING = "queryString";
    public static final String USER_ALLOWED_TO_UNLOCK_FILES = "allowFileUnlock";
    public static final String SYSTEM_USER = " system "; // org.jahia.jaas.JahiaLoginModule.SYSTEM

    private static Dictionary jahiaParamDictionary = Dictionary.getDictionary(JAHIA_GWT_PARAMETERS);


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
        String value = jahiaParamDictionary.get(PID);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return -1;
    }

    public static String getServiceEntryPoint() {
        return jahiaParamDictionary.get(SERVICE_ENTRY_POINT);
    }

    public static String getOperationMode() {
        return jahiaParamDictionary.get(OPERATION_MODE);
    }

    public static String getCurrentUser() {
        return jahiaParamDictionary.get(CURRENT_USER_NAME);
    }

    public static String getAliasedUser() {
        return getParam(ALIASED_USER_NAME);
    }

    public static String getPreviewDate() {
        return getParam(PREVIEW_DATE);
    }

    public static String getEnableAdvPreviewSettings() {
        return getParam(ENABLE_ADV_PREVIEW_SETTINGS);
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

    public static String getJahiaServerPath() {
        return jahiaParamDictionary.get(JAHIA_SERVER_PATH);
    }

    public static String getLanguage() {
        return jahiaParamDictionary.get(LANGUAGE);
    }

    public static String getEngineLanguage() {
        if (jahiaParamDictionary.keySet().contains(ENGINE_LANGUAGE)) {
            return jahiaParamDictionary.get(ENGINE_LANGUAGE);
        }
        return getLanguage();
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


    public static boolean isUserAllowedToUnlockFiles() {
        return Boolean.valueOf(
                jahiaParamDictionary.get(USER_ALLOWED_TO_UNLOCK_FILES))
                .booleanValue();
    }

}
