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

package org.jahia.ajax.gwt.subengines.categoriespicker.client.component;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.engines.categories.client.model.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.engines.categories.client.service.CategoryService;
import org.jahia.ajax.gwt.subengines.categoriespicker.client.CategoriesPickerPanel;

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
        final CategoriesPickerPanel catPicker = new CategoriesPickerPanel(selectedCategories, false, null, null);
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
