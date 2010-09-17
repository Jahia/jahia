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

package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowType;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.compare.CompareEngine;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.TranslateContentEngine;
import org.jahia.ajax.gwt.client.widget.edit.workflow.dialog.WorkflowActionDialog;
import org.jahia.ajax.gwt.client.widget.workflow.PublicationManagerEngine;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowDashboardEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: toto
 * Date: Sep 4, 2009
 * Time: 4:14:11 PM
 */
public class EditActions {

    /**
     * Switch mode
     *
     * @param linker
     */
    public static void switchMode(final Linker linker, final int mode) {
        if (linker.getMainNode() != null) {
            String path = linker.getMainNode().getPath();
            String locale = JahiaGWTParameters.getLanguage();
            JahiaContentManagementService.App.getInstance()
                    .getNodeURL(path, null, null, locale, mode, new BaseAsyncCallback<String>() {
                        public void onSuccess(String url) {
                            com.google.gwt.user.client.Window.open(url, "mode" + mode, "");
                        }

                    });
        }

    }

    /**
     * Show compare engine
     *
     * @param linker
     */
    public static void showCompare(final Linker linker) {
        if (linker.getSelectedNode() != null) {
            String locale = JahiaGWTParameters.getUILanguage();
            new CompareEngine(linker.getSelectedNode(), locale, linker).show();
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
     * Display edit content window for the source content.
     *
     * @param linker
     */
    public static void editSource(final Linker linker) {
        if (linker.getMainNode() != null) {
            EngineLoader.showEditEngine(linker, linker.getSelectedNode().getReferencedNode());
        }
    }


    /**
     * Show translate engine
     *
     * @param linker
     */
    public static void showTranslateEngine(Linker linker) {
        if (linker.getMainNode() != null) {
            new TranslateContentEngine(linker.getSelectedNode(), linker).show();
        }
    }

    /**
     * @param linker
     */
    public static void showWorkflowDashboard(Linker linker) {
        if (linker.getMainNode() != null) {
            new WorkflowDashboardEngine(linker).show();
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
                                GWTJahiaWorkflowDefinition def = selectedNode.getWorkflowInfo().getPossibleWorkflows().get(new GWTJahiaWorkflowType("publish"));
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
                            com.google.gwt.user.client.Window.alert("Cannot get status: " + caught.getMessage());
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

    /**
     * Unpublish selected content
     *
     * @param linker
     */
    public static void unpublish(final Linker linker) {
        GWTJahiaNode selectedNode = linker.getSelectedNode();
        if (selectedNode == null) {
            selectedNode = linker.getMainNode();
        }
        if (selectedNode != null) {
            linker.loading(Messages.get("label.content.unpublishing", "Unpublishing"));

            JahiaContentManagementService.App.getInstance()
                    .unpublish(Arrays.asList(selectedNode.getPath()), new BaseAsyncCallback<Object>() {
                        public void onApplicationFailure(Throwable caught) {
                            linker.loaded();
                            Log.error("Cannot publish", caught);
                            com.google.gwt.user.client.Window.alert("Cannot unpublish " + caught.getMessage());
                        }

                        public void onSuccess(Object result) {
                            linker.loaded();
                            Info.display(Messages.get("label.content.unpublished"),
                                    Messages.get("label.content.unpublished"));
                            linker.refresh(EditLinker.REFRESH_ALL);
                        }
                    });
        }
    }


    /**
     * Delete content
     *
     * @param linker
     */
    public static void delete(final Linker linker) {
        if (linker.getSelectedNodes() != null && !linker.getSelectedNodes().isEmpty()) {
            // Usages
            final List<String> l = new ArrayList<String>();
            for (GWTJahiaNode node : linker.getSelectedNodes()) {
                l.add(node.getPath());
            }
            final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
            async.getUsages(l, new BaseAsyncCallback<List<GWTJahiaNodeUsage>>() {
                public void onSuccess(List<GWTJahiaNodeUsage> result) {
                    if (l.size() == 1 && linker.getSelectedNode().isReference()) {
                        List<String> paths = new ArrayList<String>();
                        for (GWTJahiaNode node : linker.getSelectedNodes()) {
                            paths.add(node.getPath());
                        }
                        async.deletePaths(paths, new BaseAsyncCallback<Object>() {
                            public void onApplicationFailure(Throwable throwable) {
                                Log.error(throwable.getMessage(), throwable);
                                MessageBox.alert("", throwable.getMessage(), null);
                            }

                            public void onSuccess(Object o) {
                                linker.refresh(EditLinker.REFRESH_ALL);
                                linker.select(null);
                            }
                        });
                    } else {
                        String message = l.size() > 1 ? Messages.getWithArgs("message.remove.multiple.confirm",
                                                                             "Do you really want to remove the {0} selected resources?",
                                                                             new String[]{String.valueOf(
                                                                                     l.size())}) : Messages.getWithArgs(
                                "message.remove.single.confirm",
                                "Do you really want to remove the selected resource {0}?",
                                new String[]{linker.getSelectedNodes().get(0).getName()});
                        if (l.size() > 1) {
                            message += "<br/><br/>";
                            int i = 0;
                            for (GWTJahiaNode node : linker.getSelectedNodes()) {
                                if (i > 4) {
                                    message += "<br/>...";
                                    break;
                                }
                                message += "<br/>" + node.getName();
                                i++;
                            }
                        }
                        message+="<br/><br/>";
                        String n = "";
                        int size = result.size();
                        if(size>0) {
                            message +=l.size() > 1 ? Messages.get("message.remove.multiple.usage",
                                                                             "Those nodes are still used in:") : Messages.get(
                                "message.remove.single.usage",
                                "This node is still used by:");
                        }
                        int i = 0;                        
                        for (int j = 0; j < (size>4?4:size); j++) {
                            GWTJahiaNodeUsage nodeUsage = result.get(j);
                            if (!nodeUsage.getNodeName().equals(n)) {
                                message += "<br/><span style=\"font-style:italic;\">" + nodeUsage.getNodeTitle() + " " + Messages.get(
                                        "label.remove.used", "is using this node in page(s)") + "<br/>" +nodeUsage.getPageTitle()+"</span>";
                                i++;
                            } else {
                                message += "<br/><span style=\"font-style:italic;\">" + nodeUsage.getPageTitle()+"</span>";
                            }
                            n = nodeUsage.getNodeName();
                        }
                        if(i>4) {
                            message+="<br/>.<br/>.<br/>.";
                        }
                        MessageBox.confirm("", message, new Listener<MessageBoxEvent>() {
                            public void handleEvent(MessageBoxEvent be) {
                                if (be.getButtonClicked().getText().equalsIgnoreCase(Dialog.YES)) {
                                    List<String> paths = new ArrayList<String>();
                                    for (GWTJahiaNode node : linker.getSelectedNodes()) {
                                        paths.add(node.getPath());
                                    }
                                    async.deletePaths(paths, new BaseAsyncCallback<Object>() {
                                        public void onApplicationFailure(Throwable throwable) {
                                            Log.error(throwable.getMessage(), throwable);
                                            MessageBox.alert("", throwable.getMessage(), null);
                                        }

                                        public void onSuccess(Object o) {
                                            linker.refresh(EditLinker.REFRESH_ALL);
                                            linker.select(null);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }

                public void onApplicationFailure(Throwable caught) {
                    com.google.gwt.user.client.Window.alert("Cannot get status: " + caught.getMessage());
                }
            });
        }
    }

    public static void showPublicationManager(final Linker linker) {
        if (linker.getMainNode() != null) {
            JahiaContentManagementService.App.getInstance().getSiteLanguages(new BaseAsyncCallback<List<GWTJahiaLanguage>>() {
                public void onSuccess(List<GWTJahiaLanguage> result) {
                    new PublicationManagerEngine(linker,result).show();
                }
            });
        }
    }

    public static void flush(Linker linker) {
        JahiaContentManagementService.App.getInstance().flush(linker.getSelectedNode().getPath(), new BaseAsyncCallback<Void>() {
            public void onSuccess(Void result) {
                Info.display(Messages.get("label.cache.flushed"),
                                    Messages.get("label.cache.flushed"));
            }
        });
    }

    public static void flushAll() {
        JahiaContentManagementService.App.getInstance().flushAll(new BaseAsyncCallback<Void>() {
            public void onSuccess(Void result) {
                Info.display(Messages.get("label.cache.flushed.all"),
                                    Messages.get("label.cache.flushed.all"));
            }
        });
    }
}
