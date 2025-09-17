/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.uicomponents.bean.toolbar;

import org.jahia.services.uicomponents.bean.Visibility;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: jahia
 * Date: 7 avr. 2008
 * Time: 09:04:48
 * @deprecated
 */
public class Toolbar implements Serializable, BeanNameAware {

    private static final long serialVersionUID = 5058259229049097109L;

    private String name;
    private String titleKey;
    private String type;
    private boolean displayTitle = false;
    private List<Item> items = new ArrayList<Item>();
    private Visibility visibility;
    private String layout;
    public Toolbar() {
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

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(boolean displayTitle) {
        this.displayTitle = displayTitle;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void addItem(Item item) {
        this.items.add(item);
    }

    public void addItem(int index, Item item) {
        this.items.add(index, item);
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public void setBeanName(String name) {
        setName(name);
        setType(name);
    }

    public void removeItem(String itemId) {
        if (itemId != null && itemId.length() > 0) {
            for (Iterator<Item> iterator = getItems().iterator(); iterator.hasNext();) {
                Item item = iterator.next();
                if (item.getId() != null && item.getId().equals(itemId)) {
                    iterator.remove();
                }
            }
        }
    }
}
