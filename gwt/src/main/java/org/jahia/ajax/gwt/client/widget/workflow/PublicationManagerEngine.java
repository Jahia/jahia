package org.jahia.ajax.gwt.client.widget.workflow;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ComponentPlugin;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
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
    private static String[] STATE_IMAGES = new String[]{"000", "111", "130", "121", "121", "511", "000", "000", "000"};
    private List<GWTJahiaLanguage> languages;

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
        GWTJahiaNodeTreeFactory factory = new GWTJahiaNodeTreeFactory(Arrays.asList("/sites"));
        factory.setNodeTypes(Arrays.asList("jnt:virtualsitesFolder", "jnt:virtualsite", "jnt:page"));
        factory.setFields(Arrays.asList(GWTJahiaNode.NAME, GWTJahiaNode.DISPLAY_NAME, GWTJahiaNode.PUBLICATION_INFOS,
                                        GWTJahiaNode.WORKFLOW_INFO));
        factory.setSelectedPath(linker.getMainNode().getPath());
        factory.setSaveOpenPath(true);
        loader = factory.getLoader();
        store = factory.getStore();
        List<ColumnConfig> columns = new LinkedList<ColumnConfig>();
        ColumnConfig config = new ColumnConfig("displayName", "Name", 150);
        config.setRenderer(new TreeGridCellRenderer());
        columns.add(config);

        for (GWTJahiaLanguage language : languages) {
            config = new CheckColumnConfig("publicationInfos", language.getDisplayName(), 150) {
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
                protected String onRender(ModelData model, String property, ColumnData config, int rowIndex,
                                          int colIndex, ListStore<ModelData> store) {
                    GWTJahiaNode node = (GWTJahiaNode) model;
                    int state = node.getPublicationInfos() != null ? node.getPublicationInfos().get(
                            config.name).getStatus() : 0;
                    //String title = Messages.get("fm_column_publication_info_" + state, String.valueOf(state));
                    StringBuilder builder = new StringBuilder().append("<div class='x-grid3-check-col").append(
                            " x-grid3-check-col").append(getCheckState(model, state)).append(" x-grid3-cc-").append(
                            getId() + "-" + config.name).append("'>").append("<img src=\"").append(
                            JahiaGWTParameters.getContextPath()).append("/gwt/resources/images/workflow/").append(
                            STATE_IMAGES[state]).append(".png\" height=\"12\" width=\"12\" title=\"").append(
                            "\" alt=\"").append("\"/>").append("</div>");
                    return builder.toString();
                }

                private String getCheckState(ModelData model, int state) {
                    Record record = grid.getStore().getRecord(model);
                    boolean checked = false;
                    if (record != null) {
                        Object o = record.get(getDataIndex());
                        if (o != null && o instanceof Boolean) {
                            checked = (Boolean) o;
                        }
                    }
                    return state == GWTJahiaPublicationInfo.MODIFIED || state == GWTJahiaPublicationInfo.NOT_PUBLISHED ? (checked ? "-on" : "") : "-disabled";
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
                            r.set(getDataIndex(), !((Boolean) r.get(getDataIndex())));
                        }
                    }
                }
            };
            config.setDataIndex(language.getLanguage());
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
        final List<WorkflowActionDialog> dialogList = new LinkedList<WorkflowActionDialog>();
        final Window dialog = this;
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                final ListStore<GWTJahiaNode> store = m_tree.getStore();
                List<GWTJahiaNode> nodes = store.getModels();
                Map<String, GWTJahiaWorkflowDefinition> workflowDefinitionMap = new HashMap<String, GWTJahiaWorkflowDefinition>();
                Map<String, Map<String, List<String>>> workflowDefinitionMapMap = new HashMap<String, Map<String, List<String>>>();
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
                                    final GWTJahiaWorkflowDefinition definition = node.getWorkflowInfo().getPossibleWorkflows().get(
                                            0);
                                    Map<String, List<String>> map = workflowDefinitionMapMap.get(definition.getName());
                                    if (map == null) {
                                        map = new HashMap<String, List<String>>();
                                        workflowDefinitionMapMap.put(definition.getName(), map);
                                        workflowDefinitionMap.put(definition.getName(), definition);
                                    }
                                    List<String> nodeList = map.get(language.getLanguage());
                                    if (nodeList == null) {
                                        nodeList = new LinkedList<String>();
                                        map.put(language.getLanguage(), nodeList);
                                    }
                                    nodeList.add(node.getUUID());
                                }
                            }
                        }
                    }
                }
                for (String definition : workflowDefinitionMapMap.keySet()) {
                    Map<String, List<String>> map = workflowDefinitionMapMap.get(definition);
                    for (String language : map.keySet()) {
                        WorkflowActionDialog workflowActionDialog = new WorkflowActionDialog(linker.getMainNode(),
                                                                                             workflowDefinitionMap.get(
                                                                                                     definition),
                                                                                             map.get(language), false,
                                                                                             linker, language);
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
        });
    }


}