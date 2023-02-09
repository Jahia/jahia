/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin;

import org.jahia.api.Constants;
import org.jahia.services.mail.MailService;
import org.jahia.services.mail.MailServiceImpl;
import org.jahia.settings.SettingsBean;
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
            response.setCharacterEncoding(SettingsBean.getInstance().getCharacterEncoding());

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
                        Messages.get("resources.JahiaServerSettings",
                                "serverSettings.mailServerSettings.errors.email.to",
                                locale, "Please provide a valid administrator e-mail address"));
                return;
            }
            if (!MailService.isValidEmailAddress(from, false)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().append(
                        Messages.get("resources.JahiaServerSettings",
                                "serverSettings.mailServerSettings.errors.email.from",
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
