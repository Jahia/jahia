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

/**
 * User: jahia
 * Date: 4 mars 2008
 * Time: 15:40:51
 */
public class GWTJahiaToolbarItemsGroup implements Serializable {
    private String id;
    private String type;
    private String itemsGroupTitle;
    private String mediumIconStyle;
    private String minIconStyle;
    private boolean needSeparator;
    private int layout;
    private List<GWTJahiaToolbarItem> gwtToolbarItems;

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

    public String getItemsGroupTitle() {
        return itemsGroupTitle;
    }

    public void setItemsGroupTitle(String itemsGroupTitle) {
        this.itemsGroupTitle = itemsGroupTitle;
    }

    public int getLayout() {
        return layout;
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

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public boolean isNeedSeparator() {
        return needSeparator;
    }

    public void setNeedSeparator(boolean needSeparator) {
        this.needSeparator = needSeparator;
    }

    public List<GWTJahiaToolbarItem> getGwtToolbarItems() {
        return gwtToolbarItems;
    }

    public void setGwtToolbarItems(List<GWTJahiaToolbarItem> gwtToolbarItems) {
        this.gwtToolbarItems = gwtToolbarItems;
    }
}
