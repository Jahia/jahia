/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.workflow;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaCreateEngineInitBean;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.workflow.*;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineCards;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineContainer;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.toolbar.action.WorkInProgressActionItem;

import java.util.*;

/**
 * Represent the workflow action dialog widget.
 *
 * @author rincevent
 * @since JAHIA 6.5
 */
public class WorkflowActionDialog extends LayoutContainer {

    private JahiaContentManagementServiceAsync contentManagement;
    private WorkflowDashboardEngine workflowDashboard;
    private TabPanel tabPanel;
    private TabItem actionTab;
    private TabItem commentsTab;
    private EngineContainer container;
    private String title;
    private List<GWTJahiaWorkflowComment> comments;

    private String nodePath;
    private GWTJahiaWorkflow workflow;

    private int numberOfWorkflows;

    private transient GWTJahiaWorkflowDefinition wfDefinition;
    private transient CustomWorkflow customWorkflow;

    private Linker linker;
    private PropertiesEditor propertiesEditor;
    private ButtonBar buttonsBar;

    public WorkflowActionDialog(final String nodePath, final String title, final GWTJahiaWorkflowDefinition wfDefinition,
                                final Linker linker, CustomWorkflow custom, EngineContainer container, int numberOfWorkflows) {
        this(nodePath, linker, container, title, null, wfDefinition.getFormResourceName(), null, false, numberOfWorkflows);
        this.wfDefinition = wfDefinition;
        initStartWorkflowDialog(wfDefinition);
        if (custom != null) {
            custom.initStartWorkflowDialog(wfDefinition, this, numberOfWorkflows);
        }
    }

    public WorkflowActionDialog(String nodePath, final GWTJahiaWorkflow workflow, final GWTJahiaWorkflowTask task, final Linker linker,
                                CustomWorkflow custom, EngineContainer container) {
        this(nodePath, linker, container, (workflow.getVariables().get("jcr_title") != null && workflow.getVariables().get("jcr_title").getValues().size() == 1) ? workflow.getVariables().get("jcr_title").getValues().get(0).getString() : null, null, (String) task.get("formResourceName"),workflow, false);
        initExecuteActionDialog(task);
        if (custom != null) {
            custom.initExecuteActionDialog(workflow, this);
        }
    }

    public WorkflowActionDialog(final String nodePath, final String title, final GWTJahiaWorkflowDefinition wfDefinition,
                                final Linker linker, CustomWorkflow custom, final EngineContainer container, String language, int numberOfWorkflows) {
        this(nodePath, linker, container, title, language, wfDefinition != null ? wfDefinition.getFormResourceName() : null, null, wfDefinition == null, numberOfWorkflows);
        this.wfDefinition = wfDefinition;
        this.customWorkflow = custom;
        if (wfDefinition != null) {
            initStartWorkflowDialog(wfDefinition);
        }
        if (custom != null) {
            custom.initStartWorkflowDialog(wfDefinition, this, numberOfWorkflows);
        }

    }

    private WorkflowActionDialog(String nodePath, Linker linker, EngineContainer container, String title, String language, String wfDefinitionNodeType, final GWTJahiaWorkflow workflow, boolean skipWorkflow, int numberOfWorkflows){
        this(nodePath, linker, container, title, language, wfDefinitionNodeType, workflow, skipWorkflow);
        this.numberOfWorkflows = numberOfWorkflows;
    }
    private WorkflowActionDialog(String nodePath, Linker linker, EngineContainer container, String title, String language, String wfDefinitionNodeType, final GWTJahiaWorkflow workflow, boolean skipWorkflow) {
        super();
        addStyleName("workflow-action-dialog");
        this.nodePath = nodePath;
        this.workflow = workflow;
        contentManagement = JahiaContentManagementService.App.getInstance();
        this.linker = linker;
        this.title = title;

        tabPanel = new TabPanel();
        setLayout(new FitLayout());
        add(tabPanel);

        buttonsBar = new ButtonBar();
        buttonsBar.setAlignment(Style.HorizontalAlignment.CENTER);

        actionTab = new TabItem();

        this.container = container;
        if (!skipWorkflow) {
            initTabs(wfDefinitionNodeType, workflow != null ? workflow.getVariables() : new HashMap<String, GWTJahiaNodeProperty>(), language);
        }
        container.setEngine(this, this.title, buttonsBar, JahiaGWTParameters.getLanguage(language), this.linker);
        addCancelButton(container);
    }

    private void addCancelButton(final EngineContainer container) {
        if (!(container instanceof EngineCards)) {
            Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    container.closeEngine();
                }
            });
            cancel.addStyleName("button-cancel");
            buttonsBar.insert(cancel, 0);
        }
    }

    public EngineContainer getContainer() {
        return container;
    }

    public Linker getLinker() {
        return linker;
    }

    public PropertiesEditor getPropertiesEditor() {
        return propertiesEditor;
    }

    public void initStartWorkflowDialog(final GWTJahiaWorkflowDefinition workflowDefinition) {
        buttonsBar.add(generateStartWorkflowButton(workflowDefinition));
    }

    public void initExecuteActionDialog(final GWTJahiaWorkflowTask task) {
        for (Button button : generateActionButtons(task)) {
            buttonsBar.add(button);
        }

    }

    private void initTabs(final String formResourceName, Map<String, GWTJahiaNodeProperty> variables, String language) {
        if (title != null) {
            if (language != null) {
                variables.put("jcr:title", new GWTJahiaNodeProperty("jcr:title",
                        new GWTJahiaNodePropertyValue(language + " - " + title, GWTJahiaNodePropertyType.STRING)));
            } else {
                variables.put("jcr:title", new GWTJahiaNodeProperty("jcr:title",
                        new GWTJahiaNodePropertyValue(title, GWTJahiaNodePropertyType.STRING)));
            }
        }
        TabItem action = initActionTab(formResourceName, variables);
        tabPanel.add(action);
        TabItem comments = initCommentTab();
        tabPanel.add(comments);
    }

    private TabItem initActionTab(final String formResourceName, final Map<String, GWTJahiaNodeProperty> variables) {
        actionTab = new TabItem(Messages.get("label.action", "Action"));
        actionTab.setLayout(new BorderLayout());
        actionTab.setStyleName("workflow-dialog-action-tab");
        if (formResourceName != null && !"".equals(formResourceName)) {
            NodeTypeCreationCaller nodeTypeCreationCaller = new NodeTypeCreationCaller(formResourceName, nodePath);
            nodeTypeCreationCaller.add(new BaseAsyncCallback<NodeTypeCreationInfo>() {
                @Override
                public void onSuccess(NodeTypeCreationInfo result) {
                    propertiesEditor = new PropertiesEditor(Arrays.asList(result.getNodeType()), variables, Arrays.asList(GWTJahiaItemDefinition.CONTENT));
                    propertiesEditor.setChoiceListInitializersValues(result.getEngine().getChoiceListInitializersValues());
                    propertiesEditor.setViewInheritedItems(true);
                    propertiesEditor.renderNewFormPanel();
                    propertiesEditor.setFrame(true);
                    propertiesEditor.setBorders(false);
                    propertiesEditor.setBodyBorder(false);
                    actionTab.add(propertiesEditor, new BorderLayoutData(Style.LayoutRegion.CENTER));
                    actionTab.layout();
                }
            });
        }
        return actionTab;
    }

    private TabItem initCommentTab() {
        commentsTab = new TabItem(Messages.get("label.comments", "Comments"));

        commentsTab.setLayout(new BorderLayout());
        commentsTab.setStyleName("workflow-dialog-comments-tab");
        final LayoutContainer commentsContainer = new LayoutContainer(new RowLayout(Style.Orientation.VERTICAL));
        commentsTab.add(commentsContainer, new BorderLayoutData(Style.LayoutRegion.CENTER));

        commentsContainer.setScrollMode(Style.Scroll.AUTOY);
        commentsContainer.setBorders(false);

        if (workflow != null) {
            contentManagement.getWorkflowComments(workflow, new BaseAsyncCallback<List<GWTJahiaWorkflowComment>>() {
                public void onSuccess(List<GWTJahiaWorkflowComment> result) {
                    displayComments(result, commentsContainer);
                }
            });
        }

        // Display add a comment
        FormPanel formPanel = new FormPanel();
        formPanel.setHeaderVisible(false);
        formPanel.setBorders(false);
        formPanel.setBodyBorder(false);
        formPanel.setLayout(new FormLayout(FormPanel.LabelAlign.LEFT));
        final TextArea textArea = new TextArea();
        textArea.setFieldLabel(Messages.get("label.comment", "Comment"));
        textArea.setPreventScrollbars(false);
        textArea.setHeight(50);
        textArea.setWidth(750);
        textArea.setAllowBlank(false);
        FormData data = new FormData("-20");
        formPanel.add(textArea, data);
        Button button = new Button(Messages.get("label.addComment", "Add comment"));
        button.addStyleName("button-comment");
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                if (workflow != null) {
                    contentManagement.addCommentToWorkflow(workflow, textArea.getValue(),
                            new BaseAsyncCallback<List<GWTJahiaWorkflowComment>>() {
                                public void onSuccess(List<GWTJahiaWorkflowComment> result) {
                                    commentsContainer.removeAll();
                                    displayComments(result, commentsContainer);
                                    Info.display(Messages.get("label.commentAdded", "Comment Added"), Messages.get(
                                            "label.commentAdded", "Comment Added"));
                                }

                                public void onApplicationFailure(Throwable caught) {
                                    Info.display("Adding comment failed", "Adding comment failed");
                                }
                            }
                    );
                } else {
                    GWTJahiaWorkflowComment wfComment = new GWTJahiaWorkflowComment();
                    wfComment.setComment(textArea.getValue());
                    wfComment.setUser(JahiaGWTParameters.getCurrentUser());
                    wfComment.setTime(new Date());
                    if (comments == null) {
                        comments = new ArrayList<GWTJahiaWorkflowComment>();
                    }
                    comments.add(wfComment);
                    commentsContainer.removeAll();
                    displayComments(comments, commentsContainer);
                }
            }
        });
        formPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        formPanel.add(button, data);
        FormButtonBinding buttonBinding = new FormButtonBinding(formPanel);
        buttonBinding.addButton(button);
        commentsTab.add(formPanel, new BorderLayoutData(Style.LayoutRegion.SOUTH, 100));

        return commentsTab;
    }

    private void displayComments(List<GWTJahiaWorkflowComment> comments, final LayoutContainer commentsPanel) {
        int i = 0;
        for (GWTJahiaWorkflowComment comment : comments) {
            Text text = new Text(comment.getComment());
            text.setWidth(450);
            Text time = new Text(Messages.get("label.at", "at") + " " + DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(
                    comment.getTime()));
            Text user = new Text(Messages.get("label.by", "by") + " " + comment.getUser());
            HorizontalPanel commentPanel = new HorizontalPanel();
            commentPanel.setBorders(false);
            commentPanel.setWidth("100%");
            TableData data = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
            data.setPadding(5);
            commentPanel.add(text, data);
            commentPanel.setScrollMode(Style.Scroll.NONE);
            commentPanel.setStyleAttribute("background-color", i % 2 == 0 ? "#e9eff3" : "white");
            VerticalPanel verticalPanel = new VerticalPanel();
            verticalPanel.add(time, data);
            verticalPanel.add(user);
            verticalPanel.setWidth(250);
            verticalPanel.setBorders(false);
            commentPanel.add(verticalPanel);
            commentsPanel.add(commentPanel);
            i++;
        }
        commentsPanel.layout();
    }

    public void disableButtons() {
        for (Component component : buttonsBar.getItems()) {
            if (component instanceof Button) {
                ((Button) component).setEnabled(false);
            }
        }
    }

    public void enableButtons() {
        for (Component component : buttonsBar.getItems()) {
            if (component instanceof Button) {
                ((Button) component).setEnabled(true);
            }
        }
    }

    /**
     * Create a start button for a specific workflow definition
     *
     * @param wf the workflow definition for which we need to generate a button
     * @return the newly generated button
     */
    public Button generateStartWorkflowButton(final GWTJahiaWorkflowDefinition wf) {
        final Button button = new Button(Messages.get("label.workflow.start", "Start Workflow") + ":" + wf.getDisplayName());
        button.addStyleName("button-start");
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                disableButtons();
                List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<GWTJahiaNodeProperty>();
                if (propertiesEditor != null) {
                    for (PropertiesEditor.PropertyAdapterField adapterField : propertiesEditor.getFieldsMap().values()) {
                        Field<?> field = adapterField.getField();
                        if (field.isEnabled() && !field.isReadOnly() && !field.validate() && ((FieldSet) adapterField.getParent()).isExpanded()) {
                            final String status = Messages.get("label.workflow.form.error", "Your form is not valid");
                            Info.display(status, status);
                            enableButtons();
                            return;
                        }
                    }
                    nodeProperties = propertiesEditor.getProperties();
                }
                container.closeEngine();
                final String status = Messages.get("label.workflow.task", "Executing workflow task");
                Info.display(status, status);
                WorkInProgressActionItem.setStatus(status);
                contentManagement.startWorkflow(nodePath, wf, nodeProperties, getComments(), new BaseAsyncCallback<Object>() {
                    public void onSuccess(Object result) {
                        Info.display(status, Messages.get("message.workflow.task.success", "Workflow task executed successfully"));
                        WorkInProgressActionItem.removeStatus(status);
                    }

                    public void onApplicationFailure(Throwable caught) {
                        Info.display(status, Messages.get("message.workflow.task.failure", "Workflow task failed to execute"));
                        WorkInProgressActionItem.removeStatus(status);
                    }
                });
            }
        });
        return button;
    }

    private List<Button> generateActionButtons(final GWTJahiaWorkflowTask task) {
        List<GWTJahiaWorkflowOutcome> outcomes = task.getOutcomes();
        final List<Button> allButtons = new ArrayList<Button>();
        for (final GWTJahiaWorkflowOutcome outcome : outcomes) {
            Button button = new Button(outcome.getLabel());
            if (outcome.getIcon() != null) {
                button.setIcon(ToolbarIconProvider.getInstance().getIcon(outcome.getIcon()));
            }
            button.addStyleName("button-workflow-action");
            allButtons.add(button);
            button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent buttonEvent) {
                    disableButtons();
                    List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<GWTJahiaNodeProperty>();
                    if (propertiesEditor != null) {
                        for (PropertiesEditor.PropertyAdapterField adapterField : propertiesEditor.getFieldsMap().values()) {
                            Field<?> field = adapterField.getField();
                            if (field.isEnabled() && !field.isReadOnly() && !field.validate() && ((FieldSet) adapterField.getParent()).isExpanded()) {
                                final String status = Messages.get("label.workflow.form.error", "Your form is not valid");
                                Info.display(status, status);
                                enableButtons();
                                return;
                            }
                        }
                        nodeProperties = propertiesEditor.getProperties();
                    }
                    final String status = Messages.get("label.workflow.task", "Executing workflow task");
                    Info.display(status, status);
                    WorkInProgressActionItem.setStatus(status);
                    contentManagement.assignAndCompleteTask(task, outcome, nodeProperties, new BaseAsyncCallback<Object>() {
                        public void onSuccess(Object result) {
                            WorkInProgressActionItem.removeStatus(status);
                            Info.display(Messages.get("label.information", "Information"), Messages.get("label.workflow.finished", "Workflow executed"));
                            Map<String, Object> data = new HashMap<String, Object>();
                            data.put(Linker.REFRESH_MAIN, true);
                            data.put("event", "workflowExecuted");
                            if (linker != null) {
                                linker.refresh(data);
                            }
                            container.closeEngine();
                        }

                        public void onApplicationFailure(Throwable caught) {
                            WorkInProgressActionItem.removeStatus(status);
                            Window.alert(Messages.get("label.workflow.failed", "Workflow failed") + ": " + caught.getMessage());
                            container.closeEngine();
                        }
                    });
                }
            });
        }
        return allButtons;
    }

    /**
     * get the custom workflow associated with this workflow dialog
     *
     * @return the CustomWorkflow object
     */
    public CustomWorkflow getCustomWorkflow() {
        return customWorkflow;
    }

    /**
     * return the nodePath used to create this dialog
     *
     * @return the nodePath associated at creation with this dialog
     */
    public String getNodePath() {
        return nodePath;
    }

    /**
     * return the workflow definition used to create this dialog
     *
     * @return the workflow definition associated at creation with this dialog
     */
    public GWTJahiaWorkflowDefinition getWfDefinition() {
        return wfDefinition;
    }

    public void setWorkflowDashboard(WorkflowDashboardEngine workflowDashboard) {
        this.workflowDashboard = workflowDashboard;
    }

    /**
     * The list of comments in this workflow
     *
     * @return list of comments in this workflow
     */
    public List<String> getComments() {
        List<String> result = new ArrayList<String>();
        if (comments != null && comments.size() > 0) {
            for (GWTJahiaWorkflowComment c : comments) {
                result.add(c.getComment());
            }
        }

        return result;
    }

    public TabPanel getTabPanel() {
        return tabPanel;
    }

    public ButtonBar getButtonsBar() {
        return buttonsBar;
    }

    @Override
    protected void onHide() {
        super.onHide();
        if (workflowDashboard != null) {
            workflowDashboard.hide();
        }
    }

    class NodeTypeCreationCaller {
        private NodeTypeCreationInfo result;
        private List<AsyncCallback<NodeTypeCreationInfo>> deferredCallbacks = new ArrayList<AsyncCallback<NodeTypeCreationInfo>>();

        public NodeTypeCreationCaller(String nodeTypeName, final String path) {
            contentManagement.getWFFormForNodeAndNodeType(nodeTypeName, new BaseAsyncCallback<GWTJahiaNodeType>() {
                public void onSuccess(final GWTJahiaNodeType nodeType) {
                    JahiaContentManagementService.App.getInstance().initializeCreateEngine(nodeType.getName(), path, null,
                            new BaseAsyncCallback<GWTJahiaCreateEngineInitBean>() {
                                public void onSuccess(GWTJahiaCreateEngineInitBean engine) {
                                    NodeTypeCreationCaller.this.result = new NodeTypeCreationInfo(nodeType, engine);
                                    for (AsyncCallback<NodeTypeCreationInfo> callback : deferredCallbacks) {
                                        callback.onSuccess(result);
                                    }
                                    deferredCallbacks.clear();
                                }
                            }
                    );
                }
            });
        }

        public void add(final AsyncCallback<NodeTypeCreationInfo> async) {
            if (result != null) {
                // Use deferred command to display the actions once the engine size is known
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        async.onSuccess(result);
                    }
                });
            } else {
                deferredCallbacks.add(async);
            }
        }
    }

    class NodeTypeCreationInfo {
        private GWTJahiaNodeType nodeType;
        private GWTJahiaCreateEngineInitBean engine;

        public NodeTypeCreationInfo(GWTJahiaNodeType nodeType, GWTJahiaCreateEngineInitBean engine) {
            this.nodeType = nodeType;
            this.engine = engine;
        }

        public GWTJahiaCreateEngineInitBean getEngine() {
            return engine;
        }

        public void setEngine(GWTJahiaCreateEngineInitBean engine) {
            this.engine = engine;
        }

        public GWTJahiaNodeType getNodeType() {
            return nodeType;
        }

        public void setNodeType(GWTJahiaNodeType nodeType) {
            this.nodeType = nodeType;
        }
    }


}
