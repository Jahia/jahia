package org.jahia.ajax.gwt.client.widget.edit;

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarSet;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.selection.AbstractStoreSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

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

        public void refreshLeftPanel() {
            editLinker.refreshLeftPanel();
        }

        public void refreshMainComponent() {
            editLinker.refreshMainComponent();
            editLinker.refreshLeftPanel();
        }

        public void select(Object o) {
            ctx.setMainNode(selectionModel.getSelectedItem());
            ctx.setSelectedNodes(selectionModel.getSelectedItems());
            ctx.refresh();
        }

        public void setSelectPathAfterDataUpdate(String path) {
            // do nothing
        }
    }
    
    class SidePanelMenu extends Menu {
        private List<ActionItem> actionItems = new ArrayList<ActionItem>();

        public SidePanelMenu(final String toolbarBean, final AbstractStoreSelectionModel<GWTJahiaNode> selectionModel) {
            super();

            final SidePanelLinker linker = new SidePanelLinker(selectionModel);
            
            ToolbarService.App.getInstance().getGWTToolbars(toolbarBean, JahiaGWTParameters.getGWTJahiaPageContext(),
                    new AsyncCallback<GWTJahiaToolbarSet>() {
                        public void onSuccess(GWTJahiaToolbarSet gwtJahiaToolbarSet) {
                            if (gwtJahiaToolbarSet != null) {
                                createMenu(gwtJahiaToolbarSet, linker);
                            }
                            layout();
                        }

                        public void onFailure(Throwable throwable) {
                            Log.error("Unable to get toolbar bean '" + toolbarBean + "'", throwable);
                        }
                    });

            // add listener on BedoreShow Event
            addListener(Events.BeforeShow, new Listener<MenuEvent>() {
                public void handleEvent(MenuEvent baseEvent) {
                    linker.select(null);
                    checkLinkerSelection();
                }
            });
        }

        private void createMenu(final GWTJahiaToolbarSet gwtJahiaToolbarSet, final SidePanelLinker linker) {
            // add all items found in the defined menus
            for (GWTJahiaToolbar gwtJahiaToolbar : gwtJahiaToolbarSet.getToolbarList()) {
                if (gwtJahiaToolbar.isContextMenu()) {
                    for (int i = 0; i < gwtJahiaToolbar.getGwtToolbarItemsGroups().size(); i++) {
                        GWTJahiaToolbarItemsGroup itemsGroup = gwtJahiaToolbar.getGwtToolbarItemsGroups().get(i);
                        if (i > 0 && i <= gwtJahiaToolbar.getGwtToolbarItemsGroups().size()
                                && itemsGroup.getGwtToolbarItems().isEmpty()) {
                            add(new SeparatorMenuItem());
                        }
                        for (GWTJahiaToolbarItem gwtJahiaToolbarItem : itemsGroup.getGwtToolbarItems()) {
                            ActionItem actionItem = gwtJahiaToolbarItem.getActionItem();
                            if (actionItem != null) {
                                actionItem.init(gwtJahiaToolbarItem, linker);
                                actionItems.add(actionItem);
                                add(actionItem.getContextMenuItem());
                            }
                        }
                    }
                }
            }
        }

        private void checkLinkerSelection() {
            for (ActionItem item : actionItems) {
                item.handleNewLinkerSelection();
            }
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
     * @param toolbarBean the Spring bean ID to look for in the
     *            <code>applicationcontext-toolbar-sidepanel.xml</code> file
     * @param selectionModel the tree selection model
     * @return the context menu using specified Spring toolbar bean name
     */
    protected final Menu createContextMenu(String toolbarBean, AbstractStoreSelectionModel<GWTJahiaNode> selectionModel) {
        return new SidePanelMenu(toolbarBean, selectionModel);
    }

}
