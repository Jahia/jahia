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
package org.jahia.services.toolbar.bean;

import org.springframework.beans.factory.BeanNameAware;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * User: jahia
 * Date: 7 avr. 2008
 * Time: 09:05:11
 */
public class ItemsGroup implements Serializable, BeanNameAware {
    private String id;
    private String type;
    private String titleKey;
    private Visibility visibility;
    private String mediumIconStyle;
    private String minIconStyle;
    private String layout;
    private boolean separator;
    private List items = new ArrayList();

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

    public String getLayout() {
        return layout;
    }

    public boolean isSeparator() {
        return separator;
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public List getItems() {
        return items;
    }

    public void setItems(List items) {
        this.items = items;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void addItemsProvider(ItemsProvider itemsProvider) {
        items.add(itemsProvider);
    }

    public void setBeanName(String name) {
        setType(name);
    }

}
