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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.widget.category.CategoriesPickerLeftComponent;
import org.jahia.ajax.gwt.client.widget.category.PickedCategoriesGrid;
import org.jahia.ajax.gwt.client.widget.tripanel.TriPanelBrowserLayout;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;
import org.jahia.ajax.gwt.client.widget.tripanel.LeftComponent;
import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

import java.util.List;

/**
 * User: ktlili
 * Date: 9 oct. 2008
 * Time: 17:26:46
 */
public class CategoriesPickerPanel extends TriPanelBrowserLayout {

    public CategoriesPickerPanel(final List<GWTJahiaCategoryNode> selectedCategories, final boolean readonly, final String rootKey, String categoryLocale, String autoSelectParent) {
        super();
        setBorders(false);
        setBodyBorder(false);
        setWidth("100%");
        setHeight("400px");
        setWestData(new BorderLayoutData(Style.LayoutRegion.WEST, 300));

        // construction of the UI components
        TopRightComponent treeTable = new PickedCategoriesGrid(selectedCategories, readonly);
        LeftComponent selectorsLeftComponent = null;
        Component leftComponent = null;
        boolean isAutoSelectParent = true;
        if(autoSelectParent!=null && !"".equalsIgnoreCase(autoSelectParent.trim()))
        isAutoSelectParent = Boolean.valueOf(autoSelectParent);
        if (!readonly) {
            selectorsLeftComponent = new CategoriesPickerLeftComponent(rootKey,selectedCategories,categoryLocale,isAutoSelectParent);
            leftComponent = selectorsLeftComponent.getComponent();
            leftComponent.setWidth("400px");
        }
        BottomRightComponent tabs = null;
        TopBar toolbar = null;

        // setup widgets in layout
        initWidgets(leftComponent,
                treeTable.getComponent(),
                null,
                null,
                null);

        // linker initializations
        linker.registerComponents(selectorsLeftComponent, treeTable, tabs, toolbar, null);
        treeTable.initContextMenu();
        linker.handleNewSelection();
    }


}
