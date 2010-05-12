package org.jahia.ajax.gwt.client.widget;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.i18n.client.DateTimeFormat;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.data.toolbar.GWTColumn;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
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
    private List<GWTColumn> columnList;

    private static String[] STATE_IMAGES = new String[]{"000", "111", "121", "000", "000", "000"};

    private transient final GridCellRenderer<GWTJahiaNode> ICON_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                             ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
            return ContentModelIconProvider.getInstance().getIcon(modelData).getHTML();
        }
    };
    private transient final GridCellRenderer<GWTJahiaNode> LOCKED_RENDERER =
            new GridCellRenderer<GWTJahiaNode>() {
                public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                                     ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
                    if (modelData.isLocked().booleanValue()) {
                        String lockOwner = modelData.getLockOwner();
                        return lockOwner != null && lockOwner.equals(JahiaGWTParameters.SYSTEM_USER) ?
                                "<img src='../images/icons/gwt/lock_information.png'>" :
                                StandardIconsProvider.STANDARD_ICONS.lock().getHTML();
                    } else {
                        return "";
                    }
                }
            };
    private transient final GridCellRenderer<GWTJahiaNode> SIZE_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
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
    private transient final GridCellRenderer<GWTJahiaNode> DATE_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                             ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
            Date d = modelData.get(s);
            if (d != null) {
                return new DateTimePropertyEditor(DateTimeFormat.getFormat(CalendarField.DEFAULT_DATE_FORMAT))
                        .getStringValue(d);
            } else {
                return "-";
            }
        }
    };
    private transient final GridCellRenderer<GWTJahiaNode> PUBLICATION_RENDERER =
            new GridCellRenderer<GWTJahiaNode>() {
                public String render(GWTJahiaNode node, String property, ColumnData config, int rowIndex, int colIndex,
                                     ListStore<GWTJahiaNode> store, Grid<GWTJahiaNode> grid) {
                    int state = node.getPublicationInfo() != null ? node.getPublicationInfo().getStatus() : 0;
                    String title = Messages.get("fm_column_publication_info_" + state, String.valueOf(state));
                    return "<img src=\"../../gwt/resources/images/workflow/" + STATE_IMAGES[state] +
                            ".png\" height=\"12\" width=\"12\" title=\"" + title + "\" alt=\"" + title + "\"/>";
                }
            };
    private transient final GridCellRenderer<GWTJahiaNode> VERSION_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public Object render(final GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i,
                             int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore,
                             Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
            List<GWTJahiaNodeVersion> versions = gwtJahiaNode.getVersions();
            if (versions != null) {
                SimpleComboBox<String> combo = new SimpleComboBox<String>();
                combo.setForceSelection(true);
                combo.setTriggerAction(ComboBox.TriggerAction.ALL);
                for (GWTJahiaNodeVersion version : versions) {
                    combo.add(version.getVersionNumber() + " (" +
                            DateTimeFormat.getFormat("d/MM/y hh:mm").format(version.getDate()) + ")");
                }
                final String s2 = "Always Latest Version";
                combo.add(s2);
                combo.setSimpleValue(s2);
                combo.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
                    @Override
                    public void selectionChanged(
                            SelectionChangedEvent<SimpleComboValue<String>> simpleComboValueSelectionChangedEvent) {
                        SimpleComboValue<String> value =
                                simpleComboValueSelectionChangedEvent.getSelectedItem();
                        String value1 = value.getValue();
                        if (!s2.equals(value1)) {
                            gwtJahiaNode.setSelectedVersion(value1.split("\\(")[0].trim());
                        }
                    }
                });
                combo.setDeferHeight(true);
                return combo;
            } else {
                SimpleComboBox<String> combo = new SimpleComboBox<String>();
                combo.setForceSelection(false);
                combo.setTriggerAction(ComboBox.TriggerAction.ALL);
                combo.add("No version");
                combo.setSimpleValue("No version");
                combo.setEnabled(false);
                combo.setDeferHeight(true);
                return combo;
            }
        }
    };

    public NodeColumnConfigList(List<GWTColumn> columnList) {
        this(columnList,false);
    }

    public NodeColumnConfigList(List<GWTColumn> columnList, boolean init) {
        this.columnList = columnList;
        if(init){
            init();
        }
    }


    /**
     * @return
     */
    public void init() {
        List<GWTColumn> columns = columnList == null ? new ArrayList<GWTColumn>() : new ArrayList<GWTColumn>(columnList);

        for (GWTColumn column : columns) {
            ColumnConfig col = new ColumnConfig(column.getKey(), column.getTitle(), column.getSize());
            col.setResizable(column.isResizable());
            col.setSortable(column.isSortable());

            if ("icon".equals(column.getKey())) {
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(ICON_RENDERER);
            } else if ("locked".equals(column.getKey())) {
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(LOCKED_RENDERER);
            } else if ("path".equals(column.getKey())) {
                col.setHidden(true);
            } else if ("size".equals(column.getKey())) {
                col.setRenderer(SIZE_RENDERER);
            } else if ("publicationInfo".equals(column.getKey())) {
                col.setRenderer(PUBLICATION_RENDERER);
            } else if ("version".equals(column.getKey())) {
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(VERSION_RENDERER);
            }
            add(col);
        }

    }

}
