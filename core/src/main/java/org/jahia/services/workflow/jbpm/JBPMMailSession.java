/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.workflow.jbpm;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mail.MailEndpoint;
import org.springframework.beans.factory.DisposableBean;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.mail.MailServiceImpl;
import org.jbpm.pvm.internal.email.spi.MailSession;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

/**
 * Mail session used in the jBPM processes.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 14 sept. 2010
 */
public class JBPMMailSession implements MailSession, DisposableBean {
    private MailServiceImpl mailService;
    private ProducerTemplate template;

    public JBPMMailSession() {
        mailService = (MailServiceImpl) SpringContextSingleton.getBean("MailService");
        template = mailService.getCamelContext().createProducerTemplate();
    }

    public void send(Collection<Message> emails) {
        if (mailService.isEnabled()) {
            for (Message email : emails) {
//                Properties props = System.getProperties();
//                props.put("mail.smtp.host", "smtp.free.fr");
//                Session session = Session.getDefaultInstance(props, null);
//
//                try {
//                    Transport.send(email);
//                } catch (MessagingException e) {
//                    e.printStackTrace();
//                }
                try {
                    CamelContext context = mailService.getCamelContext();
                    MailEndpoint endpoint = (MailEndpoint) context.getEndpoint(mailService.getEndpointUri());
                    Exchange exchange = endpoint.createExchange(email);

                    // Message transformation for camel bindings
                    if (email.getContent() instanceof MimeMultipart) {
                        MimeMultipart multipart = (MimeMultipart) email.getContent();
                        for (int i = 0; i < multipart.getCount(); i++) {
                            Object content = multipart.getBodyPart(i).getContent();
                            if (content instanceof MimeMultipart) {
                                MimeMultipart alt = (MimeMultipart) content;
                                if (alt.getContentType().startsWith("multipart/alternative")) {
                                    exchange.getIn().setBody(alt.getBodyPart(0).getDataHandler().getContent());
                                    exchange.getIn().setHeader("contentType", alt.getBodyPart(0).getDataHandler().getContentType());

                                    if (alt.getCount() == 2) {
                                        exchange.getIn().setHeader("CamelMailAlternativeBody", alt.getBodyPart(1).getDataHandler().getContent());
                                    }
                                }
                            }
                        }
                        for (int i = 1 ; i<multipart.getCount(); i++) {
                            exchange.getIn().addAttachment("part"+i, multipart.getBodyPart(i).getDataHandler());
                        }
                    }
                    template.send("seda:mailUsers?multipleConsumers=true",
                            exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

	public void destroy() throws Exception {
		if (template != null) {
			template.stop();
		}
    }
}
