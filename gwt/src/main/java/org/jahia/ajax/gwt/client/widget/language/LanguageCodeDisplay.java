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
package org.jahia.ajax.gwt.client.widget.language;

import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.data.GWTLanguageSwitcherLocaleBean;

/**
 * User: romain
 * Date: 15 avr. 2009
 * Time: 10:28:47
 */
public class LanguageCodeDisplay {

    public static void formatToolItem(TextToolItem item, GWTLanguageSwitcherLocaleBean localeBean, boolean displayFlag, String workflowState, boolean displayWorkflow) {
        StringBuilder content = new StringBuilder(localeBean.getDisplayName()) ;
        if (displayWorkflow) {
            if (workflowState == null || workflowState.equals("")) {
                content.append("&nbsp;&nbsp;<span class='workflow-000'>nbsp;&nbsp;&nbsp;&nbsp;</span>") ;
            } else {
                content.append("&nbsp;&nbsp;<span class='workflow-").append(workflowState).append("'>&nbsp;&nbsp;&nbsp;&nbsp;</span>") ;
            }
        }
        item.setText(content.toString());
        if (displayFlag && !"".equals(localeBean.getCountryIsoCode())) {
            item.setIconStyle("flag_" + localeBean.getCountryIsoCode()) ;
        } else {
            item.setIconStyle("flag_" + localeBean.getLanguage()) ;
        }
    }

    public static void formatMenuItem(MenuItem item, GWTLanguageSwitcherLocaleBean localeBean, boolean displayFlag, String workflowState, boolean displayWorkflow) {
        StringBuilder content = new StringBuilder(localeBean.getDisplayName()) ;
        if (displayWorkflow) {
            if (workflowState == null || workflowState.equals("")) {
                content.append("&nbsp;&nbsp;<span class='workflow-000'>&nbsp;&nbsp;&nbsp;&nbsp;</span>") ;
            } else {
                content.append("&nbsp;&nbsp;<span class='workflow-").append(workflowState).append("'>&nbsp;&nbsp;&nbsp;&nbsp;</span>") ;
            }
        }
        item.setText(content.toString());
        if (displayFlag && !"".equals(localeBean.getCountryIsoCode())) {
            item.setIconStyle("flag_" + localeBean.getCountryIsoCode()) ;
        } else {
            item.setIconStyle("flag_" + localeBean.getLanguage()) ;
        }
    }

}
