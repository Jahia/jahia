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
package org.jahia.ajax.gwt.client.data.toolbar;
import java.util.List;
import java.io.Serializable;

/**
 * User: jahia
 * Date: 4 mars 2008
 * Time: 15:39:36
 */
public class GWTJahiaToolbar implements Serializable {
    private int index;
    private String name;
    private String title;
    private String type;
    private boolean displayTitle;
    private boolean contextMenu;
    private List<GWTJahiaToolbarItemsGroup> gwtToolbarItemsGroups;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(boolean displayTitle) {
        this.displayTitle = displayTitle;
    }

    public List<GWTJahiaToolbarItemsGroup> getGwtToolbarItemsGroups() {
        return gwtToolbarItemsGroups;
    }

    public void setGwtToolbarItemsGroups(List<GWTJahiaToolbarItemsGroup> gwtToolbarItemsGroups) {
        this.gwtToolbarItemsGroups = gwtToolbarItemsGroups;
    }

    public boolean isContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(boolean contextMenu) {
        this.contextMenu = contextMenu;
    }
}
