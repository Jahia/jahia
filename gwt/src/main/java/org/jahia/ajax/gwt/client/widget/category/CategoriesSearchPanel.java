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
package org.jahia.ajax.gwt.client.widget.category;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.ListViewSelectionModel;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.service.category.CategoryService;
import org.jahia.ajax.gwt.client.service.category.CategoryServiceAsync;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.List;

/**
 * User: ktlili
 * Date: 9 oct. 2008
 * Time: 17:10:13
 */
public class CategoriesSearchPanel extends ContentPanel {
    private final ListView list = new ListView();
    private final CategoryServiceAsync service = CategoryService.App.getInstance();
    private CategoriesPickerLeftComponent parentComponent;

    public CategoriesSearchPanel(CategoriesPickerLeftComponent parentComponent) {
        this.parentComponent = parentComponent;
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        setLayout(new RowLayout());
        setHeading(getResource("Search"));

        final ListViewSelectionModel<GWTJahiaCategoryNode> listSelectionModel = new ListViewSelectionModel<GWTJahiaCategoryNode>();
        list.setBorders(false);
        listSelectionModel.bindList(list);
        final ListStore<GWTJahiaCategoryNode> listStore = new ListStore<GWTJahiaCategoryNode>();
        list.setStore(listStore);
        listSelectionModel.bind(listStore);
        list.setDisplayProperty("name");
        final TextField propertyNameField = new TextField();
        propertyNameField.setFieldLabel(getResource("property"));
        propertyNameField.setWidth(10);

        final TextField propertyValueField = new TextField();
        propertyValueField.setFieldLabel(getResource("value"));
        propertyValueField.setWidth(10);

        final FieldSet fieldSet = new FieldSet();
        FormLayout layout = new FormLayout();
        layout.setLabelAlign(FormPanel.LabelAlign.TOP);
        layout.setLabelWidth(75);
        layout.setPadding(2);
        fieldSet.setCollapsible(true);
        fieldSet.setLayout(layout);
        fieldSet.setHeading(getResource("properties"));
        fieldSet.add(propertyNameField);
        fieldSet.add(propertyValueField);

        final TextToolItem selectToolItem = new TextToolItem("");
        selectToolItem.setToolTip(getResource("add"));        
        selectToolItem.setIconStyle("gwt-categoriespiker-icon-select");
        selectToolItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                parentComponent.addCategories(list.getSelectionModel().getSelectedItems());
            }
        });
        getHeader().insertTool(selectToolItem, 0);

        final TextToolItem searchButton = new TextToolItem("Search");
        searchButton.setIconStyle("gwt-categoriespiker-icon-search");        
        searchButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                listStore.removeAll();
                GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
                if (propertyNameField.getValue() != null && propertyValueField.getValue() != null) {
                    gwtJahiaNodeProperty.setName(propertyNameField.getValue().toString());
                    gwtJahiaNodeProperty.setValue(propertyValueField.getValue().toString());
                    service.search(gwtJahiaNodeProperty, -1, new AsyncCallback<List<GWTJahiaCategoryNode>>() {
                        public void onFailure(Throwable throwable) {
                            Log.error("Unable to find category");
                        }

                        public void onSuccess(List<GWTJahiaCategoryNode> categories) {
                            if (categories != null) {
                                Log.debug("Find " + categories.size() + "categories.");
                                listStore.add(categories);
                                doAttachChildren();
                                doLayout();
                            } else {
                                Log.debug("Find 0 categorie.");

                            }
                        }
                    });
                }

            }
        });
        ToolBar bar = new ToolBar();
        bar.add(searchButton);
        bar.add(new FillToolItem());
        bar.add(selectToolItem);

        add(bar);
        add(fieldSet);
        add(list);


    }

    public void refresh() {
//        list.removeAll();
    }

    public static String getResource(String key) {
        return Messages.getResource(key);
    }

}
