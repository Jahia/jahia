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
package org.jahia.ajax.gwt.client.widget.layoutmanager.picker;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.toolbar.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.DOM;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeServiceAsync;
import org.jahia.ajax.gwt.client.util.layoutmanager.JahiaPropertyHelper;
import org.jahia.ajax.gwt.client.widget.node.portlet.PortletWizardWindow;
import org.jahia.ajax.gwt.client.widget.layoutmanager.JahiaPortalManager;
import org.jahia.ajax.gwt.client.core.JahiaPageEntryPoint;
import org.jahia.ajax.gwt.client.service.layoutmanager.LayoutmanagerService;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.*;

/**
 * User: ktlili
 * Date: 10 d�c. 2008
 * Time: 14:28:08
 */

public class JahiaPortletPicker extends ContentPanel {
    private final String rootPath = "/content/shared/mashups";
    private final GWTJahiaNode directory = new GWTJahiaNode(null, null, null, rootPath, null, null, null, null, null, false, false, false, null);



    // list view
    private JahiaPortletInstanceListView listView;


    // portlet folder view
    private JahiaFolderPortletTree portletFolderView;
    // tool items
    private TextToolItem createPortletInstance;
    private TextToolItem goToMyPortal;
    private TextToolItem config;
    private ToolBar bar;
    private GWTJahiaNode selection;

    public JahiaPortletPicker() {
        setBorders(false);
        setBodyBorder(false);
        getHeader().setBorders(false);
        setHeaderVisible(false);
        setId("images-listView");
        setStyleAttribute("color", "black");
        setStyleAttribute("background", "none");

        LayoutContainer container = new LayoutContainer();
        container.setStyleAttribute("margin", "20px");
        container.setStyleAttribute("background", "none");
        container.setWidth("90%");
        container.setBorders(false);
        container.setLayout(new ColumnLayout());

        ContentPanel treeColumn = new ContentPanel();
        ContentPanel detailedColumn = new ContentPanel();


        // init createPortletInstanceToolItem
        initCreatePortletInstanceToolItem();

        // init goToMyPortalToolItem
        initGoToMyPortalToolItem();


        // init configToolItem
        initConfigToolItem();

        // add  toolbar
        initToolbar();

        // init list view
        initListView();

        // init portlet foler
        initPortletFolderView();

        initJavaScriptApi();

        treeColumn.setHeaderVisible(false);
        treeColumn.add(portletFolderView);
        treeColumn.setBorders(false);
        treeColumn.setBodyBorder(false);
        treeColumn.setStyleAttribute("background", "none");
        container.add(treeColumn, new ColumnData(200));

        // center panel
        detailedColumn.setLayout(new FitLayout());
        detailedColumn.setHeaderVisible(false);
        detailedColumn.setBorders(false);
        detailedColumn.setBodyBorder(false);
        detailedColumn.add(listView);
        container.add(detailedColumn, new ColumnData());


        add(container);
        loadContent();
    }

    private void initListView() {
        listView = new JahiaPortletInstanceListView();
        listView.setTemplate(getTemplate());
    }

    private void initPortletFolderView() {
        portletFolderView = new JahiaFolderPortletTree();
    }

    private void initToolbar() {
        ToolBar bar = new ToolBar();
        bar.setBorders(false);
        bar.setStyleAttribute("background", "none");
        bar.add(new FillToolItem());
        bar.add(goToMyPortal);
        bar.add(new SeparatorToolItem());
        bar.add(createPortletInstance);
        bar.add(new SeparatorToolItem());
        bar.add(config);
        setTopComponent(bar);
    }


    private void initCreatePortletInstanceToolItem() {
        // add to my portal
        createPortletInstance = new TextToolItem(Messages.getNotEmptyResource("p_mashup_create","Create mashup"));
        createPortletInstance.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                PortletWizardWindow window = new PortletWizardWindow(null,selection) {
                    public void onPortletCreated() {
                        loadContent();
                    }
                };
                window.show();

            }
        });
    }

    private void initGoToMyPortalToolItem() {
        // add to my portal
        goToMyPortal = new TextToolItem(Messages.getNotEmptyResource("p_my_portal","My portal"));
        goToMyPortal.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                JahiaPortalManager.getInstance().refreshPortal();
            }
        });
    }


    private void initConfigToolItem() {
        config = new TextToolItem(Messages.getNotEmptyResource("p_my_config","My config"));
        config.addSelectionListener(new SelectionListener<ComponentEvent>() {
            @Override
            public void componentSelected(ComponentEvent ce) {
                JahiaPortalManager.getInstance().getPortalConfig().show();
            }
        });
    }

    public void loadContent() {
        loadContent(directory);
    }

    public void loadContent(GWTJahiaNode folder) {
        clearPortletList();

        selection = folder;

        final JahiaNodeServiceAsync service = JahiaNodeService.App.getInstance();
        service.ls(folder, null,  null , null, null,true, new AsyncCallback<List<GWTJahiaNode>>() {
            public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                if (gwtJahiaNodes != null) {
                    listView.addPortlets(gwtJahiaNodes);
                    listView.refresh();
                    layout();
                } else {
                    com.google.gwt.user.client.Window.alert("null list");
                }
            }

            public void onFailure(Throwable throwable) {
                com.google.gwt.user.client.Window.alert("Element list retrieval failed :\n" + throwable.toString());
            }
        });
    }

    public void clearPortletList() {
        listView.clear();
    }

    public List<GWTJahiaNode> getSelections() {
        return listView.getSelectionModel().getSelectedItems();
    }

    public native String getTemplate() /*-{
        return ['<tpl for=".">',
                '<div style="padding: 0 0 10px 0;margin-bottom: 12px;border-bottom: 1px solid D9E2F4;float: left;width: 100%;" class="thumb-wrap" id="{name}">',
                '<div style="width: 100px; float: left; text-align: center;" class="thumb"><p><img  style="width: 100%; heigth:75px;", src="{preview}" title="{name}"></p><p><input id="{uuid}" style="width: 125px;" onclick="return addToMyPortal(\'{uuid}\');" type="button" tabindex="9" value="ADD TO MY PORTAL" class="button" id="submit"/></p></div>',
                '<div style="margin-left: 150px; margin-right: 110px;"><p style="font:bold;">{name}</p><br/><p> {description}</p></div></div>',
                '</tpl>',
                '<div class="x-clear"></div>'].join("");
    }-*/;

    private native void initJavaScriptApi() /*-{
        // define a static JS function with a friendly name
        $wnd.addToMyPortal = function (uuid) {@org.jahia.ajax.gwt.client.widget.layoutmanager.picker.JahiaPortletPicker::addToMyPortal(Ljava/lang/String;)(uuid); };
    }-*/;

    public static void addToMyPortal(String uuid) {
        Element ele = DOM.getElementById(uuid);
        DOM.setElementAttribute(ele, "disabled", "disabled");
        DOM.setElementAttribute(ele, "value", "added !");
        GWTJahiaNode gwtJahiaNode = new GWTJahiaNode();
        gwtJahiaNode.setUUID(uuid);
        GWTJahiaLayoutItem gwtLayoutItem = new GWTJahiaLayoutItem();
        gwtLayoutItem.setColumn(0);
        gwtLayoutItem.setRow(0);
        gwtLayoutItem.setStatus(JahiaPropertyHelper.getStatusNormaleValue());
        gwtLayoutItem.setGwtJahiaNode(gwtJahiaNode);

        // make a call ajax
        LayoutmanagerService.App.getInstance().addLayoutItem(JahiaPageEntryPoint.getJahiaGWTPage(),gwtLayoutItem, new AsyncCallback() {
            public void onSuccess(Object o) {
                Info.display("",Messages.getNotEmptyResource("p_mashup_added_myPortal", "Mashup added to 'MyPortal'"));
            }

            public void onFailure(Throwable t) {
                Window.alert("Unable to execute ajax request " + t.toString());
                t.printStackTrace();
            }
        });
        return;
    }


}
