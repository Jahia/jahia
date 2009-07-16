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
package org.jahia.ajax.gwt.client.widget.layoutmanager.picker;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.DOM;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.layoutmanager.JahiaPropertyHelper;
import org.jahia.ajax.gwt.client.widget.content.portlet.PortletWizardWindow;
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
    //private final String rootPath = "/content/shared/mashups";
    private final String rootPath = "/content";
    private final GWTJahiaNode directory = new GWTJahiaNode(null, null, null, rootPath, null, null, null, null, null, false, false, false, null,false);



    // list view
    private JahiaPortletInstanceListView listView;


    // portlet folder view
    private JahiaFolderPortletTree portletFolderView;
    // tool items
    private Button createPortletInstance;
    private Button goToMyPortal;
    private Button config;
    private GWTJahiaNode selection;

    // TO Do: make it not static
    private static String containerUuid;

    public JahiaPortletPicker() {
       this(null);
    }

    public JahiaPortletPicker(String containerUuid) {
        this.containerUuid = containerUuid;
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
        createPortletInstance = new Button(Messages.getNotEmptyResource("p_mashup_create","Create mashup"));
        createPortletInstance.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
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
        goToMyPortal = new Button(Messages.getNotEmptyResource("p_my_portal","My portal"));
        goToMyPortal.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                JahiaPortalManager.getInstance().refreshPortal();
            }
        });
    }


    private void initConfigToolItem() {
        config = new Button(Messages.getNotEmptyResource("p_my_config","My config"));
        config.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
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

        final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
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

    public static void addToMyPortal(String nodeUuid) {
        Element ele = DOM.getElementById(nodeUuid);
        DOM.setElementAttribute(ele, "disabled", "disabled");
        DOM.setElementAttribute(ele, "value", "added !");
        GWTJahiaNode gwtJahiaNode = new GWTJahiaNode();
        gwtJahiaNode.setUUID(nodeUuid);
        GWTJahiaLayoutItem gwtLayoutItem = new GWTJahiaLayoutItem();
        gwtLayoutItem.setColumn(0);
        gwtLayoutItem.setRow(0);
        gwtLayoutItem.setStatus(JahiaPropertyHelper.getStatusNormaleValue());
        gwtLayoutItem.setGwtJahiaNode(gwtJahiaNode);

        // make a call ajax
        LayoutmanagerService.App.getInstance().addLayoutItem(containerUuid,gwtLayoutItem, new AsyncCallback() {
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
