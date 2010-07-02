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
package org.jahia.services.notification;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.notification.templates.TemplateUtils;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.RepositoryException;
import javax.script.*;
import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Notification service, based on the Apache Camel framework, for sending different kinds of notifications.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 28 juin 2010
 */
public class CamelNotificationService implements CamelContextAware {
    private transient static Logger logger = Logger.getLogger(CamelNotificationService.class);

    private CamelContext camelContext;

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public void sendMessagesWithBodyAndHeaders(String target, Object body, Map<String, Object> headers) {
        ProducerTemplate template = camelContext.createProducerTemplate();
        if (headers != null) {
            template.sendBodyAndHeaders(target, body, headers);
        } else {
            template.sendBody(target, body);
        }
    }

    public void registerRoute(RoutesBuilder routesBuilder) throws Exception {
        camelContext.addRoutes(routesBuilder);
    }

    public void sendMail(String camelURI, String subject, String htmlBody, String textBody, String from, String toList,
                         String ccList, String bcclist) {
        ProducerTemplate template = camelContext.createProducerTemplate();
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("To", toList);
        if ("".equals(from)) {
            headers.put("From", SettingsBean.getInstance().getMail_from());
        } else {
            headers.put("From", from);
        }
        if (null != ccList && !"".equals(ccList)) {
            headers.put("Cc", ccList);
        }
        if (null != bcclist && !"".equals(bcclist)) {
            headers.put("Bcc", bcclist);
        }
        headers.put("Subject", subject);
        String body;
        if (null != htmlBody && !"".equals(htmlBody)) {
            headers.put("contentType", "text/html");
            headers.put("alternativeBodyHeader", textBody);
            body = htmlBody;
        } else {
            headers.put("contentType", "text/plain");
            body = textBody;
        }
        template.sendBodyAndHeaders(camelURI, body, headers);
    }

    public void sendMailWithTemplate(String template, Map<String, Object> bindedObjects, String toMail, String fromMail,
                                     String ccList, String bcclist, Locale locale, String templatePackageName)
            throws RepositoryException, ScriptException {
        // Resolve template :
        ScriptEngineManager scriptManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptManager.getEngineByExtension(StringUtils.substringAfterLast(template, "."));
        ScriptContext scriptContext = scriptEngine.getContext();
        String templateRealPath = TemplateUtils.lookupTemplate(templatePackageName, template);
        InputStream scriptInputStream = JahiaContextLoaderListener.getServletContext().getResourceAsStream(
                templateRealPath);
        if (scriptInputStream != null) {
            ResourceBundle resourceBundle;
            if (templatePackageName == null) {
                String resourceBundleName = StringUtils.substringBeforeLast(StringUtils.substringAfter(template,
                                                                                                       "/").replaceAll(
                        "/", "."), ".");
                resourceBundle = ResourceBundle.getBundle(resourceBundleName, locale);
            } else {
                resourceBundle = new JahiaResourceBundle(locale, templatePackageName);
            }
            final Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("bundle", resourceBundle);
            bindings.putAll(bindedObjects);
            Reader scriptContent = null;
            // Subject
            String subject;
            try {
                String subjectTemplatePath = StringUtils.substringBeforeLast(templateRealPath,
                                                                             ".") + ".subject." + StringUtils.substringAfterLast(
                        templateRealPath, ".");
                InputStream stream = JahiaContextLoaderListener.getServletContext().getResourceAsStream(
                        subjectTemplatePath);
                scriptContent = new InputStreamReader(stream);
                scriptContext.setWriter(new StringWriter());
                scriptEngine.eval(scriptContent, bindings);
                subject = ((StringWriter) scriptContext.getWriter()).toString().trim();
            } catch (Exception e) {
                subject = resourceBundle.getString("subject");
            }
            try {
                scriptContent = new InputStreamReader(scriptInputStream);
                scriptContext.setWriter(new StringWriter());
                // The following binding is necessary for Javascript, which doesn't offer a console by default.
                bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                scriptEngine.eval(scriptContent, bindings);
                StringWriter writer = (StringWriter) scriptContext.getWriter();
                String body = writer.toString();

                sendMail("seda:users?multipleConsumers=true", subject, body, null, fromMail, toMail, ccList, bcclist);
            } finally {
                if (scriptContent != null) {
                    IOUtils.closeQuietly(scriptContent);
                }
            }
        }
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }
}
