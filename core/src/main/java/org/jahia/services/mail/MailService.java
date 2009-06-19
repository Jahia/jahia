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
 package org.jahia.services.mail;

import org.jahia.services.JahiaService;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.Message;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * The base class for the Jahia mail service implementation.
 * User: Serge Huber
 * Date: Jul 25, 2005
 * Time: 12:22:20 PM
 * Copyright (C) Jahia Inc.
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

    /**
     * Send message to the desired destination with cc and bcc option. The
     * subject can also be mentioned. Also the
     *
     * @param from The message sender
     * @param to The message destination.
     * @param cc The message copy destination.
     * @param bcc The message copy blind destination.
     * @param subject The message subject.
     * @param mailhost A self defined mail host.
     * @param message The message to send.
     * @return True if message is sent successfully, false otherwise
     */
    public abstract boolean sendMessage(String from, String to, String cc, String bcc,
                               String subject, String mailhost, String message);

    /**
     * Send a Message type previously initialized and formated.
     *
     * @param msg The Message to send.
     * @return True if message is sent successfully, false otherwise
     */
    public abstract boolean sendMessage(Message message);

    public abstract String defaultRecipient();

    public abstract String defaultSender();

    /**
     * Advanced message sending method, that can use message preparator to generate the message
     * @param mimeMessagePreparator
     * @return true if the mail was sent, false otherwise
     */
    public abstract boolean sendTemplateMessage(MimeMessagePreparator mimeMessagePreparator);

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
}
