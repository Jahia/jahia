package org.jahia.ajax.gwt.client.widget.definitionsmodeler;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.EditEngineTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.NodeHolder;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.*;

public class ChildItemsTabItem extends EditEngineTabItem {
    private static final long serialVersionUID = 1L;

    private List<String> columnsConfig;

    protected transient ListStore<GWTJahiaNode> store;
    protected transient PropertiesEditor propertiesEditor;

    protected transient Map<GWTJahiaNode,PropertiesEditor> propertiesEditors;

    private String type;
    private transient List<String> columnsKeys;
    private transient GWTJahiaNodeType nodeType;

    public ChildItemsTabItem() {
    }


    @Override
    public void init(final NodeHolder engine,final AsyncTabItem tab, String language) {
        if (tab.isProcessed()) {
            return;
        }

        JahiaContentManagementService.App.getInstance().getNodeType(type, new BaseAsyncCallback<GWTJahiaNodeType>() {
            @Override
            public void onSuccess(GWTJahiaNodeType result) {
                nodeType = result;
            }
        });

        tab.setLayout(new RowLayout(Style.Orientation.VERTICAL));

        store = new ListStore<GWTJahiaNode>();
        propertiesEditor = null;
        propertiesEditors = new HashMap<GWTJahiaNode, PropertiesEditor>();

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columnsKeys = new ArrayList<String>();
        for(String columnConfig : columnsConfig) {
            String[] config = columnConfig.split(",");
            columnsKeys.add(config[0]);
            ColumnConfig colConfig = new ColumnConfig();
            colConfig.setId(config[0]);
            if (!"*".equals(config[1])) {
                colConfig.setWidth(new Integer(config[1]));
            } else {
                colConfig.setWidth(100);
            }
            colConfig.setHeader(Messages.get(config[2],config[2]));
            columns.add(colConfig);
        }

        JahiaContentManagementService.App.getInstance().lsLoad(engine.getNode(), Arrays.asList(type),null,
                null, columnsKeys, false, -1,0, false, null, null, false, new BaseAsyncCallback<PagingLoadResult<GWTJahiaNode>>() {
            public void onSuccess(PagingLoadResult<GWTJahiaNode> result) {
                for (GWTJahiaNode itemDefinition : result.getData()) {
                    engine.getNode().add(itemDefinition);
                    store.add(itemDefinition);
                }
            }
        });

        final Grid<GWTJahiaNode> grid = new Grid<GWTJahiaNode>(store,new ColumnModel(columns));
        grid.setHeight(200);
        tab.add(grid, new RowData(1,200, new Margins(2)));

        ToolBar toolBar = new ToolBar();
        Button add = new Button("Add");
        add.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                final MessageBox box = MessageBox.prompt("Name", "Please enter the item name:");
                box.addCallback(new Listener<MessageBoxEvent>() {
                    public void handleEvent(MessageBoxEvent be) {
                        GWTJahiaNode itemDefinition = new GWTJahiaNode();
                        itemDefinition.setName(be.getValue());
                        itemDefinition.setNodeTypes(Arrays.asList(type));
                        initValues(itemDefinition);
                        engine.getNode().add(itemDefinition);
                        store.add(itemDefinition);
                        grid.getSelectionModel().select(itemDefinition, false);
                    }
                });
            }
        });
        toolBar.add(add);
        tab.add(toolBar, new RowData(1,-1, new Margins(2)));

        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                final GWTJahiaNode item = se.getSelectedItem();

                if (propertiesEditor != null) {
                    tab.remove(propertiesEditor);
                }
                if (propertiesEditors.containsKey(item)) {
                    propertiesEditor = propertiesEditors.get(item);
                } else {
                    if (item.getPath() != null) {
                        JahiaContentManagementService.App.getInstance().getProperties(item.getPath(),engine.getDefaultLanguageCode(), new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
                            public void onSuccess(GWTJahiaGetPropertiesResult result) {
                                displayProperties(item, result.getNodeTypes(), result.getProperties());
                            }
                        });
                    } else {
                        displayProperties(item, Arrays.asList(nodeType), (Map<String, GWTJahiaNodeProperty>) item.get("default-properties"));
                        item.remove("default-properties");
                    }
                }

                tab.add(propertiesEditor, new RowData(1,1,new Margins(2)));
                tab.layout();
            }
        });
        tab.layout();
        tab.setProcessed(true);

    }

    private void initValues(GWTJahiaNode item) {
        HashMap<String, GWTJahiaNodeProperty> properties = new HashMap<String, GWTJahiaNodeProperty>();
        for (GWTJahiaItemDefinition definition : nodeType.getItems()) {
            if (definition instanceof GWTJahiaPropertyDefinition) {
                GWTJahiaPropertyDefinition propertyDefinition = (GWTJahiaPropertyDefinition) definition;
                List<GWTJahiaNodePropertyValue> defaultValues = propertyDefinition.getDefaultValues();
                if (defaultValues != null) {
                    GWTJahiaNodeProperty value = new GWTJahiaNodeProperty(definition.getName(), (GWTJahiaNodePropertyValue) null);
                    value.setMultiple(propertyDefinition.isMultiple());
                    value.setValues(defaultValues);
                    properties.put(definition.getName(), value);
                    if (propertyDefinition.isMultiple()) {
                        item.set(definition.getName(), value.getValues());
                    } else if (!value.getValues().isEmpty()){
                        item.set(definition.getName(), value.getValues().get(0));
                    }
                }
            }
        }
        item.set("default-properties", properties);
    }

    private void displayProperties(final GWTJahiaNode item, List<GWTJahiaNodeType> nodeTypes, Map<String, GWTJahiaNodeProperty> properties) {
        propertiesEditor = new PropertiesEditor(nodeTypes, properties, null);
        propertiesEditors.put(item, propertiesEditor);
        propertiesEditor.renderNewFormPanel();
        for (final String key : columnsKeys) {
            if (propertiesEditor.getFieldsMap().containsKey(key)) {
                final PropertiesEditor.PropertyAdapterField field = propertiesEditor.getFieldsMap().get(key);
                field.addListener(Events.Change, new Listener<BaseEvent>() {
                    @Override
                    public void handleEvent(BaseEvent be) {
                        Object value = field.getValue();
                        if (value instanceof GWTJahiaValueDisplayBean) {
                            value = ((GWTJahiaValueDisplayBean)value).getValue();
                        }
                        item.set(key, value);
                        store.update(item);
                    }
                });
            }
        }
    }

    public void setProcessed(boolean processed) {
        if (!processed) {
            store = null;
        }

        super.setProcessed(processed);
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, GWTJahiaNodeACL acl) {
        if (store != null) {
            for (GWTJahiaNode itemDefinition : store.getModels()) {
                if (propertiesEditors.containsKey(itemDefinition)) {
                    PropertiesEditor pe = propertiesEditors.get(itemDefinition);
                    boolean isNew = itemDefinition.getPath() == null;
                    itemDefinition.set("nodeProperties", pe.getProperties(false,true,!isNew));
                    itemDefinition.set("nodeLangCodeProperties", new HashMap<String, List<GWTJahiaNodeProperty>>());
                } else {
                    itemDefinition.set("nodeProperties", new ArrayList<GWTJahiaNodeProperty>());
                    itemDefinition.set("nodeLangCodeProperties", new HashMap<String, List<GWTJahiaNodeProperty>>());
                }
            }
            node.set(GWTJahiaNode.INCLUDE_CHILDREN, Boolean.TRUE);
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setColumnsConfig(List<String>columnsConfig) {
        this.columnsConfig = columnsConfig;
    }
}
