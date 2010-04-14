package org.jahia.services.edit.bean;

import org.jahia.services.toolbar.bean.Toolbar;
import org.jahia.services.toolbar.bean.Visibility;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;
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

    public SidePanelTab() {
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
}
