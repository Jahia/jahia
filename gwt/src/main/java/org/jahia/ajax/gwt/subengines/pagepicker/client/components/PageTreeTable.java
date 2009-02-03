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

package org.jahia.ajax.gwt.subengines.pagepicker.client.components;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.table.CellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.extjs.gxt.ui.client.widget.treetable.TreeTable;
import com.extjs.gxt.ui.client.widget.treetable.TreeTableColumnModel;
import com.extjs.gxt.ui.client.widget.treetable.TreeTableColumn;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.binder.TreeTableBinder;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.commons.client.beans.GWTJahiaPageWrapper;
import org.jahia.ajax.gwt.commons.client.rpc.JahiaContentServiceAsync;
import org.jahia.ajax.gwt.commons.client.rpc.JahiaContentService;
import org.jahia.ajax.gwt.commons.client.ui.TreeOpener;
import org.jahia.ajax.gwt.config.client.JahiaGWTParameters;
import org.jahia.ajax.gwt.tripanelbrowser.client.BrowserLinker;
import org.jahia.ajax.gwt.tripanelbrowser.client.components.TopRightComponent;

import java.util.List;
import java.util.ArrayList;

/**
 * Site/pages explorer
 */
public class PageTreeTable extends TopRightComponent {

    private LayoutContainer m_component ;
    private TreeLoader<GWTJahiaPageWrapper> loader ;
    private TreeTable m_treeTable ;
    private TreeTableStore<GWTJahiaPageWrapper> store ;
    private TreeTableBinder<GWTJahiaPageWrapper> binder ;

    private TreeItem lastSelection = null ;

    public PageTreeTable(final int homePageID, final int siteID, final String operation, String pagePath, final String parentPath) {
        m_component = new LayoutContainer(new FitLayout()) ;
        final JahiaContentServiceAsync service = JahiaContentService.App.getInstance() ;
        Log.debug("start treetable");
        // data proxy
        RpcProxy<GWTJahiaPageWrapper, List<GWTJahiaPageWrapper>> proxy = new RpcProxy<GWTJahiaPageWrapper, List<GWTJahiaPageWrapper>>() {
            @Override
            protected void load(GWTJahiaPageWrapper parentPage, final AsyncCallback<List<GWTJahiaPageWrapper>> listAsyncCallback) {
                Log.debug("load");
                if (parentPage == null) {
                    if (homePageID != -1) {
                        service.getSubPagesForCurrentUser(homePageID, listAsyncCallback);
                    } else if (siteID != -1){
                        service.getSiteHomePage(siteID, new AsyncCallback<GWTJahiaPageWrapper>() {
                            public void onFailure(Throwable throwable) {
                                listAsyncCallback.onFailure(throwable);
                            }

                            public void onSuccess(GWTJahiaPageWrapper gwtJahiaPageWrapper) {
                                List<GWTJahiaPageWrapper> l = new ArrayList<GWTJahiaPageWrapper>();
                                l.add(gwtJahiaPageWrapper);
                                listAsyncCallback.onSuccess(l);
                            }
                        });
                    } else {
                        service.getSubPagesForCurrentUser(null, listAsyncCallback);
                    }
                } else {
                    service.getSubPagesForCurrentUser(parentPage, listAsyncCallback);
                }
            }

        };

        // tree loader
        loader = new BaseTreeLoader<GWTJahiaPageWrapper>(proxy) {
            @Override
            public boolean hasChildren(GWTJahiaPageWrapper parent) {
                return parent.hasChildren() ;
            }
        };

        pagePath = "/0"+pagePath;
        // tree store
        store = new TreeTableStore<GWTJahiaPageWrapper>(loader, pagePath) ;

        m_treeTable = new TreeTable(getHeaders());
        m_treeTable.setHorizontalScroll(true);

        binder = new TreeTableBinder<GWTJahiaPageWrapper>(m_treeTable, store) ;
        binder.init() ;
        binder.setCaching(true);
        binder.setDisplayProperty("title");

        binder.setIconProvider(new ModelStringProvider<GWTJahiaPageWrapper>() {
            public String getStringValue(GWTJahiaPageWrapper node, String property) {
                if (!node.isSiteRoot()) {
                    if (operation.equals("movePage")) {
                        if (parentPath.contains("/"+node.getPid()+"/") || parentPath.endsWith("/" + node.getParentPid() + "/")) {
                            Log.debug("in path:"+node.getPid()+"/"+property);
                            return "gwt-pagepicker-icon-inpath";
                        }
                        if (node.isLocked()) {
                            return "gwt-pagepicker-icon-locked";
                        }
                    }
                    return "icon-page" ;
                } else {
                    return null;
                }
            }
        });

        m_treeTable.addListener(Events.SelectionChange, new Listener() {
            public void handleEvent(BaseEvent event) {
                TreeItem newSelection = m_treeTable.getSelectedItem() ;
                if (lastSelection != newSelection) {
                    lastSelection = newSelection ;
                    getLinker().onTableItemSelected();
                }
            }
        });

//        if ("movePage".equals(operation)) {
//            m_treeTable.addListener(Events.BeforeExpand, new Listener() {
//                public void handleEvent(BaseEvent event) {
//                    TreeEvent treeEvent = (TreeEvent) event;
//                    List<TreeItem> list = treeEvent.item.getItems();
//                    for (Iterator<TreeItem> treeItemIterator = list.iterator(); treeItemIterator.hasNext();) {
//                        TreeItem treeItem = treeItemIterator.next();
//                        if (parentPath.contains("/"+((GWTJahiaPageWrapper)treeItem.getModel()).getPid()+"/")) {
//                            Log.debug("in path:"+((GWTJahiaPageWrapper)treeItem.getModel()).getPid());
//                            treeItem.setTextStyle("in-path");
//                            // ... ?
//                        } else {
//                            treeItem.setTextStyle("normal");
//                        }
//
//                    }
//
//                }
//            });
//        }

        m_component.add(m_treeTable) ;
    }

    public void initWithLinker(BrowserLinker linker) {
        super.initWithLinker(linker);
        loader.load() ;
    }

    public void setContent(Object root) {
    }

    public void expandPath(String path) {
        new TreeOpener(m_treeTable, "title", path);
    }

    public void clearTable() {
        store.removeAll();
    }

    public Object getSelection() {
        List<GWTJahiaPageWrapper> elts = binder.getSelection() ;
        if (elts != null && elts.size()>0) {
            return elts.get(0) ;
        } else {
            return null ;
        }
    }

    public void refresh() {
        loader.load(null) ;
    }

    public Component getComponent() {
        return m_component ;
    }

    private static TreeTableColumnModel getHeaders() {
        List<TreeTableColumn> headerList = new ArrayList<TreeTableColumn>();
        TreeTableColumn col = new TreeTableColumn("title", "Title", .7f) ;
        headerList.add(col) ;


        if (!"guest".equals(JahiaGWTParameters.getCurrentUser())) {
            col = new TreeTableColumn("workflowStatus","Status", .2f) ;
            col.setRenderer(new CellRenderer() {
                public String render(Component component, String s, Object o) {
                    String r = "";
                    if (o != null) {
                        r += "<img src=\"../images/icons/workflow/" + o + ".png\">" ;
                    }
                    Boolean b = ((GWTJahiaPageWrapper) component.getModel()).isHasLive();
                    if (b != null && !b) {
                        r += "&nbsp; No live version ! ";
                    }
                    return r;
                }
            });
            headerList.add(col) ;
        }

        col = new TreeTableColumn("pid", "pid", 0.05f) ;
        col.setAlignment(Style.HorizontalAlignment.CENTER);
        col.setRenderer(new CellRenderer() {
            public String render(Component component, String s, Object o) {
                if (o != null && ((Integer)o)>0) {
                    return o.toString() ;
                } else {
                    return " " ;
                }
            }
        });
        headerList.add(col) ;

        return new TreeTableColumnModel(headerList);
    }

    /**
     * This class extends the standard load listener to allow automated child selection once the children are retrieved.
     */
    private class TreeTableStore<M extends ModelData> extends TreeStore<M> {
        private String startPath = null ;


        public TreeTableStore(TreeLoader loader, String pagePath) {
            super(loader) ;
            this.startPath = pagePath;
        }

        protected void onBeforeLoad(LoadEvent e) {
            super.onBeforeLoad(e);
            if (getLinker() != null) {
                getLinker().loading("loading...") ;
            }
        }

        /**
         * This allows selection after tree items have been loaded (asynchronous call is 'blocking' here)
         */
        protected void onLoad(TreeLoadEvent e) {
            super.onLoad(e) ;
            if (startPath != null) {
                expandPath(startPath);
                startPath = null ;
            }
            if (getLinker() != null) {
                getLinker().loaded() ;
            }
        }
    }

}
