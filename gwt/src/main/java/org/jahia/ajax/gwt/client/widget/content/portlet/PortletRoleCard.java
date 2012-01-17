/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.content.portlet;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import com.google.gwt.user.client.ui.Label;
import com.allen_sauer.gwt.log.client.Log;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

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
        List<String> permissions = acl.getAvailablePermissions().get(JCRClientUtils.PORTLET_ROLES);
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
        aclEditor = new AclEditor(acl, getPortletWizardWindow().getParentNode().getAclContext(), null, Collections.singleton(JCRClientUtils.PORTLET_ROLES));
        aclEditor.setAddUsersLabel(Messages.get("org.jahia.engines.PortletsManager.wizard.roles.adduser.label", "Add rode-user permission"));
        aclEditor.setAddGroupsLabel(Messages.get("org.jahia.engines.PortletsManager.wizard.roles.addgroup.label", "Add rode-group permission"));
    }

    @Override
    public void refreshLayout() {
        layout();
    }
}