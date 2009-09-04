package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.core.client.GWT;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.GWTJahiaBasicDataBean;
import org.jahia.ajax.gwt.client.util.icons.ActionIconsImageBundle;
import org.jahia.ajax.gwt.client.messages.Messages;

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

//    ListStore<GWTJahiaBasicDataBean> templateListStore;


    public EditModeToolBar() {
        setLayout(new FitLayout());
        toolbar = new ToolBar();
        buttons = new ArrayList<EditActionItem>();

        EditActionItem createPage = new EditActionItem(Messages.getNotEmptyResource("fm_newpagecontent", "New page"), "fm-newcontent") {
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

        EditActionItem publish = new EditActionItem("publish", "fm-newcontent") {
            public void onSelection() {
                EditActions.publish(editLinker);
            }

            public void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) {
                setEnabled(selectedModule != null && selectedModule.getNode().isWriteable());
            }
        };
        toolbar.add(publish.getTextToolitem());
        buttons.add(publish);

        EditActionItem unpublish = new EditActionItem("unpublish", "fm-newcontent") {
            public void onSelection() {
                EditActions.unpublish(editLinker);
            }

            public void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) {
                setEnabled(selectedModule != null && selectedModule.getNode().isWriteable());
            }
        };
        toolbar.add(unpublish.getTextToolitem());
        buttons.add(unpublish);

        EditActionItem lock = new EditActionItem(Messages.getResource("fm_lock"), "fm-lock") {
            public void onSelection() {
                EditActions.switchLock(editLinker);
            }

            public void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) {
                setEnabled(selectedModule != null && !selectedModule.getNode().isLocked());
            }
        };
        toolbar.add(lock.getTextToolitem());
        buttons.add(lock);

        EditActionItem unlock = new EditActionItem(Messages.getResource("fm_unlock"), "fm-unlock") {
            public void onSelection() {
                EditActions.switchLock(editLinker);
            }

            public void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) {
                setEnabled(selectedModule != null && selectedModule.getNode().isLocked());
            }
        };
        toolbar.add(unlock.getTextToolitem());
        buttons.add(unlock);

        EditActionItem edit = new EditActionItem(Messages.getResource("fm_edit"), "fm-edit") {
            public void onSelection() {
                EditActions.edit(editLinker);
            }

            public void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) {
                setEnabled(selectedModule != null && selectedModule.getNode().isWriteable());
            }
        };
        toolbar.add(edit.getTextToolitem());
        buttons.add(edit);

        EditActionItem delete = new EditActionItem(Messages.getResource("fm_remove"), "fm-remove") {
            public void onSelection() {
                EditActions.delete(editLinker);
            }

            public void enableOnConditions(Module selectedModule, GWTJahiaNode selectedNode) {
                setEnabled(selectedModule != null && selectedModule.getNode().isWriteable());
            }
        };
        toolbar.add(delete.getTextToolitem());
        buttons.add(delete);

        add(toolbar);
    }

//    private void updateButtonsState(GWTJahiaNode node) {
//        if (node == null) {
//            deleteButton.setEnabled(false);
//            lockButton.setEnabled(false);
//            editButton.setEnabled(false);
//            saveButton.setEnabled(false);
//            return;
//        }
//        if (node.isLockable()) {
//            lockButton.setEnabled(true);
//        }
//        if (!node.isLocked()) {
//            if (node.isWriteable()) {
//                deleteButton.setEnabled(true);
//                editButton.setEnabled(true);
//            }
//        } else {
//            showUnlockButton();
//        }
//    }
//


//    public void onTemplateBoxSelection(GWTJahiaBasicDataBean selectedItem) {
//         currentrySelectedTemplate = selectedItem.getValue();
//         displayPreview(currentlySelectedNode, currentrySelectedTemplate);
//    }

//    /**
//     * This will update the template conbo box based on the page selected item
//     *
//     * @param node the selected node (item on the page)
//     */
//    public void updateTemplateBox(GWTJahiaNode node) {
//        templateBox.getStore().removeAll();
//        templateBox.clearSelections();
//        if (node != null) {
//            JahiaContentManagementService.App.getInstance().getTemplatesPath(node.getPath(),new AsyncCallback<List<String[]>>() {
//                public void onFailure(Throwable throwable) {
//                    Log.error("", throwable);
//                    com.google.gwt.user.client.Window.alert("-->" + throwable.getMessage());
//                }
//                public void onSuccess(List<String[]> strings) {
//                    for(String[] template:strings) {
//                        templateBox.getStore().add(new GWTJahiaBasicDataBean(template[0], template[1]));
//                    }
//                    templateBox.setValue(new GWTJahiaBasicDataBean(currentlySelectedNodeTemplate,currentlySelectedNodeTemplate));
//                }
//            });
//        }
//    }
//

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
    }

    public void handleNewSidePanelSelection(GWTJahiaNode node) {

    }

}
