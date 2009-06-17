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

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferOverflowException;
import org.apache.commons.collections.BufferUnderflowException;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.BoundedFifoBuffer;
import org.apache.commons.collections.buffer.UnboundedFifoBuffer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.settings.SettingsBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * <p>Title: </p>
 * <p>Description:
 * This service define method to send e-mails.
 * </p>
 *
 * <p>Copyright: MAP (Jahia Solutions S�rl 2003)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 * @author MAP
 * @version 1.0
 */
public class MailServiceImpl extends MailService {
    
    public void setMailQueueMaxLength(int mailQueueMaxLength) {
        this.mailQueueMaxLength = mailQueueMaxLength;
    }

    private class MailSender extends TimerTask {

        private Buffer queue;

        /**
         * Initializes an instance of this class.
         * 
         * @param limit
         *            the maximum length of the e-mail queue
         */
        public MailSender(int limit) {
            super();
            queue = BufferUtils
                    .synchronizedBuffer(limit > 0 ? new BoundedFifoBuffer(limit)
                            : new UnboundedFifoBuffer());
        }

        void add(Object message) {
            if (message instanceof MimeMessage
                    || message instanceof SimpleMailMessage
                    || message instanceof MimeMessagePreparator) {
                try {
                    queue.add(message);
                } catch (BufferOverflowException e) {
                    logger.warn(
                            "Unable to add e-mail message to the queue. Cause: "
                                    + e.getMessage(), e);
                }
            } else {
                throw new IllegalArgumentException(
                        "Do not know how to handle messages of type '"
                                + message.getClass().getName() + "'");
            }
        }
        
        int getQueueSize() {
            return queue.size();
        }
        
        public void run() {
            while (!queue.isEmpty()) {
                Object message = null;
                try {
                    message = queue.remove();
                } catch (BufferUnderflowException ex) {
                    // ignore it
                }
                if (message != null) {
                    long timer = System.currentTimeMillis();
                    logger.info("E-mail message found. Start sending...");
                    try {
                        if (message instanceof MimeMessage) {
                            javaMailSender.send((MimeMessage) message);
                        } else if (message instanceof SimpleMailMessage) {
                            javaMailSender.send((SimpleMailMessage) message);
                        } else if (message instanceof MimeMessagePreparator) {
                            javaMailSender
                                    .send((MimeMessagePreparator) message);
                        } else {
                            logger
                                    .warn("Do not know how to handle instances of '"
                                            + message.getClass().getName()
                                            + "'");
                        }
                    } catch (Exception ex) {
                        logger.warn("Unable to send e-mail message. Cause: "
                                + ex.getMessage(), ex);
                    }
                    logger.info("...message sent in " + (System.currentTimeMillis() - timer) + " ms");
                }
            }
        }
    }  

    private static final String MAILER = "Jahia Server v."
            + Jahia.getReleaseNumber() + "." + Jahia.getPatchNumber()
            + " build " + Jahia.getBuildNumber();

    private static final String DEF_SUBJECT = "[JAHIA] Jahia Message";

    private static final char ADDRESS_SEPARATOR = ',';

    private static Logger logger = Logger.getLogger(MailServiceImpl.class);

    // Mail settings
    private MailSettings settings;

    private JavaMailSender javaMailSender;
    
    private MailSender senderTask;
    
    private int mailQueueMaxLength;
    
    private long backgroundTaskPeriod;
    
    
    /**
     * Implementation of the JahiaInitializableService. (For future purposes)
     *
     */
    public void start() {
        settings = new MailSettings(settingsBean.mail_service_activated,
                settingsBean.mail_server, settingsBean.mail_from,
                settingsBean.mail_administrator, settingsBean.mail_paranoia);

        if (settingsBean.mail_service_activated) {
            MailSettingsValidationResult result = validateSettings(settings,
                    false);
            if (result.isSuccess()) {
                settings.setConfigugationValid(true);
                
                javaMailSender = getMailSender(settings);
                if (backgroundTaskPeriod > 0) {
                    senderTask = new MailSender(mailQueueMaxLength);
                    new Timer(true).schedule(senderTask, 60000, backgroundTaskPeriod);
                }
                logger
                        .info("Started Mail Service using folowing settings: mailhost=["
                                + settings.getSmtpHost()
                                + "] to=["
                                + settings.getTo()
                                + "] from=["
                                + settings.getFrom()
                                + "] notificationLevel=["
                                + settings.getNotificationLevel() + "]");
            } else {
                settings.setConfigugationValid(false);
                logger
                        .info("Mail settings are not set or invalid. Mail Service will be disabled");
            }
        } else {
            logger.info("Mail Service is disabled.");
        }
    }

    public static JavaMailSender getMailSender(MailSettings settings) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        
        sender.setDefaultEncoding(SettingsBean.getInstance()
                .getDefaultResponseBodyEncoding());

        sender.setHost(settings.getSmtpHost());
        int port = settings.getPort();
        if (port > 0) {
            sender.setPort(port);
        }
        String value = settings.getUser();
        if (value != null) {
            sender.setUsername(value);
        }
        value = settings.getPassword();
        if (value != null) {
            sender.setPassword(value);
        }
        Map options = settings.getOptions();
        if (!options.isEmpty()) {
            sender.getJavaMailProperties().putAll(options);
        }

        return sender;
    }

    public void stop() {
        if (senderTask != null) {
            int pendingMailCount = senderTask.getQueueSize();
            if (pendingMailCount > 0) {
                logger
                        .warn("There are still '"
                                + pendingMailCount
                                + "' e-mails pending to be sent. They will be skipped.");
            }
            senderTask.cancel();
        }
        logger.info("Mail Service successfully stopped");
    }

    public boolean sendMessage(String message) {
        return sendMessage(settings.getFrom(), settings.getTo(), null, null,
                           null, settings.getHost(), message);
    }

    public boolean sendMessage(String to, String message) {
        return sendMessage(settings.getFrom(), to, null, null,
                           null, settings.getHost(), message);
    }

    public boolean sendMessage(String from, String to, String message) {
        return sendMessage(from, to, null, null,
                           null, settings.getHost(), message);
    }

    public boolean sendMessage(String from, String to, String cc, String bcc,
                               String subject, String message) {
        return sendMessage(from, to, cc, bcc,
                           subject, settings.getHost(), message);
    }

    public boolean sendMessage(String from, String to, String cc, String bcc,
                               String subject, String mailhost, String message) {
        if (!isEnabled()) {
            return false;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            
            to = StringUtils.isNotEmpty(to) ? to : defaultRecipient();
            from = StringUtils.isNotEmpty(from) ? from : defaultSender();
            msg.setTo(to.indexOf(ADDRESS_SEPARATOR) != -1 ? StringUtils.split(
                    to, ADDRESS_SEPARATOR) : new String[] { to });
            msg.setFrom(from);

            if (logger.isDebugEnabled()) {
                logger.debug("Send mail using settings mailhost=[" + mailhost +
                             "] to=[" + to + "] from=[" + from + "]");
            }
            if (!settings.getHost().equals(mailhost)) {
                logger.warn("Specified mail host: '" + mailhost
                        + "' does not match the configured one '"
                        + settings.getHost() + "'. Using configured one.");
            }
            
            if (cc != null) {
                msg.setCc(cc.indexOf(ADDRESS_SEPARATOR) != -1 ? StringUtils.split(
                        to, ADDRESS_SEPARATOR) : new String[] { cc });
            }
            if (bcc != null) {
                msg.setBcc(bcc.indexOf(ADDRESS_SEPARATOR) != -1 ? StringUtils.split(
                        to, ADDRESS_SEPARATOR) : new String[] { bcc });
            }
            msg.setSubject(StringUtils.isNotEmpty(subject) ? subject : DEF_SUBJECT);
            msg.setText(message);
            msg.setSentDate(new Date());

            if (senderTask != null) {
                senderTask.add(msg);
            } else {
                javaMailSender.send(msg);
            }
            
            logger.debug("Mail was sent successfully.");
        } catch (Exception th) {
            logger.error("Error while sending mail : " + th.getMessage(), th);
            return false;
        }
        return true;
    }

    public boolean sendMessage(Message message) {
        if (!isEnabled()) {
            return false;
        }

        try {
            message.setHeader("X-Mailer", MAILER);
            if (senderTask != null) {
                senderTask.add(message);
            } else {
                javaMailSender.send((MimeMessage) message);
            }
        } catch (Exception e) {
            logger.warn("Couldn't send message", e);
            return false;
        }
        return true;
    }

    public synchronized boolean sendTemplateMessage(
            MimeMessagePreparator mimeMessagePreparator) {
        if (!isEnabled()) {
            return false;
        }

        try {
            if (senderTask != null) {
                senderTask.add(mimeMessagePreparator);
            } else {
                javaMailSender.send(mimeMessagePreparator);
            }
        } catch (Exception t) {
            logger.warn("Couldn't send message", t);
            return false;
        }

        return true;
    }

    public String defaultRecipient() {
        return settings.getTo();
    }

    public String defaultSender() {
        return settings.getFrom();
    }

    /**
     * Returns the settings.
     * @return the settings
     */
    public MailSettings getSettings() {
        return settings;
    }

    
    /**
     * Validates entered values for mail settings.
     * 
     * @param cfg
     *            the mail settings, entered by user
     * @param skipIfEmpty
     *            skips the validation and returns successful result if all
     *            values are empty
     * @return the validation result object
     */
    public static MailSettingsValidationResult validateSettings(MailSettings cfg, boolean skipIfEmpty) {
        MailSettingsValidationResult result = MailSettingsValidationResult.SUCCESSFULL;
        boolean doValidation = cfg.getNotificationSeverity()!=0
                || cfg.getHost().length() > 0 || cfg.getTo().length() > 0
                || cfg.getFrom().length() > 0;

        if (doValidation || !skipIfEmpty) {
            if (cfg.getHost().length() == 0) {
                result = new MailSettingsValidationResult("host",
                        "org.jahia.admin.JahiaDisplayMessage.mailServer_mustSet.label");
            } else if (cfg.getNotificationSeverity()!=0
                    && cfg.getTo().length() == 0) {
                result = new MailSettingsValidationResult("to", "org.jahia.admin.JahiaDisplayMessage.mailAdmin_mustSet.label");
            } else if (cfg.getFrom().length() == 0) {
                result = new MailSettingsValidationResult("from",
                        "org.jahia.admin.JahiaDisplayMessage.mailFrom_mustSet.label");
            } else if (cfg.getNotificationSeverity()!=0
                    && !MailService.isValidEmailAddress(cfg.getTo(), true)) {
                result = new MailSettingsValidationResult("to", "org.jahia.admin.JahiaDisplayMessage.enterValidEmailAdmin.label");
            } else if (!MailService.isValidEmailAddress(cfg.getFrom(), false)) {
                result = new MailSettingsValidationResult("from",
                        "org.jahia.admin.JahiaDisplayMessage.enterValidEmailFrom.label");
            }
        }

        return result;
    }
    
    public void setBackgroundTaskPeriod(long backgroundTaskPeriod) {
        this.backgroundTaskPeriod = backgroundTaskPeriod;
    }

}
