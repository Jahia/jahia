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
package org.jahia.services.toolbar.resolver.impl;

import org.jahia.services.toolbar.resolver.VisibilityResolver;
import org.jahia.services.preferences.JahiaPreferencesProvider;
import org.jahia.services.preferences.JahiaPreferencesXpathHelper;
import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.preferences.bookmarks.BookmarksJahiaPreference;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.data.JahiaData;
import org.jahia.registries.ServicesRegistry;
import org.jahia.params.ProcessingContext;

/**
 * User: jahia
 * Date: 7 ao�t 2008
 * Time: 10:47:23
 */
public class BookmarkActionVisibilityResolver implements VisibilityResolver {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(BookmarkActionVisibilityResolver.class);
    private static final String BOOKMARK_CURRENT_PAGE = "bookmark.current.page";
    private static final String DELETE_BOOKMARK_CURRENT_PAGE = "delete.bookmark.current.page";

    public boolean isVisible(JahiaData jahiaData, String type) {
        if (type != null) {
            if (type.equalsIgnoreCase(BOOKMARK_CURRENT_PAGE)) {
                return currentPageBookmarked(jahiaData);
            } else if (type.equalsIgnoreCase(DELETE_BOOKMARK_CURRENT_PAGE)) {
                return !currentPageBookmarked(jahiaData);
            }
        }
        return false;
    }

    /**
     * Retrun true if the current page is bookmarked
     *
     * @param jahiaData
     * @return
     */
    private boolean currentPageBookmarked(JahiaData jahiaData) {
        try {
            JahiaPreferencesProvider jahiaPreferencesProvider = ServicesRegistry.getInstance().getJahiaPreferencesService().getPreferencesProviderByType("bookmarks");
            // get processing context
            ProcessingContext processingContext = jahiaData.getProcessingContext();

            // create a bookmarksJahiaPreferenceKey
            JahiaPreference bookmarksJahiaPreference = jahiaPreferencesProvider.getJahiaPreference(processingContext.getUser(), JahiaPreferencesXpathHelper.getBookmarkXpath(processingContext.getPageID()));

            return bookmarksJahiaPreference == null;
        } catch (JahiaPreferenceProviderException e) {
            logger.error(e, e);
            return false;
        }
    }
}
