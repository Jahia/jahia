package org.jahia.ajax.gwt.client.widget.edit;

import org.jahia.ajax.gwt.client.widget.edit.GWTEngine;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.widget.edit.GWTSidePanelTab;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 29, 2010
 * Time: 4:38:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTEditConfiguration {
    private String name;

    private GWTJahiaToolbar topToolbar;
    private GWTJahiaToolbar contextMenu;

    private List<GWTSidePanelTab> tabs;
    private List<GWTEngine> engines;

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

    public List<GWTEngine> getEngines() {
        return engines;
    }

    public void setEngines(List<GWTEngine> engines) {
        this.engines = engines;
    }
}
