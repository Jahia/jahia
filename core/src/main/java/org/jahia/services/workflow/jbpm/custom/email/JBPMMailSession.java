/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.workflow.jbpm.custom.email;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mail.MailEndpoint;
import org.jahia.services.mail.MailServiceImpl;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import javax.mail.Message;
import javax.mail.internet.MimeMultipart;
import java.util.Collection;

/**
 * Mail session used in the jBPM processes.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 14 sept. 2010
 */
public class JBPMMailSession implements DisposableBean {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JBPMMailSession.class);

    private MailServiceImpl mailService;
    private ProducerTemplate template;
    
    public boolean isEnabled() {
        return mailService.isEnabled() && !mailService.getSettings().isWorkflowNotificationsDisabled();
    }

    public void setMailService(MailServiceImpl mailService) {
        this.mailService = mailService;
        template = mailService.getCamelContext().createProducerTemplate();
    }

    public void send(Collection<Message> emails) {
        if (isEnabled()) {
            for (Message email : emails) {
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
                        for (int i = 1; i < multipart.getCount(); i++) {
                            exchange.getIn().addAttachment("part" + i, multipart.getBodyPart(i).getDataHandler());
                        }
                    }
                    template.send("seda:mailUsers?multipleConsumers=true",
                            exchange);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
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
