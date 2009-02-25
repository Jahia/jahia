/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.node.portlet;

import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.nodes.JCRClientUtils;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;

/**
 * User: ktlili
 * Date: 2 d�c. 2008
 * Time: 17:21:22
 */
public class PortletModesCard extends MashupWizardCard {
    private AclEditor modeMappingEditor;


    public PortletModesCard() {
        super(Messages.getNotEmptyResource("mw_modes_permissions","Modes permissions"));
        setHtmlText(Messages.getNotEmptyResource("mw_modes_permissions_description","Set modes permissions"));

    }

    public void next() {
        if (modeMappingEditor != null) {
            getGwtPortletInstanceWizard().setModes(modeMappingEditor.getAcl());
        }
    }

    public void createUI() {
        removeAll();
        modeMappingEditor = new AclEditor(getPortletWizardWindow().getGwtPortletInstanceWizard().getGwtJahiaPortletDefinition().getBaseAcl(), false);
        modeMappingEditor.setAclGroup(JCRClientUtils.MODES_ACL);
        modeMappingEditor.setAddUsersLabel(Messages.getNotEmptyResource("mw_modes_adduser","Add mode-user permission"));
        modeMappingEditor.setAddGroupsLabel(Messages.getNotEmptyResource("mw_modes_addgroup","Add mode-group permission"));        
        TextToolItem saveButton = modeMappingEditor.getSaveButton();
        saveButton.setVisible(false);


        TextToolItem restoreButton = modeMappingEditor.getRestoreButton();
        restoreButton.setVisible(false);
        add(modeMappingEditor.renderNewAclPanel());

    }

}
