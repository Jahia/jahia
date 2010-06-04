package org.jahia.services.uicomponents.bean.editmode;

import org.jahia.services.uicomponents.bean.Visibility;
import org.jahia.services.uicomponents.bean.contentmanager.Column;
import org.jahia.services.uicomponents.bean.toolbar.Toolbar;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 14, 2010
 * Time: 12:30:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class SidePanelTab implements Serializable, BeanNameAware {
    private String name;
    private String key;
    private Toolbar treeContextMenu;
    private Toolbar tableContextMenu;
    private Map<String, String> params;
    private Visibility visibility;
    private List<String> paths = new ArrayList<String>();
    private List<String> folderTypes = new ArrayList<String>();
    private List<String> nodeTypes = new ArrayList<String>();
    private List<String> filters = new ArrayList<String>();
    private List<String> mimeTypes = new ArrayList<String>();
    private List<Column> treeColumns;
    private List<Column> tableColumns;

    public SidePanelTab() {
        tableColumns = new ArrayList<Column>();
        treeColumns = new ArrayList<Column>();
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

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public List<String> getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(List<String> folderTypes) {
        this.folderTypes = folderTypes;
    }

    public List<String> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(List<String> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(List<String> mimeTypes) {
        this.mimeTypes = mimeTypes;
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

}

