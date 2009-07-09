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
import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.service.category.CategoryService;
import org.jahia.ajax.gwt.client.service.category.CategoryServiceAsync;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.tree.CustomTreeLoader;
import org.jahia.ajax.gwt.client.util.tree.PreviousPathsOpener;
import org.jahia.ajax.gwt.client.util.tree.CustomTreeBinder;

import java.util.*;

/**
 * User: ktlili
 * Date: 9 oct. 2008
 * Time: 17:00:19
 */
public class CategoriesTree extends ContentPanel {
    private MyTreeBinder<GWTJahiaCategoryNode> binder;
    private CustomTreeLoader<GWTJahiaCategoryNode> loader;
    private TreeStore<GWTJahiaCategoryNode> store;
    private PreviousPathsOpener<GWTJahiaCategoryNode> previousPathsOpener = null ;
    private Tree tree ;
    private boolean init = true ;
    private boolean autoSelectParent = true ;

    public CategoriesTree(final String categoryKey, final List<GWTJahiaCategoryNode> selectedCategories, final String categoryLocale, boolean autoSelectParent, final boolean multiple) {
        super(new FitLayout());
        this.autoSelectParent = autoSelectParent;

        setHeading(Messages.getResource("categories"));
        setScrollMode(Style.Scroll.AUTO);

        final CategoryServiceAsync service = CategoryService.App.getInstance();
        // data proxy
        RpcProxy<List<GWTJahiaCategoryNode>> proxy = new RpcProxy<List<GWTJahiaCategoryNode>>() {
            protected void load(Object gwtJahiaCategoryNode, AsyncCallback<List<GWTJahiaCategoryNode>> listAsyncCallback) {
                if (init) {
                    service.lsInit(categoryKey, selectedCategories, categoryLocale, listAsyncCallback);
                } else {
                    service.ls((GWTJahiaCategoryNode) gwtJahiaCategoryNode, categoryLocale, listAsyncCallback);
                }
            }
        };
        // tree loader
        loader = new CustomTreeLoader<GWTJahiaCategoryNode>(proxy) {
            @Override
            public boolean hasChildren(GWTJahiaCategoryNode parent) {
                return !parent.isLeaf();
            }

            protected void expandPreviousPaths() {
               expandAllPreviousPaths();
            }

//            @Override
//            protected void onLoadSuccess(Object gwtJahiaCategoryNode, List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes) {
//                super.onLoadSuccess(gwtJahiaCategoryNode, gwtJahiaCategoryNodes);
//                Log.debug("Load "+gwtJahiaCategoryNodes.size()+" categories.");
//                if (init) {
//                    init = false ;
//                } else {
//                    for (GWTJahiaCategoryNode n: gwtJahiaCategoryNodes) {
//                    //    n.setParent(gwtJahiaCategoryNode);
//                    }
//                   // gwtJahiaCategoryNode.setChildren(gwtJahiaCategoryNodes);
//                }
//            }
        };

        // trees store
        store = new TreeStore<GWTJahiaCategoryNode>(loader);
        store.setModelComparer(new ModelComparer<GWTJahiaCategoryNode>() {
            public boolean equals (GWTJahiaCategoryNode gwtJahiaCategoryNode, GWTJahiaCategoryNode gwtJahiaCategoryNode1) {
                return gwtJahiaCategoryNode != null && gwtJahiaCategoryNode1 != null && gwtJahiaCategoryNode.getCategoryId().equals(gwtJahiaCategoryNode1.getCategoryId());
            }
        });
        // tree
        tree = new Tree();
        if (multiple) {
            tree.setSelectionMode(Style.SelectionMode.MULTI);
        }

        binder = new MyTreeBinder<GWTJahiaCategoryNode>(tree, store);
        binder.setDisplayProperty("extendedName");

        add(tree);
    }

    /**
     * Get treeStrore object
     * @return
     */
    public TreeStore<GWTJahiaCategoryNode> getStore() {
        return store;
    }

    /**
     * init
     */
    public void init() {
        loader.load();
    }

    private void expandAllPreviousPaths() {
        if (previousPathsOpener == null) {
            previousPathsOpener = new PreviousPathsOpener<GWTJahiaCategoryNode>(tree, store, binder) ;
        }
        previousPathsOpener.expandPreviousPaths();
    }

    /**
     * Get selected categories
     * @return
     */
    public List<GWTJahiaCategoryNode> getSelection() {
        if (!autoSelectParent) {
            return binder.getSelection() ;
        }
        Set<GWTJahiaCategoryNode> nodes = new HashSet<GWTJahiaCategoryNode>(binder.getSelection());
        for (GWTJahiaCategoryNode n: binder.getSelection()) {
            GWTJahiaCategoryNode parent = (GWTJahiaCategoryNode) n.getParent();
            while (parent != null) {
                if("root".equals(parent.getKey())) {
                    break;
                }
                nodes.add(parent);
                parent = (GWTJahiaCategoryNode) parent.getParent();
            }
        }
        return new ArrayList<GWTJahiaCategoryNode>(nodes);
    }


    /**
     * TreeBinder that has renderChildren public.
     *
     * @param <M>
     */
    private class MyTreeBinder<M extends BaseTreeModel> extends TreeBinder<M> implements CustomTreeBinder<M> {

        public MyTreeBinder(Tree t, TreeStore<M> s) {
            super(t, s) ;
        }

        public void renderChildren(M parent, List<M> children) {
            try {
                super.renderChildren(parent, children);
            } catch (ConcurrentModificationException e) {
                // weird harmless exception
                Log.error(e.getMessage(),e);
            }
        }

    }

}
