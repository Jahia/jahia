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

package org.jahia.ajax.gwt.templates.components.layoutmanager.client.ui.gxt.picker;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.toolbar.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.DOM;
import org.jahia.ajax.gwt.filemanagement.client.model.GWTJahiaNode;
import org.jahia.ajax.gwt.filemanagement.client.JahiaNodeServiceAsync;
import org.jahia.ajax.gwt.filemanagement.client.JahiaNodeService;
import org.jahia.ajax.gwt.templates.components.layoutmanager.client.bean.GWTLayoutItem;
import org.jahia.ajax.gwt.templates.components.layoutmanager.client.util.JahiaPropertyHelper;
import org.jahia.ajax.gwt.templates.components.layoutmanager.client.service.LayoutmanagerService;
import org.jahia.ajax.gwt.templates.components.layoutmanager.client.ui.gxt.JahiaPortalManager;
import org.jahia.ajax.gwt.templates.commons.client.JahiaPageEntryPoint;
import org.jahia.ajax.gwt.filemanagement.client.ui.portlet.PortletWizardWindow;

import java.util.*;

/**
 * User: ktlili
 * Date: 10 d�c. 2008
 * Time: 14:28:08
 */

public class JahiaPortletPicker extends ContentPanel {
    private final String rootPath = "/content/shared/mashups";
    private final GWTJahiaNode directory = new GWTJahiaNode(null, null, null, rootPath, null, null, null, null, false, false, false, null);



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
        createPortletInstance = new TextToolItem("Create module");
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
        goToMyPortal = new TextToolItem("My portal");
        goToMyPortal.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                JahiaPortalManager.getInstance().refreshPortal();
            }
        });
    }


    private void initConfigToolItem() {
        config = new TextToolItem("My config");
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
        $wnd.addToMyPortal = function (uuid) {@org.jahia.ajax.gwt.templates.components.layoutmanager.client.ui.gxt.picker.JahiaPortletPicker::addToMyPortal(Ljava/lang/String;)(uuid); };
    }-*/;

    public static void addToMyPortal(String uuid) {
        Element ele = DOM.getElementById(uuid);
        DOM.setElementAttribute(ele, "disabled", "disabled");
        final List<GWTLayoutItem> layoutItemList = new ArrayList<GWTLayoutItem>();

        GWTJahiaNode gwtJahiaNode = new GWTJahiaNode();
        gwtJahiaNode.setUUID(uuid);
        GWTLayoutItem gwtLayoutItem = new GWTLayoutItem();
        gwtLayoutItem.setColumn(0);
        gwtLayoutItem.setRow(0);
        gwtLayoutItem.setStatus(JahiaPropertyHelper.getStatusNormaleValue());
        gwtLayoutItem.setGwtJahiaNode(gwtJahiaNode);
        layoutItemList.add(gwtLayoutItem);

        // make a call ajax
        LayoutmanagerService.App.getInstance().saveLayoutItems(JahiaPageEntryPoint.getJahiaGWTPage(), layoutItemList, new AsyncCallback() {
            public void onSuccess(Object o) {

            }

            public void onFailure(Throwable t) {
                Window.alert("Unable to execute ajax request " + t.toString());
                t.printStackTrace();
            }
        });
        return;
    }


}
