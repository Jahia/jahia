package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:30:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class RightsTabItem extends EditEngineTabItem {
    private AclEditor rightsEditor;

    public RightsTabItem(NodeHolder engine) {
        super(Messages.get("label.rights", "Rights"), engine);
    }

    @Override
    public void create(GWTJahiaLanguage locale) {
        if (engine.getNode() != null) {
            setProcessed(true);
            final GWTJahiaNode node = engine.getNode();
            getACL(node);
        } else if (engine.getParentNode() != null)  {
            setProcessed(true);
            final GWTJahiaNode node = engine.getParentNode();
            getACL(node);
        }
    }

    private void getACL(final GWTJahiaNode node) {
        mask("Loading ACL ...");
        JahiaContentManagementService.App.getInstance().getACL(node.getPath(), new BaseAsyncCallback<GWTJahiaNodeACL>() {
            /**
             * onsuccess
             * @param gwtJahiaNodeACL
             */
            public void onSuccess(final GWTJahiaNodeACL gwtJahiaNodeACL) {
                unmask();
                // auth. editor
                rightsEditor = new AclEditor(gwtJahiaNodeACL, node.getAclContext());
                rightsEditor.setAclGroup(JCRClientUtils.AUTHORIZATIONS_ACL); //todo parameterize
                rightsEditor.setCanBreakInheritance(false);
                if (!(node.getProviderKey().equals("default") || node.getProviderKey().equals("jahia"))) {
                    rightsEditor.setReadOnly(true);
                } else {
                    rightsEditor.setReadOnly(!node.isWriteable() || node.isLocked());
                }
                Button saveButton = rightsEditor.getSaveButton();
                if (toolbarEnabled) {
                    saveButton.setVisible(true);
                    saveButton.addSelectionListener(new SaveAclSelectionListener(engine.getNode()));
                }


                setLayout(new FitLayout());
                add(rightsEditor.renderNewAclPanel());
                layout();
            }

            /**
             * On failure
             * @param throwable
             */
            public void onApplicationFailure(Throwable throwable) {
                Log.debug("Cannot retrieve acl", throwable);
            }
        });
    }

    protected class SaveAclSelectionListener extends SelectionListener<ButtonEvent> {
        private GWTJahiaNode selectedNode;
        private GWTJahiaNodeACL acl;

        private SaveAclSelectionListener(GWTJahiaNode selectedNode) {
            this.selectedNode = selectedNode;
            this.acl = rightsEditor.getAcl();
        }

        public void componentSelected(ButtonEvent event) {
            JahiaContentManagementService.App.getInstance().setACL(selectedNode.getPath(), acl, new BaseAsyncCallback() {
                public void onSuccess(Object o) {
                    Info.display("", "ACL saved");
                    rightsEditor.setSaved();
                }

                public void onApplicationFailure(Throwable throwable) {
                    Log.error("acl save failed", throwable);
                }
            });
        }
    }


    public AclEditor getRightsEditor() {
        return rightsEditor;
    }

}
