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
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.service.category.CategoryService;
import org.jahia.ajax.gwt.client.widget.category.CategoriesPickerPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 28, 2008
 * Time: 11:17:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class CategoryField extends TriggerField<String> {
    public CategoryField() {
    }

    @Override
    protected void onTriggerClick(ComponentEvent ce) {
        super.onTriggerClick(ce);
        if (disabled || isReadOnly()) {
            return;
        }

        String s = getRawValue();
        if (s == null || s.equals("")) {
            displayPicker(new ArrayList<GWTJahiaCategoryNode>());
        } else {
            CategoryService.App.getInstance().getCategories(Arrays.asList(s.split(",")), new AsyncCallback<List<GWTJahiaCategoryNode>>() {
                public void onSuccess(List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes) {
                    displayPicker(gwtJahiaCategoryNodes);
                }

                public void onFailure(Throwable throwable) {
                    Log.error("error", throwable);
                }
            });
        }

    }

    private void displayPicker(List<GWTJahiaCategoryNode> selectedCategories) {
        final CategoriesPickerPanel catPicker = new CategoriesPickerPanel(selectedCategories, false, null, null, null);
        final Window w = new Window();
        w.setLayout(new FitLayout());
        w.setModal(true);
        w.setSize(600, 400);
        ButtonBar bar = new ButtonBar();
        Button ok = new Button("OK", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                List<GWTJahiaCategoryNode> selection = ((PickedCategoriesGrid)catPicker.getLinker().getTopRightObject()).getCategories();
                if (selection != null && selection.size() > 0) {
                    StringBuilder conCat = new StringBuilder(selection.get(0).getKey());
                    for (int i = 1; i < selection.size(); i++) {
                        conCat.append(",").append(selection.get(i).getKey());
                    }
                    setRawValue(conCat.toString());
                }
                w.hide();
            }
        });
        bar.add(ok);
        w.setButtonBar(bar);
        w.add(catPicker);
        w.show();
    }

}
