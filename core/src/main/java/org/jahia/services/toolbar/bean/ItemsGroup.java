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

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * User: jahia
 * Date: 7 avr. 2008
 * Time: 09:05:11
 */
public class ItemsGroup implements Serializable {
    private String id;
    private String type;
    private String titleKey;
    private Visibility visibility;
    private String mediumIconStyle;
    private String minIconStyle;
    private int layout;
    private boolean separator;
    private List itemList = new ArrayList();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getMediumIconStyle() {
        return mediumIconStyle;
    }

    public void setMediumIconStyle(String mediumIconStyle) {
        this.mediumIconStyle = mediumIconStyle;
    }

    public String getMinIconStyle() {
        return minIconStyle;
    }

    public void setMinIconStyle(String minIconStyle) {
        this.minIconStyle = minIconStyle;
    }

    public int getLayout() {
        return layout;
    }

    public boolean isSeparator() {
        return separator;
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public List getItemList() {
        return itemList;
    }

    public void addItem(Item item) {
        itemList.add(item);
    }

    public void addItemsProvider(ItemsProvider itemsProvider) {
        itemList.add(itemsProvider);
    }
}
