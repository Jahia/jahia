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
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.utils.properties.PropertiesManager;
import org.jahia.security.license.License;
import org.jahia.security.license.LicenseConstants;
import org.jahia.security.license.Limit;
import org.jahia.security.license.LicenseManager;
import org.jahia.security.license.LicensePackage;
import org.jahia.security.license.CommonDaysLeftValidator;
import org.jahia.admin.AbstractAdministrationModule;

import java.util.Date;


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
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(LicenseInfo.class);

    private static final String     CLASS_NAME              =  JahiaAdministration.CLASS_NAME;
    private static final String     JSP_PATH                =  JahiaAdministration.JSP_PATH;

    private              License                 coreLicense;



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
        coreLicense = Jahia.getCoreLicense();
        if ( coreLicense == null ){
            // set request attributes...
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.invalidLicenseKey.label",
                                               jParams!=null?jParams.getLocale():request.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            // redirect...
            JahiaAdministration.doRedirect( request, response, request.getSession(), JSP_PATH + "menu.jsp" );
            return;
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
                String unlimited = JahiaResourceBundle
                        .getJahiaInternalResource(
                                "org.jahia.admin.status.ManageStatus.unlimited.label",
                                jParams.getLocale(), "unlimited");
                int nbCurrentSites     = sReg.getJahiaSitesService().getNbSites();
                int nbMaxSites         = Jahia.getSiteLimit();
                int nbCurrentUsers     = sReg.getJahiaUserManagerService().getNbUsers();
                int nbMaxUsers         = Jahia.getUserLimit();
                int nbCurrentTemplates = sReg.getJahiaPageTemplateService().getNbPageTemplates();
                int nbMaxTemplates     = Jahia.getTemplateLimit();
                int nbCurrentPages     = sReg.getJahiaPageService().getRealActiveNbPages();
                int nbMaxPages         = Jahia.getPageLimit();

                String maxSites     = (nbMaxSites == -1)     ? unlimited : Integer.toString(nbMaxSites);
                String maxUsers     = (nbMaxUsers == -1)     ? unlimited : Integer.toString(nbMaxUsers);
                String maxTemplates = (nbMaxTemplates == -1) ? unlimited : Integer.toString(nbMaxTemplates);
                String maxPages     = (nbMaxPages == -1)     ? unlimited : Integer.toString(nbMaxPages);

                LicensePackage licensePackage = LicenseManager.getInstance().getJahiaLicensePackage();

                req.setAttribute("allowedDays", Integer.valueOf((LicenseManager.getInstance().getJahiaMaxUsageDays())));
                long expirationDate = LicenseManager.getInstance().getJahiaExpirationDate();
                if (expirationDate > 0) {
                    req.setAttribute("expirationDate", new Date(expirationDate));
                }

                req.setAttribute("nbCurrentSites",     Integer.toString(nbCurrentSites)      );
                req.setAttribute("nbMaxSites",         maxSites                              );
                req.setAttribute("nbCurrentUsers",     Integer.toString(nbCurrentUsers)      );
                req.setAttribute("nbMaxUsers",         maxUsers                              );
                req.setAttribute("nbCurrentTemplates", Integer.toString(nbCurrentTemplates)  );
                req.setAttribute("nbMaxTemplates",     maxTemplates                          );
                req.setAttribute("nbCurrentPages",     Integer.toString(nbCurrentPages)      );
                req.setAttribute("nbMaxPages",         maxPages                              );
                PropertiesManager pm = new PropertiesManager(Jahia.getJahiaPropertiesFileName());
                req.setAttribute("release", pm.getProperty("release"));
                req.setAttribute("build", Integer.toString(Jahia.getBuildNumber())  );
                req.setAttribute("jahiaEdition", licensePackage.getEdition());
                req.setAttribute("licenses", licensePackage.getLicenses());
                JahiaAdministration.doRedirect( req, res, sess, JSP_PATH + "show_info.jsp" );

            } catch (JahiaException je) {
                logger.debug("Error while retrieving license information", je);

                String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.jahiaServicesProblem.label",
                                                   jParams!=null?jParams.getLocale():req.getLocale());
                sess.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                JahiaAdministration.displayMenu( req, res, sess );   // back to menu in case of problems with the Managers...
            }

        } else {
          String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.noServicesRegistry.label",
                                             jParams!=null?jParams.getLocale():req.getLocale());
          sess.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
            JahiaAdministration.displayMenu( req, res, sess );   // back to menu in case of problems with the Managers...
        }

    } // end displayLicenseInfo


} // end class LicenseInfo
