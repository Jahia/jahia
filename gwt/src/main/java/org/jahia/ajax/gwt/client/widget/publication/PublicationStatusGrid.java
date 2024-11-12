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

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.compare.CompareEngine;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineContainer;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.grid.JahiaGroupingView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * User: toto
 * Date: Aug 4, 2010
 * Time: 6:30:03 PM
 */
public class PublicationStatusGrid extends Grid<GWTJahiaPublicationInfo> {

    public PublicationStatusGrid(final List<GWTJahiaPublicationInfo> infos, boolean checkbox, final Linker linker,
                                 final EngineContainer container) {
        super();
        GroupingStore<GWTJahiaPublicationInfo> store = new GroupingStore<GWTJahiaPublicationInfo>();
        store.add(infos);

        store.setStoreSorter(new StoreSorter<GWTJahiaPublicationInfo>() {
            @Override
            public int compare(Store<GWTJahiaPublicationInfo> store, GWTJahiaPublicationInfo m1,
                               GWTJahiaPublicationInfo m2, String property) {
                if (property.equals("mainPath")) {
                    return super.compare(store, m1, m2, "mainPathIndex");
                }
                return super.compare(store, m1, m2, property);
            }
        });
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

//        final CheckColumnConfig checkboxConfig = new CheckColumnConfig("checked", "", 20);
//        configs.add(checkboxConfig);

        ColumnConfig column = new ColumnConfig("title", Messages.get("label.path"), 450);
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
            @Override
            public Object render(final GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex,
                                 int colIndex, ListStore listStore, Grid grid) {
                return SafeHtmlUtils.htmlEscape(model.get("title"));
            }
        });
        configs.add(column);
        column = new ColumnConfig("nodetype", Messages.get("label.nodetype"), 150);
        configs.add(column);

        column = new ColumnConfig("status", Messages.get("org.jahia.jcr.publication.currentStatus"), 150);
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
            @Override
            public Object render(GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex,
                                 int colIndex, ListStore listStore, Grid grid) {
                final String label = GWTJahiaPublicationInfo.statusToLabel.get(model.getStatus());
                String status = Messages.get("label.publication." + label, label);
                if (model.getStatus() == GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE) {
                    return "<span style='color:red'>" + status + "</span>";
                }
                return status;
            }
        });
        configs.add(column);

        column = new ColumnConfig("action", "", 150);
        final List<String> paths = new LinkedList<String>();
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
            @Override
            public Object render(final GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex,
                                 int colIndex, ListStore listStore, Grid grid) {
                final String uuid = model.getMainUUID();
                final String language = model.getLanguage();
                final String path = model.getMainPath();
                ButtonBar buttonBar = new ButtonBar();
                if (!paths.contains(path)) {
                    paths.add(path);
                    Button compare = new Button(Messages.get("label.compare", "Compare"));
                    compare.addStyleName("button-compare");
                    compare.addSelectionListener(new SelectionListener<ButtonEvent>() {
                        @Override
                        public void componentSelected(ButtonEvent ce) {
                            new CompareEngine(uuid, language, false, path).show();
                        }
                    });
                    buttonBar.add(compare);
                }
                Button review = new Button(Messages.get("label.review.content", "Review Content"));
                review.addStyleName("button-review");
                review.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        JahiaContentManagementService.App.getInstance().getNodes(Arrays.asList(model.getPath()), null,
                                new BaseAsyncCallback<List<GWTJahiaNode>>() {
                                    public void onSuccess(List<GWTJahiaNode> result) {
                                        EngineLoader.showEditEngine(linker, result.get(0), true, null);
                                    }
                                });
                    }
                });
                buttonBar.add(review);
                return buttonBar;
            }
        });
        configs.add(column);

        column = new ColumnConfig("mainPath", Messages.get("label.parentObject", "Parent object"), 150);
        column.setHidden(true);
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
            @Override
            public Object render(final GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex,
                                 int colIndex, ListStore listStore, Grid grid) {
                return SafeHtmlUtils.htmlEscape(model.get("mainPath"));
            }
        });
        configs.add(column);

        store.groupBy("mainPath");

        final ColumnModel cm = new ColumnModel(configs);

        setStripeRows(true);
        setBorders(true);

        GroupingView view = new JahiaGroupingView();
        view.setShowGroupedColumn(false);
        view.setForceFit(true);
        view.setGroupRenderer(new GridGroupRenderer() {
            public String render(GroupColumnData data) {
                final ColumnConfig config = cm.getColumnById(data.field);
                String l = data.models.size() == 1 ? Messages.get("label.item", "Item") : Messages.get("label.items",
                        "Items");
                String v = config.getRenderer() != null ? config.getRenderer().render(data.models.get(0), null, null, 0,
                        0, null, null).toString() : data.group.substring(1);
                return SafeHtmlUtils.htmlEscape(v) + " (" + data.models.size() + " " + l + ")";
            }
        });
        setView(view);
        reconfigure(store, cm);
//        addPlugin(checkboxConfig);
        setSelectionModel(new GridSelectionModel<GWTJahiaPublicationInfo>());

    }

    protected void onAfterRenderView() {
        super.onAfterRenderView();
        ((GroupingView) getView()).collapseAllGroups();
    }
}
