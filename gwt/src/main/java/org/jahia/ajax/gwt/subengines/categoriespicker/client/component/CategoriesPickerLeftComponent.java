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

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.Style;
import org.jahia.ajax.gwt.engines.categories.client.model.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.subengines.categoriespicker.client.CategoriesPickerEntryPoint;
import org.jahia.ajax.gwt.tripanelbrowser.client.components.LeftComponent;

import java.util.List;

/**
 * User: ktlili
 * Date: 9 oct. 2008
 * Time: 17:30:21
 */
public class CategoriesPickerLeftComponent extends LeftComponent {
    private TabPanel m_component;
    private CategoriesSearchPanel searchPanel;
    private CategoriesTree categoriesTree;
    private CategoriesList categoriesList;

    public CategoriesPickerLeftComponent (final String categoryKey, final List<GWTJahiaCategoryNode> selectedCategories, String categoryLocale) {
        categoriesTree = new CategoriesTree(categoryKey, this,selectedCategories,categoryLocale);
        categoriesTree.setHeaderVisible(false);
        categoriesTree.setBodyBorder(false);

        /*categoriesList = new CategoriesList(this);
        categoriesList.setHeaderVisible(false);
        categoriesList.setBodyBorder(false);

        searchPanel = new CategoriesSearchPanel(this);
        searchPanel.setHeaderVisible(false);
        searchPanel.setBodyBorder(false);*/

        TabItem cTreeTabItem = new TabItem(getResource("categories"));
        cTreeTabItem.add(categoriesTree);
        cTreeTabItem.setScrollMode(Style.Scroll.AUTO);
        /*TabItem listTabItem = new TabItem(getResource("List"));
        listTabItem.add(categoriesList);

        TabItem searchTabItem = new TabItem(getResource("Search"));
        searchTabItem.add(searchPanel);*/

        m_component = new TabPanel();
        m_component.add(cTreeTabItem);
       /* m_component.add(listTabItem);
        m_component.add(searchTabItem);*/
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

    private static String getResource(String key) {
        return CategoriesPickerEntryPoint.getResource(key);
    }
}
