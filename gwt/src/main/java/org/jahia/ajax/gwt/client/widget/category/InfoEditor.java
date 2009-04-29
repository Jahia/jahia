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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.Style;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryTitle;
import org.jahia.ajax.gwt.client.service.category.CategoryService;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.messages.Messages;


public class InfoEditor extends FormPanel {
    private BrowserLinker linker;
    private GWTJahiaCategoryNode gwtJahiaCategoryNode;
    private boolean newCategory;
    private Window window;

    public InfoEditor(BrowserLinker linker, Window window, GWTJahiaCategoryNode gwtJahiaCategoryNode, boolean newCategory) {
        this.linker = linker;
        this.window = window;
        this.gwtJahiaCategoryNode = gwtJahiaCategoryNode;
        this.newCategory = newCategory;

        createUI();
    }

    private void createUI() {
        updateUI(gwtJahiaCategoryNode);
    }

    /**
     * Refresh my Settings panel
     */
    public void updateUI(final GWTJahiaCategoryNode gwtJahiaCategoryNode) {
        this.gwtJahiaCategoryNode = gwtJahiaCategoryNode;
        final List<GWTJahiaCategoryTitle> categoryTitles = gwtJahiaCategoryNode.getCategoryTitles();

        //removeAll();
        // creat a form panel
        setBodyBorder(false);
        setFrame(false);
        setAutoHeight(true);
        setHeaderVisible(false);
        setLayout(new FlowLayout());
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setStyleAttribute("padding", "4");

        final FieldSet fieldSet = creatFieldSet();
        fieldSet.setHeading(getResource("General"));
        final TextField selectedKeyField = new TextField();
        selectedKeyField.setName("parentCategoryKey");
        if (newCategory) {
            selectedKeyField.setFieldLabel(getResource("cat_parentkey"));
        } else {
            selectedKeyField.setFieldLabel(getResource("cat_key"));
        }
        selectedKeyField.setValue(gwtJahiaCategoryNode.getKey());
        selectedKeyField.setEnabled(false);
        fieldSet.add(selectedKeyField);

        // new caetgory key
        final TextField newCategoryKeyField = new TextField();
        newCategoryKeyField.setName("newCategoryKey");
        newCategoryKeyField.setFieldLabel(getResource("cat_key"));
        newCategoryKeyField.setAllowBlank(false);
        newCategoryKeyField.setMaxLength(200);
        if (newCategory) {
            fieldSet.add(newCategoryKeyField);
        }
        add(fieldSet);

        // add a text fiedl for each property
        final FieldSet titleFieldSet = creatFieldSet();
        titleFieldSet.setHeading(getResource("cat_titles"));
        final List<TextField> textFields = new ArrayList<TextField>();
        if (gwtJahiaCategoryNode != null) {
            if (categoryTitles != null) {
                for (GWTJahiaCategoryTitle gwtCategoryTitle : categoryTitles) {
                    // create a text field
                    final TextField textField = new TextField();
                    textField.setName(gwtCategoryTitle.getLocale());
                    textField.setFieldLabel(gwtCategoryTitle.getLocale());
                    textField.setValue(gwtCategoryTitle.getTitleValue());
                    titleFieldSet.add(textField);
                    textFields.add(textField);
                }
            }
        }
        titleFieldSet.recalculate();
        add(titleFieldSet);

        // save property listener
        SelectionListener saveOrUpdateCategoriesInfo = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent ce) {
                // create title list
                List<GWTJahiaCategoryTitle> gwtCategoryTitles = new ArrayList<GWTJahiaCategoryTitle>();
                for (TextField field : textFields) {
                    GWTJahiaCategoryTitle gwtCategoryTitle = new GWTJahiaCategoryTitle();
                    gwtCategoryTitle.setLocale(field.getName());
                    gwtCategoryTitle.setTitleValue(field.getRawValue());
                    gwtCategoryTitles.add(gwtCategoryTitle);
                }

                if (newCategory) {
                    if (newCategoryKeyField.validate()) {
                        // create categegory
                        GWTJahiaCategoryNode newGWTJahiaCategoryNode = new GWTJahiaCategoryNode();
                        newGWTJahiaCategoryNode.setKey(newCategoryKeyField.getRawValue());
                        newGWTJahiaCategoryNode.setCategoryTitles(gwtCategoryTitles);
                        CategoryService.App.getInstance().createCategory(gwtJahiaCategoryNode, newGWTJahiaCategoryNode, new AsyncCallback() {
                            public void onFailure(Throwable throwable) {
                                MessageBox.alert("",throwable.getMessage(),null);
                                window.close();
                            }

                            public void onSuccess(Object o) {
                                window.close();
                                linker.refreshAll();
                            }
                        });
                    }
                } else {
                    // update categegory
                    gwtJahiaCategoryNode.setCategoryTitles(gwtCategoryTitles);
                    CategoryService.App.getInstance().updateCategoryInfo(gwtJahiaCategoryNode, new AsyncCallback() {
                        public void onFailure(Throwable throwable) {
                            Log.error("Can't update jahia categories properties", throwable);
                            window.close();
                        }

                        public void onSuccess(Object o) {
                            window.close();
                            linker.refreshAll();
                        }
                    });
                }
            }
        };


        // save properties button
        Button saveButton = new Button(getResource("button_save"));
        saveButton.addSelectionListener(saveOrUpdateCategoriesInfo);
        addButton(saveButton);

        // remove all

        layout();
    }

    /**
     * Create a fiels set
     *
     * @return
     */
    private FieldSet creatFieldSet() {
        // general info
        final FormLayout layout = new FormLayout();
        layout.setLabelWidth(75);
        layout.setPadding(4);
        FieldSet fieldSet = new FieldSet();
        fieldSet.setLayout(layout);
        fieldSet.setStyleAttribute("padding", "4");
        return fieldSet;
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
}