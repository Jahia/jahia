/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.layoutmanager.portlet;

import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.widget.layoutmanager.listener.OnPortletRemoved;
import org.jahia.ajax.gwt.client.widget.layoutmanager.listener.OnPortletStatusChanged;
import org.jahia.ajax.gwt.client.widget.layoutmanager.JahiaPortalManager;
import org.jahia.ajax.gwt.client.util.layoutmanager.JahiaPropertyHelper;
import org.jahia.ajax.gwt.client.util.URL;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.custom.Portlet;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Window;


/**
 * User: ktlili
 * Date: 28 ao�t 2008
 * Time: 17:11:29
 */
public class JahiaPortlet extends Portlet {
    private GWTJahiaLayoutItem porletConfig = new GWTJahiaLayoutItem();
    private LayoutContainer column;
    protected Widget viewContainer = null;
    protected Widget editContainer = null;
    protected Widget helpContainer = null;

    private ToolButton viewButton = new ToolButton("x-tool-portlet-view");
    private ToolButton editButton = new ToolButton("x-tool-portlet-edit");
    private ToolButton helpButton = new ToolButton("x-tool-portlet-help");
    private ToolButton maxButton = new ToolButton("x-tool-portlet-maximize");
    private ToolButton minButton = new ToolButton("x-tool-portlet-minimize");
    private ToolButton removeButton = new ToolButton("x-tool-portlet-close");

    public JahiaPortlet() {
        configPanel();
    }

    @Override
    protected void afterRender() {
        super.afterRender();
        getCollapseBtn().changeStyle("x-tool-portlet-toggle");
    }

    public JahiaPortlet(GWTJahiaLayoutItem porletConfig) {
        this.porletConfig = porletConfig;
        configPanel();
    }

    public JahiaPortlet(Layout layout) {
        super(layout);
        configPanel();
    }

    public JahiaPortlet(Layout layout, GWTJahiaLayoutItem porletConfig) {
        super(layout);
        this.porletConfig = porletConfig;
        configPanel();
    }

    public void refreshStatus() {
        // no yet implemented
        if (JahiaPropertyHelper.isStatusFullScreen(getStatus())) {
            maxButton.setVisible(false);
            minButton.setVisible(true);
            expand();
            JahiaPortalManager.getInstance().switchToFullScreenView(this);
        }
    }

    public void doView() {
        if (porletConfig.hasViewMode()) {
            Window.Location.replace(porletConfig.getViewModeLink());
        }
    }

    public void doEdit() {
        if (porletConfig.hasEditMode()) {
            Window.Location.replace(porletConfig.getEditModeLink());
        }
    }

    public void doHelp() {
        if (porletConfig.hasHelpMode()) {
            Window.Location.replace(porletConfig.getHelpModeLink());
        }
    }

    protected boolean isViewMode() {
        return porletConfig.isViewMode();
    }

    protected boolean isEditMode() {
        return porletConfig.isEditMode();
    }

    protected boolean isHelpMode() {
        return porletConfig.isHelpMode();
    }


    /**
     * Add hide button depends on the tpye of the portlet
     */
    private void configPanel() {
        setStyleAttribute("padding", "5px 0px 0px 5px");
        setCollapsible(true);
        setAnimCollapse(false);

        if (porletConfig.hasViewMode()) {
            viewButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
                @Override
                public void componentSelected(ComponentEvent ce) {
                    doView();
                }

            });
            getHeader().addTool(viewButton);
        }

        // change mode
        if (porletConfig.hasEditMode()) {
            editButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
                @Override
                public void componentSelected(ComponentEvent ce) {
                    doEdit();
                }

            });
            getHeader().addTool(editButton);
        }

        // help mode
        if (porletConfig.hasHelpMode()) {
            helpButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
                @Override
                public void componentSelected(ComponentEvent ce) {
                    doHelp();
                }

            });
            getHeader().addTool(helpButton);
        }


        maxButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            @Override
            public void componentSelected(ComponentEvent ce) {
                OnPortletStatusChanged onPortletStatusChanged = new OnPortletStatusChanged(JahiaPortlet.this, JahiaPropertyHelper.getStatusFullscreenValue());
                onPortletStatusChanged.handleEvent(ce);
            }

        });
        getHeader().addTool(maxButton);
        // minimizebutton
        minButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            @Override
            public void componentSelected(ComponentEvent ce) {
                OnPortletStatusChanged onPortletStatusChanged = new OnPortletStatusChanged(JahiaPortlet.this, JahiaPropertyHelper.getStatusNormaleValue());
                onPortletStatusChanged.handleEvent(ce);
            }

        });
        getHeader().addTool(minButton);

        // close portlet
        removeButton.addSelectionListener(new OnPortletRemoved(this));
        getHeader().addTool(removeButton);


        if (JahiaPropertyHelper.isStatusFullScreen(getStatus())) {
            maxButton.setVisible(false);
            minButton.setVisible(true);
        } else if (JahiaPropertyHelper.isStatusMinimized(getStatus())) {
            collapse();
            maxButton.setVisible(true);
            minButton.setVisible(false);
        } else {
            expand();
            maxButton.setVisible(true);
            minButton.setVisible(false);
        }

        addListener(Events.Expand, new OnPortletStatusChanged(this, JahiaPropertyHelper.getStatusNormaleValue()));
        addListener(Events.Collapse, new OnPortletStatusChanged(this, JahiaPropertyHelper.getStatusMinimizedValue()));
        layout();

    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        int toolCount = getHeader().getToolCount();
        for (int i = 0; i < toolCount; i++) {
            getHeader().getTool(i).setVisible(b);
        }
        setPinned(b);
    }

    public LayoutContainer getColumn() {
        return column;
    }

    public void setColumn(LayoutContainer column) {
        this.column = column;
    }

    public GWTJahiaLayoutItem getPorletConfig() {
        return porletConfig;
    }

    public void setPorletConfig(GWTJahiaLayoutItem porletConfig) {
        this.porletConfig = porletConfig;
    }

    public int getColumnIndex() {
        return porletConfig.getColumn();
    }

    public String getStatus() {
        return this.porletConfig.getStatus();
    }

    public void setColumnIndex(int columnIndex) {
        porletConfig.setColumn(columnIndex);
    }

    public int getRowIndex() {
        return porletConfig.getRow();
    }

    public void setRowIndex(int rowIndex) {
        porletConfig.setRow(rowIndex);
    }

    public void setStatus(String status) {
        porletConfig.setStatus(status);
    }

    public String getEntryPointInstanceId() {
        return porletConfig.getEntryPointInstanceID();
    }

    public String getWindowId() {
        return porletConfig.getPortlet();
    }

    public Widget getViewContainer() {
        return viewContainer;
    }

    public void setViewContainer(Widget viewContainer) {
        this.viewContainer = viewContainer;
    }

    public Widget getEditContainer() {
        return editContainer;
    }

    public void setEditContainer(Widget editContainer) {
        this.editContainer = editContainer;
    }

    public Widget getHelpContainer() {
        return helpContainer;
    }

    public void setHelpContainer(Widget helpContainer) {
        this.helpContainer = helpContainer;
    }

    public boolean isFullScreen() {
        return JahiaPropertyHelper.isStatusFullScreen(getStatus());
    }

}
