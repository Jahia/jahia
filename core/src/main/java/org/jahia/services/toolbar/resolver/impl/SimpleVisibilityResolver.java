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
