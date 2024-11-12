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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;

import java.util.ArrayList;
import java.util.List;

public class NodeUsagesGrid {
    private static final JahiaContentManagementServiceAsync instance = JahiaContentManagementService.App.getInstance();

    public static Grid<GWTJahiaNodeUsage> createUsageGrid(final List<GWTJahiaNode> nodes, final String baseUsageUrl) {
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        ColumnConfig col = new ColumnConfig("pagePath", Messages.get("label.pageUrl", "Page URL"), 200);
        col.setRenderer(new GridCellRenderer<GWTJahiaNodeUsage>() {

            @Override
            public Object render(
                    GWTJahiaNodeUsage gwtJahiaNodeUsage,
                    String s,
                    ColumnData columnData,
                    int i,
                    int i1,
                    ListStore<GWTJahiaNodeUsage> gwtJahiaNodeUsageListStore,
                    com.extjs.gxt.ui.client.widget.grid.Grid<GWTJahiaNodeUsage> gwtJahiaNodeUsageGrid) {
                if (gwtJahiaNodeUsage.getLanguage() == null) {
                    return
                            "<a href=\"" + baseUsageUrl +
                                    gwtJahiaNodeUsage.getPagePath() + ".html\" target=\"_blank\">" +
                                    gwtJahiaNodeUsage.getPageTitle() + "<a>";
                } else {
                    return
                            "<a href=\"" + baseUsageUrl + "/../" + gwtJahiaNodeUsage.getLanguage() +
                                    gwtJahiaNodeUsage.getPagePath() + ".html\" target=\"_blank\">" +
                                    gwtJahiaNodeUsage.getPageTitle() + " (" + gwtJahiaNodeUsage.getLanguage() + ")<a>";

                }
            }
        });

        columns.add(col);

        col = new ColumnConfig("path", Messages.get("label.path", "URL"), 600);
        columns.add(col);
        ColumnModel cm = new ColumnModel(columns);
        final ListStore<GWTJahiaNodeUsage> usageStore = new ListStore<GWTJahiaNodeUsage>(new BaseListLoader(
                new RpcProxy<List<GWTJahiaNodeUsage>>() {
                    @Override
                    protected void load(Object loadConfig, final AsyncCallback<List<GWTJahiaNodeUsage>> asyncCallback) {
                        List<String> paths = new ArrayList<String>();
                        for (GWTJahiaNode selectedNode : nodes) {
                            paths.add(selectedNode.getPath());
                        }
                        instance.getUsages(paths, new BaseAsyncCallback<List<GWTJahiaNodeUsage>>() {

                            @Override
                            public void onApplicationFailure(Throwable throwable) {
                                Log.debug("Cannot retrieve usages", throwable);
                            }

                            @Override
                            public void onSuccess(List<GWTJahiaNodeUsage> gwtJahiaNodeUsages) {
                                asyncCallback.onSuccess(gwtJahiaNodeUsages);
                            }
                        });
                    }
                }));
        final Grid<GWTJahiaNodeUsage> tbl = new Grid<GWTJahiaNodeUsage>(usageStore, cm);

        tbl.addListener(Events.RowDoubleClick, new Listener<GridEvent<?>>() {

            @Override
            public void handleEvent(GridEvent<?> tableEvent) {
                Object url = tableEvent.getModel().get("path");
                if (url != null && url instanceof String) {
                    instance.getRenderedContent((String) url, null, JahiaGWTParameters.getUILanguage(), null,
                            "module", null, false, null, null, null, new BaseAsyncCallback<GWTRenderResult>() {

                                @Override
                                public void onSuccess(GWTRenderResult result) {
                                    HTML html = new HTML(result.getResult());
                                    Window w = new Window();
                                    w.addStyleName("node-usage-dblclick-preview");
                                    w.setScrollMode(Style.Scroll.AUTO);
                                    w.setModal(true);
                                    w.setClosable(true);
                                    w.setMaximizable(true);
                                    w.setSize(800, 600);
                                    w.setBlinkModal(true);
                                    w.setPlain(true);
                                    w.setLayout(new CenterLayout());
                                    w.add(html);
                                    w.show();
                                }

                            });
                }
            }
        });
        usageStore.getLoader().load();
        return tbl;
    }
}
