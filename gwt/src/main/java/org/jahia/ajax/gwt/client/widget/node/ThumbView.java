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

package org.jahia.ajax.gwt.client.widget.node;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.StoreFilterField;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeServiceAsync;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.nodes.actions.FileActions;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

import java.util.List;
import java.util.ArrayList;

/**
 * User: rfelden
 * Date: 9 sept. 2008 - 17:49:57
 */
public class ThumbView extends TopRightComponent {

    private ContentPanel m_component;
    private ListStore<GWTJahiaNode> store;
    private MyListView<GWTJahiaNode> view;
    private SimpleComboBox<String> sort;

    private ManagerConfiguration configuration;

    public ThumbView(final ManagerConfiguration config) {
        configuration = config;

        m_component = new ContentPanel(new FitLayout());
        m_component.setHeaderVisible(false);
        m_component.setScrollMode(Style.Scroll.AUTO);
        m_component.setId("images-view");
        m_component.setBorders(true);
        m_component.setBodyBorder(false);

        ToolBar bar = new ToolBar();
        bar.add(new LabelToolItem(Messages.getResource("fm_thumbFilter")));

        store = new ListStore<GWTJahiaNode>();

        StoreFilterField<GWTJahiaNode> field = new StoreFilterField<GWTJahiaNode>() {
            @Override
            protected boolean doSelect(Store<GWTJahiaNode> store, GWTJahiaNode parent, GWTJahiaNode record, String property, String filter) {
                String name = record.getName().toLowerCase();
                return name.indexOf(filter.toLowerCase()) != -1;
            }

            @Override
            protected void onFilter() {
                super.onFilter();
                view.getSelectionModel().select(0);
            }
        };
        field.setWidth(200);
        field.bind(store);

        bar.add(new AdapterToolItem(field));
        bar.add(new SeparatorToolItem());
        bar.add(new LabelToolItem(Messages.getResource("fm_thumbSort")));

        // please keep same order as in sort() method
        List<String> sorts = new ArrayList<String>();
        sorts.add(Messages.getResource("fm_thumbSortName"));
        sorts.add(Messages.getResource("fm_thumbSortSize"));
        sorts.add(Messages.getResource("fm_thumbSortLastModif"));

        sort = new SimpleComboBox<String>();
        sort.setTriggerAction(ComboBox.TriggerAction.ALL);
        sort.setEditable(false);
        sort.setForceSelection(true);
        sort.setWidth(250);
        sort.add(sorts);
        sort.setSimpleValue(sorts.get(0));
        sort.addListener(Events.Change, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {
                sort();
            }
        });

        bar.add(new AdapterToolItem(sort));

        m_component.setTopComponent(bar);

        view = new MyListView<GWTJahiaNode>() {
            @Override
            protected GWTJahiaNode prepareData(GWTJahiaNode model) {
                String s = model.getName();
                model.set("shortName", Util.ellipse(s, 14));
                return model;
            }

            public void setContextMenu(Menu menu) {
                super.setContextMenu(menu);
            }
        };

        view.setTemplate(getTemplate());
        view.setStore(store);
        view.setItemSelector("div.thumb-wrap");
        view.setOverStyle("x-view-over");
        view.getSelectionModel().addListener(Events.SelectionChange, new Listener<SelectionEvent<GWTJahiaNode>>() {
            public void handleEvent(SelectionEvent<GWTJahiaNode> be) {
                getLinker().onTableItemSelected();
            }
        });
        view.addListener(Event.ONDBLCLICK, new Listener<ListViewEvent>() {
            public void handleEvent(ListViewEvent event) {
                List<GWTJahiaNode> selection = (List<GWTJahiaNode>) getLinker().getTableSelection();
                if (selection != null && selection.size() > 0) {
                    GWTJahiaNode selected = selection.get(0);
                    if (selected.isFile()) {
                        if (config.isEnableFileDoubleClick()) {
                            if (selected.isDisplayable()) {
                                ImagePopup.popImage(selected);
                            } else {
                                FileActions.download(getLinker());
                            }
                        }
                    } else {
                        getLinker().onTableItemDoubleClicked(selected);
                    }
                }
            }
        });
        /*view.getSelectionModel().addListener(Events.OnDoubleClick, new Listener<SelectionEvent<GWTJahiaNode>>() {
            public void handleEvent(SelectionEvent<GWTJahiaNode> be) {
                getLinker().onTableItemDoubleClicked(be.model);
            }
        });*/
        m_component.add(view);
    }

    private void sort() {
        int index = sort.getSelectedIndex();
        if (index == 0) {
            store.sort("name", Style.SortDir.ASC);
        } else if (index == 1) {
            store.sort("size", Style.SortDir.ASC);
        } else if (index == 2) {
            store.sort("date", Style.SortDir.ASC);
        }
    }

    public void setContextMenu(Menu menu) {
        view.setContextMenu(menu);
    }

    public void setContent(Object root) {
        clearTable();
        if (root != null) {
            final JahiaNodeServiceAsync service = JahiaNodeService.App.getInstance();
            if (getLinker() != null) {
                getLinker().loading("listing directory content...");
            }
            service.ls((GWTJahiaNode) root, configuration.getNodeTypes(), configuration.getMimeTypes(), configuration.getFilters(), null, false, new AsyncCallback<List<GWTJahiaNode>>() {
                public void onFailure(Throwable throwable) {
                    Window.alert("Element list retrieval failed :\n" + throwable.getLocalizedMessage());
                    if (getLinker() != null) {
                        getLinker().loaded();
                    }
                }

                public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                    if (gwtJahiaNodes != null) {
                        store.add(gwtJahiaNodes);
                    } else {
                        Window.alert("null list");
                    }
                    if (getLinker() != null) {
                        getLinker().loaded();
                    }
                    getLinker().onTableItemSelected();
                }
            });
        }
    }

    public void setProcessedContent(Object content) {
        clearTable();
        if (content != null) {
            List<GWTJahiaNode> gwtJahiaNodes = (List<GWTJahiaNode>) content;
            store.add(gwtJahiaNodes);
            getLinker().onTableItemSelected();
        }
    }

    public void clearTable() {
        store.removeAll();
    }

    public Object getSelection() {
        return view.getSelectionModel().getSelectedItems();
    }

    public void refresh() {
        setContent(getLinker().getTreeSelection());
    }

    public Component getComponent() {
        return m_component;
    }

    private class MyListView<T extends ModelData> extends ListView<T> {
        @Override
        protected T prepareData(T model) {
            String s = ((GWTJahiaNode) model).getName();
            model.set("shortName", Util.ellipse(s, 14));
            return model;
        }

        public void setContextMenu(Menu menu) {
            super.setContextMenu(menu);
        }
    }

    public native String getTemplate() /*-{
        return ['<tpl for=".">',
                '<div class="thumb-wrap" id="{name}">',
                '<div class="thumb"><img src="{preview}" title="{name}"></div>',
                '<span class="x-editable">{shortName}</span></div>',
                '</tpl>',
                '<div class="x-clear"></div>'].join("");
    }-*/;
}
