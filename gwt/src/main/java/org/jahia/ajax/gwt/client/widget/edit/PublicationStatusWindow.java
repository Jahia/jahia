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
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* Window, displaying the current publication status.
* User: toto
* Date: Jan 28, 2010
* Time: 2:44:46 PM
*/
class PublicationStatusWindow extends Window {
    protected Linker linker;
    protected Button ok;
    protected Button noWorkflow;
    protected Button cancel;
    protected TextArea comments;

    PublicationStatusWindow(final Linker linker, final Map<String, GWTJahiaPublicationInfo> infos) {
        setLayout(new BorderLayout());

        this.linker = linker;

        setScrollMode(Style.Scroll.NONE);
        setHeading("Publish");
        setSize(800, 500);
        setResizable(false);

        setModal(true);

        comments = new TextArea();
        comments.setWidth(750);
        comments.setFieldLabel(Messages.getResource("org.jahia.jcr.publication.publicationComments"));

        VerticalPanel commentsPanel = new VerticalPanel();
        TableData d = new TableData(Style.HorizontalAlignment.CENTER, Style.VerticalAlignment.MIDDLE);
        d.setMargin(5);
        commentsPanel.setHorizontalAlign(Style.HorizontalAlignment.CENTER);
        commentsPanel.add(new Label(Messages.get("org.jahia.jcr.publication.publicationComments", "Comments")), d);
        commentsPanel.add(comments, d);
        commentsPanel.setHeight(70);
        commentsPanel.setWidth("100%");

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig("title",Messages.getResource("label.path"),450);
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
            @Override
            public Object render(GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                return model.get(property);
            }
        });
        configs.add(column);

        column = new ColumnConfig("status",Messages.getResource("org.jahia.jcr.publication.currentStatus"),150);
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
            @Override
            public Object render(GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                switch (model.getStatus()) {
                    case GWTJahiaPublicationInfo.NOT_PUBLISHED:
                        return Messages.getResource("org.jahia.jcr.publication.status_notyetpublished");
                    case GWTJahiaPublicationInfo.PUBLISHED:
                        return Messages.getResource("label.published");
                    case GWTJahiaPublicationInfo.MODIFIED:
                        return Messages.getResource("label.modified");
                    case GWTJahiaPublicationInfo.UNPUBLISHABLE:
                        return Messages.getResource("org.jahia.jcr.publication.status_notyetpublished");
                    case GWTJahiaPublicationInfo.UNPUBLISHED:
                        return "Unpublished";
                    case GWTJahiaPublicationInfo.CONFLICT:
                        return "Conflict - cannot publish";
                }
                return "";
            }
        });
        configs.add(column);

        column = new ColumnConfig("canPublish",Messages.getResource("org.jahia.jcr.publication.publicationAllowed"),150);
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
            @Override
            public Object render(GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                return model.isCanPublish().toString();
            }
        });
        configs.add(column);
        column = new ColumnConfig("mainTitle", Messages.get("label.parentObject", "Parent object"), 150);
        column.setHidden(true);
        configs.add(column);
        GroupingStore<GWTJahiaPublicationInfo> store = new GroupingStore<GWTJahiaPublicationInfo>();

        for (GWTJahiaPublicationInfo info : infos.values()) {
            if (info.getStatus() != GWTJahiaPublicationInfo.PUBLISHED) {
                info.set("mainTitle", info.getTitle());
                store.add(info);
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
                String l = data.models.size() == 1 ? Messages.get("label.item", "Item") : Messages.get("label.items", "Items");  
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

        cancel = new Button(Messages.getResource("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });
        ok = new Button(Messages.getResource("label.publish"));
        noWorkflow = new Button(Messages.get("label.bypassWorkflow", "Bypass workflow"));
        final ArrayList<String> uuids = new ArrayList<String>(infos.keySet());

        ok.addSelectionListener(new ButtonEventSelectionListener(uuids, true));
        noWorkflow.addSelectionListener(new ButtonEventSelectionListener(uuids, false));

        setButtonAlign(Style.HorizontalAlignment.CENTER);
        addButton(ok);
        addButton(noWorkflow);
        addButton(cancel);
    }

    private class ButtonEventSelectionListener extends SelectionListener<ButtonEvent> {
        private ArrayList<String> uuids;
        protected boolean workflow;

        public ButtonEventSelectionListener(ArrayList<String> uuids, boolean workflow) {
            this.uuids = uuids;
            this.workflow = workflow;
        }

        public void componentSelected(ButtonEvent event) {
            ok.setEnabled(false);
            noWorkflow.setEnabled(false);
            cancel.setEnabled(false);
            JahiaContentManagementService
                    .App.getInstance().publish(uuids, false, comments.getValue(), workflow, false, new BaseAsyncCallback() {
                public void onApplicationFailure(Throwable caught) {
                    Log.error("Cannot publish", caught);
                    com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                    hide();
                }

                public void onSuccess(Object result) {
                    Info.display(Messages.getResource("message.content.published"), Messages.getResource("message.content.published"));
                    linker.refresh(Linker.REFRESH_ALL);
                    hide();
                }
            });
        }
    }
}
