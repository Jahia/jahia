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
package org.jahia.ajax.gwt.client.widget.edit.workflow.dialog;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowAction;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowOutcome;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowTaskComment;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 28 avr. 2010
 */
public class WorkflowActionDialog extends Window {
    private JahiaContentManagementServiceAsync async;
    private JahiaContentDefinitionServiceAsync definitionsAsync;

    public WorkflowActionDialog(final GWTJahiaNode node, final GWTJahiaWorkflowAction action) {
        async = JahiaContentManagementService.App.getInstance();
        definitionsAsync = JahiaContentDefinitionService.App.getInstance();
        setModal(true);
        setHeading("Workflow action [" + action.getName() + "] for node: " + node.getDisplayName());
        setWidth(800);
        setHeight(600);
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);
        final ContentPanel contentPanel = new ContentPanel(new VBoxLayout());
        contentPanel.setHeading("Comments");
        contentPanel.setBorders(false);
        final Window dialog = this;

        createCommentsPanel(action, contentPanel, dialog);
        BorderLayoutData data;

        final ContentPanel panel = new ContentPanel(new VBoxLayout());
        panel.setHeading("Actions");
        String formResourceName = action.getFormResourceName();
        if (formResourceName!=null && !"".equals(formResourceName)) {
            definitionsAsync.getNodeType(formResourceName, new AsyncCallback<GWTJahiaNodeType>() {
                public void onFailure(Throwable caught) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                public void onSuccess(GWTJahiaNodeType result) {
                    final PropertiesEditor propertiesEditor = new PropertiesEditor(Arrays.asList(result),
                                                                                   action.getVariables(), false, false,
                                                                                   GWTJahiaItemDefinition.CONTENT, null,
                                                                                   null);
                    panel.add(propertiesEditor);
                    generateActionButtons(propertiesEditor, action, node, dialog, panel);
                    dialog.layout();
                }
            });
        } else {
            generateActionButtons(null, action, node, dialog, panel);
        }

        data = new BorderLayoutData(Style.LayoutRegion.CENTER, 150, 100, 400);
        data.setMargins(new Margins(5, 0, 0, 0));
        data.setSplit(true);
        data.setCollapsible(true);
        data.setFloatable(true);
        add(panel, data);
    }

    private void createCommentsPanel(final GWTJahiaWorkflowAction action, ContentPanel contentPanel,
                                     final Window dialog) {
        final VerticalPanel commentsPanel = new VerticalPanel();
        commentsPanel.setHeight(300);
        commentsPanel.setScrollMode(Style.Scroll.AUTOY);
        commentsPanel.setSpacing(5);
        commentsPanel.setBorders(false);
        async.getTaskComments(action, new AsyncCallback<List<GWTJahiaWorkflowTaskComment>>() {
            public void onFailure(Throwable caught) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void onSuccess(List<GWTJahiaWorkflowTaskComment> result) {
                int i = 0;
                for (GWTJahiaWorkflowTaskComment comment : result) {
                    Text text = new Text(comment.getComment());
                    text.setWidth(450);
                    Text time = new Text("at " + DateTimeFormat.getMediumDateTimeFormat().format(comment.getTime()));
                    Text user = new Text("by " + comment.getUser());
                    HorizontalPanel commentPanel = new HorizontalPanel();
                    commentPanel.setBorders(false);
                    commentPanel.setWidth("100%");
                    commentPanel.setHeight(50);
                    commentPanel.add(text);
                    commentPanel.setScrollMode(Style.Scroll.NONE);
                    commentPanel.setStyleAttribute("background-color", i % 2 == 0 ? "#e9eff3" : "white");
                    VerticalPanel verticalPanel = new VerticalPanel();
                    verticalPanel.setHorizontalAlign(Style.HorizontalAlignment.LEFT);
                    verticalPanel.add(time);
                    verticalPanel.add(user);
                    verticalPanel.setWidth(250);
                    verticalPanel.setHeight(50);
                    verticalPanel.setBorders(false);
                    commentPanel.add(verticalPanel);
                    commentsPanel.add(commentPanel);
                    dialog.layout();
                    i++;
                }
            }
        });

        contentPanel.add(commentsPanel);
        // Display add a comment
        FormPanel formPanel = new FormPanel();
        formPanel.setHeaderVisible(false);
        formPanel.setWidth("100%");
        formPanel.setBorders(false);
        final TextArea textArea = new TextArea();
        textArea.setFieldLabel("Comment");
        textArea.setPreventScrollbars(false);
        textArea.setHeight(50);
        textArea.setWidth(750);
        formPanel.add(textArea, new FormData("100%"));
        Button button = new Button("Add comment");
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                async.addCommentToTask(action, textArea.getValue(), new AsyncCallback() {
                    public void onSuccess(Object result) {
                        dialog.hide();
                        Info.display("Comment Added", "Comment Added");
                    }

                    public void onFailure(Throwable caught) {
                        dialog.hide();
                        Info.display("Adding comment failed", "Adding comment failed");
                    }
                });
            }
        });
        formPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        formPanel.add(button);
        FormButtonBinding buttonBinding = new FormButtonBinding(formPanel);
        buttonBinding.addButton(button);
        VBoxLayoutData vBoxLayoutData = new VBoxLayoutData(new Margins(5, 0, 0, 0));
        contentPanel.add(formPanel, vBoxLayoutData);
        contentPanel.setScrollMode(Style.Scroll.NONE);
        contentPanel.setWidth("100%");
        BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.NORTH, 450, 200, 500);
        data.setMargins(new Margins(5, 0, 0, 0));
        data.setSplit(true);
        data.setCollapsible(true);
        data.setFloatable(true);
        add(contentPanel, data);
    }

    public WorkflowActionDialog(final GWTJahiaNode node, final GWTJahiaWorkflowDefinition wf) {
        async = JahiaContentManagementService.App.getInstance();
        definitionsAsync = JahiaContentDefinitionService.App.getInstance();
        setModal(true);
        setHeading("Start workflow [" + wf.getName() + "] for node: " + node.getDisplayName());
        setWidth(800);
        setHeight(300);
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);
        BorderLayoutData data;
        final Window dialog = this;
        final ContentPanel panel = new ContentPanel(new VBoxLayout());
        panel.setHeading("Actions");
        String formResourceName = wf.getFormResourceName();
        if (formResourceName!=null && !"".equals(formResourceName)) {
            definitionsAsync.getNodeType(formResourceName, new AsyncCallback<GWTJahiaNodeType>() {
                public void onFailure(Throwable caught) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                public void onSuccess(GWTJahiaNodeType result) {
                    final PropertiesEditor propertiesEditor = new PropertiesEditor(Arrays.asList(result),
                                                                                   null, false, false,
                                                                                   GWTJahiaItemDefinition.CONTENT, null,
                                                                                   null);
                    panel.add(propertiesEditor);
                    generateStartWorkflowButton(propertiesEditor, wf, node, dialog, panel);
                    dialog.layout();
                }
            });
        } else {
            generateStartWorkflowButton(null, wf, node, dialog, panel);
        }

        data = new BorderLayoutData(Style.LayoutRegion.CENTER, 150, 100, 400);
        data.setMargins(new Margins(5, 0, 0, 0));
        data.setSplit(true);
        data.setCollapsible(true);
        data.setFloatable(true);
        add(panel, data);
    }

    private void generateActionButtons(final PropertiesEditor propertiesEditor, final GWTJahiaWorkflowAction action,
                                       final GWTJahiaNode node, final Window dialog, ContentPanel panel) {
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        List<GWTJahiaWorkflowOutcome> outcomes = action.getOutcomes();
        for (final GWTJahiaWorkflowOutcome outcome : outcomes) {
            Button button = new Button(outcome.getLabel());
            button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent buttonEvent) {
                    List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<GWTJahiaNodeProperty>();
                    if (propertiesEditor != null) {
                        nodeProperties = propertiesEditor.getProperties();
                    }
                    async.assignAndCompleteTask(node.getPath(), action, outcome, nodeProperties, new AsyncCallback() {
                        public void onSuccess(Object result) {
                            dialog.hide();
                            Info.display("Workflow executed", "Workflow executed");
                        }

                        public void onFailure(Throwable caught) {
                            dialog.hide();
                            Info.display("Workflow failed", "Workflow failed");
                        }
                    });
                }
            });
            horizontalPanel.add(button);
        }
        panel.add(horizontalPanel, new VBoxLayoutData(new Margins(5, 0, 0, 0)));
    }

    private void generateStartWorkflowButton(final PropertiesEditor propertiesEditor,
                                             final GWTJahiaWorkflowDefinition wf, final GWTJahiaNode node,
                                             final Window dialog, ContentPanel panel) {
        HorizontalPanel horizontalPanel = new HorizontalPanel();

        Button button = new Button("Start Workflow : " + wf.getName());
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<GWTJahiaNodeProperty>();
                if (propertiesEditor != null) {
                    nodeProperties = propertiesEditor.getProperties();
                }
                async.startWorkflow(node.getPath(), wf, nodeProperties, new AsyncCallback() {
                    public void onSuccess(Object result) {
                        dialog.hide();
                        Info.display("Workflow executed", "Workflow executed");
                    }

                    public void onFailure(Throwable caught) {
                        dialog.hide();
                        Info.display("Workflow failed", "Workflow failed");
                    }
                });
            }
        });
        horizontalPanel.add(button);
        panel.add(horizontalPanel, new VBoxLayoutData(new Margins(5, 0, 0, 0)));
    }
}
