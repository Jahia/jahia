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
package org.jahia.services.toolbar.resolver.impl;

import static org.jahia.services.preferences.user.UserPreferencesHelper.DISPLAY_ACL_DIFF_STATE;
import static org.jahia.services.preferences.user.UserPreferencesHelper.DISPLAY_INTEGRITY_STATE;
import static org.jahia.services.preferences.user.UserPreferencesHelper.DISPLAY_TBP_STATE;
import static org.jahia.services.preferences.user.UserPreferencesHelper.ENABLE_INLINE_EDITING;

import org.jahia.data.JahiaData;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.toolbar.resolver.SelectedResolver;

/**
 * Toolbar item selection resolver for the advanced edit mode settings.
 * 
 * @author Sergiy Shyrkov
 */
public class AdvancedEditModeSettingsResolver implements SelectedResolver {

    public boolean isSelected(JahiaData data, String key) {
        boolean selected = false;
        if (DISPLAY_ACL_DIFF_STATE.equals(key)) {
            selected = UserPreferencesHelper.isDisplayAclDiffState(data
                    .getProcessingContext().getUser());
        } else if (DISPLAY_TBP_STATE.equals(key)) {
            selected = UserPreferencesHelper.isDisplayTbpState(data
                    .getProcessingContext().getUser());
        } else if (DISPLAY_INTEGRITY_STATE.equals(key)) {
            selected = UserPreferencesHelper.isDisplayIntegrityState(data
                    .getProcessingContext().getUser());
        } else if (ENABLE_INLINE_EDITING.equals(key)) {
            selected = UserPreferencesHelper.isEnableInlineEditing(data
                    .getProcessingContext().getUser());
        } else {
            throw new IllegalArgumentException("Unsupported preference key '"
                    + key + "'");
        }
        return selected;
    }

}
