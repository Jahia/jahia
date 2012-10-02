package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTColumn;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SettingsPanel implements Serializable {

    protected transient ListLoader<ListLoadResult<GWTJahiaNode>> listLoader;
    protected transient ListStore<GWTJahiaNode> contentStore;

    private transient ContentPanel settingsPanel;
    private List<String> paths = new ArrayList<String>();
    private String label;
    protected List<String> folderTypes = new ArrayList<String>();
    private String requiredPermission;

    public SettingsPanel() {

    }

    public void init(final EditLinker editLinker) {

        settingsPanel = new ContentPanel();
        settingsPanel.setAnimCollapse(false);
        settingsPanel.setHeading(Messages.get(label));
        settingsPanel.setLayout(new FitLayout());

        LayoutContainer treeContainer = new LayoutContainer();
        treeContainer.setBorders(false);
        treeContainer.setScrollMode(Style.Scroll.AUTO);
        treeContainer.setLayout(new FitLayout());
        GWTJahiaNodeTreeFactory factory = new GWTJahiaNodeTreeFactory(paths);
        factory.setNodeTypes(Arrays.asList("jnt:contentTemplate"));

        NodeColumnConfigList columns = new NodeColumnConfigList(Arrays.asList(new GWTColumn("displayName", "", -1)));
        columns.init();
        columns.get(0).setRenderer(NodeColumnConfigList.NAME_TREEGRID_RENDERER);

        TreeGrid<GWTJahiaNode> tree = factory.getTreeGrid(new ColumnModel(columns));
        tree.setAutoExpandColumn(columns.getAutoExpand());
        tree.getTreeView().setRowHeight(25);
        tree.getTreeView().setForceFit(true);
        tree.setHeight("100%");
        tree.setIconProvider(ContentModelIconProvider.getInstance());

        treeContainer.add(tree);

        tree.setHideHeaders(true);
        tree.setAutoExpand(true);

        // get List of site settings
        tree.setSelectionModel(new TreeGridSelectionModel<GWTJahiaNode>() {
            @Override
            protected void handleMouseClick(GridEvent<GWTJahiaNode> e) {
                super.handleMouseClick(e);
                    MainModule.staticGoTo(JahiaGWTParameters.getSiteNode().getPath() ,getSelectedItem().getName());
            }
        });
        tree.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                listLoader.load(se.getSelectedItem());
            }
        });

        // data proxy
        RpcProxy<ListLoadResult<GWTJahiaNode>> listProxy = new RpcProxy<ListLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(final Object gwtJahiaFolder, final AsyncCallback<ListLoadResult<GWTJahiaNode>> listAsyncCallback) {
            }
        };

        listLoader = new BaseListLoader<ListLoadResult<GWTJahiaNode>>(listProxy);

        contentStore = new ListStore<GWTJahiaNode>(listLoader);
        contentStore.setStoreSorter(new StoreSorter<GWTJahiaNode>(new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                if (o1 instanceof String && o2 instanceof String) {
                    String s1 = (String) o1;
                    String s2 = (String) o2;
                    return Collator.getInstance().localeCompare(s1, s2);
                } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
                    return ((Comparable) o1).compareTo(o2);
                }
                return 0;
            }
        }));
        contentStore.setSortField("display");
        VBoxLayoutData treeVBoxData = new VBoxLayoutData();
        treeVBoxData.setFlex(1);

        settingsPanel.add(treeContainer, treeVBoxData);
    }

    public ContentPanel getSettingsPanel() {
        return settingsPanel;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public void setFolderTypes(List<String> folderTypes) {
        this.folderTypes = folderTypes;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }
}