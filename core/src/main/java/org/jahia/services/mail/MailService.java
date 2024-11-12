/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

    /**
     * Send provided mail message.
     *
     * @param message the mail message to be sent
     * @since 7.2.3.3 / 7.3.0.1
     */
    public abstract void sendMessage(MailMessage message);

    /**
     * Send provided mail message using specified template that is responsible for producing mail subject and body.
     *
     * @param message the mail message to be sent
     * @param template the template script path
     * @param boundObjects the objects for the script engine
     * @param locale the locale to find the appropriate template script
     * @param templatePackageName the module ID to lookup i18n resources from
     * @throws ScriptException in case of an error executing template script
     * @since 7.2.3.3 / 7.3.0.1
     */
    public abstract void sendMessageWithTemplate(MailMessage message, String template, Map<String, Object> boundObjects,
            Locale locale, String templatePackageName) throws ScriptException;
}
