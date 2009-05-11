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
package org.jahia.ajax.gwt.client.util.category;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.i18n.client.impl.DateRecord;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.Style;

import java.util.List;
import java.util.ArrayList;

import org.jahia.ajax.gwt.client.service.category.CategoryServiceAsync;
import org.jahia.ajax.gwt.client.service.category.CategoryService;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.widget.WorkInProgress;
import org.jahia.ajax.gwt.client.widget.category.ImportFile;
import org.jahia.ajax.gwt.client.widget.category.InfoEditor;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * User: ktlili
 * Date: 15 sept. 2008
 * Time: 16:19:38
 */
public abstract class CategoriesManagerActions {
    final static CategoryServiceAsync service = CategoryService.App.getInstance();

    /**
     * Copy category
     *
     * @param linker
     */
    public static void copy(final BrowserLinker linker) {
        final List<GWTJahiaCategoryNode> selectedItems = (List<GWTJahiaCategoryNode>) linker.getTableSelection();
        if (selectedItems != null && selectedItems.size() > 0) {
            CopyPasteEngine.getInstance().setCopiedCategories(selectedItems);
            linker.handleNewSelection();
        }
    }

    /**
     * Cut category
     *
     * @param linker
     */
    public static void cut(final BrowserLinker linker) {
        final List<GWTJahiaCategoryNode> selectedItems = (List<GWTJahiaCategoryNode>) linker.getTableSelection();
        if (selectedItems != null && selectedItems.size() > 0) {
            CopyPasteEngine.getInstance().setCutPaths(selectedItems);
            linker.handleNewSelection();
        }
    }

    /**
     * Paste selected category
     *
     * @param linker
     */
    public static void paste(final BrowserLinker linker) {
        GWTJahiaCategoryNode m = getSingleSelectedNode(linker);
        if (m != null) {
            final CopyPasteEngine copyPasteEngine = CopyPasteEngine.getInstance();
            service.paste(copyPasteEngine.getCopiedCategories(), m, copyPasteEngine.isCut(), new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Log.error("Error", throwable);
                    Window.alert("Paste failed :\n" + throwable.getLocalizedMessage());
                }

                public void onSuccess(Object o) {
                    copyPasteEngine.onPastedPath();
                    linker.refreshAll();
                }
            });
        }

    }

    private static GWTJahiaCategoryNode getSingleSelectedNode(BrowserLinker linker) {
        GWTJahiaCategoryNode m = (GWTJahiaCategoryNode) linker.getTreeSelection();
        if (m == null) {
            final List<GWTJahiaCategoryNode> selectedItems = (List<GWTJahiaCategoryNode>) linker.getTableSelection();
            if (selectedItems != null && selectedItems.size() == 1) {
                m = selectedItems.get(0);
            }
        }
        return m;
    }

    /**
     * Import categories
     *
     * @param linker
     */
    public static void importCategories(final BrowserLinker linker, String importUrl) {
        new ImportFile(linker, importUrl);
    }

    /**
     * Export categories
     *
     * @param linker
     */
    public static void exportCategories(final BrowserLinker linker, String exportUrl) {
        if (exportUrl != null) {
            Window.Location.replace(exportUrl);
        }
    }

    /**
     * Create a category
     *
     * @param linker
     */
    public static void createCategory(final BrowserLinker linker) {
        openCategoryInfoWindow(linker, true);
    }

    /**
     * Open update acl window
     *
     * @param linker
     */
    public static void openUpdateACL(final BrowserLinker linker) {
        GWTJahiaCategoryNode selectedCategory = getSingleSelectedNode(linker);

        Window.open(selectedCategory.getACLLink(), "700", "directories=no,scrollbars=yes,resizable=yes,status=no,location=no,height=768,with=1000");
    }


    /**
     * Remove category
     *
     * @param linker
     */
    public static void remove(final BrowserLinker linker) {
        final List<GWTJahiaCategoryNode> selectedItems = (List<GWTJahiaCategoryNode>) linker.getTableSelection();
        if (selectedItems != null && selectedItems.size() > 0) {
            boolean rem;
            if (selectedItems.size() == 1) {
                GWTJahiaCategoryNode selection = selectedItems.get(0);
                rem = Window.confirm("Do you really want to remove the category " + selection.getName() + " ?");
            } else {
                rem = Window.confirm("Do you really want to remove the current selection ? (" + selectedItems.size() + " items)");
            }
            if (rem) {
                List<String> selectedPaths = new ArrayList<String>(selectedItems.size());
                for (GWTJahiaCategoryNode node : selectedItems) {
                    selectedPaths.add(node.getPath());
                }
                WorkInProgress.show();
                service.deleteCategory(selectedItems, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        WorkInProgress.hide();
                        Window.alert("Deletion failed\n\n" + throwable.getLocalizedMessage());
                    }

                    public void onSuccess(Object o) {
                        linker.refreshAll();

                        WorkInProgress.hide();
                    }
                });
            }
        }
    }

    /**
     * Rename category
     *
     * @param linker
     */
    public static void updateInfo(final BrowserLinker linker) {
        openCategoryInfoWindow(linker, false);
    }

    /**
     * Open category Info window
     *
     * @param linker
     * @param newCategory
     */
    private static void openCategoryInfoWindow(BrowserLinker linker, boolean newCategory) {
        GWTJahiaCategoryNode selectedCategory = getSingleSelectedNode(linker);
        if (selectedCategory != null) {
            com.extjs.gxt.ui.client.widget.Window w = new com.extjs.gxt.ui.client.widget.Window();
            w.setModal(true);
            w.setResizable(false);
            w.setBodyBorder(false);
            w.setLayout(new FillLayout());
            if (newCategory) {
                w.setHeading(getResource("cat_create"));
            } else {
                w.setHeading(getResource("cat_update"));
            }
            w.setWidth(400);
            w.add(new InfoEditor(linker, w, selectedCategory, newCategory));
            w.setScrollMode(Style.Scroll.AUTO);
            w.layout();
            w.show();

        } else {
            com.extjs.gxt.ui.client.widget.Window w = new com.extjs.gxt.ui.client.widget.Window();
            w.setModal(true);
            w.setResizable(false);
            w.setBodyBorder(false);
            w.setLayout(new FillLayout());
            if (newCategory) {
                w.setHeading(getResource("cat_create"));
            } else {
                w.setHeading(getResource("cat_update"));
            }
            w.setWidth(400);
            GWTJahiaCategoryNode jahiaCategoryNode = new GWTJahiaCategoryNode();
            jahiaCategoryNode.setParentKey(null);
            jahiaCategoryNode.setName("root");
            jahiaCategoryNode.setKey("root");
            jahiaCategoryNode.setPath("/root");
            w.add(new InfoEditor(linker, w, jahiaCategoryNode, newCategory));
            w.setScrollMode(Style.Scroll.AUTO);
            w.layout();
            w.show();
        }
    }

    private static String getResource(String key) {
        return Messages.getResource(key);
    }

}
