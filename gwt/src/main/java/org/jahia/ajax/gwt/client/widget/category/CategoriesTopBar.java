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

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.category.CategoriesManagerActions;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

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


    private List<TextToolItem> topTableSingleSelectionButtons = new ArrayList<TextToolItem>();
    private List<TextToolItem> topTableMultipleSelectionButtons = new ArrayList<TextToolItem>();
    private TextToolItem paste;

    public CategoriesTopBar(final String exportUrl, final String importUrl) {
        m_component = new ToolBar();
        m_component.setHeight(21);
        TextToolItem cut = new TextToolItem();
        TextToolItem remove = new TextToolItem();
        paste = new TextToolItem();
        Formatter.disableTextToolItem(paste);
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
                paste.setEnabled(true);
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
        if (exportUrl != null) {
            exportCategories.setIconStyle("fm-download");
            exportCategories.setText(getResource("cat_export"));
            exportCategories.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    CategoriesManagerActions.exportCategories(getLinker(), exportUrl);
                }
            });
            m_component.add(exportCategories);
        }
        if (importUrl != null) {
            importCategories.setIconStyle("fm-upload");
            importCategories.setText(getResource("cat_import"));
            importCategories.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    CategoriesManagerActions.importCategories(getLinker(), importUrl);
                }
            });
            m_component.add(importCategories);
        }
        topTableMultipleSelectionButtons.add(cut);
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
        return Messages.getResource(key);
    }
}
