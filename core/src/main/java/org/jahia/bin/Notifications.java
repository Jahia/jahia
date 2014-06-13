/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.bin;

import org.jahia.api.Constants;
import org.jahia.services.mail.MailService;
import org.jahia.services.mail.MailServiceImpl;
import org.jahia.utils.i18n.Messages;
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
                    Constants.SESSION_UI_LOCALE);
            locale = locale != null ? locale : request.getLocale();

            if (logger.isDebugEnabled()) {
                logger.debug("Request received for sending test e-mail from '{}' "
                        + "to '{}' using configuration '{}'", new String[] { from, to, host });
            }

            if (!MailService.isValidEmailAddress(to, true)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().append(
                        Messages.getInternal(
                                "org.jahia.admin.JahiaDisplayMessage.enterValidEmailAdmin.label",
                                locale, "Please provide a valid administrator e-mail address"));
                return;
            }
            if (!MailService.isValidEmailAddress(from, false)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().append(
                        Messages.get("resources.JahiaServerSettings",
                                "org.jahia.admin.JahiaDisplayMessage.enterValidEmailFrom.label",
                                locale, "Please provide a valid sender e-mail address"));
                return;
            }

            String subject = Messages.get("resources.JahiaServerSettings",
                    "serverSettings.mailServerSettings.testSettings.mailSubject", locale,
                    "[Jahia] Test message");
            String text = Messages.get("resources.JahiaServerSettings",
                    "serverSettings.mailServerSettings.testSettings.mailText", locale,
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
