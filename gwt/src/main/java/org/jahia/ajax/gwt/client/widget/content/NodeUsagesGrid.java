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
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;

import java.util.ArrayList;
import java.util.List;

public class NodeUsagesGrid {
    private static final JahiaContentManagementServiceAsync instance = JahiaContentManagementService.App.getInstance();

    public static Grid<GWTJahiaNodeUsage> createUsageGrid(final List<GWTJahiaNode> nodes) {
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        ColumnConfig col = new ColumnConfig("url", "URL", 300);
        columns.add(col);
        /*col = new ColumnConfig("lang", Messages.getResource("fm_language"), 130);
    col.setAlignment(Style.HorizontalAlignment.CENTER);
    columns.add(col);
    col = new ColumnConfig(Messages.getResource("fm_workflow"), 30);
    col.setAlignment(Style.HorizontalAlignment.CENTER);
    col.setRenderer(new GridCellRenderer<GWTJahiaNodeUsage>() {
        public Object render(GWTJahiaNodeUsage gwtJahiaNodeUsage, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNodeUsage> gwtJahiaNodeUsageListStore, com.extjs.gxt.ui.client.widget.grid.Grid<GWTJahiaNodeUsage> gwtJahiaNodeUsageGrid) {
            String[] ws = new String[]{Messages.getResource("fm_versioned"), Messages.getResource("fm_live"), Messages.getResource("fm_staging"), Messages.getResource("fm_notify")};
            String[] images = new String[]{"600", "111", "121", "130"};
            return "<img src=\"../images/icons/workflow/" + images[gwtJahiaNodeUsage.getWorkflow()] + ".png\">&nbsp;" + ws[gwtJahiaNodeUsage.getWorkflow()];
        }
    });
    col.setHidden(true);
    columns.add(col);
    col = new ColumnConfig("versionName", Messages.getResource("fm_version"), 130);
    col.setAlignment(Style.HorizontalAlignment.CENTER);
    columns.add(col);*/

        ColumnModel cm = new ColumnModel(columns);
        final ListStore<GWTJahiaNodeUsage> usageStore = new ListStore<GWTJahiaNodeUsage>(new BaseListLoader(
                new RpcProxy() {
                    @Override
                    protected void load(Object loadConfig, final AsyncCallback asyncCallback) {
                        for (GWTJahiaNode selectedNode : nodes) {
                            String path = selectedNode.getPath();

                            instance.getUsages(path, new AsyncCallback<List<GWTJahiaNodeUsage>>() {
                                public void onFailure(Throwable throwable) {
                                    Log.debug("Cannot retrieve usages", throwable);
                                }

                                public void onSuccess(List<GWTJahiaNodeUsage> gwtJahiaNodeUsages) {
                                    asyncCallback.onSuccess(gwtJahiaNodeUsages);
                                }
                            });
                        }
                    }
                }));
        final Grid<GWTJahiaNodeUsage> tbl = new Grid<GWTJahiaNodeUsage>(usageStore, cm);
        
        tbl.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent tableEvent) {
                Object url = tableEvent.getModel().get("url");
                if (url != null && url instanceof String) {
                    instance.getRenderedContent((String) url, null, JahiaGWTParameters.getLanguage(), null,
                                                "wrapper.previewwrapper", null, false, new AsyncCallback<GWTRenderResult>() {
                                public void onSuccess(GWTRenderResult result) {
                                    HTML html = new HTML(result.getResult());
                                    Window w = new Window();
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

                                public void onFailure(Throwable caught) {
                                    Log.error("", caught);
                                    //                    com.google.gwt.user.client.Window.alert("-update preview pp->" + caught.getMessage());
                                }
                            });
                }
            }
        });
        usageStore.getLoader().load();
        return tbl;
    }
}