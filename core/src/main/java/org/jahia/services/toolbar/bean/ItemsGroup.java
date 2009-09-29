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
import org.jahia.services.toolbar.resolver.ItemsResolver;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * User: jahia
 * Date: 7 avr. 2008
 * Time: 09:05:11
 */
public class ItemsGroup extends Item implements Serializable, BeanNameAware {
    private String layout;
    private boolean separator;
    private List<Item> items = new ArrayList<Item>();
    private ItemsResolver itemsResolver;

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public boolean isSeparator() {
        return separator;
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

    public ItemsResolver getItemsResolver() {
        return itemsResolver;
    }

    public void setItemsResolver(ItemsResolver itemsResolver) {
        this.itemsResolver = itemsResolver;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void addItem(Item item){
        if(items == null){
          items = new ArrayList<Item>();
        }
        items.add(item);
    }

    public List<Item> getRealItems(org.jahia.data.JahiaData jData) {
        if (itemsResolver != null) {
            return itemsResolver.getItems(jData);
        }
        return items;
    }
}
