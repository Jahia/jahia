package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.GWTJahiaBasicDataBean;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.util.icons.ActionIconsImageBundle;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 4, 2009
 * Time: 2:29:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditModeToolBar extends HorizontalPanel {
    public static final ActionIconsImageBundle ACTION_ICONS = GWT.create(ActionIconsImageBundle.class);

    private EditLinker editLinker;

    private ToolBar toolbar;

    private List<EditActionItem> buttons;
    private EditActionItem createPage;
    private EditActionItem publish;
    private EditActionItem unpublish;
    private EditActionItem lock;
    private EditActionItem unlock;
    private EditActionItem edit;
    private EditActionItem delete;

//    ListStore<GWTJahiaBasicDataBean> templateListStore;


    public EditModeToolBar() {
        setLayout(new FitLayout());
        toolbar = new ToolBar();
        buttons = new ArrayList<EditActionItem>();

        createPage = new EditActionItem(Messages.getNotEmptyResource("fm_newpagecontent", "New page"), "fm-newcontent") {
            public void onSelection() {
                EditActions.createPage(editLinker);
            }

            public void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) {
//                setEnabled(editLinker.getMainModule().getNode().getNodeTypes().contains("jnt:page") || editLinker.getMainModule().getNode().getInheritedNodeTypes().contains("jnt:page"));
                setEnabled(true);
            }
        };
        toolbar.add(createPage.getTextToolitem());
        buttons.add(createPage);

        toolbar.add(new SeparatorMenuItem());

        publish = new EditActionItem("publish", "fm-newcontent") {
            public void onSelection() {
                EditActions.publish(editLinker);
            }

            public void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) {
//                setEnabled(selectedModule != null && selectedModule.getNode().isWriteable());
                setEnabled(false);
            }
        };
        toolbar.add(publish.getTextToolitem());
        buttons.add(publish);

        unpublish = new EditActionItem("unpublish", "fm-newcontent") {
            public void onSelection() {
                EditActions.unpublish(editLinker);
            }

            public void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) {
//                setEnabled(selectedModule != null && selectedModule.getNode().isWriteable());
                setEnabled(false);
            }
        };
        toolbar.add(unpublish.getTextToolitem());
        buttons.add(unpublish);

        toolbar.add(new SeparatorMenuItem());

        lock = new EditActionItem(Messages.getResource("fm_lock"), "fm-lock") {
            public void onSelection() {
                EditActions.switchLock(editLinker);
            }

            public void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) {
                setEnabled(selectedModule != null && selectedModule.getNode().isLockable() && !selectedModule.getNode().isLocked());
            }
        };
        toolbar.add(lock.getTextToolitem());
        buttons.add(lock);

        unlock = new EditActionItem(Messages.getResource("fm_unlock"), "fm-unlock") {
            public void onSelection() {
                EditActions.switchLock(editLinker);
            }

            public void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) {
                setEnabled(selectedModule != null && selectedModule.getNode().isLockable() && selectedModule.getNode().isLocked());
            }
        };
        toolbar.add(unlock.getTextToolitem());
        buttons.add(unlock);

        edit = new EditActionItem(Messages.getResource("fm_edit"), "fm-edit") {
            public void onSelection() {
                EditActions.edit(editLinker);
            }

            public void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) {
                setEnabled(selectedModule != null && selectedModule.getNode().isWriteable());
            }
        };
        toolbar.add(edit.getTextToolitem());
        buttons.add(edit);

        delete = new EditActionItem(Messages.getResource("fm_remove"), "fm-remove") {
            public void onSelection() {
                EditActions.delete(editLinker);
            }

            public void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) {
                setEnabled(selectedModule != null && selectedModule.getNode().isWriteable());
            }
        };
        toolbar.add(delete.getTextToolitem());
        buttons.add(delete);

        status = new Button("status : ");
        status.disable();
        toolbar.add(status);

        add(toolbar);
    }

    private Button status;

    public void initWithLinker(EditLinker editLinker) {
        this.editLinker = editLinker;

        for (EditActionItem button : buttons) {
            button.enableOnConditions(null, null);
        }
    }

    public void handleNewModuleSelection(Module selectedModule) {
        for (EditActionItem button : buttons) {
            button.enableOnConditions(selectedModule, null);
        }

        if (selectedModule != null) {
            final String s = selectedModule.getNode().getPath();
            status.setText(s);
            JahiaContentManagementService.App.getInstance().getPublicationInfo(s, new AsyncCallback<GWTJahiaPublicationInfo>() {
                public void onFailure(Throwable caught) {

                }

                public void onSuccess(GWTJahiaPublicationInfo result) {
                    switch (result.getStatus()) {
                        case GWTJahiaPublicationInfo.MODIFIED:
                            publish.setEnabled(true);
                            unpublish.setEnabled(true);
                            status.setText("status : " + s + " : modified");
                            break;
                        case GWTJahiaPublicationInfo.PUBLISHED:
                            publish.setEnabled(false);
                            unpublish.setEnabled(true);
                            status.setText("status : " + s + " : published");
                            break;
                        case GWTJahiaPublicationInfo.UNPUBLISHED:
                            publish.setEnabled(true);
                            unpublish.setEnabled(false);
                            status.setText("status : " + s + " : unpublished");
                            break;
                        case GWTJahiaPublicationInfo.UNPUBLISHABLE:
                            publish.setEnabled(false);
                            unpublish.setEnabled(false);
                            status.setText("status : " + s + " : unpublishable / publish parent first");
                            break;
                    }
                }
            });
        }

    }

    public void handleNewSidePanelSelection(GWTJahiaNode node) {

    }

}
