package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.compare.CompareEngine;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.TranslateContentEngine;
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
            String locale = JahiaGWTParameters.getUILanguage();
            JahiaContentManagementService.App.getInstance()
                    .getNodeURL(path, locale, mode, new BaseAsyncCallback<String>() {
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
     * Dispay edit content window
     *
     * @param linker
     */
    public static void edit(Linker linker) {
        if (linker.getMainNode() != null) {
            EngineLoader.showEditEngine(linker, linker.getSelectedNode());
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
            final List<GWTJahiaNode> s = new ArrayList<GWTJahiaNode>();
            final List<String> uuids = new ArrayList<String>();
            for (GWTJahiaNode selectedNode : selectedNodes) {
                uuids.add(selectedNode.getUUID());
//                if (ModuleHelper.getLinkedContentInfo().containsKey(selectedNode.getUUID())) {
//                    uuids.addAll(ModuleHelper.getLinkedContentInfo().get(selectedNode.getUUID()));
//                }
            }

            JahiaContentManagementService.App.getInstance()
                    .getPublicationInfo(uuids, allSubTree, new BaseAsyncCallback<List<GWTJahiaPublicationInfo>>() {
                        public void onSuccess(List<GWTJahiaPublicationInfo> result) {
                            new PublicationStatusWindow(linker, uuids, result, allSubTree).show();
                        }

                        public void onApplicationFailure(Throwable caught) {
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
            final GWTJahiaNode s = selectedNode;

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
            JahiaContentManagementService.App.getInstance()
                    .unpublish(Arrays.asList(selectedNode.getPath()), new BaseAsyncCallback() {
                        public void onApplicationFailure(Throwable caught) {
                            Log.error("Cannot publish", caught);
                            com.google.gwt.user.client.Window.alert("Cannot unpublish " + caught.getMessage());
                        }

                        public void onSuccess(Object result) {
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
        final int[] nbUsage = new int[1];
        if (linker.getSelectedNodes() != null && !linker.getSelectedNodes().isEmpty()) {
            // Usages
            final List<String> l = new ArrayList<String>();
            for (GWTJahiaNode node : linker.getSelectedNodes()) {
                l.add(node.getPath());
            }
            JahiaContentManagementService.App.getInstance()
                    .getUsages(l, new BaseAsyncCallback<List<GWTJahiaNodeUsage>>() {
                        public void onSuccess(List<GWTJahiaNodeUsage> result) {
                            String message = l.size() > 1 ? Messages.getWithArgs("message.remove.multiple.confirm", "Do you really want to remove the {0} selected resources?", new String[] {String.valueOf(l.size())}) : 
                                Messages.getWithArgs("message.remove.single.confirm", "Do you really want to remove the selected resource {0}?", new String[] {linker.getSelectedNodes().get(0).getName()});
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
                            String n = "";
                            for (GWTJahiaNodeUsage nodeUsage : result) {
                                if (!nodeUsage.getNodeName().equals(n)) {
                                    message += "<br><br>" + nodeUsage.getNodeName() + " " +
                                            Messages.get("label.remove.used", "is used in") + "<br>" +
                                            nodeUsage.getPageTitle();
                                } else {
                                    message += "<br>" + nodeUsage.getPageTitle();
                                }
                                n = nodeUsage.getNodeName();
                            }
                            MessageBox.confirm("", message, new Listener<MessageBoxEvent>() {
                                public void handleEvent(MessageBoxEvent be) {
                                    if (be.getButtonClicked().getText().equalsIgnoreCase(Dialog.YES)) {
                                        List<String> paths = new ArrayList<String>();
                                        for (GWTJahiaNode node : linker.getSelectedNodes()) {
                                            paths.add(node.getPath());
                                        }
                                        JahiaContentManagementService.App.getInstance()
                                                .deletePaths(paths, new BaseAsyncCallback() {
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
}
