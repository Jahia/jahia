/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

// $Id$
//
//  ManageServer
//
//  31.03.2001  AK  added in jahia.
//

package org.jahia.admin.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.springframework.web.util.WebUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.jahia.bin.JahiaAdministration;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.mail.MailService;
import org.jahia.services.mail.MailServiceImpl;
import org.jahia.services.mail.MailSettings;
import org.jahia.services.mail.MailSettingsValidationResult;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.properties.PropertiesManager;
import org.jahia.admin.AbstractAdministrationModule;


/**
 * desc:  This class is used by the administration to manage the
 * server settings of a jahia portal, like the mail notification service (when
 * jahia or a user generate error(s), or like the java server home disk path,
 * mail server, etc.
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Alexandre Kraft
 * @version 1.0
 */
public class ManageServer extends AbstractAdministrationModule {

    private static final String CLASS_NAME  =  JahiaAdministration.CLASS_NAME;
    private static final String JSP_PATH    =  JahiaAdministration.JSP_PATH;

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(ManageServer.class);

    /**
     * This method is used like a dispatcher for user requests.
     * @author  Alexandre Kraft
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     */
    public void service( HttpServletRequest    request,
                                        HttpServletResponse   response )
    throws Exception
    {
        String operation =  request.getParameter("sub");

        if(operation.equals("display")) {
            displaySettings( request, response, request.getSession() );
        } else if(operation.equals("process")) {
            processSettings( request, response, request.getSession() );
        }
    } // userRequestDispatcher



    /**
     * Display the server settings page, using doRedirect().
     * @author  Alexandre Kraft
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void displaySettings( HttpServletRequest    request,
                                  HttpServletResponse   response,
                                  HttpSession           session )
    throws IOException, ServletException
    {
        // retrieve previous form values...
        MailSettings cfg = "process".equals(request.getParameter("sub")) ? (MailSettings) session
                .getAttribute(CLASS_NAME + "jahiaMailSettings")
                : null;
        if (cfg == null) {
            cfg = ServicesRegistry.getInstance().getMailService().getSettings();
            session.setAttribute(CLASS_NAME + "jahiaMailSettings", cfg);
        }

        // set request attributes...
        request.setAttribute("jahiaMailSettings", cfg);

        JahiaAdministration.doRedirect( request, response, session, JSP_PATH + "config_server.jsp" );
    } // end displaySettings



    /**
     * Process and check the validity of the server settings page. If they are
     * not valid, display the server settings page to the user.
     * @author  Alexandre Kraft
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void processSettings( HttpServletRequest   request,
                                  HttpServletResponse  response,
                                  HttpSession          session )
    throws IOException, ServletException
    {
        // get form values...
        MailSettings cfg = new MailSettings();
        try {
        	BeanUtils.populate(cfg, WebUtils.getParametersStartingWith(request, ""));
        } catch (Exception e) {
            throw new ServletException("BeanUtils.populate", e);
        }
        session.setAttribute(CLASS_NAME + "jahiaMailSettings", cfg);
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        MailSettingsValidationResult result = MailServiceImpl.validateSettings(cfg, true);
        if (!result.isSuccess()) {
            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage",
                    getMessage(result.getMessageKey()));
        } else {
            storeSettings(cfg, jParams, request);
        }
        displaySettings( request, response, session );
    } // end processSettings



    /**
     * Store new settings for the mail server.
     * 
     * @param cfg
     *            mail settings
     * @param jParams
     *            processing context
     * @param request
     *            current request
     */
    private void storeSettings(MailSettings cfg, ProcessingContext jParams,
            HttpServletRequest request) throws IOException, ServletException {
        SettingsBean settings = SettingsBean.getInstance();

        // set new values in the properties manager...
        PropertiesManager properties = new PropertiesManager(JahiaContextLoaderListener.getServletContext().getRealPath(SettingsBean.JAHIA_PROPERTIES_FILE_PATH));
        properties.setProperty("mail_service_activated", cfg.isServiceActivated() ? "true" : "false");
        properties.setProperty("mail_server", cfg.getHost());
        properties.setProperty("mail_administrator", cfg.getTo());
        properties.setProperty("mail_from", cfg.getFrom());
        properties.setProperty("mail_paranoia", cfg.getNotificationLevel());

        // write in the jahia properties file...
        properties.storeProperties();

        settings.setMail_service_activated(cfg.isServiceActivated());
        settings.setMail_server(cfg.getHost());
        settings.setMail_administrator(cfg.getTo());
        settings.setMail_from(cfg.getFrom());
        settings.setMail_paranoia(cfg.getNotificationLevel());

        // restart the mail service
        MailService mailSrv = ServicesRegistry.getInstance().getMailService();
        try {
            mailSrv.stop();
            mailSrv.start();
            request.setAttribute("jahiaDisplayInfo", getMessage(
                            "label.changeSaved"));
        } catch (JahiaException e) {
            logger
                    .error(
                            "Unable to restart Mail Service."
                                    + " New mail settings will be taken into consideration after server restart",
                            e);
            request
                    .setAttribute(
                            "jahiaDisplayMessage",
                            getMessage("org.jahia.admin.JahiaDisplayMessage.restartJahiaAfterChange.label"));
        }
    }
}
