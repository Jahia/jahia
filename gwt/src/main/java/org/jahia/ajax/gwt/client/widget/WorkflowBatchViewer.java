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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.store.*;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.Style;

import java.util.*;

import org.jahia.ajax.gwt.client.data.GWTJahiaNodeOperationResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProcessJobAction;

/**
 * User: rfelden
 * Date: 10 oct. 2008 - 09:34:57
 */
public abstract class WorkflowBatchViewer extends Window {

    public WorkflowBatchViewer(final Map<String, Map<String, Set<String>>> batch, final Map<String, String> titleForObjectKey, boolean viewOnly) {
        this(batch, titleForObjectKey, viewOnly, false, null) ;
    }

    public WorkflowBatchViewer(final Map<String, Map<String, Set<String>>> batch, final Map<String, String> titleForObjectKey, boolean viewOnly, boolean enableExpander, final Map<String, Map<String, GWTJahiaNodeOperationResult>> errorsAndWarnings) {
        super() ;
        this.setLayout(new BorderLayout());
        this.setHeading("Workflow Batch");
        this.setModal(true);
        this.setResizable(true);
        this.setSize(600, 450);

        Map<String, List<GWTJahiaProcessJobAction>> actions = new HashMap<String, List<GWTJahiaProcessJobAction>>();
        for (String action : batch.keySet()) {
            if (!actions.containsKey(action)) {
                actions.put(action, new ArrayList<GWTJahiaProcessJobAction>());
            }
            Map<String, Set<String>> m = batch.get(action);
            for (String key : m.keySet()) {
                actions.get(action).add(new GWTJahiaProcessJobAction(key, m.get(key),action));
            }
        }

        final ReportGrid grid = new ReportGrid(actions, titleForObjectKey, enableExpander, errorsAndWarnings);
        final ListStore<ReportGrid.GWTReportElement> store = grid.getStore();

        ButtonBar buttons = new ButtonBar() ;
        final Button execute = new Button("Execute") ;
        if (!viewOnly) {
            execute.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    execute();
                }
            }) ;
        }
        if (store.getModels().isEmpty()) {
            execute.setEnabled(false);
        }
        Button close = new Button("Close", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                close() ;
            }
        }) ;
        execute.setIconStyle("wf-button_ok");
        close.setIconStyle("wf-button_cancel");
        if (!viewOnly) {
            buttons.add(execute) ;
        }
        buttons.add(close) ;

        buildContextMenu(grid) ;

        this.add(grid, new BorderLayoutData(Style.LayoutRegion.CENTER)) ;

        this.setButtonBar(buttons);

        if (!viewOnly) {
            store.addStoreListener(new StoreListener<ReportGrid.GWTReportElement>() {
                public void storeRemove(StoreEvent<ReportGrid.GWTReportElement> event) {
                    execute.setEnabled(grid.getStore().getCount() > 0);
                }
            });
        }
    }

    public abstract void buildContextMenu(Grid<ReportGrid.GWTReportElement> grid) ;

    public abstract void execute() ;

}
