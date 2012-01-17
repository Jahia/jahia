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

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.NodeUsagesGrid;

import java.util.Arrays;

/**
 * 
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:44:45 PM
 * 
 */
public class UsagesTabItem extends EditEngineTabItem {

    public UsagesTabItem() {
        setHandleCreate(false);
    }

    @Override
    public void init(final NodeHolder engine, AsyncTabItem tab, String locale) {
        tab.setLayout(new RowLayout());

        if (engine.getNode() != null) {
            Grid<GWTJahiaNodeUsage> grid = NodeUsagesGrid.createUsageGrid(Arrays.asList(engine.getNode()));
            Button button = new Button(Messages.get("label.usages.clean"), new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
                    service.cleanReferences(engine.getNode().getPath(), new BaseAsyncCallback() {
                        /**
                         * Called when an asynchronous call completes successfully.
                         *
                         * @param result the return value of the remote produced call
                         */
                        public void onSuccess(Object result) {
                            engine.getLinker().refresh(Linker.REFRESH_ALL);
                        }
                    });
                }
            });
            RowData layoutData1 = new RowData(200, 30);
            layoutData1.setMargins(new Margins(3));
            tab.add(button, layoutData1);
            tab.add(grid, new RowData(1, 1));
            tab.setProcessed(true);
        }
        tab.layout();
    }
}