/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.notification.templates;

import groovy.lang.Binding;
import groovy.text.SimpleTemplateEngine;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.engines.mysettings.MySettingsEngine;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.mail.MailHelper;
import org.jahia.services.notification.Subscription;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * Creates MIME notifications messages for sending to users based on Groovy
 * templates.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class MessageBuilder implements MimeMessagePreparator {

    private static Logger logger = Logger.getLogger(MessageBuilder.class);

    public static String getPageUrl(int pageId, JahiaSite site) {
        return pageId > 0 ? getSiteUrl(site) + "/"
                + ProcessingContext.PAGE_ID_PARAMETER + "/" + pageId
                : getSiteUrl(site);
    }

    public static String getServerUrl(int siteId) {
        return getServerUrl(TemplateUtils.getSite(siteId));
    }

    public static String getServerUrl(JahiaSite site) {
        String url = "http://localhost:8080";
        if (site != null) {
            SettingsBean settings = SettingsBean.getInstance();

            url = "http://"
                    + site.getServerName()
                    + (settings.getSiteURLPortOverride() > 0 ? ":"
                            + settings.getSiteURLPortOverride() : "");
        }

        return url;
    }

    public static String getSiteUrl(JahiaSite site) {
        String url = Jahia.getContextPath() + Jahia.getServletPath();
        if (site != null) {
            if (!site.isDefault()) {
                url = url + "/" + ProcessingContext.SITE_KEY_PARAMETER + "/"
                        + site.getSiteKey();
            }
        }

        return url;
    }

    private Locale preferredLocale;

    private JahiaSite site;

    protected int siteId;

    protected JahiaUser subscriber;

    protected String subscriberEmail;

    protected String templatePackageName;

    /**
     * Initializes an instance of this class.
     * 
     * @param subscriber
     *            the subscriber information
     * @param siteId
     *            the site ID
     */
    public MessageBuilder(JahiaUser subscriber, int siteId) {
        this(subscriber, MailHelper.getEmailAddress(subscriber), siteId);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param subscriber
     *            the subscriber information
     * @param subscriberEmail
     *            subscriber e-mail address
     * @param siteId
     *            the site ID
     */
    public MessageBuilder(JahiaUser subscriber, String subscriberEmail,
            int siteId) {
        super();
        this.subscriber = subscriber;
        this.subscriberEmail = subscriberEmail;
        this.siteId = siteId;
        resolveTemplatePackageName();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param subscriber
     *            the subscriber information
     * @param subscription
     *            the subscription information
     */
    public MessageBuilder(JahiaUser subscriber, Subscription subscription) {
        this(subscriber, subscription.getSiteId());
    }

    protected String getHtmlPart(Map vars) {
        String html = null;
        if (vars.containsKey("html")) {
            html = (String) vars.get("html");
        } else {
            String htmlBodyTemplate = getTemplateHtmlPart();
            if (htmlBodyTemplate != null) {
                try {
                    html = new SimpleTemplateEngine().createTemplate(
                            Jahia.getStaticServletConfig().getServletContext()
                                    .getResource(htmlBodyTemplate)).make(
                            new HashMap(vars)).toString();
                } catch (Exception e) {
                    logger.warn("Error evaluating groovy template '"
                            + htmlBodyTemplate + "'", e);
                }
            }
        }
        return html;
    }

    protected String getPageUrl(int pageId) {
        return getPageUrl(pageId, getSite());
    }

    protected Locale getPreferredLocale() {
        if (preferredLocale == null) {
            preferredLocale = MailHelper.getPreferredLocale(subscriber, siteId);
        }
        return preferredLocale;
    }

    protected String getServerUrl() {
        return getServerUrl(getSite());
    }

    protected JahiaSite getSite() {
        if (site == null) {
            site = TemplateUtils.getSite(siteId);
        }
        return site;
    }

    protected String getSiteUrl() {
        return getSiteUrl(getSite());
    }

    protected Link getSubscriptionManagementLink() {
        Link lnk = null;
        String url = getSiteUrl() + "/"
                + ProcessingContext.ENGINE_NAME_PARAMETER + "/"
                + MySettingsEngine.ENGINE_NAME;
        lnk = new Link("subscriptionManagement", url, getServerUrl() + url);

        return lnk;
    }

    protected abstract String getTemplateHtmlPart();

    protected abstract String getTemplateMailScript();

    protected abstract String getTemplateTextPart();

    protected String getTextPart(Map vars) {
        String text = null;
        if (vars.containsKey("text")) {
            text = (String) vars.get("text");
        } else {
            String textBodyTemplate = getTemplateTextPart();
            if (textBodyTemplate != null) {
                try {
                    text = new SimpleTemplateEngine().createTemplate(
                            Jahia.getStaticServletConfig().getServletContext()
                                    .getResource(textBodyTemplate)).make(
                            new HashMap(vars)).toString();
                } catch (Exception e) {
                    logger.warn("Error evaluating groovy template '"
                            + textBodyTemplate + "'", e);
                }
            }
        }
        return text;
    }

    protected abstract Link getUnsubscribeLink();

    protected Link getWatchedContentLink(String objectKey) {
        Link lnk = null;
        ContentObject watchedObject = null;
        try {
            watchedObject = ContentObject
                    .getContentObjectInstance(ContentObjectKey
                            .getInstance(objectKey));
        } catch (ClassNotFoundException e) {
            logger.warn("Unable to retrieve content object for key '"
                    + objectKey + "'", e);
        }

        if (watchedObject != null) {
            String url = getPageUrl(watchedObject.getPageID(), getSite());
            lnk = new Link("Watched content", url, getServerUrl() + url);
        }

        return lnk;
    }

    protected String lookupTemplate(String... filePathToTry) {
        return TemplateUtils.lookupTemplate(templatePackageName, filePathToTry);
    }

    protected void populateAddresses(MimeMessage mimeMessage, Map vars)
            throws AddressException, MessagingException {
        mimeMessage.addFrom(InternetAddress
                .parse(vars.get("from") != null ? (String) vars.get("from")
                        : ServicesRegistry.getInstance().getMailService()
                                .defaultSender()));

        mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress
                .parse(vars.get("to") != null ? (String) vars.get("to")
                        : MailHelper
                        .getPersonalizedEmailAddress(subscriberEmail,
                                subscriber)));

        if (vars.get("cc") != null) {
            mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress
                    .parse((String) vars.get("cc")));
        }

        if (vars.get("bcc") != null) {
            mimeMessage.addRecipients(Message.RecipientType.BCC,
                    InternetAddress.parse((String) vars.get("bcc")));
        }

    }

    protected void populateBinding(Binding binding) {
        binding.setVariable("subscriber", new Subscriber(MailHelper
                .getFirstName(subscriber), MailHelper.getLastName(subscriber),
                MailHelper.getFullName(subscriber), MailHelper
                        .getPersonalizedEmailAddress(subscriberEmail,
                                subscriber), subscriber));
        binding.setVariable("locale", getPreferredLocale());
        binding.setVariable("i18n", new JahiaResourceBundle(getPreferredLocale(), templatePackageName));
        binding.setVariable("subscriptionManagementLink",
                getSubscriptionManagementLink());
        binding.setVariable("unsubscribeLink", getUnsubscribeLink());
    }

    public void prepare(MimeMessage mimeMessage) throws MessagingException,
            JahiaInitializationException, ResourceException, ScriptException {

        String mailTemplate = getTemplateMailScript();
        if (null == mailTemplate) {
            throw new JahiaInitializationException(
                    "Unable to find notification mail template ('"
                            + getTemplateMailScript()
                            + "'). Skip sending notification");
        }

        String charset = SettingsBean.getInstance()
                .getDefaultResponseBodyEncoding().toLowerCase();

        Binding binding = new Binding();
        runScript(mailTemplate, binding);
        Map vars = binding.getVariables();

        populateAddresses(mimeMessage, vars);

        if (vars.get("subject") != null) {
            mimeMessage.setSubject((String) vars.get("subject"), charset);
        }

        String text = getTextPart(vars);

        String html = getHtmlPart(vars);

        MimeMultipart content = new MimeMultipart("alternative");

        if (StringUtils.isNotEmpty(text)) {
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(text, charset);
            content.addBodyPart(textPart);
        }

        if (StringUtils.isNotEmpty(html)) {
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(html, "text/html;charset=" + charset);
            content.addBodyPart(htmlPart);
        }

        if (content.getCount() == 0) {
            throw new JahiaInitializationException(
                    "Unable to find neither text nor html body part of the notification e-mail. Skip sending notification");
        }

        mimeMessage.setContent(content);
    }

    /**
     * Resolves the specified path (which is related to the root folder of the
     * template set) into the actual path, considering template set inheritance.
     * 
     * @param path
     *            the resource path to resolve
     * @return the resolved path (context related) to the requested resource or
     *         <code>null</code>, if it is not found
     */
    protected String resolvePath(String path) {
        return TemplateUtils.resolvePath(path, templatePackageName);
    }

    protected void resolveTemplatePackageName() {
        JahiaSite site = TemplateUtils.getSite(siteId);
        templatePackageName = site != null ? site.getTemplatePackageName()
                : null;
    }

    protected void runScript(String mailTemplate, Binding binding)
            throws ResourceException, ScriptException {
        GroovyScriptEngine engine = (GroovyScriptEngine) SpringContextSingleton
                .getInstance().getContext().getBean("groovyScriptEngine");
        populateBinding(binding);
        String templatesPath = TemplateUtils.getTemplatesPath();
        engine.run(mailTemplate.startsWith(templatesPath) ? StringUtils
                .substringAfter(mailTemplate, templatesPath) : mailTemplate,
                binding);
    }
}
