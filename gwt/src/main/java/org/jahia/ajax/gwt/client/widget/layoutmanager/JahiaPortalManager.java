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

package org.jahia.ajax.gwt.client.widget.layoutmanager;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.core.JahiaPageEntryPoint;
import org.jahia.ajax.gwt.client.util.templates.TemplatesDOMUtil;
import org.jahia.ajax.gwt.client.service.layoutmanager.LayoutmanagerService;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutManagerConfig;
import org.jahia.ajax.gwt.client.widget.layoutmanager.listener.OnPortletMovedListener;
import org.jahia.ajax.gwt.client.widget.layoutmanager.picker.JahiaPortletPicker;
import org.jahia.ajax.gwt.client.widget.layoutmanager.picker.JahiaPortletPickerDialog;
import org.jahia.ajax.gwt.client.widget.layoutmanager.portlet.JahiaPortal;
import org.jahia.ajax.gwt.client.widget.layoutmanager.portlet.JahiaPortletFactory;
import org.jahia.ajax.gwt.client.widget.layoutmanager.portlet.JahiaPortlet;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 27 ao�t 2008
 * Time: 15:52:11
 */
public class JahiaPortalManager extends ContentPanel {
    private static JahiaPortalManager dis;
    private JahiaPortal portal;
    private ToolBar bar = new ToolBar();
    private JahiaPortletPicker portletPicker;
    private JahiaPortletPickerDialog portletSelectorDialog;
    private JahiaPortalConfig portalConfig;
    private TextToolItem portletPickerButton;
    private TextToolItem saveAsDefaultButton;
    private TextToolItem configPortalButton;
    private static JahiaPortletFactory portletFactory = new JahiaPortletFactory();
    private GWTJahiaLayoutManagerConfig gwtPortalConfig;
    private static boolean isLiveMode = !JahiaPageEntryPoint.getJahiaGWTPage().getMode().equalsIgnoreCase("edit");


    public JahiaPortalManager(GWTJahiaLayoutManagerConfig gwtPortalConfig) {
        super(new RowLayout());
        dis = this;
        this.gwtPortalConfig = gwtPortalConfig;
        init();

    }

    public static JahiaPortalManager getInstance() {
        return dis;
    }

    public GWTJahiaLayoutManagerConfig getGwtPortalConfig() {
        return gwtPortalConfig;
    }

    public void setGwtPortalConfig(GWTJahiaLayoutManagerConfig gwtPortalConfig) {
        this.gwtPortalConfig = gwtPortalConfig;
    }

    public void init() {
        setBorders(false);
        setBodyBorder(false);
        getHeader().setBorders(false);
        setHeaderVisible(false);
        // portal config
        portalConfig = new JahiaPortalConfig(gwtPortalConfig);

        // portled selector (dialog)
        portletSelectorDialog = new JahiaPortletPickerDialog();


        // portal
        portal = new JahiaPortal(gwtPortalConfig.getNbColumns());

        // set colum widts as pixels or percentages
        double columnWidth = 1f / gwtPortalConfig.getNbColumns();

        for (int i = 0; i < gwtPortalConfig.getNbColumns(); i++) {
            portal.setColumnWidth(i, columnWidth);
        }


        portletPickerButton = new TextToolItem(Messages.getNotEmptyResource("p_add_mashups", "Add mashups"));
        portletPickerButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            @Override
            public void componentSelected(ComponentEvent ce) {
                goToPortelSelector();
            }
        });

        saveAsDefaultButton = new TextToolItem(Messages.getNotEmptyResource("p_save_default", "Save as default"));
        saveAsDefaultButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            @Override
            public void componentSelected(ComponentEvent ce) {
                LayoutmanagerService.App.getInstance().saveAsDefault(JahiaPageEntryPoint.getJahiaGWTPage(), new AsyncCallback() {
                    public void onSuccess(Object o) {
                        Log.debug("Current config saved as default config.");
                    }

                    public void onFailure(Throwable throwable) {
                        Log.error("Unable to save current config as default config.", throwable);
                    }
                });
            }
        });

        configPortalButton = new TextToolItem(Messages.getNotEmptyResource("p_my_config", "My config"));
        configPortalButton.addSelectionListener(new SelectionListener<ComponentEvent>() {


            @Override
            public void componentSelected(ComponentEvent ce) {
                portalConfig.show();
            }
        });


        setButtonAlign(Style.HorizontalAlignment.RIGHT);

        // if box are not draggable that it implies that we can't add boxes
        if (!gwtPortalConfig.isLiveDraggable()) {
            portletPickerButton.setVisible(false);
            //myPortalButton.setVisible(false);
            configPortalButton.setVisible(false);
        }
        add(portal);
        // init javascript api
        initJavaScriptApi();

        bar.setBorders(false);
        bar.setStyleAttribute("background", "none");

        bar.add(new FillToolItem());
        bar.add(portletPickerButton);
        bar.add(new SeparatorToolItem());
        bar.add(saveAsDefaultButton);
        bar.add(new SeparatorToolItem());
        bar.add(configPortalButton);
        setTopComponent(bar);

        // load portlet
        loadPortlets();
        addStyleName("gwt-jahiaportal-body");

        layout();


    }

    /**
     * refresh portal
     */
    public void refreshPortal() {
        com.google.gwt.user.client.Window.Location.reload();
    }

    /**
     * Switch to full screen view
     *
     * @param portlet
     */
    public void switchToFullScreenView(JahiaPortlet portlet) {
        removeAll();
        portlet.removeFromParent();
        add(portlet);
        layout();
    }

    /**
     * Add to portal
     *
     * @param portlet
     */
    public void addToPortal(JahiaPortlet portlet) {
        if (JahiaPortalManager.getInstance().getPortal() != null) {
            JahiaPortalManager.getInstance().getPortal().add(portlet, portlet.getColumnIndex());
            if (!JahiaPortalManager.getInstance().getGwtPortalConfig().isLiveDraggable()) {
                portlet.setHeaderVisible(false);
            }
        } else {
            Log.error("JahiaPortlet not initialised");
        }
    }

    /**
     * Remove portlet from portal
     *
     * @param jahiaPortlet
     */
    public void removeJahiaPortlet(JahiaPortlet jahiaPortlet) {
        if (jahiaPortlet != null) {
            jahiaPortlet.removeFromParent();
        }
    }


    public JahiaPortal getPortal() {
        return portal;
    }

    public JahiaPortalConfig getPortalConfig() {
        return portalConfig;
    }


    public void setPortal(JahiaPortal portal) {
        this.portal = portal;
    }

    public JahiaPortletPickerDialog getPortletSelectorDialog() {
        return portletSelectorDialog;
    }

    public void setPortletSelectorDialog(JahiaPortletPickerDialog portletSelectorDialog) {
        this.portletSelectorDialog = portletSelectorDialog;
    }

    public JahiaPortletFactory getPortletFactory() {
        return portletFactory;
    }

    public void setPortletFactory(JahiaPortletFactory aPortletFactory) {
        portletFactory = aPortletFactory;
    }


    private void loadPortlets() {
        // first update all alod brother postion
        LayoutmanagerService.App.getInstance().getLayoutItems(JahiaPageEntryPoint.getJahiaGWTPage(), new AsyncCallback<List<GWTJahiaLayoutItem>>() {
            public void onSuccess(List<GWTJahiaLayoutItem> draggableWidgetList) {
                // add boxes
                try {
                    // load widget instance
                    fillJahiaPortal(draggableWidgetList);

                    // call layout
                    layout();

                } catch (Exception e) {
                    Log.error("error when loading widget instances", e);
                }

            }

            public void onFailure(Throwable t) {
                Log.error("Unable to execute ajax action due to:" + t.toString(), t);
                t.printStackTrace();
            }
        });

    }

    /**
     * load portlets
     *
     * @param gwtLayoutItems
     */
    private void fillJahiaPortal(List<GWTJahiaLayoutItem> gwtLayoutItems) {
        if (gwtLayoutItems != null) {
            for (GWTJahiaLayoutItem gwtLayoutItem : gwtLayoutItems) {
                // create a html draggable ui
                if (gwtLayoutItem != null) {
                    try {
                        // set gwtJahiaDraggableWidgetIterator's preferences
                        if (gwtLayoutItem.getPortlet() != null) {
                            addJahiaPortlet(gwtLayoutItem);
                        } else {
                            Log.error("error when loading widget,  widetId is not defined.");
                        }

                    } catch (Exception e) {
                        Log.error("error when loading widget instance with id[" + gwtLayoutItem.getPortlet() + "]", e);

                    }
                }else{
                    Log.error("Found a null layout item.");
                }
            }
        }

        layout();

        TemplatesDOMUtil.setVisible("layout", true);
    }

    public void addJahiaPortlet(GWTJahiaLayoutItem gwtLayoutItem) {
        int column = gwtLayoutItem.getColumn();
        int row = gwtLayoutItem.getRow();
        Log.debug("add widget" + "[" + gwtLayoutItem.getPortlet() + " in column " + column + "]");
        if (column < 0) {
            column = 0;
        }

        // create and add the protlet into a column
        JahiaPortlet portlet = createJahiaPortlet(gwtLayoutItem);

        if (portlet != null) {
            // handle full screen view
            if (portlet.isFullScreen()) {
                switchToFullScreenView(portlet);
            } else {
                // add to portal
                if (gwtPortalConfig.getNbColumns() < (column + 1)) {
                    int realColumnIndex = gwtPortalConfig.getNbColumns() - 1;
                    insertJahiaPortlet(portlet, row, realColumnIndex);
                } else {
                    insertJahiaPortlet(portlet, row, column);
                }
            }
        } else {
            Log.warn("Portlet with id: " + gwtLayoutItem.getPortlet());
        }
        layout();
    }

    private void insertJahiaPortlet(JahiaPortlet portlet, int row, int column) {
        final LayoutContainer columnContainer = portal.getItem(column);
        final int currentColumnNbPortlets = columnContainer.getItemCount();
        if (row < currentColumnNbPortlets - 1) {
            portal.insert(portlet, row, column);
        } else {
            portal.add(portlet, column);
        }
    }

    public static JahiaPortlet createJahiaPortlet(final GWTJahiaLayoutItem gwtLayoutItem) {
        JahiaPortlet portlet = portletFactory.createPortlet(gwtLayoutItem);
        if (!JahiaPortalManager.getInstance().getGwtPortalConfig().isLiveDraggable() && isLiveMode) {
            portlet.setHeaderVisible(false);
        }
        return portlet;
    }


    /**
     * Return to my portal: used by a JNSI method --> must be declared as static
     */
    public void goToMyPortal() {
        JahiaPortalManager.getInstance().refreshPortal();
    }

    public void goToPortelSelector() {
        removeAll();
        portletPicker = new JahiaPortletPicker();
        portletPicker.setStyleAttribute("font", "black");
        bar.setVisible(false);
        portletPicker.setVisible(true);
        //portletPicker.loadContent();
        add(portletPicker);
        layout();
    }

    /**
     * Add shared modules :  :used by a JNSI method --> must be declared as static
     */
    public void showJahiaPortletSelector() {
        JahiaPortalManager.getInstance().getPortletSelectorDialog().show();
    }

    public JahiaPortletPicker getPortletPicker() {
        return portletPicker;
    }

    /**
     * Init javascript api
     */
    private native void initJavaScriptApi() /*-{
        // define a static JS function with a friendly name

    }-*/;

}
