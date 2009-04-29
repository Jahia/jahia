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
package org.jahia.services.toolbar.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jahia
 * Date: 7 avr. 2008
 * Time: 09:04:48
 */
public class Toolbar implements Serializable {
    private int index;
    private String name;
    private boolean settings;
    private String titleKey;
    private String type;
    private String state;
    private boolean draggable;
    private boolean mandatory;
    private boolean displayed;
    private boolean displayTitle;
    private List<ItemsGroup> itemsGroupList = new ArrayList<ItemsGroup>();
    private Visibility visibility;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public String getName() {
        return name;
    }

    public boolean isSettings() {
        return settings;
    }

    public void setSettings(boolean settings) {
        this.settings = settings;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    public boolean isDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(boolean displayTitle) {
        this.displayTitle = displayTitle;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    public List<ItemsGroup> getItemsGroupList() {
        return itemsGroupList;
    }


    public void addItemsGroup(ItemsGroup itemsGroup) {
        this.itemsGroupList.add(itemsGroup);
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
}
