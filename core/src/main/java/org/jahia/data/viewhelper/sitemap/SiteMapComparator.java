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

import java.util.Comparator;

/**
 * <p>Title: Informations site map page</p>
 * <p>Description: Implements the 'Comparator' interface to permit the flat site map
 * sort.
 * Private class, should be used with the site map view helper.
 * </p>
 * <p>Copyright: MAP (Jahia Solutions S�rl 2002)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 * @author MAP
 * @version 1.0
 */
final class SiteMapComparator implements Comparator {

    public SiteMapComparator(String languageCode, int orderBy, int sortOrder) {
        this.languageCode = languageCode;
        this.sortOrder = sortOrder;
        this.orderBy = orderBy;
    }

    /**
     * Compare the page site map to another page site map regarding the kind of
     * order and the sort order defined by the method 'setSortOrder', 'setOrderBy'.
     * @return A negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     */
    public int compare(Object object1, Object object2) {
        final PageSiteMap pageSiteMap1 = (PageSiteMap)object1;
        final PageSiteMap pageSiteMap2 = (PageSiteMap)object2;
        if (orderBy == FlatSiteMapViewHelper.ORDER_BY_PAGE_TITLE) {
            final String pageTitle1 = pageSiteMap1.getPageTitle(languageCode).toLowerCase();
            final String pageTitle2 = pageSiteMap2.getPageTitle(languageCode).toLowerCase();
            return sortOrder == FlatSiteMapViewHelper.ASCENDING_ORDER ?
                               pageTitle1.compareTo(pageTitle2) :
                               pageTitle2.compareTo(pageTitle1);
        } else if (orderBy == FlatSiteMapViewHelper.ORDER_BY_PAGE_ID) {
            return sortOrder == FlatSiteMapViewHelper.ASCENDING_ORDER ?
                                   pageSiteMap1.getPageID() - pageSiteMap2.getPageID() :
                                   pageSiteMap2.getPageID() - pageSiteMap1.getPageID();
        } else if (orderBy == FlatSiteMapViewHelper.ORDER_BY_PAGE_LEVEL) {
            final String pageTitle1 = pageSiteMap1.getPageTitle(languageCode).toLowerCase();
            final String pageTitle2 = pageSiteMap2.getPageTitle(languageCode).toLowerCase();
            boolean diff = pageSiteMap2.getPageLevel() - pageSiteMap1.getPageLevel() == 0; // Equality test case
            return sortOrder == FlatSiteMapViewHelper.ASCENDING_ORDER ?
                               diff ?
                                   pageTitle1.compareTo(pageTitle2) :
                                   pageSiteMap1.getPageLevel() - pageSiteMap2.getPageLevel()
                    :
                               diff ?
                                   pageTitle2.compareTo(pageTitle1) :
                                   pageSiteMap2.getPageLevel() - pageSiteMap1.getPageLevel();
        }
        // Let the order as is per default.
        return 0;
    }

    // Comparison parameters
    private String languageCode;
    private int orderBy;
    private int sortOrder;
}
