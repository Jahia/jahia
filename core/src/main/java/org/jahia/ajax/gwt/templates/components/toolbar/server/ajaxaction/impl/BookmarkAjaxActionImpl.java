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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.impl;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.commons.client.beans.GWTAjaxActionResult;
import org.jahia.ajax.gwt.commons.client.beans.GWTProperty;
import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.preferences.JahiaPreferencesProvider;
import org.jahia.services.preferences.bookmarks.BookmarksJahiaPreferenceKey;
import org.jahia.services.preferences.bookmarks.BookmarksJahiaPreferenceValue;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;

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

    public GWTAjaxActionResult execute(JahiaData jahiaData, String action, Map<String, GWTProperty> gwtPropertiesMap) {
        GWTAjaxActionResult result = new GWTAjaxActionResult();
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
    public String deleteBookmarkCurrentPage(JahiaData jahiaData, Map<String, GWTProperty> gwtPropertiesMap) {
        // get processing context
        ProcessingContext processingContext = jahiaData.getProcessingContext();

        // get bookmarks provider
        JahiaPreferencesProvider jahiaPreferencesProvider = getBookmarksJahiaPreferencesProvider();

        // create a bookmarksJahiaPreferenceKey
        BookmarksJahiaPreferenceKey bookmarksJahiaPreferenceKey = (BookmarksJahiaPreferenceKey) jahiaPreferencesProvider.createPartialJahiaPreferenceKey(processingContext);
        bookmarksJahiaPreferenceKey.setPid(processingContext.getPageID());

        // set preference
        jahiaPreferencesProvider.deleteJahiaPreference(bookmarksJahiaPreferenceKey);
        logger.debug("bookmark [pageID=" + bookmarksJahiaPreferenceKey.getPid() + " added]");
        return "bookmark [pageID=" + bookmarksJahiaPreferenceKey.getPid() + " added]";
    }

    /**
     * Bookmark currentpage
     * @param jahiaData
     * @param gwtPropertiesMap
     * @return
     */
    public String bookmarkCurrentPage(JahiaData jahiaData, Map<String, GWTProperty> gwtPropertiesMap) {
        // get processing context
        ProcessingContext processingContext = jahiaData.getProcessingContext();

        // get bookmarks provider
        JahiaPreferencesProvider jahiaPreferencesProvider = getBookmarksJahiaPreferencesProvider();

        // create a bookmarksJahiaPreferenceKey
        BookmarksJahiaPreferenceKey bookmarksJahiaPreferenceKey = (BookmarksJahiaPreferenceKey) jahiaPreferencesProvider.createPartialJahiaPreferenceKey(processingContext);
        bookmarksJahiaPreferenceKey.setPid(processingContext.getPageID());
        
        // create a jahiaPreferenceValue
        BookmarksJahiaPreferenceValue jahiaPreferenceValue = (BookmarksJahiaPreferenceValue) jahiaPreferencesProvider.createEmptyJahiaPreferenceValue();

        //jahiaPreferenceValue.setProperty("url", url);
        bookmarksJahiaPreferenceKey.setPid(processingContext.getPageID());

        // set preference
        jahiaPreferencesProvider.setJahiaPreference(bookmarksJahiaPreferenceKey, jahiaPreferenceValue);
        logger.debug("bookmark [pageID=" + bookmarksJahiaPreferenceKey.getPid() + " added]");
        return "bookmark [pageID=" + bookmarksJahiaPreferenceKey.getPid() + " added]";
    }

    /**
     * Get Bookmark jahia preference provider
     *
     * @return
     */
    private JahiaPreferencesProvider getBookmarksJahiaPreferencesProvider() {
        try {
            if (bookmarksPreferencesProvider == null) {
                bookmarksPreferencesProvider = SERVICES_REGISTRY.getJahiaPreferencesService().getPreferencesProviderByType("org.jahia.preferences.provider.bookmarks");
            }
            return bookmarksPreferencesProvider;
        } catch (JahiaPreferenceProviderException e) {
            logger.error(e, e);
        }
        return null;
    }
}
