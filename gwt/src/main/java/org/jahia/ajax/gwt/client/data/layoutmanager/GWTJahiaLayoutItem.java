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

    public String getPortlet() {
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
