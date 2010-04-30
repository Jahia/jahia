package org.jahia.ajax.gwt.client.widget.workflow;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowNodeTypeConfig;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 28, 2010
 * Time: 4:32:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowManagerEngine extends Window {
    private Linker linker;
    private Button ok;
    protected ButtonBar buttonBar;

    public static final int BUTTON_HEIGHT = 24;

    private WorkflowContentTypePanel mainPanel;

    public WorkflowManagerEngine(Linker linker) {
        super();
        this.linker = linker;
        init();
    }

    /**
     * init
     */
    private void init() {
        setHeading(Messages.get("label_workflowPerNodeType","Workflow per content type set-up"));
        setLayout(new FitLayout());
        setSize(800, 600);
        JahiaContentManagementService.App.getInstance().getWorkflowNodeTypeConfig(new AsyncCallback<GWTJahiaWorkflowNodeTypeConfig>() {
            public void onSuccess(final GWTJahiaWorkflowNodeTypeConfig config) {
                mainPanel = new WorkflowContentTypePanel(linker, config.getWorkflowDefinitions(), config.getContentTypeList(),config.getWorflowNodeTypes());
                add(mainPanel);
                layout();

            }

            public void onFailure(Throwable throwable) {
                Log.error(throwable.getMessage(), throwable);
            }
        });

        setFooter();

    }

    /**
     * init footer
     */
    private void setFooter() {
        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);
        ok = new Button(Messages.getResource("fm_save"));
        ok.setHeight(BUTTON_HEIGHT);
        ok.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonOK());
        ok.addSelectionListener(new SaveSelectionListener());
        buttonBar.add(ok);

        Button cancel = new Button(Messages.getResource("fm_cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                WorkflowManagerEngine.this.hide();
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
            JahiaContentManagementService.App.getInstance().updateNodeTypeWorkflowRule(mainPanel.getWorflowNodeType(), mainPanel.getWorflowNodeTypeToRemove(), new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    com.google.gwt.user.client.Window.alert(Messages.get("saved_prop_failed", "Properties save failed\n\n") + throwable.getLocalizedMessage());
                    Log.error("failed", throwable);
                }

                public void onSuccess(Object o) {
                    WorkflowManagerEngine.this.hide();
                    Log.debug("finish updating nodeType Workflow Rule");
                }
            });
        }

    }
}
