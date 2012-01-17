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

package org.jahia.ajax.gwt.client.widget.trash;


import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Trash board - display all deleted nodes in a grid, allows to undelete and preview information about each of them
 */
public class TrashboardEngine extends LayoutContainer {
    
    private static final List<String> FIELDS;
    static {
        FIELDS = new ArrayList<String>(GWTJahiaNode.DEFAULT_FIELDS);
        FIELDS.add("j:deletionUser");
        FIELDS.add("j:deletionDate");
        FIELDS.add("j:deletionMessage");
        FIELDS.add(GWTJahiaNode.PUBLICATION_INFO);
    }

    private final Linker linker;
    private EngineContainer container;
    private Grid<GWTJahiaNode> grid;
    private ButtonBar bar;

    public TrashboardEngine(Linker linker, EngineContainer container) {
        super(new FitLayout());
        this.linker = linker;
        this.container = container;
        init();

        container.setEngine(this, Messages.get("label.trashboard", "Trash Board"), bar, this.linker);
    }

    private void init() {
        setLayout(new FitLayout());
        

        // data proxy
        RpcProxy<List<GWTJahiaNode>> proxy = new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<List<GWTJahiaNode>> callback) {
                JahiaContentManagementService.App.getInstance().searchSQL("select * from [jmix:markedForDeletionRoot]",-1, null, null, null,
                        FIELDS, true, callback);
            }
        };

        // tree loader
        final ListLoader<ListLoadResult<GWTJahiaNode>> loader = new BaseListLoader<ListLoadResult<GWTJahiaNode>>(proxy);
        final ListStore<GWTJahiaNode> deletedNodes = new ListStore<GWTJahiaNode>(loader);

        ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        ColumnConfig  column = new ColumnConfig("icon", "", 40);
        column.setResizable(false);
        column.setSortable(false);
        column.setMenuDisabled(true);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        column.setRenderer(NodeColumnConfigList.ICON_RENDERER);
        columns.add(column);

        column = new ColumnConfig("displayName", Messages.get("label.name", "Name"), 100);
        columns.add(column);
        column = new ColumnConfig("j:deletionDate", Messages.get("label.deletionDate", "Deletion date"), 150);
        columns.add(column);
        column = new ColumnConfig("j:deletionUser", Messages.get("label.deletionUser", "User"), 150);
        columns.add(column);

        final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
        column = new ColumnConfig("undelete", Messages.get("label.undelete", "Undelete"), 100);
        column.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public Object render(final GWTJahiaNode gwtJahiaNode, String property, ColumnData columnData, int rowIndex, int colIndex,
                                 ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> grid) {
                Button button = new Button(Messages.get("label.undelete", "Undelete"), new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                       MessageBox.confirm(
                               Messages.get("label.information", "Information"),
                               Messages.getWithArgs(
                                                "message.undelete.confirm",
                                                "Do you really want to undelete the selected resource {0}?",
                                                new String[] { ((GWTJahiaNode)gwtJahiaNode).getDisplayName() }),
                               new Listener<MessageBoxEvent>() {
                                   public void handleEvent(MessageBoxEvent be) {
                                       if (be.getButtonClicked().getText().equalsIgnoreCase(Dialog.YES)) {
                                           service.undeletePaths(Arrays.asList(gwtJahiaNode.getPath()), new BaseAsyncCallback() {
                                               @Override
                                               public void onApplicationFailure(Throwable throwable) {
                                                   Log.error(throwable.getMessage(), throwable);
                                                   MessageBox.alert(Messages.get("label.error", "Error"), throwable.getMessage(), null);
                                               }

                                               public void onSuccess(Object result) {
                                                   deletedNodes.remove(gwtJahiaNode);
                                               }
                                           });
                                       }
                                   }
                               });
                    }
                });
                button.setIcon(StandardIconsProvider.STANDARD_ICONS.restore());
                return button;
            }
        });
        columns.add(column);

        column = new ColumnConfig("Delete", Messages.get("label.delete", "Delete"), 80);
        column.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public Object render(final GWTJahiaNode gwtJahiaNode, String property, ColumnData columnData, int rowIndex, int colIndex,
                                 ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> grid) {
                if (gwtJahiaNode.getAggregatedPublicationInfo().getStatus() != GWTJahiaPublicationInfo.NOT_PUBLISHED) {
                    return null;
                }
                Button button = new Button(Messages.get("label.delete", "Delete"), new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                       MessageBox.confirm(
                               Messages.get("label.warning", "Warning"),
                               Messages.getWithArgs(
                                       "message.remove.single.confirm",
                                       "Do you really want to remove the selected resource {0}?",
                                       new String[]{gwtJahiaNode.getDisplayName()})
                                       +
                               Messages.get("message.remove.warning",
                                       "<br/><span style=\"font-style:bold;color:red;\">" +
                               "Warning: this will erase the content definitively" +
                                               " from the repository.</span>"),
                               new Listener<MessageBoxEvent>() {
                                   public void handleEvent(MessageBoxEvent be) {
                                       if (be.getButtonClicked().getText().equalsIgnoreCase(Dialog.YES)) {
                                           service.deletePaths(Arrays.asList(gwtJahiaNode.getPath()), new BaseAsyncCallback() {
                                               @Override
                                               public void onApplicationFailure(Throwable throwable) {
                                                   Log.error(throwable.getMessage(), throwable);
                                                   MessageBox.alert(Messages.get("label.error", "Error"), throwable.getMessage(), null);
                                               }

                                               public void onSuccess(Object result) {
                                                   deletedNodes.remove(gwtJahiaNode);
                                               }
                                           });
                                       }
                                   }
                               });
                    }
                });
                button.setIcon(StandardIconsProvider.STANDARD_ICONS.delete());
                return button;
            }
        });
        columns.add(column);

        column = new ColumnConfig("infos", "", 100);
        column.setRenderer(new GridCellRenderer() {
            public Object render(final ModelData modelData, String property, ColumnData columnData, int rowIndex, int colIndex,
                                 ListStore listStore, Grid grid) {
                GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) modelData;
                Button button = new Button(Messages.get("label.information", "Information"), new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        PreviewWindow w = new PreviewWindow(linker, (GWTJahiaNode) modelData);
                        w.show();
                    }
                });
                button.setIcon(StandardIconsProvider.STANDARD_ICONS.information());
                return button;
            }
        });
        columns.add(column);

        ColumnModel cm = new ColumnModel(columns);

        grid = new Grid<GWTJahiaNode>(deletedNodes, cm);
        grid.setAutoExpandColumn("displayName");
        grid.setAutoExpandMax(1000);
        add(grid);

        bar = new ButtonBar();
        bar.setAlignment(Style.HorizontalAlignment.CENTER);

        Button cancel = new Button(Messages.get("label.close", "Close"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                container.closeEngine();
            }
        });
        bar.add(cancel);
        
        loader.load();
    }
}
