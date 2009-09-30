package org.jahia.ajax.gwt.client.widget.toolbar;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarSet;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbar;
import org.jahia.ajax.gwt.client.widget.edit.Module;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;

import java.util.List;
import java.util.ArrayList;

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

    public  ActionToolbarLayoutContainer(String toolbarGroup) {
    	super();
    	this.toolbarGroup = toolbarGroup;
        setLayout(new RowLayout());
    }

    /**
     * Load toolbar
     */
    private void loadToolbars() {
        GWTJahiaPageContext gwtJahiaPageContext = getJahiaGWTPageContext();
        // load toolbars
        ToolbarService.App.getInstance().getGWTToolbars(toolbarGroup, gwtJahiaPageContext, new AsyncCallback<GWTJahiaToolbarSet>() {
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
        handleNewModuleSelection(null);
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

    /**
     * Handle module selection
     *
     * @param selectedModule
     */
    public void handleNewModuleSelection(Module selectedModule) {
        for (int i = 0; i < actionToolbars.size(); i++) {
            actionToolbars.get(i).handleNewModuleSelection(selectedModule);
        }
    }

    /**
     * Handle new side panel selection
     *
     * @param node
     */
    public void handleNewSidePanelSelection(GWTJahiaNode node) {
        for (int i = 0; i < actionToolbars.size(); i++) {
            actionToolbars.get(i).handleNewSidePanelSelection(node);
        }
    }

    /**
     * TO DO Refactor this method
     * @param isTreeSelection
     * @param isTableSelection
     * @param isWritable
     * @param isDeleteable
     * @param isParentWritable
     * @param isSingleFile
     * @param isSingleFolder
     * @param isPasteAllowed
     * @param isLockable
     * @param isLocked
     * @param isZip
     * @param isImage
     * @param isMount
     */
    public void enableOnConditions(boolean isTreeSelection, boolean isTableSelection, boolean isWritable, boolean isDeleteable, boolean isParentWritable, boolean isSingleFile, boolean isSingleFolder, boolean isPasteAllowed, boolean isLockable, boolean isLocked, boolean isZip, boolean isImage, boolean isMount) {
        for (ActionToolbar actionToolbar : actionToolbars) {
            actionToolbar.enableOnConditions(isTreeSelection, isTableSelection, isWritable, isDeleteable, isParentWritable, isSingleFile, isSingleFolder, isPasteAllowed, isLockable, isLocked, isZip, isImage, isMount);
        }
    }

    /**
     * Executed after the load of the toolbar
     */
    public void afterToolbarLoading()  {

    }

    public void initWithLinker(Linker linker) {
        this.linker = linker;
    }

    public void init() {
        loadToolbars();
    }
}
