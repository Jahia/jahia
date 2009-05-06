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

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.StoreFilterField;
import com.extjs.gxt.ui.client.Style;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.widget.tripanel.LeftComponent;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.List;

/**
 * User: ktlili
 * Date: 9 oct. 2008
 * Time: 17:30:21
 */
public class CategoriesPickerLeftComponent extends LeftComponent {
    private CategoriesSearchPanel searchPanel;
    private CategoriesTree categoriesTree;
    private CategoriesList categoriesList;

    public CategoriesPickerLeftComponent(final String categoryKey, final List<GWTJahiaCategoryNode> selectedCategories, String categoryLocale, boolean autoSelectParent) {
        categoriesTree = new CategoriesTree(categoryKey, this,selectedCategories,categoryLocale,autoSelectParent);
        categoriesTree.setHeaderVisible(false);
        categoriesTree.setBodyBorder(false);
    }

    public void openAndSelectItem(Object item) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void refresh() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getSelectedItem() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Component getComponent() {
        return categoriesTree;
    }

    public void addCategories(List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes) {
        ((PickedCategoriesGrid) getLinker().getTopRightObject()).addCategories(gwtJahiaCategoryNodes);
    }

    public void removeAllCategories() {
        getLinker().getTopRightObject().clearTable();
    }

    public static String getResource(String key) {
        return Messages.getResource(key);
    }

    public void initWithLinker(BrowserLinker linker) {
        StoreFilterField filter = ((CategoryFilter) linker.getTopObject()).getFilter();
        filter.bind(categoriesTree.getStore());
    }



}
