/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.category;

import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ComponentEvent;
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

    public CategoriesPickerLeftComponent(final String categoryKey, final List<GWTJahiaCategoryNode> selectedCategories, String categoryLocale, boolean autoSelectParent, boolean multiple, final boolean readonly) {
        m_component = new ContentPanel(new BorderLayout()) ;
        m_component.setBodyBorder(false);
        m_component.setBorders(false);
        m_component.setHeaderVisible(false);

//        categoryFilter = new CategoryFilter() ;

        categoriesTree = new CategoriesTree(categoryKey, selectedCategories, categoryLocale, autoSelectParent, multiple);
        categoriesTree.setHeaderVisible(false);
        categoriesTree.setBodyBorder(false);

//        StoreFilterField filter = categoryFilter.getFilter();
//        filter.bind(categoriesTree.getStore());

//        m_component.setTopComponent(categoryFilter.getComponent());

        Button add = new Button(">>", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent componentEvent) {
                addCategories(categoriesTree.getSelection());
            }
        });
        Button remove = new Button("<<", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent componentEvent) {
                removeCategories();
            }
        });
        if (readonly) {
            add.setEnabled(false);
            remove.setEnabled(false);
        }

        VerticalPanel panel = new VerticalPanel();
        panel.setVerticalAlign(Style.VerticalAlignment.MIDDLE);
        panel.add(add);
        panel.add(remove);
        LayoutContainer buttons = new LayoutContainer(new CenterLayout());
        buttons.add(panel);
        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.EAST, 30, 30, 30);
        data.setFloatable(false);
        data.setSplit(false);
        m_component.add(buttons, data);
        data = new BorderLayoutData(Style.LayoutRegion.CENTER);
        data.setSplit(false);
        m_component.add(categoriesTree, data);
    }

    public void initWithLinker(BrowserLinker linker) {
        super.initWithLinker(linker);
        categoriesTree.init();
    }

    public void openAndSelectItem(Object item) {}

    public void refresh() {}

    public Object getSelectedItem() {
        return getLinker().getTopRightObject().getSelection();
    }

    public Component getComponent() {
        return m_component;
    }

    public void addCategories(List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes) {
        ((PickedCategoriesGrid) getLinker().getTopRightObject()).addCategories(gwtJahiaCategoryNodes);
    }

    public void removeCategories() {
        ((PickedCategoriesGrid) getLinker().getTopRightObject()).removeSelectedCategories();
    }

}
