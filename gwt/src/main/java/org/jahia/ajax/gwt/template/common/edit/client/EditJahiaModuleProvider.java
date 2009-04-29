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
package org.jahia.ajax.gwt.template.common.edit.client;

import org.jahia.ajax.gwt.client.core.JahiaModule;
import org.jahia.ajax.gwt.client.core.JahiaModuleProvider;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.module.*;


/**
 * User: jahia
 * Date: 17 mars 2008
 * Time: 12:21:09
 */
public class EditJahiaModuleProvider extends JahiaModuleProvider {
    public JahiaModule getJahiaModuleByJahiaType(String jahiaType) {
        if (jahiaType != null) {
            if (jahiaType.equalsIgnoreCase(JahiaType.ACTION_MENU)) {
                return new ActionMenuJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.TOOLBARS_MANAGER)) {
                return new ToolbarJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.DATE_FIELD)) {
                return new DateFieldJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.CALENDAR)) {
                return new CalendarJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.RSS)) {
                return new RSSJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.MY_SETTINGS)) {
                return new MySettingsJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.SUBSCRIPTION)) {
                return new SubscriptionJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.FORM)) {
                return new FormJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.PORTLET_RENDER)) {
                return new PortletRenderJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.INLINE_EDITING)) {
                return new InlineEditingJahiaModule();
            } else if (jahiaType.equalsIgnoreCase(JahiaType.USER_GROUP)) {
                return new UserGroupJahiaModule();
            }
        }
        return null;
    }
}
