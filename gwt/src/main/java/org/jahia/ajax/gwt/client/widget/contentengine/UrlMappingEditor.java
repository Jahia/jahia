/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record.RecordUpdate;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.seo.GWTJahiaUrlMapping;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * URL mapping for the node.
 *
 * @author Sergiy Shyrkov
 */
public class UrlMappingEditor extends LayoutContainer {
    private GWTJahiaNode node;
    private boolean editable;
    private String locale;

    private ListStore<GWTJahiaUrlMapping> store;
    /**
     * Initializes an instance of this class.
     *
     * @param node
     */
    public UrlMappingEditor(GWTJahiaNode node, String locale, boolean editable) {
        super(new FitLayout());
        this.locale = locale;
        this.node = node;
        this.editable = editable;
        setBorders(false);
        store = new ListStore<GWTJahiaUrlMapping>();
        JahiaContentManagementService.App.getInstance().getUrlMappings(node, locale,
                new BaseAsyncCallback<List<GWTJahiaUrlMapping>>() {
                    public void onApplicationFailure(Throwable throwable) {
                        com.google.gwt.user.client.Window.alert(Messages.getWithArgs("failure.load.urlmappings.label",
                                "Loading URL mapping failed\n\n{0}", new Object[]{
                                throwable.getLocalizedMessage()}));
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
        column.setHeaderHtml(Messages.get("label_url", "URL"));
        TextField<String> text = new TextField<String>();
        text.setAllowBlank(false);
        text.setMaxLength(250);
        CellEditor ce = new CellEditor(text);
        ce.addListener(Events.BeforeComplete, new Listener<EditorEvent>() {
            public void handleEvent(EditorEvent be) {
                Window.alert((String) be.getValue());
                be.stopEvent();
            }
        });
        column.setEditor(ce);
        configs.add(column);

        final CheckColumnConfig defaultColumn;
        final CheckColumnConfig activeColumn;
        if (editable) {
            defaultColumn = new CheckColumnConfig("default", Messages.get("label.urlmapping.default", "Default"), 70);
            defaultColumn.setEditor(new CellEditor(new CheckBox()));
            activeColumn = new CheckColumnConfig("active", Messages.get("label.urlmapping.active", "Active"), 55);
            activeColumn.setEditor(new CellEditor(new CheckBox()));
        } else {
            defaultColumn = new CheckColumnConfig("default", Messages.get("label.urlmapping.default", "Default"), 70){
                protected String getCheckState(ModelData model, String property, int rowIndex,
                                               int colIndex) {
                    return "-disabled";
                }
            };
            activeColumn = new CheckColumnConfig("active", Messages.get("label.urlmapping.active", "Active"), 55){
                protected String getCheckState(ModelData model, String property, int rowIndex,
                                               int colIndex) {
                    return "-disabled";
                }
            };
        }

        configs.add(defaultColumn);
        configs.add(activeColumn);

        column = new ColumnConfig("actions", "", 100);
        column.setAlignment(HorizontalAlignment.CENTER);
        column.setRenderer(new GridCellRenderer<GWTJahiaUrlMapping>() {
            public Object render(GWTJahiaUrlMapping modelData, String s, ColumnData columnData, final int rowIndex,
                    final int colIndex, ListStore<GWTJahiaUrlMapping> listStore, Grid<GWTJahiaUrlMapping> grid) {
                Button button = new Button(Messages.get("label_remove", "Remove"),
                        new SelectionListener<ButtonEvent>() {
                            @Override
                            public void componentSelected(ButtonEvent buttonEvent) {
                                store.remove(store.getAt(rowIndex));
                            }
                        });
                button.addStyleName("button-remove");

                button.setIcon(StandardIconsProvider.STANDARD_ICONS.minusRound());
                button.setEnabled(editable);
                return button;
            }
        });
        column.setFixed(true);
        configs.add(column);

        final RowEditor<GWTJahiaUrlMapping> re = new RowEditor<GWTJahiaUrlMapping>();

        // Add a cancel edit event listener to remove empty line
        re.addListener(Events.CancelEdit, new Listener<RowEditorEvent>() {
            public void handleEvent(RowEditorEvent ree) {
                GWTJahiaUrlMapping urlMapping = store.getModels().get(ree.getRowIndex());
                if (urlMapping.getUrl().length() == 0) {
                    store.remove(ree.getRowIndex());
                }
            }
        });

        re.addListener(Events.ValidateEdit, new Listener<RowEditorEvent>() {
            public void handleEvent(RowEditorEvent ree) {
                if(ree.getChanges().get("url").toString().matches("/sites")) {
                    ree.setCancelled(true);
                    store.remove(ree.getRowIndex());
                    Window.alert(Messages.get("label.urlmapping.error.contains.sites", "SEO Url should not contains /sites as it is used by the system."));
                }
            }
        });

        final Grid<GWTJahiaUrlMapping> grid = new Grid<GWTJahiaUrlMapping>(store, new ColumnModel(configs));
        grid.setAutoExpandColumn("url");
        grid.setBorders(true);
        grid.addPlugin(defaultColumn);
        grid.addPlugin(activeColumn);
        if (editable) {
            grid.addPlugin(re);
        }

        store.addStoreListener(new StoreListener<GWTJahiaUrlMapping>() {
            @Override
            public void storeUpdate(StoreEvent<GWTJahiaUrlMapping> se) {
                super.storeUpdate(se);
                if (se.getOperation() == RecordUpdate.EDIT && se.getModel().isDefault()) {
                    clearOtherDefaults(se.getModel(), se.getStore());
                }
            }

            private void clearOtherDefaults(GWTJahiaUrlMapping model, Store<? extends GWTJahiaUrlMapping> store) {
                for (GWTJahiaUrlMapping data : store.getModels()) {
                    if (data.isDefault() && !data.equals(model)) {
                        grid.getStore().getRecord(data).set(defaultColumn.getDataIndex(), Boolean.FALSE);
                    }
                }
            }

        });

        ToolBar toolBar = new ToolBar();
        Button add = new Button(Messages.get("label.add", "Add"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                re.stopEditing(false);
                GWTJahiaUrlMapping mapping = new GWTJahiaUrlMapping("", locale,
                        store.getCount() == 0, true);
                store.insert(mapping, 0);
                re.startEditing(store.indexOf(mapping), true);
            }
        });
        add.addStyleName("button-add");
        add.setIcon(StandardIconsProvider.STANDARD_ICONS.plusRound());
        add.setEnabled(editable);
        toolBar.add(add);
        if (URL.getServerBaseURL().startsWith("http://localhost:")
                || URL.getServerBaseURL().startsWith("https://localhost:")) {
            toolBar.add(new FillToolItem());
            LabelField warningLabel = new LabelField(Messages.get(
                    "label.urlmapping.inactiveOnLocalhost",
                    "URL mapping is inactive for server-name: localhost"));
            warningLabel.setStyleAttribute("color", "red");
            toolBar.add(warningLabel);
        }

        ContentPanel cp = new ContentPanel(new FitLayout());
        cp.setHeadingHtml("<div class='seo-title'>" + Messages.get("ece_seo_urlMapping", "URL mapping")+"</div><div class='seo-title-path'>"+node.getUrl()+"</div>");
       // cp.setHeaderVisible(false);
        cp.setTopComponent(toolBar);

        cp.add(grid);

//        FieldSet fs = new FieldSet();
        setLayout(new FitLayout());
//        fs.setHeadingHtml(Messages.get("ece_seo_urlMapping", "URL mapping"));
//        fs.setCollapsible(true);
//
//        fs.add(cp);



        add(cp);
    }
}
