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
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.preferences.user;

import java.security.Principal;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.settings.SettingsBean;

/**
 * Helper class for accessing user preferences.
 * 
 * @author Sergiy Shyrkov
 */
public final class UserPreferencesHelper {

    private static final String DISPLAY_ACL_DIFF_STATE = "acldiff.activated";

    private static final String DISPLAY_INTEGRITY_STATE = "integrity.activated";

    private static final String DISPLAY_TBP_STATE = "timebasepublishing.activated";

    private static final String ENABLE_INLINE_EDITING = "inlineediting.activated";

    private static JahiaPreferencesService getPrefsService() {
        return ServicesRegistry.getInstance().getJahiaPreferencesService();
    }

    public static boolean isDisplayAclDiffState(Principal principal) {
        return getPrefsService().getGenericPreferenceBooleanValue(
                DISPLAY_ACL_DIFF_STATE, SettingsBean.getInstance().isAclDisp(),
                principal);
    }

    public static boolean isDisplayIntegrityState(Principal principal) {
        return getPrefsService().getGenericPreferenceBooleanValue(
                DISPLAY_INTEGRITY_STATE,
                SettingsBean.getInstance().isIntegrityDisp(), principal);
    }

    public static boolean isDisplayTbpState(Principal principal) {
        return getPrefsService().getGenericPreferenceBooleanValue(
                DISPLAY_TBP_STATE, SettingsBean.getInstance().isTbpDisp(),
                principal);
    }

    public static boolean isDisplayWorkflowState(Principal principal) {
        return SettingsBean.getInstance().isWflowDisp();
    }

    public static boolean isEnableInlineEditing(Principal principal) {
        return getPrefsService().getGenericPreferenceBooleanValue(
                ENABLE_INLINE_EDITING,
                SettingsBean.getInstance().isInlineEditingActivated(),
                principal);
    }

    /**
     * Initializes an instance of this class.
     */
    private UserPreferencesHelper() {
        super();
    }
}
