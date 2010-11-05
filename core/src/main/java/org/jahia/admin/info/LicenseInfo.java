/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

// $Id$
//
//  LicenseInfo
//
//  09.08.2001  MJ  added in jahia.
//

package org.jahia.admin.info;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.settings.SettingsBean;
import org.jahia.admin.AbstractAdministrationModule;


/**
 * desc: This class provides the business methods for license
 * info display, from the JahiaAdministration servlet.
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Mikhael Janson
 * @version 1.0
 */
public class LicenseInfo extends AbstractAdministrationModule {
    private static org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(LicenseInfo.class);

    private static final String     CLASS_NAME              =  JahiaAdministration.CLASS_NAME;
    private static final String     JSP_PATH                =  JahiaAdministration.JSP_PATH;

    /**
    * Default constructor.
    *
    * @param   request       Servlet request.
    * @param   response      Servlet response.
    */
    public void service( HttpServletRequest    request,
                               HttpServletResponse   response )

    throws Exception
    {
      JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
      ProcessingContext jParams = null;
      if (jData != null) {
          jParams = jData.getProcessingContext();
      }

        displayLicenseInfo( request, response, request.getSession() );
    } // end constructor


    /**
     * Display installed license information, using doRedirect().
     *
     * @param   req       the HttpServletRequest object
     * @param   res       the HttpServletResponse object
     * @param   sess      the HttpSession object
     */
    private void displayLicenseInfo( HttpServletRequest   req,
                                     HttpServletResponse  res,
                                     HttpSession          sess )
    throws IOException, ServletException
    {

      JahiaData jData = (JahiaData) req.getAttribute("org.jahia.data.JahiaData");
      ProcessingContext jParams = null;
      if (jData != null) {
          jParams = jData.getProcessingContext();
      }
        ServicesRegistry       sReg  =  ServicesRegistry.getInstance();

        if (sReg != null) {

            try {
                String unlimited = getMessage("org.jahia.admin.status.ManageStatus.unlimited.label");
                int nbCurrentSites     = sReg.getJahiaSitesService().getNbSites();
                int nbMaxSites         = -1;
                int nbCurrentUsers     = sReg.getJahiaUserManagerService().getNbUsers();
                int nbMaxUsers         = -1;
                int nbCurrentTemplates = 0;
                int nbMaxTemplates     = -1;
                int nbCurrentPages     = 0;
                int nbMaxPages         = -1;

                String maxSites     = (nbMaxSites == -1)     ? unlimited : Integer.toString(nbMaxSites);
                String maxUsers     = (nbMaxUsers == -1)     ? unlimited : Integer.toString(nbMaxUsers);
                String maxTemplates = (nbMaxTemplates == -1) ? unlimited : Integer.toString(nbMaxTemplates);
                String maxPages     = (nbMaxPages == -1)     ? unlimited : Integer.toString(nbMaxPages);

                req.setAttribute("allowedDays", Integer.valueOf(0));

                req.setAttribute("nbCurrentSites",     Integer.toString(nbCurrentSites)      );
                req.setAttribute("nbMaxSites",         maxSites                              );
                req.setAttribute("nbCurrentUsers",     Integer.toString(nbCurrentUsers)      );
                req.setAttribute("nbMaxUsers",         maxUsers                              );
                req.setAttribute("nbCurrentTemplates", Integer.toString(nbCurrentTemplates)  );
                req.setAttribute("nbMaxTemplates",     maxTemplates                          );
                req.setAttribute("nbCurrentPages",     Integer.toString(nbCurrentPages)      );
                req.setAttribute("nbMaxPages",         maxPages                              );
                req.setAttribute("release", SettingsBean.getInstance().getPropertiesFile().getProperty("release"));
                req.setAttribute("build", Integer.toString(Jahia.getBuildNumber())  );
                req.setAttribute("jahiaEdition", "na");
                req.setAttribute("licenses", null);
                JahiaAdministration.doRedirect( req, res, sess, JSP_PATH + "show_info.jsp" );

            } catch (JahiaException je) {
                logger.debug("Error while retrieving license information", je);

                String dspMsg = getMessage("org.jahia.admin.JahiaDisplayMessage.jahiaServicesProblem.label");
                sess.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                JahiaAdministration.displayMenu( req, res, sess );   // back to menu in case of problems with the Managers...
            }

        } else {
          String dspMsg = getMessage("org.jahia.admin.JahiaDisplayMessage.noServicesRegistry.label");
          sess.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
            JahiaAdministration.displayMenu( req, res, sess );   // back to menu in case of problems with the Managers...
        }

    } // end displayLicenseInfo


} // end class LicenseInfo
