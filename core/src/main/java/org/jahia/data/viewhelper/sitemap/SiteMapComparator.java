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
//

/*
 * ----- BEGIN LICENSE BLOCK -----
 * Version: JCSL 1.0
 *
 * The contents of this file are subject to the Jahia Community Source License
 * 1.0 or later (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.jahia.org/license
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the rights, obligations and limitations governing use of the contents
 * of the file. The Original and Upgraded Code is the Jahia CMS and Portal
 * Server. The developer of the Original and Upgraded Code is JAHIA Ltd. JAHIA
 * Ltd. owns the copyrights in the portions it created. All Rights Reserved.
 *
 * The Shared Modifications are Jahia View Helper.
 *
 * The Developer of the Shared Modifications is Jahia Solution S�rl.
 * Portions created by the Initial Developer are Copyright (C) 2002 by the
 * Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Mar 11 2003 Jahia Solutions S�rl: MAP Initial release.
 *
 * ----- END LICENSE BLOCK -----
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
