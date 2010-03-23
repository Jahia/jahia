package org.jahia.ajax.gwt.client.widget.toolbar;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarSet;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.List;

/**
 * Action toolbar container widget.
 * User: ktlili
 * Date: Sep 4, 2009
 * Time: 4:17:57 PM
 */
public class ActionToolbarLayoutContainer extends LayoutContainer {
    private List<ActionToolbar> actionToolbars = new ArrayList<ActionToolbar>();
    private Linker linker;
    private String toolbarGroup;

    public ActionToolbarLayoutContainer(String toolbarGroup) {
        super();
        this.toolbarGroup = toolbarGroup;
        setLayout(new RowLayout());
    }

    /**
     * Load toolbar
     */
    private void loadToolbars() {
        // load toolbars
        ToolbarService.App.getInstance().getGWTToolbars(toolbarGroup, new AsyncCallback<GWTJahiaToolbarSet>() {
            public void onSuccess(GWTJahiaToolbarSet gwtJahiaToolbarSet) {
                long begin = System.currentTimeMillis();
                if (gwtJahiaToolbarSet != null) {
                    createToolbarUI(gwtJahiaToolbarSet);
                }
                afterToolbarLoading();
                long end = System.currentTimeMillis();
                layout();
                Log.info("Toolbar loaded in " + (end - begin) + "ms");
            }

            public void onFailure(Throwable throwable) {
                Log.error("Unable to get toobar due to", throwable);
            }
        });
    }

    /**
     * Create Toolbar UI
     *
     * @param gwtJahiaToolbarSet
     */
    private void createToolbarUI(GWTJahiaToolbarSet gwtJahiaToolbarSet) {
        final List<GWTJahiaToolbar> toolbarList = gwtJahiaToolbarSet.getToolbarList();
        if (toolbarList != null && !toolbarList.isEmpty()) {
            Log.debug(toolbarList.size() + " toolbar(s).");
            for (int i = 0; i < toolbarList.size(); i++) {
                GWTJahiaToolbar gwtToolbar = toolbarList.get(i);
                List<GWTJahiaToolbarItemsGroup> toolbarItemsGroups = gwtToolbar.getGwtToolbarItemsGroups();
                if (toolbarItemsGroups != null && !toolbarItemsGroups.isEmpty()) {
                    addActionToolbar(gwtToolbar);
                }
            }

            Log.debug("-- all tool bars added.");
        } else {
            Log.debug("There is no toolbar");
        }

        // no node is selected
        handleNewLinkerSelection();
    }

    /**
     * Add a toolbar widget
     *
     * @param gwtToolbar
     */
    public void addActionToolbar(GWTJahiaToolbar gwtToolbar) {
        ActionToolbar actionToolbar = new ActionToolbar(gwtToolbar, linker);
        actionToolbar.createToolBar();

        // add to widget
        add(actionToolbar);

        // add to toolbars list
        actionToolbars.add(actionToolbar);

    }

    public void insertItem(Component item, int index) {
        if (actionToolbars != null && !actionToolbars.isEmpty()) {
            actionToolbars.get(0).insert(item, index);
        }
    }

    /**
     * Handle module selection
     */
    public void handleNewLinkerSelection() {
        for (ActionToolbar actionToolbar : actionToolbars) {
            actionToolbar.handleNewLinkerSelection();
        }
    }

    /**
     * Executed after the load of the toolbar
     */
    public void afterToolbarLoading() {

    }

    public void initWithLinker(Linker linker) {
        this.linker = linker;
    }

    public void init() {
        loadToolbars();
    }
}
