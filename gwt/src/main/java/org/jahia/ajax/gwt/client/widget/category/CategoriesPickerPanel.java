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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.widget.tripanel.TriPanelBrowserLayout;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;
import org.jahia.ajax.gwt.client.widget.tripanel.LeftComponent;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

import java.util.List;

/**
 * User: ktlili
 * Date: 9 oct. 2008
 * Time: 17:26:46
 */
public class CategoriesPickerPanel extends TriPanelBrowserLayout {

    public CategoriesPickerPanel(final List<GWTJahiaCategoryNode> selectedCategories, final boolean readonly, final String rootKey, String categoryLocale, String autoSelectParent, boolean isMultiple) {
        super();
        setBorders(false);
        setBodyBorder(false);
        setWidth("100%");
        setHeight("400px");
        setWestData(new BorderLayoutData(Style.LayoutRegion.WEST, 300));

        // construction of the UI components
        TopRightComponent treeTable = new PickedCategoriesGrid(selectedCategories, readonly, isMultiple);
        LeftComponent selectorsLeftComponent = null;
        Component leftComponent = null;
        boolean isAutoSelectParent = false;
        if(autoSelectParent!=null && !"".equalsIgnoreCase(autoSelectParent.trim())){
            isAutoSelectParent = Boolean.valueOf(autoSelectParent);
        }
        if (isAutoSelectParent && !isMultiple) {
            isAutoSelectParent = false;
        }
        if (!readonly) {
            selectorsLeftComponent = new CategoriesPickerLeftComponent(rootKey,selectedCategories,categoryLocale,isAutoSelectParent, isMultiple, readonly);
            leftComponent = selectorsLeftComponent.getComponent();
            leftComponent.setWidth("400px");
        }
        BottomRightComponent tabs = null;

        // setup widgets in layout
        initWidgets(leftComponent,
                treeTable.getComponent(),
                null,
                null,
                null);

        // linker initializations
        linker.registerComponents(selectorsLeftComponent, treeTable, tabs, null, null);
        treeTable.initContextMenu();
        linker.handleNewSelection();
    }


}
