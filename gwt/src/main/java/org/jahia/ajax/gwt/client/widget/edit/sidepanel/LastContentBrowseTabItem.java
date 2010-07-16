package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Side panel tab item for browsing the content repository.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:24 PM
 */
class LastContentBrowseTabItem extends SidePanelTabItem {
    protected LayoutContainer contentContainer;
    protected ListStore<GWTJahiaNode> contentStore;
    protected DisplayGridDragSource displayGridSource;

    public LastContentBrowseTabItem(final GWTSidePanelTab config) {
        super(config);
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        setLayout(l);
        contentContainer = new LayoutContainer();
        contentContainer.setBorders(true);
        contentContainer.setScrollMode(Style.Scroll.AUTO);
        contentContainer.setLayout(new FitLayout());
        contentStore = new ListStore<GWTJahiaNode>();


        List<ColumnConfig> displayColumns = new ArrayList<ColumnConfig>();

        ColumnConfig col = new ColumnConfig("icon", "", 40);
        col.setAlignment(Style.HorizontalAlignment.CENTER);
        col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                                 ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
                return ContentModelIconProvider.getInstance().getIcon(modelData).getHTML();
            }
        });
        displayColumns.add(col);
        displayColumns.add(new ColumnConfig("displayName", Messages.getResource("label.name"), 170));
        ColumnConfig columnConfig = new ColumnConfig("jcr:lastModified", Messages.getResource("label.lastModif"),
                                                     100);
        columnConfig.setDateTimeFormat(DateTimeFormat.getShortDateTimeFormat());
        displayColumns.add(columnConfig);
        final Grid<GWTJahiaNode> grid = new Grid<GWTJahiaNode>(contentStore, new ColumnModel(displayColumns));

        contentContainer.add(grid);

        grid.setContextMenu(createContextMenu(config.getTableContextMenu(), grid.getSelectionModel()));
        grid.getStore().setMonitorChanges(true);

        final LayoutContainer previewLayoutContainer = new LayoutContainer();
        previewLayoutContainer.setLayout(new FitLayout());
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> gwtJahiaNodeSelectionChangedEvent) {
                JahiaContentManagementService.App.getInstance().getRenderedContent(
                        gwtJahiaNodeSelectionChangedEvent.getSelectedItem().getPath(), null, editLinker.getLocale(),
                        "default", "preview", null, true, config.getName(),
                        new BaseAsyncCallback<GWTRenderResult>() {

                            public void onSuccess(GWTRenderResult gwtRenderResult) {
                                previewLayoutContainer.removeAll();
                                previewLayoutContainer.add(new HTML(gwtRenderResult.getResult()));
                                previewLayoutContainer.layout(true);
                            }
                        });
            }
        });

        VBoxLayoutData contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(2);
        add(contentContainer, contentVBoxData);
        contentVBoxData.setFlex(1);
        add(previewLayoutContainer, contentVBoxData);

        displayGridSource = new DisplayGridDragSource(grid);
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        displayGridSource.addDNDListener(editLinker.getDndListener());
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        fillStore();
    }

    private void fillStore() {
        contentContainer.mask("Loading", "x-mask-loading");
        contentStore.removeAll();
        JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
        async.searchSQL(config.getParams().get("search"), Integer.valueOf(config.getParams().get("limit")),
                        JCRClientUtils.CONTENT_NODETYPES, null, null, Arrays.asList(GWTJahiaNode.ICON,"jcr:lastModified"), new BaseAsyncCallback<List<GWTJahiaNode>>() {
                    public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                        contentStore.add(gwtJahiaNodes);
                        contentContainer.layout(true);
                        contentContainer.unmask();
                    }
                });
    }

    @Override
    public void refresh(int flag) {
        if ((flag & Linker.REFRESH_PAGES) != 0) {
            fillStore();
        }

    }
}