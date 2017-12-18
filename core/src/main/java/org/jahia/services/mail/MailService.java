/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
 package org.jahia.services.mail;

import java.util.Locale;
import java.util.Map;

import org.jahia.registries.ServicesRegistry;
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
     * Returns an instance of the mail service.
     * 
     * @return an instance of the mail service
     */
    public static MailService getInstance() {
        return ServicesRegistry.getInstance().getMailService();
    }

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
        return getSettings() != null && getSettings().isServiceActivated()
                && getSettings().isConfigurationValid();
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

    /**
     * Persists the changes in mail server connection configuration.
     * 
     * @param cfg
     *            the new mail settings to be stored
     */
    public abstract void store(final MailSettings cfg);
}
