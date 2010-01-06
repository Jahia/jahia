package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

    public RightsTabItem(EditContentEngine engine) {
        super(Messages.get("ece_rights", "Rights"), engine);
    }

    @Override
    public void create() {
        final GWTJahiaNode node = engine.getNode();
        JahiaContentManagementService.App.getInstance().getACL(node.getPath(), new AsyncCallback<GWTJahiaNodeACL>() {
            /**
             * onsuccess
             * @param gwtJahiaNodeACL
             */
            public void onSuccess(final GWTJahiaNodeACL gwtJahiaNodeACL) {
                // auth. editor
                rightsEditor = new AclEditor(gwtJahiaNodeACL, node.getAclContext());
                rightsEditor.setAclGroup(JCRClientUtils.AUTHORIZATIONS_ACL);
                rightsEditor.setCanBreakInheritance(false);
                if (!(node.getProviderKey().equals("default") || node.getProviderKey().equals("jahia"))) {
                    rightsEditor.setReadOnly(true);
                } else {
                    rightsEditor.setReadOnly(!node.isWriteable() || node.isLocked());
                }
                Button saveButton = rightsEditor.getSaveButton();
                saveButton.setVisible(false);
                //saveButton.addSelectionListener(new SaveAclSelectionListener(selectedNode, AUTH_TAB_ITEM));
                setLayout(new FitLayout());
                add(rightsEditor.renderNewAclPanel());
                layout();
                setProcessed(true);
            }

            /**
             * On failure
             * @param throwable
             */
            public void onFailure(Throwable throwable) {
                Log.debug("Cannot retrieve acl", throwable);
            }

        });
    }

    public AclEditor getRightsEditor() {
        return rightsEditor;
    }
}
