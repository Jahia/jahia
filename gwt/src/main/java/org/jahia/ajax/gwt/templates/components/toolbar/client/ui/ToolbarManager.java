/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.components.toolbar.client.ui;


import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.commons.client.beans.GWTJahiaPreference;
import org.jahia.ajax.gwt.commons.client.rpc.JahiaService;
import org.jahia.ajax.gwt.commons.client.util.ResourceBundle;
import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
import org.jahia.ajax.gwt.templates.commons.client.JahiaPageEntryPoint;
import org.jahia.ajax.gwt.templates.commons.client.module.JahiaType;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTState;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbar;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbarItemsGroup;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbars;
import org.jahia.ajax.gwt.templates.components.toolbar.client.service.ToolbarService;
import org.jahia.ajax.gwt.templates.components.toolbar.client.state.ToolbarStateManager;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.dnd.TargetToolbarsPanel;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.dnd.TargetVerticalPanel;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.dnd.ToolbarDragController;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.dnd.ToolbarDragHandler;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.dnd.ToolbarIndexedDropController;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.mygwt.HorizontalJahiaToolbar;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.mygwt.JahiaToolbar;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.mygwt.VerticalJahiaToolbar;

import com.allen_sauer.gwt.dnd.client.drop.AbsolutePositionDropController;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * User: jahia
 * Date: 4 mars 2008
 * Time: 15:27:13
 */
public class ToolbarManager {
    private RootPanel topPanel;

    // target toolbar panel
    private TargetVerticalPanel mainTopPanel;
    // private TargetAbsolutePanel mainCenterPanel;
    private TargetVerticalPanel mainRigthPanel;
    private TargetToolbarsPanel[] toolbarsPanels = new TargetToolbarsPanel[2];
    private boolean toolbarsPanelsLoaded = false;

    // drag controller
    private ToolbarDragController dragController;
    private ToolbarDragController verticalDragController;

    private GWTJahiaPageContext pageContext;

    // available toolbar
    private List<JahiaToolbar> jahiaToolbars = new ArrayList<JahiaToolbar>();

    // context menu that allow to display and hide toolbars
    private Menu toolbarContextMenu = new Menu();

    // hide all toolbars
    private AbsolutePanel displayToolbarPanel = new AbsolutePanel();
    private Button hideToolbarsButton;
    // toolbars displayed pref
    public static final String TOOLBARS_DISPLAYED_PREF = "toolbars_displayed";

    // number of displayed toolbar
    private int displayedToolbars = 0;

    public ToolbarManager() {
    }

    public ToolbarManager(RootPanel topPanel, GWTJahiaPageContext pageContext) {
        this.topPanel = topPanel;
        this.pageContext = pageContext;
    }



    /**
     * Init toolbars panels
     */
    private void initToolbarsPanels() {
        String height = "100%";
        int heightInt = RootPanel.get().getOffsetHeight();
        if (heightInt != 0) {
            height = heightInt + "px";
        }

        // create a dock panel
        DockPanel dockPanel = JahiaPageEntryPoint.getDockPanel();
        dockPanel.setWidth("100%");
        dockPanel.setHeight(height);

        // in drag and drop
        dragController = new ToolbarDragController(RootPanel.get(), true);
        dragController.addDragHandler(new ToolbarDragHandler(pageContext));

        verticalDragController = new ToolbarDragController(RootPanel.get(), true);
        verticalDragController.addDragHandler(new ToolbarDragHandler(pageContext));

        // init panels
        mainTopPanel = new TargetVerticalPanel(Constants.AREA_TOP);
        mainTopPanel.setWidth("100%");


        mainRigthPanel = new TargetVerticalPanel(Constants.AREA_RIGHT);
        mainRigthPanel.addStyleName("gwt-toolbar-Panel-right");

        displayToolbarPanel.removeFromParent();
        RootPanel.get().add(displayToolbarPanel, 0, 0);

        // create draggable, target area: north
        topPanel.add(mainTopPanel);


        // add draggable behavior
        dragController.registerDropController(new ToolbarIndexedDropController(mainTopPanel));
        dragController.registerDropController(new AbsolutePositionDropController(RootPanel.get()));
        verticalDragController.registerDropController(new ToolbarIndexedDropController(mainRigthPanel));

        // update toolbar panels flag
        toolbarsPanels[0] = mainTopPanel;
        toolbarsPanels[1] = mainRigthPanel;
        toolbarsPanelsLoaded = true;

    }

    /**
     * Increment displayed toolbar
     */
    public void incrementDiplayedToolbar() {
        displayedToolbars++;
    }

    /**
     * decrement displayed toolbar
     */
    public void decrementDisplayedToolbar() {
        displayedToolbars--;
    }

    /**
     * Load toolbars and create UI
     */
    public void createUI() {
        // create hideToolbar button
        long begin = System.currentTimeMillis();
        // load toolbars
        if (ToolbarStateManager.loadToolbar()) {
            Log.debug("load toolbar");

            loadToolbars(false);
            Log.debug("toolbar loaded");

        } else {
            createHideToolbarsButton();
        }
        long end2 = System.currentTimeMillis();
        Log.info("createUI() " + (end2 - begin) + "ms");
    }

    //load toolbars
    private void loadToolbars(final boolean reset) {
        // load toolbars
        ToolbarService.App.getInstance().getGWTToolbars(pageContext, reset, new AsyncCallback<GWTToolbars>() {
            public void onSuccess(GWTToolbars gwtToolbars) {
                long begin = System.currentTimeMillis();
                if (gwtToolbars != null) {
                    if (reset) {
                        clear();
                    }

                    createToolbarUI(gwtToolbars);
                } else {
                    Log.debug("There is no toolbar (get null value)");
                    if (hideToolbarsButton != null) {
                        hideToolbarsButton.setVisible(true);
                    } else {
                        createHideToolbarsButton();
                    }
                }
                long end = System.currentTimeMillis();
                Log.info("Toolbar loaded in " + (end - begin) + "ms");
            }

            public void onFailure(Throwable throwable) {
                Log.error("Unable to get toobar due to", throwable);
            }
        });
    }

    /**
     * clear all toolbars
     */
    public void clear() {
        for (final JahiaToolbar jahiaToolbar : getJahiaToolbars()) {
            jahiaToolbar.clearAndRemoveFromParent();
            jahiaToolbars = new ArrayList<JahiaToolbar>();
            displayedToolbars = 0;
            toolbarContextMenu.removeAll();
        }
    }


    /**
     * Create toolbar ui
     *
     * @param gwtToolbars
     */
    private void createToolbarUI(GWTToolbars gwtToolbars) {

        final List<GWTToolbar> toolbarList = gwtToolbars.getToolbarList();
        if (toolbarList != null && !toolbarList.isEmpty()) {
            // init the ui
            if (!toolbarsPanelsLoaded) {
                initToolbarsPanels();
            }
            Log.debug(toolbarList.size() + " toolbar(s).");
            for (int i = 0; i < toolbarList.size(); i++) {
                GWTToolbar gwtToolbar = toolbarList.get(i);
                List<GWTToolbarItemsGroup> toolbarItemsGroups = gwtToolbar.getGwtToolbarItemsGroups();
                if (toolbarItemsGroups != null && !toolbarItemsGroups.isEmpty()) {
                    addToolBarWidget(gwtToolbar);
                }
            }

            /**
             * Create common toolbar context menu
             */
            initContextMenu();


            Log.debug("-- all tool bars added.");
        } else {
            Log.debug("There is no toolbar");
            hideToolbarsButton.setVisible(true);
        }
    }

    /**
     * Create hide toolbar buttons
     */
    private void createHideToolbarsButton() {
        Log.debug("create hide toolbar");
        hideToolbarsButton = new Button("");
        hideToolbarsButton.setIconStyle("gwt-toolbar-ItemsGroup-icons-admin-min");
        hideToolbarsButton.setToolTip(getResource("display"));
        hideToolbarsButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                JahiaService.App.getInstance().saveJahiaPreference(new GWTJahiaPreference(TOOLBARS_DISPLAYED_PREF, "true"), createShowHideAsyncCall(true));
            }
        });
        displayToolbarPanel.add(hideToolbarsButton);
        DOM.setStyleAttribute(displayToolbarPanel.getElement(), "z-index", "999");
        RootPanel.get().add(displayToolbarPanel, 0, 0);
    }

    /**
     * Show Hide AsynCall back
     *
     * @param displayToolbars
     * @return
     */
    private AsyncCallback createShowHideAsyncCall(final boolean displayToolbars) {
        return new AsyncCallback() {
            public void onSuccess(Object o) {
                // hide button
                ToolbarStateManager.updateLoadToolbar(displayToolbars);
                if (hideToolbarsButton == null) {
                    createHideToolbarsButton();
                }
                hideToolbarsButton.setVisible(!displayToolbars);
                topPanel.setVisible(displayToolbars);
                if (toolbarsPanelsLoaded) {
                    for (final JahiaToolbar jahiaToolbar : getJahiaToolbars()) {
                        jahiaToolbar.setVisible(displayToolbars);
                    }
                }
                // toolbar no yet loaded
                else {
                    if (displayToolbars) {
                        loadToolbars(false);
                    }
                }
            }

            public void onFailure(Throwable throwable) {
                Log.error("Unable to update 'toolbars_displayed' preference due to", throwable);
            }
        };
    }

    /**
     * Create context menu
     */
    private void initContextMenu() {
        if (getJahiaToolbars() != null) {
            for (final JahiaToolbar jahiaToolbar : getJahiaToolbars()) {
                final CheckMenuItem item = new CheckMenuItem();
                item.setChecked(jahiaToolbar.getGwtToolbar().getState().isDisplay());
                item.setEnabled(!jahiaToolbar.getGwtToolbar().isMandatory());
                item.setText(jahiaToolbar.getGwtToolbar().getTitle());
                item.addSelectionListener(new SelectionListener<ComponentEvent>() {
                    public void componentSelected(ComponentEvent event) {
                        // check if there are more that 2 toolbar diplsay
                        Log.debug("Displayed toolabr: " + displayedToolbars);
                        if (!item.isChecked() && displayedToolbars == 1) {
                            MessageBox.alert(getResource("alert"), getResource("hide_alert"), null);
                            item.setChecked(true);
                        } else {
                            jahiaToolbar.setDisplay(item.isChecked());
                        }
                    }
                });
                toolbarContextMenu.add(item);
            }
            toolbarContextMenu.add(new SeparatorMenuItem());

            // hide all
            MenuItem hideAllMenu = new MenuItem(getResource("hide_all"));
            hideAllMenu.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    JahiaService.App.getInstance().saveJahiaPreference(new GWTJahiaPreference(TOOLBARS_DISPLAYED_PREF, "false"), createShowHideAsyncCall(false));
                }
            });
            toolbarContextMenu.add(hideAllMenu);

            // reset position
            MenuItem resetPosition = new MenuItem(getResource("reset"));
            resetPosition.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    loadToolbars(true);
                }
            });
            toolbarContextMenu.add(resetPosition);

        }
    }

    /**
     * Add a toolbar widget
     *
     * @param gwtToolbar
     */
    public void addToolBarWidget(GWTToolbar gwtToolbar) {
        JahiaToolbar jahiaToolbar = null;
        // box state
        if (gwtToolbar.isFloatHorizontalState()) {
            Log.debug("is horizontal box state");
            jahiaToolbar = new HorizontalJahiaToolbar(this, gwtToolbar);
            if (gwtToolbar.getState().isDisplay()) {
                jahiaToolbar.createToolBarUI();
                addFloatingToolbar(jahiaToolbar);
            } else {
                jahiaToolbar.setVisible(false);
            }
        } // horizontal state
        else if (gwtToolbar.isFloatVerticalState()) {
            Log.debug("is vertical box state");
            jahiaToolbar = new VerticalJahiaToolbar(this, gwtToolbar);
            if (gwtToolbar.getState().isDisplay()) {
                jahiaToolbar.createToolBarUI();
                addFloatingToolbar(jahiaToolbar);
            } else {
                jahiaToolbar.setVisible(false);
            }
        }
        // horizontal state
        else if (gwtToolbar.isTop()) {
            Log.debug("is top");
            jahiaToolbar = new HorizontalJahiaToolbar(this, gwtToolbar);
            if (gwtToolbar.getState().isDisplay()) {
                jahiaToolbar.createToolBarUI();
                mainTopPanel.addToolbar(jahiaToolbar);
            } else {
                jahiaToolbar.setVisible(mainTopPanel, false);

            }
        }
        // vertical state
        else if (gwtToolbar.isRight()) {
            Log.debug("is Right");
            jahiaToolbar = new VerticalJahiaToolbar(this, gwtToolbar);
            if (gwtToolbar.getState().isDisplay()) {
                jahiaToolbar.createToolBarUI();
                mainRigthPanel.addToolbar(jahiaToolbar);
            } else {
                jahiaToolbar.setVisible(mainRigthPanel, false);
            }
        }

        // refresh ui
        if (jahiaToolbar != null) {
            jahiaToolbar.setPageContext(pageContext);
            jahiaToolbars.add(jahiaToolbar);
            // handle drag option
            handleDraggable(jahiaToolbar);
            jahiaToolbar.setContextMenu(toolbarContextMenu);
            if (jahiaToolbar.isLoaded() && hideToolbarsButton != null) {
                hideToolbarsButton.setVisible(false);
            }
        }
    }

    public void addFloatingToolbar(JahiaToolbar jahiaToolbar) {
        GWTState state = jahiaToolbar.getGwtToolbar().getState();

        // get X position
        int pagePositionX = state.getPagePositionX();
       /* if(pagePositionX > Window.getClientHeight()){
            pagePositionX = Window.getClientHeight() - jahiaToolbar.getOffsetHeight();
        } */

        // get Y position
        int pagePositionY = state.getPagePositionY();
       /* if(pagePositionY  > Window.getClientWidth()){
            pagePositionY  = Window.getClientWidth() - jahiaToolbar.getOffsetWidth();
        }*/

        RootPanel.get().add(jahiaToolbar, pagePositionX, pagePositionY);
        jahiaToolbar.makeOnScrollFixed();
        if (GXT.isIE6) {
            jahiaToolbar.setAutoWidth(true);
        }
    }

    public void handleDraggable(JahiaToolbar jahiaToolbar) {
        if (jahiaToolbar != null) {
            GWTToolbar gwtToolbar = jahiaToolbar.getGwtToolbar();
            if (gwtToolbar.isDraggable() && jahiaToolbar != null && gwtToolbar.getState().isDisplay()) {
                Log.debug(gwtToolbar.getName() + ": is draggable");
                if (gwtToolbar.isRight() || gwtToolbar.isFloatVerticalState()) {
                    verticalDragController.makeDraggable(jahiaToolbar, jahiaToolbar.getDraggableArea());
                } else {
                    dragController.makeDraggable(jahiaToolbar, jahiaToolbar.getDraggableArea());
                }
            } else {
                Log.debug("is not draggable");
            }
        }
    }

    public List<JahiaToolbar> getJahiaToolbars() {
        return jahiaToolbars;
    }

    public GWTJahiaPageContext getPageContext() {
        return pageContext;
    }

    public void setPageContext(GWTJahiaPageContext pageContext) {
        this.pageContext = pageContext;
    }

    public String getResource(String key) {
        return ResourceBundle.getResource(JahiaType.TOOLBARS_MANAGER, JahiaType.TOOLBARS_MANAGER, key);
    }
}
