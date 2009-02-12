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

package org.jahia.ajax.gwt.engines.categories.client.components;

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.commons.client.util.Formatter;
import org.jahia.ajax.gwt.engines.categories.client.CategoriesManagerEntryPoint;
import org.jahia.ajax.gwt.engines.categories.client.model.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.engines.categories.client.util.CategoriesManagerActions;
import org.jahia.ajax.gwt.tripanelbrowser.client.components.TopBar;
import org.jahia.ajax.gwt.tripanelbrowser.client.components.TopRightComponent;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

/**
 * User: ktlili
 * Date: 19 sept. 2008
 * Time: 16:09:29
 */
public class CategoriesTopBar extends TopBar {

    private ToolBar m_component;
    private TopRightComponent current;


    private List<TextToolItem> topTableSingleSelectionButtons = new ArrayList<TextToolItem>();
    private List<TextToolItem> topTableMultipleSelectionButtons = new ArrayList<TextToolItem>();
    private TextToolItem paste;

    public CategoriesTopBar(final TopRightComponent manager) {
        current = manager;
        m_component = new ToolBar();
        m_component.setHeight(21);
        TextToolItem cut = new TextToolItem();
        TextToolItem remove = new TextToolItem();
        paste = new TextToolItem();
        TextToolItem exportCategories = new TextToolItem();
        TextToolItem importCategories = new TextToolItem();
        TextToolItem newCategory = new TextToolItem();
        TextToolItem updateInfo = new TextToolItem();
        TextToolItem updateACL = new TextToolItem();

        // new category
        newCategory.setIconStyle("fm-newfolder");
        newCategory.setText(getResource("cat_create"));
        newCategory.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                CategoriesManagerActions.createCategory(getLinker());
            }
        });
        m_component.add(newCategory);

        // remove
        remove.setIconStyle("fm-remove");
        remove.setText(getResource("cat_remove"));
        remove.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                CategoriesManagerActions.remove(getLinker());
            }
        });
        m_component.add(remove);

        // update
        updateInfo.setIconStyle("fm-rename");
        updateInfo.setText(getResource("cat_update"));
        updateInfo.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                CategoriesManagerActions.updateInfo(getLinker());
            }
        });
        m_component.add(updateInfo);
        m_component.add(new SeparatorToolItem());

        // cut
        cut.setIconStyle("fm-cut");
        cut.setText(getResource("cat_cut"));
        cut.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                CategoriesManagerActions.cut(getLinker());
            }
        });
        m_component.add(cut);

        // paste
        paste.setIconStyle("fm-paste");
        paste.setText(getResource("cat_paste"));
        paste.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                CategoriesManagerActions.paste(getLinker());
            }
        });
        m_component.add(paste);
        m_component.add(new SeparatorToolItem());

        exportCategories.setIconStyle("fm-download");
        exportCategories.setText(getResource("cat_export"));
        exportCategories.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                CategoriesManagerActions.exportCategories(getLinker());
            }
        });
        m_component.add(exportCategories);
        importCategories.setIconStyle("fm-upload");
        importCategories.setText(getResource("cat_import"));
        importCategories.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                CategoriesManagerActions.importCategories(getLinker());
            }
        });
        m_component.add(importCategories);

        topTableMultipleSelectionButtons.add(cut);
        topTableMultipleSelectionButtons.add(paste);
        topTableMultipleSelectionButtons.add(remove);

        topTableSingleSelectionButtons.add(newCategory);
        topTableSingleSelectionButtons.add(updateInfo);
        topTableSingleSelectionButtons.add(updateACL);

        // nothing is selected at init
        handleNewSelection(null,null);
    }

    /**
     * Handle new selection
     *
     * @param leftTreeSelection
     * @param topTableSelectionEl
     */
    public void handleNewSelection(Object leftTreeSelection, Object topTableSelectionEl) {
        List<GWTJahiaCategoryNode> topTableSelection = (List<GWTJahiaCategoryNode>) topTableSelectionEl;
        if (topTableSelection != null) {
            // first activate all buttons
            for (TextToolItem ti : topTableSingleSelectionButtons) {
                Formatter.enableTextToolItem(ti);
            }
            for (TextToolItem ti : topTableMultipleSelectionButtons) {
                Formatter.enableTextToolItem(ti);
            }

            // handle multiple selection
            if (topTableSelection.size() > 1) {
                for (TextToolItem ti : topTableMultipleSelectionButtons) {
                    Formatter.enableTextToolItem(ti);
                }

                for (TextToolItem ti : topTableSingleSelectionButtons) {
                    Formatter.disableTextToolItem(ti);
                }
            }
            // handle single selection
            else if (topTableSelection.size() == 1) {
                for (TextToolItem ti : topTableMultipleSelectionButtons) {
                    Formatter.enableTextToolItem(ti);
                }
                for (TextToolItem ti : topTableSingleSelectionButtons) {
                    Formatter.enableTextToolItem(ti);
                }
            }

            // check if one of the selected categories is only read access
            for (GWTJahiaCategoryNode gwtJahiaCategoryNode : topTableSelection) {
                if (!gwtJahiaCategoryNode.isWriteable()) {
                    for (TextToolItem ti : topTableSingleSelectionButtons) {
                        Formatter.disableTextToolItem(ti);
                    }
                    for (TextToolItem ti : topTableMultipleSelectionButtons) {
                        Formatter.disableTextToolItem(ti);
                    }
                    break;
                }
            }

        } else if (leftTreeSelection != null) {
            for (TextToolItem ti : topTableMultipleSelectionButtons) {
                Formatter.disableTextToolItem(ti);
            }

            for (TextToolItem ti : topTableSingleSelectionButtons) {
                Formatter.disableTextToolItem(ti);
            }

            // nothing is selected
        } else {
            for (TextToolItem ti : topTableMultipleSelectionButtons) {
                Formatter.disableTextToolItem(ti);
            }

            for (TextToolItem ti : topTableSingleSelectionButtons) {
                Formatter.disableTextToolItem(ti);
            }
        }
    }

    /**
     * Get main componet
     *
     * @return
     */
    public Component getComponent() {
        return m_component;
    }

    private String getResource(String key) {
        return CategoriesManagerEntryPoint.getResource(key);
    }
}
