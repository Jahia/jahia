/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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

    public WorkflowBatchViewer(final Map<String, Map<String, Set<String>>> batch, final Map<String, String> titleForObjectKey, final Map<String, String> workflowStates, boolean viewOnly) {
        this(batch, titleForObjectKey, workflowStates, viewOnly, false, null) ;
    }

    public WorkflowBatchViewer(final Map<String, Map<String, Set<String>>> batch, final Map<String, String> titleForObjectKey, final Map<String, String> workflowStates, boolean viewOnly, boolean enableExpander, final Map<String, Map<String, GWTJahiaNodeOperationResult>> errorsAndWarnings) {
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
                actions.get(action).add(new GWTJahiaProcessJobAction(key, m.get(key),action, workflowStates));
            }
        }

        final ReportGrid grid = new ReportGrid(actions, titleForObjectKey, enableExpander, errorsAndWarnings, false);
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
