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
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import com.google.gwt.user.client.ui.Label;
import com.allen_sauer.gwt.log.client.Log;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * User: ktlili
 * Date: 2 d�c. 2008
 * Time: 17:21:22
 */
public class PortletModesCard extends PortletWizardCard {
    private AclEditor modeMappingEditor;


    public PortletModesCard() {
        super(Messages.get("org.jahia.engines.PortletsManager.wizard.modesperm.label", "Modes permissions"), Messages.get("org.jahia.engines.PortletsManager.wizard.modesperm.description.label", "Set modes permissions"));
    }

    public void next() {
        if (modeMappingEditor != null) {
            getGwtJahiaNewPortletInstance().setModes(modeMappingEditor.getAcl());
        }
    }

    public void createUI() {
        removeAll();
        super.createUI();
        // update
        final GWTJahiaNodeACL acl = getPortletWizardWindow().getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition().getBaseAcl().cloneObject();
        List<String> permissions = acl.getAvailablePermissions().get(JCRClientUtils.PORTLET_MODES_ROLES);
        acl.setBreakAllInheritance(true);
        if (permissions != null && permissions.size() > 0) {
            JahiaContentManagementService.App.getInstance().createDefaultUsersGroupACE(permissions, true, new BaseAsyncCallback<GWTJahiaNodeACE>() {
                public void onSuccess(GWTJahiaNodeACE gwtJahiaNodeACE) {
                    Log.debug("Add group ACE");
                    removeAll();
                    List<GWTJahiaNodeACE> aces = new ArrayList<GWTJahiaNodeACE>();
                    aces.add(gwtJahiaNodeACE);
                    acl.setAce(aces);
                    initModeMappingEditor(acl);
                    modeMappingEditor.addNewAclPanel(PortletModesCard.this);
                    getPortletWizardWindow().updateWizard();
                }

                public void onApplicationFailure(Throwable throwable) {
                    Log.error("Unable to Add group ACE");
                    removeAll();
                    initModeMappingEditor(acl);
                    modeMappingEditor.addNewAclPanel(PortletModesCard.this);
                }
            });
        } else {
            add(new Label(Messages.get("org.jahia.engines.PortletsManager.wizard.modesperm.onlyViewMode", "The selected portlets contains only view mode.")));
        }
    }

    private void initModeMappingEditor(GWTJahiaNodeACL acl) {
        modeMappingEditor = new AclEditor(acl, getPortletWizardWindow().getParentNode().getAclContext(), null, Collections.singleton(JCRClientUtils.PORTLET_MODES_ROLES));
        modeMappingEditor.setAddUsersLabel(Messages.get("org.jahia.engines.PortletsManager.wizard.modes.adduser.label", "Add mode-user permission"));
        modeMappingEditor.setAddGroupsLabel(Messages.get("org.jahia.engines.PortletsManager.wizard.modes.addgroup.label", "Add mode-group permission"));
    }

    @Override
    public void refreshLayout() {
        // do nothing
    }
}