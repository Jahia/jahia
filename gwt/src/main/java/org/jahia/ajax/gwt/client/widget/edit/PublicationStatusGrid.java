/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.workflow.PublicationManagerEngine;

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
                final String label = PublicationManagerEngine.statusToLabel.get(model.getStatus());
                return Messages.get("label.publication." + label, label);
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
