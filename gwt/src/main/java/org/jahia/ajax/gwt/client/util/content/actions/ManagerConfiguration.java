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
package org.jahia.ajax.gwt.client.util.content.actions;

import java.util.List;
import java.util.ArrayList;

/**
 * User: rfelden
 * Date: 7 janv. 2009 - 11:24:10
 */
public class ManagerConfiguration {

    private List<ContentActionItemItf> items;
    private List<ContentActionItemGroup> groupedItems;

    private boolean enableTextMenu;

    private List<String> tableColumns;

    private List<String> accordionPanels;
    private List<String> tabs;
    private String selectedAccordion = null;
    private boolean hideLeftPanel = false;

    private String folderTypes;
    private String nodeTypes;
    private String filters;
    private String mimeTypes;

    private short defaultView;
    private boolean enableFileDoubleClick = true;
    private boolean displaySize = true;
    private boolean displayExt = true;
    private boolean displayLock = true;
    private boolean displayDate = true;
    private boolean displayProvider = false;
    
    private boolean allowConnections = true;

    public ManagerConfiguration() {
        items = new ArrayList<ContentActionItemItf>();
        groupedItems = new ArrayList<ContentActionItemGroup>();
        tableColumns = new ArrayList<String>();
        accordionPanels = new ArrayList<String>();
        tabs = new ArrayList<String>();
    }

    public void addItem(ContentActionItemItf item) {
        items.add(item);
    }

    public void addGroup(ContentActionItemGroup group) {
        groupedItems.add(group);
    }

    public void addColumn(String col) {
        tableColumns.add(col);
    }

    public void addAccordion(String accordion) {
        accordionPanels.add(accordion);
    }

    public void setSelectedAccordion(String accordion) {
        selectedAccordion = accordion;
    }

    public List<String> getTabs() {
        return tabs;
    }

    public void setTabs(List<String> tabs) {
        this.tabs = tabs;
    }

    public void addTab(String tab) {
        tabs.add(tab);
    }

    public void setHideLeftPanel(boolean hide) {
        this.hideLeftPanel = hide;
    }

    public List<ContentActionItemItf> getItems() {
        return items;
    }

    public List<ContentActionItemGroup> getGroupedItems() {
        return groupedItems;
    }

    public List<String> getTableColumns() {
        return tableColumns;
    }

    public List<String> getAccordionPanels() {
        return accordionPanels;
    }

    public String getSelectedAccordion() {
        return selectedAccordion;
    }

    public boolean isHideLeftPanel() {
        return hideLeftPanel;
    }

    public boolean isEnableTextMenu() {
        return enableTextMenu;
    }

    public void setEnableTextMenu(boolean enableTextMenu) {
        this.enableTextMenu = enableTextMenu;
    }

    public String getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(String folderTypes) {
        this.folderTypes = folderTypes;
    }

    public String getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(String nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public String getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(String mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public short getDefaultView() {
        return defaultView;
    }

    public void setDefaultView(short defaultView) {
        this.defaultView = defaultView;
    }

    public boolean isEnableFileDoubleClick() {
        return enableFileDoubleClick;
    }

    public void setEnableFileDoubleClick(boolean enableFileDoubleClick) {
        this.enableFileDoubleClick = enableFileDoubleClick;
    }

    public boolean isDisplaySize() {
        return displaySize;
    }

    public void setDisplaySize(boolean displaySize) {
        this.displaySize = displaySize;
    }

    public boolean isDisplayExt() {
        return displayExt;
    }

    public void setDisplayExt(boolean displayExt) {
        this.displayExt = displayExt;
    }

    public boolean isDisplayLock() {
        return displayLock;
    }

    public void setDisplayLock(boolean displayLock) {
        this.displayLock = displayLock;
    }

    public boolean isDisplayDate() {
        return displayDate;
    }

    public void setDisplayDate(boolean displayDate) {
        this.displayDate = displayDate;
    }

    public boolean isDisplayProvider() {
        return displayProvider;
    }

    public void setDisplayProvider(boolean displayProvider) {
        this.displayProvider = displayProvider;
    }

	public boolean isAllowConnections() {
    	return allowConnections;
    }

	public void setAllowConnections(boolean allowConnections) {
    	this.allowConnections = allowConnections;
    }
}
