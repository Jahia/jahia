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
 package org.jahia.security.license;

import java.util.Iterator;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
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

public class PageCountValidator extends AbstractValidator {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(PageCountValidator.class);

    public PageCountValidator(String name, String value, License license) {
        super(name, value, license);
    }

    public boolean assertEquals(String value) {

        int maxPages = Integer.parseInt(value);

        // Check if the number of pages is not exceeding the fixed limit
        try {

            // get all the list of site
            Iterator enumeration = ServicesRegistry.getInstance()
                                                .getJahiaSitesService()
                                                .getSites();
            JahiaSite aSite = null;
            int nbItems = 0;
            while( enumeration.hasNext() ){
                aSite = (JahiaSite)enumeration.next();

                nbItems = ServicesRegistry.getInstance().
                                           getJahiaPageService().
                                           getRealActiveNbPages (aSite.getID());

                if (nbItems > maxPages) {
                    errorMessage = new ResourceMessage("org.jahia.security.license.PageCountValidator.invalidPageCount.label", new Integer(nbItems), new Integer(maxPages), new Integer(aSite.getID()));
                    return false;
                }
            }

        } catch (JahiaException ex) {
            logger.error("Error in page limit check", ex);
            errorMessage = new ResourceMessage("org.jahia.security.license.PageCountValidator.errorInPageCountCheck.label");
            return false;
        }

        return true;
    }

    public boolean assertInRange(String fromValue, String toValue) {

        int minPages = Integer.parseInt(fromValue);
        int maxPages = Integer.parseInt(toValue);

        // Check if the number of pages is not exceeding the fixed limit
        try {

            // get all the list of site
            Iterator enumeration = ServicesRegistry.getInstance()
                                                .getJahiaSitesService()
                                                .getSites();
            JahiaSite aSite = null;
            int nbItems = 0;
            while( enumeration.hasNext() ){
                aSite = (JahiaSite)enumeration.next();

                nbItems = ServicesRegistry.getInstance().
                                           getJahiaPageService().
                                           getRealActiveNbPages (aSite.getID());

                if ((nbItems > maxPages) || (nbItems < minPages)) {
                    errorMessage = new ResourceMessage("org.jahia.security.license.PageCountValidator.pageCountNotInRange.label", new Integer(nbItems), new Integer(minPages), new Integer(maxPages), new Integer(aSite.getID()));
                    return false;
                }
            }

        } catch (JahiaException ex) {
            logger.error("Error in page limit check", ex);
            errorMessage = new ResourceMessage("org.jahia.security.license.PageCountValidator.errorInPageCountCheck.label");
            return false;
        }
        return true;
    }

}