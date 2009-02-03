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