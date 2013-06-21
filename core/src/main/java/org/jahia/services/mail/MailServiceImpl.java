/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.script.*;

import org.apache.camel.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.utils.Patterns;
import org.jahia.utils.ScriptEngineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.jahia.api.Constants;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.bin.listeners.JahiaContextLoaderListener.RootContextInitializedEvent;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.templates.TemplateUtils;
import org.jahia.utils.i18n.ResourceBundles;

/**
 * This service define method to send e-mails.
 * 
 * @author MAP
 * @author Serge Huber
 */
public class MailServiceImpl extends MailService implements CamelContextAware, InitializingBean, DisposableBean, ApplicationListener<ApplicationEvent> {
    
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

    private static Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);
    
    private String charset;
    
    private ProducerTemplate template;
    private ScriptEngineUtils scriptEngineUtils;

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

    public CamelContext getCamelContext() {
        return camelContext;
    }

    /**
     * Returns the settings.
     * 
     * @return the settings
     */
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
        
        long timer = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug("Sending message: " + exchange);
        }
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

    public void start() {
        // do nothing
    }

    public void stop() {
        logger.info("Mail Service successfully stopped");
    }

    @Override
    public void sendMessageWithTemplate(String template, Map<String, Object> boundObjects, String toMail,
            String fromMail, String ccList, String bcclist, Locale locale, String templatePackageName)
            throws RepositoryException, ScriptException {
        // Resolve template :
        ScriptEngine scriptEngine = scriptEngineUtils.scriptEngine(StringUtils.substringAfterLast(template, "."));
        ScriptContext scriptContext = new SimpleScriptContext();    	
    	
    	//try if it is multilingual 
        String suffix = StringUtils.substringAfterLast(template, ".");
    	String languageMailConfTemplate = template.substring(0, template.length() - (suffix.length()+1)) + "_" + locale.toString() + "." + suffix;
        String templateRealPath = TemplateUtils.lookupTemplate(templatePackageName, languageMailConfTemplate);
    	if(templateRealPath == null) {
          templateRealPath = TemplateUtils.lookupTemplate(templatePackageName, template);
    	}  
        InputStream scriptInputStream = JahiaContextLoaderListener.getServletContext().getResourceAsStream(
                templateRealPath);
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
                String subjectTemplatePath = StringUtils.substringBeforeLast(templateRealPath, ".") + ".subject."
                        + StringUtils.substringAfterLast(templateRealPath, ".");
                InputStream stream = JahiaContextLoaderListener.getServletContext().getResourceAsStream(
                        subjectTemplatePath);
                scriptContent = new InputStreamReader(stream);
                scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                scriptContext.setBindings(scriptEngine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE);
                scriptContext.setWriter(new StringWriter());
                scriptEngine.eval(scriptContent, scriptContext);
                subject = ((StringWriter) scriptContext.getWriter()).toString().trim();
            } catch (Exception e) {
                subject = resourceBundle.getString(StringUtils.substringBeforeLast(StringUtils.substringAfterLast(template, "/"), ".") + ".subject");
            } finally {
                IOUtils.closeQuietly(scriptContent);
            }
            try {
                scriptContent = new InputStreamReader(scriptInputStream);
                scriptContext.setWriter(new StringWriter());
                // The following binding is necessary for JavaScript, which
                // doesn't offer a console by default.
                bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                scriptEngine.eval(scriptContent, scriptContext);
                StringWriter writer = (StringWriter) scriptContext.getWriter();
                String body = writer.toString();
 
                sendMessage(fromMail, toMail, ccList, bcclist, subject, null, body);
            } finally {
                IOUtils.closeQuietly(scriptContent);
            }
        } else {
            logger.warn("Cannot send mail, template [" + template + "] from module [" + templatePackageName + "] not found");
        }
    }

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
            settings = JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE, new JCRCallback<MailSettings>() {
                public MailSettings doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    MailSettings cfg = new MailSettings();

                    JCRNodeWrapper mailNode = null;
                    try {
                        mailNode = session.getNode("/settings/mail-server");
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
                        + settings.getNotificationLevel() + "]");
            } else {
                settings.setConfigurationValid(false);
                logger.info("Mail settings are not set or invalid."
                        + " Mail Service will be disabled");
            }
        } else {
            logger.info("Mail Service is disabled.");
        }
        
        // backward compatibility
        settingsBean.setMail_server(settings.getUri());
        settingsBean.setMail_administrator(settings.getTo());
        settingsBean.setMail_from(settings.getFrom());
        settingsBean.setMail_service_activated(settings.isServiceActivated());
        settingsBean.setMail_paranoia(settings.getNotificationLevel());
    }

    public void store(final MailSettings cfg) {
        try {
            // store mail settings
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
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
            mailNode = session.getNode("/settings/mail-server");
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

        session.save();
    }

    public void onApplicationEvent(ApplicationEvent evt) {
        if (evt instanceof RootContextInitializedEvent || evt instanceof MailSettingsChangedEvent) {
            sendMailEndpointUri = null;
            load();
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (charset == null) {
            charset = settingsBean.getCharacterEncoding();
        } else if (charset.length() == 0) {
            charset = null;
        }
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

}