/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.seo.GWTJahiaUrlMapping;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EditorEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowEditor;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * URL mapping for the node.
 * 
 * @author Sergiy Shyrkov
 */
public class UrlMappingEditor extends LayoutContainer {

    private static class SingleSelectionCheckColumnConfig extends CheckColumnConfig {

        /**
         * Initializes an instance of this class.
         * 
         * @param id
         * @param name
         * @param width
         */
        SingleSelectionCheckColumnConfig(String id, String name, int width) {
            super(id, name, width);
        }

        @Override
        protected void onMouseDown(GridEvent<ModelData> ge) {
            String cls = ge.getTarget().getClassName();
            if (cls != null && cls.indexOf("x-grid3-cc-" + getId()) != -1 && cls.indexOf("disabled") == -1) {
                ge.stopEvent();
                int index = grid.getView().findRowIndex(ge.getTarget());
                ModelData m = grid.getStore().getAt(index);
                Record r = grid.getStore().getRecord(m);
                boolean b = (Boolean) m.get(getDataIndex());
                if (!b) {
                    for (ModelData data : grid.getStore().getModels()) {
                        GWTJahiaUrlMapping mapping = (GWTJahiaUrlMapping) data;
                        if (mapping.isDefault()) {
                            grid.getStore().getRecord(mapping).set(getDataIndex(), Boolean.FALSE);
                        }
                    }
                }
                r.set(getDataIndex(), !b);
            }
        }

    }

    private GWTJahiaLanguage locale;

    private ListStore<GWTJahiaUrlMapping> store;

    /**
     * Initializes an instance of this class.
     * 
     * @param node
     */
    public UrlMappingEditor(GWTJahiaNode node, GWTJahiaLanguage locale) {
        super(new FitLayout());
        this.locale = locale;
        setBorders(false);
        store = new ListStore<GWTJahiaUrlMapping>();

        JahiaContentManagementService.App.getInstance().getUrlMappings(node, locale.getCountryIsoCode(),
                new AsyncCallback<List<GWTJahiaUrlMapping>>() {
                    public void onFailure(Throwable throwable) {
                        com.google.gwt.user.client.Window.alert(Messages.get("load_url_mappings_failed",
                                "Loading URL mapping failed\n\n")
                                + throwable.getLocalizedMessage());
                        Log.error("failed", throwable);
                    }

                    public void onSuccess(List<GWTJahiaUrlMapping> mappings) {
                        store.add(mappings);
                    }
                });
    }

    public List<GWTJahiaUrlMapping> getMappings() {
        return store.getModels();
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        setHeight(610);
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig();
        column.setId("url");
        column.setHeader(Messages.get("label.url", "URL"));
        TextField<String> text = new TextField<String>();
        text.setAllowBlank(false);
        CellEditor ce = new CellEditor(text);
        ce.addListener(Events.BeforeComplete, new Listener<EditorEvent>() {
            public void handleEvent(EditorEvent be) {
                Window.alert((String) be.getValue());
                be.stopEvent();
            }
        });
        column.setEditor(ce);
        configs.add(column);

        CheckColumnConfig defaultColumn = new SingleSelectionCheckColumnConfig("default", Messages.get("label.default", "Default"), 55);
        defaultColumn.setEditor(new CellEditor(new CheckBox()));
        configs.add(defaultColumn);

        CheckColumnConfig activeColumn = new CheckColumnConfig("active", Messages.get("label.active", "Active"), 55);
        activeColumn.setEditor(new CellEditor(new CheckBox()));
        configs.add(activeColumn);

        column = new ColumnConfig("actions", "", 100);
        column.setAlignment(HorizontalAlignment.CENTER);
        column.setRenderer(new GridCellRenderer<GWTJahiaUrlMapping>() {
            public Object render(GWTJahiaUrlMapping modelData, String s, ColumnData columnData, final int rowIndex,
                    final int colIndex, ListStore<GWTJahiaUrlMapping> listStore, Grid<GWTJahiaUrlMapping> grid) {
                Button button = new Button(Messages.get("label.remove", "Remove"),
                        new SelectionListener<ButtonEvent>() {
                            @Override
                            public void componentSelected(ButtonEvent buttonEvent) {
                                store.remove(store.getAt(rowIndex));
                            }
                        });
                button.setIcon(ContentModelIconProvider.getInstance().getMinusRound());
                return button;
            }
        });
        column.setFixed(true);
        configs.add(column);

        final RowEditor<GWTJahiaUrlMapping> re = new RowEditor<GWTJahiaUrlMapping>();
        final Grid<GWTJahiaUrlMapping> grid = new Grid<GWTJahiaUrlMapping>(store, new ColumnModel(configs));
        grid.setAutoExpandColumn("url");
        grid.setBorders(true);
        grid.addPlugin(defaultColumn);
        grid.addPlugin(activeColumn);
        grid.addPlugin(re);

        ToolBar toolBar = new ToolBar();
        Button add = new Button(Messages.get("label_add", "Add"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                re.stopEditing(false);
                GWTJahiaUrlMapping mapping = new GWTJahiaUrlMapping("", locale.getCountryIsoCode(),
                        store.getCount() == 0, true);
                store.insert(mapping, 0);
                re.startEditing(store.indexOf(mapping), true);
            }
        });
        add.setIcon(ContentModelIconProvider.getInstance().getPlusRound());
        toolBar.add(add);

        ContentPanel cp = new ContentPanel(new FitLayout());
        cp.setHeaderVisible(false);
        cp.setTopComponent(toolBar);
        cp.add(grid);

        FieldSet fs = new FieldSet();
        fs.setLayout(new FitLayout());
        fs.setHeading(Messages.get("label.urlMapping", "URL mapping"));
        fs.setCollapsible(true);
        fs.add(cp);

        add(fs);
    }
}
