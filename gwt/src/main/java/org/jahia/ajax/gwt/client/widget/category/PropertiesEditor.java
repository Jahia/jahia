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
import org.jahia.ajax.gwt.client.util.ResourceBundle;


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
        return ResourceBundle.getResource("categories_manager", "categories_manager", key);
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
