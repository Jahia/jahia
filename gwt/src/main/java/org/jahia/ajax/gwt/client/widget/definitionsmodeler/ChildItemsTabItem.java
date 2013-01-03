package org.jahia.ajax.gwt.client.widget.definitionsmodeler;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.GridDropTarget;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DualListField;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaFieldInitializer;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
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

    protected transient Map<GWTJahiaNode, PropertiesEditor> propertiesEditors;
    protected transient Map<GWTJahiaNode, Map<String, PropertiesEditor>> propertiesEditorsByLang;

    private String type;
    private transient Grid<GWTJahiaNode> grid;
    private transient List<String> columnsKeys;
    private transient GWTJahiaNodeType nodeType;
    private transient Map<String, GWTJahiaFieldInitializer> initializerMap = new HashMap<String, GWTJahiaFieldInitializer>();

    private transient String currentLanguage;
    private transient GWTJahiaNode engineNode;
    private transient List<GWTJahiaNode> children;
    private transient List<GWTJahiaNode> removedChildren;

    private transient String[] STRING = {"1", "string", "String", "STRING"};
    private transient String[] BINARY = {"2", "binary", "Binary", "BINARY"};
    private transient String[] LONG = {"3", "long", "Long", "LONG"};
    private transient String[] DOUBLE = {"4", "double", "Double", "DOUBLE"};
    private transient String[] BOOLEAN = {"5", "boolean", "Boolean", "BOOLEAN"};
    private transient String[] DATE = {"6", "date", "Date", "DATE"};
    private transient String[] NAME = {"7", "name", "Name", "NAME"};
    private transient String[] PATH = {"8", "path", "Path", "PATH"};
    private transient String[] REFERENCE = {"9", "reference", "Reference", "REFERENCE"};
    private transient String[] WEAKREFERENCE = {"10", "WEAKREFERENCE", "WeakReference", "weakreference"};
    private transient String[] URI = {"11", "URI", "Uri", "uri"};
    private transient String[] DECIMAL = {"12", "DECIMAL", "Decimal", "decimal"};
    private transient String[] UNDEFINED = new String[]{"0", "undefined", "Undefined", "UNDEFINED", "*"};

    public ChildItemsTabItem() {
    }


    @Override
    public void init(final NodeHolder engine, final AsyncTabItem tab, String language) {
        if (tab.isProcessed()) {
            return;
        }

        JahiaContentManagementService.App.getInstance().getNodeType(type, new BaseAsyncCallback<GWTJahiaNodeType>() {
            public void onSuccess(GWTJahiaNodeType result) {
                nodeType = result;
            }
        });

        engineNode = engine.getNode();
        if (engineNode == null) {
            engineNode = new GWTJahiaNode();
            engineNode.setPath("newNode");
        }
        currentLanguage = language;
        children = new ArrayList<GWTJahiaNode>();
        removedChildren = new ArrayList<GWTJahiaNode>();
        JahiaContentManagementService.App.getInstance().getFieldInitializerValues("jnt:childNodeDefinition", "j:defaultPrimaryType", engine.getNode() != null ? engine.getNode().getPath() : engine.getTargetNode().getPath(), new HashMap<String, List<GWTJahiaNodePropertyValue>>(), new BaseAsyncCallback<GWTJahiaFieldInitializer>() {
            @Override
            public void onSuccess(GWTJahiaFieldInitializer result) {
                initializerMap.put("jnt:childNodeDefinition.j:defaultPrimaryType", result);
                initializerMap.put("jnt:childNodeDefinition.j:requiredPrimaryTypes", result);
                initializerMap.put("jnt:unstructuredChildNodeDefinition.j:requiredPrimaryTypes", result);
                initializerMap.put("jnt:childNodeDefinition.j:requiredPrimaryTypes", result);
                initializerMap.put("jnt:unstructuredChildNodeDefinition.j:requiredPrimaryTypes", result);
            }
        });

        tab.setLayout(new RowLayout(Style.Orientation.VERTICAL));

        store = new ListStore<GWTJahiaNode>();
        propertiesEditor = null;
        propertiesEditors = new HashMap<GWTJahiaNode, PropertiesEditor>();
        propertiesEditorsByLang = new HashMap<GWTJahiaNode, Map<String, PropertiesEditor>>();

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columnsKeys = new ArrayList<String>();
        for (String columnConfig : columnsConfig) {
            String[] config = columnConfig.split(",");
            columnsKeys.add(config[0]);
            ColumnConfig colConfig = new ColumnConfig();
            colConfig.setId(config[0]);
            if (!"*".equals(config[1])) {
                colConfig.setWidth(new Integer(config[1]));
            } else {
                colConfig.setWidth(100);
            }
            colConfig.setHeader(Messages.get(config[2], config[2]));
            columns.add(colConfig);
            colConfig.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                @Override
                public Object render(GWTJahiaNode node, String property, ColumnData config, int rowIndex, int colIndex, ListStore store, Grid grid) {
                    String cellValue = "";
                    if (node.get(property) != null) {
                        if (node.get(property) instanceof ArrayList) {
                            for (Object s : (ArrayList<Object>) node.get(property)) {
                                if (s instanceof GWTJahiaNodePropertyValue) {
                                    cellValue = cellValue.equals("") ? ((GWTJahiaNodePropertyValue) s).getString() : cellValue + "," + ((GWTJahiaNodePropertyValue) s).getString();
                                } else if (s instanceof String) {
                                    cellValue = cellValue.equals("") ? (String) s : cellValue + "," + s;
                                }
                            }
                        } else {
                            Object p = node.get(property);
                            if (p instanceof GWTJahiaNodePropertyValue) {
                                cellValue = ((GWTJahiaNodePropertyValue) p).getString();
                            } else if (p instanceof String) {
                                cellValue = (String) p;
                            }
                        }
                        if (cellValue.startsWith("__")) {
                            cellValue = "*";
                        }
                    }
                    return cellValue;
                }
            });
        }

        if (engine.getNode() != null) {
            JahiaContentManagementService.App.getInstance().lsLoad(engine.getNode(), Arrays.asList(type), null,
                    null, columnsKeys, false, -1, 0, false, null, null, false, new BaseAsyncCallback<PagingLoadResult<GWTJahiaNode>>() {
                public void onSuccess(PagingLoadResult<GWTJahiaNode> result) {
                    for (GWTJahiaNode itemDefinition : result.getData()) {
                        children.add(itemDefinition);
                        store.add(itemDefinition);
                    }
                }
            });
        }

        grid = new Grid<GWTJahiaNode>(store, new ColumnModel(columns));

        GridDropTarget target = new GridDropTarget(grid);
        target.setAllowSelfAsSource(true);
        target.setFeedback(DND.Feedback.INSERT);

        grid.setHeight(200);
        tab.add(grid, new RowData(1, 200, new Margins(2)));

        ToolBar toolBar = new ToolBar();
        Button add = new Button(Messages.get("label.add", "Add"));
        add.setIcon(StandardIconsProvider.STANDARD_ICONS.plusRound());
        add.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                final MessageBox box = MessageBox.prompt("Name", "Please enter the item name:");
                box.addCallback(new Listener<MessageBoxEvent>() {
                    public void handleEvent(MessageBoxEvent be) {
                        if (Dialog.OK.equalsIgnoreCase(be.getButtonClicked().getItemId())) {
                            GWTJahiaNode itemDefinition = new GWTJahiaNode();
                            StringBuilder name = new StringBuilder("*".equals(be.getValue()) ? "__undef":be.getValue());
                            name = findAvailableName(name);
                            itemDefinition.setName(name.toString());
                            String t = type;
                            if ("*".equals(be.getValue())) {
                                if (type.equals("jnt:propertyDefinition")) {
                                    t = "jnt:unstructuredPropertyDefinition";
                                } else {
                                    t = "jnt:unstructuredChildNodeDefinition";
                                }
                            }
                            itemDefinition.setNodeTypes(Arrays.asList(t));
                            initValues(itemDefinition);
                            removedChildren.remove(itemDefinition);
                            children.add(itemDefinition);
                            store.add(itemDefinition);
                            grid.getSelectionModel().select(itemDefinition, false);
                        }
                    }

                    private StringBuilder findAvailableName(StringBuilder name) {
                        for (GWTJahiaNode n : store.getModels()) {
                            if (n.getName().equals(name.toString())) {
                                name.append("_");
                                return findAvailableName(name);
                            }
                        }
                        return name;
                    }
                });
            }
        });
        toolBar.add(add);
        Button remove = new Button(Messages.get("label.remove", "Remove"));
        remove.setIcon(StandardIconsProvider.STANDARD_ICONS.minusRound());
        remove.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                if (propertiesEditor != null) {
                    tab.remove(propertiesEditor);
                    propertiesEditor = null;
                    tab.layout();
                }
                grid.getSelectionModel().setFiresEvents(false);
                List<GWTJahiaNode> selection = grid.getSelectionModel().getSelection();
                for (GWTJahiaNode node : selection) {
                    if (!node.getName().startsWith("__undef")) {
                        removedChildren.add(node);
                    }
                    children.remove(node);
                    store.remove(node);
                }
                grid.getSelectionModel().setFiresEvents(true);
            }
        });
        toolBar.add(remove);
        Button moveUp = new Button(Messages.get("label.move.up", "move up"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                for (GWTJahiaNode node : getOrderedSelectedList()) {
                    execute(node);
                }
                grid.getView().refresh(false);
            }

            public void execute(GWTJahiaNode selectedNode) {
                // find a better way to get index
                removeSorter();
                int index = grid.getStore().indexOf(selectedNode);
                if (index > 0) {
                    grid.getStore().remove(selectedNode);
                    grid.getStore().insert(selectedNode, index - 1);
                    grid.getSelectionModel().select(index - 1, true);
                }
            }


        });
        moveUp.setIcon(StandardIconsProvider.STANDARD_ICONS.moveUp());
        toolBar.add(moveUp);

        Button moveFirst = new Button(Messages.get("label.move.first", "move first"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                int iteration = 0;
                for (GWTJahiaNode node : getOrderedSelectedList()) {
                    execute(node, iteration);
                    iteration++;
                }
            }

            public void execute(GWTJahiaNode node, int index) {
                removeSorter();
                grid.getStore().remove(node);
                grid.getStore().insert(node, index);
                grid.getSelectionModel().select(index, true);
                grid.getView().refresh(false);
            }
        });
        moveFirst.setIcon(StandardIconsProvider.STANDARD_ICONS.moveFirst());
        toolBar.add(moveFirst);

        Button moveDown = new Button(Messages.get("label.move.down", "move down"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                List<GWTJahiaNode> ordered = getOrderedSelectedList();
                Collections.reverse(ordered);
                for (GWTJahiaNode node : ordered) {
                    execute(node);
                }
            }

            public void execute(GWTJahiaNode selectedNode) {
                // find a better way to get index
                int index = grid.getStore().indexOf(selectedNode);
                if (index < grid.getStore().getCount() - 1) {
                    removeSorter();
                    grid.getStore().remove(selectedNode);
                    grid.getStore().insert(selectedNode, index + 1);
                    grid.getSelectionModel().select(index + 1, true);
                    grid.getView().refresh(false);
                }
            }
        });
        moveDown.setIcon(StandardIconsProvider.STANDARD_ICONS.moveDown());
        toolBar.add(moveDown);

        Button moveLast = new Button(Messages.get("label.move.last", "move last"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                List<GWTJahiaNode> ordered = getOrderedSelectedList();
                Collections.reverse(ordered);
                int index = grid.getStore().getCount() - 1;
                for (GWTJahiaNode node : ordered) {
                    execute(node, index);
                    index--;
                }
                grid.getSelectionModel().setSelection(grid.getSelectionModel().getSelection());
                grid.getView().refresh(false);
            }


            public void execute(GWTJahiaNode node, int index) {
                removeSorter();
                grid.getStore().remove(node);
                grid.getStore().insert(node, index);
                grid.getSelectionModel().select(index, true);

            }
        });
        moveLast.setIcon(StandardIconsProvider.STANDARD_ICONS.moveLast());
        toolBar.add(moveLast);
        tab.add(toolBar, new RowData(1, -1, new Margins(2)));

        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                switchPropertiesEditor(tab);
            }
        });
        tab.layout();
        tab.setProcessed(true);

    }

    @Override
    public void onLanguageChange(String language, TabItem tabItem) {
        currentLanguage = language;
        switchPropertiesEditor(tabItem);
        super.onLanguageChange(language, tabItem);
    }


    private void switchPropertiesEditor(final TabItem tab) {
        final GWTJahiaNode item = grid.getSelectionModel().getSelectedItem();

        if (propertiesEditor != null) {
            tab.remove(propertiesEditor);
        }
        if (item == null) {
            propertiesEditor = null;
            tab.layout();
        } else if (propertiesEditorsByLang.containsKey(item) && propertiesEditorsByLang.get(item).containsKey(currentLanguage)) {
            propertiesEditor = propertiesEditorsByLang.get(item).get(currentLanguage);
            PropertiesEditor previous = propertiesEditors.get(item);
            propertiesEditors.put(item,propertiesEditor);
            syncWithPrevious(previous, propertiesEditor);
            tab.add(propertiesEditor, new RowData(1, 1, new Margins(2)));
            tab.layout();
        } else {
            if (item.getPath() != null) {
                JahiaContentManagementService.App.getInstance().getProperties(item.getPath(), currentLanguage, new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
                    public void onSuccess(GWTJahiaGetPropertiesResult result) {
                        displayProperties(item, result.getNodeTypes(), result.getProperties());
                        tab.add(propertiesEditor, new RowData(1, 1, new Margins(2)));
                        tab.layout();
                    }
                });
            } else {
                item.set("newItem", "true");
                item.setPath(engineNode.getPath() + "/" + item.getName());
                displayProperties(item, Arrays.asList(nodeType), (Map<String, GWTJahiaNodeProperty>) item.get("default-properties"));
                item.remove("default-properties");
                tab.add(propertiesEditor, new RowData(1, 1, new Margins(2)));
                tab.layout();
            }
        }
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
                    } else if (!value.getValues().isEmpty()) {
                        item.set(definition.getName(), value.getValues().get(0));
                    }
                }
            }
        }
        item.set("default-properties", properties);
    }

    private void displayProperties(final GWTJahiaNode item, List<GWTJahiaNodeType> nodeTypes, Map<String, GWTJahiaNodeProperty> properties) {
        // change property
        for (GWTJahiaNodeType nt : nodeTypes) {
            if (nt.getName().equals("jnt:unstructuredChildNodeDefinition") ||
                    nt.getSuperTypes().contains("jnt:unstructuredChildNodeDefinition") ||
                    nt.getName().equals("jnt:unstructuredPropertyDefinition") ||
                    nt.getSuperTypes().contains("jnt:unstructuredPropertyDefinition")) {

                for (GWTJahiaItemDefinition def : nt.getInheritedItems()) {
                    if (def.isHidden()) {
                        def.setHidden(false);
                        def.setProtected(true);
                    }
                }

            }
        }
        propertiesEditor = new PropertiesEditor(nodeTypes, properties, null);
        propertiesEditor.setInitializersValues(initializerMap);
        if (!propertiesEditorsByLang.containsKey(item)) {
            propertiesEditorsByLang.put(item, new HashMap<String, PropertiesEditor>());
        }
        propertiesEditorsByLang.get(item).put(currentLanguage, propertiesEditor);
        PropertiesEditor previous = propertiesEditors.get(item);
        propertiesEditors.put(item, propertiesEditor);
        propertiesEditor.renderNewFormPanel();
        syncWithPrevious(previous, propertiesEditor);

        for (final String key : columnsKeys) {
            if (propertiesEditor.getFieldsMap().containsKey(key)) {
                final PropertiesEditor.PropertyAdapterField field = propertiesEditor.getFieldsMap().get(key);
                if (field.getField() instanceof DualListField) {
                    ((DualListField<GWTJahiaValueDisplayBean>) field.getField()).getToList().getStore().addStoreListener(new StoreListener<GWTJahiaValueDisplayBean>() {

                        @Override
                        public void handleEvent(StoreEvent<GWTJahiaValueDisplayBean> e) {
                            List<String> strings = new ArrayList<String>();
                            List<GWTJahiaValueDisplayBean> beans = ((ListStore<GWTJahiaValueDisplayBean>) e.getSource()).getModels();

                            for (GWTJahiaValueDisplayBean bean : beans) {
                                strings.add(bean.getValue());
                            }

                            item.set(key, strings);
                            store.update(item);
                            super.handleEvent(e);
                        }
                    });
                } else {
                    field.addListener(Events.Change, new Listener<BaseEvent>() {
                        public void handleEvent(BaseEvent be) {
                            Object value = field.getValue();
                            if (value instanceof GWTJahiaValueDisplayBean) {
                                value = ((GWTJahiaValueDisplayBean) value).getValue();
                            }
                            item.set(key, value);
                            store.update(item);
                        }
                    });
                }
            }
        }
    }

    private List<GWTJahiaNode> getOrderedSelectedList() {
        List<GWTJahiaNode> selectedNodes = grid.getSelectionModel().getSelection();
        Comparator<GWTJahiaNode> c = new Comparator<GWTJahiaNode>() {
            public int compare(GWTJahiaNode gwtJahiaNode, GWTJahiaNode gwtJahiaNode1) {
                int index = grid.getStore().indexOf(gwtJahiaNode);
                int index2 = grid.getStore().indexOf(gwtJahiaNode1);
                if (index == index2) {
                    return 0;
                }

                if (index > index2) {
                    return 1;
                }

                return -1;
            }
        };
        Collections.sort(selectedNodes, c);
        return selectedNodes;
    }

    public List<GWTJahiaNode> getOrderedNodes() {
        return grid.getStore().getModels();
    }

    public void setProcessed(boolean processed) {
        if (!processed) {
            store = null;
            engineNode = null;
        }

        super.setProcessed(processed);
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, List<GWTJahiaNode> children, GWTJahiaNodeACL acl) {
        if (store != null) {
            List<GWTJahiaNode> definitions = new ArrayList<GWTJahiaNode>(store.getModels());
            for (GWTJahiaNode itemDefinition : definitions) {
                boolean duplicate = false;
                if (propertiesEditorsByLang.containsKey(itemDefinition)) {
                    PropertiesEditor pe = propertiesEditors.get(itemDefinition);
                    Map<String,PropertiesEditor> peByLang = propertiesEditorsByLang.get(itemDefinition);
                    boolean isNew = "true".equals(itemDefinition.get("newItem"));
                    if (isNew) {
                        if (itemDefinition.getName().startsWith("__undef")) {
                            itemDefinition.setName(computeUnstructuredItemName(itemDefinition));
                            String path = itemDefinition.getPath().substring(0,itemDefinition.getPath().indexOf("__undef")) + itemDefinition.getName();
                            for (GWTJahiaNode n : store.getModels()) {
                                if (n.getPath().equals(path)) {
                                 duplicate = true;
                                }
                            }
                            if (!duplicate) {
                                itemDefinition.setPath(itemDefinition.getPath().substring(0,itemDefinition.getPath().indexOf("__undef")) + itemDefinition.getName());
                            }
                        }
                        if (!duplicate) {
                            itemDefinition.remove("newItem");
                        }
                    }
                    itemDefinition.set("nodeProperties", pe.getProperties(false, true, !isNew));
                    HashMap<String, List<GWTJahiaNodeProperty>> langCodeProperties = new HashMap<String, List<GWTJahiaNodeProperty>>();
                    for (String s : peByLang.keySet()) {
                        langCodeProperties.put(s, peByLang.get(s).getProperties(true,false,!isNew));
                    }
                    itemDefinition.set("nodeLangCodeProperties", langCodeProperties);
                } else {
                    itemDefinition.set("nodeProperties", new ArrayList<GWTJahiaNodeProperty>());
                    itemDefinition.set("nodeLangCodeProperties", new HashMap<String, List<GWTJahiaNodeProperty>>());
                }
            }
            Collections.sort(this.children, new Comparator<ModelData>() {
                @Override
                public int compare(ModelData o1, ModelData o2) {
                    GWTJahiaNode c1 = null;
                    GWTJahiaNode c2 = null;

                    for (GWTJahiaNode n : grid.getStore().getModels()) {
                        if (n.getPath().equals(((GWTJahiaNode) o1).getPath())) {
                            c1 = n;
                        }
                        if (n.getPath().equals(((GWTJahiaNode) o2).getPath())) {
                            c2 = n;
                        }
                    }
                    if (c1 != null && c2 != null) {
                        int index = grid.getStore().indexOf(c1);
                        int index2 = grid.getStore().indexOf(c2);
                        if (index == index2) {
                            return 0;
                        }

                        if (index > index2) {
                            return 1;
                        }
                    }

                    return -1;
                }
            });
            if (node != null) {
                for (GWTJahiaNode child : this.removedChildren) {
                    node.add(child);
                    node.remove(child);
                }
                for (GWTJahiaNode child : this.children) {
                    node.add(child);
                }
                node.set(GWTJahiaNode.INCLUDE_CHILDREN, Boolean.TRUE);
            } else {
                children.addAll(this.children);
            }
        }
    }
    private void removeSorter() {
        if (grid.getStore().getStoreSorter() != null) {
            grid.getStore().setSortField(null);
            grid.getStore().setStoreSorter(null);
            grid.getView().refresh(true);
        } else {
            grid.getView().refresh(false);
        }
    }

    private String computeUnstructuredItemName(GWTJahiaNode node) {
        String s1 = "";
        if (node.getNodeTypes().contains("jnt:unstructuredPropertyDefinition")) {
            if (node.get("j:mutliple") != null) {
                if (node.get("j:requiredType") instanceof String) {
                    int i = 256 + getPropertyType((String) node.get("j:requiredType"));
                    s1 = "__prop__" + i;
                } else if (node.get("j:requiredType") instanceof GWTJahiaNodePropertyValue) {
                    int i = 256 + getPropertyType(((GWTJahiaNodePropertyValue) node.get("j:requiredType")).getString());
                    s1 = "__prop__" + i;
                }
            } else {
                if (node.get("j:requiredType") instanceof String) {
                    s1 = "__prop__" + getPropertyType((String) node.get("j:requiredType"));
                } else if (node.get("j:requiredType") instanceof GWTJahiaNodePropertyValue) {
                    s1 = "__prop__" + getPropertyType(((GWTJahiaNodePropertyValue) node.get("j:requiredType")).getString());
                }
            }
        }  else {
            StringBuilder s = new StringBuilder("__node__");
            for (Object e : (List<Object>) node.get("j:requiredPrimaryTypes")) {
                if (e instanceof String) {
                    s.append(e).append(" ");
                } else if (e instanceof GWTJahiaNodePropertyValue) {
                    s.append(((GWTJahiaNodePropertyValue) e).getString()).append(" ");
                }
            }
            s1 = s.toString().trim();
        }
        return s1;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setColumnsConfig(List<String> columnsConfig) {
        this.columnsConfig = columnsConfig;
    }

    public void syncWithPrevious(PropertiesEditor previous, PropertiesEditor propertiesEditor) {
        if (previous != null && previous != propertiesEditor) {
            // synch non18n properties
            List<GWTJahiaNodeProperty> previousNon18nProperties = previous.getProperties(false, true, true);
            if (previousNon18nProperties != null && !previousNon18nProperties.isEmpty()) {
                Map<String, PropertiesEditor.PropertyAdapterField> fieldsMap = propertiesEditor.getFieldsMap();
                for (GWTJahiaNodeProperty property : previousNon18nProperties) {
                    if (fieldsMap.containsKey(property.getName()))  {
                        FormFieldCreator.fillValue(fieldsMap.get(property.getName()).getField(), propertiesEditor.getGWTJahiaItemDefinition(property), property, null);
                    }
                }
            }
            Set<String> previousAddedTypes = previous.getAddedTypes();
            Set<String> previousRemovedTypes = previous.getRemovedTypes();
            if (previousAddedTypes != null) {
                Map<String, FieldSet> f = propertiesEditor.getFieldSetsMap();
                propertiesEditor.getAddedTypes().addAll(previousAddedTypes);
                propertiesEditor.getRemovedTypes().addAll(previousRemovedTypes);
                for (String addedType : previousAddedTypes) {
                    f.get(addedType).expand();
                }
                for (String addedType : previousRemovedTypes) {
                    f.get(addedType).collapse();
                }
            }
        }
    }

    private int getPropertyType(String token) {
        if (tokenEquals(token,STRING)) {
            return Integer.parseInt(STRING[0]);
        } else if (tokenEquals(token,BINARY)) {
            return Integer.parseInt(BINARY[0]);
        } else if (tokenEquals(token,LONG)) {
            return Integer.parseInt(LONG[0]);
        } else if (tokenEquals(token,DOUBLE)) {
            return Integer.parseInt(DOUBLE[0]);
        } else if (tokenEquals(token,BOOLEAN)) {
            return Integer.parseInt(BOOLEAN[0]);
        } else if (tokenEquals(token,DATE)) {
            return Integer.parseInt(DATE[0]);
        } else if (tokenEquals(token,NAME)) {
            return Integer.parseInt(NAME[0]);
        } else if (tokenEquals(token,PATH)) {
            return Integer.parseInt(PATH[0]);
        } else if (tokenEquals(token,REFERENCE)) {
            return Integer.parseInt(REFERENCE[0]);
        } else if (tokenEquals(token,WEAKREFERENCE)) {
            return Integer.parseInt(WEAKREFERENCE[0]);
        } else if (tokenEquals(token,URI)) {
            return Integer.parseInt(URI[0]);
        } else if (tokenEquals(token,DECIMAL)) {
            return Integer.parseInt(DECIMAL[0]);
        } else if (tokenEquals(token,UNDEFINED)) {
            return Integer.parseInt(UNDEFINED[0]);
        } else {
            return -1;
        }
    }

    private boolean tokenEquals(String token, String[] s) {
        for (String e : s) {
            if (token.equals(e)) {
                return true;
            }
        }
        return false;
    }
}
