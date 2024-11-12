/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.publication;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowActionDialog;

import java.util.*;

/**
 * PublicationManagerEngine allows to launch publication process for different languages from one simple UI.
 * User: rincevent
 * Date: Apr 28, 2010
 * Time: 4:32:33 PM
 */
public class PublicationManagerEngine extends Window {
    private final Linker linker;
    private TreeLoader<GWTJahiaNode> loader;
    private TreeGrid<GWTJahiaNode> m_tree;

    private List<GWTJahiaLanguage> languages;
    private Map<String, LayoutContainer> checkboxMap;
    private List<String> nodeTypes;

    public PublicationManagerEngine(Linker linker, List<GWTJahiaLanguage> result, List<String> nodeTypes) {
        super();
        addStyleName("publication-manager-engine");
        this.linker = linker;
        this.languages = result;
        this.nodeTypes = nodeTypes;
        setLayout(new FitLayout());
        init();
    }

    /**
     * init
     */
    private void init() {
        setHeadingHtml(Messages.get("label.publicationmanager", "Publication Manager"));
        setLayout(new FitLayout());
        setSize(800, 600);
        setBorders(false);
        setBodyBorder(false);
        setModal(true);
        setMaximizable(true);
        getHeader().setBorders(false);
        getHeader().setIcon(ToolbarIconProvider.getInstance().getIcon("siteRepository"));

        // tree component
        GWTJahiaNodeTreeFactory factory = new GWTJahiaNodeTreeFactory(Arrays.asList("/sites/"+linker.getSelectionContext().getMainNode().getSiteKey()), true);
        factory.setNodeTypes(nodeTypes);
        factory.setFields(Arrays.asList(GWTJahiaNode.NAME, GWTJahiaNode.DISPLAY_NAME, GWTJahiaNode.PUBLICATION_INFOS,
                                        GWTJahiaNode.WORKFLOW_INFOS));
        factory.setSelectedPath(linker.getSelectionContext().getMainNode().getPath());
        loader = factory.getLoader();
        List<ColumnConfig> columns = new LinkedList<ColumnConfig>();
        ColumnConfig config = new ColumnConfig("displayName", Messages.get("label.name", "Name"), 150);
        config.setRenderer(NodeColumnConfigList.NAME_TREEGRID_RENDERER);
        config.setSortable(false);
        columns.add(config);
        checkboxMap = new HashMap<String, LayoutContainer>();
        for (GWTJahiaLanguage language : languages) {
            config = new PublicationCheckColumnConfig("publicationInfos", language.getDisplayName(), 100);
            config.setDataIndex(language.getLanguage());
            config.setSortable(false);

            TableData td = new TableData();
            td.setHorizontalAlign(Style.HorizontalAlignment.CENTER);
            td.setVerticalAlign(Style.VerticalAlignment.MIDDLE);

            HorizontalPanel p = new HorizontalPanel();
//            final LayoutContainer ctn = new LayoutContainer();
//            ctn.addStyleName("x-grid3-check-col");
//            ctn.setWidth(16);
//            ctn.setHeight(16);
//            p.add(ctn,td);
            p.add(new Text(language.getDisplayName()),td);
            config.setWidget(p, language.getLanguage());
//            checkboxMap.put(language.getLanguage(), ctn);
            columns.add(config);
        }


        ColumnModel cm = new ColumnModel(columns);
//        cm.addHeaderGroup(0, 1, new HeaderGroupConfig("Publication Info", 1, columns.size() - 1));
        m_tree = factory.getTreeGrid(cm);

        for (ColumnConfig column : columns) {
            if (column instanceof CheckColumnConfig) {
                m_tree.addPlugin((ComponentPlugin) column);
            }
        }
        m_tree.setHideHeaders(false);
        m_tree.setIconProvider(ContentModelIconProvider.getInstance());
        m_tree.setAutoExpand(false);
        m_tree.setAutoExpandMax(1000);
        m_tree.setAutoExpandMin(150);
        m_tree.setAutoExpandColumn("displayName");
        m_tree.setBorders(true);

        setScrollMode(Style.Scroll.AUTO);
        add(m_tree);
        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        Button button = new Button(Messages.get("label.publish", "Publish"));
        button.addStyleName("button-publish");
        buttonBar.add(button);
        Button cancelButton = new Button(Messages.get("label.cancel", "Cancel"));
        cancelButton.addStyleName("button-cancel");
        buttonBar.add(cancelButton);
        setBottomComponent(buttonBar);

        m_tree.mask(Messages.get("label.loading","Loading..."), "x-mask-loading");

        loader.addLoadListener(new LoadListener() {
            public void loaderLoad(LoadEvent le) {
                m_tree.unmask();
            }
        });
        loader.load();

        cancelButton.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                hide();
            }
        });
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

            List<GWTJahiaPublicationInfo> all = new ArrayList<GWTJahiaPublicationInfo>();
            for (GWTJahiaNode node : nodes) {
                Record record = store.getRecord(node);
                for (GWTJahiaLanguage language : languages) {
                    if (record != null) {
                        Object o = record.get(language.getLanguage());
                        if (o != null && o instanceof Boolean) {
                            boolean checked = (Boolean) o;
                            if (checked) {
                                final List<GWTJahiaPublicationInfo> list =
                                        node.getFullPublicationInfos().get(language.getLanguage());
                                if (list != null) {
                                    all.addAll(list);
                                }
                            }
                        }
                    }
                }
            }

            if (all.isEmpty()) {
                MessageBox.info(Messages.get("label.publish", "Publication"), Messages.get("label.publication.nothingToPublish", "Nothing to publish"), null);
                return;
            }

            PublicationWorkflow.create(all, linker, false);
            hide();
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
            setRenderer(new GridCellRenderer<ModelData>() {
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                                     ListStore<ModelData> listStore, Grid<ModelData> grid) {
                    return renderHTML(model,property, config, rowIndex, colIndex, listStore);
                }
            });
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
        protected Object renderHTML(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                                  ListStore<ModelData> store) {

            GWTJahiaNode node = (GWTJahiaNode) model;
            Map<String,GWTJahiaPublicationInfo> infos = node.getAggregatedPublicationInfos();
            if (infos != null) {
                GWTJahiaPublicationInfo info = infos.get(getDataIndex());

                TableData td = new TableData();
                td.setHorizontalAlign(Style.HorizontalAlignment.CENTER);
                td.setVerticalAlign(Style.VerticalAlignment.MIDDLE);

                HorizontalPanel p = new HorizontalPanel();
                final LayoutContainer ctn = new LayoutContainer();
                ctn.addStyleName("x-grid3-check-col");
                ctn.addStyleName("x-grid3-check-col" + getCheckState(node, info));
                ctn.addStyleName("x-grid3-cc-"+ getId() + "-" + config.name);
                ctn.setWidth(16);
                ctn.setHeight(16);
                p.add(ctn,td);
                Image res = GWTJahiaPublicationInfo.renderPublicationStatusImage(info);
                p.add(res, td);
                if (info.isLocked()) {
                    p.add(StandardIconsProvider.STANDARD_ICONS.lock().createImage());
                    ctn.addStyleName("x-grid3-check-col-disabled");
                }
                return p;
            } else {
                return "";
            }
        }

        private int getState(GWTJahiaNode node) {
            return node.getAggregatedPublicationInfos() != null ? node.getAggregatedPublicationInfos().get(
                    getDataIndex()).getStatus() : 0;
        }

        private String getCheckState(GWTJahiaNode model, GWTJahiaPublicationInfo info) {
            Record record = grid.getStore().getRecord(model);
            boolean checked = false;
            if (record != null) {
                Object o = record.get(getDataIndex());
                if (o != null && o instanceof Boolean) {
                    checked = (Boolean) o;
                }
            }
            boolean wfStatus = getStatus(model, info);
            return wfStatus ? (checked ? "-on" : "") : "-disabled";
        }

        public boolean getStatus(GWTJahiaNode node, GWTJahiaPublicationInfo info) {
            if (info.get("checkboxEnabled") != null) {
                return info.<Boolean>get("checkboxEnabled").booleanValue();
            }

            boolean b = info.isPublishable() && (info.getWorkflowDefinition() != null || info.isAllowedToPublishWithoutWorkflow());
            info.set("checkboxEnable", b);
            return b;
        }

        /**
         * Called when the cell is clicked.
         *
         * @param ge the grid event
         */
        @Override
        protected void onMouseDown(GridEvent<ModelData> ge) {
            String cls = ge.getTarget().getClassName();
            if (cls != null &&
            		cls.indexOf("x-grid3-cc-" + getId() + "-" + getDataIndex() + " ") != -1 &&
            		cls.indexOf("disabled") == -1) {
                ge.stopEvent();
                int index = grid.getView().findRowIndex(ge.getTarget());
                ModelData m = grid.getStore().getAt(index);
                Record r = grid.getStore().getRecord(m);
                if (r.get(getDataIndex()) == null) {
                    r.set(getDataIndex(), Boolean.TRUE);
                } else {
                    boolean value = !((Boolean) r.get(getDataIndex()));
                    r.set(getDataIndex(), value);
//                    boolean b = checkboxMap.get(getDataIndex()).getStyleName().contains("x-grid3-check-col-on");
//                    if (!value && b) {
//                        checkboxMap.get(getDataIndex()).setValue(Boolean.FALSE);
//                    }
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
//            grid.addListener(Events.HeaderClick, new Listener<GridEvent>() {
//                public void handleEvent(GridEvent e) {
//                    onHeaderClick(e);
//                }
//            });
        }

//        private void onHeaderClick(GridEvent<ModelData> ge) {
//            ColumnConfig column = grid.getColumnModel().getColumn(ge.getColIndex());
//            if (column.getDataIndex().equals(getDataIndex())) {
//                final LayoutContainer c = checkboxMap.get(getDataIndex());
//                if (c.getStyleName().contains("x-grid3-check-col-on")) {
//                    c.removeStyleName("x-grid3-check-col-on");
//                } else {
//                    c.addStyleName("x-grid3-check-col-on");
//                }
//                final ListStore<GWTJahiaNode> store = m_tree.getStore();
//                List<GWTJahiaNode> nodes = store.getModels();
//                for (GWTJahiaNode node : nodes) {
//                    Record record = store.getRecord(node);
//                    if (node.getAggregatedPublicationInfos() != null) {
//                        boolean wfStatus = getStatus(node, node.getAggregatedPublicationInfos().get(getDataIndex()));
//                        if (wfStatus) {
//                            if (record.get(getDataIndex()) == null) {
//                                record.set(getDataIndex(), Boolean.TRUE);
//                            } else {
//                                record.set(getDataIndex(), c.getStyleName().contains("x-grid3-check-col-on"));
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }
}
