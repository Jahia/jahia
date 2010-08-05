package org.jahia.ajax.gwt.client.widget.workflow;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ComponentPlugin;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.workflow.dialog.WorkflowActionDialog;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 28, 2010
 * Time: 4:32:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationManagerEngine extends Window {
    private final Linker linker;
    private TreeLoader<GWTJahiaNode> loader;
    private TreeStore<GWTJahiaNode> store;
    private TreeGrid<GWTJahiaNode> m_tree;
    private static String[] STATE_IMAGES = new String[]{"000", "111", "130", "121", "121", "511", "000", "000", "000", "521"};
    private List<GWTJahiaLanguage> languages;
    private Map<String, CheckBox> checkboxMap;

    public PublicationManagerEngine(Linker linker, List<GWTJahiaLanguage> result) {
        super();
        this.linker = linker;
        this.languages = result;
        setLayout(new FitLayout());
        init();
    }

    /**
     * init
     */
    private void init() {
        setHeading(Messages.get("label.publicationmanager", "Publication Manager"));
        setLayout(new FitLayout());
        setSize(800, 600);
        setBorders(false);
        setBodyBorder(false);
        getHeader().setBorders(false);
        getHeader().setIcon(ToolbarIconProvider.getInstance().getIcon("siteRepository"));

        // tree component
        GWTJahiaNodeTreeFactory factory = new GWTJahiaNodeTreeFactory(Arrays.asList("/sites"), true);
        factory.setNodeTypes(Arrays.asList("jnt:virtualsitesFolder", "jnt:virtualsite", "jmix:publication",
                                           "jmix:worklfowRulesable"));
        factory.setFields(Arrays.asList(GWTJahiaNode.NAME, GWTJahiaNode.DISPLAY_NAME, GWTJahiaNode.PUBLICATION_INFOS,
                                        GWTJahiaNode.WORKFLOW_INFOS));
        factory.setSelectedPath(linker.getMainNode().getPath());
        factory.setSaveOpenPath(true);
        loader = factory.getLoader();
        store = factory.getStore();
        List<ColumnConfig> columns = new LinkedList<ColumnConfig>();
        ColumnConfig config = new ColumnConfig("displayName", "Name", 150);
        config.setRenderer(new TreeGridCellRenderer());
        config.setSortable(false);
        columns.add(config);
        checkboxMap = new HashMap<String, CheckBox>();
        for (GWTJahiaLanguage language : languages) {
            config = new PublicationCheckColumnConfig("publicationInfos", language.getDisplayName(), 150);
            config.setDataIndex(language.getLanguage());
            config.setSortable(false);
            CheckBox checkBox = new CheckBox();
            checkBox.setBoxLabel(language.getDisplayName());
            checkboxMap.put(language.getLanguage(), checkBox);
            config.setWidget(checkBox, language.getLanguage());
            columns.add(config);
        }


        ColumnModel cm = new ColumnModel(columns);
        cm.addHeaderGroup(0, 1, new HeaderGroupConfig("Publication Info", 1, columns.size() - 1));
        m_tree = factory.getTreeGrid(cm);

        for (ColumnConfig column : columns) {
            if (column instanceof CheckColumnConfig) {
                m_tree.addPlugin((ComponentPlugin) column);
            }
        }
        m_tree.setHideHeaders(false);
        m_tree.setIconProvider(ContentModelIconProvider.getInstance());
        m_tree.setAutoExpand(false);
        m_tree.setAutoExpandColumn("displayName");
        m_tree.setBorders(false);

        setScrollMode(Style.Scroll.AUTO);
        add(m_tree);
        ButtonBar buttonBar = new ButtonBar();
        Button button = new Button("Start Workflow");
        buttonBar.add(button);
        setBottomComponent(buttonBar);
        loader.load();
        button.addSelectionListener(new StartWorkflowButtonSelectionListener(this));
    }

    private class StartWorkflowButtonSelectionListener extends SelectionListener<ButtonEvent> {
        private final Window dialog;
        private List<WorkflowActionDialog> dialogList;

        public StartWorkflowButtonSelectionListener(Window window) {
            dialog = window;
            dialogList = new LinkedList<WorkflowActionDialog>();
        }

        @Override
        public void componentSelected(ButtonEvent ce) {
            final ListStore<GWTJahiaNode> store = m_tree.getStore();
            List<GWTJahiaNode> nodes = store.getModels();
            Map<String, GWTJahiaWorkflowDefinition> workflowDefinitionMap = new HashMap<String, GWTJahiaWorkflowDefinition>();
            Map<String, Map<String, List<GWTJahiaNode>>> workflowDefinitionMapMap = new HashMap<String, Map<String, List<GWTJahiaNode>>>();
            for (GWTJahiaNode node : nodes) {
                Record record = store.getRecord(node);
                for (GWTJahiaLanguage language : languages) {
                    if (record != null) {
                        Object o = record.get(language.getLanguage());
                        if (o != null && o instanceof Boolean) {
                            boolean checked = (Boolean) o;
                            if (checked) {
                                Info.display("Worlfow",
                                             "Starting worflow for node " + node.getPath() + " in language " + language.getDisplayName());
                                final GWTJahiaWorkflowDefinition definition = node.getWorkflowInfos().get(
                                        language.getLanguage()).getPossibleWorkflows().get(0);
                                Map<String, List<GWTJahiaNode>> map = workflowDefinitionMapMap.get(
                                        definition.getName());
                                if (map == null) {
                                    map = new HashMap<String, List<GWTJahiaNode>>();
                                    workflowDefinitionMapMap.put(definition.getName(), map);
                                    workflowDefinitionMap.put(definition.getName(), definition);
                                }
                                List<GWTJahiaNode> nodeList = map.get(language.getLanguage());
                                if (nodeList == null) {
                                    nodeList = new LinkedList<GWTJahiaNode>();
                                    map.put(language.getLanguage(), nodeList);
                                }
                                nodeList.add(node);
                            }
                        }
                    }
                }
            }
            for (String definition : workflowDefinitionMapMap.keySet()) {
                Map<String, List<GWTJahiaNode>> map = workflowDefinitionMapMap.get(definition);
                for (String language : map.keySet()) {
                    List<GWTJahiaNode> gwtJahiaNodes = map.get(language);
                    List<String> identifiers = new LinkedList<String>();
                    GWTJahiaNode node = gwtJahiaNodes.get(0);
                    for (GWTJahiaNode jahiaNode : gwtJahiaNodes) {
                        identifiers.add(jahiaNode.getUUID());
                    }
                    WorkflowActionDialog workflowActionDialog = new WorkflowActionDialog(node,
                                                                                         workflowDefinitionMap.get(
                                                                                                 definition),
                                                                                         identifiers, false, linker,
                                                                                         language);
                    workflowActionDialog.addWindowListener(new WindowListener() {
                        @Override
                        public void windowHide(WindowEvent we) {
                            super.windowHide(we);
                            if (!dialogList.isEmpty()) {
                                dialogList.remove(0).show();
                            } else {
                                dialog.hide();
                            }
                        }
                    });
                    dialogList.add(workflowActionDialog);
                }
            }
            dialogList.remove(0).show();
        }
    }

    private class PublicationCheckColumnConfig extends CheckColumnConfig {
        /**
         * Creates a new check column config.
         *
         * @param id    the column id
         * @param name  the column name
         * @param width the column width
         */
        public PublicationCheckColumnConfig(String id, String name, int width) {
            super(id, name, width);
        }

        /**
         * Called to render each check cell.
         *
         * @param model    the model
         * @param property the model property
         * @param config   the config object
         * @param rowIndex the row index
         * @param colIndex the column index
         * @param store    the list store
         * @return the rendered HTML
         */
        @Override
        protected String onRender(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                                  ListStore<ModelData> store) {
            GWTJahiaNode node = (GWTJahiaNode) model;
            int state = getState(node);
            //String title = Messages.get("fm_column_publication_info_" + state, String.valueOf(state));
            StringBuilder builder = new StringBuilder().append("<div class='x-grid3-check-col").append(
                    " x-grid3-check-col").append(getCheckState(node, state)).append(" x-grid3-cc-").append(
                    getId() + "-" + config.name).append("'>").append("<img src=\"").append(
                    JahiaGWTParameters.getContextPath()).append("/gwt/resources/images/workflow/").append(
                    STATE_IMAGES[state]).append(".png\" height=\"12\" width=\"12\" title=\"").append(
                    "\" alt=\"").append("\"/>").append("</div>");
            return builder.toString();
        }

        private int getState(GWTJahiaNode node) {
            int state = node.getPublicationInfos() != null ? node.getPublicationInfos().get(
                    getDataIndex()).getStatus() : 0;
            boolean wfStatus = state == GWTJahiaPublicationInfo.MODIFIED || state == GWTJahiaPublicationInfo.NOT_PUBLISHED;
            if (wfStatus) {
                // is there a workflow started
                GWTJahiaWorkflowInfo info = node.getWorkflowInfos().get(getDataIndex());
                if (info.getAvailableActions().size() > 0) {
                    state = GWTJahiaPublicationInfo.LOCKED;
                }
            }
            return state;
        }

        private String getCheckState(GWTJahiaNode model, int state) {
            Record record = grid.getStore().getRecord(model);
            boolean checked = false;
            if (record != null) {
                Object o = record.get(getDataIndex());
                if (o != null && o instanceof Boolean) {
                    checked = (Boolean) o;
                }
            }
            boolean wfStatus = state == GWTJahiaPublicationInfo.MODIFIED || state == GWTJahiaPublicationInfo.NOT_PUBLISHED;
            return wfStatus ? (checked ? "-on" : "") : "-disabled";
        }

        /**
         * Called when the cell is clicked.
         *
         * @param ge the grid event
         */
        @Override
        protected void onMouseDown(GridEvent<ModelData> ge) {
            String cls = ge.getTarget().getClassName();
            if (cls != null && cls.indexOf("x-grid3-cc-" + getId() + "-" + getDataIndex()) != -1 && cls.indexOf(
                    "disabled") == -1) {
                ge.stopEvent();
                int index = grid.getView().findRowIndex(ge.getTarget());
                ModelData m = grid.getStore().getAt(index);
                Record r = grid.getStore().getRecord(m);
                if (r.get(getDataIndex()) == null) {
                    r.set(getDataIndex(), Boolean.TRUE);
                } else {
                    boolean value = !((Boolean) r.get(getDataIndex()));
                    r.set(getDataIndex(), value);
                    if (!value && checkboxMap.get(getDataIndex()).getValue()) {
                        checkboxMap.get(getDataIndex()).setValue(Boolean.FALSE);
                    }
                }
            }
        }

        @Override
        public void init(Component component) {
            this.grid = (Grid) component;
            grid.addListener(Events.CellClick, new Listener<GridEvent>() {
                public void handleEvent(GridEvent e) {
                    onMouseDown(e);
                }
            });
            grid.addListener(Events.HeaderClick, new Listener<GridEvent>() {
                public void handleEvent(GridEvent e) {
                    onDoubleClick(e);
                }
            });
        }

        private void onDoubleClick(GridEvent<ModelData> ge) {
            ColumnConfig column = grid.getColumnModel().getColumn(ge.getColIndex());
            if (column.getDataIndex().equals(getDataIndex())) {
                checkboxMap.get(getDataIndex()).setValue(!checkboxMap.get(getDataIndex()).getValue());
                final ListStore<GWTJahiaNode> store = m_tree.getStore();
                List<GWTJahiaNode> nodes = store.getModels();
                for (GWTJahiaNode node : nodes) {
                    Record record = store.getRecord(node);
                    int state = getState(node);
                    if (state == GWTJahiaPublicationInfo.MODIFIED || state == GWTJahiaPublicationInfo.NOT_PUBLISHED) {
                        if (record.get(getDataIndex()) == null) {
                            record.set(getDataIndex(), Boolean.TRUE);
                        } else {
                            record.set(getDataIndex(), checkboxMap.get(getDataIndex()).getValue());
                        }
                    }
                }
            }
        }
    }
}