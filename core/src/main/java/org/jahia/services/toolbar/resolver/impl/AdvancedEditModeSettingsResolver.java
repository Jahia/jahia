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
