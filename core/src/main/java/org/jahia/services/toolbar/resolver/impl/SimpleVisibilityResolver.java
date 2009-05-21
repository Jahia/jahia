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

import org.jahia.services.toolbar.resolver.VisibilityResolver;
import org.jahia.services.sites.JahiaSite;
import org.jahia.data.JahiaData;

import java.util.Iterator;

/**
 * User: ktlili
 * Date: 7 janv. 2009
 * Time: 10:10:51
 */
public class SimpleVisibilityResolver implements VisibilityResolver {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SimpleVisibilityResolver.class);

    private static final String STATISTICS = "statistics";
    private static final String PAGE_STATS = "pageStats";

    public boolean isVisible(JahiaData jData, String type) {
        if (type == null) {
            logger.debug("type is not defined");
            return false;
        }

        if (type.equalsIgnoreCase(STATISTICS)) {
            return googleAnalyticsTrackingActivated(jData);

        }
        if (type.equalsIgnoreCase(PAGE_STATS)) {
            return customizedTrackingModeIsEnabled(jData);
        }
        return false;
    }

    private boolean customizedTrackingModeIsEnabled(JahiaData jData) { 
        JahiaSite currentSite = jData.getProcessingContext().getSite();
         Iterator it = ((currentSite.getSettings()).keySet()).iterator();
                    // check if at list one profile is enabled
                    boolean oneVirtualEnabled = false;
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        if (key.startsWith("jahiaGAprofile")) {
                            if (/*Boolean.valueOf(currentSite.getSettings().getProperty(currentSite.getSettings().getProperty(key) + "_" + currentSite.getID() + "_trackingEnabled"))
                                    &&*/currentSite.getSettings().getProperty(currentSite.getSettings().getProperty(key) + "_" + currentSite.getSiteKey() + "_trackedUrls").equals("virtual")) {
                                oneVirtualEnabled = true;
                                break;
                            }
                        }
                    }


        // one virtual is configured even if it's not enabled
        return oneVirtualEnabled;//!(currentSite.getSettings().getProperty("gaUserAccountCustom").equals(""))&&!(currentSite.getSettings().getProperty("gaProfileCustom").equals(""));
    }

    private boolean googleAnalyticsTrackingActivated(JahiaData jData) {    // todo should be adapted to the new version
        //logger.info("check if the site is tracked");
        JahiaSite currentSite = jData.getProcessingContext().getSite();

        Iterator it = ((currentSite.getSettings()).keySet()).iterator();
                    // check if at list one profile is enabled
                    boolean oneEnabled = false;
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        if (key.startsWith("jahiaGAprofile")) {
                            //if (Boolean.valueOf(currentSite.getSettings().getProperty(currentSite.getSettings().getProperty(key) + "_" + currentSite.getID() + "_trackingEnabled"))) {
                                oneEnabled = true;
                                break;
                            //}
                        }
                    }


       /* String customAccount = currentSite.getSettings().getProperty("gaUserAccountCustom");
        String defaultAccount = currentSite.getSettings().getProperty("gaUserAccountDefault");
        Boolean trackingEnabled = Boolean.valueOf(currentSite.getSettings().getProperty("trackingEnabled"));

        Boolean wellConfigured = trackingEnabled && ((customAccount.startsWith("UA")) || (defaultAccount.startsWith("UA")));*/
        //logger.info("result "+wellConfigured);
        return oneEnabled;
    }
}
