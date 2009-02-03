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

package org.jahia.ajax.gwt.engines.workflow.client.components;

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
import org.jahia.ajax.gwt.engines.workflow.client.model.GWTJahiaWorkflowBatch;
import org.jahia.ajax.gwt.engines.workflow.client.WorkflowService;
import org.jahia.ajax.gwt.engines.workflow.client.WorkflowManager;
import org.jahia.ajax.gwt.commons.client.ui.WindowUtil;
import org.jahia.ajax.gwt.tripanelbrowser.client.BrowserLinker;


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
        if (((WorkflowToolbar)linker.getTopObject()).getBatch().isEmpty()) {
            execute.setEnabled(false);
        }
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
                close();
                WindowUtil.close() ;
            }
        });
    }

}

