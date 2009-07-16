/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.layoutmanager.portlet;

import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.widget.layoutmanager.listener.OnPortletRemoved;
import org.jahia.ajax.gwt.client.widget.layoutmanager.listener.OnPortletStatusChanged;
import org.jahia.ajax.gwt.client.widget.layoutmanager.JahiaPortalManager;
import org.jahia.ajax.gwt.client.util.layoutmanager.JahiaPropertyHelper;

import com.extjs.gxt.ui.client.event.*;
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
    private GWTJahiaLayoutItem gwtJahiaLayoutItem = new GWTJahiaLayoutItem();
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

    public JahiaPortlet(GWTJahiaLayoutItem gwtJahiaLayoutItem) {
        this.gwtJahiaLayoutItem = gwtJahiaLayoutItem;
        configPanel();
    }

    public JahiaPortlet(Layout layout) {
        super(layout);
        configPanel();
    }

    public JahiaPortlet(Layout layout, GWTJahiaLayoutItem gwtJahiaLayoutItem) {
        super(layout);
        this.gwtJahiaLayoutItem = gwtJahiaLayoutItem;
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
        if (gwtJahiaLayoutItem.hasViewMode()) {
            Window.Location.replace(gwtJahiaLayoutItem.getViewModeLink());
        }
    }

    public void doEdit() {
        if (gwtJahiaLayoutItem.hasEditMode()) {
            Window.Location.replace(gwtJahiaLayoutItem.getEditModeLink());
        }
    }

    public void doHelp() {
        if (gwtJahiaLayoutItem.hasHelpMode()) {
            Window.Location.replace(gwtJahiaLayoutItem.getHelpModeLink());
        }
    }

    protected boolean isViewMode() {
        return gwtJahiaLayoutItem.isViewMode();
    }

    protected boolean isEditMode() {
        return gwtJahiaLayoutItem.isEditMode();
    }

    protected boolean isHelpMode() {
        return gwtJahiaLayoutItem.isHelpMode();
    }

    protected boolean isPortletApplicatin() {
        return gwtJahiaLayoutItem.isPortletApplication();
    }


    /**
     * Add hide button depends on the tpye of the portlet
     */
    private void configPanel() {
        setStyleAttribute("padding", "5px 0px 0px 5px");
        setCollapsible(true);
        setAnimCollapse(false);

        if (gwtJahiaLayoutItem.hasViewMode()) {
            viewButton.addSelectionListener(new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent ce) {
                    doView();
                }

            });
            getHeader().addTool(viewButton);
        }

        // change mode
        if (gwtJahiaLayoutItem.hasEditMode()) {
            editButton.addSelectionListener(new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent ce) {
                    doEdit();
                }

            });
            getHeader().addTool(editButton);
        }

        // help mode
        if (gwtJahiaLayoutItem.hasHelpMode()) {
            helpButton.addSelectionListener(new SelectionListener<IconButtonEvent>() {
                @Override
                public void componentSelected(IconButtonEvent ce) {
                    doHelp();
                }

            });
            getHeader().addTool(helpButton);
        }


        maxButton.addSelectionListener(new SelectionListener<IconButtonEvent>() {
            @Override
            public void componentSelected(IconButtonEvent ce) {
                OnPortletStatusChanged onPortletStatusChanged = new OnPortletStatusChanged(JahiaPortlet.this, JahiaPropertyHelper.getStatusFullscreenValue());
                onPortletStatusChanged.handleEvent(ce);
            }

        });
        getHeader().addTool(maxButton);
        // minimizebutton
        minButton.addSelectionListener(new SelectionListener<IconButtonEvent>() {
            @Override
            public void componentSelected(IconButtonEvent ce) {
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

    public GWTJahiaLayoutItem getGwtJahiaLayoutItem() {
        return gwtJahiaLayoutItem;
    }

    public void setGwtJahiaLayoutItem(GWTJahiaLayoutItem gwtJahiaLayoutItem) {
        this.gwtJahiaLayoutItem = gwtJahiaLayoutItem;
    }

    public int getColumnIndex() {
        return gwtJahiaLayoutItem.getColumn();
    }

    public String getStatus() {
        return this.gwtJahiaLayoutItem.getStatus();
    }

    public void setColumnIndex(int columnIndex) {
        gwtJahiaLayoutItem.setColumn(columnIndex);
    }

    public int getRowIndex() {
        return gwtJahiaLayoutItem.getRow();
    }

    public void setRowIndex(int rowIndex) {
        gwtJahiaLayoutItem.setRow(rowIndex);
    }

    public void setStatus(String status) {
        gwtJahiaLayoutItem.setStatus(status);
    }

    public String getEntryPointInstanceId() {
        return gwtJahiaLayoutItem.getEntryPointInstanceID();
    }

    public String getWindowId() {
        return gwtJahiaLayoutItem.getNode();
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
