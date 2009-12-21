package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.dnd.TreePanelDropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.TreeStoreEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

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
                }
            }
        };
        tree.setIconProvider(ContentModelIconProvider.getInstance());
        tree.setDisplayProperty("displayName");

        treeContainer.add(tree);

        VBoxLayoutData treeVBoxData = new VBoxLayoutData();
        treeVBoxData.setFlex(3);

        add(treeContainer, treeVBoxData);


        this.tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent se) {

            }
        });

        TreePanelDragSource source = new TreePanelDragSource(tree);
        TreePanelDropTarget target = new TreePanelDropTarget(tree) {
            @Override
            protected void handleInsert(DNDEvent dndEvent, TreePanel.TreeNode treeNode) {
                super.handleInsert(dndEvent, treeNode);
            }

            @Override
            protected void handleAppend(DNDEvent event, TreePanel.TreeNode item) {
                super.handleAppend(event, item);
                if (((List) event.getData()).contains(activeItem.getModel())) {
                    event.getStatus().setStatus(false);
                }
            }

            @Override
            protected void handleInsertDrop(DNDEvent dndEvent, TreePanel.TreeNode item, int index) {
                if (dndEvent.getStatus().getStatus()) {
//                    ContentActions.move(editLinker, (List<GWTJahiaNode>) dndEvent.getData(), (GWTJahiaNode) treeNode.getModel());
//                    JahiaContentManagementService.App.getInstance().moveOnTopOf(nodes.get(0).getPath(), targetPath, new DropAsyncCallback());
                }
            }

            @Override
            protected void handleAppendDrop(DNDEvent dndEvent, TreePanel.TreeNode treeNode) {
                if (dndEvent.getStatus().getStatus()) {
//                    ContentActions.move(editLinker, (List<GWTJahiaNode>) dndEvent.getData(), (GWTJahiaNode) treeNode.getModel());
//                    JahiaContentManagementService.App.getInstance().moveOnTopOf(nodes.get(0).getPath(), targetPath, new DropAsyncCallback());
                }
            }

        };
        target.setAllowDropOnLeaf(true);
        target.setAllowSelfAsSource(true);
        target.setAutoExpand(true);
        target.setFeedback(DND.Feedback.INSERT);
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        path = linker.getMainModule().getPath();

    }

}
