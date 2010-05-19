package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.compare.CompareEngine;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.*;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowDashboardEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * User: toto
 * Date: Sep 4, 2009
 * Time: 4:14:11 PM
 */
public class EditActions {

    /**
     * Create page
     *
     * @param linker
     */
    public static void createPage(final Linker linker) {
        if (linker.getMainNode() != null) {
            JahiaContentDefinitionService.App.getInstance().getNodeType("jnt:page", new BaseAsyncCallback<GWTJahiaNodeType>() {
                public void onApplicationFailure(Throwable throwable) {
                    Log.error("", throwable);
                    com.google.gwt.user.client.Window.alert("-create page->" + throwable.getMessage());
                }

                public void onSuccess(GWTJahiaNodeType gwtJahiaNodeType) {
                    new CreatePageContentEngine(linker, linker.getMainNode(), gwtJahiaNodeType, null).show();
//                    new ContentTypeWindow(linker, linker.getMainNode(), gwtJahiaNodeType,true).show();
                }
            });
        }
    }

    /**
     * Create page
     *
     * @param linker
     */
    public static void createTemplate(final Linker linker) {
        JahiaContentDefinitionService.App.getInstance().getNodeType("jnt:page", new BaseAsyncCallback<GWTJahiaNodeType>() {
            public void onApplicationFailure(Throwable throwable) {
                Log.error("", throwable);
                com.google.gwt.user.client.Window.alert("-create page->" + throwable.getMessage());
            }

            public void onSuccess(GWTJahiaNodeType gwtJahiaNodeType) {
                if (linker instanceof EditLinker) {
//                    GWTJahiaNode n = ((EditLinker) linker).getSidePanel().getPagesTabItem().findTemplateFolder();
//                    new CreatePageContentEngine(linker, n, gwtJahiaNodeType, null).show();
                }

            }
        });
    }

    /**
     * Switch mode
     *
     * @param linker
     */
    public static void switchMode(final Linker linker,final int mode) {
        if (linker.getMainNode() != null) {
            String path = linker.getMainNode().getPath();
            String locale = JahiaGWTParameters.getUILanguage();
            JahiaContentManagementService.App.getInstance().getNodeURL(path,  locale, mode, new BaseAsyncCallback<String>() {
                public void onSuccess(String url) {
                    com.google.gwt.user.client.Window.open(url,"mode"+mode,"");
                }

            });
        }

    }

    /**
     * Show compare engine
     * @param linker
     */
    public static void showCompare(final Linker linker) {
        if (linker.getSelectedNode() != null) {
            String locale = JahiaGWTParameters.getUILanguage();
            new CompareEngine(linker.getSelectedNode(),locale,linker).show();
        }

    }


    /**
     * Dispay edit content window
     *
     * @param linker
     */
    public static void edit(Linker linker) {
        if (linker.getMainNode() != null) {
            new EditContentEngine(linker.getSelectedNode(), linker).show();
        }
    }


    /**
     * Show translate engine
     * @param linker
     */
    public static void showTranslateEngine(Linker linker) {
        if (linker.getMainNode() != null) {
            new TranslateContentEngine(linker.getSelectedNode(), linker).show();
        }
    }

    /**
     *
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
     */
    public static void publish(final Linker linker) {
        List<GWTJahiaNode> selectedNodes = linker.getSelectedNodes();
        if (selectedNodes.isEmpty()) {
            selectedNodes = new ArrayList<GWTJahiaNode>();
            selectedNodes.add(linker.getMainNode());
        }
        if (!selectedNodes.isEmpty()) {
            final List<GWTJahiaNode> s = new ArrayList<GWTJahiaNode>();
            List<String> uuids = new ArrayList<String>();
            for (GWTJahiaNode selectedNode : selectedNodes) {
                if (ModuleHelper.getWrappedContentInfo().containsKey(selectedNode.getUUID())) {
                    uuids.addAll(ModuleHelper.getWrappedContentInfo().get(selectedNode.getUUID()));
                }
                uuids.add(selectedNode.getUUID());
            }

            JahiaContentManagementService.App.getInstance().getPublicationInfo(uuids, true,
                    new BaseAsyncCallback<Map<String,GWTJahiaPublicationInfo>>() {
                        public void onSuccess(Map<String,GWTJahiaPublicationInfo> result) {
                            new PublicationStatusWindow(linker, result).show();
                        }

                        public void onApplicationFailure(Throwable caught) {
                            com.google.gwt.user.client.Window.alert("Cannot get status: " + caught.getMessage());
                        }
                    }
            );
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

            JahiaContentManagementService.App.getInstance().publish(Arrays.asList(selectedNode.getUUID()), false, "", false, true, new BaseAsyncCallback() {
                public void onApplicationFailure(Throwable caught) {
                    Log.error("Cannot publish", caught);
                    com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                }

                public void onSuccess(Object result) {
                    Info.display(Messages.getResource("message.content.published"), Messages.getResource("message.content.published"));
                    linker.refresh(EditLinker.REFRESH_ALL);
                }
            });
        }
    }

    /**
     * Publish all content under selected node
     *
     * @param linker
     */
    public static void publishAll(final Linker linker) {
        GWTJahiaNode selectedNode = linker.getSelectedNode();
        if (selectedNode == null) {
            selectedNode = linker.getMainNode();
        }
        if (selectedNode != null) {
            new PublishAllConfirmWindow(linker, selectedNode).show();
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
            JahiaContentManagementService.App.getInstance().unpublish(Arrays.asList(selectedNode.getPath()), new BaseAsyncCallback() {
                public void onApplicationFailure(Throwable caught) {
                    Log.error("Cannot publish", caught);
                    com.google.gwt.user.client.Window.alert("Cannot unpublish " + caught.getMessage());
                }

                public void onSuccess(Object result) {
                    Info.display(Messages.getResource("label.content.unpublished"), Messages.getResource("label.content.unpublished"));
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
            MessageBox.confirm("", Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.confirm.remove.label", "Do you really want to continue?"), new Listener<MessageBoxEvent>() {
                public void handleEvent(MessageBoxEvent be) {
                    if (be.getButtonClicked().getText().equalsIgnoreCase(Dialog.YES)) {
                        List<String> paths = new ArrayList<String>();
                        for (GWTJahiaNode node : linker.getSelectedNodes()) {
                            paths.add(node.getPath());
                        }
                        JahiaContentManagementService.App.getInstance().deletePaths(paths, new BaseAsyncCallback() {
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


    private static class PublishAllConfirmWindow extends Window {

        private PublishAllConfirmWindow(final Linker linker, final GWTJahiaNode selectedNode) {
            setScrollMode(Style.Scroll.AUTO);
            setHeading(Messages.getResource("label.publish"));
            setSize(800, 500);
            setResizable(false);

            setModal(true);

            final FormPanel form = new FormPanel();
            form.setFrame(false);
            form.setHeaderVisible(false);
            form.setBodyBorder(false);
            form.setBorders(false);

            final TextArea comments = new TextArea();
            comments.setName("comments");
            comments.setFieldLabel(Messages.getResource("org.jahia.jcr.publication.publicationComments"));
            form.add(comments);

            final Button cancel = new Button(Messages.getResource("label.cancel"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    hide();
                }
            });
            final Button ok = new Button(Messages.getResource("label.publish"));
            SelectionListener<ButtonEvent> selectionListener = new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    ok.setEnabled(false);
                    cancel.setEnabled(false);
                    List<String> toPublish = new ArrayList<String>();
                    toPublish.add(selectedNode.getUUID());
                    toPublish.addAll(ModuleHelper.getWrappedContentInfo().get(selectedNode.getUUID()));

                    JahiaContentManagementService.App.getInstance().publish(toPublish, true, comments.getValue(), false,  false, new BaseAsyncCallback() {
                        public void onApplicationFailure(Throwable caught) {
                            Log.error("Cannot publish", caught);
                            com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                            hide();
                        }

                        public void onSuccess(Object result) {
                            Info.display(Messages.getResource("message.content.published"), Messages.getResource("message.content.published"));
                            linker.refresh(EditLinker.REFRESH_ALL);
                            hide();
                        }
                    });

                }
            };
            ok.addSelectionListener(selectionListener);
            setButtonAlign(Style.HorizontalAlignment.CENTER);
            addButton(ok);
            addButton(cancel);
            add(form);
        }
    }

}
