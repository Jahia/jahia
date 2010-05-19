package org.jahia.ajax.gwt.client.widget.workflow;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowNodeTypeConfig;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 28, 2010
 * Time: 4:32:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowDashboardEngine extends Window {
    private Linker linker;
    private Button ok;
    protected ButtonBar buttonBar;

    public static final int BUTTON_HEIGHT = 24;

    private WorkflowContentTypePanel mainPanel;

    private WorkflowInstancesPanel instancesPanel;

    public WorkflowDashboardEngine(Linker linker) {
        super();
        this.linker = linker;
        init();
    }

    /**
     * init
     */
    private void init() {
        setHeading(Messages.get("label.workflowPerNodeType", "Workflow per content type set-up"));
        setLayout(new FitLayout());
        setSize(800, 600);
        ToolBar toolBar = new ButtonBar();
        Button button = new Button(Messages.get("label.workflowPerNodeType", "Workflow per content type set-up"));
        button.setHeight(BUTTON_HEIGHT);
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                if(mainPanel==null || !mainPanel.isVisible()) {
                JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
                async.getWorkflowNodeTypeConfig(new BaseAsyncCallback<GWTJahiaWorkflowNodeTypeConfig>() {
                    public void onSuccess(final GWTJahiaWorkflowNodeTypeConfig config) {
                        instancesPanel.hide();;
                        mainPanel = new WorkflowContentTypePanel(linker, config.getWorkflowDefinitions(),
                                                                 config.getContentTypeList(),
                                                                 config.getWorflowNodeTypes());
                        add(mainPanel);
                        layout();

                    }
                });
                } else {
                    mainPanel.hide();
                    instancesPanel.show();
                }
            }
        });
        toolBar.add(button);
        //
        instancesPanel = new WorkflowInstancesPanel(linker);
        add(instancesPanel);
        setTopComponent(toolBar);
        setFooter();

    }

    /**
     * init footer
     */
    private void setFooter() {
        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);
        ok = new Button(Messages.getResource("label.save"));
        ok.setHeight(BUTTON_HEIGHT);
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        ok.addSelectionListener(new SaveSelectionListener());
        buttonBar.add(ok);

        Button cancel = new Button(Messages.getResource("label.cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                WorkflowDashboardEngine.this.hide();
            }
        });
        buttonBar.add(cancel);
        setBottomComponent(buttonBar);

        setFooter(true);
    }

    /**
     * Save listener
     */
    private class SaveSelectionListener extends SelectionListener<ButtonEvent> {
        public void componentSelected(ButtonEvent event) {
            if (mainPanel != null) {
                JahiaContentManagementService.App.getInstance().updateNodeTypeWorkflowRule(
                        mainPanel.getWorflowNodeType(), mainPanel.getWorflowNodeTypeToRemove(), new BaseAsyncCallback() {
                            public void onApplicationFailure(Throwable throwable) {
                                com.google.gwt.user.client.Window.alert(Messages.get("saved_prop_failed",
                                                                                     "Properties save failed\n\n") + throwable.getLocalizedMessage());
                                Log.error("failed", throwable);
                            }

                            public void onSuccess(Object o) {
                                WorkflowDashboardEngine.this.hide();
                                Log.debug("finish updating nodeType Workflow Rule");
                            }
                        });
            }
        }

    }
}
