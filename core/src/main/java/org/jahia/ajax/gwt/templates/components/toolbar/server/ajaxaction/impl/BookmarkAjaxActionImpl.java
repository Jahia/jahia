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
package org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.impl;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.preferences.JahiaPreferencesProvider;
import org.jahia.services.preferences.JahiaPreferencesQueryHelper;
import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.preferences.bookmarks.BookmarksJahiaPreference;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.pages.ContentPage;

import java.util.Map;

/**
 * User: jahia
 * Date: 31 juil. 2008
 * Time: 13:15:30
 */
public class BookmarkAjaxActionImpl extends AjaxAction {
    private static Logger logger = Logger.getLogger(BookmarkAjaxActionImpl.class);

    private static final ServicesRegistry SERVICES_REGISTRY = ServicesRegistry.getInstance();
    private static JahiaPreferencesProvider bookmarksPreferencesProvider;
    public final static String BOOKMARK_CURRENT_PAGE = "bookmark.current.page";
    public final static String BOOKMARK_DELETE_CURRENT_PAGE = "bookmark.delete.current.page";

    public GWTJahiaAjaxActionResult execute(JahiaData jahiaData, String action, Map<String, GWTJahiaProperty> gwtPropertiesMap) {
        GWTJahiaAjaxActionResult result = new GWTJahiaAjaxActionResult();
        if (action != null) {
            if (action.equalsIgnoreCase(BOOKMARK_CURRENT_PAGE)) {
                result.setValue(bookmarkCurrentPage(jahiaData, gwtPropertiesMap));
            } else if (action.equalsIgnoreCase(BOOKMARK_DELETE_CURRENT_PAGE)) {
                result.setValue(deleteBookmarkCurrentPage(jahiaData, gwtPropertiesMap));
            }
        }
        return result;
    }

    /**
     * Bookmark current page
     *
     * @param jahiaData
     * @return
     */
    public String deleteBookmarkCurrentPage(JahiaData jahiaData, Map<String, GWTJahiaProperty> gwtPropertiesMap) {

        try {
            ProcessingContext processingContext = jahiaData.getProcessingContext();

            // get bookmarks provider
            JahiaPreferencesProvider jahiaPreferencesProvider = getBookmarksJahiaPreferencesProvider();

            // delete a bookmarksJahiaPreferenceKey
            jahiaPreferencesProvider.deleteJahiaPreference(processingContext.getUser(), JahiaPreferencesQueryHelper.getBookmarkSQL(processingContext.getPageID()));

            // set preference
            return "";
        } catch (Exception e) {
            logger.error(e, e);
            return "";
        }

    }

    /**
     * Bookmark currentpage
     *
     * @param jahiaData
     * @param gwtPropertiesMap
     * @return
     */
    public String bookmarkCurrentPage(JahiaData jahiaData, Map<String, GWTJahiaProperty> gwtPropertiesMap) {
        try {
            // get processing context
            ProcessingContext processingContext = jahiaData.getProcessingContext();
            ContentPage page = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(processingContext.getPageID(), false);
            String pageUUID = page.getUUID();

            // get bookmarks provider
            JahiaPreferencesProvider jahiaPreferencesProvider = getBookmarksJahiaPreferencesProvider();

            // create a bookmarksJahiaPreferenceKey
            JahiaPreference bookmarksJahiaPreference = jahiaPreferencesProvider.createJahiaPreferenceNode(processingContext);
            BookmarksJahiaPreference node = (BookmarksJahiaPreference) bookmarksJahiaPreference.getNode();
            node.setPageUUID(pageUUID);


            // set preference
            jahiaPreferencesProvider.setJahiaPreference(bookmarksJahiaPreference);
            logger.debug("bookmark [pageID=" + node.getPageUUID() + " added]");
            return "bookmark [pageID=" + node.getPageUUID() + " added]";
        } catch (Exception e) {
            logger.error(e, e);
            return null;
        }
    }


    /**
     * Get Bookmark jahia preference provider
     *
     * @return
     */
    private JahiaPreferencesProvider getBookmarksJahiaPreferencesProvider() {
        try {
            if (bookmarksPreferencesProvider == null) {
                bookmarksPreferencesProvider = SERVICES_REGISTRY.getJahiaPreferencesService().getPreferencesProviderByType("bookmarks");
            }
            return bookmarksPreferencesProvider;
        } catch (JahiaPreferenceProviderException e) {
            logger.error(e, e);
        }
        return null;
    }

}
