package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.Events;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Event;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarSet;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.widget.toolbar.EditModeToolbar;
import org.jahia.ajax.gwt.client.widget.toolbar.edit.EditModeSelectionHandler;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Sep 4, 2009
 * Time: 4:17:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditModeToolbarContainer extends LayoutContainer {
    private List<EditModeToolbar> editModeToolbars = new ArrayList<EditModeToolbar>();
    private EditModeSelectionHandler editModeSelectionHandler = new EditModeSelectionHandler();
    private EditLinker editLinker;

    public EditModeToolbarContainer() {
        loadToolbars(false);
        setLayout(new RowLayout());
    }

    public void initWithLinker(EditLinker linker) {
        this.editLinker = linker;
    }

    /**
     * Load toolbar
     *
     * @param reset
     */
    public void loadToolbars(final boolean reset) {
        GWTJahiaPageContext gwtJahiaPageContext = getJahiaGWTPageContext();
        // load toolbars
        ToolbarService.App.getInstance().getGWTToolbars(gwtJahiaPageContext, reset, new AsyncCallback<GWTJahiaToolbarSet>() {
            public void onSuccess(GWTJahiaToolbarSet gwtJahiaToolbarSet) {
                long begin = System.currentTimeMillis();
                if (gwtJahiaToolbarSet != null) {
                    createToolbarUI(gwtJahiaToolbarSet);
                }
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
                    addEditModeToolbar(gwtToolbar);
                }
            }


            Log.debug("-- all tool bars added.");
        } else {
            Log.debug("There is no toolbar");
        }
    }

    /**
     * Add a toolbar widget
     *
     * @param gwtToolbar
     */
    public void addEditModeToolbar(GWTJahiaToolbar gwtToolbar) {
        EditModeToolbar editModeToolbar = new EditModeToolbar(gwtToolbar,editLinker,editModeSelectionHandler);
        if (gwtToolbar.getState().isDisplay()) {
            editModeToolbar.createToolBarUI();
            add(editModeToolbar);
        } else {
            editModeToolbar.setVisible(false);
        }
        editModeToolbars.add(editModeToolbar);

    }

    /**
     * Get the jahia page context
     *
     * @return
     */
    public GWTJahiaPageContext getJahiaGWTPageContext() {
        // init panel
        GWTJahiaPageContext page = new GWTJahiaPageContext(URL.getRelativeURL());
        page.setPid(JahiaGWTParameters.getPID());
        page.setMode(JahiaGWTParameters.getOperationMode());
        return page;
    }

    public EditLinker getEditLinker() {
        return editLinker;
    }

    public void setEditLinker(EditLinker editLinker) {
        this.editLinker = editLinker;
    }

    /**
     * Handle module selection
     *
     * @param selectedModule
     */
    public void handleNewModuleSelection(Module selectedModule) {

    }

    public void handleNewSidePanelSelection(GWTJahiaNode node) {

    }
}
