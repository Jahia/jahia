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
