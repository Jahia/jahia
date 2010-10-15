/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.content.ImagePopup;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 6 sept. 2010
 */
public class VersioningTabItem extends EditEngineTabItem {

    @Override public AsyncTabItem create(GWTEngineTab engineTab, NodeHolder engine) {
        setHandleCreate(false);
        return super.create(engineTab,engine);
    }

    /**
     * Create the tab item
     */
    @Override
    public void init(final String locale) {
        JahiaContentManagementService.App.getInstance().getVersions(engine.getNode().getPath(), new BaseAsyncCallback<List<GWTJahiaNodeVersion>>() {
            public void onSuccess(List<GWTJahiaNodeVersion> result) {
                final ListStore<GWTJahiaNodeVersion> all = new ListStore<GWTJahiaNodeVersion>();
                all.add(result);
                List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

                ColumnConfig column = new ColumnConfig();
                column.setId("label");
                column.setSortable(true);
                column.setHeader("Name");
                column.setWidth(100);
                column.setRenderer(new GridCellRenderer() {
                    public Object render(ModelData model, String property, ColumnData config, int rowIndex,
                                         int colIndex, ListStore listStore, Grid grid) {
                        GWTJahiaNodeVersion version = (GWTJahiaNodeVersion) model;
                        String value = Messages.get("label.version", "Version") + " ";
                        if (version.getLabel() != null && !"".equals(version.getLabel())) {
                            String[] strings = version.getLabel().split("_at_");
                            if (strings.length == 2) {
                                String s1;
                                if (strings[0].contains("published")) {
                                    s1 = Messages.get("label.version.published", "published at");
                                } else if (strings[0].contains("uploaded")) {
                                    s1 = Messages.get("label.version.uploaded", "uploaded at");
                                } else {
                                    s1 = Messages.get("label.version." + strings[0], strings[0]);
                                }
                                value = value + s1 + " " + DateTimeFormat.getMediumDateTimeFormat()
                                        .format(DateTimeFormat.getFormat("yyyy_MM_dd_HH_mm_ss").parse(strings[1]));
                            } else {
                                value = version.getLabel();
                            }
                        }
                        return value;
                    }
                });
                configs.add(column);
                column = new ColumnConfig();
                column.setId("versionNumber");
                column.setSortable(true);
                column.setHeader("Version");
                column.setWidth(50);
                configs.add(column);
                if (engine.getNode().isWriteable() && !engine.getNode().isLocked()) {
                    column = new ColumnConfig();
                    column.setSortable(false);
                    column.setHeader("Action");
                    column.setWidth(100);
                    column.setRenderer(new GridCellRenderer() {
                        public Object render(ModelData model, String property, ColumnData config, int rowIndex,
                                             int colIndex, ListStore listStore, Grid grid) {
                            Button button = new Button(Messages.get("label.restore", "Restore"));
                            final GWTJahiaNodeVersion version = (GWTJahiaNodeVersion) model;
                            button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                                @Override
                                public void componentSelected(ButtonEvent ce) {
                                    tab.mask(Messages.get("label.restoring","Restoring")+"...", "x-mask-loading");
                                    JahiaContentManagementService.App.getInstance().restoreNode(version, false, new BaseAsyncCallback() {
                                        public void onSuccess(Object result) {
                                            tab.removeAll();
                                            init(locale);
                                            engine.getLinker().refresh(EditLinker.REFRESH_MAIN + EditLinker.REFRESH_PAGES);
                                            engine.close();
                                        }
                                    });
                                }
                            });
                            return button;
                        }
                    });
                    configs.add(column);
                }
                Grid<GWTJahiaNodeVersion> grid = new Grid<GWTJahiaNodeVersion>(all, new ColumnModel(configs));
                grid.setAutoExpandColumn("label");
                grid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
                    public void handleEvent(GridEvent ge) {
                        List<GWTJahiaNodeVersion> sel = ge.getGrid().getSelectionModel().getSelectedItems();
                        if (sel != null && sel.size() == 1) {
                            final GWTJahiaNodeVersion version = sel.get(0);
                            GWTJahiaNode el = version.getNode();
                            if (el.isFile()) {
                                if (el.isDisplayable()) {
                                    ImagePopup.popImage(el);
                                } else {
                                    if (el.getUrl() != null) {
                                        HTML link = new HTML(
                                                Messages.get("downloadMessage.label") + "<br /><br /><a href=\"" +
                                                        el.getUrl() + "\" target=\"_new\">" + el.getName() + "</a>");
                                        final com.extjs.gxt.ui.client.widget.Window dl =
                                                new com.extjs.gxt.ui.client.widget.Window();
                                        dl.setModal(true);
                                        dl.setHeading(Messages.get("label.download"));
                                        dl.setLayout(new FlowLayout());
                                        dl.setScrollMode(Style.Scroll.AUTO);
                                        dl.add(link);
                                        dl.setHeight(120);
                                        dl.show();
                                    } else {
                                        Window.alert(Messages.get("failure.download.label"));
                                    }
                                }
                            } else {
                                JahiaContentManagementService.App.getInstance().getNodeURL(null, el.getPath(), version.getDate(), version.getLabel(), "default", locale, new BaseAsyncCallback<String>() {
                                            public void onSuccess(String result) {
                                                final com.extjs.gxt.ui.client.widget.Window dl =
                                                        new com.extjs.gxt.ui.client.widget.Window();
                                                dl.setModal(true);
                                                dl.setHeading(Messages.get("label.preview", "Preview") + " " +
                                                        Messages.get("label.version", "version") + " " +
                                                        version.getVersionNumber());
                                                dl.setLayout(new FlowLayout());
                                                dl.setScrollMode(Style.Scroll.AUTO);
                                                dl.setUrl(result);
                                                dl.setHeight(600);
                                                dl.setWidth(800);
                                                dl.show();
                                            }
                                        });
                            }
                        }
                    }
                });
                tab.add(grid);
                tab.layout();
                tab.show();
            }
        });
    }
}
