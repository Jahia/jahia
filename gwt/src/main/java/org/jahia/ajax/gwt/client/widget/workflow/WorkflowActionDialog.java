/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Window;
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
    private transient GWTJahiaWorkflowDefinition wfDefinition;
    private transient CustomWorkflow customWorkflow;

    private Linker linker;
    private PropertiesEditor propertiesEditor;
    private ButtonBar buttonsBar;

    public WorkflowActionDialog(final String nodePath, final String title, final GWTJahiaWorkflowDefinition wfDefinition,
                                final Linker linker, CustomWorkflow custom, EngineContainer container) {
        this(linker, container, title, null, wfDefinition.getFormResourceName(), null);
        this.nodePath = nodePath;
        this.wfDefinition = wfDefinition;
        initStartWorkflowDialog(wfDefinition);
        if (custom != null) {
            custom.initStartWorkflowDialog(wfDefinition, this);
        }
    }

    public WorkflowActionDialog(final GWTJahiaWorkflow workflow, final GWTJahiaWorkflowTask task, final Linker linker,
                                CustomWorkflow custom, EngineContainer container) {
        this(linker, container, (workflow.getVariables().get("jcr_title") != null && workflow.getVariables().get("jcr_title").getValues().size() == 1) ? workflow.getVariables().get("jcr_title").getValues().get(0).getString() : null, null, (String) task.get("formResourceName"),workflow);
        initExecuteActionDialog(task);
        if (custom != null) {
            custom.initExecuteActionDialog(workflow, this);
        }
    }

    public WorkflowActionDialog(final String nodePath, final String title, final GWTJahiaWorkflowDefinition wfDefinition,
                                final Linker linker, CustomWorkflow custom, final EngineContainer container, String language) {
        this(linker, container, title, language, wfDefinition.getFormResourceName(), null);
        this.nodePath = nodePath;
        this.wfDefinition = wfDefinition;
        this.customWorkflow = custom;
        initStartWorkflowDialog(wfDefinition);
        if (custom != null) {
            custom.initStartWorkflowDialog(wfDefinition, this);
        }

    }

    private WorkflowActionDialog(Linker linker, EngineContainer container, String title, String language, String wfDefinitionNodeType, final GWTJahiaWorkflow workflow) {
        super();
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
        initTabs(wfDefinitionNodeType, language);

        container.setEngine(this, this.title, buttonsBar, JahiaGWTParameters.getLanguage(language), this.linker);
        addCancelButton(container);
    }

    private void addCancelButton(final EngineContainer container) {
        if (! (container instanceof EngineCards)) {
            Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    container.closeEngine();
                }
            });
            buttonsBar.add(cancel);
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
        Button button = generateStartWorkflowButton(workflowDefinition);
        buttonsBar.insert(button, buttonsBar.getItems().size() > 0 ? buttonsBar.getItems().size() - 1 : 0);
    }

    public void initExecuteActionDialog(final GWTJahiaWorkflowTask task) {
        List<Button> buttons = generateActionButtons(task);

        for (Button button : buttons) {
            buttonsBar.insert(button, buttonsBar.getItems().size() > 0 ? buttonsBar.getItems().size() - 1 : 0);
        }

    }

    private void initTabs(final String formResourceName, String language) {
        Map<String, GWTJahiaNodeProperty> variables = new HashMap<String, GWTJahiaNodeProperty>();
        if (title != null) {
            if (language != null) {
                variables.put("jcr:title", new GWTJahiaNodeProperty("jcr:title",
                        new GWTJahiaNodePropertyValue(language + " - " + title, GWTJahiaNodePropertyType.STRING)));
            } else {
                variables.put("jcr:title", new GWTJahiaNodeProperty("jcr:title",
                        new GWTJahiaNodePropertyValue(title, GWTJahiaNodePropertyType.STRING)));
            }
        }
        initTabs(formResourceName, variables);
    }

    private void initTabs(final String formResourceName, Map<String, GWTJahiaNodeProperty> variables) {
        TabItem action = initActionTab(formResourceName, variables);
        tabPanel.add(action);
        TabItem comments = initCommentTab();
        tabPanel.add(comments);
    }


    private TabItem initActionTab(String formResourceName, final Map<String, GWTJahiaNodeProperty> variables) {
        actionTab = new TabItem(Messages.get("label.action", "Action"));
        actionTab.setLayout(new BorderLayout());
        if (formResourceName != null && !"".equals(formResourceName)) {
            contentManagement.getWFFormForNodeAndNodeType(formResourceName, new BaseAsyncCallback<GWTJahiaNodeType>() {
                public void onSuccess(final GWTJahiaNodeType result) {
                    JahiaContentManagementService.App.getInstance().initializeCreateEngine(result.getName(),
                            linker.getSelectionContext().getMultipleSelection().size() > 1 ? linker.getSelectionContext().getMultipleSelection().get(0).getPath() : linker.getSelectionContext().getSingleSelection().getPath(), null,
                            new BaseAsyncCallback<GWTJahiaCreateEngineInitBean>() {
                                public void onSuccess(GWTJahiaCreateEngineInitBean result2) {
                                    propertiesEditor = new PropertiesEditor(Arrays.asList(result), variables,
                                            Arrays.asList(GWTJahiaItemDefinition.CONTENT));
                                    propertiesEditor.setChoiceListInitializersValues(result2.getChoiceListInitializersValues());
                                    propertiesEditor.setViewInheritedItems(true);
                                    propertiesEditor.renderNewFormPanel();
                                    propertiesEditor.setFrame(true);
                                    propertiesEditor.setBorders(false);
                                    propertiesEditor.setBodyBorder(false);
                                    actionTab.add(propertiesEditor, new BorderLayoutData(Style.LayoutRegion.CENTER));
                                    actionTab.layout();
                                }
                            }
                    );
                }
            });
        }
        return actionTab;
    }

    private TabItem initCommentTab() {
        commentsTab = new TabItem(Messages.get("label.comments", "Comments"));

        commentsTab.setLayout(new BorderLayout());

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
     * @param wf the workflow definition for which we need to generate a button
     * @return the newly generated button
     */
    public Button generateStartWorkflowButton(final GWTJahiaWorkflowDefinition wf) {
        final Button button = new Button(Messages.get("label.workflow.start", "Start Workflow") + ":" + wf.getDisplayName());
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
                            linker.refresh(data);
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
     * @return the CustomWorkflow object
     */
    public CustomWorkflow getCustomWorkflow() {
        return customWorkflow;
    }

    /**
     * return the nodePath used to create this dialog
     * @return the nodePath associated at creation with this dialog
     */
    public String getNodePath() {
        return nodePath;
    }

    /**
     * return the workflow definition used to create this dialog
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
}
