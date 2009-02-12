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

package org.jahia.engines.shared;

import org.jahia.services.pages.JahiaPage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * For use in the Page_Field engine, to handle every page parameters without changing/creating
 * a real page until the user really want to save it.
 */
public class JahiaPageEngineTempBean implements Serializable {

    private int id;
    private int siteID;
    private int parentID;
    private int pageType;
    private Map<String, String> titles = new HashMap<String, String>();
    private int pageTemplateID;
    private Map<String, String> remoteURLs = new HashMap<String, String>();
    private int pageLinkID;
    private String creator;
    private int linkFieldID;
    private String operation = Page_Field.RESET_LINK;
    private boolean sharedTitle;
    private boolean deleteOldContainer = true;
    private boolean hideFromNavigationMenu = false;
    private String urlKey;

    public JahiaPageEngineTempBean(int id,
                                   int siteID,
                                   int parentID,
                                   int pageType,
                                   int pageTemplateID,
                                   int pageLinkID,
                                   String creator,
                                   int linkFieldID) {
        this.id = id;
        this.siteID = siteID;
        this.parentID = parentID;
        this.pageType = pageType;
        this.pageTemplateID = pageTemplateID;
        this.pageLinkID = pageLinkID;
        this.creator = creator;
        this.linkFieldID = linkFieldID;
    } // end constructor

    //------------------------------------------------------------------------
    public int getID() {
        return this.id;
    }

    /**
     * get the site ID
     *
     * @return Return the Jahia site ID.
     */
    public int getSiteID() {
        return siteID;
    }

    //------------------------------------------------------------------------
    public int getParentID() {
        return parentID;
    }

    //------------------------------------------------------------------------
    public int getPageType() {
        return pageType;
    }

    //------------------------------------------------------------------------
    public String getTitle(String languageCode) {
        return titles.get(languageCode);
    }

    //------------------------------------------------------------------------
    public int getPageTemplateID() {
        return pageTemplateID;
    }

    //------------------------------------------------------------------------
    public String getRemoteURL(String languageCode) {
        return remoteURLs.get(languageCode);
    }

    public Map<String, String> getRemoteURLs() {
        return remoteURLs;
    }

    //------------------------------------------------------------------------
    public int getPageLinkID() {
        return pageLinkID;
    }

    //------------------------------------------------------------------------
    public String getCreator() {
        return creator;
    }

    //------------------------------------------------------------------------
    public int getLinkFieldID() {
        return linkFieldID;
    }

    //------------------------------------------------------------------------
    public void setSiteID(int siteID) {
        this.siteID = siteID;
    }

    //------------------------------------------------------------------------
    public void setParentID(int parentID) {
        this.parentID = parentID;
    }

    //------------------------------------------------------------------------
    public void setPageType(int pageType) {
        this.pageType = pageType;
    }

    //------------------------------------------------------------------------
    public void setTitle(String languageCode, String title) {
        if (languageCode == null || title == null) {
            return;
        }
        this.titles.put(languageCode, title);
    }

    //------------------------------------------------------------------------
    public void removeTitle(String languageCode) {
        this.titles.remove(languageCode);
    }

    //------------------------------------------------------------------------
    public void setPageTemplateID(int pageTemplateID) {
        this.pageTemplateID = pageTemplateID;
    }

    //------------------------------------------------------------------------
    public void setRemoteURL(String languageCode, String remoteURL) {
        if (languageCode == null || remoteURL == null) {
            return;
        }
        this.remoteURLs.put(languageCode, remoteURL);
    }

    //------------------------------------------------------------------------
    public void removeRemoteUrl(String languageCode) {
        this.remoteURLs.remove(languageCode);
    }

    public void setRemoteURLs(Map<String, String> remoteURLs) {
        this.remoteURLs = remoteURLs;
    }

    //------------------------------------------------------------------------
    public void setPageLinkID(int pageLinkID) {
        this.pageLinkID = pageLinkID;
    }

    //------------------------------------------------------------------------
    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Map<String, String> getTitles() {
        return this.titles;
    }

    public void setTitles(Map<String, String> titles) {
        this.titles = titles;
    }

    public void setOperation(String _operation) {
        operation = _operation;
    }

    public String getOperation() {
        return operation;
    }

    public void sharedTitle(boolean _sharedTitle) {
        sharedTitle = _sharedTitle;
    }

    public boolean isSharedTitle() {
        return sharedTitle;
    }

    public boolean deleteOldContainer() {
        return this.deleteOldContainer;
    }

    public void setDeleteOldContainer(boolean value) {
        this.deleteOldContainer = value;
    }

    public String getUrlKey() {
        return urlKey;
    }

    public void setUrlKey(String urlKey) {
        this.urlKey = urlKey;
    }

    public boolean isHideFromNavigationMenu() {
        return hideFromNavigationMenu;
    }

    public void setHideFromNavigationMenu(boolean hideFromNavigationMenu) {
        this.hideFromNavigationMenu = hideFromNavigationMenu;
    }

    public String toString() {

        final StringBuffer pageAttribute =
                new StringBuffer("JahiaPageEngineTempBean detail :\n");
        pageAttribute.append("- siteID         : [").append(siteID).append("]\n");
        pageAttribute.append("- parentID       : [").append(parentID).append("]\n");
        String typeName = pageType == -1 ? "No type defined" : JahiaPage.PAGE_TYPE_NAMES[pageType];
        pageAttribute.append("- pageType       : [").append(typeName).append("]\n");
        pageAttribute.append("- pageTemplateID : [").append(pageTemplateID).append("]\n");
        pageAttribute.append("- remoteURL      : [").append(remoteURLs).append("]\n");
        pageAttribute.append("- pageLinkID     : [").append(pageLinkID).append("]\n");
        pageAttribute.append("- creator        : [").append(creator).append("]\n");
        pageAttribute.append("- linkFieldID    : [").append(linkFieldID).append("]\n");
        pageAttribute.append("- operation      : [").append(operation).append("]\n");
        pageAttribute.append("- urlKey         : [").append(urlKey).append("]\n");
        pageAttribute.append("- titles         : [");
        final Iterator<String> titlesEnum = titles.values().iterator();
        while (titlesEnum.hasNext()) {
            pageAttribute.append(titlesEnum.next());
            if (titlesEnum.hasNext()) {
                pageAttribute.append(", ");
            }
        }
        pageAttribute.append("]\n");
        return pageAttribute.toString();
    }

}
