package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;

/**
 * Created by IntelliJ IDEA.
 * User: romain
 * Date: Aug 19, 2009
 * Time: 11:56:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class SidePanel extends ContentPanel {

    private boolean init = false;
    private final ListStore<GWTJahiaNode> displayStore;
    private final ContentPanel properties;

    public SidePanel() {
        super();
        setHeaderVisible(true);
        VBoxLayout layout = new VBoxLayout();
        layout.setPadding(new Padding(5));
        layout.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        setLayout(layout);

        // repository panel
        ContentPanel repository = new ContentPanel();
        repository.setHeading("Repository");
        repository.setLayout(new FitLayout());
        repository.setBorders(false);
        repository.setBodyBorder(false);
        repository.getHeader().setBorders(false);
        repository.setCollapsible(true);

        TabPanel tabs = new TabPanel();

        // creating
        TabItem create = new TabItem("Create");
        create.setLayout(new FitLayout());
        final ListStore<GWTJahiaNodeType> createStore = new ListStore<GWTJahiaNodeType>();
        JahiaContentDefinitionService.App.getInstance().getNodeTypes(new AsyncCallback<List<GWTJahiaNodeType>>() {
            public void onFailure(Throwable caught) {
                MessageBox.alert("Alert","Unable to load content definitions. Cause: "
                                + caught.getLocalizedMessage(),null);
            }

            public void onSuccess(List<GWTJahiaNodeType> result) {
                createStore.add(result);
            }
        });
        List<ColumnConfig> createColumns = new ArrayList<ColumnConfig>();
        createColumns.add(new ColumnConfig("name", "Name", 150));
        createColumns.add(new ColumnConfig("label", "Label", 150));
        final Grid<GWTJahiaNodeType> createGrid = new Grid<GWTJahiaNodeType>(createStore, new ColumnModel(createColumns));
        createGrid.setBorders(false);
        createGrid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        new GridDragSource(createGrid);
        create.add(createGrid);

        // browsing
        TabItem browse = new TabItem("Browse");
        browse.setLayout(new FitLayout());
        // data proxy
        RpcProxy<List<GWTJahiaNode>> privateProxy = new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                if (init) {
                    JahiaContentManagementService.App.getInstance().getRoot(JCRClientUtils.GLOBAL_REPOSITORY, "", "", "", null, listAsyncCallback);
                } else {
                    JahiaContentManagementService.App.getInstance().ls((GWTJahiaNode) gwtJahiaFolder,"", "", "", null, false, listAsyncCallback);
                }
            }
        };
        TreeLoader<GWTJahiaNode> loader = new BaseTreeLoader<GWTJahiaNode>(privateProxy) {
            @Override
            public boolean hasChildren(GWTJahiaNode parent) {
                return parent.hasFolderChildren() ;
            }
            protected void onLoadSuccess(Object gwtJahiaNode, List<GWTJahiaNode> gwtJahiaNodes) {
                super.onLoadSuccess(gwtJahiaNode, gwtJahiaNodes);
                if (init) {
                    Log.debug("setting init to false") ;
                    init = false ;
                }
            }
        };
        TreeStore<GWTJahiaNode> treeStore = new TreeStore<GWTJahiaNode>(loader);
        TreePanel<GWTJahiaNode> m_tree = new TreePanel<GWTJahiaNode>(treeStore);
        m_tree.setIconProvider(ContentModelIconProvider.getInstance());
        m_tree.setDisplayProperty("displayName");
        m_tree.setBorders(false);
        m_tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> gwtJahiaNodeSelectionChangedEvent) {
                GWTJahiaNode selected = gwtJahiaNodeSelectionChangedEvent.getSelectedItem();
                if (selected != null) {
                    displayProperties(selected);
                    if (selected.hasChildren()) {
                        JahiaContentManagementService.App.getInstance().ls(gwtJahiaNodeSelectionChangedEvent.getSelectedItem(), null, null, null, null, false, new AsyncCallback<List<GWTJahiaNode>>() {
                            public void onFailure(Throwable throwable) {
                                // TODO
                            }
                            public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                                setDisplayContent(gwtJahiaNodes);
                            }
                        });
                    } else {
                        List<GWTJahiaNode> list = new ArrayList<GWTJahiaNode>(1);
                        list.add(selected);
                        setDisplayContent(list);
                    }
                } else {
                    setDisplayContent(null);
                }
            }
        });
        new TreePanelDragSource(m_tree);
        browse.add(m_tree);

        // searching
        TabItem search = new TabItem("Search");
        FormPanel searchForm = new FormPanel();
        searchForm.setHeaderVisible(false);
        searchForm.setBorders(false);
        searchForm.setBodyBorder(false);
        final TextField<String> searchField = new TextField<String>();
        searchField.setFieldLabel("Search for");
        final CheckBox tags = new CheckBox();
        tags.setFieldLabel("Within");
        tags.setBoxLabel("tag");
        final CheckBox nameDesc = new CheckBox();
        nameDesc.setBoxLabel("name and description");
        final CheckBox props = new CheckBox();
        props.setBoxLabel("all properties");
        final CheckBox content = new CheckBox();
        content.setBoxLabel("file content");
        final DateField date = new DateField();
        date.setFieldLabel("Newer than");
        final TextField<String> searchRoot = new TextField<String>();
        searchRoot.setFieldLabel("Search in");
        Button ok = new Button("Search", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                search(searchField.getValue(), date.getValue(), searchRoot.getValue());
            }
        });
        searchForm.add(searchField);
        searchForm.add(tags);
        searchForm.add(nameDesc);
        searchForm.add(props);
        searchForm.add(content);
        searchForm.add(date);
        searchForm.add(searchRoot);
        searchForm.add(ok);
        search.add(searchForm);

        tabs.add(create);
        tabs.add(browse);
        tabs.add(search);
        repository.add(tabs);




        // display panel
        ContentPanel display = new ContentPanel();
        display.setHeading("Display");
        display.setLayout(new FitLayout());
        display.setCollapsible(true);
        displayStore = new ListStore<GWTJahiaNode>();
        List<ColumnConfig> displayColumns = new ArrayList<ColumnConfig>();
        displayColumns.add(new ColumnConfig("name", "Name", 100));
        displayColumns.add(new ColumnConfig("path", "Path", 200));
        final Grid<GWTJahiaNode> displayGrid = new Grid<GWTJahiaNode>(displayStore, new ColumnModel(displayColumns));
        displayGrid.setBorders(false);
        displayGrid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        displayGrid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> gwtJahiaNodeSelectionChangedEvent) {
                displayProperties(gwtJahiaNodeSelectionChangedEvent.getSelectedItem());
            }
        });
        new GridDragSource(displayGrid);
        display.add(displayGrid);

        // properties panel
        properties = new ContentPanel();
        properties.setHeading("Properties");
        properties.setLayout(new FitLayout());
        properties.setCollapsible(true);

        VBoxLayoutData vBoxData = new VBoxLayoutData(5, 5, 5, 5);
        vBoxData.setFlex(1);
        add(repository, vBoxData);
        add(display, vBoxData);
        add(properties, vBoxData);
    }

    /**
     * Method used by the search form
     *
     * @param query the query string
     * @param date search for items newer than date
     * @param searchRoot search within this path
     */
    private void search(String query, Date date, String searchRoot) {
        JahiaContentManagementService.App.getInstance().search(query, 500, new AsyncCallback<List<GWTJahiaNode>>() {
            public void onFailure(Throwable throwable) {
                // TODO
            }
            public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                setDisplayContent(gwtJahiaNodes);
            }
        });
    }

    /**
     * Clear the display list and add the provided list.
     *
     * @param content items to add
     */
    private void setDisplayContent(List<GWTJahiaNode> content) {
        displayStore.removeAll();
        if (content != null) {
            displayStore.add(content);
        }
    }

    /**
     * Clear the properties panel and display the current node properties
     *
     * @param node the current node
     */
    private void displayProperties(GWTJahiaNode node) {
        properties.removeAll();
        if (node != null) {
            properties.add(new Label("Path: " + node.getPath()));
        }
        properties.layout();
    }



}


