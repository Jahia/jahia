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
package org.jahia.services.notification.templates;

import groovy.lang.Binding;
import groovy.text.SimpleTemplateEngine;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.UnsupportedEncodingException;
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
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.notification.Subscription;
import org.jahia.services.preferences.user.UserPreferencesHelper;
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

    /**
     * Returns the e-mail address with the personal name of the current user (or
     * the system's default one).
     * 
     * @param ctx
     *            current processing context with the user information
     * @return the e-mail address with the personal name of the current user (or
     *         the system's default one)
     */
    public static String getSenderEmailAddress(ProcessingContext ctx) {
        String email = ServicesRegistry.getInstance().getMailService()
                .defaultSender();
        if (email.contains("<")) {
            return email;
        }
        JahiaUser user = ctx != null ? ctx.getUser() : null;
        if (user != null) {
            String name = UserPreferencesHelper.getPersonalName(user);
            JahiaSite site = ctx.getSite();
            if (site != null) {
                name = name != null ? name + " (" + site.getTitle() + ")"
                        : site.getTitle();
            }
            try {
                email = new InternetAddress(email, name, SettingsBean
                        .getInstance().getDefaultResponseBodyEncoding())
                        .toString();
            } catch (UnsupportedEncodingException e) {
                logger.warn(e.getMessage(), e);
                try {
                    email = new InternetAddress(email, name).toString();
                } catch (UnsupportedEncodingException e2) {
                    // ignore
                }
            }
        }
        return email;
    }

    private Locale preferredLocale;

    private String relativeSiteUrl;

    private String serverUrl;

    private JahiaSite site;

    protected int siteId;

    protected JahiaUser subscriber;

    protected String subscriberEmail;

    protected String templatePackageName;

    protected ProcessingContext ctx;

    /**
     * Initializes an instance of this class.
     * 
     * @param subscriber
     *            the subscriber information
     * @param siteId
     *            the site ID
     */
    public MessageBuilder(JahiaUser subscriber, int siteId) {
        this(subscriber, UserPreferencesHelper.getEmailAddress(subscriber),
                siteId, Jahia.getThreadParamBean());
    }

    /**
     * Initializes an instance of this class.
     *
     * @param subscriber
     *            the subscriber information
     * @param siteId
     *            the site ID
     */
    public MessageBuilder(JahiaUser subscriber, int siteId,String templatePackageName) {
        this(subscriber, UserPreferencesHelper.getEmailAddress(subscriber),
                siteId, Jahia.getThreadParamBean());
        this.templatePackageName = templatePackageName;
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param subscriber
     *            the subscriber information
     * @param subscriberEmail
 *            subscriber e-mail address
     * @param siteId
     * @param ctx
     */
    public MessageBuilder(JahiaUser subscriber, String subscriberEmail,
                          int siteId, ProcessingContext ctx) {
        super();
        this.subscriber = subscriber;
        this.subscriberEmail = subscriberEmail;
        this.siteId = siteId;
        this.ctx = ctx;
        resolveServerAndSiteUrl();
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
        // TODO consider page URL key
        return pageId > 0 ? getSiteUrl() + "/"
                + ProcessingContext.PAGE_ID_PARAMETER + "/" + pageId
                : getSiteUrl();
    }

    protected Locale getPreferredLocale() {
        if (preferredLocale == null) {
            preferredLocale = UserPreferencesHelper.getPreferredLocale(
                    subscriber, siteId);
        }
        return preferredLocale;
    }

    protected String getServerUrl() {
        return serverUrl;
    }

    protected JahiaSite getSite() {
        if (site == null) {
            site = TemplateUtils.getSite(siteId);
        }
        return site;
    }

    /**
     * Return the relative site URL (without scheme, server name and port).
     * 
     * @return the relative site URL (without scheme, server name and port)
     */
    protected String getSiteUrl() {
        return relativeSiteUrl;
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
            String url = getPageUrl(watchedObject.getPageID());
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
                        : UserPreferencesHelper.getPersonalizedEmailAddress(
                                subscriberEmail, subscriber)));

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
        Jahia.setThreadParamBean(ctx);
        JCRSessionFactory.getInstance().setCurrentUser(ctx.getTheUser());
        binding.setVariable("subscriber", new Subscriber(UserPreferencesHelper
                .getFirstName(subscriber), UserPreferencesHelper
                .getLastName(subscriber), UserPreferencesHelper
                .getFullName(subscriber), UserPreferencesHelper
                .getPersonalizedEmailAddress(subscriberEmail, subscriber),
                subscriber));
        binding.setVariable("locale", getPreferredLocale());
        binding.setVariable("i18n", new JahiaResourceBundle(
                getPreferredLocale(), templatePackageName));
        binding.setVariable("subscriptionManagementLink",
                getSubscriptionManagementLink());
        binding.setVariable("unsubscribeLink", getUnsubscribeLink());
        binding.setVariable("jParams", ctx);
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

    protected void resolveServerAndSiteUrl() {
        ProcessingContext ctx = Jahia.getThreadParamBean();
        String scheme = null;
        String serverName = null;
        int serverPort = 0;

        if (ctx != null) {
            scheme = ctx.getScheme();
            serverName = ctx.getServerName();
            serverPort = SettingsBean.getInstance().getSiteURLPortOverride();
            if (serverPort <= 0) {
                serverPort = ctx.getServerPort();
            }
            JahiaSite site = getSite();
            if (site != null) {
                serverName = site.getServerName();
            }
            StringBuilder url = new StringBuilder(32).append(scheme).append(
                    "://").append(serverName);
            if (serverPort != 80) {
                url.append(":").append(serverPort);
            }
            serverUrl = url.toString();
        } else {
            serverUrl = "http://localhost:8080";
            logger
                    .warn("ProcessingContext is not available."
                            + " Unable to resolve server name, port and scheme for absolute server and site links.");
        }

        StringBuilder siteUrl = new StringBuilder(32).append(
                Jahia.getContextPath()).append(Jahia.getServletPath());
        JahiaSite site = getSite();
        if (site != null && !site.isDefault()) {
            siteUrl.append("/" + ProcessingContext.SITE_KEY_PARAMETER + "/")
                    .append(site.getSiteKey());
        }
        relativeSiteUrl = siteUrl.toString();
    }

    protected void resolveTemplatePackageName() {
        JahiaSite site = getSite();
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
