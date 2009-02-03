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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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