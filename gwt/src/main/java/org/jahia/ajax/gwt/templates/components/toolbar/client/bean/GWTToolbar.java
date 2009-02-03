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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.components.toolbar.client.bean;
import java.util.List;
import java.io.Serializable;

import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.Constants;

/**
 * User: jahia
 * Date: 4 mars 2008
 * Time: 15:39:36
 */
public class GWTToolbar implements Serializable {
    private int index;
    private String name;
    private String title;
    private String type;
    private boolean allowTabs;
    private boolean draggable;
    private boolean mandatory;
    private boolean displayTitle;
    private GWTState state = new GWTState();
    private List<GWTToolbarItemsGroup> gwtToolbarItemsGroups;

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

    public GWTState getState() {
        return state;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public void setState(GWTState state) {
        this.state = state;
    }

    public boolean isDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(boolean displayTitle) {
        this.displayTitle = displayTitle;
    }

    public List<GWTToolbarItemsGroup> getGwtToolbarItemsGroups() {
        return gwtToolbarItemsGroups;
    }

    public void setGwtToolbarItemsGroups(List<GWTToolbarItemsGroup> gwtToolbarItemsGroups) {
        this.gwtToolbarItemsGroups = gwtToolbarItemsGroups;
    }

    public boolean isFloatHorizontalState() {
        return getState().getValue() == Constants.TOOLBAR_HORIZONTAL_BOX;
    }

    public boolean isFloatVerticalState() {
        return getState().getValue() == Constants.TOOLBAR_VERTICAL_BOX;
    }

    public boolean isRight() {
        return getState().getValue() == Constants.TOOLBAR_RIGHT;
    }

    public boolean isTop() {
        return getState().getValue() == Constants.TOOLBAR_TOP;
    }

}
