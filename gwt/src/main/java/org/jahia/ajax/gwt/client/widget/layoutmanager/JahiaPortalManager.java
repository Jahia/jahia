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
package org.jahia.ajax.gwt.client.widget.layoutmanager;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.core.JahiaPageEntryPoint;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
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
    private TextToolItem restoreDefaultButton;
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
                final MessageBox box = new MessageBox();
                final Listener confirmBoxListener = new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent ce) {
                        Dialog dialog = (Dialog) ce.component;
                        Button btn = dialog.getButtonPressed();
                        if (btn.getText().equalsIgnoreCase(MessageBox.OK)) {
                            LayoutmanagerService.App.getInstance().saveAsDefault(JahiaPageEntryPoint.getJahiaGWTPage(), new AsyncCallback() {
                                public void onSuccess(Object o) {
                                   Log.debug("Save as defaut");
                                }

                                public void onFailure(Throwable throwable) {
                                    Log.error("Unable to restore default.", throwable);
                                }
                            });
                        }
                    }
                };


                box.setButtons(MessageBox.OKCANCEL);
                box.setIcon(MessageBox.QUESTION);
                box.setTitle("Restore");
                box.addCallback(confirmBoxListener);
                box.setMessage("Do you really want to restore dafault?");
                box.show();
            }
        });

        restoreDefaultButton = new TextToolItem(Messages.getNotEmptyResource("p_restore_default", "Restore default"));
        restoreDefaultButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            @Override
            public void componentSelected(ComponentEvent ce) {
                final MessageBox box = new MessageBox();
                final Listener confirmBoxListener = new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent ce) {
                        Dialog dialog = (Dialog) ce.component;
                        Button btn = dialog.getButtonPressed();
                        if (btn.getText().equalsIgnoreCase(MessageBox.OK)) {
                            LayoutmanagerService.App.getInstance().restoreDefault(JahiaPageEntryPoint.getJahiaGWTPage(), new AsyncCallback() {
                                public void onSuccess(Object o) {
                                    refreshPortal();
                                }

                                public void onFailure(Throwable throwable) {
                                    Log.error("Unable to restore default.", throwable);
                                }
                            });
                        }
                    }
                };


                box.setButtons(MessageBox.OKCANCEL);
                box.setIcon(MessageBox.QUESTION);
                box.setTitle("Restore");
                box.addCallback(confirmBoxListener);
                box.setMessage("Do you really want to restore dafault?");
                box.show();
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
        if (JahiaGWTParameters.hasWriteAccess()) {
            bar.add(saveAsDefaultButton);
            bar.add(new SeparatorToolItem());
        }
        bar.add(restoreDefaultButton);
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
                } else {
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
        if (row < currentColumnNbPortlets) {
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
