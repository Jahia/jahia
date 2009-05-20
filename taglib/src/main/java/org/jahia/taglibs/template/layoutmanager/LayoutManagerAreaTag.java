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
package org.jahia.taglibs.template.layoutmanager;

import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.params.ProcessingContext;
import org.jahia.data.JahiaData;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 21 nov. 2007
 * Time: 11:43:22
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class LayoutManagerAreaTag extends AbstractJahiaTag {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LayoutManagerAreaTag.class);
    private String width;

    public int doStartTag() {
        final JspWriter out = pageContext.getOut();
        try {
            //define area
            out.print("<div id='layoutManager' " + JahiaType.JAHIA_TYPE + "=\"" + JahiaType.LAYOUT_MANAGER + "\"");
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
            addGwtDictionaryMessage("mw_roles_description", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.rolesperm.description.label"));
            addGwtDictionaryMessage("mw_finish", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.finish.label"));
            addGwtDictionaryMessage("mw_save_as", getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.saveas.label"));
            addGwtDictionaryMessage("mw_name",getJahiaInternalResourceValue("org.jahia.admin.components.ManageComponents.applicationName.label"));
            addGwtDictionaryMessage("mw_description",getJahiaInternalResourceValue("org.jahia.admin.components.ManageComponents.applicationDesc.label"));
            addGwtDictionaryMessage("mw_roles_perm_desc",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.saveas.label"));
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
            addGwtDictionaryMessage("p_mashup_create", getJahiaInternalResourceValue("org.jahia.engines.filemanager.Filemanager_Engine.newMashup.label"));
            addGwtDictionaryMessage("p_my_portal", getJahiaInternalResourceValue("org.jahia.myportal.myPortal"));
            addGwtDictionaryMessage("p_mashup_added_myPortal", getJahiaInternalResourceValue("org.jahia.myportal.mashupAdded"));



        } catch (IOException e) {
            logger.error(e, e);
        }

        return EVAL_BODY_INCLUDE;
    }


    public int doEndTag() {
        final JspWriter out = pageContext.getOut();
        try {
            // end box declaration
            out.print("\n</div>\n");
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
}
