/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.util.content.actions;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowType;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.portlet.PortletWizardWindow;
import org.jahia.ajax.gwt.client.widget.edit.ContentTypeWindow;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.publication.PublicationStatusWindow;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowActionDialog;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 7 juil. 2008 - 14:45:03
 */
public class ContentActions {

    /**
     * Pste as reference
     *
     * @param linker
     */
    public static void pasteReference(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        GWTJahiaNode m = null;
        if (selectedItems != null && selectedItems.size() == 1) {
            m = selectedItems.get(0);
        }
        if (m == null) {
            m = linker.getMainNode();
        }
        if (m != null && !m.isFile()) {
            linker.loading(Messages.get("statusbar.pastingref.label"));
            final CopyPasteEngine copyPasteEngine = CopyPasteEngine.getInstance();
            JahiaContentManagementService.App.getInstance().pasteReferences(JCRClientUtils.getPathesList(copyPasteEngine.getCopiedPaths()), m.getPath(), null, new BaseAsyncCallback() {
                public void onApplicationFailure(Throwable throwable) {
                    Window.alert(Messages.get("failure.pasteref.label") + "\n" + throwable.getLocalizedMessage());
                    linker.loaded();
                }

                public void onSuccess(Object o) {
                    boolean refresh = false;
                    for (GWTJahiaNode n : copyPasteEngine.getCopiedPaths()) {
                        if (!n.isFile()) {
                            refresh = true;
                            break;
                        }
                    }
                    copyPasteEngine.onPastedPath();
                    linker.loaded();
                    if (refresh) {
                        linker.refresh(EditLinker.REFRESH_ALL);
                    } else {
                        linker.refresh(Linker.REFRESH_MAIN);
                    }
                }
            });
        }
    }


    /**
     * Move a file
     *
     * @param linker
     * @param sources
     * @param target
     */
    public static void move(final Linker linker, final List<GWTJahiaNode> sources, GWTJahiaNode target) {
        JahiaContentManagementService.App.getInstance().paste(JCRClientUtils.getPathesList(sources), target.getPath(), null, true, new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable throwable) {
                Window.alert("Paste failed :\n" + throwable.getLocalizedMessage());
                linker.loaded();
            }

            public void onSuccess(Object o) {
                boolean refresh = false;
                for (GWTJahiaNode n : sources) {
                    if (!n.isFile()) {
                        refresh = true;
                        break;
                    }
                }

                linker.loaded();
                if (refresh) {
                    linker.refresh(EditLinker.REFRESH_ALL);
                } else {
                    linker.refresh(Linker.REFRESH_MAIN);
                }
            }
        });
    }

    /**
     * Display download link
     *
     * @param linker
     */
    public static void download(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selection = selectedItems.get(0);
            download(linker, selection, selection.getUrl());
        }
    }

    /**
     * Display download link
     *
     * @param linker
     * @param selection
     * @param url
     */
    public static void download(Linker linker, GWTJahiaNode selection, String url) {
        if (selection != null && selection.isFile().booleanValue()) {
            linker.loading(Messages.get("statusbar.downloading.label"));
            if (url != null) {
                HTML link = new HTML(Messages.get("downloadMessage.label") + "<br /><br /><a href=\"" + url + "\" target=\"_new\">" + selection.getName() + "</a>");
                final com.extjs.gxt.ui.client.widget.Window dl = new com.extjs.gxt.ui.client.widget.Window();
                dl.setModal(true);
                dl.setHeading(Messages.get("label.download"));
                dl.setLayout(new FlowLayout());
                dl.setScrollMode(Style.Scroll.AUTO);
                dl.add(link);
                dl.setHeight(120);
                dl.show();
            } else {
                Window.alert(Messages.get("failure.download.label"));
            }
            linker.loaded();
        }
    }

    /**
     * Create a folder
     *
     * @param linker
     */
    public static void createFolder(final Linker linker) {
        GWTJahiaNode parent = linker.getMainNode();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            String newFolder = Window.prompt(Messages.get("newDirName.label"), "untitled");
            if (newFolder != null && newFolder.length() > 0) {
                linker.loading(Messages.get("statusbar.newfoldering.label"));
                JahiaContentManagementService.App.getInstance().createFolder(parent.getPath(), newFolder, new BaseAsyncCallback() {
                    public void onApplicationFailure(Throwable throwable) {
                        Window.alert(Messages.get("failure.newDir.label") + "\n" + throwable.getLocalizedMessage());
                        linker.loaded();
                    }

                    public void onSuccess(Object o) {
                        linker.loaded();
                        linker.refresh(Linker.REFRESH_FOLDERS);
                    }
                });
            }
        }
    }

    /**
     * Create a node
     *
     * @param linker
     * @param windowHeaer
     * @param nodeType
     */
    public static void createNode(final Linker linker, final String windowHeaer, final String nodeType) {
        GWTJahiaNode parent = linker.getMainNode();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            String nodeName = Window.prompt(windowHeaer, "untitled");
            if (nodeName != null && nodeName.length() > 0) {
                linker.loading(Messages.get("statusbar.newfoldering.label"));
                JahiaContentManagementService.App.getInstance().createNode(parent.getPath(), nodeName, nodeType, null, null, null, new BaseAsyncCallback<GWTJahiaNode>() {
                    public void onSuccess(GWTJahiaNode o) {
                        linker.loaded();
                        linker.refresh(EditLinker.REFRESH_ALL);
                    }

                    public void onApplicationFailure(Throwable throwable) {
                        Log.error("Unable to create [" + nodeType + "]", throwable);
                        linker.loaded();
                    }
                });
            }
        }
    }

    /**
     * Show portlet wizard form
     *
     * @param linker
     */
    public static void showPortletWizard(final Linker linker) {
        GWTJahiaNode parent = (GWTJahiaNode) linker.getMainNode();
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        if (parent != null && !parent.isFile()) {
            PortletWizardWindow window = new PortletWizardWindow(linker, parent);
            window.show();
        }
    }

    /**
     * Show deploy portlet form
     * @param linker
     */

    /**
     * Show content wizard with a selected node type
     *
     * @param linker
     * @param nodeTypes
     */
    public static void showContentWizard(final Linker linker, final String nodeTypes) {
        GWTJahiaNode parent = linker.getSelectedNode();
        if (parent == null) {
            parent = linker.getMainNode();
        }
        if (parent == null) {
            final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
            if (selectedItems != null && selectedItems.size() == 1) {
                parent = selectedItems.get(0);
            }
        }
        showContentWizard(linker, nodeTypes, parent);
    }

    public static void showContentWizard(final Linker linker, final String nodeTypes, final GWTJahiaNode parent) {
        if (parent != null && !parent.isFile()) {
            JahiaContentDefinitionService.App.getInstance().getSubNodetypes(nodeTypes,
                    new BaseAsyncCallback<Map<GWTJahiaNodeType, List<GWTJahiaNodeType>>>() {
                        public void onApplicationFailure(Throwable caught) {
                            MessageBox.alert("Alert",
                                    "Unable to load content definitions for base type '" + nodeTypes + "'. Cause: " + caught.getLocalizedMessage(),
                                    null);
                        }

                        public void onSuccess(
                                Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> result) {
                            if (result.size() == 1 && result.values().iterator().next().size() == 1) {
                                EngineLoader.showCreateEngine(linker, parent,
                                        result.values().iterator().next().iterator().next(), new HashMap<String, GWTJahiaNodeProperty>(),
                                        null, false);
                                        ;
                            } else {
                                new ContentTypeWindow(linker, parent, result, false).show();
                            }
                        }
                    });
        }
    }

    public static void lock(boolean lock, final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() > 0) {
            List<String> selectedPaths = new ArrayList<String>(selectedItems.size());
            List<String> lockedBySystem = new LinkedList<String>();
            for (GWTJahiaNode node : selectedItems) {
                if (lock && !node.isLocked() || !lock && node.isLocked()) {
                    if (!lock
                            && node.getLockOwner() != null
                            && node.getLockOwner().equals(
                            JahiaGWTParameters.SYSTEM_USER)) {
                        lockedBySystem.add(node.getPath());
                    }
                    selectedPaths.add(node.getPath());
                }
            }
            boolean continueOperation = true;
            if (!lockedBySystem.isEmpty()) {
                StringBuffer lockedFiles = new StringBuffer();
                for (String file : lockedBySystem) {
                    lockedFiles.append("\n").append(
                            file.contains("/") ? file.substring(file
                                    .lastIndexOf('/') + 1) : file);
                }
                continueOperation = Window
                        .confirm(Messages.get("warning.systemLock.label") + "\n"
                                + lockedFiles.toString()
                                + "\n\n" + Messages.get("confirm.unlock.label"));
            }
            if (continueOperation && !selectedPaths.isEmpty()) {
                linker.loading(lock ? Messages.get("statusbar.locking.label") : Messages.get("statusbar.unlocking.label"));
                JahiaContentManagementService.App.getInstance().setLock(selectedPaths, lock, new BaseAsyncCallback() {
                    public void onApplicationFailure(Throwable throwable) {
                        MessageBox.alert("Error", throwable.getLocalizedMessage(), null);
                        linker.loaded();
                        linker.refresh(Linker.REFRESH_MAIN);
                    }

                    public void onSuccess(Object o) {
                        linker.loaded();
                        linker.refresh(Linker.REFRESH_MAIN);
                    }
                });
            }
        }
    }

    /**
     * Display edit content window
     *
     * @param linker
     */
    public static void edit(Linker linker) {
        if (linker.getMainNode() != null) {
            EngineLoader.showEditEngine(linker, linker.getSelectedNode());
        }
    }

    /**
     * Publish selected content
     *
     * @param linker
     * @param allSubTree
     */
    public static void publish(final Linker linker, final boolean allSubTree) {
        List<GWTJahiaNode> selectedNodes = linker.getSelectedNodes();
        if (selectedNodes.isEmpty()) {
            selectedNodes = new ArrayList<GWTJahiaNode>();
            selectedNodes.add(linker.getMainNode());
        }
        if (!selectedNodes.isEmpty()) {
            final List<String> uuids = new ArrayList<String>();
            for (GWTJahiaNode selectedNode : selectedNodes) {
                uuids.add(selectedNode.getUUID());
//                if (ModuleHelper.getLinkedContentInfo().containsKey(selectedNode.getUUID())) {
//                    uuids.addAll(ModuleHelper.getLinkedContentInfo().get(selectedNode.getUUID()));
//                }
            }
            linker.loading(Messages.get("label.gettingPublicationInfo","Getting publication information"));
            JahiaContentManagementService.App.getInstance()
                    .getPublicationInfo(uuids, allSubTree, new BaseAsyncCallback<List<GWTJahiaPublicationInfo>>() {
                        public void onSuccess(List<GWTJahiaPublicationInfo> result) {
                            linker.loaded();

                            List<GWTJahiaPublicationInfo> filteredList = new ArrayList<GWTJahiaPublicationInfo>();
                            for (GWTJahiaPublicationInfo info : result) {
                                if (info.getStatus() > GWTJahiaPublicationInfo.PUBLISHED) {
                                    filteredList.add(info);
                                }
                            }
                            if (filteredList.isEmpty()) {
                                MessageBox.info(Messages.get("label.publication","Publication"), Messages.get("label.nothingToPublish","Nothing to publish"), null);
                            } else {
                                final GWTJahiaNode selectedNode = linker.getSelectedNode();
                                GWTJahiaWorkflowDefinition
                                        def = selectedNode.getWorkflowInfo().getPossibleWorkflows().get(new GWTJahiaWorkflowType("publish"));
                                if (def != null) {
                                    WorkflowActionDialog wad = new WorkflowActionDialog(selectedNode, linker);
                                    wad.setCustom(new PublicationWorkflow(filteredList, uuids, allSubTree, selectedNode.getLanguageCode()));
                                    wad.initStartWorkflowDialog(def);
                                    wad.show();
                                } else {
                                    new PublicationStatusWindow(linker, uuids, filteredList, allSubTree).show();
                                }
                            }
                        }

                        public void onApplicationFailure(Throwable caught) {
                            linker.loaded();
                            Window.alert("Cannot get status: " + caught.getMessage());
                        }
                    });
        }
    }

    /**
     * Publish selected content
     *
     * @param linker
     */
    public static void reversePublish(final Linker linker) {
        GWTJahiaNode selectedNode = linker.getSelectedNode();
        if (selectedNode == null) {
            selectedNode = linker.getMainNode();
        }
        if (selectedNode != null) {

//            JahiaContentManagementService.App.getInstance().publish(Arrays.asList(selectedNode.getUUID()), false, "", false, true, new BaseAsyncCallback() {
//                public void onApplicationFailure(Throwable caught) {
//                    Log.error("Cannot publish", caught);
//                    com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
//                }
//
//                public void onSuccess(Object result) {
//                    Info.display(Messages.getResource("message.content.published"), Messages.getResource("message.content.published"));
//                    linker.refresh(EditLinker.REFRESH_ALL);
//                }
//            });
        }
    }
}
