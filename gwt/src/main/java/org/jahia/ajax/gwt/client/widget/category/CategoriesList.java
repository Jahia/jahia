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
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.service.category.CategoryService;
import org.jahia.ajax.gwt.client.service.category.CategoryServiceAsync;
import org.jahia.ajax.gwt.client.messages.Messages;

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

    private String getResource(String key) {
        return Messages.getResource(key);
    }


}

