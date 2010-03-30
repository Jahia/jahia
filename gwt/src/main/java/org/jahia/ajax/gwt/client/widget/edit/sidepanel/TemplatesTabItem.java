package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.TreeGridDropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
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
public class TemplatesTabItem extends SidePanelTabItem {

    protected LayoutContainer treeContainer;
    protected TreeGrid<GWTJahiaNode> tree;
    protected String path;
    protected GWTJahiaNodeTreeFactory factory;

    public TemplatesTabItem() {
        setIcon(ContentModelIconProvider.CONTENT_ICONS.tabPages());
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        setLayout(new FitLayout());
    }

    private void initTree() {
        ColumnConfig columnConfig = new ColumnConfig("displayName","Name",80);
        columnConfig.setRenderer(new TreeGridCellRenderer<GWTJahiaNode>());
        ColumnConfig author = new ColumnConfig("createdBy", "Author", 40);

        GWTJahiaNodeTreeFactory factory = new GWTJahiaNodeTreeFactory(JCRClientUtils.TEMPLATES_REPOSITORY);
        factory.setNodeTypes("jnt:virtualsite,jnt:page,jnt:templatesFolder,jnt:templatesSetFolder");
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

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        path = linker.getMainModule().getPath();
        initTree();
    }

    @Override
    public void refresh() {
        tree.getTreeStore().removeAll();
        tree.getTreeStore().getLoader().load();
    }

    public void addOpenPath(String path) {
        factory.setOpenPath(path);
    }


}