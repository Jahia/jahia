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

import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.widget.tripanel.LeftComponent;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;

import java.util.List;

/**
 * User: ktlili
 * Date: 9 oct. 2008
 * Time: 17:30:21
 */
public class CategoriesPickerLeftComponent extends LeftComponent {
    private ContentPanel m_component;
//    private CategoryFilter categoryFilter;
    private CategoriesTree categoriesTree;

    public CategoriesPickerLeftComponent(final String categoryKey, final List<GWTJahiaCategoryNode> selectedCategories, String categoryLocale, boolean autoSelectParent) {
        m_component = new ContentPanel(new FitLayout()) ;
        m_component.setBodyBorder(false);
        m_component.setBorders(false);
        m_component.setHeaderVisible(false);

//        categoryFilter = new CategoryFilter() ;

        categoriesTree = new CategoriesTree(categoryKey, this,selectedCategories,categoryLocale,autoSelectParent);
        categoriesTree.setHeaderVisible(false);
        categoriesTree.setBodyBorder(false);

//        StoreFilterField filter = categoryFilter.getFilter();
//        filter.bind(categoriesTree.getStore());

//        m_component.setTopComponent(categoryFilter.getComponent());
        m_component.add(categoriesTree);
    }

    public void initWithLinker(BrowserLinker linker) {
        super.initWithLinker(linker);
        categoriesTree.init();
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
        return m_component;
    }

    public void addCategories(List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes) {
        ((PickedCategoriesGrid) getLinker().getTopRightObject()).addCategories(gwtJahiaCategoryNodes);
    }

    public void removeAllCategories() {
        getLinker().getTopRightObject().clearTable();
    }

}
