package org.jahia.ajax.gwt.client.widget.publication;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.workflow.CustomWorkflow;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowActionDialog;
import org.jahia.ajax.gwt.client.widget.toolbar.action.WorkInProgressActionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 10, 2010
 * Time: 3:32:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationWorkflow implements CustomWorkflow {
    private List<GWTJahiaPublicationInfo> publicationInfos;
    private List<String> uuids;
    private boolean allSubTree;
    private String language;

    private static final long serialVersionUID = -4916142720074054130L;


    public PublicationWorkflow() {
    }

    public PublicationWorkflow(List<GWTJahiaPublicationInfo> publicationInfos, List<String> uuids, boolean allSubTree,
                               String language) {
        this.publicationInfos = publicationInfos;
        this.uuids = uuids;
        this.allSubTree = allSubTree;
        this.language = language;
    }

    public List<TabItem> getAdditionalTabs() {
        List tabs = new ArrayList<TabItem>();

        TabItem tab = new TabItem("Publication infos");
        tab.setLayout(new FitLayout());
        tabs.add(tab);

        PublicationStatusGrid g = new PublicationStatusGrid(publicationInfos);
        tab.add(g);

        return tabs;
    }

    public Button getStartWorkflowButton(final GWTJahiaWorkflowDefinition wf, final WorkflowActionDialog dialog) {
        final Button button = new Button(Messages.get("label.workflow.start", "Start Workflow") +": "+ wf.getDisplayName());
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                button.setEnabled(false);
                List<GWTJahiaNodeProperty> nodeProperties = new ArrayList<GWTJahiaNodeProperty>();
                if (dialog.getPropertiesEditor() != null) {
                    nodeProperties = dialog.getPropertiesEditor().getProperties();
                }
                dialog.hide();
                Info.display("Starting publication workflow", "Starting publication workflow");
                final String status = Messages.get("label.workflow.task", "Executing workflow task");
                WorkInProgressActionItem.setStatus(status);
                JahiaContentManagementService.App.getInstance()
                        .publish(uuids, allSubTree, true, false, nodeProperties, dialog.getComments(), language, new BaseAsyncCallback() {
                            public void onApplicationFailure(Throwable caught) {
                                WorkInProgressActionItem.removeStatus(status);
                                Log.error("Cannot publish", caught);
                                com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                            }

                            public void onSuccess(Object result) {
                                Info.display("Publication workflow started", "Publication workflow started");
                                WorkInProgressActionItem.removeStatus(status);
                                dialog.getLinker().refresh(Linker.REFRESH_ALL);
                            }
                        });
            }
        });
        return button;
    }

    public Button getBypassWorkflowButton(final GWTJahiaWorkflowDefinition wf, final WorkflowActionDialog dialog) {
        final Button button = new Button(Messages.get("label.bypassWorkflow", "Bypass workflow"));

        if (PermissionsUtils.isPermitted("edit-mode/jahia-administration", JahiaGWTParameters.getSiteKey())) {
            button.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    dialog.hide();
                    final String status = Messages.get("label.publication.task", "Publishing content");
                    Info.display(status, status);
                    WorkInProgressActionItem.setStatus(status);
                    JahiaContentManagementService.App.getInstance()
                            .publish(uuids, allSubTree, false, false, null, null, null, new BaseAsyncCallback() {
                                public void onApplicationFailure(Throwable caught) {
                                    WorkInProgressActionItem.removeStatus(status);
                                    Info.display("Cannot publish", "Cannot publish");
                                    Log.error("Cannot publish", caught);
                                }

                                public void onSuccess(Object result) {
                                    WorkInProgressActionItem.removeStatus(status);
                                    Info.display(Messages.get("message.content.published"),
                                            Messages.get("message.content.published"));
                                    dialog.getLinker().refresh(Linker.REFRESH_ALL);
                                }
                            });
                }
            });
            return button;
        } else {
            return null;
        }

    }

}
