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

package org.jahia.ajax.gwt.engines.categories.client.components;

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
import org.jahia.ajax.gwt.engines.categories.client.model.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.engines.categories.client.model.GWTCategoryTitle;
import org.jahia.ajax.gwt.engines.categories.client.service.CategoryService;
import org.jahia.ajax.gwt.engines.categories.client.CategoriesManagerEntryPoint;
import org.jahia.ajax.gwt.tripanelbrowser.client.BrowserLinker;


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
        final List<GWTCategoryTitle> categoryTitles = gwtJahiaCategoryNode.getCategoryTitles();

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
                for (GWTCategoryTitle gwtCategoryTitle : categoryTitles) {
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
                List<GWTCategoryTitle> gwtCategoryTitles = new ArrayList<GWTCategoryTitle>();
                for (TextField field : textFields) {
                    GWTCategoryTitle gwtCategoryTitle = new GWTCategoryTitle();
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
                                Log.error("Can't update jahia user properties", throwable);
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
        return CategoriesManagerEntryPoint.getResource(key);
    }
}