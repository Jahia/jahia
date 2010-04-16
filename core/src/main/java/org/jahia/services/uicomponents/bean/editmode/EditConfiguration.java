package org.jahia.services.uicomponents.bean.editmode;

import org.jahia.services.uicomponents.bean.toolbar.Toolbar;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 14, 2010
 * Time: 12:26:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditConfiguration implements Serializable, BeanNameAware {
    private String name;
    
    private Toolbar topToolbar;
    private Toolbar contextMenu;

    private List<SidePanelTab> tabs;

    private List<Engine> createEngines;
    private List<Engine> editEngines;


    public void setBeanName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Toolbar getTopToolbar() {
        return topToolbar;
    }

    public void setTopToolbar(Toolbar topToolbar) {
        this.topToolbar = topToolbar;
    }

    public Toolbar getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(Toolbar contextMenu) {
        this.contextMenu = contextMenu;
    }

    public List<SidePanelTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<SidePanelTab> tabs) {
        this.tabs = tabs;
    }

    public List<Engine> getCreateEngines() {
        return createEngines;
    }

    public void setCreateEngines(List<Engine> createEngines) {
        this.createEngines = createEngines;
    }

    public List<Engine> getEditEngines() {
        return editEngines;
    }

    public void setEditEngines(List<Engine> editEngines) {
        this.editEngines = editEngines;
    }
}
