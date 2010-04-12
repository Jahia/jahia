package org.jahia.ajax.gwt.client.widget.edit;

import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 6, 2010
 * Time: 7:27:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTSidePanelBrowseTab extends GWTSidePanelTab {
    private GWTJahiaToolbar tableContextMenu;
    private String repositoryType;
    private String folderTypes;

    public GWTSidePanelBrowseTab(String name) {
        super(name);
    }

    public GWTJahiaToolbar getTableContextMenu() {
        return tableContextMenu;
    }

    public void setTableContextMenu(GWTJahiaToolbar tableContextMenu) {
        this.tableContextMenu = tableContextMenu;
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
    }

    public String getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(String folderTypes) {
        this.folderTypes = folderTypes;
    }
}