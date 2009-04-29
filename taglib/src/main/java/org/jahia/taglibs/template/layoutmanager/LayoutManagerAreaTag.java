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
            addGwtDictionaryMessage("mw_mashups",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.portletdef.label"));
            addGwtDictionaryMessage("mw_select_portlet_def",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.portletdef.label"));
            addGwtDictionaryMessage("mw_ok",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.ok.label"));
            addGwtDictionaryMessage("mw_params",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.parameters.label"));
            addGwtDictionaryMessage("mw_edit_params",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.parameters.edit.label"));
            addGwtDictionaryMessage("mw_prop_load_error",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.props.load.error.label"));
            addGwtDictionaryMessage("mw_modes_permissions",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modesperm.label"));
            addGwtDictionaryMessage("mw_modes_permissions_description",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modesperm.description.label"));
            addGwtDictionaryMessage("mw_modes_adduser",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modes.adduser.label"));
            addGwtDictionaryMessage("mw_modes_addgroup",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.modes.addgroup.label"));
            addGwtDictionaryMessage("mw_roles_adduser",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.roles.adduser.label"));
            addGwtDictionaryMessage("mw_roles_addgroup",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.roles.addgroup.label"));
            addGwtDictionaryMessage("mw_roles_perm",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.rolesperm.label"));
            addGwtDictionaryMessage("mw_roles_description",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.rolesperm.description.label"));
            addGwtDictionaryMessage("mw_finish",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.finish.label"));
            addGwtDictionaryMessage("mw_save_as",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.wizard.saveas.label"));
            addGwtDictionaryMessage("ae_principal",getJahiaInternalResourceValue("org.jahia.engines.rights.ManageRights.principal.label"));
            

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

    private String getJahiaInternalResourceValue(String key) {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

        Locale currentLocale = request.getLocale();
        HttpSession session = pageContext.getSession();
        if (session != null) {
            if (session.getAttribute(ProcessingContext.SESSION_LOCALE) != null) {
                currentLocale = (Locale) session.getAttribute(ProcessingContext.
                        SESSION_LOCALE);
            }
        }

        String resValue = null;

        try {

            if (jData != null) {
                resValue = JahiaResourceBundle.getJahiaInternalResource(key, jData.getProcessingContext().getLocale());
            } else {
                // for any reason the jData wasn't loaded correctly
                resValue = JahiaResourceBundle.getJahiaInternalResource(key, currentLocale);
            }
        } catch (MissingResourceException mre) {
            logger.error(mre.toString(), mre);
        }
        if (resValue == null) {
            resValue = key;
        }
        return resValue;
    }
}
