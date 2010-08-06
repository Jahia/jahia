package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.workflow.dialog.WorkflowActionDialog;

import java.util.List;

/**
 * Window, displaying the current publication status.
 * User: toto
 * Date: Jan 28, 2010
 * Time: 2:44:46 PM
 */
class PublicationStatusWindow extends Window {
    protected Linker linker;
    protected Button ok;
    protected Button noWorkflow;
    protected Button cancel;
    protected boolean allSubTree;

    PublicationStatusWindow(final Linker linker, final List<String> uuids, final List<GWTJahiaPublicationInfo> infos,
                            boolean allSubTree) {
        setLayout(new FitLayout());

        this.linker = linker;
        this.allSubTree = allSubTree;
        setScrollMode(Style.Scroll.NONE);
        setHeading("Publish");
        setSize(800, 500);
        setResizable(false);

        setModal(true);

        TableData d = new TableData(Style.HorizontalAlignment.CENTER, Style.VerticalAlignment.MIDDLE);
        d.setMargin(5);

        GroupingStore<GWTJahiaPublicationInfo> store = new GroupingStore<GWTJahiaPublicationInfo>();
        store.add(infos);

        final Grid<GWTJahiaPublicationInfo> grid = new PublicationStatusGrid(store);
        add(grid);

        cancel = new Button(Messages.getResource("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });

        setButtonAlign(Style.HorizontalAlignment.CENTER);


        ok = new Button(Messages.getResource("label.publish"));
        GWTJahiaNode selectedNode = linker.getSelectedNode();
        if (selectedNode == null) {
            selectedNode = linker.getMainNode();
        }
        if (selectedNode.getWorkflowInfo().getPossibleWorkflows().get(0) != null) {
            ok.addSelectionListener(new ButtonEventSelectionListener(uuids, selectedNode,
                    selectedNode.getWorkflowInfo().getPossibleWorkflows().get(0),infos));
            addButton(ok);
        }
        if (PermissionsUtils.isPermitted("edit-mode/publication", JahiaGWTParameters.getSiteKey())) {
            noWorkflow = new Button(Messages.get("label.bypassWorkflow", "Bypass workflow"));
            noWorkflow.addSelectionListener(new ButtonEventSelectionListener(uuids, null, null, null));
            addButton(noWorkflow);
        }

        addButton(cancel);
    }

    private class ButtonEventSelectionListener extends SelectionListener<ButtonEvent> {
        private List<String> uuids;
        private final GWTJahiaNode selectedNode;
        private final GWTJahiaWorkflowDefinition gwtJahiaWorkflowDefinition;
        private final List<GWTJahiaPublicationInfo> infos;
        protected boolean workflow;

        public ButtonEventSelectionListener(List<String> uuids, GWTJahiaNode selectedNode,
                                            GWTJahiaWorkflowDefinition gwtJahiaWorkflowDefinition,
                                            List<GWTJahiaPublicationInfo> infos) {
            this.uuids = uuids;
            this.selectedNode = selectedNode;
            this.gwtJahiaWorkflowDefinition = gwtJahiaWorkflowDefinition;
            this.infos = infos;
        }

        public void componentSelected(ButtonEvent event) {
            ok.setEnabled(false);
            if (noWorkflow != null) {
                noWorkflow.setEnabled(false);
            }
            cancel.setEnabled(false);
            if (gwtJahiaWorkflowDefinition == null) {
                JahiaContentManagementService.App.getInstance()
                        .publish(uuids, allSubTree, false, false, null,null,
                                new BaseAsyncCallback() {
                                    public void onApplicationFailure(Throwable caught) {
                                        Log.error("Cannot publish", caught);
                                        com.google.gwt.user.client.Window
                                                .alert("Cannot publish " + caught.getMessage());
                                        hide();
                                    }

                                    public void onSuccess(Object result) {
                                        Info.display(Messages.getResource("message.content.published"),
                                                Messages.getResource("message.content.published"));
                                        linker.refresh(Linker.REFRESH_ALL);
                                        hide();
                                    }
                                });
            } else {
                hide();
                new WorkflowActionDialog(selectedNode, gwtJahiaWorkflowDefinition, uuids, allSubTree, linker,selectedNode.getLanguageCode(),infos).show();
            }
        }
    }
}
