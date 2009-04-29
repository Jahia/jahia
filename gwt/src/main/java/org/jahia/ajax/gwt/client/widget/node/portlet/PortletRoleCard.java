/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.node.portlet;

import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.nodes.JCRClientUtils;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: ktlili
 * Date: 25 nov. 2008
 * Time: 10:28:09
 */
public class PortletRoleCard extends MashupWizardCard {
    private AclEditor aclEditor;

    public PortletRoleCard() {
        super(Messages.getNotEmptyResource("mw_roles_perm", "Roles permissions"));
        setHtmlText(Messages.getNotEmptyResource("mw_roles_perm_desc", "Set roles permissions"));
    }

    public void next() {
        if (aclEditor != null) {
            getGwtJahiaNewPortletInstance().setRoles(aclEditor.getAcl());
        }
    }

    public void createUI() {
        // update
        final GWTJahiaNodeACL acl = getPortletWizardWindow().getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition().getBaseAcl();
        List<String> permissions = acl.getAvailablePermissions().get(JCRClientUtils.ROLES_ACL);
        JahiaNodeService.App.getInstance().createDefaultUsersGroupACE(permissions, true, new AsyncCallback<GWTJahiaNodeACE>() {
            public void onSuccess(GWTJahiaNodeACE gwtJahiaNodeACE) {
                Log.debug("Add group ACE");
                removeAll();
                List<GWTJahiaNodeACE> aces = acl.getAce();
                if(aces == null){
                    aces = new ArrayList<GWTJahiaNodeACE>();
                }
                aces.add(gwtJahiaNodeACE);
                acl.setAce(aces);
                initAclEditor(acl);
                add(aclEditor.renderNewAclPanel());
            }

            public void onFailure(Throwable throwable) {
                Log.error("Unable to Add group ACE");
                removeAll();                
                initAclEditor(acl);
                add(aclEditor.renderNewAclPanel());
            }
        });
    }

    private void initAclEditor(GWTJahiaNodeACL acl) {
        aclEditor = new AclEditor(getPortletWizardWindow().getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition().getBaseAcl(), getPortletWizardWindow().getParentNode().getAclContext());
        aclEditor.setAclGroup(JCRClientUtils.ROLES_ACL);
        aclEditor.setAddUsersLabel(Messages.getNotEmptyResource("mw_roles_adduser", "Add rode-user permission"));
        aclEditor.setAddGroupsLabel(Messages.getNotEmptyResource("mw_roles_addgroup", "Add rode-group permission"));
        Widget saveButton = aclEditor.getSaveButton();
        saveButton.setVisible(false);

        Widget restoreButton = aclEditor.getRestoreButton();
        restoreButton.setVisible(false);
    }

}
