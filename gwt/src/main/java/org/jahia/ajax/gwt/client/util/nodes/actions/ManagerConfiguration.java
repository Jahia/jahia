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
package org.jahia.ajax.gwt.client.util.nodes.actions;

import java.util.List;
import java.util.ArrayList;

/**
 * User: rfelden
 * Date: 7 janv. 2009 - 11:24:10
 */
public class ManagerConfiguration {

    private List<FileActionItemItf> items;
    private List<FileActionItemGroup> groupedItems;

    private boolean enableTextMenu;

    private List<String> tableColumns;

    private List<String> accordionPanels;
    private List<String> tabs;
    private String selectedAccordion = null;
    private boolean hideLeftPanel = false;

    private String nodeTypes;
    private String filters;
    private String mimeTypes;

    private short defaultView;
    private boolean enableFileDoubleClick = true;
    private boolean displaySize = true;
    private boolean displayExt = true;
    private boolean displayLock = true;
    private boolean displayDate = true;

    public ManagerConfiguration() {
        items = new ArrayList<FileActionItemItf>();
        groupedItems = new ArrayList<FileActionItemGroup>();
        tableColumns = new ArrayList<String>();
        accordionPanels = new ArrayList<String>();
        tabs = new ArrayList<String>();
    }

    public void addItem(FileActionItemItf item) {
        items.add(item);
    }

    public void addGroup(FileActionItemGroup group) {
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

    public List<FileActionItemItf> getItems() {
        return items;
    }

    public List<FileActionItemGroup> getGroupedItems() {
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
}
