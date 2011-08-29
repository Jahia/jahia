package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaCreateEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaEditEngineInitBean;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.*;


/**
 * Display information about node visibility and allows to add/edit
 * visibility conditions on that node
 *
 */
public class VisibilityTabItem extends EditEngineTabItem {

    private transient Map<String, PropertiesEditor> propertiesEditorMap = new HashMap<String, PropertiesEditor>();
    private transient ListStore<GWTJahiaNode> conditionsStore;

    @Override
    public void init(NodeHolder engine, AsyncTabItem tab, String language) {
        if (engine.getNode() == null) {
            return;
        }
        final GWTJahiaNode node = engine.getNode();
        LayoutContainer container = new LayoutContainer(new RowLayout());
        tab.add(container);
        tab.setProcessed(true);

        LayoutContainer top = new LayoutContainer();
        container.add(top, new RowData(1, 80));

        Text status = new Text("Current status : ");
        top.add(status);

        HorizontalPanel p = new HorizontalPanel();
        top.add(p);

        Text addText = new Text("Add new condition : ");
        p.add(addText);

        RpcProxy<List<GWTJahiaNodeType>> typesProxy = new RpcProxy<List<GWTJahiaNodeType>>() {
            protected void load(Object loadConfig, final AsyncCallback<List<GWTJahiaNodeType>> listAsyncCallback) {
                JahiaContentManagementService.App.getInstance().getSubNodeTypes(Arrays.asList("jnt:condition"), listAsyncCallback);
            }
        };
        final ListLoader<GWTJahiaNodeType> typesLoader = new BaseListLoader(typesProxy);
        final ListStore<GWTJahiaNodeType> typesStore = new ListStore<GWTJahiaNodeType>();
        final ComboBox<GWTJahiaNodeType> types = new ComboBox<GWTJahiaNodeType>();
        types.setDisplayField("label");
        types.setStore(typesStore);
        p.add(types);

        final Map<String, GWTJahiaNodeType> typesMap = new HashMap<String, GWTJahiaNodeType>();

        final Button add = new Button();
        add.setIcon(StandardIconsProvider.STANDARD_ICONS.plusRound());
        add.setEnabled(false);
        p.add(add);

        // data proxy
        RpcProxy<List<GWTJahiaNode>> conditionsProxy = new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<List<GWTJahiaNode>> callback) {
                List<String> l = new ArrayList<String>(GWTJahiaNode.DEFAULT_FIELDS);
                JahiaContentManagementService.App.getInstance().searchSQL("select * from [jnt:condition] as c where ischildnode(c,['"+node.getPath()+"'])",-1, null, null, null,
                        l, true, callback);
            }
        };
        ListLoader<GWTJahiaNode> conditionsLoader = new BaseListLoader(conditionsProxy);
        conditionsStore = new ListStore<GWTJahiaNode>(conditionsLoader);

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig name = new ColumnConfig("name", Messages.get("label.title"), 500);
        name.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public Object render(GWTJahiaNode condition, String property, com.extjs.gxt.ui.client.widget.grid.ColumnData config, int rowIndex, int colIndex,
                                 ListStore<GWTJahiaNode> store, Grid<GWTJahiaNode> grid) {
                Object v = condition.get(property);
                if (condition.get("node-removed") != null) {
                    v = "<span class=\"markedForDeletion\">" + v + "</span>";
                }
                return v;
            }
        });
        name.setFixed(true);
        configs.add(name);

        ColumnConfig remove = new ColumnConfig("remove", Messages.get("label.remove"), 100);
        remove.setAlignment(Style.HorizontalAlignment.RIGHT);
        remove.setRenderer(new GridCellRenderer() {
            public Object render(final ModelData condition, String property, com.extjs.gxt.ui.client.widget.grid.ColumnData config, final int rowIndex, final int colIndex, ListStore listStore, final Grid grid) {
                final Button button;
                if (condition.get("node-removed") ==null) {
                    button = new Button(Messages.get("label.remove"));
                    button.setIcon(StandardIconsProvider.STANDARD_ICONS.minusRound());
                } else {
                    button = new Button(Messages.get("label.undelete"));
                    button.setIcon(StandardIconsProvider.STANDARD_ICONS.restore());
                }
                button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        if (condition.get("new-node") != null) {
                            conditionsStore.remove((GWTJahiaNode) condition);
                        } else {
                            if (condition.get("node-removed") == null) {
                                condition.set("node-removed", Boolean.TRUE);
                                grid.getView().refresh(false);
                            } else {
                                condition.set("node-removed", null);
                                grid.getView().refresh(false);
                            }
                        }
                    }
                });
                return button;
            }
        });
        configs.add(remove);

        ColumnConfig publish = new ColumnConfig("publish", Messages.get("label.publish"), 100);
        publish.setAlignment(Style.HorizontalAlignment.RIGHT);
        publish.setRenderer(new GridCellRenderer() {
            public Object render(final ModelData model, String property, com.extjs.gxt.ui.client.widget.grid.ColumnData config, int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                Button button = new Button(Messages.get("label.publish"), new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {

                    }
                });
                button.setIcon(ToolbarIconProvider.getInstance().getIcon("publish"));
                return button;
            }
        });
        configs.add(publish);

        ColumnModel cm = new ColumnModel(configs);

        final Grid<GWTJahiaNode> conditions = new Grid<GWTJahiaNode>(conditionsStore, cm);
        conditions.setAutoExpandColumn("name");

        container.add(conditions, new RowData(1, 200));

        final LayoutContainer form = new LayoutContainer(new FitLayout());
        container.add(form, new RowData(1, 250));

        final GridSelectionModel<GWTJahiaNode> selectionModel = conditions.getSelectionModel();
        selectionModel.setSelectionMode(Style.SelectionMode.SINGLE);
        selectionModel.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                form.removeAll();
                final GWTJahiaNode conditionNode = selectionModel.getSelectedItem();
                PropertiesEditor pe = propertiesEditorMap.get(conditionNode.getPath());
                if (pe != null) {
                    form.add(pe);
                    form.layout();
                } else {
                    if (conditionNode.get("new-node") != null) {
                        final GWTJahiaNodeType type = types.getSelection().get(0);
                        JahiaContentManagementService.App.getInstance().initializeCreateEngine(type.getName(),
                                node.getPath(), new BaseAsyncCallback<GWTJahiaCreateEngineInitBean>() {
                            public void onSuccess(GWTJahiaCreateEngineInitBean result) {
                                PropertiesEditor pe = new PropertiesEditor(Arrays.asList(type), new HashMap<String, GWTJahiaNodeProperty>(), null);
                                pe.renderNewFormPanel();
                                propertiesEditorMap.put(conditionNode.getPath(), pe);
                                form.add(pe);
                                form.layout();
                            }
                        });
                    } else {
                        JahiaContentManagementService.App.getInstance().initializeEditEngine(conditionNode.getPath(), false, new BaseAsyncCallback<GWTJahiaEditEngineInitBean>() {
                            public void onSuccess(GWTJahiaEditEngineInitBean result) {
                                PropertiesEditor pe = new PropertiesEditor(result.getNodeTypes(), result.getProperties(), null);
                                pe.renderNewFormPanel();
                                propertiesEditorMap.put(conditionNode.getPath(), pe);
                                form.add(pe);
                                form.layout();
                            }
                        });
                    }
                }
            }
        });

        add.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                GWTJahiaNode newCondition = new GWTJahiaNode();
                List<GWTJahiaNodeType> nodeTypes = types.getSelection();
                String nodeTypeName = nodeTypes.get(0).getName();
                newCondition.setNodeTypes(Arrays.asList(nodeTypeName));
                newCondition.setName(nodeTypeName + conditionsStore.getCount());
                newCondition.setPath(node.getPath() + "/new" + conditionsStore.getCount());
                newCondition.set("new-node", Boolean.TRUE);
                conditionsStore.add(newCondition);
                selectionModel.select(Arrays.asList(newCondition), false);
            }
        });

        typesLoader.addLoadListener(new LoadListener() {
            @Override
            public void loaderLoad(LoadEvent le) {
                List<GWTJahiaNodeType> l = (List<GWTJahiaNodeType>) le.getData();
                typesStore.add(l);
                types.setSelection(Arrays.asList(l.get(0)));
                for (GWTJahiaNodeType type : l) {
                    typesMap.put(type.getName(), type);
                }
            }
        });

        types.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNodeType>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNodeType> se) {
                add.enable();
            }
        });


        conditionsLoader.load();
        typesLoader.load();
        tab.layout();
    }

    @Override
    public void setProcessed(boolean processed) {
        if (!processed) {
            propertiesEditorMap = new HashMap<String, PropertiesEditor>();
            conditionsStore = null;
        }
        super.setProcessed(processed);
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, GWTJahiaNodeACL acl) {
        for (GWTJahiaNode jahiaNode : conditionsStore.getModels()) {
            PropertiesEditor pe = propertiesEditorMap.get(jahiaNode.getPath());
            if (pe != null) {
                jahiaNode.set("gwtproperties",pe.getProperties(false, true, false));
            }
        }
        node.set("visibilityConditions", conditionsStore.getModels());
    }
}
