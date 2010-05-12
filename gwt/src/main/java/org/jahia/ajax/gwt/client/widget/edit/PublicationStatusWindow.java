package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* Created by IntelliJ IDEA.
* User: toto
* Date: Jan 28, 2010
* Time: 2:44:46 PM
* To change this template use File | Settings | File Templates.
*/
class PublicationStatusWindow extends Window {

    PublicationStatusWindow(final Linker linker, Map<String, GWTJahiaPublicationInfo> infos) {
        setLayout(new BorderLayout());

        setScrollMode(Style.Scroll.NONE);
        setHeading("Publish");
        setSize(800, 500);
        setResizable(false);

        setModal(true);

        final TextArea comments = new TextArea();
        comments.setWidth(750);
        comments.setFieldLabel(Messages.getResource("publication_publicationComments"));

        VerticalPanel commentsPanel = new VerticalPanel();
        TableData d = new TableData(Style.HorizontalAlignment.CENTER, Style.VerticalAlignment.MIDDLE);
        d.setMargin(5);
        commentsPanel.setHorizontalAlign(Style.HorizontalAlignment.CENTER);
        commentsPanel.add(new Label("Comments"), d);
        commentsPanel.add(comments, d);
        commentsPanel.setHeight(70);
        commentsPanel.setWidth("100%");

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig("title",Messages.getResource("publication_path"),450);
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
            @Override
            public Object render(GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                return model.get(property);
            }
        });
        configs.add(column);

        column = new ColumnConfig("status",Messages.getResource("publication_currentStatus"),150);
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
            @Override
            public Object render(GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                switch (model.getStatus()) {
                    case GWTJahiaPublicationInfo.NOT_PUBLISHED:
                        return Messages.getResource("publication_status_notyetpublished");
                    case GWTJahiaPublicationInfo.PUBLISHED:
                        return Messages.getResource("publication_status_published");
                    case GWTJahiaPublicationInfo.MODIFIED:
                        return Messages.getResource("publication_status_modified");
                    case GWTJahiaPublicationInfo.UNPUBLISHABLE:
                        return Messages.getResource("publication_status_notyetpublished");
                    case GWTJahiaPublicationInfo.UNPUBLISHED:
                        return "Unpublished";
                    case GWTJahiaPublicationInfo.CONFLICT:
                        return "Conflict - cannot publish";
                }
                return "";
            }
        });
        configs.add(column);

        column = new ColumnConfig("canPublish",Messages.getResource("publication_publicationAllowed"),150);
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
            @Override
            public Object render(GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                return model.isCanPublish().toString();
            }
        });
        configs.add(column);
        column = new ColumnConfig("mainTitle", "Parent object", 150);
        column.setHidden(true);
        configs.add(column);
        GroupingStore<GWTJahiaPublicationInfo> store = new GroupingStore<GWTJahiaPublicationInfo>();

        final List<String> paths = new ArrayList<String>();

        for (GWTJahiaPublicationInfo info : infos.values()) {
            if (info.getStatus() != GWTJahiaPublicationInfo.PUBLISHED) {
                info.set("mainTitle", info.getTitle());
                store.add(info);
                paths.add(info.getPath());
            }
            for (ModelData data : info.getChildren()) {
                GWTJahiaPublicationInfo subInfo = (GWTJahiaPublicationInfo) data;
                if (subInfo.getStatus() != GWTJahiaPublicationInfo.PUBLISHED) {
                    subInfo.set("mainTitle", info.getTitle());
                    store.add(subInfo);
                }
            }
        }
        store.groupBy("mainTitle");
        final ColumnModel cm = new ColumnModel(configs);

        final Grid<GWTJahiaPublicationInfo> grid = new Grid<GWTJahiaPublicationInfo>(store, cm) {

            @Override
            protected void afterRenderView() {
                super.afterRenderView();
                getSelectionModel().selectAll();
            }

        };
        grid.setStripeRows(true);
        grid.setBorders(true);

        GroupingView view = new GroupingView();
        view.setShowGroupedColumn(false);
        view.setForceFit(true);
        view.setGroupRenderer(new GridGroupRenderer() {
            public String render(GroupColumnData data) {
                final ColumnConfig config = cm.getColumnById(data.field);
                String f = config.getHeader();
                String l = data.models.size() == 1 ? "Item" : "Items";  
                String v = config.getRenderer() != null ? config.getRenderer().render(data.models.get(0), null,null,0,0,null,null).toString() : data.group;
                return f + ": " + v + " (" + data.models.size() + " " + l + ")";
            }
        });
        grid.setView(view);

        BorderLayoutData borderData = new BorderLayoutData(Style.LayoutRegion.CENTER);
//        borderData.setMargins(new Margins(5));
        add(grid, borderData);
        borderData = new BorderLayoutData(Style.LayoutRegion.SOUTH, 80);
//        borderData.setMargins(new Margins(5));
        add(commentsPanel, borderData);

        final Button cancel = new Button(Messages.getResource("fm_cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });
        final Button ok = new Button(Messages.getResource("publication_publish"));
        SelectionListener<ButtonEvent> selectionListener = new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                ok.setEnabled(false);
                cancel.setEnabled(false);
                JahiaContentManagementService.App.getInstance().publish(paths, false, comments.getValue(), false, new AsyncCallback() {
                    public void onFailure(Throwable caught) {
                        Log.error("Cannot publish", caught);
                        com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                        hide();
                    }

                    public void onSuccess(Object result) {
                        Info.display(Messages.getResource("publication_published_title"), Messages.getResource("publication_published_text"));
                        linker.refresh(Linker.REFRESH_ALL);
                        hide();
                    }
                });
            }
        };
        ok.addSelectionListener(selectionListener);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        addButton(ok);
        addButton(cancel);
    }
}
