package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import java.util.List;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionMenu;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.selection.AbstractStoreSelectionModel;

/**
 * Represents a single tab item in the side panel.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:21:40 PM
 */
public class SidePanelTabItem extends TabItem {

    protected EditLinker editLinker;
    protected GWTSidePanelTab config;

    public SidePanelTabItem(GWTSidePanelTab config) {
        super("&nbsp;");
        this.config = config;
        setIcon(ToolbarIconProvider.getInstance().getIcon(config.getIcon()));
        getHeader().setToolTip(config.getName());
    }

    public void initWithLinker(EditLinker linker) {
        this.editLinker = linker;
    }

    /**
     * Refreshes the content of this tab if applicable. Does nothing by default.
     * Should be overridden in subclasses to implement the refresh.
     * @param flag
     */
    public void refresh(int flag) {
        // do nothing by default
    }

    public void handleNewModuleSelection(Module selectedModule) {
        // do nothing by default
    }

    /**
     * Creates the context menu using specified Spring toolbar bean name.
     *
     * @param toolbarBean    the Spring bean ID to look for in the
     *                       <code>applicationcontext-toolbar-sidepanel.xml</code> file
     * @param selectionModel the tree selection model
     * @return the context menu using specified Spring toolbar bean name
     */
    protected final Menu createContextMenu(GWTJahiaToolbar toolbarBean, AbstractStoreSelectionModel<GWTJahiaNode> selectionModel) {
        if (toolbarBean != null) {
            final SidePanelLinker linker = new SidePanelLinker(selectionModel);
            return new ActionMenu(toolbarBean, linker);
        }
        return null;
    }

    class SidePanelLinker implements Linker {

        private AbstractStoreSelectionModel<GWTJahiaNode> selectionModel;

        private LinkerSelectionContext ctx;

        public SidePanelLinker(AbstractStoreSelectionModel<GWTJahiaNode> selectionModel) {
            super();
            this.selectionModel = selectionModel;
            ctx = new LinkerSelectionContext();
            select(null);
        }

        public GWTJahiaNode getMainNode() {
            return selectionModel.getSelectedItem();
        }

        public GWTJahiaNode getSelectedNode() {
            return selectionModel.getSelectedItem();
        }

        public List<GWTJahiaNode> getSelectedNodes() {
            return selectionModel.getSelectedItems();
        }

        public LinkerSelectionContext getSelectionContext() {
            return ctx;
        }

        public void loaded() {
            // do nothing
        }

        public void loading(String resource) {
            // do nothing
        }

        public void refresh(int flag) {
            editLinker.refresh(flag);
        }

        public void select(Object o) {
            syncSelectionContext();
        }

        public void syncSelectionContext() {
            ctx.setMainNode(selectionModel.getSelectedItem());
            ctx.setSelectedNodes(selectionModel.getSelectedItems());
            ctx.refresh();
        }

        public void setSelectPathAfterDataUpdate(String path) {
            // do nothing
        }

        public GWTConfiguration getConfig() {
            return editLinker.getConfig();
        }
    }




}
