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
package org.jahia.ajax.gwt.client.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.jahia.ajax.gwt.client.data.rss.GWTJahiaRSSFeed;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.util.URL;

import java.util.List;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 5 juil. 2007
 * Time: 14:04:48
 * To change this template use File | Settings | File Templates.
 */
public interface JahiaService extends RemoteService {
    /**
     * Utility/Convenience class.
     * Use JahiaService.App.getInstance() to access static instance of MyServiceAsync
     */
    public static class App {
        private static JahiaServiceAsync ourInstance = null;

        public static synchronized JahiaServiceAsync getInstance() {
            if (ourInstance == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint()+"base/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                ourInstance = (JahiaServiceAsync) GWT.create(JahiaService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(serviceEntryPoint);
            }

            return ourInstance;
        }
    }

    public String drawAdministrationLauncher(GWTJahiaPageContext page);

    public String drawLogoutUrl(GWTJahiaPageContext page);

    public String drawLoginUrl(GWTJahiaPageContext page);

    public String drawPagePropertiesUrl(GWTJahiaPageContext page);

    public String workflowLauncher(GWTJahiaPageContext page);

    public String drawNormalModeLink(GWTJahiaPageContext page);

    public String drawEditModeLink(GWTJahiaPageContext page);

    public String drawPreviewModeLink(GWTJahiaPageContext page);

    public String drawAddContainerUrl(GWTJahiaPageContext page, int parentConatinerId, String containerListName);

    public void saveUserProperties(GWTJahiaPageContext page, List<GWTJahiaPageUserProperty> properties);

    public void saveJahiaPreference(GWTJahiaPreference jahiaPreference);

    public GWTJahiaPreference getJahiaPreference(String name);

    public GWTJahiaPortletOutputBean drawPortletInstanceOutput(GWTJahiaPageContext page, String windowID, String entryPointIDStr, String pathInfo, String queryString);

    public void deleteBookmark(GWTJahiaPageContext page, GWTJahiaBookmark gwtJahiaBookmark);

    public List<GWTJahiaBookmark> getBookmarks(GWTJahiaPageContext page);

    public void releaseLocks(String lockType) throws GWTJahiaServiceException ;
    
    public GWTJahiaRSSFeed loadRssFeed(GWTJahiaPageContext pageContext, String url, Integer maxEntries) throws GWTJahiaServiceException;

    public GWTJahiaLanguageSwitcherBean getAvailableLanguagesAndWorkflowStates (boolean displayIsoCode,boolean displayLanguage, boolean inEngine);

    public GWTJahiaInlineEditingResultBean inlineUpdateField(Integer containerID, Integer fieldID, String updatedContent);

    public Boolean isInlineEditingAllowed(Integer containerID, Integer fieldID);

    public GWTJahiaProcessJob getProcessJob(String name, String groupName);

    public void changeLocaleForAllPagesAndEngines(String languageSelected) throws GWTJahiaServiceException;

    public void changeLocaleForCurrentEngine(String languageSelected);

    public String getLanguageURL(String language) throws GWTJahiaServiceException;

    public List<GWTJahiaSite> getAvailableSites ();
    
    Boolean releaseLock(String lockType);
}
