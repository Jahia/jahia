package org.jahia.ajax.gwt.client.widget;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.i18n.client.DateTimeFormat;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 22, 2010
 * Time: 2:54:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodeColumnConfigList extends ArrayList<ColumnConfig> {
    private static String[] STATE_IMAGES = new String[]{"000", "111", "121", "000", "000", "000"};
    private List<String> columnList;

    public NodeColumnConfigList() {
        init();
    }

    public NodeColumnConfigList(List<String> columnList) {
        this.columnList = columnList;
        init();
    }

    /**
     * Extension renderer
     */
    private transient static final GridCellRenderer<GWTJahiaNode> EXT_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
            return ContentModelIconProvider.getInstance().getIcon(modelData).getHTML();
        }
    };

    /**
     * Locked rendered
     */
    private transient static final GridCellRenderer<GWTJahiaNode> LOCKED_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                             ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
            if (modelData.isLocked().booleanValue()) {
                String lockOwner = modelData.getLockOwner();
                return lockOwner != null && lockOwner.equals(JahiaGWTParameters.SYSTEM_USER) ? "<img src='../images/icons/gwt/lock_information.png'>"
                        : ContentModelIconProvider.getInstance().getLockIcon().getHTML();
            } else {
                return "";
            }
        }
    };

    /**
     * Size Renderer
     */
    private transient static final GridCellRenderer<GWTJahiaNode> SIZE_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                             ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
            if (modelData.getSize() != null) {
                long size = modelData.getSize().longValue();
                return Formatter.getFormattedSize(size);
            } else {
                return "-";
            }
        }
    };

    /**
     * Date Renderer
     */
    private transient static final GridCellRenderer<GWTJahiaNode> DATE_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                             ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
            Date d = modelData.get(s);
            if (d != null) {
                return new DateTimePropertyEditor(DateTimeFormat.getFormat(CalendarField.DEFAULT_DATE_FORMAT)).getStringValue(d);
            } else {
                return "-";
            }
        }
    };

    /**
     * @return
     */
    private void init() {
        List<String> columnNames = columnList == null ? new ArrayList<String>() : new ArrayList<String>(columnList);
        if (columnNames.isEmpty()) {
            columnNames.add("providerKey");
            columnNames.add("ext");
            columnNames.add("name");
            columnNames.add("locked");
            columnNames.add("path");
            columnNames.add("size");
            columnNames.add("publicationInfo");
            columnNames.add("created");
            columnNames.add("createdBy");
            columnNames.add("lastModified");
            columnNames.add("lastModifiedBy");
            columnNames.add("lastPublished");
            columnNames.add("lastPublishedBy");
        }

        for (String columnName : columnNames) {
            ColumnConfig col = null;

            if ("providerKey".equals(columnName)) {
                col = new ColumnConfig("providerKey", Messages.getResource("fm_column_provider"), 100);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("ext".equals(columnName)) {
                col = new ColumnConfig("ext", Messages.getResource("fm_column_type"), 40);
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(EXT_RENDERER);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("locked".equals(columnName)) {
                col = new ColumnConfig("locked", Messages.getResource("fm_column_locked"), 60);
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(LOCKED_RENDERER);
                col.setSortable(false);
                col.setResizable(true);
            } else if ("name".equals(columnName)) {
                col = new ColumnConfig("displayName", Messages.getResource("fm_column_name"), 240);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("path".equals(columnName)) {
                col = new ColumnConfig("path", Messages.getResource("fm_column_path"), 270);
                col.setSortable(true);
                col.setResizable(true);
                col.setHidden(true);
            } else if ("size".equals(columnName)) {
                col = new ColumnConfig("size", Messages.getResource("fm_column_size"), 140);
                col.setResizable(true);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setRenderer(SIZE_RENDERER);
                col.setResizable(true);
                col.setSortable(true);
            } else if ("created".equals(columnName)) {
                col = new ColumnConfig("created", Messages.get("fm_column_created", "Created"), 100);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setRenderer(DATE_RENDERER);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("createdBy".equals(columnName)) {
                col = new ColumnConfig("createdBy", Messages.get("fm_column_created_by", "Created by"), 100);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("lastModified".equals(columnName)) {
                col = new ColumnConfig("lastModified", Messages.get("fm_column_modified", "Modified"), 100);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setRenderer(DATE_RENDERER);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("lastModifiedBy".equals(columnName)) {
                col = new ColumnConfig("lastModifiedBy", Messages.get("fm_column_modified_by", "Modified by"), 100);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("lastPublished".equals(columnName)) {
                col = new ColumnConfig("lastPublished", Messages.get("fm_column_published", "Published"), 100);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setRenderer(DATE_RENDERER);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("lastPublishedBy".equals(columnName)) {
                col = new ColumnConfig("lastPublishedBy", Messages.get("fm_column_published_by", "Published by"), 100);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("publicationInfo".equals(columnName)) {
                col = new ColumnConfig("publicationInfo", Messages.get("fm_column_publication_info", "State"), 30);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                    public String render(GWTJahiaNode node, String property, ColumnData config, int rowIndex,
                                         int colIndex, ListStore<GWTJahiaNode> store, Grid<GWTJahiaNode> grid) {
                        int state = node.getPublicationInfo() != null ? node.getPublicationInfo().getStatus() : 0;
                        String title = Messages.get("fm_column_publication_info_" + state, String.valueOf(state));
                        return "<img src=\"../../gwt/resources/images/workflow/" + STATE_IMAGES[state]
                                + ".png\" height=\"12\" width=\"12\" title=\"" + title + "\" alt=\"" + title + "\"/>";
                    }
                });
                col.setSortable(true);
                col.setResizable(true);
            } else if ("count".equals(columnName)) {
                col = new ColumnConfig("count", Messages.get("fm_column_count", "Count"), 60);
                col.setSortable(true);
                col.setResizable(true);
            }
            if (col != null) {
                add(col);
            }
        }

    }

}
