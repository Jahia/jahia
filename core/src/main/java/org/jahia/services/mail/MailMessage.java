/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

/**
 * Represents an e-mail message.
 * 
 * @author Sergiy Shyrkov
 *
 * @since 7.2.3.3 / 7.3.0.1
 */
public class MailMessage {

    public static class Builder {
        private MailMessage msg;

        protected Builder() {
            this.msg = new MailMessage();
        }

        public Builder attachment(String id, DataHandler handler) {
            msg.getAttachments().put(id, handler);
            return this;
        }

        public Builder attachments(Map<String, DataHandler> attachments) {
            msg.setAttachments(attachments);
            return this;
        }

        public Builder bcc(String bcc) {
            msg.setBcc(bcc);
            return this;
        }

        public MailMessage build() {
            // we return a copy
            return new MailMessage(msg);
        }

        public Builder cc(String cc) {
            msg.setCc(cc);
            return this;
        }

        public Builder from(String from) {
            msg.setFrom(from);
            return this;
        }

        public Builder htmlBody(String htmlBody) {
            msg.setHtmlBody(htmlBody);
            return this;
        }

        public Builder subject(String subject) {
            msg.setSubject(subject);
            return this;
        }

        public Builder textBody(String textBody) {
            msg.setTextBody(textBody);
            return this;
        }

        public Builder to(String to) {
            msg.setTo(to);
            return this;
        }
    }

    public static Builder newMessage() {
        return new Builder();
    }

    private Map<String, DataHandler> attachments = new HashMap<String, DataHandler>();

    private String bcc;

    private String cc;

    private String from;

    private String htmlBody;

    private String subject;

    private String textBody;

    private String to;

    public MailMessage() {
        super();
    }

    public MailMessage(MailMessage msg) {
        this();
        attachments.putAll(msg.getAttachments());
        setBcc(msg.getBcc());
        setCc(msg.getCc());
        setFrom(msg.getFrom());
        setHtmlBody(msg.getHtmlBody());
        setSubject(msg.getSubject());
        setTextBody(msg.getTextBody());
        setTo(msg.getTo());
    }

    public Map<String, DataHandler> getAttachments() {
        return attachments;
    }

    public String getBcc() {
        return bcc;
    }

    public String getCc() {
        return cc;
    }

    public String getFrom() {
        return from;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public String getSubject() {
        return subject;
    }

    public String getTextBody() {
        return textBody;
    }

    public String getTo() {
        return to;
    }

    public void setAttachments(Map<String, DataHandler> attachments) {
        this.attachments = attachments != null ? attachments : new HashMap<>();
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTextBody(String textBody) {
        this.textBody = textBody;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
