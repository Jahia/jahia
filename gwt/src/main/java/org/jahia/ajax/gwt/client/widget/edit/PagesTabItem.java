package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.TreePanelDropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.TreeStoreEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelSelectionModel;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:37 PM
 * To change this template use File | Settings | File Templates.
 */
class PagesTabItem extends SidePanelTabItem {

    private boolean init = true;
    protected LayoutContainer treeContainer;
    protected TreeLoader<GWTJahiaNode> treeLoader;
    protected TreeStore<GWTJahiaNode> treeStore;
    protected TreePanel<GWTJahiaNode> tree;
    protected String path;

    public PagesTabItem() {
        setText("&nbsp;");
        setIcon(ContentModelIconProvider.CONTENT_ICONS.page());
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        setLayout(l);

        treeContainer = new LayoutContainer();
        treeContainer.setBorders(true);
        treeContainer.setScrollMode(Style.Scroll.AUTO);
        // data proxy
        RpcProxy<List<GWTJahiaNode>> treeProxy = new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object currentPage, AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                if (init) {
                    JahiaContentManagementService.App.getInstance().getRoot(JCRClientUtils.SITE_REPOSITORY, JCRClientUtils.SITE_NODETYPES, "", "", null, listAsyncCallback);
                } else {
                    JahiaContentManagementService.App.getInstance().ls(JCRClientUtils.SITE_REPOSITORY, (GWTJahiaNode) currentPage, JCRClientUtils.SITE_NODETYPES, "", "", null, true, listAsyncCallback);
                }
            }
        };

        treeLoader = new BaseTreeLoader<GWTJahiaNode>(treeProxy) {
            @Override
            public boolean hasChildren(GWTJahiaNode parent) {
                return parent.hasChildren();
            }

            protected void onLoadSuccess(Object gwtJahiaNode, List<GWTJahiaNode> gwtJahiaNodes) {
                super.onLoadSuccess(gwtJahiaNode, gwtJahiaNodes);
                if (init) {
                    Log.debug("setting init to false");
                    init = false;
                }

            }
        };
        treeStore = new TreeStore<GWTJahiaNode>(treeLoader);
        tree = new TreePanel<GWTJahiaNode>(treeStore) {
            @Override
            protected void onDataChanged(TreeStoreEvent<GWTJahiaNode> mTreeStoreEvent) {
                super.onDataChanged(mTreeStoreEvent);
                init = false;
                GWTJahiaNode p = mTreeStoreEvent.getParent();
                if (p == null) {
                    expandChildren(store.getRootItems());
                } else {
                    expandChildren(store.getChildren(p));
                }
            }

            /**
             * Expand children
             *
             * @param children
             */
            private void expandChildren(List<GWTJahiaNode> children) {
                for (GWTJahiaNode child : children) {
                    if (path.startsWith(child.getPath())) {
                        setExpanded(child, true);
                    }
                    if (path.equals(child.getPath())) {
                        List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
                        l.add(child);
                        tree.getSelectionModel().setSelection(l);
                    }

                }
            }
        };
        tree.setIconProvider(ContentModelIconProvider.getInstance());
        tree.setDisplayProperty("displayName");

        treeContainer.add(tree);

        VBoxLayoutData treeVBoxData = new VBoxLayoutData();
        treeVBoxData.setFlex(3);

        add(treeContainer, treeVBoxData);

        this.tree.setSelectionModel(new TreePanelSelectionModel<GWTJahiaNode>() {
            @Override
            protected void onMouseDown(TreePanelEvent be) {
                return;
            }

            @Override
            protected void onMouseClick(TreePanelEvent e) {
                super.onMouseDown(e);
            }
        });
        this.tree.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        this.tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                if (!se.getSelectedItem().getPath().equals(editLinker.getMainModule().getPath())) {
                    editLinker.getMainModule().mask("Loading","x-mask-loading");
                    editLinker.getMainModule().setPath(se.getSelectedItem().getPath());
                    editLinker.getMainModule().setTemplate(null);
                    editLinker.getMainModule().refresh();
                }
            }
        });

    }

    private void initDND() {
        EditModeTreePanelDragSource source = new PageTreePanelDragSource();
        TreePanelDropTarget target = new PageTreePanelDropTarget();
        target.setAllowDropOnLeaf(true);
        target.setAllowSelfAsSource(true);
        target.setAutoExpand(true);
        target.setFeedback(DND.Feedback.INSERT);

        source.addDNDListener(editLinker.getDndListener());
        target.addDNDListener(editLinker.getDndListener());
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        path = linker.getMainModule().getPath();
        initDND();
    }

    public void refresh() {
        treeStore.removeAll();
        init = true;
        treeLoader.load();
    }

    class PageTreePanelDropTarget extends TreePanelDropTarget {
        public PageTreePanelDropTarget() {
            super(PagesTabItem.this.tree);
        }

        @Override
        protected void onDragEnter(DNDEvent e) {

            boolean allowed = EditModeDNDListener.PAGETREE_TYPE.equals(e.getStatus().getData(EditModeDNDListener.SOURCE_TYPE));
            if (allowed) {
                e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.PAGETREE_TYPE);
            }
            e.getStatus().setStatus(allowed);
            e.setCancelled(false);

        }

        @Override
        protected void showFeedback(DNDEvent e) {
            super.showFeedback(e);
            e.getStatus().setData("type", status);
            if (activeItem != null) {
                GWTJahiaNode activeNode = (GWTJahiaNode) activeItem.getModel();
                GWTJahiaNode parent = treeStore.getParent(activeNode);
                if (status == 1) {
                    List<GWTJahiaNode> children = treeStore.getChildren(parent);
                    int next = children.indexOf(activeNode) + 1;
                    if (next < children.size()) {
                        GWTJahiaNode n = children.get(next);
                        e.getStatus().setData(EditModeDNDListener.TARGET_NEXT_NODE, n);
                    } else {
                        e.getStatus().setData(EditModeDNDListener.TARGET_NEXT_NODE, null);
                    }
                }
                e.getStatus().setData(EditModeDNDListener.TARGET_NODE, activeNode);
                e.getStatus().setData(EditModeDNDListener.TARGET_PARENT, parent);
                e.getStatus().setData(EditModeDNDListener.TARGET_PATH, activeNode.get("path"));
            } else {
                e.getStatus().setData(EditModeDNDListener.TARGET_NODE, null);
                e.getStatus().setData(EditModeDNDListener.TARGET_PARENT, null);
                e.getStatus().setData(EditModeDNDListener.TARGET_PATH, null);
            }
        }

        @Override
        protected void handleInsertDrop(DNDEvent e, TreePanel.TreeNode item, int index) {
        }

        @Override
        protected void handleAppendDrop(DNDEvent e, TreePanel.TreeNode treeNode) {
        }

        public AsyncCallback getCallback() {
            AsyncCallback callback = new AsyncCallback() {
                public void onSuccess(Object o) {
                    editLinker.getMainModule().refresh();
                    refresh();
                }

                public void onFailure(Throwable throwable) {
                    Window.alert("Failed : "+throwable);
                }
            };
            return callback;
        }
    }

    private class PageTreePanelDragSource extends EditModeTreePanelDragSource {
        public PageTreePanelDragSource() {
            super(PagesTabItem.this.tree);
        }

        @Override
        protected void onDragStart(DNDEvent e) {
            super.onDragStart(e);
            Selection.getInstance().hide();
            e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.PAGETREE_TYPE);
            List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
            l.add(PagesTabItem.this.tree.getSelectionModel().getSelectedItem());
            e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, l);
        }


        @Override
        protected void onDragDrop(DNDEvent event) {
            // do nothing
        }
    }
}
