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

 package org.jahia.services.mail;

import java.util.Locale;
import java.util.Map;

import org.jahia.services.JahiaService;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.jcr.RepositoryException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.script.ScriptException;

/**
 * Jahia mail service implementation.
 * 
 * @author Serge Huber
 * Date: Jul 25, 2005
 * Time: 12:22:20 PM
 */
public abstract class MailService extends JahiaService {
    /**
     * Send message to the default Jahia settings defined in the jahia.properties
     * file.
     *
     * @param message The message to send
     * @return True if message is sent successfully, false otherwise
     */
    public abstract boolean sendMessage(String message);

    /**
     * Send message to the desired destination.
     *
     * @param to The message destination.
     * @param message The message to send.
     * @return True if message is sent successfully, false otherwise
     */
    public abstract boolean sendMessage(String to, String message);

    /**
     * Send message to the desired destination.
     *
     * @param from The message sender
     * @param to The message destination.
     * @param message The message to send.
     * @return True if message is sent successfully, false otherwise
     */
    public abstract boolean sendMessage(String from, String to, String message);

    /**
     * Send message to the desired destination with cc and bcc option. The
     * subject can also be mentioned.
     *
     * @param from The message sender
     * @param to The message destination.
     * @param cc The message copy destination.
     * @param bcc The message copy blind destination.
     * @param subject The message subject.
     * @param message The message to send.
     * @return True if message is sent successfully, false otherwise
     */
    public abstract boolean sendMessage(String from, String to, String cc, String bcc,
                               String subject, String message);

    /**
     * Send message to the desired destination with cc and bcc option. The
     * subject can also be mentioned.
     *
     * @param from The message sender
     * @param to The message destination.
     * @param cc The message copy destination.
     * @param bcc The message copy blind destination.
     * @param subject The message subject.
     * @param textBody The text message to send
     * @param htmlBody The HTML message to send
     * @return True if message is sent successfully, false otherwise
     */
    public abstract void sendMessage(String from, String to, String cc, String bcc,
            String subject, String textBody,
            String htmlBody);
    
    /**
     * Send message in the HTML format to the desired destination with cc and bcc option. The
     * subject can also be mentioned.
     *
     * @param from The message sender
     * @param to The message destination.
     * @param cc The message copy destination.
     * @param bcc The message copy blind destination.
     * @param subject The message subject.
     * @param message The message to send.
     * @return True if message is sent successfully, false otherwise
     */
    public abstract boolean sendHtmlMessage(String from, String to, String cc, String bcc,
                               String subject, String message);

    public abstract String defaultRecipient();

    public abstract String defaultSender();

    /**
     * Returns <code>true</code> if the mail service is enabled and settings
     * are valid.
     * 
     * @return <code>true</code> if the mail service is enabled and settings
     *         are valid
     */
    public boolean isEnabled() {
        return getSettings().isServiceActivated()
                && getSettings().isConfigugationValid();
    }

    /**
     * Returns mail configuration settings.
     * 
     * @return mail configuration settings
     */
    public abstract MailSettings getSettings();

    /**
     * Checks, if the specified string is a valid e-mail address according to
     * RFC822.
     * 
     * @param address
     *            the address to be checked
     * @param allowMultiple
     *            are multiple addresses allowed (separated by comma)
     * @return <code>true</code>, if the specified string is a valid e-mail
     *         address according to RFC822
     */
    public static boolean isValidEmailAddress(String address,
            boolean allowMultiple) {
        InternetAddress[] addr = null;
        try {
            if (allowMultiple) {
                addr = InternetAddress.parse(address, true);
                for (InternetAddress internetAddress : addr) {
                    internetAddress.validate();
                }
            } else {
                addr = new InternetAddress[] { new InternetAddress(address,
                        true) };
            }
        } catch (AddressException e) {
            // address is not valid
            addr = null;
        }
        return addr != null;
    }

    public void sendMessage(MimeMessagePreparator mimeMessagePreparator) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public abstract void sendMessageWithTemplate(String template, Map<String, Object> boundObjects, String toMail,
            String fromMail, String ccList, String bcclist, Locale locale, String templatePackageName)
            throws RepositoryException, ScriptException;
}
