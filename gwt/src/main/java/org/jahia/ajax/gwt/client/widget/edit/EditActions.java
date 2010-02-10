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
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.CreatePageContentEngine;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EditContentEngine;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.TranslateContentEngine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            JahiaContentDefinitionService.App.getInstance().getNodeType("jnt:page", new AsyncCallback<GWTJahiaNodeType>() {
                public void onFailure(Throwable throwable) {
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
     * Switch mode
     *
     * @param linker
     */
    public static void switchMode(final Linker linker,int mode) {
        if (linker.getMainNode() != null) {
            String path = linker.getMainNode().getPath();
            String locale = JahiaGWTParameters.getLanguage();
            JahiaContentManagementService.App.getInstance().getNodeURL(path,  locale, mode, new AsyncCallback<String>() {
                public void onSuccess(String url) {
                    com.google.gwt.user.client.Window.Location.replace(url);
                }

                public void onFailure(Throwable throwable) {
                    Log.error("", throwable);
                }
            });
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
     * Show transla engine
     * @param linker
     */
    public static void showTranslateEngine(Linker linker) {
        if (linker.getMainNode() != null) {
            new TranslateContentEngine(linker.getSelectedNode(), linker).show();
        }
    }


    /**
     * Publish selected content
     *
     * @param linker
     */
    public static void publish(final Linker linker) {
        GWTJahiaNode selectedNode = linker.getSelectedNode();
        if (selectedNode == null) {
            selectedNode = linker.getMainNode();
        }
        if (selectedNode != null) {
            final GWTJahiaNode s = selectedNode;
            JahiaContentManagementService.App.getInstance().getPublicationInfo(s.getPath(), true,
                    new AsyncCallback<GWTJahiaPublicationInfo>() {
                        public void onSuccess(GWTJahiaPublicationInfo result) {
                            new PublicationStatusWindow(linker, s, result).show();
                        }

                        public void onFailure(Throwable caught) {
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

            JahiaContentManagementService.App.getInstance().publish(selectedNode.getPath(), false, "", true, new AsyncCallback() {
                public void onFailure(Throwable caught) {
                    Log.error("Cannot publish", caught);
                    com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                }

                public void onSuccess(Object result) {
                    Info.display(Messages.getResource("publication_published_title"), Messages.getResource("publication_published_text"));
                    linker.refresh();
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
            JahiaContentManagementService.App.getInstance().unpublish(selectedNode.getPath(), new AsyncCallback() {
                public void onFailure(Throwable caught) {
                    Log.error("Cannot publish", caught);
                    com.google.gwt.user.client.Window.alert("Cannot unpublish " + caught.getMessage());
                }

                public void onSuccess(Object result) {
                    Info.display(Messages.getResource("publication_unpublished_title"), Messages.getResource("publication_unpublished_text"));
                    linker.refresh();
                }
            });
        }
    }


    /**
     * Switch lock
     *
     * @param linker
     */
    public static void switchLock(final Linker linker) {
        if (linker.getSelectedNode() != null) {
            List<String> paths = new ArrayList<String>(1);
            paths.add(linker.getSelectedNode().getPath());
            final boolean isLock = !linker.getSelectedNode().isLocked();
            JahiaContentManagementService.App.getInstance().setLock(paths, isLock, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Log.error("", throwable);
                    com.google.gwt.user.client.Window.alert("-switch lock->" + throwable.getMessage());
                }

                public void onSuccess(Object o) {
                    linker.refreshMainComponent();
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
            MessageBox.confirm("", Messages.get("fm_confRemove", "Do you really want to continue?"), new Listener<MessageBoxEvent>() {
                public void handleEvent(MessageBoxEvent be) {
                    if (be.getButtonClicked().getText().equalsIgnoreCase(Dialog.YES)) {
                        List<String> paths = new ArrayList<String>();
                        for (GWTJahiaNode node : linker.getSelectedNodes()) {
                            paths.add(node.getPath());
                        }
                        JahiaContentManagementService.App.getInstance().deletePaths(paths, new AsyncCallback() {
                            public void onFailure(Throwable throwable) {
                                Log.error(throwable.getMessage(), throwable);
                                MessageBox.alert("", throwable.getMessage(), null);
                            }

                            public void onSuccess(Object o) {
                                linker.refresh(EditLinker.REFRESH_PAGES);
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
            setHeading("Publish");
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
            comments.setFieldLabel(Messages.getResource("publication_publicationComments"));
            form.add(comments);

            final Button cancel = new Button(Messages.getResource("fm_cancel"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    hide();
                }
            });
            final Button ok = new Button(Messages.getResource("publication_publish"));
            SelectionListener<ButtonEvent> selectionListener = new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    ok.setEnabled(false);
                    cancel.setEnabled(false);
                    List<String> selectedPaths = new ArrayList<String>();
                    JahiaContentManagementService.App.getInstance().publish(selectedNode.getPath(), true, comments.getValue(), false, new AsyncCallback() {
                        public void onFailure(Throwable caught) {
                            Log.error("Cannot publish", caught);
                            com.google.gwt.user.client.Window.alert("Cannot publish " + caught.getMessage());
                            hide();
                        }

                        public void onSuccess(Object result) {
                            Info.display(Messages.getResource("publication_published_title"), Messages.getResource("publication_published_text"));
                            linker.refresh();
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
