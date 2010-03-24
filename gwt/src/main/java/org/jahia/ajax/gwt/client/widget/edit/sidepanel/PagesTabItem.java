package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.TreeGridDropTarget;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeTreeGridDragSource;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Selection;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Side panel tab item for browsing the pages tree.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:37 PM
 */
public class PagesTabItem extends SidePanelTabItem {

    protected LayoutContainer treeContainer;
    protected TreeGrid<GWTJahiaNode> tree;
    protected String path;
    protected GWTJahiaNodeTreeFactory factory;

    public PagesTabItem() {
        setIcon(ContentModelIconProvider.CONTENT_ICONS.tabPages());
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        setLayout(new FitLayout());
    }

    private void initTree() {
        ColumnConfig columnConfig = new ColumnConfig("displayName","Name",80);
        columnConfig.setRenderer(new TreeGridCellRenderer<GWTJahiaNode>());
        ColumnConfig author = new ColumnConfig("createdBy", "Author", 40);

        GWTJahiaNodeTreeFactory factory = new GWTJahiaNodeTreeFactory(JCRClientUtils.SITE_REPOSITORY);
        factory.setNodeTypes(JCRClientUtils.SITE_NODETYPES);
        this.factory = factory;
        this.factory.setSelectedPath(path);

        tree = factory.getTreeGrid(new ColumnModel(Arrays.asList(columnConfig,author)));

        tree.setAutoExpandColumn("displayName");
        tree.getTreeView().setRowHeight(25);
        tree.getTreeView().setForceFit(true);
        tree.setHeight("100%");
        tree.setIconProvider(ContentModelIconProvider.getInstance());

        this.tree.setSelectionModel(new TreeGridSelectionModel<GWTJahiaNode>() {
            @Override
            protected void handleMouseClick(GridEvent<GWTJahiaNode> e) {
                super.handleMouseClick(e);
                if (!getSelectedItem().getPath().equals(editLinker.getMainModule().getPath())) {
                    if (!getSelectedItem().getNodeTypes().contains("jnt:templatesFolder") && !getSelectedItem().getNodeTypes().contains("jnt:virtualsite")) {
                        editLinker.getMainModule().goTo(getSelectedItem().getPath(), null);
                    }
                }
            }
        });
        this.tree.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        
        tree.setContextMenu(createContextMenu("org.jahia.toolbar.sidePanel.pages", tree.getSelectionModel()));

        add(tree);
    }

    private void initDND() {
        EditModeTreeGridDragSource source = new PageTreeGridDragSource();
        TreeGridDropTarget target = new PageTreeGridDropTarget();
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
        initTree();
        initDND();
    }

    @Override
    public void refresh() {
        tree.getTreeStore().removeAll();
        tree.getTreeStore().getLoader().load();
    }

    public void addOpenPath(String path) {
        factory.setOpenPath(path);        
    }

    public GWTJahiaNode findTemplateFolder () {
        return tree.getStore().findModel("path","/sites/"+ JahiaGWTParameters.getSiteKey() +"/templates");
    }

    public class PageTreeGridDropTarget extends TreeGridDropTarget {
        public PageTreeGridDropTarget() {
            super(PagesTabItem.this.tree);
        }

        @Override
        protected void showFeedback(DNDEvent e) {
            super.showFeedback(e);
            e.getStatus().setData("type", status);
            if (activeItem != null) {
                GWTJahiaNode activeNode = (GWTJahiaNode) activeItem.getModel();
                GWTJahiaNode parent = tree.getTreeStore().getParent(activeNode);
                if (status == 1) {
                    List<GWTJahiaNode> children = tree.getTreeStore().getChildren(parent);
                    int next = children.indexOf(activeNode) + 1;
                    if (next < children.size()) {
                        GWTJahiaNode n = children.get(next);
                        e.getStatus().setData(EditModeDNDListener.TARGET_NEXT_NODE, n);
                    } else {
                        e.getStatus().setData(EditModeDNDListener.TARGET_NEXT_NODE, null);
                    }
                }

                if (activeNode.getExt().equals("icon-page")) {
                    e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.PAGETREE_TYPE);
                } else if (activeNode.getNodeTypes().contains("jnt:templatesFolder")
                        && EditModeDNDListener.PAGETREE_TYPE.equals(e.getStatus().getData(EditModeDNDListener.SOURCE_TYPE))) {
                    e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.TEMPLATETREE_TYPE);
                } else {
                    e.getStatus().setStatus(false);
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
        protected void onDragDrop(DNDEvent event) {
            //
        }

        public AsyncCallback<Object> getCallback() {
            AsyncCallback<Object> callback = new AsyncCallback<Object>() {
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

    private class PageTreeGridDragSource extends EditModeTreeGridDragSource {
        public PageTreeGridDragSource() {
            super(PagesTabItem.this.tree);
        }

        @Override
        protected void onDragStart(DNDEvent e) {
            super.onDragStart(e);
            Selection.getInstance().hide();

            List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
            final GWTJahiaNode node = PagesTabItem.this.tree.getSelectionModel().getSelectedItem();
            if (node.getExt().equals("icon-template")) {
                l.add(node);
                e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.TEMPLATETREE_TYPE);
                e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, l);
            } else if (node.getExt().equals("icon-page")) {
                l.add(node);
                e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.PAGETREE_TYPE);
                e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, l);
            } else {
                e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, null);
                e.getStatus().setStatus(false);
                e.setCancelled(true);
            }
        }


        @Override
        protected void onDragDrop(DNDEvent event) {
            // do nothing
        }
    }

}
