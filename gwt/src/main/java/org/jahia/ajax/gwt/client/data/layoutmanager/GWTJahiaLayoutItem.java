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
package org.jahia.ajax.gwt.client.data.layoutmanager;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.io.Serializable;

/**
 * User: jahia
 * Date: 20 mars 2008
 * Time: 11:19:43
 */
public class GWTJahiaLayoutItem implements Serializable {
    public static int MODE_VIEW = 0;
    public static int MODE_EDIT = 1;
    public static int MODE_HELP = 2;
    private String uuid;
    private int currentMode;
    private String viewModeLink;
    private String editModeLink;
    private String helpModeLink;
    private GWTJahiaNode gwtJahiaNode;

    // layout manager configuration
    private int column;
    private int row;
    private String status;

    public GWTJahiaLayoutItem() {
    }

    public GWTJahiaLayoutItem(String uuid, GWTJahiaNode gwtJahiaNode, String viewModeLink, String editModeLink, String helpModeLink, int column, int row, String status, int currentMode) {
        this.gwtJahiaNode = gwtJahiaNode;
        this.viewModeLink = viewModeLink;
        this.editModeLink = editModeLink;
        this.helpModeLink = helpModeLink;
        this.column = column;
        this.row = row;
        this.status = status;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public int getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(int currentMode) {
        this.currentMode = currentMode;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNode() {
        if (gwtJahiaNode != null) {
            return gwtJahiaNode.getUUID();
        }
        return "";
    }


    public String getViewModeLink() {
        return viewModeLink;
    }

    public void setViewModeLink(String viewModeLink) {
        this.viewModeLink = viewModeLink;
    }

    public String getEditModeLink() {
        return editModeLink;
    }

    public void setEditModeLink(String editModeLink) {
        this.editModeLink = editModeLink;
    }

    public String getHelpModeLink() {
        return helpModeLink;
    }

    public void setHelpModeLink(String helpModeLink) {
        this.helpModeLink = helpModeLink;
    }

    public String getEntryPointInstanceID() {
        if (gwtJahiaNode != null) {
            return gwtJahiaNode.getUUID();
        }
        return "";
    }

    public GWTJahiaNode getGwtJahiaNode() {
        return gwtJahiaNode;
    }

    public void setGwtJahiaNode(GWTJahiaNode gwtJahiaNode) {
        this.gwtJahiaNode = gwtJahiaNode;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean hasViewMode() {
        return this.viewModeLink != null;
    }

    public boolean hasEditMode() {
        return this.editModeLink != null;
    }

    public boolean hasHelpMode() {
        return this.helpModeLink != null;
    }

    public boolean isViewMode() {
        return currentMode == MODE_VIEW;
    }

    public boolean isEditMode() {
        return currentMode == MODE_EDIT;
    }

    public boolean isHelpMode() {
        return currentMode == MODE_HELP;
    }

    public boolean isPortletApplication() {
        return  gwtJahiaNode != null && gwtJahiaNode.isPortlet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !(o instanceof GWTJahiaLayoutItem)) {
            return false;
        }

        GWTJahiaLayoutItem that = (GWTJahiaLayoutItem) o;

        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) {
            return false;
        }

        return true;
    }

}
