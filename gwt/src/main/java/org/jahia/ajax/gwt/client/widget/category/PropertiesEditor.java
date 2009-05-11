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

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;
import java.util.ArrayList;

import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.AbsolutePanel;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.service.category.CategoryService;
import org.jahia.ajax.gwt.client.messages.Messages;


/**
 * User: ktlili
 * Date: 4 sept. 2008
 * Time: 16:20:11
 */
public class PropertiesEditor extends AbsolutePanel {
    private GWTJahiaCategoryNode gwtJahiaCategoryNode;
    private CategoryDetails categoryDetails;

    public PropertiesEditor(CategoryDetails categoryDetails) {
        this.categoryDetails = categoryDetails;
    }

    public GWTJahiaCategoryNode getGwtJahiaCategoryNode() {
        return gwtJahiaCategoryNode;
    }

    public void setGwtJahiaCategoryNode(GWTJahiaCategoryNode gwtJahiaCategoryNode) {
        this.gwtJahiaCategoryNode = gwtJahiaCategoryNode;
    }

    /**
     * Refresh my Settings panel
     */
    public FormPanel createForm() {
        final FormPanel formPanel = new FormPanel();

        if (gwtJahiaCategoryNode != null) {
            final List<GWTJahiaNodeProperty> gwtJahiaNodeProperties = gwtJahiaCategoryNode.getCategoryProperties();

            clear();
            // creat a form panel
            formPanel.setHeight(100);
            formPanel.setFieldWidth(300);
            formPanel.setLabelWidth(150);
            formPanel.setScrollMode(Style.Scroll.AUTO);
            formPanel.setBorders(false);
            formPanel.setBodyBorder(false);
            formPanel.setHeaderVisible(false);
            formPanel.setFrame(false);
            formPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);


            // add a text fiedl for each property
            if (gwtJahiaCategoryNode != null) {
                Log.debug("Category node:" + gwtJahiaCategoryNode.getName());
                if (gwtJahiaNodeProperties != null) {
                    for (GWTJahiaNodeProperty gwtJahiaNodeProperty : gwtJahiaNodeProperties) {
                        // create a text field
                        TextField textField = new TextField();
                        if (gwtJahiaNodeProperty.getName() != null) {
                            textField.setName(gwtJahiaNodeProperty.getName());
                        }
                        Log.debug("add property:" + gwtJahiaNodeProperty.getName());
                        textField.setFieldLabel(gwtJahiaNodeProperty.getName());
                        textField.setValue(gwtJahiaNodeProperty.getValue());
                        textField.setEnabled(!gwtJahiaNodeProperty.isReadOnly());
                        formPanel.add(textField);
                    }
                }
            }

            // save property listener
            SelectionListener saveJahiaUserPropertiesListener = new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent ce) {
                    List<GWTJahiaNodeProperty> newJahiaUserProperties = new ArrayList<GWTJahiaNodeProperty>();
                    for (Field field : formPanel.getFields()) {
                        GWTJahiaNodeProperty jahiaUserProperty = new GWTJahiaNodeProperty();
                        jahiaUserProperty.setName(field.getName());
                        jahiaUserProperty.setValue(field.getRawValue());
                        newJahiaUserProperties.add(jahiaUserProperty);
                    }

                    // update user properties
                    CategoryService.App.getInstance().saveProperties(gwtJahiaCategoryNode, newJahiaUserProperties, new AsyncCallback() {
                        public void onFailure(Throwable throwable) {
                            Log.error("Can't update jahia user properties", throwable);
                        }

                        public void onSuccess(Object o) {

                        }
                    });
                }
            };


            ToolBar toolBar = new ToolBar();
            TextToolItem item = new TextToolItem(getResource("cat_prop_add"), "cat-prop-add");
            item.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent ce) {
                    createAddPropertyWindow();
                }
            });
            toolBar.add(item);
            item = new TextToolItem(getResource("cat_prop_remove"), "cat-prop-remove");
            if (gwtJahiaNodeProperties != null) {
                item.setEnabled(!gwtJahiaNodeProperties.isEmpty());
            } else {
                item.setEnabled(false);
            }
            item.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent ce) {
                    createRemovePropertiesWindow();
                }
            });
            toolBar.add(item);

            item = new TextToolItem(getResource("button_save"), "cate-save");
            if (gwtJahiaNodeProperties != null) {
                item.setEnabled(!gwtJahiaNodeProperties.isEmpty());
            } else {
                item.setEnabled(false);
            }
            item.addSelectionListener(saveJahiaUserPropertiesListener);
            toolBar.add(item);
            formPanel.setTopComponent(toolBar);

            if (gwtJahiaCategoryNode == null || !gwtJahiaCategoryNode.isWriteable()) {
               formPanel.setEnabled(false);
            }

        }

        // remove all
        return formPanel;
    }

    /**
     * Get resources
     *
     * @param key
     * @return
     */
    private String getResource(String key) {
        return Messages.getResource(key);
    }


    /**
     * Create 'add new property' window
     */
    private void createRemovePropertiesWindow() {
        final com.extjs.gxt.ui.client.widget.Window removePropertiesWindow = new com.extjs.gxt.ui.client.widget.Window();
        removePropertiesWindow.setLayout(new FitLayout());
        removePropertiesWindow.setModal(true);
        removePropertiesWindow.setResizable(false);
        removePropertiesWindow.setHeading(getResource("cat_prop_remove"));
        removePropertiesWindow.setBodyBorder(false);
        removePropertiesWindow.setInsetBorder(false);
        removePropertiesWindow.setWidth(310);


        // add a text fiedl for each property
        if (gwtJahiaCategoryNode != null) {
            final DataList list = new DataList();
            list.setWidth(280);
            list.setHeight(100);
            list.setScrollMode(Style.Scroll.AUTO);
            list.setSelectionMode(Style.SelectionMode.MULTI);
            for (GWTJahiaNodeProperty gwtJahiaUserProperty : gwtJahiaCategoryNode.getCategoryProperties()) {
                DataListItem item = new DataListItem();
                item.setItemId(gwtJahiaUserProperty.getName());
                item.setText(gwtJahiaUserProperty.getName());
                list.add(item);
            }

            removePropertiesWindow.add(list);

            // save button
            Button save = new Button(getResource("button_remove"));
            save.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    // update user properties
                    List<GWTJahiaNodeProperty> categoryProperties = new ArrayList<GWTJahiaNodeProperty>();

                    // get selected properties and add it to "old properties list"
                    List<DataListItem> dataListItems = list.getSelectedItems();
                    for (DataListItem dataListItem : dataListItems) {
                        GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
                        gwtJahiaNodeProperty.setName(dataListItem.getItemId());
                        categoryProperties.add(gwtJahiaNodeProperty);
                    }
                    Log.debug("Number of properties to remove: " + categoryProperties.size());
                    // apply modification
                    CategoryService.App.getInstance().removeProperties(gwtJahiaCategoryNode, categoryProperties, new LoadCategoryAsyncCallback(PropertiesEditor.this));
                    removePropertiesWindow.close();

                }
            });
            removePropertiesWindow.addButton(save);

        }
        removePropertiesWindow.layout();
        removePropertiesWindow.show();
    }

    /**
     * Create 'add new property' window
     */
    private void createAddPropertyWindow() {
        final com.extjs.gxt.ui.client.widget.Window newFieldWindow = new com.extjs.gxt.ui.client.widget.Window();
        newFieldWindow.setModal(true);
        newFieldWindow.setResizable(false);
        newFieldWindow.setHeading(getResource("cat_prop_add"));
        newFieldWindow.setBodyBorder(false);
        newFieldWindow.setInsetBorder(false);
        newFieldWindow.setLayout(new FitLayout());

        FormPanel formPanel = new FormPanel();
        formPanel.setFieldWidth(210);
        formPanel.setLabelWidth(70);
        formPanel.setHeaderVisible(false);
        formPanel.setBodyBorder(false);
        // label fiels
        final TextField<String> labelField = new TextField();
        labelField.setFieldLabel(getResource("cat_prop_name"));
        formPanel.add(labelField);

        // value field
        final TextField<String> valueField = new TextField();
        valueField.setFieldLabel(getResource("cat_prop_value"));
        formPanel.add(valueField);

        // apply button
        Button apply = new Button(getResource("button_apply"));
        apply.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                savePropertyAsync(labelField, valueField, newFieldWindow);
            }
        });
        formPanel.addButton(apply);

        // save button
        Button save = new Button(getResource("button_save"));
        save.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                savePropertyAsync(labelField, valueField, newFieldWindow);
                newFieldWindow.close();

            }
        });
        formPanel.addButton(save);

        newFieldWindow.add(formPanel);
        newFieldWindow.setWidth(400);
        newFieldWindow.setHeight(150);
        newFieldWindow.layout();
        newFieldWindow.show();
    }

    /**
     * @param labelField
     * @param valueField
     * @param newFieldWindow
     */
    private void savePropertyAsync(TextField<String> labelField, TextField<String> valueField, Window newFieldWindow) {
        // update user properties
        List<GWTJahiaNodeProperty> gwtJahiaNodeProperties = new ArrayList<GWTJahiaNodeProperty>();
        GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
        gwtJahiaNodeProperty.setName(labelField.getRawValue());
        gwtJahiaNodeProperty.setValue(valueField.getRawValue());
        gwtJahiaNodeProperties.add(gwtJahiaNodeProperty);

        // save propertiey
        CategoryService.App.getInstance().saveProperties(gwtJahiaCategoryNode, gwtJahiaNodeProperties, new LoadCategoryAsyncCallback(this));
    }


    /**
     * Asyncall that load user properties
     */
    private class LoadCategoryAsyncCallback implements AsyncCallback {
        private PropertiesEditor propertiesEditor;

        private LoadCategoryAsyncCallback(PropertiesEditor propertiesEditor) {
            this.propertiesEditor = propertiesEditor;
        }

        public void onFailure(Throwable throwable) {
            Log.error("Can't retrieve jahia user properties", throwable);
        }

        public void onSuccess(Object o) {
            // load user properties
            if (o == null) {
                Log.error("categroy nod is null.");
            }
            Log.error("nb properties after update" + ((GWTJahiaCategoryNode) o).getCategoryProperties().size());
            categoryDetails.updatePropertiesTab((GWTJahiaCategoryNode) o);
        }


    }
}
