/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.uicomponents.bean.editmode;

import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.uicomponents.bean.Visibility;
import org.jahia.services.uicomponents.bean.contentmanager.Column;
import org.jahia.services.uicomponents.bean.toolbar.Toolbar;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a single tab in the edit mode side panel.
 * User: ktlili
 * Date: Apr 14, 2010
 * Time: 12:30:01 PM
 */
public class SidePanelTab implements Serializable, BeanNameAware, InitializingBean, DisposableBean {
    
    private static final long serialVersionUID = -4170052202882342097L;
    
    private String name;
    private String key;
    private Toolbar treeContextMenu;
    private Toolbar tableContextMenu;
    private Map<String, String> params;
    private Visibility visibility;
    private List<Column> treeColumns;
    private List<Column> tableColumns;
    private String icon;
    private SidePanelTabItem tabItem;
    private String requiredPermission;

    private Object parent;
    private int position = -1;
    private String positionAfter;
    private String positionBefore;

    public SidePanelTab() {
        super();
        tableColumns = new ArrayList<Column>();
        treeColumns = new ArrayList<Column>();
    }

    public SidePanelTab(String key) {
        this();
        setKey(key);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Toolbar getTreeContextMenu() {
        return treeContextMenu;
    }

    public void setTreeContextMenu(Toolbar treeContextMenu) {
        this.treeContextMenu = treeContextMenu;
    }

    public Toolbar getTableContextMenu() {
        return tableContextMenu;
    }

    public void setTableContextMenu(Toolbar tableContextMenu) {
        this.tableContextMenu = tableContextMenu;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void setBeanName(String name) {
        this.name = name;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public List<Column> getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(List<Column> tableColumns) {
        this.tableColumns = tableColumns;
    }

    public List<Column> getTreeColumns() {
        return treeColumns;
    }

    public void setTreeColumns(List<Column> treeColumns) {
        this.treeColumns = treeColumns;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public SidePanelTabItem getTabItem() {
        return tabItem;
    }

    public void setTabItem(SidePanelTabItem tabItem) {
        this.tabItem = tabItem;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }
    
    public void setPosition(int position) {
        this.position = position;
    }

    public void setPositionAfter(String positionAfter) {
        this.positionAfter = positionAfter;
    }

    public void setPositionBefore(String positionBefore) {
        this.positionBefore = positionBefore;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj != null && this.getClass() == obj.getClass()) {
            SidePanelTab other = (SidePanelTab) obj;
            return getKey() != null ? other.getKey() != null && getKey().equals(other.getKey())
                    : other.getKey() == null;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (parent != null) {
            if (parent instanceof List) {
                for (Object o : (List) parent) {
                    addTab(getSidePanelTabs(o));
                }
            } else {
                addTab(getSidePanelTabs(parent));
            }
        }
    }

    private void addTab(List<List<SidePanelTab>> tabs) {
        if (!tabs.isEmpty()) {
            removeTab(tabs, getKey());
            for (List<SidePanelTab> t : tabs) {
                int index = -1;
                if (position >= 0) {
                    index = position;
                } else if (positionBefore != null) {
                    index = t.indexOf(new SidePanelTab(positionBefore));
                } else if (positionAfter != null) {
                    index = t.indexOf(new SidePanelTab(positionAfter));
                    if (index != -1) {
                        index++;
                    }
                    if (index >= t.size()) {
                        index = -1;
                    }
                }
                if (index != -1) {
                    t.add(index, this);
                } else {
                    t.add(this);
                }
            }
        } else if (this.parent != null) {
            throw new IllegalArgumentException("Unknown parent type '"
                    + this.parent.getClass().getName() + "'. Can accept EditConfiguration or"
                    + " a String value with a beanId of the EditConfiguration bean");

        }
    }

    @Override
    public void destroy() throws Exception {
        if (!JahiaContextLoaderListener.isRunning()) {
            return;
        }
        if (parent instanceof List) {
            for (Object o : (List<?>) parent) {
                removeTab(getSidePanelTabs(o), getKey());
            }
        } else {
            removeTab(getSidePanelTabs(parent), getKey());
        }
    }

    private void removeTab(List<List<SidePanelTab>> tabs, String tabKey) {
        if (!tabs.isEmpty() && tabKey != null && !tabKey.isEmpty()) {
            for (List<SidePanelTab> t : tabs) {
                for (Iterator<SidePanelTab> iterator = t.iterator(); iterator.hasNext();) {
                    SidePanelTab tab = iterator.next();
                    if (tabKey.equals(tab.getKey())) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private List<List<SidePanelTab>> getSidePanelTabs(Object parent) {
        List<List<SidePanelTab>> results = new ArrayList<>();
        if (parent == null) {
            return results;
        }
        if (parent instanceof String) {
            parent = SpringContextSingleton.getBean((String) parent);
        }
        List<SidePanelTab> tabs = null;

        if (parent instanceof EditConfiguration) {
            tabs = ((EditConfiguration) parent).getTabs();
            if (tabs == null) {
                tabs = new LinkedList<>();
                ((EditConfiguration) parent).setTabs(tabs);
            }

            for (Map.Entry<String, ?> entry : SpringContextSingleton.getBeansOfType(EditConfiguration.class).entrySet()) {
                if (entry.getKey().startsWith(((EditConfiguration) parent).getName() + "-")) {
                    results.addAll(getSidePanelTabs(entry.getValue()));
                }
            }
        }
        results.add(tabs);
        return results;
    }
}