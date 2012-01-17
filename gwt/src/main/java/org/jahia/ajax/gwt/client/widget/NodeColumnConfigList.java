/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTColumn;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: ktlili
 * Date: Apr 22, 2010
 * Time: 2:54:17 PM
 */
public class NodeColumnConfigList extends ArrayList<ColumnConfig> {
    private List<GWTColumn> columnList;
    private String autoExpand;

    public static final GridCellRenderer<GWTJahiaNode> ICON_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                             ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
            return ContentModelIconProvider.getInstance().getIcon(modelData).getHTML();
        }
    };

    public static final GridCellRenderer<GWTJahiaNode> LOCKED_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                             ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
            if (modelData.getLockInfos().containsKey(null) && (modelData.getLockInfos().size() == 1 || modelData.getLockInfos().containsKey(JahiaGWTParameters.getLanguage()))) {
                return StandardIconsProvider.STANDARD_ICONS.lock().getHTML();
            } else if (modelData.getLockInfos().size() > 1) {
                return StandardIconsProvider.STANDARD_ICONS.lockLanguage().getHTML();
            } else {
                return "";
            }
        }
    };

    public static final GridCellRenderer<GWTJahiaNode> SIZE_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
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

    public static final GridCellRenderer<GWTJahiaNode> DATE_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                             ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
            Date d = modelData.get(s);
            if (d != null) {
                return new DateTimePropertyEditor(DateTimeFormat.getFormat(
                        CalendarField.DEFAULT_DATE_FORMAT)).getStringValue(d);
            } else {
                return "-";
            }
        }
    };

    public static final GridCellRenderer<GWTJahiaNode> PUBLICATION_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public Object render(GWTJahiaNode node, String property, ColumnData config, int rowIndex, int colIndex,
                             ListStore<GWTJahiaNode> store, Grid<GWTJahiaNode> grid) {
            final GWTJahiaPublicationInfo info = node.getAggregatedPublicationInfo();
            HorizontalPanel p = new HorizontalPanel();
            if (info != null) {
                Image res = GWTJahiaPublicationInfo.renderPublicationStatusImage(info.getStatus());
                p.add(res);
                return p;
            }
            return "";
        }
    };

    public static final GridCellRenderer<GWTJahiaNode> VERSION_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public Object render(final GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1,
                             ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
            List<GWTJahiaNodeVersion> versions = gwtJahiaNode.getVersions();
            if (versions != null) {
                SimpleComboBox<String> combo = new SimpleComboBox<String>();
                combo.setForceSelection(true);
                combo.setTriggerAction(ComboBox.TriggerAction.ALL);
                for (GWTJahiaNodeVersion version : versions) {
                    String value = Messages.get("label.version", "Version") + " ";
                    if (version.getLabel() != null && !"".equals(version.getLabel())) {
                        String[] strings = version.getLabel().split("_at_");
                        if (strings.length == 2) {
                            String s1;
                            if (strings[0].contains("published")) {
                                s1 = Messages.get("label.version.published", "published at");
                            } else {
                                s1 = Messages.get("label.version.uploaded", "uploaded at");
                            }
                            value = value + s1 + " " + DateTimeFormat.getMediumDateTimeFormat().format(
                                    DateTimeFormat.getFormat("yyyy_MM_dd_HH_mm_ss").parse(strings[1]));
                        }
                    }
                    combo.add(value + " (" + version.getVersionNumber() + ")");
                }
                final String s2 = "Always Latest Version";
                combo.add(s2);
                combo.setSimpleValue(s2);
                combo.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
                    @Override
                    public void selectionChanged(
                            SelectionChangedEvent<SimpleComboValue<String>> simpleComboValueSelectionChangedEvent) {
                        SimpleComboValue<String> value = simpleComboValueSelectionChangedEvent.getSelectedItem();
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

    public static final GridCellRenderer<GWTJahiaNode> NAME_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public Object render(GWTJahiaNode node, String property, ColumnData config, int rowIndex, int colIndex,
                             ListStore<GWTJahiaNode> store, Grid<GWTJahiaNode> grid) {
            Object v = node.get(property);
            if (node.getNodeTypes().contains("jmix:markedForDeletion")) {
                v = "<span class=\"markedForDeletion\">" + v + "</span>";
            }
            return v;
        }
    };

    public static final TreeGridCellRenderer<GWTJahiaNode> NAME_TREEGRID_RENDERER = new TreeGridCellRenderer<GWTJahiaNode>() {
        @Override
        protected String getText(TreeGrid<GWTJahiaNode> gwtJahiaNodeTreeGrid, GWTJahiaNode node, String property, int rowIndex, int colIndex) {
            String v = super.getText(gwtJahiaNodeTreeGrid, node, property, rowIndex, colIndex);
            if (node.getNodeTypes().contains("jmix:markedForDeletion")) {
                v = "<span class=\"markedForDeletion\">" + v + "</span>";
            }
            if (!PermissionsUtils.isPermitted("editModeAccess", node) && !PermissionsUtils.isPermitted("jcr:write_default", node)) {
                v = "<span class=\"noEditModeAccess\">" + v + "</span>";
            }
            return v;
        }
    };


    public NodeColumnConfigList(List<GWTColumn> columnList) {
        this(columnList, false);
    }

    public NodeColumnConfigList(List<GWTColumn> columnList, boolean init) {
        this.columnList = columnList;
        if (init) {
            init();
        }
    }

    public String getAutoExpand() {
        return autoExpand;
    }

    /**
     * @return
     */
    public void init() {
        List<GWTColumn> columns = columnList == null ? new ArrayList<GWTColumn>() : new ArrayList<GWTColumn>(
                columnList);

        for (GWTColumn column : columns) {
            int i = column.getSize();
            if (i == -1) {
                autoExpand = column.getKey();
                i = 250;
            }
            ColumnConfig col = new ColumnConfig(column.getKey(), column.getTitle(), i);
            col.setResizable(column.isResizable());
            col.setSortable(column.isSortable());
            col.setMenuDisabled(true);
            if ("icon".equals(column.getKey())) {
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setSortable(false);
                col.setRenderer(ICON_RENDERER);
            } else if ("locked".equals(column.getKey())) {
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(LOCKED_RENDERER);
            } else if ("path".equals(column.getKey())) {
                col.setHidden(true);
            } else if ("pathVisible".equals(column.getKey())) {
                col.setId("path");
            } else if ("size".equals(column.getKey())) {
                col.setRenderer(SIZE_RENDERER);
            } else if ("publicationInfo".equals(column.getKey())) {
                col.setRenderer(PUBLICATION_RENDERER);
            } else if ("version".equals(column.getKey())) {
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(VERSION_RENDERER);
            } else if ("jcr:created".equals(column.getKey())) {
                col.setRenderer(DATE_RENDERER);
            } else if ("jcr:lastModified".equals(column.getKey())) {
                col.setRenderer(DATE_RENDERER);
            } else if ("index".equals(column.getKey())) {
                col.setHeader("");
                col.setResizable(false);
                col.setFixed(true);
                col.setId("numberer");
                col.setDataIndex("index");
            } else if ("name".equals(column.getKey()) || "displayName".equals(column.getKey())) {
                col.setRenderer(NAME_RENDERER);
            }
            add(col);
        }

    }

}
