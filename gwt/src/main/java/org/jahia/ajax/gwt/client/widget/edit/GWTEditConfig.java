package org.jahia.ajax.gwt.client.widget.edit;

import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 29, 2010
 * Time: 4:38:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTEditConfig {
    private String name;

    private GWTJahiaToolbar topToolbar;
    private GWTJahiaToolbar contextMenu;

    public GWTEditConfig() {
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
}
