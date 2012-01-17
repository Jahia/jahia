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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
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
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.content.ImagePopup;
import org.jahia.ajax.gwt.client.widget.content.compare.CompareEngine;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 6 sept. 2010
 */
public class VersioningTabItem extends EditEngineTabItem {
    private transient String locale;


    public VersioningTabItem() {
        setHandleCreate(false);
    }

    /**
     * Create the tab item
     */
    @Override
    public void init(final NodeHolder engine, final AsyncTabItem tab, String locale) {
        if (!tab.isProcessed()) {
            this.locale = locale;
            JahiaContentManagementService.App.getInstance().getVersions(engine.getNode().getPath(), new BaseAsyncCallback<List<GWTJahiaNodeVersion>>() {
                public void onSuccess(List<GWTJahiaNodeVersion> result) {
                    final ListStore<GWTJahiaNodeVersion> all = new ListStore<GWTJahiaNodeVersion>();
                    all.add(result);
                    List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

                    ColumnConfig column = new ColumnConfig();
                    column.setId("label");
                    column.setSortable(false);
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
                                    Date date = DateTimeFormat.getFormat("yyyy_MM_dd_HH_mm_ss").parse(
                                            strings[1]);
                                    value = value + s1 + " " + DateTimeFormat.getMediumDateTimeFormat()
                                            .format(date);
                                } else {
                                    value = version.getLabel();
                                }
                            }
                            return value;
                        }
                    });
                    configs.add(column);
                    if (PermissionsUtils.isPermitted("jcr:write", engine.getNode()) && !engine.getNode().isLocked()) {
                        column = new ColumnConfig();
                        column.setSortable(false);
                        column.setHeader("Action");
                        column.setWidth(200);
                        column.setRenderer(new GridCellRenderer() {
                            public Object render(ModelData model, String property, ColumnData config, int rowIndex,
                                                 int colIndex, ListStore listStore, Grid grid) {
                                ButtonBar buttonBar = new ButtonBar();
                                Button button = new Button(Messages.get("label.compare.with.staging.engine", "Compare With Staging"));
                                final GWTJahiaNodeVersion version = (GWTJahiaNodeVersion) model;
                                button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                                    @Override
                                    public void componentSelected(ButtonEvent ce) {
                                        // add 30s to the date to be sure to display the right version
                                        new CompareEngine(version.getNode().getUUID(), VersioningTabItem.this.locale, false, version.getNode().getPath(), new Date(version.getDate().getTime() + (30l * 1000l)), engine, version.getWorkspace(), version.getLabel()).show();
                                    }
                                });
                                buttonBar.add(button);
                                return buttonBar;
                            }
                        });
                        configs.add(column);
                    }
                    Grid<GWTJahiaNodeVersion> grid = new Grid<GWTJahiaNodeVersion>(all, new ColumnModel(configs));
                    grid.setAutoExpandColumn("label");
                    tab.add(grid);
                    tab.layout();
                    tab.show();
                }
            });
            tab.setProcessed(true);
        }
    }

    @Override
    public void onLanguageChange(String language) {
        this.locale = language;
        super.onLanguageChange(language);
    }
}
