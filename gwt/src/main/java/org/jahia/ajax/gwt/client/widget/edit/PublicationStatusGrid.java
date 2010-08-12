package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 4, 2010
 * Time: 6:30:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationStatusGrid extends Grid<GWTJahiaPublicationInfo> {

    public PublicationStatusGrid(GroupingStore<GWTJahiaPublicationInfo> store) {
        super();
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig("title", Messages.get("label.path"), 450);
        configs.add(column);
        column = new ColumnConfig("nodetype", Messages.get("label.nodetype"), 150);
        configs.add(column);

        column = new ColumnConfig("status", Messages.get("org.jahia.jcr.publication.currentStatus"), 150);
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaPublicationInfo>() {
            @Override
            public Object render(GWTJahiaPublicationInfo model, String property, ColumnData config, int rowIndex,
                                 int colIndex, ListStore listStore, Grid grid) {
                switch (model.getStatus()) {
                    case GWTJahiaPublicationInfo.NOT_PUBLISHED:
                        return Messages.get("org.jahia.jcr.publication.status_notyetpublished");
                    case GWTJahiaPublicationInfo.LOCKED:
                        return Messages.get("label.locked");
                    case GWTJahiaPublicationInfo.PUBLISHED:
                        return Messages.get("label.published");
                    case GWTJahiaPublicationInfo.MODIFIED:
                        return Messages.get("label.modified");
                    case GWTJahiaPublicationInfo.UNPUBLISHED:
                        return "Unpublished";
                    case GWTJahiaPublicationInfo.CONFLICT:
                        return "Conflict - cannot publish";
                }
                return "";
            }
        });
        configs.add(column);

        column = new ColumnConfig("mainTitle", Messages.get("label.parentObject", "Parent object"), 150);
        column.setHidden(true);
        configs.add(column);

        store.groupBy("mainTitle");
        final ColumnModel cm = new ColumnModel(configs);

        setStripeRows(true);
        setBorders(true);

        GroupingView view = new GroupingView();
        view.setShowGroupedColumn(false);
        view.setForceFit(true);
        view.setGroupRenderer(new GridGroupRenderer() {
            public String render(GroupColumnData data) {
                final ColumnConfig config = cm.getColumnById(data.field);
                String f = config.getHeader();
                String l = data.models.size() == 1 ? Messages.get("label.item", "Item") :
                        Messages.get("label.items", "Items");
                String v = config.getRenderer() != null ?
                        config.getRenderer().render(data.models.get(0), null, null, 0, 0, null, null).toString() :
                        data.group;
                return v + " (" + data.models.size() + " " + l + ")";
            }
        });
        setView(view);
        setSelectionModel(new GridSelectionModel<GWTJahiaPublicationInfo>());
        reconfigure(store, cm);
    }
}
