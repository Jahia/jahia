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
package org.jahia.ajax.gwt.utils;

import org.jahia.ajax.gwt.client.data.GWTJahiaAdvCompareModeSettings;
import org.jahia.ajax.gwt.client.data.GWTJahiaAdvPreviewSettings;
import org.jahia.ajax.gwt.client.data.GWTJahiaContext;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.params.AdvCompareModeSettings;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 ao�t 2008
 * Time: 16:02:03
 * To change this template use File | Settings | File Templates.
 */
public class JahiaGWTUtils {

    public static GWTJahiaContext jahiaContextToGWTJahiaContext(ProcessingContext context){
        GWTJahiaContext gwtContext = new GWTJahiaContext();
        if (context != null){
            AdvPreviewSettings advPreviewSettings = (AdvPreviewSettings)context.getSessionState()
                    .getAttribute(ProcessingContext.SESSION_ADV_PREVIEW_SETTINGS);
            if (advPreviewSettings != null){
                gwtContext.setAdvPreviewSettings(jahiaAdvPreviewSettingsToGWTBean(advPreviewSettings));
            }
            AdvCompareModeSettings advCompareModeSettings = (AdvCompareModeSettings)context.getSessionState()
                    .getAttribute(ProcessingContext.SESSION_ADV_COMPARE_MODE_SETTINGS);
            if (advCompareModeSettings != null){
                gwtContext.setAdvCompareModeSettings(jahiaAdvCompareModeSettingsToGWTBean(advCompareModeSettings));
            }
        }
        return gwtContext;
    }

    public static GWTJahiaUser jahiaUserToGWTBean(JahiaUser user){
        if (user==null){
            return null;
        }
        return new GWTJahiaUser(user.getUsername(),user.getUserKey());
    }

    public static GWTJahiaAdvPreviewSettings jahiaAdvPreviewSettingsToGWTBean(AdvPreviewSettings settings){
        if (settings==null){
            return null;
        }
        GWTJahiaAdvPreviewSettings gwtAdvPreviewSettings = new GWTJahiaAdvPreviewSettings();
        gwtAdvPreviewSettings.setMainUser(jahiaUserToGWTBean(settings.getMainUser()));
        gwtAdvPreviewSettings.setAliasedUser(jahiaUserToGWTBean(settings.getAliasedUser()));
        gwtAdvPreviewSettings.setEnabled(settings.isEnabled());
        gwtAdvPreviewSettings.setPreviewDate(settings.getPreviewDate());
        return gwtAdvPreviewSettings;
    }

    public static GWTJahiaAdvCompareModeSettings jahiaAdvCompareModeSettingsToGWTBean(AdvCompareModeSettings settings){
        if (settings==null){
            return null;
        }
        GWTJahiaAdvCompareModeSettings gwtAdvCompareModeSettings = new GWTJahiaAdvCompareModeSettings();
        gwtAdvCompareModeSettings.setEnabled(settings.isEnabled());
        gwtAdvCompareModeSettings.setRevision1(settings.getGWTRevision1());
        gwtAdvCompareModeSettings.setRevision2(settings.getGWTRevision2());
        return gwtAdvCompareModeSettings;
    }

    /**
     * Add "*" at beginning and end of query if not present in original search string.
     * Ex: *query   -->   *query
     *     query*   -->   query*
     *     query    -->   *query*
     *
     * @param rawQuery the raw query string
     * @return formatted query string
     */
    public static String formatQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.length() == 0) {
            return "" ;
        } else if (rawQuery.startsWith("*") || rawQuery.endsWith("*")) {
            return rawQuery ;
        } else {
            return new StringBuilder("*").append(rawQuery).append("*").toString() ;
        }
    }
    
}
