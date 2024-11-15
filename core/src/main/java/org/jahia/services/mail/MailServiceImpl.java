/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.mail;

import org.apache.camel.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.listeners.JahiaContextLoaderListener.RootContextInitializedEvent;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.settings.StartupOptions;
import org.jahia.utils.Patterns;
import org.jahia.utils.ScriptEngineUtils;
import org.jahia.utils.i18n.ResourceBundles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.script.*;
import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * This service define method to send e-mails.
 *
 * @author MAP
 * @author Serge Huber
 */
public class MailServiceImpl extends MailService implements CamelContextAware, InitializingBean, DisposableBean, ApplicationListener<ApplicationEvent> {

    private static final String MAIL_SERVER_SETTINGS_NODE = "/settings/mail-server";

    /**
     * This event is fired when the changes in mail server connection settings are detected (notification from other cluster nodes).
     *
     * @author Sergiy Shyrkov
     */
    public static class MailSettingsChangedEvent extends ApplicationEvent {
        private static final long serialVersionUID = 3762898577271668634L;

        public MailSettingsChangedEvent(Object source) {
            super(source);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

    private String charset;

    private ProducerTemplate template;

    private String templateCharset;

    private ScriptEngineUtils scriptEngineUtils;

    private JahiaTemplateManagerService templateManagerService;

    /**
     * Validates entered values for mail settings.
     *
     * @param cfg the mail settings, entered by user
     * @param skipIfEmpty skips the validation and returns successful result if
     *            all values are empty
     * @return the validation result object
     */
    public static MailSettingsValidationResult validateSettings(MailSettings cfg, boolean skipIfEmpty) {
        MailSettingsValidationResult result = MailSettingsValidationResult.SUCCESSFULL;
        boolean doValidation = cfg.getNotificationSeverity() != 0 || cfg.getUri().length() > 0
                || cfg.getTo().length() > 0 || cfg.getFrom().length() > 0;

        if (doValidation || !skipIfEmpty) {
            if (cfg.getUri().length() == 0) {
                result = new MailSettingsValidationResult("host", "serverSettings.mailServerSettings.errors.server.mandatory");
            } else if (cfg.getNotificationSeverity() != 0 && cfg.getTo().length() == 0) {
                result = new MailSettingsValidationResult("to", "serverSettings.mailServerSettings.errors.administrator.mandatory");
            } else if (cfg.getFrom().length() == 0) {
                result = new MailSettingsValidationResult("from", "serverSettings.mailServerSettings.errors.from.mandatory");
            } else if (cfg.getNotificationSeverity() != 0 && !MailService.isValidEmailAddress(cfg.getTo(), true)) {
                result = new MailSettingsValidationResult("to",
                        "org.jahia.admin.JahiaDisplayMessage.enterValidEmailAdmin.label");
            } else if (!MailService.isValidEmailAddress(cfg.getFrom(), false)) {
                result = new MailSettingsValidationResult("from",
                        "org.jahia.admin.JahiaDisplayMessage.enterValidEmailFrom.label");
            }
        }

        return result;
    }

    private CamelContext camelContext;

    private String mailEndpointUri;

    private String sendMailEndpointUri;

    // Mail settings
    private MailSettings settings;

    @Override
    public String defaultRecipient() {
        return settings.getTo();
    }

    @Override
    public String defaultSender() {
        return settings.getFrom();
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    /**
     * Returns the settings.
     *
     * @return the settings
     */
    @Override
    public MailSettings getSettings() {
        return settings;
    }

    @Handler
    public void handleSend(Exchange exchange) {
        if (!isEnabled()) {
            logger.warn("Mail service is not enabled. Skip sending message.");
            return;
        }

        if (charset != null && exchange.getProperty(Exchange.CHARSET_NAME) == null) {
            exchange.setProperty(Exchange.CHARSET_NAME, charset);
        }

        //if to in exchange message is set it must be initialized correctly if the To is initialized with null
        if (exchange.getIn().getHeader("From") != null && exchange.getIn().getHeader("To") == null) {
            exchange.getIn().setHeader("To", settings.getTo());
        }

        long timer = System.currentTimeMillis();
        logger.debug("Sending message: {}", exchange);
        try {
            template.send(getEndpointUri(), exchange);
        } catch (RuntimeException e) {
            logger.debug(e.getMessage(), e);
        }
        logger.info("Mail message sent in " + (System.currentTimeMillis() - timer) + " ms");
    }

    public String getEndpointUri() {
        if (sendMailEndpointUri == null) {
            StringBuilder uri = new StringBuilder();
            if (!settings.getUri().startsWith("smtp://") && !settings.getUri().startsWith("smtps://")) {
                uri.append("smtp://");
            }
            uri.append(settings.getUri());
            if (StringUtils.isNotEmpty(settings.getFrom())) {
                uri.append(uri.indexOf("?") != -1 ? "&" : "?").append("from=").append(settings.getFrom());
            }
            if (StringUtils.isNotEmpty(settings.getTo())) {
                uri.append(uri.indexOf("?") != -1 ? "&" : "?").append("to=").append(settings.getTo());
            }

            sendMailEndpointUri = uri.toString();

            logger.debug("Using mail endpoint: {}", sendMailEndpointUri);
        }

        return sendMailEndpointUri;
    }

    @Override
    public boolean sendHtmlMessage(final String from, final String to, final String cc, final String bcc,
            final String subject, final String message) {

        if (isEnabled()) {
            sendMessage(from, to, cc, bcc, subject, null, message);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean sendMessage(String message) {
        return sendMessage(settings.getFrom(), settings.getTo(), null, null, null, message);
    }

    @Override
    public boolean sendMessage(String to, String message) {
        return sendMessage(settings.getFrom(), to, null, null, null, message);
    }

    @Override
    public boolean sendMessage(String from, String to, String message) {
        return sendMessage(from, to, null, null, null, message);
    }

    @Override
    public boolean sendMessage(String from, String to, String cc, String bcc,
                               String subject, String message) {
        if (isEnabled()) {
            sendMessage(from, to, cc, bcc, subject, message, null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void sendMessage(String from, String toList, String ccList, String bcclist, String subject, String textBody,
            String htmlBody) {
        sendMessage(mailEndpointUri, from, toList, ccList, bcclist, subject, textBody, htmlBody);
    }

    public void sendMessage(String endpointUri, String from, String toList, String ccList, String bcclist, String subject, String textBody,
            String htmlBody) {
        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("To", toList);
        if (StringUtils.isEmpty(from)) {
            headers.put("From", settings.getFrom());
        } else {
            headers.put("From", from);
        }
        if (StringUtils.isNotEmpty(ccList)) {
            headers.put("Cc", ccList);
        }
        if (StringUtils.isNotEmpty(bcclist)) {
            headers.put("Bcc", bcclist);
        }
        headers.put("Subject", subject);
        final String body;
        if (StringUtils.isNotEmpty(htmlBody)) {
            headers.put("contentType", charset != null ? "text/html; charset=" + charset : "text/html");
            headers.put("alternativeBodyHeader", textBody);
            body = htmlBody;
        } else {
            headers.put("contentType", charset != null ? "text/plain; charset=" + charset : "text/plain");
            body = textBody;
        }

        template.send(endpointUri, new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                if (charset != null) {
                    exchange.setProperty(Exchange.CHARSET_NAME, charset);
                }
                Message in = exchange.getIn();
                for (Map.Entry<String, Object> header : headers.entrySet()) {
                    in.setHeader(header.getKey(), header.getValue());
                }
                in.setBody(body);
            }
        });
    }

    @Override
    public void sendMessage(MailMessage message) {
        sendMessage(mailEndpointUri, message);
    }

    /**
     * Sends the provided mail message to the specified endpoint.
     *
     * @param endpointUri the target endpoint URL to send the message
     * @param message the mail message to be sent
     */
    public void sendMessage(String endpointUri, MailMessage message) {
        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("To", message.getTo());
        headers.put("From", StringUtils.defaultIfEmpty(message.getFrom(), settings.getFrom()));
        if (StringUtils.isNotEmpty(message.getCc())) {
            headers.put("Cc", message.getCc());
        }
        if (StringUtils.isNotEmpty(message.getBcc())) {
            headers.put("Bcc", message.getBcc());
        }
        headers.put("Subject", message.getSubject());
        final String body;
        if (StringUtils.isNotEmpty(message.getHtmlBody())) {
            headers.put("contentType", charset != null ? "text/html; charset=" + charset : "text/html");
            headers.put("alternativeBodyHeader", message.getTextBody());
            body = message.getHtmlBody();
        } else {
            headers.put("contentType", charset != null ? "text/plain; charset=" + charset : "text/plain");
            body = message.getTextBody();
        }

        template.send(endpointUri, new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                if (charset != null) {
                    exchange.setProperty(Exchange.CHARSET_NAME, charset);
                }
                Message in = exchange.getIn();
                for (Map.Entry<String, Object> header : headers.entrySet()) {
                    in.setHeader(header.getKey(), header.getValue());
                }
                in.setBody(body);
                if (!message.getAttachments().isEmpty()) {
                    message.getAttachments().entrySet().stream().forEach(attachment -> {
                        in.addAttachment(attachment.getKey(), attachment.getValue());
                    });
                }
            }
        });
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
        template = camelContext.createProducerTemplate();
    }

    /**
     * Sets the URI of the default mail sending endpoint.
     *
     * @param mailEndpointUri the URI of the default mail sending endpoint
     */
    public void setMailEndpointUri(String mailEndpointUri) {
        this.mailEndpointUri = mailEndpointUri;
    }

    @Override
    public void start() {
        // do nothing
    }

    @Override
    public void stop() {
        logger.info("Mail Service successfully stopped");
    }

    @Override
    public void sendMessageWithTemplate(String template, Map<String, Object> boundObjects, String toMail,
            String fromMail, String ccList, String bcclist, Locale locale, String templatePackageName)
            throws RepositoryException, ScriptException {
        sendMessageWithTemplate(MailMessage.newMessage().to(toMail).from(fromMail).cc(ccList).bcc(bcclist).build(),
                template, boundObjects, locale, templatePackageName);
    }

    @Override
    public void sendMessageWithTemplate(MailMessage message, String template, Map<String, Object> boundObjects,
            Locale locale, String templatePackageName) throws ScriptException {
        // Resolve template :
        ScriptEngine scriptEngine = scriptEngineUtils.scriptEngine(StringUtils.substringAfterLast(template, "."));
        ScriptContext scriptContext = new SimpleScriptContext();

        //try if it is multilingual
        String suffix = StringUtils.substringAfterLast(template, ".");
        String languageMailConfTemplate = template.substring(0, template.length() - (suffix.length() + 1)) + "_" + locale.toString() + "." + suffix;
        JahiaTemplatesPackage templatePackage = templateManagerService.getTemplatePackage(templatePackageName);
        Resource templateRealPath = templatePackage.getResource(languageMailConfTemplate);
        if (templateRealPath == null) {
          templateRealPath = templatePackage.getResource(template);
        }
        InputStream scriptInputStream = null;
        try {
            scriptInputStream = templateRealPath.getInputStream();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        if (scriptInputStream != null) {
            ResourceBundle resourceBundle;
            if (templatePackageName == null) {
                String resourceBundleName = StringUtils.substringBeforeLast(
                        Patterns.SLASH.matcher(
                                StringUtils.substringAfter(Patterns.WEB_INF.matcher(template)
                                        .replaceAll(""), "/")).replaceAll("."), ".");
                resourceBundle = ResourceBundles.get(resourceBundleName, locale);
            } else {
                resourceBundle = ResourceBundles.get(ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(templatePackageName), locale);
            }
            final Bindings bindings = new SimpleBindings();
            bindings.put("bundle", resourceBundle);
            bindings.putAll(boundObjects);
            Reader scriptContent = null;
            // Subject
            String subject;
            try {
                String subjectTemplatePath = StringUtils.substringBeforeLast(template, ".") + ".subject." + StringUtils.substringAfterLast(template, ".");
                InputStream stream = templatePackage.getResource(subjectTemplatePath).getInputStream();
                scriptContent = templateCharset != null ? new InputStreamReader(stream, templateCharset) : new InputStreamReader(stream);
                scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                scriptContext.setBindings(scriptEngine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE);
                scriptContext.setWriter(new StringWriter());
                scriptEngine.eval(scriptContent, scriptContext);
                subject = scriptContext.getWriter().toString().trim();
            } catch (Exception e) {
                logger.warn("Not able to render mail subject using " + StringUtils.substringBeforeLast(template, ".") + ".subject."
                                        + StringUtils.substringAfterLast(template, ".") + " template file - set org.jahia.services.mail.MailService in debug for more information");
                logger.debug("Generating the mail subject threw an exception: {}", e);
                subject = resourceBundle.getString(StringUtils.substringBeforeLast(StringUtils.substringAfterLast(template, "/"), ".") + ".subject");
            } finally {
                IOUtils.closeQuietly(scriptContent);
            }
            try {
                try {
                    scriptContent = templateCharset != null ? new InputStreamReader(scriptInputStream, templateCharset) : new InputStreamReader(scriptInputStream);
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(e);
                }
                scriptContext.setWriter(new StringWriter());
                scriptContext.setErrorWriter(new StringWriter());
                // The following binding is necessary for JavaScript, which
                // doesn't offer a console by default.
                bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                scriptEngine.eval(scriptContent, scriptContext);
                @SuppressWarnings("resource")
                StringWriter writer = (StringWriter) scriptContext.getWriter();
                String body = writer.toString();

                message.setHtmlBody(body);
                if (subject != null) {
                    message.setSubject(subject);
                }
                sendMessage(message);
            } finally {
                IOUtils.closeQuietly(scriptContent);
            }
        } else {
            logger.warn("Cannot send mail, template [" + template + "] from module [" + templatePackageName + "] not found");
        }
    }

    @Override
    public void destroy() throws Exception {
        if (template != null) {
            template.stop();
        }
    }

    public void setScriptEngineUtils(ScriptEngineUtils scriptEngineUtils) {
        this.scriptEngineUtils = scriptEngineUtils;
    }

    protected void load() {
        sendMailEndpointUri = null;

        settings = new MailSettings();
        try {
            // read mail settings
            settings = JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, null, new JCRCallback<MailSettings>() {

                @Override
                public MailSettings doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    MailSettings cfg = new MailSettings();

                    JCRNodeWrapper mailNode = null;
                    try {
                        mailNode = session.getNode(MAIL_SERVER_SETTINGS_NODE);
                        cfg.setServiceActivated(mailNode.hasProperty("j:activated")
                                && mailNode.getProperty("j:activated").getBoolean());
                        cfg.setUri(mailNode.hasProperty("j:uri") ? mailNode.getProperty("j:uri")
                                .getString() : null);
                        cfg.setFrom(mailNode.hasProperty("j:from") ? mailNode.getProperty("j:from")
                                .getString() : null);
                        cfg.setTo(mailNode.hasProperty("j:to") ? mailNode.getProperty("j:to")
                                .getString() : null);
                        cfg.setNotificationLevel(mailNode.hasProperty("j:notificationLevel") ? mailNode
                                .getProperty("j:notificationLevel").getString() : "Disabled");
                        cfg.setWorkflowNotificationsDisabled(
                                mailNode.hasProperty("j:workflowNotificationsDisabled")
                                        ? mailNode.getProperty("j:workflowNotificationsDisabled").getBoolean()
                                        : false);
                    } catch (PathNotFoundException e) {
                        store(cfg, session);
                    }

                    return cfg;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error reading mail server settings from the repository."
                    + " Mail server will be disabled.", e);
        }

        if (settings.isServiceActivated()) {
            MailSettingsValidationResult result = validateSettings(settings, false);
            if (result.isSuccess()) {
                settings.setConfigurationValid(true);

                logger.info("Mail Service is using following settings: host=["
                        + settings.getSmtpHost() + "] to=[" + settings.getTo() + "] from=["
                        + settings.getFrom() + "] notificationLevel=["
                        + settings.getNotificationLevel() + "] workflowNotificationsDisabled=["
                        + settings.isWorkflowNotificationsDisabled() + "]");
            } else {
                settings.setConfigurationValid(false);
                logger.info("Mail settings are not set or invalid."
                        + " Mail Service will be disabled");
            }
        } else {
            logger.info("Mail Service is disabled.");
        }
    }

    @Override
    public void store(final MailSettings cfg) {
        try {
            // store mail settings
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {

                @Override
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    store(cfg, session);
                    return Boolean.TRUE;
                }
            });
            load();
        } catch (RepositoryException e) {
            logger.error("Error storing mail server settings into the repository.", e);
        }
    }

    protected void store(MailSettings cfg, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper mailNode = null;
        try {
            mailNode = session.getNode(MAIL_SERVER_SETTINGS_NODE);
        } catch (PathNotFoundException e) {
            if (session.nodeExists("/settings")) {
                mailNode = session.getNode("/settings").addNode("mail-server",
                        "jnt:mailServerSettings");
            } else {
                mailNode = session.getNode("/").addNode("settings", "jnt:globalSettings")
                        .addNode("mail-server", "jnt:mailServerSettings");
            }
        }

        mailNode.setProperty("j:activated", cfg.isServiceActivated());
        mailNode.setProperty("j:uri", cfg.getUri());
        mailNode.setProperty("j:from", cfg.getFrom());
        mailNode.setProperty("j:to", cfg.getTo());
        mailNode.setProperty("j:notificationLevel", cfg.getNotificationLevel());
        mailNode.setProperty("j:workflowNotificationsDisabled", cfg.isWorkflowNotificationsDisabled());

        session.save();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onApplicationEvent(ApplicationEvent evt) {
        if (evt instanceof RootContextInitializedEvent || evt instanceof MailSettingsChangedEvent) {
            sendMailEndpointUri = null;

            if (evt instanceof RootContextInitializedEvent
                    && (settingsBean.isStartupOptionSet(StartupOptions.OPTION_DISABLE_MAIL_SERVICE)
                        || Boolean.getBoolean(SettingsBean.JAHIA_BACKUP_RESTORE_SYSTEM_PROP))) {
                logger.info("Detected startup option for disabling mail service. Checking if the mail service needs to be disabled...");

                try {
                    JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, null,
                            new JCRCallback<Void>() {

                                @Override
                                public Void doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                    if (session.nodeExists(MAIL_SERVER_SETTINGS_NODE)) {
                                        JCRNodeWrapper node = session.getNode(MAIL_SERVER_SETTINGS_NODE);
                                        if (node.hasProperty("j:activated")
                                                && node.getProperty("j:activated").getBoolean()) {
                                            node.setProperty("j:activated", false);
                                            session.save();
                                            logger.info("Mail service successfully disabled");
                                        } else {
                                            logger.info("Mail service is already disabled");
                                        }
                                    }
                                    return null;
                                }
                            });
                } catch (RepositoryException e) {
                    logger.error("Error disabling mail service", e);
                }
            }

            load();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (charset == null) {
            charset = settingsBean.getCharacterEncoding();
        } else if (charset.length() == 0) {
            charset = null;
        }
        if (templateCharset == null) {
            templateCharset = settingsBean.getCharacterEncoding();
        } else if (templateCharset.length() == 0) {
            templateCharset = null;
        }
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    public void setTemplateCharset(String templateCharset) {
        this.templateCharset = templateCharset;
    }
}
