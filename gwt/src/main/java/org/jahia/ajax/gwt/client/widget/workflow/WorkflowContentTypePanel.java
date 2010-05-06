package org.jahia.ajax.gwt.client.widget.workflow;

import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorflowNodeType;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 28, 2010
 * Time: 4:33:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowContentTypePanel extends ContentPanel {
    private EditorGrid<GWTJahiaWorflowNodeType> grid;
    private Linker linker;
    private List<GWTJahiaWorkflowDefinition> workflowDefinitions;
    private List<GWTJahiaNodeType> contentTypeList;
    private List<GWTJahiaWorflowNodeType> initialWorflowNodeTypes;
    private List<GWTJahiaWorflowNodeType> worflowNodeTypeToRemove = new ArrayList<GWTJahiaWorflowNodeType>();


    public WorkflowContentTypePanel(Linker linker, List<GWTJahiaWorkflowDefinition> workflowDefinitions, List<GWTJahiaNodeType> contentTypeList, List<GWTJahiaWorflowNodeType> worflowNodeTypes) {
        this.linker = linker;
        this.workflowDefinitions = workflowDefinitions;
        this.contentTypeList = contentTypeList;
        this.initialWorflowNodeTypes= worflowNodeTypes;
        setBodyBorder(true);
        setLayout(new FitLayout());
        init();

    }


    /**
     * Init UI
     */
    private void init() {
        final List<ColumnConfig> columnConfigList = new ArrayList<ColumnConfig>();


        ColumnConfig column = new ColumnConfig("key", "key", 150);
        column.setEditor(new CellEditor(createTextField()));
        column.setHidden(true);
        columnConfigList.add(column);

        // path
        column = new ColumnConfig("path", Messages.get("fm_info_path", "Path"), 150);
        column.setEditor(new CellEditor(createTextField()));
        columnConfigList.add(column);

        // contentType
        column = new ColumnConfig("nodeType", Messages.get("label_nodeType", "Node Type"), 150);
        column.setEditor(new CellEditor(createContentNodeTypeField()));
        columnConfigList.add(column);

        // workflow type
        final ToolBar toolBar = new ToolBar();

        final Button add = new Button(Messages.get("label_add", "Add"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                GWTJahiaWorflowNodeType gwnt = new GWTJahiaWorflowNodeType();
                gwnt.setKey("w" + System.currentTimeMillis());
                gwnt.setPath(linker.getMainNode().getPath());
                gwnt.getWorkflowDefinitions().add(workflowDefinitions.get(0));
                gwnt.setNodeType(contentTypeList.get(0));
                grid.stopEditing();
                grid.getStore().insert(gwnt, 0);
                grid.startEditing(0, 1);

            }

        });
        add.setIcon(StandardIconsProvider.STANDARD_ICONS.plusRound());
        toolBar.add(add);
        toolBar.add(new SeparatorToolItem());

        boolean displayWColumn = true;
        for (final GWTJahiaWorkflowDefinition gnt : workflowDefinitions) {
            final ColumnConfig wColumn = new ColumnConfig("", gnt.getName(), 200);
            wColumn.setRenderer(new GridCellRenderer<GWTJahiaWorflowNodeType>() {
                public Object render(final GWTJahiaWorflowNodeType model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<GWTJahiaWorflowNodeType> gwtJahiaWorflowNodeTypeListStore, Grid<GWTJahiaWorflowNodeType> gwtJahiaWorflowNodeTypeGrid) {
                    final CheckBox chb = new CheckBox();
                    chb.setValue(model.getWorkflowDefinitions().contains(gnt));
                    chb.addListener(Events.Change, new Listener<ComponentEvent>() {
                        public void handleEvent(ComponentEvent componentEvent) {
                            if (chb.getValue()) {
                                model.getWorkflowDefinitions().add(gnt);
                            } else {
                                model.getWorkflowDefinitions().remove(gnt);
                            }
                        }
                    });
                    return chb;
                }
            });

            final CheckBox chb = new CheckBox();
            chb.setBoxLabel(gnt.getName());
            chb.addListener(Events.Change, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent componentEvent) {
                    if (grid != null && grid.getView() != null) {
                        wColumn.setHidden(!chb.getValue());
                        grid.getView().refresh(true);
                    }
                }
            });
            if (displayWColumn) {
                wColumn.setHidden(false);
                displayWColumn = false;
                chb.setValue(true);
            } else{
                wColumn.setHidden(true);
            }
            columnConfigList.add(wColumn);

            toolBar.add(chb);
        }

       
        // add remove
        column = new ColumnConfig("", "", 50);
        column.setRenderer(new GridCellRenderer<GWTJahiaWorflowNodeType>() {
            public Object render(final GWTJahiaWorflowNodeType model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<GWTJahiaWorflowNodeType> gwtJahiaWorflowNodeTypeListStore, Grid<GWTJahiaWorflowNodeType> gwtJahiaWorflowNodeTypeGrid) {
                if(model.getKey() == null || model.getKey().equalsIgnoreCase("default")){
                    return null;
                }

                final Button remove = new Button("", new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        grid.getStore().remove(model);
                        worflowNodeTypeToRemove.add(model);
                        grid.getView().refresh(false);
                    }

                });
                remove.setIcon(StandardIconsProvider.STANDARD_ICONS.minusRound());
                return remove;
            }
        });
        columnConfigList.add(column);


        final ListStore<GWTJahiaWorflowNodeType> gridStore = new ListStore<GWTJahiaWorflowNodeType>();
        gridStore.add(initialWorflowNodeTypes);
        grid = new EditorGrid<GWTJahiaWorflowNodeType>(gridStore, new ColumnModel(columnConfigList));
        grid.setBorders(true);
        grid.setHeight(500);


        /*  final Button editPath = new Button(Messages.get("label_editPath", "Edit Path"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                grid.stopEditing();
                grid.startEditing(grid.getStore().indexOf(grid.getSelectionModel().getSelectedItem()), 1);

            }

        });
        //editPath.setIcon(ContentModelIconProvider.getInstance().getPlusRound());
        toolBar.add(editPath);

        final Button editNodeType = new Button(Messages.get("label_editNodeType", "Edit node type"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                grid.stopEditing();
                grid.startEditing(grid.getStore().indexOf(grid.getSelectionModel().getSelectedItem()), 2);

            }

        });
        //editNodeType.setIcon(ContentModelIconProvider.getInstance().getPlusRound());

        toolBar.add(editNodeType);

        final Button editWorkflowType = new Button(Messages.get("label_editWorflowType", "Edit worflow type"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                grid.stopEditing();
                grid.startEditing(grid.getStore().indexOf(grid.getSelectionModel().getSelectedItem()), 3);

            }

        });
        //editNodeType.setIcon(ContentModelIconProvider.getInstance().getPlusRound());

        toolBar.add(editWorkflowType);*/


        setLayout(new FitLayout());
        setHeaderVisible(false);
        setTopComponent(toolBar);
        //grid.setSelectionModel(sm);
        grid.setBorders(true);
        // grid.addPlugin(sm);
        add(grid);
    }

    /**
     * Create path field
     *
     * @return
     */
    private TextField<String> createTextField() {
        TextField<String> text = new TextField<String>();
        text.setAllowBlank(false);
        return text;
    }

    /**
     * Create workflow type field
     *
     * @return
     */
    private ComboBox<GWTJahiaWorkflowDefinition> createWorkflowTypeField() {
        ComboBox<GWTJahiaWorkflowDefinition> workflowTypeComboBox = new ComboBox<GWTJahiaWorkflowDefinition>();
        final BaseListLoader wfLoader = new BaseListLoader(
                new RpcProxy<List<GWTJahiaWorkflowDefinition>>() {
                    @Override
                    protected void load(Object loadConfig, AsyncCallback<List<GWTJahiaWorkflowDefinition>> asyncCallback) {
                        JahiaContentManagementService.App.getInstance().getWorkflowDefinitions(asyncCallback);
                    }
                });
        workflowTypeComboBox.setStore(new ListStore<GWTJahiaWorkflowDefinition>(wfLoader));
        workflowTypeComboBox.setAllowBlank(false);
        workflowTypeComboBox.setDisplayField("name");
        wfLoader.load();
        return workflowTypeComboBox;
    }

    /**
     * Create content node type field
     *
     * @return
     */
    private ComboBox<GWTJahiaNodeType> createContentNodeTypeField() {
        ComboBox<GWTJahiaNodeType> combo = new ComboBox<GWTJahiaNodeType>();
        final ListStore<GWTJahiaNodeType> contentTypeStore = new ListStore<GWTJahiaNodeType>();
        contentTypeStore.add(contentTypeList);
        combo.setStore(contentTypeStore);
        combo.setTriggerAction(ComboBox.TriggerAction.ALL);
        combo.setForceSelection(true);
        combo.setDisplayField("label");
        return combo;
    }

    /**
     * Get workflow node type
     *
     * @return
     */
    public List<GWTJahiaWorflowNodeType> getWorflowNodeType() {
        return grid.getStore().getModels();
    }

    public List<GWTJahiaWorflowNodeType> getWorflowNodeTypeToRemove() {
        return worflowNodeTypeToRemove;
    }
}
