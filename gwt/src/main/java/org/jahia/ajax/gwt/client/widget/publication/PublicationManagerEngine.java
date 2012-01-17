/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
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
        setModal(true);
        getHeader().setBorders(false);
        getHeader().setIcon(ToolbarIconProvider.getInstance().getIcon("siteRepository"));

        // tree component
        GWTJahiaNodeTreeFactory factory = new GWTJahiaNodeTreeFactory(Arrays.asList("/sites/"+linker.getSelectionContext().getMainNode().getSiteKey()), true);
        factory.setNodeTypes(Arrays.asList("jmix:publication","jmix:workflowRulesable"));
        factory.setFields(Arrays.asList(GWTJahiaNode.NAME, GWTJahiaNode.DISPLAY_NAME, GWTJahiaNode.PUBLICATION_INFOS,
                                        GWTJahiaNode.WORKFLOW_INFOS));
        factory.setSelectedPath(linker.getSelectionContext().getMainNode().getPath());
        factory.setSaveOpenPath(true);
        loader = factory.getLoader();
        List<ColumnConfig> columns = new LinkedList<ColumnConfig>();
        ColumnConfig config = new ColumnConfig("displayName", "Name", 150);
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
        m_tree.setAutoExpandColumn("displayName");
        m_tree.setBorders(true);

        setScrollMode(Style.Scroll.AUTO);
        add(m_tree);
        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);
        Button button = new Button(Messages.get("label.customWorkflowsMenu", "Start workflow"));
        buttonBar.add(button);
        setBottomComponent(buttonBar);

        m_tree.mask(Messages.get("label.loading","Loading..."), "x-mask-loading");

        loader.addLoadListener(new LoadListener() {
            public void loaderLoad(LoadEvent le) {
                m_tree.unmask();
            }
        });
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
            final Map<String, GWTJahiaWorkflowDefinition> workflowDefinitionMap = new HashMap<String, GWTJahiaWorkflowDefinition>();
            Map<String, Map<String, List<GWTJahiaNode>>> workflowDefinitionMapMap = new HashMap<String, Map<String, List<GWTJahiaNode>>>();


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

            PublicationWorkflow.create(all, linker);
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
                Image res = GWTJahiaPublicationInfo.renderPublicationStatusImage(info.getStatus());
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

            boolean b = GWTJahiaPublicationInfo.canPublish(node, info, JahiaGWTParameters.getLanguage());
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