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
