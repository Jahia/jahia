package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.form.*;
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
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.DND;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.DOM;
import com.google.gwt.core.client.GWT;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.Map;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.GWTJahiaBasicDataBean;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.ActionIconsImageBundle;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: romain
 * Date: Aug 19, 2009
 * Time: 11:56:07 AM
 *
 * This is the content browser / editor side panel.
 *
 */
public class SidePanel extends ContentPanel {

    public static final ActionIconsImageBundle ACTION_ICONS = GWT.create(ActionIconsImageBundle.class);

    private boolean init = false;
    private final ListStore<GWTJahiaNode> displayStore;
    private final ListStore<GWTJahiaNodeType> displayTypesStore;
    private final PreviewTabItem previewTabItem;
    private final TabItem propertiesTabItem;
    private final ContentPanel contentList;
    private Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> definitions;
    ListStore<GWTJahiaBasicDataBean> templateListStore;
    private final Grid<GWTJahiaNode> displayGrid;
    private final Grid<GWTJahiaNodeType> displayTypesGrid;

    public SidePanel(EditManager editManager) {
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

        TabPanel repositoryTabs = new TabPanel();

        // creating
        TabItem create = new TabItem("Create");
        create.setLayout(new FitLayout());
        final ListStore<GWTJahiaNodeType> createStore = new ListStore<GWTJahiaNodeType>();
        JahiaContentDefinitionService.App.getInstance().getNodeTypes(new AsyncCallback<Map<GWTJahiaNodeType, List<GWTJahiaNodeType>>>() {
            public void onFailure(Throwable caught) {
                MessageBox.alert("Alert","Unable to load content definitions. Cause: "
                                + caught.getLocalizedMessage(),null);
            }

            public void onSuccess(Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> result) {
                definitions = result;
                List<GWTJahiaNodeType> list = new ArrayList<GWTJahiaNodeType>(result.keySet());
                createStore.add(list);
            }

        });
        List<ColumnConfig> createColumns = new ArrayList<ColumnConfig>();
        createColumns.add(new ColumnConfig("name", "Name", 150));
        createColumns.add(new ColumnConfig("label", "Label", 150));
        final Grid<GWTJahiaNodeType> createGrid = new Grid<GWTJahiaNodeType>(createStore, new ColumnModel(createColumns));
        createGrid.setBorders(false);
        createGrid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
//        GridDragSource createGridSource = new CreateGridDragSource(createGrid);
//        createGridSource.addDNDListener(editManager.getDndListener());
        create.add(createGrid);
        createGrid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNodeType>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNodeType> gwtJahiaNodeSelectionChangedEvent) {
                GWTJahiaNodeType selected = gwtJahiaNodeSelectionChangedEvent.getSelectedItem();
                if (selected != null) {
                    List<GWTJahiaNodeType> list = definitions.get(selected);
                    setDisplayTypesContent(list);
                } else {
                    setDisplayTypesContent(null);
                }
            }
        });

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
                    displaySelection(selected);
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
        TreePanelDragSource contentTreeSource = new ContentTreeDragSource(m_tree);
        contentTreeSource.addDNDListener(editManager.getDndListener());
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

        repositoryTabs.add(create);
        repositoryTabs.add(browse);
        repositoryTabs.add(search);
        repository.add(repositoryTabs);




        // content list panel
        contentList = new ContentPanel();
        contentList.setHeading("Content list");
        contentList.setLayout(new FitLayout());
        contentList.setCollapsible(true);
        displayStore = new ListStore<GWTJahiaNode>();
        List<ColumnConfig> displayColumns = new ArrayList<ColumnConfig>();
        displayColumns.add(new ColumnConfig("name", "Name", 300));
//        displayColumns.add(new ColumnConfig("path", "Path", 200));
        displayGrid = new Grid<GWTJahiaNode>(displayStore, new ColumnModel(displayColumns));
        displayGrid.setBorders(false);
        displayGrid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        displayGrid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> gwtJahiaNodeSelectionChangedEvent) {
                displaySelection(gwtJahiaNodeSelectionChangedEvent.getSelectedItem());
            }
        });
        GridDragSource displayGridSource = new DisplayGridDragSource(displayGrid);
        displayGridSource.addDNDListener(editManager.getDndListener());
        contentList.add(displayGrid);

        displayTypesStore = new ListStore<GWTJahiaNodeType>();

        displayTypesGrid = new Grid<GWTJahiaNodeType>(displayTypesStore, new ColumnModel(displayColumns));
        displayTypesGrid.setBorders(false);
        displayTypesGrid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        GridDragSource createGridSource = new CreateGridDragSource(displayTypesGrid);
        createGridSource.addDNDListener(editManager.getDndListener());

        // displayPanel panel
        ContentPanel displayPanel = new ContentPanel();
        displayPanel.setHeading("Display");
        displayPanel.setLayout(new FitLayout());
        displayPanel.setCollapsible(true);
        TabPanel displayTabs = new TabPanel();

        previewTabItem = new PreviewTabItem("Preview");
        previewTabItem.setLayout(new FitLayout());
        displayTabs.add(previewTabItem);
        propertiesTabItem = new TabItem("Properties");
        propertiesTabItem.setLayout(new FitLayout());
        displayTabs.add(previewTabItem);
        displayTabs.add(propertiesTabItem);
        displayPanel.add(displayTabs);

        // add to side panel
        VBoxLayoutData vBoxData = new VBoxLayoutData(5, 5, 5, 5);
        vBoxData.setFlex(1);
        add(repository, vBoxData);
        add(contentList, vBoxData);
        add(displayPanel, vBoxData);

        ToolBar toolbar = new ToolBar();
        templateListStore = new ListStore<GWTJahiaBasicDataBean>();
        ComboBox<GWTJahiaBasicDataBean> templateBox = new ComboBox<GWTJahiaBasicDataBean>();
        templateBox.setStore(templateListStore);
        templateBox.setDisplayField(GWTJahiaBasicDataBean.DISPLAY_NAME);
        // TODO fill in real templates (see updateToolBar method below)
        templateListStore.add(new GWTJahiaBasicDataBean("template1", "template1"));
        templateListStore.add(new GWTJahiaBasicDataBean("template2", "template2"));
        templateListStore.add(new GWTJahiaBasicDataBean("template3", "template3"));
        Button lock = new Button();
        lock.setIcon(ACTION_ICONS.lock());
        lock.setToolTip("lock");
        Button edit = new Button();
        edit.setIcon(ACTION_ICONS.edit());
        lock.setToolTip("edit");
        Button remove = new Button();
        remove.setIcon(ACTION_ICONS.remove());
        lock.setToolTip("remove");
        Button delete = new Button();
        delete.setIcon(ACTION_ICONS.delete());
        lock.setToolTip("delete");
        Button undo = new Button();
        undo.setIcon(ACTION_ICONS.undo());
        lock.setToolTip("undo");
        toolbar.add(templateBox);
        toolbar.add(lock);
        toolbar.add(edit);
        toolbar.add(remove);
        toolbar.add(delete);
        toolbar.add(undo);
        setBottomComponent(toolbar);
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
        contentList.removeAll();
        displayStore.removeAll();
        if (content != null) {
            displayStore.add(content);
        }
        contentList.add(displayGrid);
        contentList.layout();
    }

    private void setDisplayTypesContent(List<GWTJahiaNodeType> content) {
        contentList.removeAll();
        displayTypesStore.removeAll();
        if (content != null) {
            displayTypesStore.add(content);
        }
        contentList.add(displayTypesGrid);
        contentList.layout();
    }

    private void displaySelection(final GWTJahiaNode node) {
        displayPreview(node);
        displayProperties(node);
    }

    /**
     * Display the rendered html of the given node in the preview panel
     *
     * @param node the node to render
     */
    private void displayPreview(final GWTJahiaNode node) {
        previewTabItem.removeAll();
        if (node != null) {
            JahiaContentManagementService.App.getInstance().getRenderedContent(node.getPath(), null, false, new AsyncCallback<String>() {
                public void onSuccess(String result) {
                    HTML html = new HTML(result);
                    previewTabItem.add(html);
                    previewTabItem.setHtml(html);
                    previewTabItem.setNode(node);
                    previewTabItem.layout();

                }

                public void onFailure(Throwable caught) {
                    Log.error("", caught);
                    Window.alert("-->"+caught.getMessage());
                }
            });
        }
    }

    /**
     * Clear the properties panel and display the current node properties
     *
     * @param node the current node
     */
    private void displayProperties(final GWTJahiaNode node) {
        propertiesTabItem.removeAll();
        JahiaContentManagementService.App.getInstance().getProperties(node.getPath(), new AsyncCallback<GWTJahiaGetPropertiesResult>() {
            public void onFailure(Throwable throwable) {
                Log.debug("Cannot get properties", throwable);
            }

            public void onSuccess(GWTJahiaGetPropertiesResult result) {
                final List<GWTJahiaNode> elements = new ArrayList<GWTJahiaNode>();
                elements.add(node);

                List<String> list = new ArrayList<String>();
                list.add("jcr:content");
                list.add("j:thumbnail");
                final PropertiesEditor propertiesEditor = new PropertiesEditor(result.getNodeTypes(), result.getProperties(), false, true, list, null);

                ToolBar toolBar = (ToolBar) propertiesEditor.getTopComponent();
                Button item = new Button(Messages.getResource("fm_save"));
                item.setIconStyle("gwt-icons-save");
                item.setEnabled(node.isWriteable() && !node.isLocked());
                item.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    public void componentSelected(ButtonEvent event) {
                        JahiaContentManagementService.App.getInstance().saveProperties(elements, propertiesEditor.getProperties(), new AsyncCallback() {
                            public void onFailure(Throwable throwable) {
                                com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                                Log.error("failed", throwable);
                            }

                            public void onSuccess(Object o) {
                                Info.display("", "Properties saved");
                                //getLinker().refreshTable();
                            }
                        });
                    }
                });
                toolBar.add(new FillToolItem());
                toolBar.add(item);
                item = new Button(Messages.getResource("fm_restore"));
                item.setIconStyle("gwt-icons-restore");
                item.setEnabled(node.isWriteable() && !node.isLocked());

                item.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    public void componentSelected(ButtonEvent event) {
                        propertiesEditor.resetForm();
                    }
                });
                toolBar.add(item);
                toolBar.setVisible(true);
                propertiesTabItem.add(propertiesEditor);
                propertiesTabItem.layout();
            }
        });
    }

    /**
     * This will update the template conbo box based on the page selected item
     *
     * @param node the selected node (item on the page)
     */
    public void updateToolBar(GWTJahiaNode node) {
        templateListStore.removeAll();
        // TODO retrieve selected node in the page and fill in the store with its available templates
    }

    public class PreviewDragSource extends DragSource {
        private final PreviewTabItem previewTabItem;

        public PreviewDragSource(PreviewTabItem previewTabItem) {
            super(previewTabItem);
            this.previewTabItem = previewTabItem;
        }

        @Override
        protected void onDragStart(DNDEvent e) {
            e.setCancelled(false);
            List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(1);
            nodes.add(previewTabItem.getNode());
            e.setData(nodes);
            e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, EditModeDNDListener.CONTENT_SOURCE_TYPE);

            List<GWTJahiaNode> list = (List<GWTJahiaNode>) e.getData();
            e.getStatus().setData("size", list.size());

            e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, list);
            if (getStatusText() == null) {
                e.getStatus().update(DOM.clone(previewTabItem.getWidget(0).getElement(),true));
            }
            super.onDragStart(e);
        }
    }

    private class PreviewTabItem extends TabItem {
        HTML html;
        GWTJahiaNode node;
        public PreviewTabItem(String s) {
            super(s);
        }

        public HTML getHtml() {
            return html;
        }

        public void setHtml(HTML html) {
            this.html = html;
        }

        public GWTJahiaNode getNode() {
            return node;
        }

        public void setNode(GWTJahiaNode node) {
            this.node = node;
        }
    }
}


