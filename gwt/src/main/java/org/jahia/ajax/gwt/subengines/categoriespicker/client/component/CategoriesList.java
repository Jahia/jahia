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
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.binder.DataListBinder;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.engines.categories.client.model.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.engines.categories.client.service.CategoryServiceAsync;
import org.jahia.ajax.gwt.engines.categories.client.service.CategoryService;
import org.jahia.ajax.gwt.subengines.categoriespicker.client.CategoriesPickerEntryPoint;

import java.util.List;

/**
 * User: ktlili
 * Date: 6 nov. 2008
 * Time: 16:19:10
 */
public class CategoriesList extends ContentPanel {
    private DataListBinder<GWTJahiaCategoryNode> binder;
    private CategoriesPickerLeftComponent parentComponent;

    public CategoriesList(CategoriesPickerLeftComponent parentComponent) {
        this.parentComponent = parentComponent;
    }

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
        setHeading(getResource("categories"));
        setScrollMode(Style.Scroll.AUTO);

        final CategoryServiceAsync service = CategoryService.App.getInstance();

        // trees store
        final ListStore<GWTJahiaCategoryNode> store = new ListStore<GWTJahiaCategoryNode>();

        // tree
        final DataList list = new DataList();
        list.setBorders(false);

        binder = new DataListBinder<GWTJahiaCategoryNode>(list, store);
        binder.setDisplayProperty("name");

        ToolBar bar = new ToolBar();
        TextToolItem selectToolItem = new TextToolItem("");
        selectToolItem.setToolTip(getResource("add"));
        selectToolItem.setIconStyle("gwt-categoriespiker-icon-select");
        selectToolItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                parentComponent.addCategories(binder.getSelection());
            }
        });

        // filter
        final TextField filter = new TextField<GWTJahiaCategoryNode>();
        filter.addKeyListener(new KeyListener() {
            @Override
            public void componentKeyUp(ComponentEvent event) {
                super.componentKeyUp(event);
                if (filter.getRawValue() != null && filter.getRawValue().length() > 3) {
                    service.searchByTitle(filter.getRawValue(), null, 100, new AsyncCallback<List<GWTJahiaCategoryNode>>() {
                        public void onSuccess(List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes) {
                            store.removeAll();
                            store.add(gwtJahiaCategoryNodes);
                            Log.debug("Find " + gwtJahiaCategoryNodes.size() + " categories. for title "+filter.getRawValue());
                        }

                        public void onFailure(Throwable throwable) {
                            store.removeAll();
                            Log.error("Error while loading categories", throwable);
                        }
                    });
                }

            }
        });
        bar.add(new AdapterToolItem(filter));
        bar.add(new FillToolItem());
        bar.add(selectToolItem);

        // loader.load(null);
        add(bar);
        add(list);
    }

    private static String getResource(String key) {
        return CategoriesPickerEntryPoint.getResource(key);
    }


}

