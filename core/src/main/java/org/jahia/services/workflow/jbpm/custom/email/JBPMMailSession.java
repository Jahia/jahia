/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
        return mailService.isEnabled();
    }

    public void setMailService(MailServiceImpl mailService) {
        this.mailService = mailService;
        template = mailService.getCamelContext().createProducerTemplate();
    }

    public void send(Collection<Message> emails) {
        if (mailService.isEnabled()) {
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
