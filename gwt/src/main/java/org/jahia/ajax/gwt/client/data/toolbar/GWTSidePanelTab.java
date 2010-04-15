package org.jahia.ajax.gwt.client.data.toolbar;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 6, 2010
 * Time: 7:27:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTSidePanelTab  implements Serializable {
    private String name;
    private GWTJahiaToolbar treeContextMenu;
    private GWTJahiaToolbar tableContextMenu;
    private Map<String, String> params;
 
    public GWTSidePanelTab() {
    }


    public GWTSidePanelTab(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GWTJahiaToolbar getTreeContextMenu() {
        return treeContextMenu;
    }

    public void setTreeContextMenu(GWTJahiaToolbar treeContextMenu) {
        this.treeContextMenu = treeContextMenu;
    }

    public GWTJahiaToolbar getTableContextMenu() {
        return tableContextMenu;
    }

    public void setTableContextMenu(GWTJahiaToolbar tableContextMenu) {
        this.tableContextMenu = tableContextMenu;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
