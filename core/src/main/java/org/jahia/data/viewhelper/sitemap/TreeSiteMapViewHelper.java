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
 * Sep 24 2002 Jahia Solutions S�rl: MAP Initial release.
 *
 * ----- END LICENSE BLOCK -----
 */

package org.jahia.data.viewhelper.sitemap;

import java.util.List;

import org.jahia.content.ContentObject;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;

/**
 * <p>The Jahia Shared Modification is: Jahia View Helper</p>
 *
 * <p>Description:
 * Create a flat tree from Jahia page tree structure destinated to display a
 * sorted Jahia site map.
 * </p>
 * <p>Copyright: MAP (Jahia Solutions S�rl 2002)</p>
 * <p>Company: Jahia Solutions Sàrl</p>
 * @author MAP
 * @version 1.0
 */
public class TreeSiteMapViewHelper extends SiteMapViewHelper {

    /**
     * Create a view helper on a entire Jahia site map restricted to the actual
     * logged user.
     *
     * @param user       The actual user logged in Jahia.
     * @param startPage  The start page for site map.
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     * @param languageCode Get the page with the specified language code.
     * @param defaultMaxLevel Site map expansion default level max.
     * the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     * ARCHIVED_PAGE_INFOS constants.
     * @param pagesFilter     Filter object for page filtering 
     */
    public TreeSiteMapViewHelper(JahiaUser user,
                                 ContentPage startPage,
                                 int pageInfosFlag,
                                 String languageCode,
                                 int defaultMaxLevel,
                                 PagesFilter pagesFilter,                                 
                                 boolean doLoad) {
        super(user, startPage, pageInfosFlag, languageCode, defaultMaxLevel, pagesFilter, doLoad);
        _jahiaPageSiteMap = super.getJahiaPageSiteMap();
    }

    /**
     * Create a view helper on a entire Jahia site map restricted to the actual
     * logged user.
     *
     * @param user       The actual user logged in Jahia.
     * @param startPage  The start page for site map.
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     * @param languageCode Get the page with the specified language code.
     * @param defaultMaxLevel Site map expansion default level max.
     * the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     * ARCHIVED_PAGE_INFOS constants.
     * @param pagesFilter     Filter object for page filtering  
     */
    public TreeSiteMapViewHelper(JahiaUser user,
                                 ContentPage startPage,
                                 int pageInfosFlag,
                                 String languageCode,
                                 int defaultMaxLevel,
                                 boolean directPageOnly,
                                 PagesFilter pagesFilter,                                 
                                 boolean doLoad) {
        super(user, startPage, pageInfosFlag, languageCode, defaultMaxLevel, directPageOnly, pagesFilter, doLoad);
        _jahiaPageSiteMap = super.getJahiaPageSiteMap();

    }

    /**
     * Create a view helper on a entire Jahia site map restricted to the actual
     * logged user.
     *
     * @param user       The actual user logged in Jahia.
     * @param startPages The list of start pages for site map.
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     * @param languageCode Get the page with the specified language code.
     * @param defaultMaxLevel Site map expansion default level max.
     * the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     * ARCHIVED_PAGE_INFOS constants.
     * @param pagesFilter     Filter object for page filtering 
     */    
    public TreeSiteMapViewHelper(JahiaUser user, List startPages,
            int pageInfosFlag, String languageCode, int defaultMaxLevel,
            boolean directPageOnly, PagesFilter pagesFilter, boolean doLoad) {
        super(user, startPages, pageInfosFlag, languageCode, defaultMaxLevel,
                directPageOnly, pagesFilter, doLoad);
        _jahiaPageSiteMap = super.getJahiaPageSiteMap();
    }    

    /**
     * Return the page pointed by the index. If the page is not displayable
     * a null pointer is returned.
     * WARNING ! A null result does NOT automatically mean that the page does
     * not exist !
     *
     * @param index The array index of the desired page.
     * @return      The JahiaPage, null is not displayable or problem finding it.
     */
    public ContentPage getContentPage(int index) {
        final PageSiteMap pageSiteMap = (PageSiteMap)_jahiaPageSiteMap.get(index);
        return pageSiteMap.isDisplayable() ? lookupContentPage(pageSiteMap.getPageID()) : null;
    }

    /**
     * Return the page pointed by the index. If the page is not displayable
     * a null pointer is returned.
     * WARNING ! A null result does NOT automatically mean that the page does
     * not exist !
     *
     * @param index The array index of the desired page.
     * @return      The JahiaPage, null is not displayable or problem finding it.
     */
    public ContentObject getContentObject(int index) {
        try {
            final PageSiteMap pageSiteMap = (PageSiteMap)_jahiaPageSiteMap.get(index);
            return pageSiteMap.isDisplayable() ? ContentObject.getContentObjectInstance(pageSiteMap.getObjectKey()) : null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Return a Jahia page corresponding ID
     *
     * @param index The array index of the desired page.
     * @return The ID of the page.
     */
    public int getPageID(int index) {
        return ((PageSiteMap)_jahiaPageSiteMap.get(index)).getPageID();
    }

    /**
     * Return the index of a given pageID, -1 if not found
     *
     * @param pageID
     */
    public int getPageIndex(int pageID){
        for ( int i=0 ; i<_jahiaPageSiteMap.size() ; i++ ){
            final PageSiteMap pageSiteMap = (PageSiteMap)_jahiaPageSiteMap.get(i);
            if ( pageSiteMap.getPageID() == pageID ){
                return i;
            }
        }
        return -1;
    }

    /**
     * Return the RELATIVE page level for the tree structure. Used for indent
     * display.
     * WARNING ! The RELATIVE page level is ABSOLUTE if the home page is given
     * in the constructor.
     *
     * @param index The array index of the desired page.
     * @return The page level starting from 0.
     */
    public int getPageLevel(int index) {
        return ((PageSiteMap)_jahiaPageSiteMap.get(index)).getPageLevel();
    }

    /**
     * Return if the page pointed by the index has childs or not.
     *
     * @param index The array index of the desired page.
     * @return True if childs, otherwise false.
     */
    public boolean hasChild(int index) {
        return ((PageSiteMap)_jahiaPageSiteMap.get(index)).hasChild();
    }

    /**
     * Get the parent page ID from the current indexed page.
     *
     * @param index The array index of the desired page.
     * @return The parent page ID
     */
    public int getParentPageID(int index) {
        return ((PageSiteMap)_jahiaPageSiteMap.get(index)).getParentPageID();
    }

    /**
     * Return if the page pointed by the index is the last page sister.
     *
     * @param index The array index of the desired page.
     * @return True if the page pointed by the index is the last page sister.
     */
    public boolean isLastSister(int index) {
        return ((PageSiteMap)_jahiaPageSiteMap.get(index)).isLastSister();
    }

    /**
     * Return the size of the site map.
     *
     * @return The List containing the page size.
     */
    public int size() {
        return _jahiaPageSiteMap.size();
    }

    /**
     * Return if the page pointed by the index is expanded or not.
     *
     * @param index The array index of the desired page.
     * @return True if expanded.
     */
    public boolean isExpanded(int index) {
        return ((PageSiteMap)_jahiaPageSiteMap.get(index)).isExpanded();
    }

    /**
     * Mark for collapse the sub tree structure.
     *
     * @param index The index of the site map array list.
     */
    public void collapsePagesSubTree(int index) {
        PageSiteMap pageSiteMap = (PageSiteMap)_jahiaPageSiteMap.get(index++);
        pageSiteMap.setExpanded(false);
        int pageLevel = pageSiteMap.getPageLevel();
        for (int i = index; i < _jahiaPageSiteMap.size(); i++) {
            pageSiteMap = (PageSiteMap)_jahiaPageSiteMap.get(i);
            if (pageSiteMap.getPageLevel() > pageLevel) {
                pageSiteMap.setDisplayable(false);
            } else {
                break;
            }
        }
    }

    /**
     * Set the page information page flag. The information page is divided in
     * three parts (Warning, Error, Event) informing the user if a page is
     * valid for activation or not, which events has occured, etc...
     *
     * @param index The index of the site map array list.
     */
    public void showInformation(int index) {
        ((PageSiteMap)_jahiaPageSiteMap.get(index)).setShowInformation(true);
    }

    public void hideInformation(int index) {
        ((PageSiteMap)_jahiaPageSiteMap.get(index)).setShowInformation(false);
    }

    /**
     * Return if the page information should be displayed or not.
     *
     * @param index The index of the site map array list.
     * @return True if the view should display the informations, false otherwise.
     */
    public boolean shouldShowInformation(int index) {
        return ((PageSiteMap)_jahiaPageSiteMap.get(index)).isShowInformation();
    }

    /**
     * Set the Show/Hide errors flag.
     *
     * @param index The index of the site map array list.
     */
    public void showWarnings(int index) {
        ((PageSiteMap)_jahiaPageSiteMap.get(index)).setShowWarnings(true);
    }

    public void hideWarnings(int index) {
        ((PageSiteMap)_jahiaPageSiteMap.get(index)).setShowWarnings(false);
    }

    /**
     * @param index The index of the site map array list.
     * @return True if the view should display the warnings, false otherwise.
     */
    public boolean shouldShowWarnings(int index) {
        return ((PageSiteMap)_jahiaPageSiteMap.get(index)).isShowWarnings();
    }

    /**
     * Set the Show/Hide errors flag.
     *
     * @param index The index of the site map array list.
     */
    public void showErrors(int index) {
        ((PageSiteMap)_jahiaPageSiteMap.get(index)).setShowErrors(true);
    }

    public void hideErrors(int index) {
        ((PageSiteMap)_jahiaPageSiteMap.get(index)).setShowErrors(false);
    }

    /**
     * @param index The index of the site map array list.
     * @return True if the view should display the errors, false otherwise.
     */
    public boolean shouldShowErrors(int index) {
        return ((PageSiteMap)_jahiaPageSiteMap.get(index)).isShowErrors();
    }

    /**
     * Set the Show/Hide events flag.
     *
     * @param index The index of the site map array list.
     */
    public void showEvents(int index) {
        ((PageSiteMap)_jahiaPageSiteMap.get(index)).setShowEvents(true);
    }

    public void hideEvents(int index) {
        ((PageSiteMap)_jahiaPageSiteMap.get(index)).setShowEvents(false);
    }

    /**
     * @param index The index of the site map array list.
     * @return True if the view should display the events, false otherwise.
     */
    public boolean shouldShowEvents(int index) {
        return ((PageSiteMap)_jahiaPageSiteMap.get(index)).isShowEvents();
    }

    /**
     * Expand from root to the given page.
     *
     * @param pageID, the pageID
     */
    public void expandToPage(int pageID) {
        final int pageIndex = getPageIndex(pageID);
        if ( pageIndex != -1 ){
            expandPagesSubTree(pageIndex);
            int parentID = this.getParentPageID(pageIndex);
            if ( parentID != 0 ){
                expandToPage(parentID);
            }
        }
    }

    /**
     * Mark for expansion the direct page childs from the subtree structure.
     *
     * @param index The index of the site map array list.
     */
    public void expandPagesSubTree(int index) {

        PageSiteMap pageSiteMap = (PageSiteMap)_jahiaPageSiteMap.get(index++);
        if (!pageSiteMap.hasChild()) return;
        pageSiteMap.setExpanded(true);
        int pageLevel = pageSiteMap.getPageLevel();
        int actualPageLevel = pageSiteMap.getPageLevel() + 1;
        for (int i = index; i < _jahiaPageSiteMap.size(); i++) {
            pageSiteMap = (PageSiteMap)_jahiaPageSiteMap.get(i);
            if (actualPageLevel == pageSiteMap.getPageLevel()) {
                pageSiteMap.setDisplayable(true);
                if (pageSiteMap.isExpanded()) {
                    actualPageLevel++;
                }
            } else if (actualPageLevel > pageSiteMap.getPageLevel()) {
                actualPageLevel = pageSiteMap.getPageLevel() + 1;
                pageSiteMap.setDisplayable(true);
            }
            if (pageLevel >= pageSiteMap.getPageLevel()) break;
        }
    }

    /**
     * Mark for expansion the entire sub tree structure.
     *
     * @param index The index of the site map array list.
     */
    public void expandAllPagesSubTree(int index) {
        PageSiteMap pageSiteMap = (PageSiteMap)_jahiaPageSiteMap.get(index++);
        pageSiteMap.setExpanded(true);
        int pageLevel = pageSiteMap.getPageLevel();
        for (int i = index; i < _jahiaPageSiteMap.size(); i++) {
            pageSiteMap = (PageSiteMap)_jahiaPageSiteMap.get(i);
            if (pageLevel >= pageSiteMap.getPageLevel()) break;
            pageSiteMap.setDisplayable(true);
            if (pageSiteMap.hasChild()) {
                pageSiteMap.setExpanded(true);
            }
        }
    }

    /**
     * Return the page title coresponding to a language code.
     * @param index The index of the site map array list.
     * @param languageCode The title language code desired.
     * @return A string correponsding to the page title.
     */
    public String getPageTitle(int index, String languageCode) {
        return ((PageSiteMap)_jahiaPageSiteMap.get(index)).getPageTitle(languageCode);
    }

    private List _jahiaPageSiteMap = null;
}
