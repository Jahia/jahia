package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import java.util.List;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
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

        public void refresh() {
            editLinker.refresh();
            editLinker.refreshLeftPanel();
        }

        public void refresh(int flag) {
            refresh();
        }

        public void refreshLeftPanel() {
            editLinker.refreshLeftPanel();
        }

        public void refreshLeftPanel(int flag) {
            refreshLeftPanel();
        }

        public void refreshMainComponent() {
            editLinker.refreshMainComponent();
            editLinker.refreshLeftPanel();
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
    }

    protected EditLinker editLinker;

    public SidePanelTabItem() {
        super("&nbsp;");
    }

    public SidePanelTabItem(String text) {
        super(text);
    }

    public void initWithLinker(EditLinker linker) {
        this.editLinker = linker;
    }

    /**
     * Refreshes the content of this tab if applicable. Does nothing by default.
     * Should be overridden in subclasses to implement the refresh.
     */
    public void refresh() {
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
    protected final Menu createContextMenu(String toolbarBean, AbstractStoreSelectionModel<GWTJahiaNode> selectionModel) {
        final SidePanelLinker linker = new SidePanelLinker(selectionModel);
        return new ActionMenu(toolbarBean, linker);
    }

}
