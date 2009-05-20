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
 package org.jahia.security.license;

import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaException;
import org.jahia.resourcebundle.ResourceMessage;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class SiteCountValidator extends AbstractValidator {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(SiteCountValidator.class);

    public SiteCountValidator(String name, String value, License license) {
        super(name, value, license);
    }

    public boolean assertEquals(String value) {

        int maxSites = Integer.parseInt(value);

        // Check if the number of users is not exceeding the fixed limit
        try {
            int nbItems = ServicesRegistry.getInstance().
                                           getJahiaSitesService().
                                           getNbSites ();

            if (nbItems > maxSites) {
                errorMessage = new ResourceMessage("org.jahia.security.license.SiteCountValidator.invalidSiteCount.label", new Integer(nbItems), new Integer(maxSites));
                return false;
            }
        }
        catch (JahiaException ex) {
            logger.error("Error while checking site limit", ex);
            errorMessage = new ResourceMessage("org.jahia.security.license.SiteCountValidator.errorInSiteCountCheck.label");
        }
        return true;
    }
    public boolean assertInRange(String fromValue, String toValue) {
        int minSites = Integer.parseInt(fromValue);
        int maxSites = Integer.parseInt(toValue);

        // Check if the number of users is not exceeding the fixed limit
        try {
            int nbItems = ServicesRegistry.getInstance().
                                           getJahiaSitesService().
                                           getNbSites ();

            if ((nbItems > maxSites) || (nbItems < minSites)) {
                errorMessage = new ResourceMessage("org.jahia.security.license.SiteCountValidator.siteCountNotInRange.label", new Integer(nbItems), new Integer(minSites), new Integer(maxSites));
                return false;
            }
        }
        catch (JahiaException ex) {
            logger.error("Error while checking site limit", ex);
            errorMessage = new ResourceMessage("org.jahia.security.license.SiteCountValidator.errorInSiteCountCheck.label");
        }
        return true;
    }

}