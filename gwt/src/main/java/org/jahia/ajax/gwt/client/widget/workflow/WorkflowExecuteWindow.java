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
package org.jahia.ajax.gwt.client.widget.workflow;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowBatch;
import org.jahia.ajax.gwt.client.service.workflow.WorkflowService;
import org.jahia.ajax.gwt.client.util.WindowUtil;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowManager;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;


/**
 * Confirmation window for adding a job to batch or executing batch / single action.
 * Optional comment can be entered.
 *
 * User: toto
 * Date: Sep 25, 2008 - 7:43:40 PM
 */
public class WorkflowExecuteWindow extends Window {

    private BrowserLinker linker ;
    private Button execute ;
    private TextArea comments ;
    private TextField title ;

    public WorkflowExecuteWindow(final BrowserLinker linker) {
        super();
        this.linker = linker ;
        setLayout(new FormLayout());
        setHeading(WorkflowManager.getResource("wf_executeBatch"));
        setResizable(false);
        setModal(true);

        ButtonBar buttons = new ButtonBar() ;
        Button cancel = new Button(WorkflowManager.getResource("wf_cancel"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                hide() ;
            }
        }) ;
        execute = new Button(WorkflowManager.getResource("wf_execute"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                execute() ;
            }
        }) ;
        buttons.add(execute) ;
        buttons.add(cancel) ;
        execute.setIconStyle("wf-button_ok");
        cancel.setIconStyle("wf-button_cancel");
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setButtonBar(buttons);

        title = new TextField() ;
        title.setFieldLabel(WorkflowManager.getResource("wf_title"));
        comments = new TextArea();
        comments.setFieldLabel(WorkflowManager.getResource("wf_comment"));
        add(title) ;
        add(comments);
    }

    public void showWindow() {
        if (!((WorkflowTable)linker.getTopRightObject()).getChecked().isEmpty()) {
            ((WorkflowToolbar)linker.getTopObject()).addToBatch();
        }
        // this cleans empty entries in the batch map
        ((WorkflowToolbar)linker.getTopObject()).cleanBatch();
        execute.setEnabled(!((WorkflowToolbar)linker.getTopObject()).getBatch().isEmpty());
        show();
        setSize(350, 160);
    }

    private void execute() {
        GWTJahiaWorkflowBatch batch = new GWTJahiaWorkflowBatch(((WorkflowToolbar)linker.getTopObject()).getBatch(), title.getRawValue(), comments.getValue()) ;
        WorkflowService.App.getInstance().execute(batch, new AsyncCallback() {
            public void onFailure(Throwable throwable) {
                Log.error(throwable.toString());
                com.google.gwt.user.client.Window.alert("Could not execute batch\n\n" + throwable.getLocalizedMessage()) ;
            }

            public void onSuccess(Object o) {
                ((WorkflowToolbar)linker.getTopObject()).clearBatch() ;
                close();
                WindowUtil.close() ;
            }
        });
    }

}

