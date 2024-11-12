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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.content.util.ContentHelper;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeleteItemWindow extends Window {

    private final int windowHeight = 500;
    private final int windowWidth = 650;

    final Grid<GWTJahiaNodeUsage> usagesGrid;
    final FormPanel formPanel;

    public DeleteItemWindow(final Linker linker, List<GWTJahiaNode> nodesToBeDeleted, final boolean permanentlyDelete, final boolean skipRefreshOnDelete, String baseUsageUrl) {

        addStyleName("delete-item-window");
        setSize(windowWidth, windowHeight);
        setHeadingHtml("Informations");
        setResizable(false);
        setModal(true);
        /* Information message */
        formPanel = new FormPanel();
        formPanel.setHeadingHtml(Messages.get("label.information"));
        formPanel.setHeaderVisible(false);
        formPanel.setBorders(false);
        formPanel.setId("JahiaGxtDeleteItem");
        formPanel.setLayout(new RowLayout(Orientation.VERTICAL));
        formPanel.setSize(windowWidth, windowHeight);

        final Html textMessage = new Html();
        formPanel.add(textMessage);

        /* Usages grid */
        final List<GWTJahiaNode> selectedNodeList = new ArrayList<GWTJahiaNode>();
        final List<String> selectedPathList = new ArrayList<String>();
        for (GWTJahiaNode node : nodesToBeDeleted) {
            selectedNodeList.add(node);
            selectedPathList.add(node.getPath());
        }

        // use this content panel to get vertical scrollbar only on the grid
        final ContentPanel cp = new ContentPanel();
        cp.setLayout(new FitLayout());
        cp.setBorders(false);
        cp.setHeaderVisible(false);
        usagesGrid = NodeUsagesGrid.createUsageGrid(selectedNodeList, baseUsageUrl);
        usagesGrid.setSize(windowWidth, 200);
        cp.add(usagesGrid);
        formPanel.add(cp);

        /* Comments textarea */
        final TextArea textArea = new TextArea();
        textArea.setSize(windowWidth - 30, 100);
        if (!permanentlyDelete) {
            formPanel.add(new Html("<br />" + Messages.get("label.comment", "Comment") + ": <br />"));
            formPanel.add(textArea);
        }

        // listener on the grid because the message depends on the number of usages found, and we get this at the very end
        final int nbSelectedNodes = selectedNodeList.size();
        usagesGrid.getStore().getLoader().addLoadListener(new LoadListener() {
            @Override
            public void loaderLoad(LoadEvent le) {
                List<GWTJahiaNode> data = le.getData();
                int nbRows = data.size();
                String strMessage = getConfirmationMessage(nodesToBeDeleted, nbSelectedNodes);
                if (nbRows > 0) {
                    strMessage += "<br /><br />" + (nbSelectedNodes > 1 ? Messages.get("message.remove.multiple.usage", "Those nodes are still used in:") : Messages.get("message.remove.single.usage", "This node is still used by:"));
                } else {
                    // no empty grid if no usages
                    formPanel.remove(cp);
                    textArea.setSize("" + (windowWidth - 30), "70%");

                    if (permanentlyDelete) {
                        setHeight(130);
                        formPanel.setHeight(130);
                    }
                }
                textMessage.setHtml(strMessage);
                formPanel.layout();
            }
        });

        if (permanentlyDelete) {
            String permanentDeletionMessage = Messages.get("message.remove.warning",
                    "<br/><span style=\"font-style:bold;color:red;\">Warning: this will erase the content definitively from the repository<br/>So it will not be displayed anymore anywere</span>");
            formPanel.add(new Html(permanentDeletionMessage));
            setIcon(StandardIconsProvider.STANDARD_ICONS.warning());
        }

        /* Buttons */
        Button submit = new Button(Messages.get("label.yes"), new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent event) {

                hide();
                linker.loading(Messages.get("label.executing"));
                final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();

                BaseAsyncCallback<GWTJahiaNode> baseAsyncCallback = new BaseAsyncCallback<GWTJahiaNode>() {

                    @Override
                    public void onApplicationFailure(Throwable throwable) {
                        linker.loaded();
                        Log.error(throwable.getMessage(), throwable);
                        MessageBox.alert(Messages.get("label.error", "Error"), throwable.getMessage(), null);
                    }

                    @Override
                    public void onSuccess(GWTJahiaNode displayableParentNode) {

                        linker.loaded();
                        EditLinker editLinker = null;
                        if (linker instanceof SidePanelTabItem.SidePanelLinker) {
                            editLinker = ((SidePanelTabItem.SidePanelLinker) linker).getEditLinker();
                        } else if (linker instanceof EditLinker) {
                            editLinker = (EditLinker) linker;
                        }

                        if (!skipRefreshOnDelete) {
                            Map<String, Object> data = new HashMap<String, Object>();
                            if (editLinker != null && selectedPathList.contains(editLinker.getSelectionContext().getMainNode().getPath())) {
                                data.put(Linker.MAIN_DELETED, true);
                                if (displayableParentNode == null) {
                                    data.put("node", editLinker.getSelectionContext().getMainNode());
                                } else {
                                    data.put("node", displayableParentNode);
                                }
                            } else {
                                data.put(Linker.REFRESH_ALL, true);
                            }
                            linker.refresh(data);
                        }
                        linker.select(null);

                        if (permanentlyDelete && selectedNodeList.size() == 1 && selectedNodeList.get(0).isPage()) {
                            updateReduxStoreForPageComposer();
                        }

                        String operation = (permanentlyDelete ? "delete" : "update");
                        for (GWTJahiaNode selectedNode : selectedNodeList) {
                            ContentHelper.sendContentModificationEvent(selectedNode.getUUID(), selectedNode.getPath(), selectedNode.getName(), operation, null);
                        }
                    }
                };

                if (permanentlyDelete) {
                    async.deletePaths(selectedPathList, baseAsyncCallback);
                } else {
                    async.markForDeletion(selectedPathList, textArea.getValue(), baseAsyncCallback);
                }
            }
        });
        submit.addStyleName("button-yes");

        Button cancel = new Button(Messages.get("label.no"), new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });
        cancel.addStyleName("button-no");

        ButtonBar buttons = new ButtonBar() ;
        buttons.setAlignment(HorizontalAlignment.CENTER);
        buttons.add(submit);
        buttons.add(cancel);

        formPanel.add(buttons);
        setBottomComponent(buttons);

        add(formPanel);
    }

    private String getConfirmationMessage(List<GWTJahiaNode> nodes, int nbSelectedNodes) {
        String message = "";
        if (nbSelectedNodes > 1) {
            message = Messages.getWithArgs("message.remove.multiple.confirm", "Do you really want to remove the {0} selected resources?", new String[] { String.valueOf(nbSelectedNodes) });
        } else {
            GWTJahiaNode node = nodes.get(0);
            if (node.getNodeTypes().contains("jmix:canBeUseAsTemplateModel")) {
                message = Messages.getWithArgs("message.remove.single.pagemodel.confirm", "Do you really want to remove the selected PAGE model {0}?", new String[] { node.getName() });
            } else if (node.getNodeTypes().contains("jnt:page")) {
                message = Messages.getWithArgs("message.remove.single.page.confirm", "Do you really want to remove the selected PAGE {0}?", new String[] { node.getName() });
            } else {
                message = Messages.getWithArgs("message.remove.single.confirm", "Do you really want to remove the selected resource {0}?", new String[] { node.getName() });
            }
        }
        return message;
    }

    public static native void updateReduxStoreForPageComposer() /*-{
        try {
            if ($wnd.top.jahia && $wnd.top.jahia.reduxStore) {
               $wnd.top.jahia.reduxStore.dispatch({type: 'PC_SET_NAVIGATE_TO', payload: null});
            }
        } catch (e) {
            console.warn('Failed to update PageComposer redux store, you should still have all functionality but may have to do an extra click.', e);
        }
    }-*/;
}
