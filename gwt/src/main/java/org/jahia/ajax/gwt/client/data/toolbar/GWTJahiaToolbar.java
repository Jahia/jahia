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
package org.jahia.ajax.gwt.client.data.toolbar;
import java.util.List;
import java.io.Serializable;

import org.jahia.ajax.gwt.client.util.ToolbarConstants;

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
    private boolean allowTabs;
    private boolean draggable;
    private boolean mandatory;
    private boolean displayTitle;
    private GWTJahiaState state = new GWTJahiaState();
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

    public boolean isAllowTabs() {
        return allowTabs;
    }

    public void setAllowTabs(boolean allowTabs) {
        this.allowTabs = allowTabs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    public GWTJahiaState getState() {
        return state;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public void setState(GWTJahiaState state) {
        this.state = state;
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

    public boolean isFloatHorizontalState() {
        return getState().getValue() == ToolbarConstants.TOOLBAR_HORIZONTAL_BOX;
    }

    public boolean isFloatVerticalState() {
        return getState().getValue() == ToolbarConstants.TOOLBAR_VERTICAL_BOX;
    }

    public boolean isRight() {
        return getState().getValue() == ToolbarConstants.TOOLBAR_RIGHT;
    }

    public boolean isTop() {
        return getState().getValue() == ToolbarConstants.TOOLBAR_TOP;
    }

}
