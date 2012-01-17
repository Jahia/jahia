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

package org.jahia.bin;

import org.jahia.params.ProcessingContext;
import org.jahia.services.mail.MailService;
import org.jahia.services.mail.MailServiceImpl;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * Performs various notification-related tasks.
 * 
 * @author Sergiy Shyrkov
 */
public class Notifications extends JahiaMultiActionController {

    private static Logger logger = LoggerFactory.getLogger(Notifications.class);

    private MailServiceImpl mailService;

    private void sendEmail(String host, String from, String to, String subject, String text) {
        mailService.sendMessage(
                (!host.startsWith("smtp://") && !host.startsWith("smtps://") ? "smtp://" : "")
                        + host, from, to, null, null, subject, text, null);
    }

    public void setMailService(MailServiceImpl mailService) {
        this.mailService = mailService;
    }

    public void testEmail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            checkUserAuthorized();

            String host = getParameter(request, "host");
            String from = getParameter(request, "from");
            String to = getParameter(request, "to");

            Locale locale = (Locale) request.getSession(true).getAttribute(
                    ProcessingContext.SESSION_UI_LOCALE);
            locale = locale != null ? locale : request.getLocale();

            if (logger.isDebugEnabled()) {
                logger.debug("Request received for sending test e-mail from '{}' "
                        + "to '{}' using configuration '{}'", new String[] { from, to, host });
            }

            if (!MailService.isValidEmailAddress(to, true)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().append(
                        JahiaResourceBundle.getJahiaInternalResource(
                                "org.jahia.admin.JahiaDisplayMessage.enterValidEmailAdmin.label",
                                locale, "Please provide a valid administrator e-mail address"));
                return;
            }
            if (!MailService.isValidEmailAddress(from, false)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().append(
                        JahiaResourceBundle.getJahiaInternalResource(
                                "org.jahia.admin.JahiaDisplayMessage.enterValidEmailFrom.label",
                                locale, "Please provide a valid sender e-mail address"));
                return;
            }

            String subject = JahiaResourceBundle.getJahiaInternalResource(
                    "org.jahia.admin.server.ManageServer.testSettings.mailSubject", locale,
                    "[Jahia] Test message");
            String text = JahiaResourceBundle.getJahiaInternalResource(
                    "org.jahia.admin.server.ManageServer.testSettings.mailText", locale,
                    "Test message");

            sendEmail(host, from, to, subject, text);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            logger.warn("Error sending test e-mail message. Cause: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            if(response.getWriter()!=null) {
                if(e.getCause()!=null) {
                    response.getWriter().append(e.getCause().getMessage());
                } else {
                    response.getWriter().append(e.getMessage());
                }
            }
        }
    }
}
