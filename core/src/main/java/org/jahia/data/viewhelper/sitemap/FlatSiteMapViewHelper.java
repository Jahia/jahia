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
package org.jahia.data.viewhelper.sitemap;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.PageLoadFlags;
import org.jahia.services.usermanager.JahiaUser;

import java.util.Arrays;
import java.util.Iterator;

/**
 * <p>The Jahia Shared Modification is: Jahia View Helper</p>
 * <p/>
 * <p>Description:
 * Create a flat tree from Jahia page tree structure destinated to display a site
 * map.
 * </p>
 * <p>Copyright: MAP (Jahia Solutions S�rl 2002)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 *
 * @author MAP
 * @version 1.0
 */
public class FlatSiteMapViewHelper extends SiteMapViewHelper {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(FlatSiteMapViewHelper.class);

    // Flat site map sort flag.
    public static final int ORDER_AS_IS = -1;
    public static final int ORDER_BY_PAGE_TITLE = 0;
    public static final int ORDER_BY_PAGE_ID = 1;
    public static final int ORDER_BY_PAGE_LEVEL = 2;
    public static final int ASCENDING_ORDER = 0;
    public static final int DESCENDING_ORDER = 1;

    /**
     * Create a view helper on a entire Jahia site map restricted to the actual
     * logged user.
     *
     * @param user            The actual user logged in Jahia.
     * @param startPage       The start page for site map.
     * @param pageInfosFlag   Kind of page infos desired. This parameter can associate
     * @param defaultMaxLevel Site map expansion default level max.
     *                        the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     *                        ARCHIVED_PAGE_INFOS constants.
     * @param languageCode    Get the page with the specified language code.
     * @param pagesFilter     Filter object for page filtering  
     */
    public FlatSiteMapViewHelper(ProcessingContext jParams,
                                 JahiaUser user,
                                 ContentPage startPage,
                                 int pageInfosFlag,
                                 String languageCode,
                                 int defaultMaxLevel,
                                 PagesFilter pagesFilter) {
        super(user, startPage, pageInfosFlag, languageCode, defaultMaxLevel, pagesFilter, false);
        try {
            final JahiaPageService jahiaPageService = ServicesRegistry.getInstance().getJahiaPageService();
            final Iterator en = jahiaPageService.getAllPages(jParams.getSiteID(), PageLoadFlags.DIRECT, jParams, user);
            while (en.hasNext()) {
                final JahiaPage page = (JahiaPage) en.next();
                final ContentPage cPage = page.getContentPage();
                if (pagesFilter == null
                        || !pagesFilter.filterForDisplay(cPage, jParams)) {
                    final PageSiteMap pageSiteMap = new PageSiteMap(cPage
                            .getObjectKey(), cPage
                            .getTitles(ContentPage.LAST_UPDATED_TITLES));
                    _jahiaPageSiteMap.add(pageSiteMap);
                }
            }
            logger.debug("Found: " + _jahiaPageSiteMap.size());

            // Initialize flat site map
            _sortedPageSiteMap = new PageSiteMap[getJahiaPageSiteMap().size()];
            getJahiaPageSiteMap().toArray(_sortedPageSiteMap);
            // Set the default sort.
            sortSiteMap(languageCode, ORDER_BY_PAGE_TITLE, ASCENDING_ORDER);
        } catch (JahiaException je) {
            logger.error("Unable to instantiate FlatSiteMapViewHelper: ", je);
        }
    }

    /**
     * Sort the site map by given criteriums such as page title, page ID, etc...
     *
     * @param languageCode The language code in which the sort should be made.
     * @param orderBy      The sort key. One of ORDER_AS_IS, ORDER_BY_PAGE_TITLE,
     *                     ORDER_BY_PAGE_ID, ORDER_BY_PAGE_LEVEL.
     * @param sortOrder    One of ASCENDING_ORDER or DESCENDING_ORDER order.
     */
    public void sortSiteMap(String languageCode, int orderBy, int sortOrder) {
        if (languageCode == null) {
            languageCode = "en"; /** todo : Set a better default language. */
        }
        _sortOrder = sortOrder;
        SiteMapComparator siteMapComparator = new SiteMapComparator(languageCode, orderBy, sortOrder);
        Arrays.sort(_sortedPageSiteMap, siteMapComparator);
    }

    /**
     * Return the last site map ASCENDING_ORDER or DESCENDING_ORDER order.
     *
     * @return The sort order.
     */
    public int getSortOrder() {
        return _sortOrder;
    }

    /**
     * Get a Jahia page content indexed by the sorted flat site map.
     *
     * @param index The sorted flat site map index.
     * @return The Jahia page content object.
     */
    public ContentPage getContentPage(int index) {
        final PageSiteMap pageSiteMap = _sortedPageSiteMap[index];
        return pageSiteMap.isDisplayable() ? lookupContentPage(pageSiteMap.getPageID()) : null;
    }

    /**
     * Get a page title in a given language indexed by the sorted flat site map.
     *
     * @param index        The sorted flat site map index.
     * @param languageCode The language code in which the title should be displayed.
     * @return The page title in a given language indexed by the sorted flat site map.
     */
    public String getPageTitle(int index, String languageCode) {
        return _sortedPageSiteMap[index].getPageTitle(languageCode);
    }

    /**
     * Get the page ID indexed by the sorted flat site map.
     *
     * @param index The sorted flat site map index.
     * @return The page ID indexed by the sorted flat site map.
     */
    public int getPageID(int index) {
        return _sortedPageSiteMap[index].getPageID();
    }

    /**
     * Return the index of a given pageID, -1 if not found
     *
     * @param pageID
     */
    public int getPageIndex(int pageID) {
        for (int i = 0; i < _sortedPageSiteMap.length; i++) {
            final PageSiteMap pageSiteMap = _sortedPageSiteMap[i];
            if (pageSiteMap.getPageID() == pageID) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the page level indexed by the sorted flat site map. The there is two
     * same page level the page title is then used to sorted the level.
     *
     * @param index The sorted flat site map index.
     * @return The page level indexed by the sorted flat site map index.
     */
    public int getPageLevel(int index) {
        return _sortedPageSiteMap[index].getPageLevel();
    }

    /**
     * Get the parent page ID indexed by the sorted flat site map.
     *
     * @param index The sorted flat site map index.
     * @return The parent page ID indexed by the sorted flat site map index.
     */
    public int getParentPageID(int index) {
        return _sortedPageSiteMap[index].getParentPageID();
    }

    /**
     * Return the size of the sorted array corresponding to the number of page
     * included in the site map.
     *
     * @return The flat site map size.
     */
    public int size() {
        return _sortedPageSiteMap.length;
    }

    // Sort flags for flat site map.
    private int _sortOrder;
    private PageSiteMap[] _sortedPageSiteMap;
}
