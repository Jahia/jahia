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
package org.jahia.ajax.gwt.client.widget.pagepicker;

import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.grid.*;
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
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.GWTJahiaPageWrapper;
import org.jahia.ajax.gwt.client.service.JahiaContentServiceAsync;
import org.jahia.ajax.gwt.client.service.JahiaContentService;
import org.jahia.ajax.gwt.client.util.tree.TreeOpener;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.widget.SearchField;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.List;
import java.util.ArrayList;

/**
 * Site/pages explorer
 */
public class PageExplorer extends TopRightComponent {

    private ContentPanel m_component ;
    private TreeLoader<GWTJahiaPageWrapper> loader ;
    private TreeTable m_treeTable ;
    private TreeTableStore<GWTJahiaPageWrapper> store ;
    private TreeTableBinder<GWTJahiaPageWrapper> binder ;

    private Grid<GWTJahiaPageWrapper> m_searchTable;
    private ListStore<GWTJahiaPageWrapper> searchStore ;

    private TabPanel tabs ;
    private TabItem treeTable ;
    private TabItem search ;

    private TreeItem lastSelection = null ;

    public PageExplorer(final int homePageID, final int siteID, final String operation, String pagePath, final String parentPath) {
        m_component = new ContentPanel(new FitLayout()) ;
        m_component.setBodyBorder(false);
        m_component.setBorders(false);
        m_component.setHeaderVisible(false);

        tabs = new TabPanel() ;
        tabs.setBodyBorder(false);
        tabs.setBorders(false);

        final JahiaContentServiceAsync service = JahiaContentService.App.getInstance() ;
        // data proxy
        RpcProxy<GWTJahiaPageWrapper, List<GWTJahiaPageWrapper>> proxy = new RpcProxy<GWTJahiaPageWrapper, List<GWTJahiaPageWrapper>>() {
            @Override
            protected void load(GWTJahiaPageWrapper parentPage, final AsyncCallback<List<GWTJahiaPageWrapper>> listAsyncCallback) {
                if (parentPage == null) {
                    if (homePageID != -1) {
                        service.getSubPagesForCurrentUser(homePageID, listAsyncCallback);
                    } else if (siteID != -1){
                        JahiaContentService.App.getInstance().getSiteHomePage(siteID, new AsyncCallback<GWTJahiaPageWrapper>() {
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
            public String getStringValue(GWTJahiaPageWrapper page, String property) {
                if (!page.isSiteRoot()) {
                    if (operation.equals("movePage")) {
                        if (parentPath.contains("/"+page.getPid()+"/")) {
                            Log.debug("in path:"+page.getPid()+"/"+property);
                            return "gwt-pagepicker-icon-inpath";
                        }
                        if (page.isLocked()) {
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

        treeTable = new TabItem(Messages.getResource("pp_browse")) ;
        treeTable.setLayout(new FitLayout());
        treeTable.add(m_treeTable) ;
        tabs.add(treeTable) ;

        searchStore = new ListStore<GWTJahiaPageWrapper>() ;
        m_searchTable = new Grid<GWTJahiaPageWrapper>(searchStore, getSearchHeaders(operation, parentPath)) ;
        m_searchTable.setBorders(false);
        m_searchTable.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        m_searchTable.addListener(Events.RowClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                GWTJahiaPageWrapper newSelection = m_searchTable.getSelectionModel().getSelectedItem() ;
                if (newSelection != null) {
                    getLinker().onTableItemSelected();
                }
            }
        });

        search = new TabItem(Messages.getResource("pp_search")) ;
        search.setLayout(new FitLayout());
        search.add(m_searchTable) ;
        tabs.add(search) ;

        SearchField searchField = new SearchField(Messages.getResource("pp_search") + ": ", false) {
            public void onFieldValidation(String value) {
                if (tabs.getSelectedItem() == treeTable) {
                    tabs.setSelection(search);
                }
                setSearchContent(value);
            }

            public void onSaveButtonClicked(String value) {}
        };
        m_component.setTopComponent(searchField);

        m_component.add(tabs) ;
    }

    public void setSearchContent(String text) {
        searchStore.removeAll();
        if (text != null && text.length()>0) {
            if (getLinker() != null) {
                getLinker().loading("searching content...");
            }
            JahiaContentService.App.getInstance().searchInPages(text, new AsyncCallback<List<GWTJahiaPageWrapper>>() {
                public void onFailure(Throwable throwable) {
                    com.google.gwt.user.client.Window.alert("Element list retrieval failed :\n" + throwable.getLocalizedMessage()) ;
                    if (getLinker() != null) {
                        getLinker().loaded();
                    }
                }

                public void onSuccess(List<GWTJahiaPageWrapper> pages) {
                    if (pages != null) {
                        setProcessedContent(pages);
                    } else {

                    }
                    if (getLinker() != null) {
                        getLinker().loaded();
                    }
                }
            });
        } else {
            refresh();
        }
    }

    public void initWithLinker(BrowserLinker linker) {
        super.initWithLinker(linker);
        loader.load() ;
    }

    public void setContent(Object root) {}

    public void setProcessedContent(Object content) {
        searchStore.removeAll();
        searchStore.add((List<GWTJahiaPageWrapper>) content) ;
    }

    public void expandPath(String path) {
        new TreeOpener(m_treeTable, "title", path);
    }

    public void clearTable() {
        store.removeAll();
    }

    public Object getSelection() {
        if (tabs.getSelectedItem() == treeTable) {
            List<GWTJahiaPageWrapper> elts = binder.getSelection() ;
            if (elts != null && elts.size()>0) {
                return elts.get(0) ;
            } else {
                return null ;
            }
        } else {
            return m_searchTable.getSelectionModel().getSelectedItem() ;
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
        TreeTableColumn col = new TreeTableColumn("title", Messages.getResource("pp_title"), .75f) ;
        headerList.add(col) ;
        if (!"guest".equals(JahiaGWTParameters.getCurrentUser())) {
            col = new TreeTableColumn("workflowStatus", Messages.getResource("pp_wfState"), .2f) ;
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

    private static ColumnModel getSearchHeaders(final String operation, final String parentPath) {
        List<ColumnConfig> headerList = new ArrayList<ColumnConfig>();
        ColumnConfig col = new ColumnConfig("title", Messages.getResource("pp_title"), 300) ;
        col.setRenderer(new GridCellRenderer<GWTJahiaPageWrapper>() {
            public String render(GWTJahiaPageWrapper page, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaPageWrapper> gwtJahiaPageWrapperListStore) {
                StringBuilder title = new StringBuilder() ;
                if (!page.isSiteRoot()) {
                    if (operation.equals("movePage")) {
                        if (parentPath.contains("/"+page.getPid()+"/")) {
                            title.append("<span class=\"gwt-pagepicker-icon-inpath\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
                        } else if (page.isLocked()) {
                            title.append("<span class=\"gwt-pagepicker-icon-locked\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
                        } else {
                            title.append("<span class=\"icon-page\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
                        }
                    }
                    title.append("<span>&nbsp;").append(page.getTitle()).append("</span>");
                    return title.toString();
                } else {
                    return "";
                }
            }
        });
        headerList.add(col) ;
        if (!"guest".equals(JahiaGWTParameters.getCurrentUser())) {
            col = new ColumnConfig("workflowStatus", Messages.getResource("pp_wfState"), 100) ;
            col.setRenderer(new GridCellRenderer<GWTJahiaPageWrapper>() {
                public String render(GWTJahiaPageWrapper page, String s, ColumnData columnData, int i, int i1, ListStore listStore) {
                    if (page == null) {
                        return "" ;
                    } else {
                        StringBuilder r = new StringBuilder("<img src=\"../images/icons/workflow/").append(page.getWorkflowStatus()).append(".png\">") ;
                        if (page.isHasLive()) {
                            r.append("&nbsp;nbsp;No live version ! ");
                        }
                        return r.toString();
                    }
                }
            });
            headerList.add(col) ;
        }
        col = new ColumnConfig("pid", "pid", 50) ;
        col.setAlignment(Style.HorizontalAlignment.CENTER);
        col.setRenderer(new GridCellRenderer<GWTJahiaPageWrapper>() {
                public String render(GWTJahiaPageWrapper page, String s, ColumnData columnData, int i, int i1, ListStore listStore) {
                    if (page == null || page.getPid() < 1) {
                        return "" ;
                    } else {
                        return String.valueOf(page.getPid()) ;
                    }
                }
            });
        headerList.add(col) ;
        return new ColumnModel(headerList);
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
