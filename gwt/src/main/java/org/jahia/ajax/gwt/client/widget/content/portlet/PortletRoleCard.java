/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.content.portlet;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.ui.Label;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: ktlili
 * Date: 25 nov. 2008
 * Time: 10:28:09
 */
public class PortletRoleCard extends PortletWizardCard {
    private AclEditor aclEditor;

    public PortletRoleCard() {
        super(Messages.get("org.jahia.engines.PortletsManager.wizard.rolesperm.label", "Roles permissions"), Messages.get("org.jahia.engines.PortletsManager.wizard.rolesperm.description.label", "Set roles permissions"));
    }

    public void next() {
        if (aclEditor != null) {
            getGwtJahiaNewPortletInstance().setRoles(aclEditor.getAcl());
        }
    }

    public void createUI() {
        // update
        super.createUI();        
        final GWTJahiaNodeACL acl = getPortletWizardWindow().getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition().getBaseAcl().cloneObject();
        acl.setBreakAllInheritance(true);
        List<String> permissions = acl.getAvailableRoles().get(JCRClientUtils.PORTLET_ROLES);
        if (permissions != null && permissions.size() > 0) {
            JahiaContentManagementService.App.getInstance().createDefaultUsersGroupACE(permissions, true, new BaseAsyncCallback<GWTJahiaNodeACE>() {
                public void onSuccess(GWTJahiaNodeACE gwtJahiaNodeACE) {
                    Log.debug("Add group ACE");
                    removeAll();
                    List<GWTJahiaNodeACE> aces = new ArrayList<GWTJahiaNodeACE>();
                    aces.add(gwtJahiaNodeACE);
                    acl.setAce(aces);
                    initAclEditor(acl);
                    aclEditor.addNewAclPanel(PortletRoleCard.this);
                    getPortletWizardWindow().updateWizard();
                }

                public void onApplicationFailure(Throwable throwable) {
                    Log.error("Unable to Add group ACE");
                    removeAll();
                    initAclEditor(acl);
                    aclEditor.addNewAclPanel(PortletRoleCard.this);
                }
            });
        } else {
            add(new Label(Messages.get("org.jahia.engines.PortletsManager.wizard.roles.any", "The selected portlets defines any roles.")));
        }
    }

    private void initAclEditor(GWTJahiaNodeACL acl) {
        aclEditor = new AclEditor(acl, getPortletWizardWindow().getParentNode().getSiteKey(), null, Collections.singleton(JCRClientUtils.PORTLET_ROLES), null);
        aclEditor.setAddUsersLabel(Messages.get("org.jahia.engines.PortletsManager.wizard.roles.adduser.label", "Add rode-user permission"));
        aclEditor.setAddGroupsLabel(Messages.get("org.jahia.engines.PortletsManager.wizard.roles.addgroup.label", "Add rode-group permission"));
    }

    @Override
    public void refreshLayout() {
        layout();
    }
}