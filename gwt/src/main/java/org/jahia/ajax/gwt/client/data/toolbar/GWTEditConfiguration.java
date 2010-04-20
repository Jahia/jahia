package org.jahia.ajax.gwt.client.data.toolbar;

import org.jahia.ajax.gwt.client.data.toolbar.GWTEngine;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 29, 2010
 * Time: 4:38:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTEditConfiguration extends GWTConfiguration implements Serializable {
    private String name;

    private GWTJahiaToolbar topToolbar;
    private GWTJahiaToolbar contextMenu;

    private List<GWTSidePanelTab> tabs;

    private List<GWTEngine> createEngines;
    private List<GWTEngine> editEngines;

    public GWTEditConfiguration() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GWTJahiaToolbar getTopToolbar() {
        return topToolbar;
    }

    public void setTopToolbar(GWTJahiaToolbar topToolbar) {
        this.topToolbar = topToolbar;
    }

    public GWTJahiaToolbar getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(GWTJahiaToolbar contextMenu) {
        this.contextMenu = contextMenu;
    }

    public List<GWTSidePanelTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<GWTSidePanelTab> tabs) {
        this.tabs = tabs;
    }

    public List<GWTEngine> getCreateEngines() {
        return createEngines;
    }

    public void setCreateEngines(List<GWTEngine> createEngines) {
        this.createEngines = createEngines;
    }

    public List<GWTEngine> getEditEngines() {
        return editEngines;
    }

    public void setEditEngines(List<GWTEngine> editEngines) {
        this.editEngines = editEngines;
    }
}
