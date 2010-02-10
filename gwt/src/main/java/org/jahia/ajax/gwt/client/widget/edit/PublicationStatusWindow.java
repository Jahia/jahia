package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
* Created by IntelliJ IDEA.
* User: toto
* Date: Jan 28, 2010
* Time: 2:44:46 PM
* To change this template use File | Settings | File Templates.
*/
class PublicationStatusWindow extends Window {

    PublicationStatusWindow(final Linker linker, final GWTJahiaNode selectedNode, GWTJahiaPublicationInfo info) {
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

        ColumnConfig column = new ColumnConfig();
        column.setId("title");
        column.setHeader(Messages.getResource("publication_path"));
        column.setWidth(450);
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
            @Override
            public Object render(GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                return model.get(property);
            }
        });
        configs.add(column);

        column = new ColumnConfig();
        column.setId("status");
        column.setHeader(Messages.getResource("publication_currentStatus"));
        column.setWidth(150);
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

        column = new ColumnConfig();
        column.setId("canPublish");
        column.setHeader(Messages.getResource("publication_publicationAllowed"));
        column.setWidth(150);
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
            @Override
            public Object render(GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                return model.isCanPublish().toString();
            }
        });
        configs.add(column);

        ListStore<GWTJahiaPublicationInfo> store = new ListStore<GWTJahiaPublicationInfo>();
        if (info.getStatus() != GWTJahiaPublicationInfo.PUBLISHED) {
            store.add(info);
        }
        for (ModelData data : info.getChildren()) {
            GWTJahiaPublicationInfo subInfo = (GWTJahiaPublicationInfo) data;
            if (subInfo.getStatus() != GWTJahiaPublicationInfo.PUBLISHED) {
                store.add(subInfo);
            }
        }
        ColumnModel cm = new ColumnModel(configs);

        final Grid<GWTJahiaPublicationInfo> grid = new Grid<GWTJahiaPublicationInfo>(store, cm) {

            @Override
            protected void afterRenderView() {
                super.afterRenderView();
                getSelectionModel().selectAll();
            }

        };
        grid.setStripeRows(true);
        grid.setBorders(true);

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
                JahiaContentManagementService.App.getInstance().publish(selectedNode.getPath(), false, comments.getValue(), false, new AsyncCallback() {
                    public void onFailure(Throwable caught) {
                        Log.error("Cannot publish", caught);
                        com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                        hide();
                    }

                    public void onSuccess(Object result) {
                        Info.display(Messages.getResource("publication_published_title"), Messages.getResource("publication_published_text"));
                        linker.refresh();
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
