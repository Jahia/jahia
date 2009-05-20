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
