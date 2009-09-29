/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.template.layoutmanager;

import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspWriter;
import javax.jcr.Node;
import java.io.IOException;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 21 nov. 2007
 * Time: 11:43:22
 */
@SuppressWarnings("serial")
public class LayoutManagerAreaTag extends AbstractJahiaTag {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LayoutManagerAreaTag.class);
    private String width;
    private Node node;

    public int doStartTag() {
        final JspWriter out = pageContext.getOut();
        try {
            //define area
            out.print("<div uuid='"+node.getUUID()+"' id='layoutManager' " + JahiaType.JAHIA_TYPE + "=\"" + JahiaType.LAYOUT_MANAGER + "\"");
            if (width != null) {
                out.print(" jahia-layoutmanager-width='" + width + "'");
            }
            out.print("></div>\n");

            // begin box declaration
            out.print("<div id='layout'  style='display:none;'>\n");

            // add resouces bundels
            addGwtDictionaryMessage("ae_principal", getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.principal.label"));
            addGwtDictionaryMessage("mw_mashups", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.portletdef.label"));
            addGwtDictionaryMessage("mw_select_portlet_def", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.portletdef.label"));
            addGwtDictionaryMessage("mw_ok", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.ok.label"));
            addGwtDictionaryMessage("mw_params", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.parameters.label"));
            addGwtDictionaryMessage("mw_edit_params", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.parameters.edit.label"));
            addGwtDictionaryMessage("mw_prop_load_error", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.props.load.error.label"));
            addGwtDictionaryMessage("mw_modes_permissions", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modesperm.label"));
            addGwtDictionaryMessage("mw_modes_permissions_description", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modesperm.description.label"));
            addGwtDictionaryMessage("mw_modes_adduser", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modes.adduser.label"));
            addGwtDictionaryMessage("mw_modes_addgroup", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modes.addgroup.label"));
            addGwtDictionaryMessage("mw_roles_adduser", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.roles.adduser.label"));
            addGwtDictionaryMessage("mw_roles_addgroup", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.roles.addgroup.label"));
            addGwtDictionaryMessage("mw_roles_perm", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.rolesperm.label"));
            addGwtDictionaryMessage("mw_roles_perm_desc", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.rolesperm.description.label"));
            addGwtDictionaryMessage("mw_finish", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.finish.label"));
            addGwtDictionaryMessage("mw_save_as", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.saveas.label"));
            addGwtDictionaryMessage("mw_name",getJahiaInternalResourceValue("org.jahia.admin.components.ManageComponents.applicationName.label"));
            addGwtDictionaryMessage("mw_description",getJahiaInternalResourceValue("org.jahia.admin.components.ManageComponents.applicationDesc.label"));
            addGwtDictionaryMessage("mw_finish_description",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.saveas.label"));
            addGwtDictionaryMessage("mw_no_role",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.roles.any"));
            addGwtDictionaryMessage("p_add_mashups", getJahiaInternalResourceValue("org.jahia.myportal.addMashup"));
            addGwtDictionaryMessage("p_save_default", getJahiaInternalResourceValue("org.jahia.myportal.saveAsDefault"));
            addGwtDictionaryMessage("p_confirm_save_default", getJahiaInternalResourceValue("org.jahia.myportal.saveAsDefault.confirm"));
            addGwtDictionaryMessage("p_restore_default", getJahiaInternalResourceValue("org.jahia.myportal.restoreDefault"));
            addGwtDictionaryMessage("p_restore_confirm", getJahiaInternalResourceValue("org.jahia.myportal.restoreDefault.confirm"));
            addGwtDictionaryMessage("p_my_config", getJahiaInternalResourceValue("org.jahia.myportal.myconfig"));
            addGwtDictionaryMessage("p_config_title", getJahiaInternalResourceValue("org.jahia.myportal.myConfig.title"));
            addGwtDictionaryMessage("p_number_columns", getJahiaInternalResourceValue("org.jahia.myportal.myConfig.numberColumns"));
            addGwtDictionaryMessage("p_portal_editable_live_mode", getJahiaInternalResourceValue("org.jahia.myportal.myConfig.editableLiveMode"));
            addGwtDictionaryMessage("p_add_mashup_live_mode", getJahiaInternalResourceValue("org.jahia.myportal.myConfig.addMashupLiveMode"));
            addGwtDictionaryMessage("p_save", getJahiaInternalResourceValue("org.jahia.myportal.myConfig.save"));
            addGwtDictionaryMessage("p_mashup_create", getJahiaInternalResourceValue("toolbar.manager.button.newMashup"));
            addGwtDictionaryMessage("p_my_portal", getJahiaInternalResourceValue("org.jahia.myportal.myPortal"));
            addGwtDictionaryMessage("p_mashup_added_myPortal", getJahiaInternalResourceValue("org.jahia.myportal.mashupAdded"));
            addGwtDictionaryMessage("p_mashup_added", getJahiaInternalResourceValue("org.jahia.myportal.mashupAddedLabel"));
            addGwtDictionaryMessage("p_add_mashup_to_portal", getJahiaInternalResourceValue("org.jahia.myportal.addToPortal"));
            addGwtDictionaryMessage("p_delete_confirm", getJahiaInternalResourceValue("org.jahia.myportal.delete.confirm"));

            addGwtDictionaryMessage("wizard_button_cancel", getJahiaInternalResourceValue("org.jahia.engines.wizard.button.cancel"));
            addGwtDictionaryMessage("wizard_button_finish", getJahiaInternalResourceValue("org.jahia.engines.wizard.button.finish"));
            addGwtDictionaryMessage("wizard_button_next", getJahiaInternalResourceValue("org.jahia.engines.wizard.button.next"));
            addGwtDictionaryMessage("wizard_button_prev", getJahiaInternalResourceValue("org.jahia.engines.wizard.button.prev"));
            addGwtDictionaryMessage("wizard_steps_of", getJahiaInternalResourceValue("org.jahia.engines.wizard.button.steps.of"));
            addGwtDictionaryMessage("wizard_steps_current", getJahiaInternalResourceValue("org.jahia.engines.wizard.button.steps.current"));
            addGwtDictionaryMessage("wizard_header_title", getJahiaInternalResourceValue("org.jahia.engines.wizard.title"));

            addGwtDictionaryMessage("mw_title", getJahiaInternalResourceValue("toolbar.manager.button.newMashup"));
        } catch (Exception e) {
            logger.error(e, e);
        }

        return EVAL_BODY_INCLUDE;
    }


    public int doEndTag() {
        final JspWriter out = pageContext.getOut();
        try {
            // end box declaration
            out.print("\n</div>\n");
            this.width = null;
            this.node = null;
        } catch (IOException e) {
            logger.error(e, e);
        }

        return SKIP_BODY;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
