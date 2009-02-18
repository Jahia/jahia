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

package org.jahia.ajax.gwt.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.rss.GWTJahiaRSSFeed;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.*;

import java.util.List;


/**
 * Created by Jahia.
 * User: ktlili
 * Date: 5 juil. 2007
 * Time: 14:04:49
 * To change this template use File | Settings | File Templates.
 */
public interface JahiaServiceAsync {

    void drawAdministrationLauncher(GWTJahiaPageContext page, AsyncCallback async);

    void drawLogoutUrl(GWTJahiaPageContext page, AsyncCallback async);

    void drawLoginUrl(GWTJahiaPageContext page, AsyncCallback async);

    void drawPagePropertiesUrl(GWTJahiaPageContext page, AsyncCallback async);

    void workflowLauncher(GWTJahiaPageContext page, AsyncCallback async);

    void drawNormalModeLink(GWTJahiaPageContext page, AsyncCallback async);

    void drawEditModeLink(GWTJahiaPageContext page, AsyncCallback async);

    void drawPreviewModeLink(GWTJahiaPageContext page, AsyncCallback async);

    void drawAddContainerUrl(GWTJahiaPageContext page, int parentConatinerId, String containerListName, AsyncCallback async);

    void saveUserProperties(GWTJahiaPageContext page, List<GWTJahiaPageUserProperty> properties, AsyncCallback async);

    void saveJahiaPreference(GWTJahiaPreference jahiaPreference,AsyncCallback async);

    void getJahiaPreference(String name,AsyncCallback async);

    void drawPortletInstanceOutput(GWTJahiaPageContext page, String windowID, String entryPointIDStr, String pathInfo, String queryString, AsyncCallback<GWTJahiaPortletOutputBean> async);

    void deleteBookmark(GWTJahiaPageContext page, GWTJahiaBookmark gwtJahiaBookmark, AsyncCallback async);

    /**
     * Get list of all bookmarks for current user.
     *
     * @param page
     * @return
     */
    void getBookmarks(GWTJahiaPageContext page, AsyncCallback async);

    void releaseLocks(String lockType, AsyncCallback async) ;

    void loadRssFeed(GWTJahiaPageContext pageContext, String url, Integer maxEntries, AsyncCallback<GWTJahiaRSSFeed> async);

    void getAvailableLanguagesAndWorkflowStates (boolean displayIsoCode,boolean displayLanguage, boolean inEngine, AsyncCallback<GWTJahiaLanguageSwitcherBean> async) ;

    void inlineUpdateField(Integer containerID, Integer fieldID, String updatedContent, AsyncCallback<GWTJahiaInlineEditingResultBean> async);

    void isInlineEditingAllowed(Integer containerID, Integer fieldID, AsyncCallback<Boolean> async);

    void getProcessJob(String name, String groupName, AsyncCallback<GWTJahiaProcessJob> async);

    void changeLocaleForAllPagesAndEngines(String languageSelected, AsyncCallback async) ;

    void changeLocaleForCurrentEngine(String languageSelected, AsyncCallback async);

    void getLanguageURL(String language, AsyncCallback<String> async) ;

    void getAvailableSites(AsyncCallback<List<GWTJahiaSite>> asyncCallback);
    
    void releaseLock(String lockType, AsyncCallback<Boolean> async);
}
