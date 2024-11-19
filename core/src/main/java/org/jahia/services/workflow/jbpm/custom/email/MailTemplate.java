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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

public class MailTemplate {

    private AddressTemplate from;
    private AddressTemplate to;
    private AddressTemplate cc;
    private AddressTemplate bcc;
    private String subject;
    private String text;
    private String html;
    private String language;
    private List<AttachmentTemplate> attachmentTemplates = new ArrayList<AttachmentTemplate>();

    public AddressTemplate getFrom() {
        return from;
    }

    public void setFrom(AddressTemplate from) {
        this.from = from;
    }

    public AddressTemplate getTo() {
        return to;
    }

    public void setTo(AddressTemplate to) {
        this.to = to;
    }

    public AddressTemplate getCc() {
        return cc;
    }

    public void setCc(AddressTemplate cc) {
        this.cc = cc;
    }

    public AddressTemplate getBcc() {
        return bcc;
    }

    public void setBcc(AddressTemplate bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<AttachmentTemplate> getAttachmentTemplates() {
        return attachmentTemplates;
    }

    public boolean addAttachmentTemplate(AttachmentTemplate attachmentTemplate) {
        return attachmentTemplates.add(attachmentTemplate);
    }
}
