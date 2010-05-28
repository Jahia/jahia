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

    private GWTJahiaToolbar topToolbar;
    private GWTJahiaToolbar contextMenu;

    private List<GWTSidePanelTab> tabs;

    public GWTEditConfiguration() {
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

}
