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
            addGwtDictionaryMessage("mw_mashups",getJahiaInternalResourceValue("org.jahia.engines.MashupsManager.mashups.label"));
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
