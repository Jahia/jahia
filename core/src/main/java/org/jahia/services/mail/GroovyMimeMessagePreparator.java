/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.mail;

import groovy.lang.Binding;
import groovy.lang.MissingPropertyException;
import groovy.util.GroovyScriptEngine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * User: Serge Huber
 * Date: 10 mars 2006
 * Time: 15:17:55
 * Copyright (C) Jahia Inc.
 */
public class GroovyMimeMessagePreparator implements MimeMessagePreparator,
        InitializingBean {

    private GroovyScriptEngine groovyScriptEngine;

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GroovyMimeMessagePreparator.class);

    private String templatePath = null;

    private Binding binding;

    public void prepare(MimeMessage msg) throws Exception {
        try {

            groovyScriptEngine.run(templatePath, binding);
            //msg.setHeader("Content-Transfer-Encoding", "quoted-printable");
            // create wrapper multipart/alternative part
            MimeMultipart ma = new MimeMultipart("alternative");
            msg.setContent(ma);
            //charset should be specified in jahia.props to support exotic languages
            final String charset = "charset="+org.jahia.settings.SettingsBean.getInstance().getDefaultResponseBodyEncoding().toLowerCase();

            String textBody = (String) binding.getVariable("textBody");
            if (textBody != null) {
                // create the plain text
                BodyPart plainText = new MimeBodyPart();

                plainText.setContent(textBody,"text/plain; "+charset);
                //logger.debug("plain text message part="+ plainText.getContent());
                ma.addBodyPart(plainText);
            }

            final String htmlBody = (String) binding.getVariable("htmlBody");
            if (htmlBody != null) {
                //  create the html part
                MimeBodyPart html = new MimeBodyPart();
                new DataHandler(html,"");
                html.setDataHandler(new DataHandler(new DataSource() {
                    public String getContentType() {
                        return "text/html; "+charset;
                    }

                    public InputStream getInputStream() throws IOException {
                        //charset should be specified in jahia.props to support exotic languages
                        return new ByteArrayInputStream(htmlBody.getBytes(org.jahia.settings.SettingsBean.getInstance().getDefaultResponseBodyEncoding().toUpperCase()));
                    }

                    public String getName() {
                        return "dummy";
                    }

                    public OutputStream getOutputStream() throws IOException {
                        throw new IOException("cannot do this");
                    }
                }));
                //logger.debug("html message part="+html.getContent());
                ma.addBodyPart(html);
            }

            // set header details, we do this after, so that the groovy script can modify the data.
            try {
                String from = (String) binding.getVariable("from");
                msg.addFrom(InternetAddress.parse(from));
            } catch (MissingPropertyException mpe) {
            }
            try {
                String to = (String) binding.getVariable("to");
                msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            } catch (MissingPropertyException mpe) {
            }
            try {
                String cc = (String) binding.getVariable("cc");
                msg.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
            } catch (MissingPropertyException mpe) {
            }
            try {
                String bcc = (String) binding.getVariable("bcc");
                msg.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
            } catch (MissingPropertyException mpe) {
            }
            try {
                String subject = (String) binding.getVariable("subject");
                msg.setSubject(subject);
            } catch (MissingPropertyException mpe) {
            }
        } catch (Exception e) {
            logger.error("Error preparing the E-Mail", e);
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (groovyScriptEngine == null) {
            throw new IllegalArgumentException(
                    "Must set the velocityEngine property of "
                            + getClass().getName());
        }
    }

    public GroovyScriptEngine getGroovyScriptEngine() {
        return groovyScriptEngine;
    }

    public void setGroovyScriptEngine(GroovyScriptEngine groovyScriptEngine) {
        this.groovyScriptEngine = groovyScriptEngine;
    }

    public Binding getBinding() {
        return binding;
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }
}